package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.Sensor;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataIngestionService {

    private final MinioService minioService;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Transactional(readOnly = true)
    public void processTelemetryData(Message<?> message) {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();

        try {
            // 1. Phân tích Topic
            String[] parts = topic.split("/");
            if (parts.length < 3) {
                log.warn("Invalid topic format: {}", topic);
                return;
            }
            String uniqueId = parts[2];

            // 2. Tìm thiết bị trong DB
            Optional<Device> deviceOpt = deviceRepository.findByUniqueIdentifier(uniqueId);

            if (deviceOpt.isEmpty()) {
                log.warn("Device not found with Identifier: {}", uniqueId);
                return;
            }

            Device device = deviceOpt.get();

            // 3. Parse dữ liệu payload gốc
            JsonNode rawDataNode;
            try {
                rawDataNode = objectMapper.readTree(payload);
            } catch (Exception e) {
                log.error("Failed to parse JSON payload from device {}: {}", uniqueId, payload);
                return;
            }

            // Chỉ giữ lại các trường có trong danh sách Sensor của thiết bị
            ObjectNode filteredData = objectMapper.createObjectNode();
            List<Sensor> deviceSensors = device.getSensors();

            // Map để tra cứu nhanh: "temperature" -> Sensor Entity
            Map<String, Sensor> sensorMap = new HashMap<>();
            for (Sensor s : deviceSensors) {
                if (s.getProperty() != null) {
                    sensorMap.put(s.getProperty().getName().toLowerCase(), s);
                }
            }

            String finalLabel = "NORMAL";
            List<String> reasons = new ArrayList<>();

            Iterator<String> fieldNames = rawDataNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                String keyLower = key.toLowerCase();

                // KIỂM TRA QUAN TRỌNG: Key gửi lên có nằm trong cấu hình thiết bị không?
                if (sensorMap.containsKey(keyLower)) {
                    JsonNode valueNode = rawDataNode.get(key);

                    // Thêm vào dữ liệu đã lọc (Giữ nguyên key gốc để đẹp đội hình)
                    filteredData.set(key, valueNode);

                    // --- Logic Gán nhãn (Di chuyển vào đây để dùng luôn) ---
                    if (valueNode.isNumber()) {
                        double value = valueNode.asDouble();
                        Sensor matchedSensor = sensorMap.get(keyLower);
                        BigDecimal val = BigDecimal.valueOf(value);

                        if (matchedSensor.getThresholdCritical() != null
                                && val.compareTo(matchedSensor.getThresholdCritical()) >= 0) {
                            finalLabel = "CRITICAL";
                            reasons.add(String.format("[%s] %s (%.2f) >= Critical (%.2f)",
                                    matchedSensor.getName(), key, val, matchedSensor.getThresholdCritical()));
                        }
                        else if (!"CRITICAL".equals(finalLabel)
                                && matchedSensor.getThresholdWarning() != null
                                && val.compareTo(matchedSensor.getThresholdWarning()) >= 0) {
                            finalLabel = "WARNING";
                            reasons.add(String.format("[%s] %s (%.2f) >= Warning (%.2f)",
                                    matchedSensor.getName(), key, val, matchedSensor.getThresholdWarning()));
                        }
                    }
                } else {
                    // Log cảnh báo nếu thiết bị gửi trường lạ (Optional)
                    log.debug("Ignored unknown property '{}' from device '{}'", key, uniqueId);
                }
            }

            // 5. Nếu sau khi lọc mà không còn dữ liệu nào -> Bỏ qua, không lưu
            if (filteredData.isEmpty()) {
                log.warn("Payload rejected: No valid properties found matching device configuration. Device: {}", uniqueId);
                return;
            }

            // 6. Lưu xuống Data Lake (Sử dụng filteredData thay vì rawDataNode)
            saveToDataLake(device, filteredData, finalLabel, reasons, uniqueId);

        } catch (Exception e) {
            log.error("Unexpected error processing AI data", e);
        }
    }


    // Hàm tìm Sensor khớp key
    private Sensor findSensorByKey(Device device, String key) {
        return device.getSensors().stream()
                .filter(s -> s.getProperty() != null && s.getProperty().getName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    private void saveToDataLake(Device device, JsonNode data, String label, List<String> reasons, String uniqueId) {
        try {
            ObjectNode record = objectMapper.createObjectNode();

            // --- Metadata ---
            ObjectNode metadata = record.putObject("metadata");
            metadata.put("device_id", device.getId());
            metadata.put("device_identifier", uniqueId);
            metadata.put("device_name", device.getName());
            metadata.put("device_type", device.getDeviceType() != null ? device.getDeviceType().getName() : "unknown");
            metadata.put("timestamp", LocalDateTime.now().toString());

            metadata.put("location_province", device.getProvince() != null ? device.getProvince() : "N/A");
            metadata.put("location_district", device.getDistrict() != null ? device.getDistrict() : "N/A");
            metadata.put("location_ward", device.getWard() != null ? device.getWard() : "N/A");
            metadata.put("location_specific", device.getLocation() != null ? device.getLocation() : "N/A");

            // --- Features (Dữ liệu đã lọc) ---
            record.set("features", data);

            // --- Units (Lưu đơn vị đo) ---
            ObjectNode unitsNode = objectMapper.createObjectNode();
            Iterator<String> fieldNames = data.fieldNames();

            while (fieldNames.hasNext()) {
                String key = fieldNames.next();

                // Tìm lại sensor để lấy Unit
                Sensor s = findSensorByKey(device, key);
                String unit = (s != null && s.getProperty() != null) ? s.getProperty().getUnit() : "";

                unitsNode.put(key, unit);
            }
            record.set("units", unitsNode);
            // ----------------------------------------------------

            record.put("label", label);

            if (!reasons.isEmpty()) {
                record.put("label_reason", String.join("; ", reasons));
            }

            LocalDateTime now = LocalDateTime.now();
            String path = String.format("telemetry/%s/%d/%02d/%02d/%s_%s.json",
                    uniqueId,
                    now.getYear(),
                    now.getMonthValue(),
                    now.getDayOfMonth(),
                    label,
                    System.currentTimeMillis()
            );

            minioService.uploadJson(path, objectMapper.writeValueAsString(record));

            log.info(">> AI Data Ingested: Label=[{}] | Device=[{}] | Path=[{}]", label, uniqueId, path);

        } catch (Exception e) {
            log.error("Failed to save data to Data Lake", e);
        }
    }
}
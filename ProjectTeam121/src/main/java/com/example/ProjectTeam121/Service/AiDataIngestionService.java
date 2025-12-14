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
            // Lưu ý: Nếu có method findByUniqueIdentifier trong repo thì nên dùng để tối ưu hơn stream()
            Optional<Device> deviceOpt = deviceRepository.findAll().stream()
                    .filter(d -> d.getUniqueIdentifier().equals(uniqueId))
                    .findFirst();

            if (deviceOpt.isEmpty()) {
                log.warn("Device not found with Identifier: {}", uniqueId);
                return;
            }

            Device device = deviceOpt.get();

            // 3. Xác định danh sách Sensor mục tiêu
            List<Sensor> targetSensors = determineTargetSensors(device);

            // 4. Parse dữ liệu payload
            JsonNode dataNode;
            try {
                dataNode = objectMapper.readTree(payload);
            } catch (Exception e) {
                log.error("Failed to parse JSON payload from device {}: {}", uniqueId, payload);
                return;
            }

            // 5. Logic Gán nhãn
            String finalLabel = "NORMAL";
            List<String> reasons = new ArrayList<>();

            Iterator<String> fieldNames = dataNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                if (!dataNode.get(key).isNumber()) continue; // Bỏ qua nếu không phải số

                double value = dataNode.get(key).asDouble();

                Sensor matchedSensor = targetSensors.stream()
                        .filter(s -> isSensorMatchingKey(s, key))
                        .findFirst()
                        .orElse(null);

                if (matchedSensor != null) {
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
            }

            // 6. Lưu xuống Data Lake
            saveToDataLake(device, dataNode, finalLabel, reasons, uniqueId);

        } catch (Exception e) {
            log.error("Unexpected error processing AI data", e);
        }
    }

    /**
     * Logic xác định Sensor chính: CHỈ DỰA VÀO TRƯỜNG primaryProperty CỦA DEVICE
     */
    private List<Sensor> determineTargetSensors(Device device) {
        // 1. Kiểm tra xem Device có chọn Primary Property không
        if (device.getPrimaryProperty() != null) {
            String targetPropId = device.getPrimaryProperty().getId();

            // Lọc ra các sensor đo thuộc tính này
            List<Sensor> mainSensors = device.getSensors().stream()
                    .filter(s -> s.getProperty() != null &&
                            s.getProperty().getId().equals(targetPropId))
                    .collect(Collectors.toList());

            if (!mainSensors.isEmpty()) {
                return mainSensors;
            }
        }

        // 2. Fallback: Nếu không chọn gì, check hết (An toàn)
        return new ArrayList<>(device.getSensors());
    }

    private boolean isSensorMatchingKey(Sensor sensor, String jsonKey) {
        if (sensor.getProperty() != null) {
            String propName = sensor.getProperty().getName();
            return propName.equalsIgnoreCase(jsonKey)
                    || propName.toLowerCase().contains(jsonKey.toLowerCase())
                    || jsonKey.toLowerCase().contains(propName.toLowerCase());
        }
        return false;
    }

    private void saveToDataLake(Device device, JsonNode data, String label, List<String> reasons, String uniqueId) {
        try {
            ObjectNode record = objectMapper.createObjectNode();

            ObjectNode metadata = record.putObject("metadata");
            metadata.put("device_id", device.getId());
            metadata.put("device_identifier", uniqueId);
            metadata.put("device_name", device.getName());
            // Vẫn lấy tên device type để phân thư mục, nhưng không dùng để check logic
            metadata.put("device_type", device.getDeviceType() != null ? device.getDeviceType().getName() : "unknown");
            metadata.put("timestamp", LocalDateTime.now().toString());

            // Thêm đầy đủ thông tin vị trí
            metadata.put("location_province", device.getProvince() != null ? device.getProvince() : "N/A");
            metadata.put("location_district", device.getDistrict() != null ? device.getDistrict() : "N/A");
            metadata.put("location_ward", device.getWard() != null ? device.getWard() : "N/A");
            metadata.put("location_specific", device.getLocation() != null ? device.getLocation() : "N/A");
            // ---------------------------------------

            record.set("features", data);
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

            log.info(">> AI Data Ingested: Label=[{}] | Device=[{}] | Location=[{}, {}] | Path=[{}]",
                    label, uniqueId, device.getDistrict(), device.getProvince(), path);

        } catch (Exception e) {
            log.error("Failed to save data to Data Lake", e);
        }
    }
}
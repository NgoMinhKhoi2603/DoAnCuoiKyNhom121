package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.Sensor;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.utils.DataValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDataIngestionService {

    private final MinioService minioService;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;
    private final DataValidator dataValidator;

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

            // --- LẤY ID CỦA PROPERTY CHÍNH (Nếu có) ---
            String primaryPropId = (device.getPrimaryProperty() != null)
                    ? device.getPrimaryProperty().getId()
                    : null;
            // -----------------------------------------------

            String finalLabel = "NORMAL";
            List<String> reasons = new ArrayList<>();

            Iterator<String> fieldNames = rawDataNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                String keyLower = key.toLowerCase();

                // KIỂM TRA: Key có nằm trong danh sách Sensor của thiết bị không?
                if (sensorMap.containsKey(keyLower)) {
                    JsonNode valueNode = rawDataNode.get(key);
                    Sensor matchedSensor = sensorMap.get(keyLower);

                    // =================================================================
                    // 4. KIỂM TRA DATA TYPE (VALIDATION) - Mới thêm
                    // =================================================================
                    PropertyDataType expectedType = matchedSensor.getProperty().getDataType();
                    String valueAsString = valueNode.asText();

                    if (!dataValidator.isValid(valueAsString, expectedType)) {
                        log.warn("DATA REJECTED [Device: {}]: Field '{}' value '{}' is not valid type {}",
                                uniqueId, key, valueAsString, expectedType);
                        continue; // Bỏ qua trường này, không xử lý tiếp
                    }
                    // =================================================================

                    // Nếu hợp lệ, thêm vào danh sách dữ liệu sạch
                    filteredData.set(key, valueNode);

                    // --- Logic Gán nhãn (chỉ áp dụng nếu là Số) ---
                    if (valueNode.isNumber() && expectedType == PropertyDataType.NUMERIC) {

                        // --- KIỂM TRA QUYỀN GÁN NHÃN ---
                        boolean isEligibleForLabeling = true;
                        if (primaryPropId != null) {
                            if (!matchedSensor.getProperty().getId().equals(primaryPropId)) {
                                isEligibleForLabeling = false;
                            }
                        }
                        // --------------------------------------

                        if (isEligibleForLabeling) {
                            double value = valueNode.asDouble();
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
                } else {
                    log.debug("Ignored unknown property '{}' from device '{}'", key, uniqueId);
                }
            }

            // 5. Nếu sau khi lọc mà không còn dữ liệu nào -> Bỏ qua
            if (filteredData.isEmpty()) {
                log.warn("Payload rejected: All fields invalid or unknown. Device: {}", uniqueId);
                return;
            }

            // 6. Lưu xuống Data Lake
            saveToDataLake(device, filteredData, finalLabel, reasons, uniqueId);

        } catch (Exception e) {
            log.error("Unexpected error processing AI data", e);
        }
    }

    private Sensor findSensorByKey(Device device, String key) {
        return device.getSensors().stream()
                .filter(s -> s.getProperty() != null && s.getProperty().getName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    @Async("iotTaskExecutor")
    public void saveToDataLake(Device device, JsonNode data, String label, List<String> reasons, String uniqueId) {
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

            // Features
            record.set("features", data);

            // Units
            ObjectNode unitsNode = objectMapper.createObjectNode();
            Iterator<String> fieldNames = data.fieldNames();

            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                Sensor s = findSensorByKey(device, key);
                String unit = (s != null && s.getProperty() != null) ? s.getProperty().getUnit() : "";
                unitsNode.put(key, unit);
            }
            record.set("units", unitsNode);

            record.put("label", label);

            if (!reasons.isEmpty()) {
                record.put("label_reason", String.join("; ", reasons));
            }

            LocalDateTime now = LocalDateTime.now();
            String path = String.format("telemetry/%s/%d/%02d/%02d/%s_%s.json",
                    uniqueId, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), label, System.currentTimeMillis());

            minioService.uploadJson(path, objectMapper.writeValueAsString(record));

            log.info("Thread: {} | Upload success for Device: {}", Thread.currentThread().getName(), uniqueId);
            log.info(">> AI Data Ingested: Label=[{}] | Device=[{}]", label, uniqueId);

        } catch (Exception e) {
            log.error("Failed to save data", e);
        }
    }
}
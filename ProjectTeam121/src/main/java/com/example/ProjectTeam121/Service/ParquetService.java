package com.example.ProjectTeam121.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ParquetService {

    // 1. Định nghĩa Schema Avro (Cấu trúc file)
    private static final String SCHEMA_JSON = "{"
            + "\"type\": \"record\","
            + "\"name\": \"IotTelemetry\","
            + "\"fields\": ["
            + "  {\"name\": \"device_id\", \"type\": \"string\"},"
            + "  {\"name\": \"device_identifier\", \"type\": \"string\"},"
            + "  {\"name\": \"timestamp\", \"type\": \"string\"},"
            + "  {\"name\": \"label\", \"type\": \"string\"},"
            + "  {\"name\": \"features\", \"type\": {\"type\": \"map\", \"values\": \"double\"}},"
            + "  {\"name\": \"units\", \"type\": {\"type\": \"map\", \"values\": \"string\"}},"
            + "  {\"name\": \"location_province\", \"type\": [\"null\", \"string\"], \"default\": null},"
            + "  {\"name\": \"location_district\", \"type\": [\"null\", \"string\"], \"default\": null},"
            + "  {\"name\": \"location_ward\", \"type\": [\"null\", \"string\"], \"default\": null},"
            + "  {\"name\": \"location_specific\", \"type\": [\"null\", \"string\"], \"default\": null}"
            + "]"
            + "}";

    private final Schema schema;

    public ParquetService() {
        this.schema = new Schema.Parser().parse(SCHEMA_JSON);
    }

    // Hàm chuyển đổi JSON sang File Parquet (Lưu tạm vào file)
    public File convertJsonToParquet(JsonNode jsonData) throws IOException {
        // Tạo file tạm
        File tempFile = File.createTempFile("iot_data_", ".parquet");

        // === Xóa file rỗng vừa tạo ===
        // ParquetWriter cần tự mình tạo file mới. Nếu file đã tồn tại (do lệnh trên tạo), nó sẽ báo lỗi.
        if (tempFile.exists()) {
            tempFile.delete();
        }

        Path path = new Path(tempFile.getAbsolutePath());

        // Cấu hình Writer
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(path)
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY) // Nén Snappy (nhanh & nhẹ)
                .withConf(new Configuration())
                .build()) {

            // Tạo Record theo Schema
            GenericRecord record = new GenericData.Record(schema);

            // --- Mapping Metadata ---
            JsonNode meta = jsonData.get("metadata");
            record.put("device_id", meta.get("device_id").asText());
            record.put("device_identifier", meta.get("device_identifier").asText());
            record.put("timestamp", meta.get("timestamp").asText());

            // Xử lý các trường vị trí (nullable)
            record.put("location_province", meta.has("location_province") ? meta.get("location_province").asText() : null);
            record.put("location_district", meta.has("location_district") ? meta.get("location_district").asText() : null);
            record.put("location_ward", meta.has("location_ward") ? meta.get("location_ward").asText() : null);
            record.put("location_specific", meta.has("location_specific") ? meta.get("location_specific").asText() : null);

            record.put("label", jsonData.has("label") ? jsonData.get("label").asText() : "NORMAL");

            // --- Mapping Features (Map<String, Double>) ---
            Map<String, Double> featuresMap = new HashMap<>();
            JsonNode features = jsonData.get("features");
            if (features != null) {
                Iterator<String> featureNames = features.fieldNames();
                while (featureNames.hasNext()) {
                    String key = featureNames.next();
                    featuresMap.put(key, features.get(key).asDouble());
                }
            }
            record.put("features", featuresMap);

            // --- Mapping Units (Map<String, String>) ---
            Map<String, String> unitsMap = new HashMap<>();
            JsonNode units = jsonData.get("units");
            if (units != null) {
                Iterator<String> unitNames = units.fieldNames();
                while (unitNames.hasNext()) {
                    String key = unitNames.next();
                    unitsMap.put(key, units.get(key).asText());
                }
            }
            record.put("units", unitsMap);

            // Ghi vào file
            writer.write(record);
        }

        return tempFile;
    }

    public List<JsonNode> readParquetFile(File file) throws IOException {
        List<JsonNode> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Path path = new Path(file.getAbsolutePath());

        // Sử dụng AvroParquetReader để đọc
        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(path)
                .withConf(new Configuration())
                .build()) {

            GenericRecord record;
            while ((record = reader.read()) != null) {
                // Tạo root object
                ObjectNode rootNode = mapper.createObjectNode();

                // === 1. TÁI TẠO NODE METADATA (Quan trọng cho IoTLakeService) ===
                ObjectNode metadataNode = mapper.createObjectNode();
                if (record.get("device_id") != null) metadataNode.put("device_id", record.get("device_id").toString());
                if (record.get("device_identifier") != null) metadataNode.put("device_identifier", record.get("device_identifier").toString());
                if (record.get("timestamp") != null) metadataNode.put("timestamp", record.get("timestamp").toString());

                // Các trường vị trí
                if (record.get("location_province") != null) metadataNode.put("location_province", record.get("location_province").toString());
                if (record.get("location_district") != null) metadataNode.put("location_district", record.get("location_district").toString());
                if (record.get("location_ward") != null) metadataNode.put("location_ward", record.get("location_ward").toString());
                if (record.get("location_specific") != null) metadataNode.put("location_specific", record.get("location_specific").toString());

                // Gắn metadata vào root
                rootNode.set("metadata", metadataNode);

                // Lấy label (thường nằm ở root hoặc metadata tùy thiết kế, ở đây để root cho tiện)
                if (record.get("label") != null) rootNode.put("label", record.get("label").toString());

                // === 2. Lấy Map Features (Cảm biến) ===
                Object featuresObj = record.get("features");
                if (featuresObj instanceof Map) {
                    Map<?, ?> features = (Map<?, ?>) featuresObj;
                    ObjectNode featuresNode = rootNode.putObject("features");
                    for (Map.Entry<?, ?> entry : features.entrySet()) {
                        featuresNode.put(entry.getKey().toString(), Double.valueOf(entry.getValue().toString()));
                    }
                }

                // === 3. Lấy Map Units (Đơn vị) ===
                Object unitsObj = record.get("units");
                if (unitsObj instanceof Map) {
                    Map<?, ?> units = (Map<?, ?>) unitsObj;
                    ObjectNode unitsNode = rootNode.putObject("units");
                    for (Map.Entry<?, ?> entry : units.entrySet()) {
                        unitsNode.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }

                // Thêm vào danh sách kết quả
                result.add(rootNode);
            }
        } catch (Exception e) {
            log.error("Lỗi đọc file Parquet: {}", e.getMessage());
            throw e;
        }
        return result;
    }
}
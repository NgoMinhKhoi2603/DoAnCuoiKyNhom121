package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Iot.Request.LakeQueryRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.LakeQueryResponse;
import com.example.ProjectTeam121.Dto.Response.ExportFilterResponse;
import com.example.ProjectTeam121.Entity.ExportFilter;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.Property;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.ExportFilterRepository;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.Iot.PropertyRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class IoTLakeService {

    private final MinioClient minioClient;
    private final DeviceRepository deviceRepository;
    private final PropertyRepository propertyRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ExportFilterRepository exportFilterRepository;
    private final ParquetService parquetService;

    @Value("${minio.bucket-name}")
    private String bucket;

    /**
     * Đọc file mới nhất trong bucket Data Lake
     */
    public String loadLatestRawData() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .recursive(true)
                            .build()
            );

            Item latest = null;

            for (Result<Item> result : results) {
                Item item = result.get();
                if (latest == null || item.lastModified().isAfter(latest.lastModified())) {
                    latest = item;
                }
            }

            if (latest == null) {
                return null;
            }

            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(latest.objectName())
                            .build())) {
                return new String(stream.readAllBytes());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Truy vấn dữ liệu Data Lake (Hỗ trợ JSON và PARQUET)
     */
    @Async("queryExecutor")
    public CompletableFuture<List<LakeQueryResponse>> queryData(LakeQueryRequest request) {

        log.info("Start Async Query on Thread: {}", Thread.currentThread().getName());
        List<LakeQueryResponse> results = new ArrayList<>();

        // 1. Chuẩn bị các điều kiện lọc
        boolean hasLocationFilter = (request.getProvince() != null && !request.getProvince().isEmpty())
                || (request.getDistrict() != null && !request.getDistrict().isEmpty())
                || (request.getWard() != null && !request.getWard().isEmpty())
                || (request.getSpecificLocation() != null && !request.getSpecificLocation().isEmpty());

        List<Device> targetDevices = deviceRepository.findByLocationAndType(
                request.getProvince(), request.getDistrict(), request.getWard(),
                request.getSpecificLocation(), request.getDeviceTypeId()
        );

        if (targetDevices.isEmpty()) return CompletableFuture.completedFuture(results);

        // Map Property -> Allowed Units
        Set<String> targetPropIds = (request.getPropertyIds() == null) ? new HashSet<>() : new HashSet<>(request.getPropertyIds());
        Map<String, Set<String>> allowedPropertyUnits = new HashMap<>();
        if (!targetPropIds.isEmpty()) {
            List<Property> props = propertyRepository.findAllById(targetPropIds);
            for (Property p : props) {
                allowedPropertyUnits.computeIfAbsent(p.getName(), k -> new HashSet<>())
                        .add(p.getUnit() != null ? p.getUnit() : "");
            }
        }

        // 2. Duyệt qua từng thiết bị và từng ngày
        for (Device device : targetDevices) {
            LocalDate current = request.getFromDate();
            String uniqueId = device.getUniqueIdentifier();

            while (!current.isAfter(request.getToDate())) {
                try {
                    String prefix = String.format("telemetry/%s/%d/%02d/%02d/",
                            uniqueId, current.getYear(), current.getMonthValue(), current.getDayOfMonth());

                    Iterable<Result<Item>> minioObjects = minioClient.listObjects(
                            ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build());

                    for (Result<Item> result : minioObjects) {
                        File tempFile = null;
                        try {
                            Item item = result.get();
                            String objectName = item.objectName();

                            // === TRƯỜNG HỢP 1: FILE PARQUET ===
                            if (objectName.endsWith(".parquet")) {
                                try (InputStream stream = minioClient.getObject(
                                        GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {

                                    // Tải về file tạm
                                    tempFile = File.createTempFile("query_", ".parquet");
                                    Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                    // Dùng ParquetService đọc thành List JsonNode
                                    List<JsonNode> records = parquetService.readParquetFile(tempFile);

                                    // Xử lý từng bản ghi
                                    for (JsonNode rootNode : records) {
                                        processSingleRecord(rootNode, device, request, hasLocationFilter, allowedPropertyUnits, targetPropIds, results);
                                    }
                                }
                            }
                            // === TRƯỜNG HỢP 2: FILE JSON (Cũ) ===
                            else if (objectName.endsWith(".json")) {
                                try (InputStream stream = minioClient.getObject(
                                        GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {

                                    JsonNode rootNode = objectMapper.readTree(stream);
                                    processSingleRecord(rootNode, device, request, hasLocationFilter, allowedPropertyUnits, targetPropIds, results);
                                }
                            }

                        } catch (Exception e) {
                            log.warn("Error processing file: {}", e.getMessage());
                        } finally {
                            // Xóa file tạm nếu có
                            if (tempFile != null && tempFile.exists()) {
                                tempFile.delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore folder error
                }
                current = current.plusDays(1);
            }
        }

        results.sort(Comparator.comparing(LakeQueryResponse::getTimestamp).reversed());
        return CompletableFuture.completedFuture(results);
    }

    /**
     * Hàm tách biệt logic xử lý 1 bản ghi JSON (Dùng chung cho cả JSON và Parquet)
     */
    private void processSingleRecord(JsonNode rootNode, Device device, LakeQueryRequest request,
                                     boolean hasLocationFilter, Map<String, Set<String>> allowedPropertyUnits,
                                     Set<String> targetPropIds, List<LakeQueryResponse> results) {

        JsonNode metadataNode = rootNode.get("metadata");
        JsonNode unitsNode = rootNode.get("units");

        // --- XỬ LÝ VỊ TRÍ ---
        Function<String, String> getMeta = (key) -> {
            if (metadataNode.has(key) && !metadataNode.get(key).isNull()) {
                String txt = metadataNode.get(key).asText();
                if (txt == null || txt.equalsIgnoreCase("null") || txt.equalsIgnoreCase("N/A")) return "";
                return txt;
            }
            return "";
        };

        String specific = getMeta.apply("location_specific");
        String ward = getMeta.apply("location_ward");
        String district = getMeta.apply("location_district");
        String province = getMeta.apply("location_province");

        // Fallback vị trí từ Device nếu trong file không có
        if (specific.isEmpty() && ward.isEmpty() && district.isEmpty() && province.isEmpty()) {
            specific = (device.getLocation() != null) ? device.getLocation() : "";
            ward = (device.getWard() != null) ? device.getWard() : "";
            district = (device.getDistrict() != null) ? device.getDistrict() : "";
            province = (device.getProvince() != null) ? device.getProvince() : "";
        }

        List<String> locParts = new ArrayList<>();
        if (!specific.isEmpty()) locParts.add(specific);
        if (!ward.isEmpty() && !specific.contains(ward)) locParts.add(ward);
        if (!district.isEmpty() && !specific.contains(district)) locParts.add(district);
        if (!province.isEmpty() && !specific.contains(province)) locParts.add(province);

        String fileLocation = locParts.isEmpty() ? "Chưa cập nhật vị trí" : String.join(", ", locParts);

        // --- BỘ LỌC VỊ TRÍ ---
        if (hasLocationFilter) {
            String locLower = fileLocation.toLowerCase();
            if (StringUtils.hasText(request.getProvince()) && !locLower.contains(request.getProvince().toLowerCase())) return;
            if (StringUtils.hasText(request.getDistrict()) && !locLower.contains(request.getDistrict().toLowerCase())) return;
            if (StringUtils.hasText(request.getWard()) && !locLower.contains(request.getWard().toLowerCase())) return;
            if (StringUtils.hasText(request.getSpecificLocation()) && !locLower.contains(request.getSpecificLocation().toLowerCase())) return;
        }

        String timeStr = getMeta.apply("timestamp");
        String label = rootNode.has("label") ? rootNode.get("label").asText() : "NORMAL";

        if (!timeStr.isEmpty()) {
            LocalDateTime timestamp = LocalDateTime.parse(timeStr);
            LocalTime recordTime = timestamp.toLocalTime();

            if (request.getFromTime() != null && recordTime.isBefore(request.getFromTime())) return;
            if (request.getToTime() != null && recordTime.isAfter(request.getToTime())) return;

            JsonNode featuresNode = rootNode.get("features");
            if (featuresNode != null) {
                Iterator<String> fieldNames = featuresNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    String fileUnit = (unitsNode != null && unitsNode.has(key)) ? unitsNode.get(key).asText() : "";

                    boolean isMatch = false;
                    if (targetPropIds.isEmpty()) {
                        isMatch = true;
                    } else if (allowedPropertyUnits.containsKey(key)) {
                        Set<String> validUnits = allowedPropertyUnits.get(key);
                        if (validUnits.contains(fileUnit)) isMatch = true;
                    }

                    if (isMatch) {
                        results.add(LakeQueryResponse.builder()
                                .deviceId(device.getId())
                                .deviceName(device.getName())
                                .propertyName(key)
                                .value(featuresNode.get(key).asText())
                                .unit(fileUnit)
                                .timestamp(timestamp)
                                .location(fileLocation)
                                .label(label)
                                .build());
                    }
                }
            }
        }
    }

    // ... (Giữ nguyên các hàm saveExportHistory, getMyExportHistory, deleteExportHistory) ...
    public void saveExportHistory(LakeQueryRequest request, String fileName) {
        // ... (Code cũ của bạn)
        try {
            String currentUserEmail = SecurityUtils.getCurrentUsername();
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String jsonPayload = objectMapper.writeValueAsString(request);
            String desc = String.format("Từ %s đến %s", request.getFromDate(), request.getToDate());
            if (request.getDeviceTypeId() != null) desc += " | Có lọc thiết bị";
            if (request.getProvince() != null) desc += " | " + request.getProvince();

            ExportFilter history = ExportFilter.builder()
                    .user(user)
                    .filterName(fileName)
                    .filterJson(jsonPayload)
                    .description(desc)
                    .build();
            exportFilterRepository.save(history);
        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON lịch sử export", e);
        }
    }

    public List<ExportFilterResponse> getMyExportHistory() {
        String currentUser = SecurityUtils.getCurrentUsername();
        List<ExportFilter> entities = exportFilterRepository.findByUser_EmailOrderByCreateAtDesc(currentUser);
        return entities.stream().map(e -> ExportFilterResponse.builder()
                .id(e.getId())
                .filterName(e.getFilterName())
                .filterJson(e.getFilterJson())
                .description(e.getDescription())
                .createAt(e.getCreateAt())
                .build()
        ).toList();
    }

    public void deleteExportHistory(Long id) {
        String currentUserEmail = SecurityUtils.getCurrentUsername();
        ExportFilter history = exportFilterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử truy vấn này."));
        if (!history.getUser().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Bạn không có quyền xóa lịch sử của người khác.");
        }
        exportFilterRepository.delete(history);
    }
}
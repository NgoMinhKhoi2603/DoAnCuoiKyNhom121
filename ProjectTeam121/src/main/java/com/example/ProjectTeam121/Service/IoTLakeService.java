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
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

            // Đọc nội dung file
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(latest.objectName())
                            .build()
            );

            return new String(stream.readAllBytes());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<LakeQueryResponse> queryData(LakeQueryRequest request) {
        List<LakeQueryResponse> results = new ArrayList<>();

        // 1. Kiểm tra xem người dùng có đang lọc theo vị trí không?
        boolean hasLocationFilter = (request.getProvince() != null && !request.getProvince().isEmpty())
                || (request.getDistrict() != null && !request.getDistrict().isEmpty())
                || (request.getWard() != null && !request.getWard().isEmpty())
                || (request.getSpecificLocation() != null && !request.getSpecificLocation().isEmpty());

        // 2. Lọc Thiết bị theo Vị trí và Loại (MySQL)
        // Bước này giúp thu hẹp phạm vi quét file
        List<Device> targetDevices = deviceRepository.findByLocationAndType(
                request.getProvince(),
                request.getDistrict(),
                request.getWard(),
                request.getSpecificLocation(),
                request.getDeviceTypeId()
        );

        if (targetDevices.isEmpty()) return results;

        // Map Property ID sang Name
        Set<String> targetPropIds = (request.getPropertyIds() == null) ? new HashSet<>() : new HashSet<>(request.getPropertyIds());
        Map<String, String> propertyIdToNameMap = new HashMap<>();
        if (!targetPropIds.isEmpty()) {
            List<Property> props = propertyRepository.findAllById(targetPropIds);
            for(Property p : props) propertyIdToNameMap.put(p.getId(), p.getName());
        }

        // 3. Duyệt file MinIO
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
                        try {
                            Item item = result.get();
                            try (InputStream stream = minioClient.getObject(
                                    GetObjectArgs.builder().bucket(bucket).object(item.objectName()).build())) {

                                JsonNode rootNode = objectMapper.readTree(stream);
                                JsonNode metadataNode = rootNode.get("metadata");

                                // --- XỬ LÝ VỊ TRÍ & FALLBACK ---

                                // Hàm helper: Trả về rỗng nếu giá trị là null/"null"/"N/A"
                                Function<String, String> getMeta = (key) -> {
                                    if (metadataNode.has(key) && !metadataNode.get(key).isNull()) {
                                        String txt = metadataNode.get(key).asText();
                                        // Xử lý triệt để các trường hợp null dạng chuỗi
                                        if (txt == null || txt.equalsIgnoreCase("null") || txt.equalsIgnoreCase("N/A")) {
                                            return "";
                                        }
                                        return txt;
                                    }
                                    return "";
                                };

                                String specific = getMeta.apply("location_specific");
                                String ward = getMeta.apply("location_ward");
                                String district = getMeta.apply("location_district");
                                String province = getMeta.apply("location_province");

                                // Fallback: Nếu file rỗng -> Lấy từ Device hiện tại
                                boolean isFileLocationEmpty = specific.isEmpty() && ward.isEmpty() && district.isEmpty() && province.isEmpty();

                                if (isFileLocationEmpty) {
                                    specific = (device.getLocation() != null) ? device.getLocation() : "";
                                    ward = (device.getWard() != null) ? device.getWard() : "";
                                    district = (device.getDistrict() != null) ? device.getDistrict() : "";
                                    province = (device.getProvince() != null) ? device.getProvince() : "";
                                }

                                // Ghép chuỗi (Tránh lặp)
                                List<String> locParts = new ArrayList<>();
                                if (!specific.isEmpty()) locParts.add(specific);
                                if (!ward.isEmpty() && !specific.contains(ward)) locParts.add(ward);
                                if (!district.isEmpty() && !specific.contains(district)) locParts.add(district);
                                if (!province.isEmpty() && !specific.contains(province)) locParts.add(province);

                                String fileLocation = locParts.isEmpty() ? "Chưa cập nhật vị trí" : String.join(", ", locParts);

                                // === LOGIC MỚI: BỘ LỌC NGHIÊM NGẶT (STRICT FILTER) ===
                                // Nếu người dùng CÓ tìm kiếm địa chỉ, nhưng kết quả cuối cùng lại không hợp lệ
                                // (vẫn là "null", "N/A" hoặc "Chưa cập nhật..."), thì BỎ QUA dòng này.
                                if (hasLocationFilter) {
                                    if (locParts.isEmpty() ||
                                            fileLocation.equalsIgnoreCase("Chưa cập nhật vị trí") ||
                                            fileLocation.toLowerCase().contains("null")) {
                                        continue; // Next bản ghi
                                    }

                                    // Tùy chọn nâng cao: Kiểm tra xem fileLocation có CHỨA từ khóa tìm kiếm không?
                                    // Ví dụ tìm "Hà Nội" mà fileLocation ra "Hồ Chí Minh" (do thiết bị di chuyển) -> Nên bỏ qua
                                    if (request.getProvince() != null && !request.getProvince().isEmpty() &&
                                            !fileLocation.toLowerCase().contains(request.getProvince().toLowerCase())) continue;

                                    if (request.getDistrict() != null && !request.getDistrict().isEmpty() &&
                                            !fileLocation.toLowerCase().contains(request.getDistrict().toLowerCase())) continue;
                                }
                                // =====================================================

                                String timeStr = getMeta.apply("timestamp");
                                String label = rootNode.has("label") ? rootNode.get("label").asText() : "NORMAL";

                                if (!timeStr.isEmpty()) {
                                    LocalDateTime timestamp = LocalDateTime.parse(timeStr);
                                    LocalTime recordTime = timestamp.toLocalTime();

                                    if (request.getFromTime() != null && recordTime.isBefore(request.getFromTime())) continue;
                                    if (request.getToTime() != null && recordTime.isAfter(request.getToTime())) continue;

                                    JsonNode featuresNode = rootNode.get("features");
                                    Iterator<String> fieldNames = featuresNode.fieldNames();

                                    while (fieldNames.hasNext()) {
                                        String key = fieldNames.next();
                                        boolean isMatch = targetPropIds.isEmpty();
                                        if (!isMatch) {
                                            isMatch = propertyIdToNameMap.containsValue(key);
                                        }

                                        if (isMatch) {
                                            results.add(LakeQueryResponse.builder()
                                                    .deviceId(device.getId())
                                                    .deviceName(device.getName())
                                                    .propertyName(key)
                                                    .value(featuresNode.get(key).asText())
                                                    .unit("")
                                                    .timestamp(timestamp)
                                                    .location(fileLocation)
                                                    .label(label)
                                                    .build());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore single file error
                        }
                    }
                } catch (Exception e) {
                    // Ignore folder error
                }
                current = current.plusDays(1);
            }
        }

        results.sort(Comparator.comparing(LakeQueryResponse::getTimestamp).reversed());
        return results;
    }

    public void saveExportHistory(LakeQueryRequest request, String fileName) {
        try {
            String currentUserEmail = SecurityUtils.getCurrentUsername();

            // Tìm User Entity
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jsonPayload = objectMapper.writeValueAsString(request);

            String desc = String.format("Từ %s đến %s", request.getFromDate(), request.getToDate());
            if (request.getDeviceTypeId() != null) desc += " | Có lọc thiết bị";
            if (request.getProvince() != null) desc += " | " + request.getProvince();

            // Map sang Entity của bạn
            ExportFilter history = ExportFilter.builder()
                    .user(user)               // Set User object
                    .filterName(fileName)     // filterName
                    .filterJson(jsonPayload)  // filterJson
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

        // Convert Entity -> DTO
        return entities.stream().map(e -> ExportFilterResponse.builder()
                .id(e.getId())
                .filterName(e.getFilterName())
                .filterJson(e.getFilterJson())
                .description(e.getDescription())
                .createAt(e.getCreateAt())
                .build()
        ).toList();
    }
}

package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Iot.Request.LakeQueryRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.LakeQueryResponse;
import com.example.ProjectTeam121.Dto.Response.ExportFilterResponse;
import com.example.ProjectTeam121.Service.IoTLakeService;
import com.example.ProjectTeam121.Service.MinioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/data-query")
@RequiredArgsConstructor
public class DataQueryController {

    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final IoTLakeService ioTLakeService;

    @GetMapping
    public ResponseEntity<List<JsonNode>> queryDeviceData(
            @RequestParam String deviceId,
            @RequestParam String date) { // date format: dd-MM-yyyy

        // 1. Parse ngày để tạo đường dẫn thư mục
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Prefix: telemetry/SENSOR-001/2024/12/11/
        String prefix = String.format("telemetry/%s/%d/%02d/%02d/",
                deviceId,
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth());

        // 2. Lấy danh sách file từ MinIO
        List<String> files = minioService.listFiles(prefix);
        List<JsonNode> dataPoints = new ArrayList<>();

        // 3. Đọc từng file và gom vào list
        for (String file : files) {
            try {
                String content = minioService.getFileContent(file);
                JsonNode node = objectMapper.readTree(content);
                dataPoints.add(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(dataPoints);
    }

    @PostMapping("/lake")
    public CompletableFuture<ResponseEntity<List<LakeQueryResponse>>> queryLakeData(@RequestBody LakeQueryRequest request) {
        return ioTLakeService.queryData(request)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/history")
    public ResponseEntity<Void> saveHistory(
            @RequestParam String fileName,
            @RequestBody LakeQueryRequest request) {

        ioTLakeService.saveExportHistory(request, fileName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<ExportFilterResponse>> getHistory() {
        return ResponseEntity.ok(ioTLakeService.getMyExportHistory());
    }
}
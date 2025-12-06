package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.ExportFilterRequest;
import com.example.ProjectTeam121.Dto.Response.ExportFilterResponse;
import com.example.ProjectTeam121.Service.ExportFilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/export-filters")
@RequiredArgsConstructor
public class ExportFilterController {

    private final ExportFilterService exportFilterService;

    // Tạo mới filter
    @PostMapping
    public ResponseEntity<ExportFilterResponse> create(@Valid @RequestBody ExportFilterRequest request) {
        return ResponseEntity.ok(exportFilterService.create(request));
    }

    // Lấy danh sách filter của user đang đăng nhập
    @GetMapping
    public ResponseEntity<List<ExportFilterResponse>> getMyFilters() {
        return ResponseEntity.ok(exportFilterService.getMyFilters());
    }

    // Cập nhật filter
    @PutMapping("/{id}")
    public ResponseEntity<ExportFilterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExportFilterRequest request) {
        return ResponseEntity.ok(exportFilterService.update(id, request));
    }

    // Xóa filter
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exportFilterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
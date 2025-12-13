package com.example.ProjectTeam121.Controller.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceTypeRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceTypeResponse;
import com.example.ProjectTeam121.Service.Iot.DeviceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/device-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // Cho phép FE gọi API
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    // API Tạo mới loại thiết bị
    @PostMapping
    public ResponseEntity<DeviceTypeResponse> create(@Valid @RequestBody DeviceTypeRequest request) {
        return ResponseEntity.ok(deviceTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> update(@PathVariable String id, @Valid @RequestBody DeviceTypeRequest request) {
        return ResponseEntity.ok(deviceTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(deviceTypeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DeviceTypeResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(deviceTypeService.getAll(pageable));
    }
}
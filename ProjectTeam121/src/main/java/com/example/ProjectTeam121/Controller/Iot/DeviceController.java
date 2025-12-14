package com.example.ProjectTeam121.Controller.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Service.Iot.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // Cho phép FE gọi API
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable String id, @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DeviceResponse>> getAllDevices(Pageable pageable) {
        return ResponseEntity.ok(deviceService.getAllDevices(pageable));
    }
    @GetMapping({ "", "/" })
public ResponseEntity<Page<DeviceResponse>> getDevices(Pageable pageable) {
    return ResponseEntity.ok(deviceService.getAllDevices(pageable));
}
}

package com.example.ProjectTeam121.Controller.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Service.Iot.DeviceService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    private String getUsername() {
        return SecurityUtils.getCurrentUsername();
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.create(request, getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable String id, @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request, getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deviceService.delete(id, getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getById(id, getUsername()));
    }

    @GetMapping("/my-devices")
    public ResponseEntity<Page<DeviceResponse>> getMyDevices(Pageable pageable) {
        return ResponseEntity.ok(deviceService.getDevicesByUser(getUsername(), pageable));
    }
}
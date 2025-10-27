package com.example.ProjectTeam121.Controller.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.SensorRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.SensorResponse;
import com.example.ProjectTeam121.Service.Iot.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping("/by-device/{deviceId}")
    public ResponseEntity<SensorResponse> create(@PathVariable String deviceId, @Valid @RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.create(deviceId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorResponse> update(@PathVariable String id, @Valid @RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        sensorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(sensorService.getById(id));
    }

    @GetMapping("/by-device/{deviceId}")
    public ResponseEntity<Page<SensorResponse>> getSensorsByDevice(@PathVariable String deviceId, Pageable pageable) {
        return ResponseEntity.ok(sensorService.getSensorsByDevice(deviceId, pageable));
    }
}
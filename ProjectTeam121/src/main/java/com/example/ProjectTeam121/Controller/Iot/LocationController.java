package com.example.ProjectTeam121.Controller.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.LocationRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.LocationResponse;
import com.example.ProjectTeam121.Service.Iot.LocationService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iot/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    private String getUsername() {
        return SecurityUtils.getCurrentUsername();
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.create(request, getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(@PathVariable String id, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.update(id, request, getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        locationService.delete(id, getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(locationService.getById(id, getUsername()));
    }

    @GetMapping("/my-roots")
    public ResponseEntity<Page<LocationResponse>> getMyRootLocations(Pageable pageable) {
        return ResponseEntity.ok(locationService.getRootLocationsByUser(getUsername(), pageable));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<Page<LocationResponse>> getChildLocations(@PathVariable String parentId, Pageable pageable) {
        return ResponseEntity.ok(locationService.getChildLocations(parentId, getUsername(), pageable));
    }
}
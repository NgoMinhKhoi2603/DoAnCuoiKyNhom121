package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.LocationRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.LocationResponse;
import com.example.ProjectTeam121.Entity.Iot.Location;
import com.example.ProjectTeam121.Mapper.Iot.LocationMapper;
import com.example.ProjectTeam121.Repository.Iot.LocationRepository;
import com.example.ProjectTeam121.Service.HistoryService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final HistoryService historyService;

    // Helper: Tìm location (không cần check ownership)
    private Location findLocationById(String id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.LOCATION_NOT_FOUND, "Location not found"));
    }


    @Transactional
    public LocationResponse create(LocationRequest request) {
        Location location = locationMapper.toEntity(request);

        // Xử lý Parent Location
        if (request.getParentId() != null) {
            Location parent = findLocationById(request.getParentId());
            location.setParent(parent);
        }

        Location savedLocation = locationRepository.save(location);

        // Ghi log
        historyService.saveHistory(savedLocation, ActionLog.CREATE, HistoryType.LOCATION_MANAGEMENT,
                savedLocation.getId(), SecurityUtils.getCurrentUsername());

        return locationMapper.toResponse(savedLocation);
    }

    @Transactional
    public LocationResponse update(String id, LocationRequest request) {
        Location location = findLocationById(id);

        // Cập nhật
        locationMapper.updateEntityFromRequest(request, location);

        // Xử lý Parent
        if (request.getParentId() != null) {
            Location parent = findLocationById(request.getParentId());
            location.setParent(parent);
        } else {
            location.setParent(null);
        }

        Location updatedLocation = locationRepository.save(location);

        // Ghi log
        historyService.saveHistory(updatedLocation, ActionLog.UPDATE, HistoryType.LOCATION_MANAGEMENT,
                updatedLocation.getId(), SecurityUtils.getCurrentUsername());

        return locationMapper.toResponse(updatedLocation);
    }

    @Transactional
    public void delete(String id) {
        Location location = findLocationById(id);
        locationRepository.delete(location);

        // Ghi log
        historyService.saveHistory(location, ActionLog.DELETE, HistoryType.LOCATION_MANAGEMENT,
                location.getId(), SecurityUtils.getCurrentUsername());
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(String id) {
        Location location = findLocationById(id);
        return locationMapper.toResponse(location);
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> getRootLocations(Pageable pageable) {
        return locationRepository.findByParentIsNull(pageable)
                .map(locationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> getChildLocations(String parentId, Pageable pageable) {
        findLocationById(parentId);
        return locationRepository.findByParent_Id(parentId, pageable)
                .map(locationMapper::toResponse);
    }
}
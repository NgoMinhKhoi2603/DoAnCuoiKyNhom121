package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.LocationRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.LocationResponse;
import com.example.ProjectTeam121.Entity.Iot.Location;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.Iot.LocationMapper;
import com.example.ProjectTeam121.Repository.Iot.LocationRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Service.HistoryService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationMapper locationMapper;
    private final HistoryService historyService;

    // Helper: Tìm user hiện tại
    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Helper: Tìm location và kiểm tra quyền sở hữu
    private Location findLocationByIdAndCheckOwnership(String id, String username) {
        return locationRepository.findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new ValidationException(ErrorCode.LOCATION_NOT_FOUND, "Location not found or access denied"));
    }

    @Transactional
    public LocationResponse create(LocationRequest request, String username) {
        User currentUser = getCurrentUser(username);

        Location location = locationMapper.toEntity(request);
        location.setUser(currentUser);

        // Xử lý Parent Location
        if (request.getParentId() != null) {
            Location parent = findLocationByIdAndCheckOwnership(request.getParentId(), username);
            location.setParent(parent);
        }

        Location savedLocation = locationRepository.save(location);

        // Ghi log
        historyService.saveHistory(savedLocation, ActionLog.CREATE, HistoryType.LOCATION_MANAGEMENT,
                savedLocation.getId(), username);

        return locationMapper.toResponse(savedLocation);
    }

    @Transactional
    public LocationResponse update(String id, LocationRequest request, String username) {
        Location location = findLocationByIdAndCheckOwnership(id, username);

        // Cập nhật
        locationMapper.updateEntityFromRequest(request, location);

        // Xử lý Parent
        if (request.getParentId() != null) {
            Location parent = findLocationByIdAndCheckOwnership(request.getParentId(), username);
            location.setParent(parent);
        } else {
            location.setParent(null);
        }

        Location updatedLocation = locationRepository.save(location);

        // Ghi log
        historyService.saveHistory(updatedLocation, ActionLog.UPDATE, HistoryType.LOCATION_MANAGEMENT,
                updatedLocation.getId(), username);

        return locationMapper.toResponse(updatedLocation);
    }

    @Transactional
    public void delete(String id, String username) {
        Location location = findLocationByIdAndCheckOwnership(id, username);
        // (Tùy chọn: kiểm tra xem location có device con không trước khi xóa)
        locationRepository.delete(location);

        // Ghi log
        historyService.saveHistory(location, ActionLog.DELETE, HistoryType.LOCATION_MANAGEMENT,
                location.getId(), username);
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(String id, String username) {
        Location location = findLocationByIdAndCheckOwnership(id, username);
        return locationMapper.toResponse(location);
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> getRootLocationsByUser(String username, Pageable pageable) {
        return locationRepository.findByUser_UsernameAndParentIsNull(username, pageable)
                .map(locationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<LocationResponse> getChildLocations(String parentId, String username, Pageable pageable) {
        // Kiểm tra quyền sở hữu của parent
        findLocationByIdAndCheckOwnership(parentId, username);
        return locationRepository.findByParent_Id(parentId, pageable)
                .map(locationMapper::toResponse);
    }
}
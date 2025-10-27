package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import com.example.ProjectTeam121.Entity.Iot.Location;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.Iot.DeviceMapper;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.Iot.DeviceTypeRepository;
import com.example.ProjectTeam121.Repository.Iot.LocationRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Service.HistoryService;
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
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceMapper deviceMapper;
    private final HistoryService historyService;

    // Helper: Tìm user hiện tại
    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Helper: Tìm device và kiểm tra quyền sở hữu
    private Device findDeviceByIdAndCheckOwnership(String id, String username) {
        return deviceRepository.findByIdAndUser_Username(id, username)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_NOT_FOUND, "Device not found or access denied"));
    }

    @Transactional
    public DeviceResponse create(DeviceRequest request, String username) {
        User currentUser = getCurrentUser(username);

        if (deviceRepository.existsByUniqueIdentifier(request.getUniqueIdentifier())) {
            throw new ValidationException(ErrorCode.DEVICE_IDENTIFIER_EXISTS, "Unique identifier already exists");
        }

        // Tìm Location (phải thuộc user)
        Location location = locationRepository.findByIdAndUser_Username(request.getLocationId(), username)
                .orElseThrow(() -> new ValidationException(ErrorCode.LOCATION_NOT_FOUND, "Location not found or access denied"));

        // Tìm DeviceType (global)
        DeviceType deviceType = deviceTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));

        Device device = deviceMapper.toEntity(request);
        device.setUser(currentUser);
        device.setLocation(location);
        device.setDeviceType(deviceType);

        Device savedDevice = deviceRepository.save(device);

        historyService.saveHistory(savedDevice, ActionLog.CREATE, HistoryType.DEVICE_MANAGEMENT,
                savedDevice.getId(), username);

        return deviceMapper.toResponse(savedDevice);
    }

    @Transactional
    public DeviceResponse update(String id, DeviceRequest request, String username) {
        Device device = findDeviceByIdAndCheckOwnership(id, username);

        // Cập nhật các trường
        deviceMapper.updateEntityFromRequest(request, device);

        // Cập nhật Location (nếu thay đổi)
        if (!device.getLocation().getId().equals(request.getLocationId())) {
            Location location = locationRepository.findByIdAndUser_Username(request.getLocationId(), username)
                    .orElseThrow(() -> new ValidationException(ErrorCode.LOCATION_NOT_FOUND, "Location not found or access denied"));
            device.setLocation(location);
        }

        // Cập nhật DeviceType (nếu thay đổi)
        if (!device.getDeviceType().getId().equals(request.getTypeId())) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));
            device.setDeviceType(deviceType);
        }

        Device updatedDevice = deviceRepository.save(device);

        historyService.saveHistory(updatedDevice, ActionLog.UPDATE, HistoryType.DEVICE_MANAGEMENT,
                updatedDevice.getId(), username);

        return deviceMapper.toResponse(updatedDevice);
    }

    @Transactional
    public void delete(String id, String username) {
        Device device = findDeviceByIdAndCheckOwnership(id, username);
        // (Xóa device sẽ tự động xóa các Sensor con nhờ `cascade = CascadeType.ALL` trong Entity)
        deviceRepository.delete(device);

        historyService.saveHistory(device, ActionLog.DELETE, HistoryType.DEVICE_MANAGEMENT,
                device.getId(), username);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(String id, String username) {
        Device device = findDeviceByIdAndCheckOwnership(id, username);
        return deviceMapper.toResponse(device);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getDevicesByUser(String username, Pageable pageable) {
        return deviceRepository.findByUser_Username(username, pageable)
                .map(deviceMapper::toResponse);
    }
}
package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import com.example.ProjectTeam121.Entity.Iot.Property;
import com.example.ProjectTeam121.Mapper.Iot.DeviceMapper;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.Iot.DeviceTypeRepository;
import com.example.ProjectTeam121.Repository.Iot.PropertyRepository;
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
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceMapper deviceMapper;
    private final HistoryService historyService;
    private final PropertyRepository propertyRepository;

    // Helper: Tìm device (không cần check ownership)
    private Device findDeviceById(String id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_NOT_FOUND, "Device not found"));
    }

    @Transactional
    public DeviceResponse create(DeviceRequest request) {
        if (deviceRepository.existsByUniqueIdentifier(request.getUniqueIdentifier())) {
            throw new ValidationException(ErrorCode.DEVICE_IDENTIFIER_EXISTS, "Unique identifier already exists");
        }

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));

        Device device = deviceMapper.toEntity(request);
        device.setDeviceType(deviceType);

        // Gán Primary Property
        if (request.getPrimaryPropertyId() != null && !request.getPrimaryPropertyId().isEmpty()) {
            Property property = propertyRepository.findById(request.getPrimaryPropertyId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
            device.setPrimaryProperty(property);
        }

        Device savedDevice = deviceRepository.save(device);

        // ... (phần ghi log history giữ nguyên)

        return deviceMapper.toResponse(savedDevice);
    }

    @Transactional
    public DeviceResponse update(String id, DeviceRequest request) {
        Device device = findDeviceById(id);

        deviceMapper.updateEntityFromRequest(request, device);

        // Cập nhật DeviceType (giữ nguyên code cũ)
        if (!device.getDeviceType().getId().equals(request.getDeviceTypeId())) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));
            device.setDeviceType(deviceType);
        }

        // Cập nhật Primary Property
        if (request.getPrimaryPropertyId() != null && !request.getPrimaryPropertyId().isEmpty()) {
            Property property = propertyRepository.findById(request.getPrimaryPropertyId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
            device.setPrimaryProperty(property);
        } else {
            // Nếu người dùng xóa chọn (gửi null/empty), ta set về null
            device.setPrimaryProperty(null);
        }

        Device updatedDevice = deviceRepository.save(device);

        // ... (phần ghi log history giữ nguyên)

        return deviceMapper.toResponse(updatedDevice);
    }

    @Transactional
    public void delete(String id) {
        Device device = findDeviceById(id);
        deviceRepository.delete(device);

        historyService.saveHistory(device, ActionLog.DELETE, HistoryType.DEVICE_MANAGEMENT,
                device.getId(), SecurityUtils.getCurrentUsername(), "Delete a device");
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(String id) {
        Device device = findDeviceById(id);
        return deviceMapper.toResponse(device);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(deviceMapper::toResponse);
    }
}
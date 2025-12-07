package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import com.example.ProjectTeam121.Mapper.Iot.DeviceMapper;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.Iot.DeviceTypeRepository;
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

        if (deviceRepository.existsByDeviceCode(request.getDeviceCode())) {
            // Nếu bạn chưa thêm ErrorCode mới thì dùng tạm INVALID_INPUT
            throw new ValidationException(ErrorCode.DEVICE_CODE_EXISTS, "Mã thiết bị (Device Code) đã tồn tại");
        }

        // Tìm DeviceType (global)
        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));

        Device device = deviceMapper.toEntity(request);
        device.setDeviceType(deviceType);

        // Các trường location, province... được map tự động bởi mapper

        Device savedDevice = deviceRepository.save(device);

        historyService.saveHistory(savedDevice, ActionLog.CREATE, HistoryType.DEVICE_MANAGEMENT,
                savedDevice.getId(), SecurityUtils.getCurrentUsername());

        return deviceMapper.toResponse(savedDevice);
    }

    @Transactional
    public DeviceResponse update(String id, DeviceRequest request) {
        Device device = findDeviceById(id);

        // Cập nhật các trường (bao gồm location, province...)
        deviceMapper.updateEntityFromRequest(request, device);

        // Cập nhật DeviceType (nếu thay đổi)
        if (!device.getDeviceType().getId().equals(request.getDeviceTypeId())) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));
            device.setDeviceType(deviceType);
        }

        if (!device.getDeviceCode().equals(request.getDeviceCode())
                && deviceRepository.existsByDeviceCode(request.getDeviceCode())) {
            throw new ValidationException(ErrorCode.DEVICE_CODE_EXISTS, "Mã thiết bị (Device Code) đã tồn tại");
        }

        Device updatedDevice = deviceRepository.save(device);

        historyService.saveHistory(updatedDevice, ActionLog.UPDATE, HistoryType.DEVICE_MANAGEMENT,
                updatedDevice.getId(), SecurityUtils.getCurrentUsername());

        return deviceMapper.toResponse(updatedDevice);
    }

    @Transactional
    public void delete(String id) {
        Device device = findDeviceById(id);
        deviceRepository.delete(device);

        historyService.saveHistory(device, ActionLog.DELETE, HistoryType.DEVICE_MANAGEMENT,
                device.getId(), SecurityUtils.getCurrentUsername());
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
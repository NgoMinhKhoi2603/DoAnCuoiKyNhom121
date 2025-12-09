package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.DeviceTypeRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceTypeResponse;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import com.example.ProjectTeam121.Mapper.Iot.DeviceTypeMapper;
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
public class DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceTypeMapper deviceTypeMapper;
    private final HistoryService historyService;

    private DeviceType findById(String id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));
    }

    @Transactional
    public DeviceTypeResponse create(DeviceTypeRequest request) {
        if (deviceTypeRepository.existsByName(request.getName())) {
            throw new ValidationException(ErrorCode.DEVICE_TYPE_NAME_EXISTS, "DeviceType name already exists");
        }
        DeviceType deviceType = deviceTypeMapper.toEntity(request);
        DeviceType savedDeviceType = deviceTypeRepository.save(deviceType);

        historyService.saveHistory(savedDeviceType, ActionLog.CREATE, HistoryType.DEVICE_TYPE_MANAGEMENT,
                savedDeviceType.getId(), SecurityUtils.getCurrentUsername(), "Create a device type");

        return deviceTypeMapper.toResponse(savedDeviceType);
    }

    @Transactional
    public DeviceTypeResponse update(String id, DeviceTypeRequest request) {
        DeviceType deviceType = findById(id);
        deviceTypeMapper.updateEntityFromRequest(request, deviceType);
        DeviceType updatedDeviceType = deviceTypeRepository.save(deviceType);

        historyService.saveHistory(updatedDeviceType, ActionLog.UPDATE, HistoryType.DEVICE_TYPE_MANAGEMENT,
                updatedDeviceType.getId(), SecurityUtils.getCurrentUsername(), "Update a device type");

        return deviceTypeMapper.toResponse(updatedDeviceType);
    }

    @Transactional
    public void delete(String id) {
        DeviceType deviceType = findById(id);
        // (Tùy chọn: kiểm tra xem có device nào đang dùng type này không)
        deviceTypeRepository.delete(deviceType);

        historyService.saveHistory(deviceType, ActionLog.DELETE, HistoryType.DEVICE_TYPE_MANAGEMENT,
                deviceType.getId(), SecurityUtils.getCurrentUsername(), "Delete a device type");
    }

    @Transactional(readOnly = true)
    public DeviceTypeResponse getById(String id) {
        return deviceTypeMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<DeviceTypeResponse> getAll(Pageable pageable) {
        return deviceTypeRepository.findAll(pageable).map(deviceTypeMapper::toResponse);
    }
}
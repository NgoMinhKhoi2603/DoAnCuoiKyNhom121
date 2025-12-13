package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Enum.SensorStatus;
import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import com.example.ProjectTeam121.Entity.Iot.Property;
import com.example.ProjectTeam121.Entity.Iot.Sensor;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceMapper deviceMapper;
    private final HistoryService historyService;
    private final PropertyRepository propertyRepository;

    private Device findDeviceById(String id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_NOT_FOUND, "Device not found"));
    }

    @Transactional
    public DeviceResponse create(DeviceRequest request) {
        if (deviceRepository.existsByUniqueIdentifier(request.getUniqueIdentifier())) {
            throw new ValidationException(ErrorCode.DEVICE_IDENTIFIER_EXISTS, "Mã thiết bị đã tồn tại");
        }

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "Loại thiết bị không tồn tại"));

        Device device = deviceMapper.toEntity(request);
        device.setDeviceType(deviceType);

        // 1. Xử lý Sensors (Dùng List thay vì Set)
        if (request.getPropertyIds() != null && !request.getPropertyIds().isEmpty()) {
            // SỬA: Dùng ArrayList
            List<Sensor> sensors = new ArrayList<>();
            List<Property> properties = propertyRepository.findAllById(request.getPropertyIds());

            for (Property prop : properties) {
                Sensor.SensorBuilder sensorBuilder = Sensor.builder()
                        .name(device.getName() + " - " + prop.getName())
                        .device(device)
                        .property(prop)
                        .status(SensorStatus.ACTIVE)
                        .isActuator(false);

                // Check Threshold cho Primary Property
                if (prop.getId().equals(request.getPrimaryPropertyId())) {
                    sensorBuilder.thresholdWarning(request.getThresholdWarning());
                    sensorBuilder.thresholdCritical(request.getThresholdCritical());
                }

                sensors.add(sensorBuilder.build());
            }
            device.setSensors(sensors);
        }

        // 2. Set Primary Property Reference
        if (request.getPrimaryPropertyId() != null && !request.getPrimaryPropertyId().isEmpty()) {
            if (request.getPropertyIds() != null && !request.getPropertyIds().contains(request.getPrimaryPropertyId())) {
                throw new ValidationException(ErrorCode.INVALID_REQUEST, "Thuộc tính chính phải thuộc danh sách thuộc tính đã chọn");
            }
            Property property = propertyRepository.findById(request.getPrimaryPropertyId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Thuộc tính không tìm thấy"));
            device.setPrimaryProperty(property);
        }

        Device savedDevice = deviceRepository.save(device);

        historyService.saveHistory(savedDevice, ActionLog.CREATE, HistoryType.DEVICE_MANAGEMENT,
                savedDevice.getId(), SecurityUtils.getCurrentUsername(), "Create a device");

        return deviceMapper.toResponse(savedDevice);
    }

    @Transactional
    public DeviceResponse update(String id, DeviceRequest request) {
        Device device = findDeviceById(id);
        deviceMapper.updateEntityFromRequest(request, device);

        if (!device.getDeviceType().getId().equals(request.getDeviceTypeId())) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_TYPE_NOT_FOUND, "DeviceType not found"));
            device.setDeviceType(deviceType);
        }

        // 1. Đồng bộ Sensors (Logic cho List)
        if (request.getPropertyIds() != null) {
            // Đảm bảo list sensors không null
            if (device.getSensors() == null) {
                device.setSensors(new ArrayList<>());
            }

            List<String> newPropIds = request.getPropertyIds();

            // Xóa sensor cũ không còn trong list mới
            device.getSensors().removeIf(sensor -> !newPropIds.contains(sensor.getProperty().getId()));

            List<String> existingPropIds = device.getSensors().stream()
                    .map(s -> s.getProperty().getId())
                    .collect(Collectors.toList());

            // Thêm sensor mới
            for (String propId : newPropIds) {
                if (!existingPropIds.contains(propId)) {
                    Property prop = propertyRepository.findById(propId)
                            .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));

                    Sensor newSensor = Sensor.builder()
                            .name(device.getName() + " - " + prop.getName())
                            .device(device)
                            .property(prop)
                            .status(SensorStatus.ACTIVE)
                            .isActuator(false)
                            .build();
                    device.getSensors().add(newSensor);
                }
            }
        }

        if (device.getSensors() != null) {
            for (Sensor sensor : device.getSensors()) {
                if (sensor.getProperty().getId().equals(request.getPrimaryPropertyId())) {
                    sensor.setThresholdWarning(request.getThresholdWarning());
                    sensor.setThresholdCritical(request.getThresholdCritical());
                }
            }
        }

        if (request.getPrimaryPropertyId() != null && !request.getPrimaryPropertyId().isEmpty()) {
            if (request.getPropertyIds() != null && !request.getPropertyIds().contains(request.getPrimaryPropertyId())) {
                throw new ValidationException(ErrorCode.INVALID_REQUEST, "Thuộc tính chính phải thuộc danh sách thuộc tính đã chọn");
            }
            Property property = propertyRepository.findById(request.getPrimaryPropertyId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
            device.setPrimaryProperty(property);
        } else {
            device.setPrimaryProperty(null);
        }

        Device updatedDevice = deviceRepository.save(device);

        historyService.saveHistory(updatedDevice, ActionLog.UPDATE, HistoryType.DEVICE_MANAGEMENT,
                updatedDevice.getId(), SecurityUtils.getCurrentUsername(), "Update a device");

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
        return deviceMapper.toResponse(findDeviceById(id));
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable).map(deviceMapper::toResponse);
    }
}
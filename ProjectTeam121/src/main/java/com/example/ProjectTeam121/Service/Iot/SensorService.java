package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.SensorRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.SensorResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import com.example.ProjectTeam121.Entity.Iot.Property;
import com.example.ProjectTeam121.Entity.Iot.Sensor;
import com.example.ProjectTeam121.Mapper.Iot.SensorMapper;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.Iot.PropertyRepository;
import com.example.ProjectTeam121.Repository.Iot.SensorRepository;
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
public class SensorService {

    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final PropertyRepository propertyRepository;
    private final SensorMapper sensorMapper;
    private final HistoryService historyService;

    // Helper: Tìm device và kiểm tra quyền sở hữu
    private Device findDeviceByIdAndCheckOwnership(String deviceId, String username) {
        return deviceRepository.findByIdAndUser_Username(deviceId, username)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_NOT_FOUND, "Device not found or access denied"));
    }

    // Helper: Tìm sensor và kiểm tra quyền sở hữu (thông qua device)
    private Sensor findSensorByIdAndCheckOwnership(String id, String username) {
        return sensorRepository.findByIdAndDeviceUserUsername(id, username)
                .orElseThrow(() -> new ValidationException(ErrorCode.SENSOR_NOT_FOUND, "Sensor not found or access denied"));
    }

    @Transactional
    public SensorResponse create(String deviceId, SensorRequest request, String username) {
        // Kiểm tra quyền sở hữu device
        Device device = findDeviceByIdAndCheckOwnership(deviceId, username);

        // Tìm Property (global)
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));

        Sensor sensor = sensorMapper.toEntity(request);
        sensor.setDevice(device);
        sensor.setProperty(property);

        Sensor savedSensor = sensorRepository.save(sensor);

        historyService.saveHistory(savedSensor, ActionLog.CREATE, HistoryType.SENSOR_MANAGEMENT,
                savedSensor.getId(), username);

        return sensorMapper.toResponse(savedSensor);
    }

    @Transactional
    public SensorResponse update(String id, SensorRequest request, String username) {
        // Kiểm tra quyền sở hữu sensor
        Sensor sensor = findSensorByIdAndCheckOwnership(id, username);

        // Cập nhật
        sensorMapper.updateEntityFromRequest(request, sensor);

        // Cập nhật Property (nếu thay đổi)
        if (!sensor.getProperty().getId().equals(request.getPropertyId())) {
            Property property = propertyRepository.findById(request.getPropertyId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
            sensor.setProperty(property);
        }

        Sensor updatedSensor = sensorRepository.save(sensor);

        historyService.saveHistory(updatedSensor, ActionLog.UPDATE, HistoryType.SENSOR_MANAGEMENT,
                updatedSensor.getId(), username);

        return sensorMapper.toResponse(updatedSensor);
    }

    @Transactional
    public void delete(String id, String username) {
        Sensor sensor = findSensorByIdAndCheckOwnership(id, username);
        sensorRepository.delete(sensor);

        historyService.saveHistory(sensor, ActionLog.DELETE, HistoryType.SENSOR_MANAGEMENT,
                sensor.getId(), username);
    }

    @Transactional(readOnly = true)
    public SensorResponse getById(String id, String username) {
        Sensor sensor = findSensorByIdAndCheckOwnership(id, username);
        return sensorMapper.toResponse(sensor);
    }

    @Transactional(readOnly = true)
    public Page<SensorResponse> getSensorsByDevice(String deviceId, String username, Pageable pageable) {
        // Kiểm tra quyền sở hữu device
        findDeviceByIdAndCheckOwnership(deviceId, username);
        return sensorRepository.findByDevice_IdAndDeviceUserUsername(deviceId, username, pageable)
                .map(sensorMapper::toResponse);
    }
}
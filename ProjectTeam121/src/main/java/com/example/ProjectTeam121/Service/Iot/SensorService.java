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

    // Helper: Tìm device (không cần check ownership)
    private Device findDeviceById(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ValidationException(ErrorCode.DEVICE_NOT_FOUND, "Device not found"));
    }

    // Helper: Tìm sensor (không cần check ownership)
    private Sensor findSensorById(String id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.SENSOR_NOT_FOUND, "Sensor not found"));
    }

    @Transactional
    public SensorResponse create(String deviceId, SensorRequest request) {
        // Tìm device
        Device device = findDeviceById(deviceId);

        // Tìm Property (global)
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));

        Sensor sensor = sensorMapper.toEntity(request);
        sensor.setDevice(device);
        sensor.setProperty(property);

        Sensor savedSensor = sensorRepository.save(sensor);

        historyService.saveHistory(savedSensor, ActionLog.CREATE, HistoryType.SENSOR_MANAGEMENT,
                savedSensor.getId(), SecurityUtils.getCurrentUsername());

        return sensorMapper.toResponse(savedSensor);
    }

    @Transactional
    public SensorResponse update(String id, SensorRequest request) {
        // Tìm sensor
        Sensor sensor = findSensorById(id);

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
                updatedSensor.getId(), SecurityUtils.getCurrentUsername());

        return sensorMapper.toResponse(updatedSensor);
    }

    @Transactional
    public void delete(String id) {
        Sensor sensor = findSensorById(id);
        sensorRepository.delete(sensor);

        historyService.saveHistory(sensor, ActionLog.DELETE, HistoryType.SENSOR_MANAGEMENT,
                sensor.getId(), SecurityUtils.getCurrentUsername());
    }

    @Transactional(readOnly = true)
    public SensorResponse getById(String id) {
        Sensor sensor = findSensorById(id);
        return sensorMapper.toResponse(sensor);
    }

    @Transactional(readOnly = true)
    public Page<SensorResponse> getSensorsByDevice(String deviceId, Pageable pageable) {
        // Kiểm tra device tồn tại
        findDeviceById(deviceId);
        return sensorRepository.findByDevice_Id(deviceId, pageable)
                .map(sensorMapper::toResponse);
    }
}
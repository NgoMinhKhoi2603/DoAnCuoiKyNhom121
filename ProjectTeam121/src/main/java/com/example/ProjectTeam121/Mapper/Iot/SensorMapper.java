package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.SensorRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.SensorResponse;
import com.example.ProjectTeam121.Entity.Iot.Sensor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SensorMapper {

    @Mapping(target = "device", ignore = true)
    @Mapping(target = "property", ignore = true)
    Sensor toEntity(SensorRequest request);

    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "property.name", target = "propertyName")
    SensorResponse toResponse(Sensor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "lastValue", ignore = true) // lastValue chỉ nên được cập nhật bởi hệ thống
    void updateEntityFromRequest(SensorRequest request, @MappingTarget Sensor entity);
}
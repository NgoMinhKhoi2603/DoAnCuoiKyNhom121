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

    // Map thông tin property sang response phẳng
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "property.name", target = "propertyName")
    @Mapping(source = "property.unit", target = "propertyUnit")
    SensorResponse toResponse(Sensor entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "device", ignore = true)
    @Mapping(target = "property", ignore = true)
    void updateEntityFromRequest(SensorRequest request, @MappingTarget Sensor entity);
}
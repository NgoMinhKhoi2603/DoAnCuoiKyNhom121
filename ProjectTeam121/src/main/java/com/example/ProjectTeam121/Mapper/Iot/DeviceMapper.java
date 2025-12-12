package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "deviceType", ignore = true)
    @Mapping(target = "primaryProperty", ignore = true)
    Device toEntity(DeviceRequest request);

    @Mapping(source = "deviceType.id", target = "deviceTypeId")
    @Mapping(source = "deviceType.name", target = "typeName")
    // MAPPING MỚI
    @Mapping(source = "primaryProperty.id", target = "primaryPropertyId")
    @Mapping(source = "primaryProperty.name", target = "primaryPropertyName")
    DeviceResponse toResponse(Device entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deviceType", ignore = true)
    @Mapping(target = "sensors", ignore = true)
    @Mapping(target = "primaryProperty", ignore = true)
    void updateEntityFromRequest(DeviceRequest request, @MappingTarget Device entity);
}

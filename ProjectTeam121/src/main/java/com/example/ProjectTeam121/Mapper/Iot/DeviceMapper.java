package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceResponse;
import com.example.ProjectTeam121.Entity.Iot.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// Thêm uses = {SensorMapper.class} để nó biết cách map list sensors
@Mapper(componentModel = "spring", uses = {SensorMapper.class})
public interface DeviceMapper {

    @Mapping(target = "deviceType", ignore = true)
    @Mapping(target = "primaryProperty", ignore = true)
    @Mapping(target = "sensors", ignore = true) // Ignored khi tạo mới từ request
    Device toEntity(DeviceRequest request);

    @Mapping(source = "deviceType.id", target = "deviceTypeId")
    @Mapping(source = "deviceType.name", target = "typeName")
    @Mapping(source = "primaryProperty.id", target = "primaryPropertyId")
    @Mapping(source = "primaryProperty.name", target = "primaryPropertyName")
    @Mapping(source = "sensors", target = "sensors") // Map danh sách sensors
    DeviceResponse toResponse(Device entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deviceType", ignore = true)
    @Mapping(target = "sensors", ignore = true)
    @Mapping(target = "primaryProperty", ignore = true)
    void updateEntityFromRequest(DeviceRequest request, @MappingTarget Device entity);
}
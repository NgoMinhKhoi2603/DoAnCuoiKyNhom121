package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceTypeRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceTypeResponse;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeviceTypeMapper {

    DeviceType toEntity(DeviceTypeRequest request);

    DeviceTypeResponse toResponse(DeviceType entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "devices", ignore = true)
    void updateEntityFromRequest(DeviceTypeRequest request, @MappingTarget DeviceType entity);
}
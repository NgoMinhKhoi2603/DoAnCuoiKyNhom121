package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.DeviceTypeRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.DeviceTypeResponse;
import com.example.ProjectTeam121.Entity.Iot.DeviceType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Quan trọng: componentModel = "spring" để Spring quản lý bean này
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceTypeMapper {

    DeviceType toEntity(DeviceTypeRequest request);

    DeviceTypeResponse toResponse(DeviceType entity);

    void updateEntityFromRequest(DeviceTypeRequest request, @MappingTarget DeviceType entity);
}
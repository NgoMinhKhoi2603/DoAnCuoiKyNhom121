package com.example.ProjectTeam121.Mapper.Iot;

import com.example.ProjectTeam121.Dto.Iot.Request.PropertyRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.PropertyResponse;
import com.example.ProjectTeam121.Entity.Iot.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    Property toEntity(PropertyRequest request);

    PropertyResponse toResponse(Property entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sensors", ignore = true)
    void updateEntityFromRequest(PropertyRequest request, @MappingTarget Property entity);
}
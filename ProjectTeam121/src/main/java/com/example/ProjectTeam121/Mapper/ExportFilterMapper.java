package com.example.ProjectTeam121.Mapper;

import com.example.ProjectTeam121.Dto.Request.ExportFilterRequest;
import com.example.ProjectTeam121.Dto.Response.ExportFilterResponse;
import com.example.ProjectTeam121.Entity.ExportFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExportFilterMapper {

    @Mapping(target = "user", ignore = true) // User sẽ được set trong Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    ExportFilter toEntity(ExportFilterRequest request);

    @Mapping(source = "user.email", target = "userEmail")
    ExportFilterResponse toResponse(ExportFilter entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    void updateEntityFromRequest(ExportFilterRequest request, @MappingTarget ExportFilter entity);
}
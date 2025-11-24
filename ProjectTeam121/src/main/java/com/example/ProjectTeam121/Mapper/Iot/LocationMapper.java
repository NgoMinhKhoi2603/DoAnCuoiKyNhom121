//package com.example.ProjectTeam121.Mapper.Iot;
//
//import com.example.ProjectTeam121.Dto.Iot.Request.LocationRequest;
//import com.example.ProjectTeam121.Dto.Iot.Response.LocationResponse;
//import com.example.ProjectTeam121.Entity.Iot.Location;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//
//@Mapper(componentModel = "spring")
//public interface LocationMapper {
//
////    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "parent", ignore = true)
//    Location toEntity(LocationRequest request);
//
//    @Mapping(source = "parent.id", target = "parentId")
////    @Mapping(source = "user.username", target = "username")
//    LocationResponse toResponse(Location entity);
//
//    @Mapping(target = "id", ignore = true)
////    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "parent", ignore = true)
//    @Mapping(target = "devices", ignore = true)
//    void updateEntityFromRequest(LocationRequest request, @MappingTarget Location entity);
//}
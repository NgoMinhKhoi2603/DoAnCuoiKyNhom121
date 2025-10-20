package com.example.ProjectTeam121.Mapper;


import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BaseMapper<E, RQ, RP> {

    E toEntity(RQ requestDto);

    RP toResponse(E entity);

    List<E> toEntityList(List<RQ> requestDtoList);

    List<RP> toResponseList(List<E> entityList);

    void update(@MappingTarget E entity, RQ request);

    default Page<RP> toResponsePage(Page<E> entity) {
        return entity.map(this::toResponse);
    }

}

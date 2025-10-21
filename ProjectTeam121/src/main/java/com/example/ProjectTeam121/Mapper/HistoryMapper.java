package com.example.ProjectTeam121.Mapper;

import com.example.ProjectTeam121.Dto.Response.HistoryResponse;
import com.example.ProjectTeam121.Entity.HistoryEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;


import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class HistoryMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "content", expression = "java(convertStringToJsonNode(entity.getContent()))")
    public abstract HistoryResponse toResponse(HistoryEntity entity);

    public Page<HistoryResponse> toResponsePage(Page<HistoryEntity> entityPage) {
        List<HistoryResponse> responses = entityPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, entityPage.getPageable(), entityPage.getTotalElements());
    }

    protected JsonNode convertStringToJsonNode(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception e) {
            return objectMapper.createObjectNode().put("error", "Failed to parse JSON content");
        }
    }
}
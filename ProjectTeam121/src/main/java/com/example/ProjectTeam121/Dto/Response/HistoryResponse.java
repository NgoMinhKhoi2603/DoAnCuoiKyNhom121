package com.example.ProjectTeam121.Dto.Response;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
public class HistoryResponse extends BaseResponse{
    private ActionLog action;

    @Enumerated(EnumType.STRING)
    private HistoryType historyType;

    private JsonNode content;

    private String identify;
}

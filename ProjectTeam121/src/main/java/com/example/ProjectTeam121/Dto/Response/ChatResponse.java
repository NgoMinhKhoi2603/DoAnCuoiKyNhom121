package com.example.ProjectTeam121.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String answer;
    private String sql;
    private Object rawResult;
}

package com.example.ProjectTeam121.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExportFilterResponse {
    private Long id;
    private String filterName;
    private String filterJson;
    private String description;
    private LocalDateTime createAt;
    private String userEmail;
}
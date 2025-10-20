package com.example.ProjectTeam121.Dto.Response;



import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class BaseResponse {

    private String id;

    private String createdBy;

    private LocalDateTime createDate;

    private String lastUpdatedBy;

    private LocalDateTime lastUpdateDate;
}


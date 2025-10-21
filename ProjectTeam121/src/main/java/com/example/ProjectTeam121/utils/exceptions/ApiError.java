package com.example.ProjectTeam121.utils.exceptions;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String path;
    private String timestamp;
    private Integer code;
    private String fullCode;
    private String message;
    private Integer httpStatus;
    private String clientMessageId;
    private String transactionId;
}
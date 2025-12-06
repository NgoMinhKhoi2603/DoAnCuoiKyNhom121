package com.example.ProjectTeam121.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExportFilterRequest {

    @NotBlank(message = "Tên bộ lọc không được để trống")
    @Size(max = 100, message = "Tên bộ lọc không được quá 100 ký tự")
    private String filterName;

    private String filterJson;

    private String description;
}
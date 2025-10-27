package com.example.ProjectTeam121.Dto.Iot.Request;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PropertyRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 20)
    private String unit;

    @NotNull
    private PropertyDataType dataType;
}
package com.example.ProjectTeam121.Dto.Iot.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceTypeRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String manufacturer;

    private String description;

    @Size(max = 50)
    private String category;
}
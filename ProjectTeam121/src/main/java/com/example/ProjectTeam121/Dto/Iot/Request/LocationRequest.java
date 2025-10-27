package com.example.ProjectTeam121.Dto.Iot.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String parentId; // ID của Location cha (nếu có)
}
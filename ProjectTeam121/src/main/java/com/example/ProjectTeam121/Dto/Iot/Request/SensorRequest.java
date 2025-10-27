package com.example.ProjectTeam121.Dto.Iot.Request;

import com.example.ProjectTeam121.Dto.Enum.SensorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SensorRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private SensorStatus status;

    @NotNull
    private Boolean isActuator;

    private BigDecimal thresholdWarning;
    private BigDecimal thresholdCritical;

    @NotBlank
    private String propertyId;
}
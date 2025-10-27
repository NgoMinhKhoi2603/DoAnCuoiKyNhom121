package com.example.ProjectTeam121.Dto.Iot.Response;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PropertyResponse extends BaseIotResponse {
    private String name;
    private String unit;
    private PropertyDataType dataType;
}
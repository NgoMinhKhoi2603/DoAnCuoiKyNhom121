package com.example.ProjectTeam121.Dto.Iot.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LocationResponse extends BaseIotResponse {
    private String name;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String parentId;
    private String username;
}
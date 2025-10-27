package com.example.ProjectTeam121.Dto.Iot.Response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DeviceTypeResponse extends BaseIotResponse {
    private String name;
    private String manufacturer;
    private String description;
    private String category;
}
package com.example.ProjectTeam121.Dto.Iot.Response;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List; // Import List

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DeviceResponse extends BaseIotResponse {
    private String uniqueIdentifier;
    private String name;
    private DeviceStatus status;
    private String config;
    private LocalDateTime installedAt;
    private LocalDateTime lastSeen;
    private String location;
    private String province;
    private String district;
    private String ward;
    private String description;

    private String deviceTypeId;
    private String typeName;

    private String primaryPropertyId;
    private String primaryPropertyName;

    private List<SensorResponse> sensors;
}
package com.example.ProjectTeam121.Dto.Iot.Response;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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
    private String username;
    private String locationId;
    private String locationName;
    private String typeId;
    private String typeName;
}
package com.example.ProjectTeam121.Dto.Iot.Request;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceRequest {
    @NotBlank
    @Size(max = 255)
    private String uniqueIdentifier;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private DeviceStatus status;

    private String config;

    private LocalDateTime installedAt;

    private String location;

    @NotBlank
    @Size(max = 50)
    private String deviceCode;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String district;

    @Size(max = 100)
    private String ward;

    private String description;

    private String deviceTypeId;
}
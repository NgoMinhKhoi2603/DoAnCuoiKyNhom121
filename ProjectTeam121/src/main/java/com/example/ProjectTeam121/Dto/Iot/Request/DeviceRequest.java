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

    private String config; // JSON string
    private LocalDateTime installedAt;
    private String locationId;
    private String typeId;
}
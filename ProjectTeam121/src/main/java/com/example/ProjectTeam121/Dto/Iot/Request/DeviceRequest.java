package com.example.ProjectTeam121.Dto.Iot.Request;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeviceRequest {
    @NotBlank(message = "Mã thiết bị không được để trống")
    @Size(max = 255)
    private String uniqueIdentifier;

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Trạng thái không được để trống")
    private DeviceStatus status;

    private String config;

    private LocalDateTime installedAt;

    private String location;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String district;

    @Size(max = 100)
    private String ward;

    private String description;

    @NotBlank(message = "Loại thiết bị không được để trống")
    private String deviceTypeId;

    private String primaryPropertyId;

    private List<String> propertyIds;

    private BigDecimal thresholdWarning;
    private BigDecimal thresholdCritical;
}
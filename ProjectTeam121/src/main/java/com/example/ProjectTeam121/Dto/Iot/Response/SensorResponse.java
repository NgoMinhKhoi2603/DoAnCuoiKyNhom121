package com.example.ProjectTeam121.Dto.Iot.Response;

import com.example.ProjectTeam121.Dto.Enum.SensorStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SensorResponse extends BaseIotResponse {
    private String name;
    private SensorStatus status;
    private boolean isActuator;
    private BigDecimal thresholdWarning;
    private BigDecimal thresholdCritical;
    private String latestValue;

    // Thêm thông tin Property để hiển thị
    private String propertyId;
    private String propertyName;
    private String propertyUnit;
}
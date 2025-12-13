package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Dto.Enum.SensorStatus;
import com.example.ProjectTeam121.Entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "SENSORS", indexes = {
        @Index(name = "idx_sensor_device", columnList = "device_id"),
        @Index(name = "idx_sensor_property", columnList = "property_id")
})
public class Sensor extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SensorStatus status = SensorStatus.ACTIVE;

    @NotNull
    private boolean isActuator = false;

    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal thresholdWarning;

    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal thresholdCritical;

    @Size(max = 255)
    private String latestValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sensors"})
    private Property property;
}
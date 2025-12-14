package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import com.example.ProjectTeam121.Entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "DEVICES", indexes = {
        @Index(name = "idx_device_type", columnList = "device_type_id")
})

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Device extends BaseEntity {
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(columnDefinition = "TEXT")
    private String config;

    private LocalDateTime installedAt;

    private LocalDateTime lastSeen;

    @Column(columnDefinition = "TEXT")
    private String location;

    @Size(max = 100)
    @Column(length = 100)
    private String province;

    @Size(max = 100)
    @Column(length = 100)
    private String district;

    @Size(max = 100)
    @Column(length = 100)
    private String ward;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_property_id")
    @JsonIgnore
    private Property primaryProperty;

    @JsonProperty("primary_property_id")
    public String getPrimaryPropertyId() {
        return primaryProperty != null ? primaryProperty.getId() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id")
    @JsonIgnore
    private DeviceType deviceType;

    @JsonProperty("device_type_id")
    public String getDeviceTypeId() {
        return deviceType != null ? deviceType.getId() : null;
    }

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Sensor> sensors = new ArrayList<>();
}
package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "DEVICE_TYPES")
public class DeviceType extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 100)
    private String manufacturer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 50)
    @Column(length = 50)
    private String category; // "Gateway", "Sensor Node", "Actuator"

    // Quan hệ: Một loại thiết bị có nhiều thiết bị
    @OneToMany(mappedBy = "deviceType")
    private Set<Device> devices;
}
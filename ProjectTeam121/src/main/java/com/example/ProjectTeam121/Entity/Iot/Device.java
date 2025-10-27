package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Dto.Enum.DeviceStatus;
import com.example.ProjectTeam121.Entity.BaseEntity;
import com.example.ProjectTeam121.Entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "DEVICES", indexes = {
        @Index(name = "idx_device_user", columnList = "user_id"),
        @Index(name = "idx_device_location", columnList = "location_id"),
        @Index(name = "idx_device_type", columnList = "type_id")
})
public class Device extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier; // Serial Number hoặc MAC Address

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(columnDefinition = "TEXT")
    private String config; // Lưu JSON config

    private LocalDateTime installedAt;

    private LocalDateTime lastSeen;

    // Quan hệ: Thiết bị này thuộc về User nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ: Thiết bị này ở Vị trí nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    // Quan hệ: Thiết bị này thuộc Loại nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private DeviceType deviceType;

    // Quan hệ: Một thiết bị có nhiều cảm biến
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Sensor> sensors;
}
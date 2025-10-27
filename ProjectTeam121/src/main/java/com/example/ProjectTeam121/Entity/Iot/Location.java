package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Entity.BaseEntity;
import com.example.ProjectTeam121.Entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "LOCATIONS", indexes = {
        @Index(name = "idx_location_user", columnList = "user_id")
})
public class Location extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    // Quan hệ: Vị trí này thuộc về User nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ: Cấu trúc cây (Vị trí cha)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_location_id")
    private Location parent;

    // Quan hệ: Một vị trí có nhiều thiết bị
    @OneToMany(mappedBy = "location")
    private Set<Device> devices;
}
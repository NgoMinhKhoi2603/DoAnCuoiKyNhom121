package com.example.ProjectTeam121.Entity.Iot;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import com.example.ProjectTeam121.Entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "PROPERTIES")
public class Property extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name; // Ví dụ: 'Khói', 'Ánh sáng', 'Nhiệt độ'

    @Size(max = 20)
    @Column(length = 20)
    private String unit; // Ví dụ: 'ppm', 'lux', '°C'

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PropertyDataType dataType; // NUMERIC, BOOLEAN, STRING

    // Quan hệ: Một thuộc tính có thể được đo bởi nhiều cảm biến
    @OneToMany(mappedBy = "property")
    @JsonIgnore //Thêm dòng này để tránh lỗi Lazy Loading
    private Set<Sensor> sensors;
}
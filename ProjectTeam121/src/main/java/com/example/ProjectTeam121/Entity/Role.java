package com.example.ProjectTeam121.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "Role name must start with ROLE_ and contain only uppercase letters and underscores")
    @Column(length = 50, nullable = false, unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
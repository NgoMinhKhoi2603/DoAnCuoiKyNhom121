package com.example.ProjectTeam121.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_filters", indexes = {
        @Index(name = "idx_export_filter_user", columnList = "user_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExportFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "filter_name", nullable = false, length = 100)
    private String filterName;

    @Column(name = "filter_json", columnDefinition = "TEXT")
    private String filterJson;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    // Quan hệ Many-to-One với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
}
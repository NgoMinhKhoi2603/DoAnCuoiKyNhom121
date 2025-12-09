package com.example.ProjectTeam121.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_filters", indexes = {
        @Index(name = "idx_export_filter_created_by", columnList = "created_by")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ExportFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private User user;

    @Size(max = 100)
    @Column(name = "filter_name", length = 100)
    private String filterName;

    @Column(name = "filter_json", columnDefinition = "TEXT")
    private String filterJson;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @PrePersist
    protected void onCreate() {
        if (createAt == null) {
            createAt = LocalDateTime.now();
        }
    }
}
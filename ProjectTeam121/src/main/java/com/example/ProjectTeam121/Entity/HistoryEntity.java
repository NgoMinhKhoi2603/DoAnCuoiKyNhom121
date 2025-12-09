package com.example.ProjectTeam121.Entity;


import org.hibernate.annotations.Comment;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "HISTORY", indexes = {
        @Index(name = "idx_history_type", columnList = "historyType"),
        @Index(name = "idx_history_identify", columnList = "identify") // Thêm index cho identify
})
public class HistoryEntity extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 32, nullable = false)
    private ActionLog action;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", length = 64, nullable = false)
    private HistoryType historyType;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @Comment("Định danh của đối tượng liên quan đến lịch sử")
    @Size(max = 255) // Thêm giới hạn kích thước cho identify
    private String identify;

    @Column(columnDefinition = "TEXT")
    private String description;
}
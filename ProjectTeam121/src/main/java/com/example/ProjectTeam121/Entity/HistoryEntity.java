package com.example.ProjectTeam121.Entity;


import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import jakarta.persistence.*;
import lombok.*;
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
        @Index(name = "idx_history_type", columnList = "historyType")
})
public class HistoryEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 32, nullable = false)
    private ActionLog action;

    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", length = 64, nullable = false)
    private HistoryType historyType;

    @Lob
    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Comment("Định danh của đối tượng liên quan đến lịch sử")
    @Column(name = "identify", length = 255)
    private String identify;
}

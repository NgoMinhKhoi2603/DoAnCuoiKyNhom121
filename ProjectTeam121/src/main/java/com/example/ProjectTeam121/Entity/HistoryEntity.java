package com.example.ProjectTeam121.Entity;


import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

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
    private ActionLog action;

    @Enumerated(EnumType.STRING)
    private HistoryType historyType;

    @Column(columnDefinition = "CLOB")
    private String content;

    @Comment("Định danh của đối tượng liên quan đến lịch sử")
    private String identify;
}

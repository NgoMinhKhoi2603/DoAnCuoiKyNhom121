package com.example.ProjectTeam121.Entity;


import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

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
    private ActionLog action;

    @NotNull
    @Enumerated(EnumType.STRING)
    private HistoryType historyType;

    @NotNull
    @Column(columnDefinition = "CLOB") // CLOB/TEXT thường không thể là "blank", nhưng không nên null
    private String content;

    @Comment("Định danh của đối tượng liên quan đến lịch sử")
    @Size(max = 255) // Thêm giới hạn kích thước cho identify
    private String identify;
}
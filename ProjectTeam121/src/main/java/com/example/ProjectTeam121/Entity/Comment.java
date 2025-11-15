package com.example.ProjectTeam121.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "COMMENTS", indexes = {
        @Index(name = "idx_comment_parent", columnList = "parent_comment_id"),
        @Index(name = "idx_comment_user", columnList = "CREATED_BY")
})
public class Comment extends BaseEntity {

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isHidden = false;

    // Quan hệ: Trả lời bình luận nào (cha)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parent;

    // Quan hệ: Các bình luận trả lời (con)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();
}
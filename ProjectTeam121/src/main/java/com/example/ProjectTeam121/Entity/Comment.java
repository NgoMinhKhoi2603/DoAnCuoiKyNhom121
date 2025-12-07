package com.example.ProjectTeam121.Entity;

import com.example.ProjectTeam121.utils.SecurityUtils;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();

    @Column(nullable = false)
    private int likes = 0;

    @ElementCollection
    @CollectionTable(
            name = "COMMENT_LIKES",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Column(name = "user_email")
    private Set<String> likedUsers = new HashSet<>();


    public boolean isLikedBy(String email) {
        return likedUsers.contains(email);
    }

    public boolean isLikedByCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        return likedUsers.contains(email);
    }

    public void like(String email) {
        likedUsers.add(email);
        likes = likedUsers.size();
    }

    public void unlike(String email) {
        likedUsers.remove(email);
        likes = likedUsers.size();
    }
}

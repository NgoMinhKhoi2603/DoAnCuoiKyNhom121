package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    // Tìm bình luận cấp cao nhất (không ẩn)
    Page<Comment> findByParentIsNullAndIsHiddenFalse(Pageable pageable);

    // Tìm bình luận trả lời cho 1 bình luận cha (không ẩn)
    Page<Comment> findByParent_IdAndIsHiddenFalse(String parentId, Pageable pageable);

    // (Admin) Tìm bình luận cấp cao nhất (bao gồm cả bị ẩn)
    Page<Comment> findByParentIsNull(Pageable pageable);

    // (Admin) Tìm trả lời (bao gồm cả bị ẩn)
    Page<Comment> findByParent_Id(String parentId, Pageable pageable);
}
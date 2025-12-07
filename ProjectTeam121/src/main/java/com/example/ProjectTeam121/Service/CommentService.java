package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Request.CommentRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Entity.Comment;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.CommentMapper;
import com.example.ProjectTeam121.Repository.CommentRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final HistoryService historyService;

    private Comment findCommentById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.COMMENT_NOT_FOUND, "Không tìm thấy bình luận"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
    }


    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        String currentEmail = SecurityUtils.getCurrentUsername();
        User currentUser = findUserByEmail(currentEmail);

        if (currentUser.isCommentingLocked()) {
            throw new ValidationException(ErrorCode.COMMENTING_BLOCKED, "Bạn đã bị chặn bình luận");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());

        if (request.getParentId() != null) {
            Comment parent = findCommentById(request.getParentId());
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);

        historyService.saveHistory(savedComment, ActionLog.CREATE, HistoryType.COMMENT_MANAGEMENT,
                savedComment.getId(), currentEmail);

        return commentMapper.toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getTopLevelComments(Pageable pageable) {
        return commentRepository.findByParentIsNullAndIsHiddenFalse(pageable)
                .map(commentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getRepliesForComment(String parentId, Pageable pageable) {
        return commentRepository.findByParent_IdAndIsHiddenFalse(parentId, pageable)
                .map(commentMapper::toResponse);
    }

    @Transactional
    public CommentResponse hideComment(String commentId) {
        Comment comment = findCommentById(commentId);
        comment.setHidden(true);
        Comment updated = commentRepository.save(comment);

        historyService.saveHistory(updated, ActionLog.UPDATE, HistoryType.COMMENT_MANAGEMENT,
                updated.getId(), SecurityUtils.getCurrentUsername());

        return commentMapper.toResponse(updated);
    }

    @Transactional
    public CommentResponse unhideComment(String commentId) {
        Comment comment = findCommentById(commentId);
        comment.setHidden(false);
        Comment updated = commentRepository.save(comment);

        historyService.saveHistory(updated, ActionLog.UPDATE, HistoryType.COMMENT_MANAGEMENT,
                updated.getId(), SecurityUtils.getCurrentUsername());

        return commentMapper.toResponse(updated);
    }

    @Transactional
    public void deleteComment(String commentId) {
        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);

        historyService.saveHistory(comment, ActionLog.DELETE, HistoryType.COMMENT_MANAGEMENT,
                comment.getId(), SecurityUtils.getCurrentUsername());
    }

    // ======================================================
    // Like / Unlike bình luận
    // ======================================================
    @Transactional
    public int toggleLike(String commentId) {
        Comment comment = findCommentById(commentId);
        String email = SecurityUtils.getCurrentUsername();

        if (comment.isLikedByCurrentUser()) {
            comment.unlike(email);
        } else {
            comment.like(email);
        }

        commentRepository.save(comment);
        return comment.getLikes();
    }



}

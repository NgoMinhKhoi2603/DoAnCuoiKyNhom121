package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.CommentRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    @GetMapping("/toplevel")
    public ResponseEntity<Page<CommentResponse>> getTopLevelComments(Pageable pageable) {
        return ResponseEntity.ok(commentService.getTopLevelComments(pageable));
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<Page<CommentResponse>> getReplies(
            @PathVariable String parentId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.getRepliesForComment(parentId, pageable));
    }
}
package com.example.ProjectTeam121.Controller.Admin;

import com.example.ProjectTeam121.Dto.Request.AssignRoleRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Service.CommentService;
import com.example.ProjectTeam121.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final CommentService commentService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getPage(pageable));
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @PostMapping("/users/{username}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.lockUser(username));
    }

    @PostMapping("/users/{username}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.unlockUser(username));
    }

    @PostMapping("/users/{username}/assign-role")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable String username,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userService.assignRole(username, request.getRoleName()));
    }

    @PostMapping("/users/{username}/remove-role")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable String username,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userService.removeRole(username, request.getRoleName()));
    }

    @PostMapping("/users/{username}/lock-commenting")
    public ResponseEntity<UserResponse> lockCommenting(@PathVariable String username) {
        return ResponseEntity.ok(userService.lockCommenting(username));
    }

    @PostMapping("/users/{username}/unlock-commenting")
    public ResponseEntity<UserResponse> unlockCommenting(@PathVariable String username) {
        return ResponseEntity.ok(userService.unlockCommenting(username));
    }

    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<CommentResponse> hideComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.hideComment(commentId));
    }

    @PostMapping("/comments/{commentId}/unhide")
    public ResponseEntity<CommentResponse> unhideComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.unhideComment(commentId));
    }
}
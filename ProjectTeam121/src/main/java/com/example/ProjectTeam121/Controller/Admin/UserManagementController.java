package com.example.ProjectTeam121.Controller.Admin;

import com.example.ProjectTeam121.Dto.Request.AssignRoleRequest;
import com.example.ProjectTeam121.Dto.Request.UpdateUserRequest;
import com.example.ProjectTeam121.Dto.Response.CommentResponse;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Dto.Response.UserStatisticsResponse;
import com.example.ProjectTeam121.Service.CommentService;
import com.example.ProjectTeam121.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:3000")   
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final CommentService commentService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getPage(pageable));
    }

    @GetMapping("/users/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PostMapping("/users/{email}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.lockUser(email));
    }

    @PostMapping("/users/{email}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.unlockUser(email));
    }

    @PostMapping("/users/{email}/assign-role")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable String email,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userService.assignRole(email, request.getRoleName()));
    }

    @PostMapping("/users/{email}/remove-role")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable String email,
            @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userService.removeRole(email, request.getRoleName()));
    }

    @PostMapping("/users/{email}/lock-commenting")
    public ResponseEntity<UserResponse> lockCommenting(@PathVariable String email) {
        return ResponseEntity.ok(userService.lockCommenting(email));
    }

    @PostMapping("/users/{email}/unlock-commenting")
    public ResponseEntity<UserResponse> unlockCommenting(@PathVariable String email) {
        return ResponseEntity.ok(userService.unlockCommenting(email));
    }

    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<CommentResponse> hideComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.hideComment(commentId));
    }

    @PostMapping("/comments/{commentId}/unhide")
    public ResponseEntity<CommentResponse> unhideComment(@PathVariable String commentId) {
        return ResponseEntity.ok(commentService.unhideComment(commentId));
    }

    @GetMapping("/users/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }

    @PutMapping("/users/update/{email}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String email,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUserInfo(request, email));
    }

    //Thêm nhiều tài khoản bằng file excel
    @PostMapping("/import")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        String message = userService.importUsersFromExcel(file);
        return ResponseEntity.ok(message);
    }
}
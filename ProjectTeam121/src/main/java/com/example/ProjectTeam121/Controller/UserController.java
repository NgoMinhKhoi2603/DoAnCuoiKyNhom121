package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.AvatarRequest;
import com.example.ProjectTeam121.Dto.Request.ChangePasswordRequest;
import com.example.ProjectTeam121.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/change-avatar")
    public ResponseEntity<String> updateAvatar(@Valid @RequestBody AvatarRequest request) {
        userService.updateAvatar(request);
        return ResponseEntity.ok("Cập nhật ảnh đại diện thành công");
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
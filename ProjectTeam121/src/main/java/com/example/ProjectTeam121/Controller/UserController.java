package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.AvatarRequest;
import com.example.ProjectTeam121.Dto.Request.ChangePasswordRequest;
import com.example.ProjectTeam121.Dto.Request.DeleteAccountRequest; // Import DTO mới
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

    //Tự khóa tài khoản
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(@Valid @RequestBody DeleteAccountRequest request) {
        userService.deactivateAccount(request.getPassword());
        return ResponseEntity.ok("Tài khoản đã được vô hiệu hóa. Bạn sẽ bị đăng xuất.");
    }

    //Xóa dữ liệu
    @PostMapping("/wipe-data")
    public ResponseEntity<String> wipeUserData(@Valid @RequestBody DeleteAccountRequest request) {
        userService.deleteAllUserData(request.getPassword());
        return ResponseEntity.ok("Toàn bộ dữ liệu (bình luận, lịch sử, ảnh đại diện, ...) đã được xóa thành công.");
    }
}
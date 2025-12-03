package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.*;
import com.example.ProjectTeam121.Dto.Response.AuthenticationResponse;
import com.example.ProjectTeam121.Dto.Response.CurrentUserResponse;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Service.AuthenticationService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) { // Thêm @Valid
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) { // Thêm @Valid
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        // Lấy token raw từ header (bỏ chữ "Bearer ")
        String token = authHeader.replace("Bearer ", "");

        // Gọi Service xử lý
        return ResponseEntity.ok(service.getCurrentUser(token));
    }

    @GetMapping(value = "/activate", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        try {
            String message = service.activateAccount(token);

            return ResponseEntity.ok(
                    "<div style='margin:40px auto;max-width:500px;padding:20px;" +
                            "font-family:Segoe UI;border:1px solid #eee;border-radius:12px;" +
                            "text-align:center;box-shadow:0 4px 12px rgba(0,0,0,0.05)'>" +
                            "<h2 style='color:green'>✔ " + message + "</h2>" +
                            "<a href='http://localhost:3000/login' style='display:inline-block;margin-top:20px;" +
                            "padding:10px 18px;background:#c8102e;color:#fff;text-decoration:none;" +
                            "border-radius:8px;font-weight:bold;'>Đến trang đăng nhập</a>" +
                            "</div>"
            );

        } catch (Exception ex) {
            return ResponseEntity.status(400).body(
                    "<div style='margin:40px auto;max-width:500px;padding:20px;" +
                            "font-family:Segoe UI;border:1px solid #eee;border-radius:12px;" +
                            "text-align:center;box-shadow:0 4px 12px rgba(0,0,0,0.05)'>" +
                            "<h2 style='color:red'>✘ Kích hoạt thất bại!</h2>" +
                            "<p>" + ex.getMessage() + "</p>" +
                            "</div>"
            );
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(service.forgotPassword(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(service.changePassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(service.resetPassword(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    @PutMapping(value = "/update", consumes = {"multipart/form-data"})
    public ResponseEntity<CurrentUserResponse> updateCurrentUser(
            @RequestPart("data") UpdateUserRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        System.out.println("AvatarFile = " + (avatarFile != null ? avatarFile.getOriginalFilename() : "NULL"));
        return ResponseEntity.ok(service.updateCurrentUser(request, avatarFile));
    }

}
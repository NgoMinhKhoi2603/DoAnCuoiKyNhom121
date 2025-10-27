package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.AuthenticationRequest;
import com.example.ProjectTeam121.Dto.Response.AuthenticationResponse;
import com.example.ProjectTeam121.Dto.Request.RegisterRequest;
import com.example.ProjectTeam121.Service.AuthenticationService;
import com.example.ProjectTeam121.Service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid; // Thêm import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final LogoutService logoutService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) { // Thêm @Valid
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) { // Thêm @Valid
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        logoutService.logout(request);
        return ResponseEntity.ok("Logged out successfully.");
    }
}
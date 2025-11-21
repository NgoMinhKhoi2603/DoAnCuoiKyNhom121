package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.AuthenticationRequest;
import com.example.ProjectTeam121.Dto.Response.AuthenticationResponse;
import com.example.ProjectTeam121.Dto.Request.RegisterRequest;
import com.example.ProjectTeam121.Dto.Response.CurrentUserResponse;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Service.AuthenticationService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        String email = (user != null) ? user.getEmail() : "";

        return ResponseEntity.ok(
                new CurrentUserResponse(username, email, token)
        );
    }
}
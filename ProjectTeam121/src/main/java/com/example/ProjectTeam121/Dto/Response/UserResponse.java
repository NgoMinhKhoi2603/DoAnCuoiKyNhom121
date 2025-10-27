package com.example.ProjectTeam121.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean locked;
    private LocalDateTime createdAt;
    private Set<String> roles;
}

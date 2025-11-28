package com.example.ProjectTeam121.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private boolean enabled;
    private boolean locked;
    private LocalDateTime createdAt;
    private Set<String> roles;
}

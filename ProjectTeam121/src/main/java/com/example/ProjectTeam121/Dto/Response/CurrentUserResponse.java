package com.example.ProjectTeam121.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private String username;
    private String email;
    private String unit;
    private String unitDescription;
    private List<String> roles;
    private String token;
    private LocalDateTime createdAt;
}
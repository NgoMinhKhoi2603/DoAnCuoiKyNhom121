package com.example.ProjectTeam121.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUserResponse {
    private String username;
    private String email;
    private String token;
}
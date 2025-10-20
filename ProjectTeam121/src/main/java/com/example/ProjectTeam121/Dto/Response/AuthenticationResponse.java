package com.example.ProjectTeam121.Dto.Response;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class AuthenticationResponse {
    private String token;
}

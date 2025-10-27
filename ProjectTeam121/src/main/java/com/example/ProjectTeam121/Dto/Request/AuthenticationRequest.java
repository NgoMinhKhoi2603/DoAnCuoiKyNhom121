package com.example.ProjectTeam121.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class AuthenticationRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
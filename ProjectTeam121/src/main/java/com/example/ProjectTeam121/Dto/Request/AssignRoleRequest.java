package com.example.ProjectTeam121.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotBlank(message = "Role name cannot be blank")
    private String roleName;
}
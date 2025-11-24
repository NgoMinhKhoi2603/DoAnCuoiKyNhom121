package com.example.ProjectTeam121.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarRequest {
    @NotBlank(message = "Avatar data cannot be empty")
    private String base64Image;
}
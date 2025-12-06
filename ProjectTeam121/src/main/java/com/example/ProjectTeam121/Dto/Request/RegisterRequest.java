package com.example.ProjectTeam121.Dto.Request;

import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest {

    @NotBlank(message = "FullName cannot be blank")
    @Size(min = 3, max = 100, message = "FullName must be between 3 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull
    private UnitEnum unit;

}
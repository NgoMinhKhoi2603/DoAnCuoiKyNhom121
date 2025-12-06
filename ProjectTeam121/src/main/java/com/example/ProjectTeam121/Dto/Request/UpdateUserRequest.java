package com.example.ProjectTeam121.Dto.Request;

import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private UnitEnum unit;
    private String avatar;
}

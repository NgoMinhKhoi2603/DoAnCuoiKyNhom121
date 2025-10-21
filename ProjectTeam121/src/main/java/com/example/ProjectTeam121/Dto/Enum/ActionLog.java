package com.example.ProjectTeam121.Dto.Enum;

import lombok.Getter;

@Getter
public enum ActionLog {
    CREATE("Tạo mới"),
    UPDATE("Cập nhật"),
    DELETE("Xóa"),
    ;

    private final String description;

    ActionLog(String description) {
        this.description = description;
    }
}

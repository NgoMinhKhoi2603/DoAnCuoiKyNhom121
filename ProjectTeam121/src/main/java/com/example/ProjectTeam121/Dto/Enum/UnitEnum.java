package com.example.ProjectTeam121.Dto.Enum;

import lombok.Getter;

@Getter
public enum UnitEnum {
    CNTT ("Khoa Công Nghệ Thông Tin"),
    DTVT ("Khoa Điện Tử Viễn Thông"),
    ;
    private final String description;

    UnitEnum(String description) {
        this.description = description;
    }
}

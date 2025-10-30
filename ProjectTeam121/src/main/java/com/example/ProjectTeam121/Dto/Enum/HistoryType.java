package com.example.ProjectTeam121.Dto.Enum;


public enum HistoryType {
    USER("người dùng"),
    ROLE("role"),
    DEVICE("thiết bị"),
    ;
    private final String description;

    HistoryType(String description) {
        this.description = description;
    }
}

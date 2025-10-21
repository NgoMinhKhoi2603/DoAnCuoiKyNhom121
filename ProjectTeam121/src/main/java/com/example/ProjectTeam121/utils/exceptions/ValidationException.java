package com.example.ProjectTeam121.utils.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String i18nKey;
    private final Object[] args;
    private final String customMessage; // message tự định nghĩa


    public ValidationException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
        this.customMessage = message;
        this.i18nKey = null;
        this.args = null;
    }

    public ValidationException(ErrorCode code, String i18nKey, Object... args) {
        super(i18nKey);
        this.errorCode = code;
        this.i18nKey = i18nKey;
        this.args = args;
        this.customMessage = null;
    }
}

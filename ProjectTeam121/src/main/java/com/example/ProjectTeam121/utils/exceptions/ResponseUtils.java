package com.example.ProjectTeam121.utils.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

public final class ResponseUtils {
    private ResponseUtils() {}

    public static ResponseEntity<Object> error(
            HttpServletRequest req,
            ErrorCode errorCode,
            String messageOverride
    ) {
        HttpStatus status = errorCode.getHttpStatus();
        String message = messageOverride != null ? messageOverride : errorCode.getMessageKey();

        ApiError body = ApiError.builder()
                .path(req != null ? req.getRequestURI() : null)
                .timestamp(OffsetDateTime.now().toString())
                .code(errorCode.getCode())
                .fullCode(errorCode.getFullCode())
                .message(message)
                .httpStatus(status.value())
                .build();

        return ResponseEntity.status(status).body(body);
    }

    public static ResponseEntity<Object> errorRaw(
            HttpStatus status,
            String message,
            Integer code,
            String fullCode,
            String path
    ) {
        ApiError body = ApiError.builder()
                .path(path)
                .timestamp(OffsetDateTime.now().toString())
                .code(code)
                .fullCode(fullCode)
                .message(message)
                .httpStatus(status.value())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
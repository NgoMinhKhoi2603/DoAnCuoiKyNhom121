package com.example.ProjectTeam121.utils.exceptions;

import com.example.ProjectTeam121.utils.constants.I18nUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final I18nUtil i18n;

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResponseEntity<Object> handleValidation(
            ValidationException ex,
            HttpServletRequest request
    ) {
        String message;

        if (ex.getCustomMessage() != null) {
            message = ex.getCustomMessage();
        } else if (ex.getI18nKey() != null) {
            String template = i18n.get(ex.getI18nKey());
            message = (ex.getArgs() != null && ex.getArgs().length > 0)
                    ? MessageFormat.format(template, ex.getArgs())
                    : template;
        } else {
            message = "Validation error";
        }

        log.warn("ValidationException: code={}, msg={}", ex.getErrorCode().getCode(), message);
        return ResponseUtils.error(request, ex.getErrorCode(), message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseUtils.error(request, ErrorCode.INVALID_INPUT, msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("AccessDenied: {}", ex.getMessage());
        return ResponseUtils.error(request, ErrorCode.ACCESS_DENIED, i18n.get(ErrorCode.ACCESS_DENIED.getMessageKey()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("AuthorizationDenied: {}", ex.getMessage());
        return ResponseUtils.error(request, ErrorCode.ACCESS_DENIED, i18n.get(ErrorCode.ACCESS_DENIED.getMessageKey()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Object> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception: ", ex);
        return ResponseUtils.errorRaw(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                9999,
                "project-team121.9999",
                request != null ? request.getRequestURI() : null
        );
    }
}

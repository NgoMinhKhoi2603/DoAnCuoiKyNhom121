package com.example.ProjectTeam121.utils.constants;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class I18nUtil {
    private final MessageSource messageSource;

    public String get(String key, Object... args) {
        // Locale mặc định: vi-VN (bạn có thể truyền theo request nếu muốn)
        return messageSource.getMessage(key, args, key, new Locale("vi", "VN"));
    }
}
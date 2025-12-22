package com.example.ProjectTeam121.utils;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isValid(String value, PropertyDataType dataType) {
        if (value == null) return false;
        // Nếu database chưa set type (null) thì mặc định cho qua (hoặc chặn tùy bạn)
        if (dataType == null) return true;

        try {
            switch (dataType) {
                case NUMERIC:
                    // Chấp nhận số nguyên và số thực
                    Double.parseDouble(value);
                    return true;

                case BOOLEAN:
                    // Chấp nhận: true, false, 1, 0 (không phân biệt hoa thường)
                    return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")
                            || value.equals("1") || value.equals("0");

                case JSON:
                    // Check đúng cấu trúc JSON
                    objectMapper.readTree(value);
                    return true;

                case STRING:
                    // String chấp nhận tất cả
                    return true;

                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
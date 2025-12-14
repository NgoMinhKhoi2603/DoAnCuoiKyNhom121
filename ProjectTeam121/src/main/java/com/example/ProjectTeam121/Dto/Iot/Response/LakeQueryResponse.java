package com.example.ProjectTeam121.Dto.Iot.Response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LakeQueryResponse {
    private String deviceId;
    private String deviceName;
    private String propertyName;
    private String value;
    private String unit;
    private LocalDateTime timestamp;
    private String location; // Vị trí (Lấy từ file JSON)
    private String label;    // Nhãn (NORMAL, WARNING...) - Mới thêm
}
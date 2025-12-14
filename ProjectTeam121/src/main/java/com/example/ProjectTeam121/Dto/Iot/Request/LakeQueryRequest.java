package com.example.ProjectTeam121.Dto.Iot.Request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class LakeQueryRequest {
    // --- SỬA: Thay danh sách ID thiết bị bằng Loại thiết bị ---
    private String deviceTypeId;

    private List<String> propertyIds;

    // Các trường thời gian giữ nguyên
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;

    // Các trường vị trí giữ nguyên
    private String province;
    private String district;
    private String ward;
    private String specificLocation;
}
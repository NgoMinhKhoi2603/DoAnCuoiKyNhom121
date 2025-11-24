package com.example.ProjectTeam121.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatisticsResponse {
    private long totalUsers;
    private long activeUsers;    // Đã xác minh & không bị khóa
    private long onlineUsers;    // Đang online (hoạt động trong 5 phút gần đây)
    private long newUsersToday;  // Đăng ký hôm nay
}
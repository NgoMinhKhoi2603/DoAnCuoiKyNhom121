package com.example.ProjectTeam121.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;          // Ảnh đại diện
    private String unit;            // Mã khoa/đơn vị
    private String unitDescription; // Tên đầy đủ khoa/đơn vị
    private List<String> roles;     // Danh sách quyền
    private String token;           // JWT Token hiện tại

    private boolean enabled;
    private boolean locked;
    private boolean commentingLocked;

    private LocalDateTime createdAt;
    private LocalDateTime lastActive; // Thời gian online cuối
}
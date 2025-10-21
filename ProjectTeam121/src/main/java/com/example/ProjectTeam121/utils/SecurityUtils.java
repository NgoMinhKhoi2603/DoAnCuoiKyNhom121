package com.example.ProjectTeam121.utils;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {

    private SecurityUtils() {}

    /** Lấy username hiện tại từ SecurityContext; trả "system" nếu chưa đăng nhập */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }

    /** Có thể dùng khi cần lấy principal gốc (VD: để log) */
    public static Object getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getPrincipal() : null;
    }
}
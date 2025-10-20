package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String jwt = authHeader.substring(7);

        // Lấy thời gian hết hạn từ token
        Claims claims = jwtService.extractAllClaims(jwt);
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        long ttl = expirationTime - currentTime; // Time to live

        // Lưu token vào Redis blacklist với TTL
        if (ttl > 0) {
            redisTemplate.opsForValue().set("blacklist:" + jwt, "", Duration.ofMillis(ttl));
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
package com.example.ProjectTeam121.Repository;

import com.example.ProjectTeam121.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByFullNameContaining(String name);
    Boolean existsByEmail(String email);

    List<User> findByEnabledFalseAndCreatedAtBefore(LocalDateTime cutoffTime);
    Optional<User> findByEmail(String email);

    // Đếm user khả thi (Đã kích hoạt và Chưa bị khóa)
    long countByEnabledTrueAndLockedFalse();

    // Đếm user online (Có hoạt động sau thời gian X)
    long countByLastActiveAfter(LocalDateTime time);

    // Đếm user mới đăng ký (Tạo sau thời gian X)
    long countByCreatedAtAfter(LocalDateTime time);
}
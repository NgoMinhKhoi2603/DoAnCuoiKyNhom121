package com.example.DemoProjectTinasoft.Scheduler;

import com.example.DemoProjectTinasoft.Entity.User;
import com.example.DemoProjectTinasoft.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(UserCleanupTask.class);

    private final UserRepository userRepository;

    /**
     * Tác vụ này sẽ chạy vào lúc 2 giờ sáng mỗi ngày.
     * Nó sẽ tìm và xóa các tài khoản chưa được kích hoạt (enabled=false)
     * và đã được tạo ra hơn 24 giờ trước.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Chạy vào 2:00 AM mỗi ngày
    @Transactional
    public void cleanupUnactivatedUsers() {
        log.info("Bat dau tac vu don dep user chua kich hoat...");

        // Tính toán thời gian giới hạn (24 giờ trước)
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        // Tìm các user thỏa mãn điều kiện
        List<User> usersToDelete = userRepository.findByEnabledFalseAndCreatedAtBefore(cutoffTime);

        if (usersToDelete.isEmpty()) {
            log.info("Khong tim thay user nao can don dep.");
        } else {
            userRepository.deleteAll(usersToDelete);
            log.info("Da xoa thanh cong {} user chua kich hoat.", usersToDelete.size());
        }
    }
}

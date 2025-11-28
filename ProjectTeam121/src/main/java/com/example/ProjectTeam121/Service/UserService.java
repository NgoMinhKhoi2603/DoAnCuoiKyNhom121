package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Request.AvatarRequest;
import com.example.ProjectTeam121.Dto.Request.ChangePasswordRequest;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Dto.Response.UserStatisticsResponse;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.UserMapper;
import com.example.ProjectTeam121.Repository.RoleRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final HistoryService historyService;
    private final PasswordEncoder passwordEncoder;

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Helper tìm Role
    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ValidationException(ErrorCode.ROLE_NOT_FOUND, "error.role_not_found", roleName));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getPage(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userMapper.toUserResponsePage(userPage);
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return userMapper.toUserResponse(findUserByEmail(email));
    }

    @Transactional
    public UserResponse lockUser(String email) {
        User user = findUserByEmail(email);
        user.setLocked(true);
        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse unlockUser(String email) {
        User user = findUserByEmail(email);
        user.setLocked(false);
        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Gán một role cho user
     */
    @Transactional
    public UserResponse assignRole(String email, String roleName) {
        User user = findUserByEmail(email);
        Role role = findRoleByName(roleName);

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        // Ghi log (Nội dung log sẽ là "Gán role [roleName] cho [username]")
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Xoá một role khỏi user
     */
    @Transactional
    public UserResponse removeRole(String email, String roleName) {
        User user = findUserByEmail(email);
        Role role = findRoleByName(roleName);

        if (!user.getRoles().contains(role)) {
            // Nếu user không có role này thì không cần làm gì cả
            throw new ValidationException(ErrorCode.INVALID_INPUT, "User does not have this role");
        }

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);

        // Ghi log (Nội dung log sẽ là "Xoá role [roleName] khỏi [username]")
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Chặn khả năng bình luận của user
     */
    @Transactional
    public UserResponse lockCommenting(String email) {
        User user = findUserByEmail(email);
        user.setCommentingLocked(true);
        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Mở khóa khả năng bình luận của user
     */
    @Transactional
    public UserResponse unlockCommenting(String email) {
        User user = findUserByEmail(email);
        user.setCommentingLocked(false);
        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Cập nhật Avatar cho user đang đăng nhập
     */
    @Transactional
    public void updateAvatar(AvatarRequest request) {
        // SecurityUtils.getCurrentUsername() giờ trả về email
        String currentUserEmail = SecurityUtils.getCurrentUsername();
        User user = findUserByEmail(currentUserEmail);

        // Lưu chuỗi base64 vào database
        user.setAvatar(request.getBase64Image());

        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), currentUserEmail);
    }

    /**
     * Thay đổi mật khẩu
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String currentUserEmail = SecurityUtils.getCurrentUsername();
        User user = findUserByEmail(currentUserEmail);

        // 1. Kiểm tra mật khẩu cũ (Quan trọng)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            // SỬA: Dùng mã lỗi cụ thể
            throw new ValidationException(ErrorCode.PASSWORD_NOT_CORRECT, "Mật khẩu hiện tại không đúng");
        }

        // 2. Kiểm tra xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            // SỬA: Dùng mã lỗi cụ thể
            throw new ValidationException(ErrorCode.PASSWORD_CONFIRMATION_INCORRECT, "Mật khẩu xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                "Change Password", currentUserEmail);
    }

    /**
     * Thống kê
     */
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        // 1. Tổng số người dùng
        long total = userRepository.count();

        // 2. Người dùng khả thi (Enabled=true, Locked=false)
        long active = userRepository.countByEnabledTrueAndLockedFalse();

        // 3. Người dùng Online (Hoạt động trong 5 phút gần đây)
        long online = userRepository.countByLastActiveAfter(LocalDateTime.now().minusMinutes(5));

        // 4. Người dùng mới hôm nay (Từ 00:00 sáng nay)
        long news = userRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());

        return UserStatisticsResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .onlineUsers(online)
                .newUsersToday(news)
                .build();
    }

    /**
     * Cập nhật Last Active, hàm này sẽ được gọi ngầm mỗi khi user request
     */
    @Transactional
    public void updateLastActive(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
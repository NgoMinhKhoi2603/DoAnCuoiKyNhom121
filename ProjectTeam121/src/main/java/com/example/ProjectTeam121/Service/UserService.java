package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Thêm RoleRepository
    private final UserMapper userMapper;
    private final HistoryService historyService;

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
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
    public UserResponse findByUsername(String username) {
        return userMapper.toUserResponse(findUserByUsername(username));
    }

    @Transactional
    public UserResponse lockUser(String username) {
        User user = findUserByUsername(username);
        user.setLocked(true);
        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse unlockUser(String username) {
        User user = findUserByUsername(username);
        user.setLocked(false);
        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Gán một role cho user
     */
    @Transactional
    public UserResponse assignRole(String username, String roleName) {
        User user = findUserByUsername(username);
        Role role = findRoleByName(roleName);

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        // Ghi log (Nội dung log sẽ là "Gán role [roleName] cho [username]")
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Xoá một role khỏi user
     */
    @Transactional
    public UserResponse removeRole(String username, String roleName) {
        User user = findUserByUsername(username);
        Role role = findRoleByName(roleName);

        if (!user.getRoles().contains(role)) {
            // Nếu user không có role này thì không cần làm gì cả
            throw new ValidationException(ErrorCode.INVALID_INPUT, "User does not have this role");
        }

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);

        // Ghi log (Nội dung log sẽ là "Xoá role [roleName] khỏi [username]")
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Chặn khả năng bình luận của user
     */
    @Transactional
    public UserResponse lockCommenting(String username) {
        User user = findUserByUsername(username);
        user.setCommentingLocked(true);
        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }

    /**
     * Mở khóa khả năng bình luận của user
     */
    @Transactional
    public UserResponse unlockCommenting(String username) {
        User user = findUserByUsername(username);
        user.setCommentingLocked(false);
        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getUsername(), SecurityUtils.getCurrentUsername());

        return userMapper.toUserResponse(savedUser);
    }
}
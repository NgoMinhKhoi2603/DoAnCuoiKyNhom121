package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import com.example.ProjectTeam121.Dto.Request.AvatarRequest;
import com.example.ProjectTeam121.Dto.Request.ChangePasswordRequest;
import com.example.ProjectTeam121.Dto.Request.UpdateUserRequest;
import com.example.ProjectTeam121.Dto.Response.UserResponse;
import com.example.ProjectTeam121.Dto.Response.UserStatisticsResponse;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.UserMapper;
import com.example.ProjectTeam121.Repository.CommentRepository;
import com.example.ProjectTeam121.Repository.HistoryRepository;
import com.example.ProjectTeam121.Repository.Iot.DeviceRepository;
import com.example.ProjectTeam121.Repository.RoleRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final HistoryService historyService;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;
    private final HistoryRepository historyRepository;
    private final DeviceRepository deviceRepository;

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

        return userPage.map(user -> {
            UserResponse response = userMapper.toUserResponse(user);

            long deviceCount = deviceRepository.countByCreatedBy(user.getEmail());

            response.setDeviceCount(deviceCount);

            return response;
        });
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
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Lock a user");

        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse unlockUser(String email) {
        User user = findUserByEmail(email);
        user.setLocked(false);
        User savedUser = userRepository.save(user);

        // Ghi log lịch sử
        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Unlock a user");

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
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Add role for account");

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
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Remove role from account");

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
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Lock commenting of a account");

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
                savedUser.getEmail(), SecurityUtils.getCurrentUsername(), "Unlock commenting of a account");

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
                savedUser.getEmail(), currentUserEmail, "Update avatar");
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
                currentUserEmail, currentUserEmail, "Change Password");
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
        userRepository.updateLastActive(email, LocalDateTime.now());
    }

    /**
     * Chức năng: Người dùng tự khóa tài khoản (Self Deactivate)
     */
    @Transactional
    public void deactivateAccount(String password) {
        String currentEmail = SecurityUtils.getCurrentUsername();
        User user = findUserByEmail(currentEmail);

        // 1. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException(ErrorCode.PASSWORD_NOT_CORRECT, "Mật khẩu không đúng, không thể khóa tài khoản");
        }

        // 2. Set trạng thái TỰ KHÓA (deactivated)
        user.setDeactivated(true);

        userRepository.save(user);

        // 3. Ghi log
        historyService.saveHistory(user, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                currentEmail, currentEmail, "User self-deactivated account");
    }

    /**
     * Chức năng 2: Xóa sạch dữ liệu nhưng giữ tài khoản (Reset Data)
     * Yêu cầu xác nhận mật khẩu
     */
    @Transactional
    public void deleteAllUserData(String password) {
        String currentEmail = SecurityUtils.getCurrentUsername();
        User user = findUserByEmail(currentEmail);

        // 1. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException(ErrorCode.PASSWORD_NOT_CORRECT, "Mật khẩu không đúng, không thể xóa dữ liệu");
        }

        // 2. Xóa dữ liệu liên quan
        // Xóa Comment
        commentRepository.deleteByCreatedBy(currentEmail);
        // Xóa History
        historyRepository.deleteByCreatedBy(currentEmail);

        // 3. Reset thông tin cá nhân về mặc định
        user.setAvatar("uploads/noimage.png");
        userRepository.save(user);

        // 4. Ghi log
        historyService.saveHistory(user, ActionLog.DELETE, HistoryType.USER_MANAGEMENT,
                currentEmail, currentEmail, "User wiped all data");
    }

    @Transactional
    public UserResponse updateUserInfo(UpdateUserRequest request, String email) {
        User user = findUserByEmail(email);

        user.setFullName(request.getFullName());
        user.setUnit(request.getUnit());

        User savedUser = userRepository.save(user);

        historyService.saveHistory(savedUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(), email, "Update information of account");

        return userMapper.toUserResponse(savedUser);
    }


    /**
     * Import User từ file Excel (.xlsx)
     * Cấu trúc file: Cột A: Email, Cột B: Họ tên, Cột C: Đơn vị (Unit), Cột D: Role (USER/ADMIN)
     */
    @Transactional
    public String importUsersFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "File không được để trống");
        }

        List<User> newUsers = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên

            // Duyệt từng dòng (Bỏ qua dòng tiêu đề index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 1. Đọc dữ liệu từ các cột
                String email = getCellValue(row.getCell(0));
                String fullName = getCellValue(row.getCell(1));
                String unitStr = getCellValue(row.getCell(2));
                String roleName = getCellValue(row.getCell(3)).toUpperCase();

                // 2. Validate
                if (email.isEmpty()) {
                    skipCount++;
                    continue;
                }

                if (fullName.length() < 3 || fullName.length() > 100) {
                    skipCount++;
                    continue;
                }

                // 3. Kiểm tra email đã tồn tại chưa
                if (userRepository.existsByEmail(email)) {
                    skipCount++;
                    continue;
                }

                // 4. Tạo User mới
                User user = new User();
                user.setEmail(email);
                user.setFullName(fullName);
                user.setUnit(parseUnitEnum(unitStr));
                user.setEnabled(true);
                user.setLocked(false);
                user.setAvatar("uploads/noimage.png"); // Avatar mặc định

                // Mật khẩu mặc định là 123456
                user.setPassword(passwordEncoder.encode("123456"));

                // Xử lý Role (Mặc định là USER nếu không tìm thấy hoặc để trống)
                // 1. Luôn gán quyền USER trước (Mặc định ai cũng phải có)
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new ValidationException(ErrorCode.ROLE_NOT_FOUND,
                                "Lỗi hệ thống: Không tìm thấy 'ROLE_USER'. Vui lòng kiểm tra database."));

                user.getRoles().add(userRole);

                // 2. Kiểm tra file Excel, nếu là ADMIN thì gán thêm quyền ADMIN
                if (!roleName.isEmpty()) {
                    String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

                    if (normalized.equals("ROLE_ADMIN")) {
                        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseThrow(() -> new ValidationException(ErrorCode.ROLE_NOT_FOUND,
                                        "Lỗi hệ thống: Không tìm thấy 'ROLE_ADMIN'. Vui lòng kiểm tra database."));

                        // Thêm quyền Admin vào danh sách quyền (Lúc này user có 2 role)
                        user.getRoles().add(adminRole);
                    }
                }

                newUsers.add(user);
                successCount++;
            }

            // Lưu tất cả vào DB
            userRepository.saveAll(newUsers);

            if (successCount > 0) {
                String currentEmail = SecurityUtils.getCurrentUsername();
                User adminUser = findUserByEmail(currentEmail);

                String description = String.format("Imported %d users from Excel file", successCount);
                String identify = String.format("%d new users", successCount);

                historyService.saveHistory(adminUser, ActionLog.CREATE, HistoryType.USER_MANAGEMENT,
                        identify, currentEmail, description
                );
            }

        } catch (IOException e) {
            throw new ValidationException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Lỗi đọc file Excel");
        }

        return String.format("Đã thêm thành công %d người dùng. Bỏ qua %d người dùng (do trùng Email hoặc lỗi dữ liệu).", successCount, skipCount);
    }

    // Helper để lấy giá trị text từ ô Excel tránh NullPointerException
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    // Helper method để convert String -> UnitEnum an toàn
    private UnitEnum parseUnitEnum(String unitName) {
        if (unitName == null || unitName.trim().isEmpty()) {
            return null; // Hoặc trả về giá trị mặc định nếu có, ví dụ: UnitEnum.OTHER
        }
        try {
            // Cố gắng tìm Enum có tên trùng khớp (Không phân biệt hoa thường)
            return UnitEnum.valueOf(unitName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Nếu trong file Excel điền linh tinh không khớp enum nào -> trả về null hoặc mặc định
            return null;
        }
    }
}
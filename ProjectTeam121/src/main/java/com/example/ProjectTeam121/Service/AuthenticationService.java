package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import com.example.ProjectTeam121.Dto.Request.*;
import com.example.ProjectTeam121.Dto.Response.AuthenticationResponse;
import com.example.ProjectTeam121.Dto.Response.CurrentUserResponse;
import com.example.ProjectTeam121.Mapper.UserMapper;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.RoleRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Security.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ProjectTeam121.Entity.VerificationToken;
import com.example.ProjectTeam121.Repository.VerificationTokenRepository;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final HistoryService historyService;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Value("${application.backend.url}")
    private String backendUrl;

    private static final Path AVATAR_FOLDER = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Kiểm tra Email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException(ErrorCode.EMAIL_EXISTS, "Email đã được sử dụng");
        }

        var user = new User();
        user.setFullName(request.getFullName()); // Set FullName
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUnit((request.getUnit()));
        user.setAvatar("uploads/noimage.png");

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ValidationException(ErrorCode.ROLE_NOT_FOUND, "Không tìm thấy quyền hạn mặc định (ROLE_USER)"));
        roles.add(userRole);
        user.setRoles(roles);

        user.setEnabled(false);
        user.setLocked(false);

        User savedUser = userRepository.save(user);

        // THÊM: Tạo Verification Token và lưu xuống DB
        VerificationToken verificationToken = new VerificationToken(savedUser);
        tokenRepository.save(verificationToken);

        // THÊM: Gửi email (Giả sử server chạy localhost:8080)
        String activationLink = "http://localhost:8080/api/v1/auth/activate?token=" + verificationToken.getToken();
        emailService.sendActivationEmail(savedUser.getEmail(), "Kích hoạt tài khoản ProjectTeam121", activationLink);

        // Ghi log
        historyService.saveHistory(
                savedUser,
                ActionLog.CREATE,
                HistoryType.USER_MANAGEMENT,
                savedUser.getEmail(),
                savedUser.getEmail()
        );

        return AuthenticationResponse.builder()
                .message("Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.")
                .build();
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Spring Security sẽ kiểm tra username/password và check User.isAccountNonLocked() (tức là check trường locked của Admin)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // 2. Kiểm tra thêm: Nếu User tự khóa (deactivated) -> Chặn đăng nhập và báo lỗi riêng
        if (user.isDeactivated()) {
            throw new ValidationException(ErrorCode.ACCOUNT_DEACTIVATED,
                    "Tài khoản đang bị vô hiệu hóa bởi người dùng. Vui lòng sử dụng chức năng mở khóa tài khoản.");
        }

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("Đăng nhập thành công")
                .build();
    }

    @Transactional
    public String activateAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_INPUT, "Mã kích hoạt không hợp lệ hoặc đã hết hạn"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Mã kích hoạt đã hết hạn");
        }

        User user = verificationToken.getUser();

        // Kiểm tra nếu đã kích hoạt rồi thì báo thành công luôn (tránh lỗi 1007)
        if (user.isEnabled()) {
            return "Tài khoản của bạn đã được kích hoạt trước đó. Vui lòng đăng nhập.";
        }

        user.setEnabled(true);
        userRepository.save(user);

        return "Kích hoạt tài khoản thành công!";
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException(ErrorCode.RESOURCE_NOT_FOUND, "Email không tồn tại trong hệ thống"));

        // Xóa token cũ nếu có để tránh lỗi Unique Constraint
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Tạo token mới
        VerificationToken token = new VerificationToken(user);
        tokenRepository.save(token);

        String resetLink = frontendUrl + "/reset-password?token=" + token.getToken();
        emailService.sendResetPasswordEmail(user.getEmail(), "Yêu cầu đặt lại mật khẩu", resetLink);

        return "Link đặt lại mật khẩu đã được gửi đến email của bạn.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new ValidationException(ErrorCode.PASSWORD_CONFIRMATION_INCORRECT, "Mật khẩu xác nhận không khớp");
        }

        VerificationToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_INPUT, "Token không hợp lệ hoặc không tồn tại"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Token đã hết hạn");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa token sau khi dùng
        tokenRepository.delete(token);

        historyService.saveHistory(user, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                "Reset Password via Email", user.getEmail());

        return "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay bây giờ.";
    }

    @Transactional
    public String changePassword(ChangePasswordRequest request) {

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new ValidationException(ErrorCode.PASSWORD_CONFIRMATION_INCORRECT,
                    "Mật khẩu xác nhận không khớp");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        // Log lại
        historyService.saveHistory(currentUser, ActionLog.UPDATE, HistoryType.USER_MANAGEMENT,
                "User Changed Password", currentUser.getEmail());

        return "Đổi mật khẩu thành công!";
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String token) {
        // Lấy email từ SecurityContext
        String email = SecurityUtils.getCurrentUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Map sang DTO response
        CurrentUserResponse response = userMapper.toCurrentUserResponse(user);

        // Gán token hiện tại vào response
        response.setToken(token);

        return response;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // 1. xử lý Yêu cầu mở khóa
    @Transactional
    public void requestReactivation(ReactivateRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!user.isDeactivated()) {
            throw new RuntimeException("Tài khoản này không bị khóa");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        VerificationToken existing = tokenRepository.findByUserId(user.getId());
        if (existing != null) {
            existing.setToken(token);
            existing.setExpiryDate(expiry);
            tokenRepository.save(existing);
        } else {
            VerificationToken vt = new VerificationToken();
            vt.setToken(token);
            vt.setExpiryDate(expiry);
            vt.setUser(user);
            tokenRepository.save(vt);
        }

        String link = backendUrl + "/api/v1/auth/confirm-reactivation?token=" + token;

        Map<String, Object> model = new HashMap<>();
        model.put("reactivateLink", link);

        emailService.sendTemplateEmail(
                user.getEmail(),
                "Yêu cầu mở khóa tài khoản",
                "email/reactivation-request",
                model
        );
    }


    // 2. Xác nhận token để mở khóa
    @Transactional
    public void confirmReactivation(String token) {

        VerificationToken verify = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_INPUT, "Mã không hợp lệ"));

        if (verify.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Mã đã hết hạn");
        }

        User user = verify.getUser();
        user.setDeactivated(false);
        userRepository.save(user);

        tokenRepository.delete(verify);

        Map<String, Object> model = new HashMap<>();
        model.put("fullName", user.getFullName());

        emailService.sendTemplateEmail(
                user.getEmail(),
                "Tài khoản đã được mở khóa",
                "email/reactivation-success",
                model
        );
    }



    @Transactional
    public CurrentUserResponse updateCurrentUser(UpdateUserRequest request, MultipartFile avatarFile) {

        String email = SecurityUtils.getCurrentUsername();
        User user = findUserByEmail(email);

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());

        if (request.getUnit() != null)
            user.setUnit(request.getUnit());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveAvatar(avatarFile);
            user.setAvatar(avatarUrl);
        }
        userRepository.save(user);

        return userMapper.toCurrentUserResponse(user);
    }


    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(AVATAR_FOLDER)) {
                Files.createDirectories(AVATAR_FOLDER);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload folder", e);
        }
    }

    public String saveAvatar(MultipartFile file) {
        try {
            System.out.println("Avatar folder = " + AVATAR_FOLDER.toAbsolutePath());

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = AVATAR_FOLDER.resolve(fileName);

            System.out.println("Saving to = " + filePath.toAbsolutePath());
            System.out.println("Exists? " + Files.exists(AVATAR_FOLDER));

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error saving avatar", e);
        }
    }
}
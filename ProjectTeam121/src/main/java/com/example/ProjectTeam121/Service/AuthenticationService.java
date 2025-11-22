package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import com.example.ProjectTeam121.Dto.Request.AuthenticationRequest;
import com.example.ProjectTeam121.Dto.Response.AuthenticationResponse;
import com.example.ProjectTeam121.Dto.Request.RegisterRequest;
import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.RoleRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.Security.JwtService;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ProjectTeam121.Entity.VerificationToken;
import com.example.ProjectTeam121.Repository.VerificationTokenRepository;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

//    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        var user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUnit(resolveUnitEnum(request.getUnit()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
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
                savedUser.getUsername(),
                savedUser.getUsername()
        );

        // Lưu ý: Không trả về JWT token ngay lập tức vì tài khoản chưa kích hoạt
        // Hoặc trả về thông báo yêu cầu check mail. Ở đây mình vẫn trả về cấu trúc cũ nhưng token null hoặc message
        return AuthenticationResponse.builder().token("Please check your email to activate account").build();
    }

    private UnitEnum resolveUnitEnum(String input) {
        try {
            return UnitEnum.valueOf(input.toUpperCase());
        } catch (Exception ignored) {}

        // Trường hợp FE gửi mô tả đầy đủ
        return Arrays.stream(UnitEnum.values())
                .filter(u -> u.getDescription().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid unit: " + input));
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    @Transactional
    public String activateAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_INPUT, "Invalid activation token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException(ErrorCode.INVALID_INPUT, "Token has expired");
        }

        User user = verificationToken.getUser();
        if (user.isEnabled()) {
            return "Account is already activated";
        }

        user.setEnabled(true);
        userRepository.save(user);

        // Xóa token sau khi dùng xong
        tokenRepository.delete(verificationToken);

        return "Account activated successfully";
    }
}
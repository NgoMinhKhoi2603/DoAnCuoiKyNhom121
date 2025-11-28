package com.example.ProjectTeam121.Config;

import com.example.ProjectTeam121.Entity.Role;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Repository.RoleRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Bat dau khoi tao du lieu mau...");

        // Tạo Role
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_ADMIN");

        // Tạo Admin User
        createAdminUserIfNotFound();

        log.info("Hoan tat khoi tao du lieu");
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(name));
            log.info("Da tao Role: {}", name);
        }
    }

    private void createAdminUserIfNotFound() {
        if (!userRepository.existsByEmail("admin@example.com")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Khong tim thay ROLE_ADMIN"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User adminUser = new User();
            adminUser.setFullName("Administrator");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setRoles(roles);
            adminUser.setEnabled(true);
            adminUser.setLocked(false);
            adminUser.setCommentingLocked(false);

            userRepository.save(adminUser);
            log.info("Da tao tai khoan Admin: admin@example.com / 123456");
        }
    }
}
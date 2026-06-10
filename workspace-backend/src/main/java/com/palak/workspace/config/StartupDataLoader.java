package com.palak.workspace.config;

import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import com.palak.workspace.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class StartupDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StartupDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createSuperAdmin() {

        if (userRepository.findByEmail("admin@workspace.com").isPresent()) {
            return;
        }

        User admin = new User();

        admin.setFirstName("Platform");
        admin.setLastName("Admin");
        admin.setEmail("admin@workspace.com");

        admin.setPassword(passwordEncoder.encode("Admin@123"));

        admin.setRole(UserRole.SUPER_ADMIN);

        admin.setTenantId(null);
        admin.setOrganizationCode(null);
        admin.setEmployeeId(null);
        admin.setIsActive(true);

        userRepository.save(admin);

        System.out.println("SUPER ADMIN CREATED");
    }
}
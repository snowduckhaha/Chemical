package com.qidian.site.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminUserInitializer implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final String adminUsername;
    private final String adminPasswordHash;
    private final String operatorUsername;
    private final String operatorPasswordHash;

    public InitialAdminUserInitializer(
        AdminUserRepository adminUserRepository,
        @Value("${site.auth.initial-admin-username:}") String adminUsername,
        @Value("${site.auth.initial-admin-password-hash:}") String adminPasswordHash,
        @Value("${site.auth.initial-operator-username:}") String operatorUsername,
        @Value("${site.auth.initial-operator-password-hash:}") String operatorPasswordHash
    ) {
        this.adminUserRepository = adminUserRepository;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = adminPasswordHash;
        this.operatorUsername = operatorUsername;
        this.operatorPasswordHash = operatorPasswordHash;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfConfigured(adminUsername, adminPasswordHash, AdminRole.ADMIN);
        createIfConfigured(operatorUsername, operatorPasswordHash, AdminRole.OPERATOR);
    }

    private void createIfConfigured(String username, String passwordHash, AdminRole role) {
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            return;
        }
        if (!passwordHash.matches("^\\$2[aby]\\$12\\$.+")) {
            throw new IllegalStateException("Configured initial password hash must use BCrypt strength 12");
        }
        adminUserRepository.createIfMissing(username.trim(), passwordHash, role);
    }
}
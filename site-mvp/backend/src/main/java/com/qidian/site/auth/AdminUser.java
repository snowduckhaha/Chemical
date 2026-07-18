package com.qidian.site.auth;

import java.time.LocalDateTime;

public record AdminUser(
    long id,
    String username,
    String passwordHash,
    AdminRole role,
    boolean enabled,
    boolean mustChangePassword,
    LocalDateTime lastLoginAt
) {
}
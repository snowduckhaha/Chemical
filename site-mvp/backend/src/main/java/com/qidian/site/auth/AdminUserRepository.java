package com.qidian.site.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AdminUserRepository {

    private static final RowMapper<AdminUser> ROW_MAPPER = (rs, rowNum) -> new AdminUser(
        rs.getLong("id"),
        rs.getString("username"),
        rs.getString("password_hash"),
        AdminRole.valueOf(rs.getString("role_code")),
        rs.getBoolean("enabled"),
        rs.getBoolean("must_change_password"),
        rs.getTimestamp("last_login_at") == null ? null : rs.getTimestamp("last_login_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public AdminUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return jdbcTemplate.query("""
            SELECT id, username, password_hash, role_code, enabled, must_change_password, last_login_at
            FROM admin_user WHERE username = ?
            """, ROW_MAPPER, username).stream().findFirst();
    }

    public void createIfMissing(String username, String passwordHash, AdminRole role) {
        if (findByUsername(username).isPresent()) {
            return;
        }
        jdbcTemplate.update("""
            INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
            VALUES (?, ?, ?, TRUE, TRUE)
            """, username, passwordHash, role.name());
    }

    public void updateLastLogin(String username) {
        jdbcTemplate.update("UPDATE admin_user SET last_login_at = CURRENT_TIMESTAMP WHERE username = ?", username);
    }
}
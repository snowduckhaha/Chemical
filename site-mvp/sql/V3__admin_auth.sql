-- Backend P0 authentication migration (MySQL 8)
-- Initial credentials are supplied only through SITE_* environment variables.

CREATE TABLE IF NOT EXISTS admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(128) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role_code VARCHAR(16) NOT NULL COMMENT 'ADMIN|OPERATOR',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  must_change_password TINYINT(1) NOT NULL DEFAULT 1,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_admin_user_username (username),
  KEY idx_admin_user_role_enabled (role_code, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
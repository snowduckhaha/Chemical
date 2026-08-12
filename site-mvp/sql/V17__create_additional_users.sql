-- ============================================================
-- 重置后台管理员账号密码（生产环境执行）
-- 用法：
--   docker compose --env-file .env -f deploy/docker-compose.prod.yml exec -T mysql \
--     mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site < sql/V17__create_additional_users.sql
-- ============================================================

-- 1. admin 账号（ADMIN 角色，全部权限）
--    密码: rost*2026@
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('admin', '$2b$12$r72NP7i9fcjU/Mm3bphljeJmPycOwNbhvvDjCCr8CZcjR6dWolS62', 'ADMIN', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);

-- 2. operator 账号（OPERATOR 角色，内容编辑权限）
--    密码: user*2026#
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('operator', '$2b$12$UyWREtpCN/dUtZF6bwjpvukAdmiI3bCSgWjR0eGPXizMm8DEM4RzG', 'OPERATOR', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);


-- 3. zelin 账号（ADMIN 角色，全部权限）
--    密码: 2661136@ZZL
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('zelin', '$2a$12$tZhU4/KCGVA4DZe.PSr6O.ud3LT12qU4ujvaoBR5J.L9ab4j/3sgG', 'ADMIN', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);
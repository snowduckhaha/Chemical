-- ============================================================
-- 新增后台管理员账号（生产环境执行）
-- 用法：
--   docker compose --env-file .env -f deploy/docker-compose.prod.yml exec -T mysql \
--     mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site < sql/V17__create_additional_users.sql
-- ============================================================

-- 1. admin 账号（ADMIN 角色，全部权限）
--    密码: rost*2026@
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('admin', '$2a$12$uIbVHT6L3xo.P1L3EDpDa.XRk9bvJ8G5iuXrjiDl0wk/qFSuTqohm', 'ADMIN', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);

-- 2. operator 账号（OPERATOR 角色，内容编辑权限）
--    密码: user*2026#
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('operator', '$2a$12$C1BHSJOS7HrbwjGINk71LuFDBqY6s1GHgZ4Op/qk.ukk0dUY8bAI6', 'OPERATOR', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);
-- ============================================================
-- 新增后台管理员账号（生产环境执行）
-- 用法：
--   docker compose --env-file .env -f deploy/docker-compose.prod.yml exec -T mysql \
--     mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site < sql/V17__create_additional_users.sql
-- ============================================================

-- 1. admin 账号（ADMIN 角色，全部权限）
--    密码: rost*2026@
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('admin', '$2a$12$g3EFqtelndtuSG1.pfEzm.oYu4wZP6yIe16z9kJ2.C0KZE8.inVh.', 'ADMIN', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);

-- 2. operator 账号（OPERATOR 角色，内容编辑权限）
--    密码: user*2026#
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('operator', '$2a$12$HAd/rJj6i5MdUJeA5JjRbuAM6MbmWW1KC2gAN8uCaopopgAscwpb6', 'OPERATOR', 1, 0)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = VALUES(enabled);

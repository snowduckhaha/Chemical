#!/usr/bin/env bash
set -Eeuo pipefail

# ============================================================
# 首次发布脚本 — 包含数据库初始化
# 仅在新服务器或需要重建数据库时使用
# 日常发布请使用 deploy.sh
# ============================================================

APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a

# ---------- 1. 准备目录 ----------
echo "==> 创建数据目录..."
mkdir -p "$UPLOADS_DIR" "$MYSQL_DATA_DIR"
chown 999:999 "$MYSQL_DATA_DIR"
chmod 0700 "$MYSQL_DATA_DIR"

# ---------- 2. 登录 GHCR ----------
if [[ -n "${GHCR_USERNAME:-}" && -n "${GHCR_TOKEN:-}" ]]; then
  echo "==> 登录 GHCR..."
  printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

# ---------- 3. 拉取镜像 ----------
echo "==> 拉取镜像..."
docker compose --env-file .env -f "$COMPOSE_FILE" pull

# ---------- 4. 启动 MySQL 并等待就绪 ----------
echo "==> 启动 MySQL..."
docker compose --env-file .env -f "$COMPOSE_FILE" up -d mysql

echo "==> 等待 MySQL 就绪..."
for _ in $(seq 1 60); do
  if docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
    mysqladmin ping -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent 2>/dev/null; then
    break
  fi
  sleep 2
done

# 最终确认 MySQL 可用
docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
  mysqladmin ping -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent

# ---------- 5. 执行 SQL 初始化 ----------
echo "==> 执行数据库初始化脚本..."
sql_scripts=(
  mvp_schema.sql product_center_schema_v2.sql V3__admin_auth.sql V4__admin_uploads.sql
  V5__product_soft_delete.sql V6__inquiry_admin.sql V7__certificate_management.sql
  V8__seo_admin.sql V9__analytics.sql V10__product_content_fields.sql
  V11__inquiry_lead_score.sql V12__news_admin.sql V13__repair_seed_media_urls.sql
  V14__application_field_configuration.sql V15__seed_initial_news_categories.sql
  V16__temporary_news_reference_assets.sql mvp_seed.sql
)
for script in "${sql_scripts[@]}"; do
  echo "    执行: $script"
  docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
    mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site <"$APP_DIR/sql/$script"
done

# ---------- 6. 启动全部服务 ----------
echo "==> 启动全部服务..."
docker compose --env-file .env -f "$COMPOSE_FILE" up -d --remove-orphans

# ---------- 7. 健康检查 ----------
echo "==> 等待后端服务就绪..."
for _ in $(seq 1 24); do
  if curl --fail --silent --show-error --retry 1 "http://${SITE_DOMAIN}/api/v1/health" 2>/dev/null; then
    break
  fi
  sleep 5
done

curl --fail --silent --show-error --retry 3 --retry-delay 5 "http://${SITE_DOMAIN}/api/v1/health"

# ---------- 8. 清理 ----------
docker image prune -f

echo ""
echo "============================================"
echo "  首次发布完成！"
echo "  数据库已初始化，所有服务已启动。"
echo "  后续日常发布请使用: deploy.sh"
echo "============================================"

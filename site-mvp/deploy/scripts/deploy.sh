#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a
git fetch origin main
git checkout main
git pull --ff-only origin main

mkdir -p "$UPLOADS_DIR" "$MYSQL_DATA_DIR"
chown 999:999 "$MYSQL_DATA_DIR"
chmod 0700 "$MYSQL_DATA_DIR"
docker compose --env-file .env -f "$COMPOSE_FILE" build --pull backend frontend
docker compose --env-file .env -f "$COMPOSE_FILE" up -d mysql

for _ in $(seq 1 60); do
  if docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent; then
    break
  fi
  sleep 2
done

docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent

sql_scripts=(
  mvp_schema.sql product_center_schema_v2.sql V3__admin_auth.sql V4__admin_uploads.sql
  V5__product_soft_delete.sql V6__inquiry_admin.sql V7__certificate_management.sql
  V8__seo_admin.sql V9__analytics.sql V10__product_content_fields.sql
  V11__inquiry_lead_score.sql V12__news_admin.sql V13__repair_seed_media_urls.sql
  V14__application_field_configuration.sql V15__seed_initial_news_categories.sql
  V16__temporary_news_reference_assets.sql mvp_seed.sql
)
for script in "${sql_scripts[@]}"; do
  docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
    mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site <"$APP_DIR/sql/$script"
done

docker compose --env-file .env -f "$COMPOSE_FILE" up -d --remove-orphans
curl --fail --silent --show-error --retry 12 --retry-delay 5 "https://${SITE_DOMAIN}/api/v1/health"
docker image prune -f

echo "Deployment completed successfully."

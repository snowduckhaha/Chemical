#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"
BACKUP_DIR="${BACKUP_DIR:-/opt/chemical/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a

stamp="$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$BACKUP_DIR/mysql" "$BACKUP_DIR/uploads"

docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
  mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --single-transaction --routines --triggers --events qidian_site \
  | gzip >"$BACKUP_DIR/mysql/qidian_site_${stamp}.sql.gz"

tar -C "$(dirname "$UPLOADS_DIR")" -czf "$BACKUP_DIR/uploads/uploads_${stamp}.tar.gz" "$(basename "$UPLOADS_DIR")"

find "$BACKUP_DIR/mysql" "$BACKUP_DIR/uploads" -type f -mtime "+$RETENTION_DAYS" -delete

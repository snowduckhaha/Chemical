#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"

cd "$APP_DIR"
docker run --rm \
  -v /etc/letsencrypt:/etc/letsencrypt \
  -v /var/www/certbot:/var/www/certbot \
  certbot/certbot renew --webroot -w /var/www/certbot --quiet

docker compose --env-file .env -f "$COMPOSE_FILE" exec -T gateway nginx -s reload

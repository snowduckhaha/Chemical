#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a

# Package-only deploy mode is the default. Enable git sync only when explicitly requested.
if [[ "${ENABLE_GIT_SYNC:-0}" == "1" ]]; then
  if [[ ! -d .git ]]; then
    echo "ENABLE_GIT_SYNC=1 but current directory is not a git repository: $APP_DIR" >&2
    exit 1
  fi
  git fetch origin main
  git checkout main
  git pull --ff-only origin main
else
  echo "Skipping git sync (ENABLE_GIT_SYNC != 1)."
fi

mkdir -p "$UPLOADS_DIR" "$MYSQL_DATA_DIR"

if [[ -n "${GHCR_USERNAME:-}" && -n "${GHCR_TOKEN:-}" ]]; then
  printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
fi

docker compose --env-file .env -f "$COMPOSE_FILE" pull backend frontend
docker compose --env-file .env -f "$COMPOSE_FILE" up -d --remove-orphans

# Wait for backend health (up to 120s)
for _ in $(seq 1 24); do
  if curl --fail --silent --show-error --retry 1 "http://${SITE_DOMAIN}/api/v1/health"; then
    break
  fi
  sleep 5
done

curl --fail --silent --show-error --retry 3 --retry-delay 5 "http://${SITE_DOMAIN}/api/v1/health"
docker image prune -f

echo "Deployment completed successfully."

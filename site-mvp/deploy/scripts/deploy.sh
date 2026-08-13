#!/usr/bin/env bash
set -Eeuo pipefail

# Production deployment runs entirely on this host. It fast-forwards the
# configured branch, builds Docker images from that exact commit, and lets the
# frontend image pre-render from backend:8080 on the private Docker network.
APP_DIR="${APP_DIR:-/opt/chemical/app}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"
SOURCE_REF="${DEPLOY_REF:-origin/main}"
AUTO_ROLLBACK="${AUTO_ROLLBACK:-1}"

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a

SOURCE_DIR="$(git -C "$APP_DIR" rev-parse --show-toplevel)"
if ! git -C "$SOURCE_DIR" diff --quiet || ! git -C "$SOURCE_DIR" diff --cached --quiet; then
  echo "Refusing to deploy: the production checkout has tracked local changes." >&2
  exit 1
fi
previous_source_commit="$(git -C "$SOURCE_DIR" rev-parse HEAD)"

git -C "$SOURCE_DIR" fetch --prune origin main
if [[ "$SOURCE_REF" == "origin/main" ]]; then
  git -C "$SOURCE_DIR" checkout main
  git -C "$SOURCE_DIR" merge --ff-only origin/main
else
  resolved_ref="$(git -C "$SOURCE_DIR" rev-parse --verify "${SOURCE_REF}^{commit}")"
  git -C "$SOURCE_DIR" checkout --detach "$resolved_ref"
fi

RESOLVED_COMMIT="$(git -C "$SOURCE_DIR" rev-parse HEAD)"
BUILD_TAG="${RESOLVED_COMMIT:0:12}"
export BUILD_TAG

previous_backend_id="$(docker compose --env-file .env -f "$COMPOSE_FILE" ps -q backend 2>/dev/null || true)"
previous_frontend_id="$(docker compose --env-file .env -f "$COMPOSE_FILE" ps -q frontend 2>/dev/null || true)"
previous_backend_image=""
previous_frontend_image=""
if [[ -n "$previous_backend_id" ]]; then previous_backend_image="$(docker inspect --format '{{.Config.Image}}' "$previous_backend_id")"; fi
if [[ -n "$previous_frontend_id" ]]; then previous_frontend_image="$(docker inspect --format '{{.Config.Image}}' "$previous_frontend_id")"; fi

rollback() {
  local exit_code=$?
  trap - ERR
  if [[ "$AUTO_ROLLBACK" == "1" && -n "$previous_backend_image" && -n "$previous_frontend_image" ]]; then
    echo "Deployment failed; restoring the previous container images." >&2
    # Restore Compose and Nginx configuration from the same source revision as
    # the images. Database changes remain forward-only by design.
    git -C "$SOURCE_DIR" checkout --detach "$previous_source_commit" || true
    BACKEND_IMAGE="$previous_backend_image" FRONTEND_IMAGE="$previous_frontend_image" \
      docker compose --env-file .env -f "$COMPOSE_FILE" up -d --no-build --no-deps --force-recreate backend frontend gateway || true
  fi
  exit "$exit_code"
}
trap rollback ERR

mkdir -p "$UPLOADS_DIR" "$MYSQL_DATA_DIR" "${BACKUP_DIR:-/opt/chemical/backups}"

# Keep the database available for the new backend. These additive migrations
# are safe to run repeatedly and include deployment jobs plus public SEO data.
docker compose --env-file .env -f "$COMPOSE_FILE" up -d mysql
for migration in V19__deployment_jobs.sql V20__seo_meta_page_brand.sql V21__improve_public_image_alt_text.sql; do
  docker compose --env-file .env -f "$COMPOSE_FILE" exec -T mysql \
    mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site <"$APP_DIR/sql/$migration"
done

docker compose --env-file .env -f "$COMPOSE_FILE" build backend
docker compose --env-file .env -f "$COMPOSE_FILE" up -d --no-deps --force-recreate backend

# Wait until the freshly-built backend responds on the private Docker network.
# The throwaway curl container never uses the public host name or CDN.
for _ in $(seq 1 24); do
  if docker run --rm --network qidian_private curlimages/curl:8.10.1 \
    --fail --silent --show-error "http://backend:8080/api/v1/health"; then
    break
  fi
  sleep 5
done
docker run --rm --network qidian_private curlimages/curl:8.10.1 \
  --fail --silent --show-error --retry 3 --retry-delay 5 "http://backend:8080/api/v1/health"

# The frontend build runs on the host network and reads published content
# from the loopback-only backend exposure (127.0.0.1:8080). It never touches
# the public domain or CDN.
docker compose --env-file .env -f "$COMPOSE_FILE" build frontend
docker compose --env-file .env -f "$COMPOSE_FILE" up -d --no-build --force-recreate backend frontend gateway

curl --fail --silent --show-error --retry 3 --retry-delay 5 -H "Host: ${SITE_DOMAIN}" "http://127.0.0.1/api/v1/health"
curl --fail --silent --show-error --retry 3 --retry-delay 5 -H "Host: ${SITE_DOMAIN}" "http://127.0.0.1/zh" >/dev/null

docker image prune -f
trap - ERR
printf '%s\n' "$RESOLVED_COMMIT" >"$APP_DIR/.deployed-commit"
echo "Deployment completed successfully: $RESOLVED_COMMIT"
echo "Refresh CDN cache for changed public HTML, sitemap.xml, and robots.txt."

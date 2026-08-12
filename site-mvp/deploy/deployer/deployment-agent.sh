#!/usr/bin/env bash
set -Eeuo pipefail

# Run this as a single long-lived process outside the application containers.
# It reads only deployment_job rows and executes a fixed script. Production
# pulls source and performs the Docker/SSG build locally.
APP_DIR="${DEPLOY_APP_DIR:?Set DEPLOY_APP_DIR to the checked-out site-mvp directory}"
TARGET_ENVIRONMENT="${DEPLOY_ENVIRONMENT:?Set DEPLOY_ENVIRONMENT to LOCAL or PRODUCTION}"
# The production systemd unit sets this to 600. Keep the same safe default
# for manual starts so a low-frequency CMS publish does not poll MySQL often.
POLL_SECONDS="${DEPLOY_POLL_SECONDS:-1200}"
LOG_DIR="${DEPLOY_LOG_DIR:-$APP_DIR/../deployer-logs}"

case "$TARGET_ENVIRONMENT" in LOCAL|PRODUCTION) ;; *) echo "DEPLOY_ENVIRONMENT must be LOCAL or PRODUCTION" >&2; exit 64 ;; esac
mkdir -p "$LOG_DIR"

if [[ "$TARGET_ENVIRONMENT" == "PRODUCTION" ]]; then
  # The agent is a host process. Read only the deployment environment file so
  # its systemd unit contains no database secret.
  set -a
  # shellcheck disable=SC1091
  source "$APP_DIR/.env"
  set +a
  COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"
  MYSQL=(docker compose --env-file "$APP_DIR/.env" -f "$COMPOSE_FILE" exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD:?Set MYSQL_ROOT_PASSWORD}")
else
  MYSQL=(docker exec -e MYSQL_PWD="${MYSQL_ROOT_PASSWORD:?Set MYSQL_ROOT_PASSWORD}" qidian-mysql mysql -uroot)
fi

query() { "${MYSQL[@]}" qidian_site -N -B -e "$1"; }
escape_sql() { sed "s/'/''/g"; }
write_job() {
  local job_id="$1" status="$2" log_file="$3" resolved_commit="${4:-}" error_summary="${5:-}"
  local excerpt
  excerpt="$(tail -c 12000 "$log_file" 2>/dev/null | escape_sql || true)"
  resolved_commit="$(printf '%s' "$resolved_commit" | cut -c1-40 | escape_sql)"
  error_summary="$(printf '%s' "$error_summary" | cut -c1-1000 | escape_sql)"
  query "UPDATE deployment_job SET status='$status', resolved_commit=NULLIF('$resolved_commit', ''), finished_at=NOW(), log_excerpt='$excerpt', error_summary=NULLIF('$error_summary', '') WHERE id=$job_id"
}

while true; do
  job_id="$(query "SELECT id FROM deployment_job WHERE status='QUEUED' AND environment='$TARGET_ENVIRONMENT' ORDER BY id ASC LIMIT 1" | head -n1 || true)"
  if [[ -z "$job_id" ]]; then sleep "$POLL_SECONDS"; continue; fi

  claimed="$(query "UPDATE deployment_job SET status='RUNNING', started_at=NOW(), log_excerpt='Deployment agent started.' WHERE id=$job_id AND status='QUEUED'; SELECT ROW_COUNT();" | tail -n1)"
  [[ "$claimed" == "1" ]] || continue

  log_file="$LOG_DIR/deployment-$job_id.log"
  : >"$log_file"
  if DEPLOY_APP_DIR="$APP_DIR" DEPLOY_ENVIRONMENT="$TARGET_ENVIRONMENT" \
      bash "$APP_DIR/deploy/deployer/run-deployment.sh" >"$log_file" 2>&1; then
    resolved_commit="$(tr -d '\r\n' <"$APP_DIR/.deployed-commit" 2>/dev/null || true)"
    write_job "$job_id" SUCCESS "$log_file" "$resolved_commit"
  else
    write_job "$job_id" FAILED "$log_file" "" "Fixed deployment script failed; inspect the task log."
  fi
done

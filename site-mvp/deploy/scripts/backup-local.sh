#!/usr/bin/env bash
set -Eeuo pipefail

# Database recovery chain: a weekly consistent full dump plus daily closed
# binary-log increments.  MySQL retains active binlogs on the host-backed data
# volume; this script flushes one each run, archives the now-closed file and
# records the precise baseline coordinates required for point-in-time restore.
APP_DIR="${APP_DIR:-/opt/chemical/app/site-mvp}"
COMPOSE_FILE="$APP_DIR/deploy/docker-compose.prod.yml"
BACKUP_DIR="${BACKUP_DIR:-/opt/chemical/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-${BACKUP_RETENTION_DAYS:-35}}"
FULL_BACKUP_WEEKDAY="${FULL_BACKUP_WEEKDAY:-7}" # ISO weekday: Sunday

cd "$APP_DIR"
set -a
# shellcheck disable=SC1091
source .env
set +a

stamp="$(date -u +%Y%m%dT%H%M%SZ)"
mysql_dir="$BACKUP_DIR/mysql"
increment_dir="$mysql_dir/incremental"
full_dir="$mysql_dir/full"
upload_dir="$BACKUP_DIR/uploads"
manifest_dir="$mysql_dir/manifests"
mkdir -p "$full_dir" "$increment_dir" "$upload_dir" "$manifest_dir"
umask 077

compose=(docker compose --env-file .env -f "$COMPOSE_FILE")
mysql_exec() {
  "${compose[@]}" exec -T mysql mysql -N -B -uroot -p"${MYSQL_ROOT_PASSWORD}" "$@"
}

latest_file="$(mysql_exec -e 'SHOW MASTER STATUS' | awk 'NR==1 {print $1}')"
latest_pos="$(mysql_exec -e 'SHOW MASTER STATUS' | awk 'NR==1 {print $2}')"
if [[ -z "$latest_file" || -z "$latest_pos" ]]; then
  echo 'MySQL binary logging is not available; confirm --log-bin is enabled.' >&2
  exit 1
fi

if [[ "$(date -u +%u)" == "$FULL_BACKUP_WEEKDAY" ]] || ! find "$full_dir" -name 'qidian_site_*.sql.gz' -print -quit | grep -q .; then
  # Start a fresh log before dumping. The dump's --master-data coordinate is
  # therefore the beginning of the new recovery chain, not the middle of a
  # log which also contains pre-baseline writes.
  mysql_exec -e 'FLUSH BINARY LOGS'
  # --master-data=2 places the binary-log coordinates in a comment inside the
  # dump. It is safe to restore manually and provides the increment start.
  "${compose[@]}" exec -T mysql \
    mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
    --single-transaction --routines --triggers --events --master-data=2 qidian_site \
    | gzip >"$full_dir/qidian_site_${stamp}.sql.gz"
  baseline_file="$(mysql_exec -e 'SHOW MASTER STATUS' | awk 'NR==1 {print $1}')"
  baseline_pos="$(mysql_exec -e 'SHOW MASTER STATUS' | awk 'NR==1 {print $2}')"
  printf '%s\t%s\t%s\n' "$stamp" "$baseline_file" "$baseline_pos" >"$manifest_dir/full_${stamp}.tsv"
  echo "Created full database baseline: $full_dir/qidian_site_${stamp}.sql.gz"
else
  # Rotate first: MySQL moves the previously active log to a closed file,
  # making it safe to copy without racing an in-flight write. The newly opened
  # log is archived on the next daily run.
  mysql_exec -e 'FLUSH BINARY LOGS'
  archive="$increment_dir/${latest_file}_${stamp}.gz"
  "${compose[@]}" exec -T mysql sh -c "cat /var/lib/mysql/$latest_file" | gzip >"$archive"
  printf '%s\t%s\t%s\t%s\n' "$stamp" "$latest_file" "$latest_file" "$latest_pos" \
    >"$manifest_dir/increment_${stamp}.tsv"
  echo "Archived database increment: $archive"
fi

# Uploads are not in MySQL. Preserve a daily snapshot in the same recovery
# window; archive mode does not mutate the live host directory.
tar -C "$(dirname "$UPLOADS_DIR")" -czf "$upload_dir/uploads_${stamp}.tar.gz" "$(basename "$UPLOADS_DIR")"

find "$full_dir" "$increment_dir" "$upload_dir" "$manifest_dir" -type f -mtime "+$RETENTION_DAYS" -delete
echo "Backup completed: $stamp (retention ${RETENTION_DAYS} days)"

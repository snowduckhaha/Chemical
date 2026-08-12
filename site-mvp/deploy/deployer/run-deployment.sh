#!/usr/bin/env bash
set -Eeuo pipefail

# This script deliberately accepts no values from the web request. The agent
# passes only a pre-validated environment and every command below is fixed.
APP_DIR="${DEPLOY_APP_DIR:?Set DEPLOY_APP_DIR to the checked-out site-mvp directory}"
TARGET_ENVIRONMENT="${DEPLOY_ENVIRONMENT:?Set DEPLOY_ENVIRONMENT to LOCAL or PRODUCTION}"

case "$TARGET_ENVIRONMENT" in
  LOCAL)
    git -C "$APP_DIR" fetch --prune origin main
    git -C "$APP_DIR" checkout main
    git -C "$APP_DIR" pull --ff-only origin main
    powershell.exe -ExecutionPolicy Bypass -File "$APP_DIR/scripts/stop-all.ps1"
    powershell.exe -ExecutionPolicy Bypass -File "$APP_DIR/scripts/start-all.ps1"
    ;;
  PRODUCTION)
    APP_DIR="$APP_DIR" bash "$APP_DIR/deploy/scripts/deploy.sh"
    ;;
  *)
    echo "Unsupported deployment environment: $TARGET_ENVIRONMENT" >&2
    exit 64
    ;;
esac

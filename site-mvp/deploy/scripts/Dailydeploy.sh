#!/usr/bin/env bash
set -Eeuo pipefail

sudo -i
cd /opt/chemical/server-deploy/
set -a; source .env; set +a
APP_DIR="$PWD" deploy/scripts/deploy.sh
#!/usr/bin/env bash
cd /opt/chemical/server-deploy

cp deploy/.env.production.example .env
sed -i 's/\r$//' .env

sed -i 's/\r$//' deploy/scripts/*.sh
chmod 600 .env
chmod +x deploy/scripts/*.sh
set -a; source .env; set +a
APP_DIR="$PWD" bash deploy/scripts/deploy.sh
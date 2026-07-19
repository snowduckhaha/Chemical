#!/usr/bin/env bash
set -Eeuo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <domain> <email>" >&2
  exit 1
fi

DOMAIN="$1"
EMAIL="$2"

docker run --rm -p 80:80 \
  -v /etc/letsencrypt:/etc/letsencrypt \
  -v /var/www/certbot:/var/www/certbot \
  certbot/certbot certonly --standalone \
  --non-interactive --agree-tos --email "$EMAIL" \
  -d "$DOMAIN" -d "www.$DOMAIN"
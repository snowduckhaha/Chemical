#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# 构建镜像并推送 GHCR----在GIT bash中执行
# 用法: bash "构建镜像并推送GHCR.sh" [backend|frontend|all]
# 默认构建并推送全部镜像
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DEPLOY_DIR="$PROJECT_ROOT/deploy"

GHCR_USER="${GHCR_USERNAME:-snowduckhaha}"
GHCR_TOKEN="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"
BACKEND_IMAGE="ghcr.io/$GHCR_USER/chemical-backend:latest"
FRONTEND_IMAGE="ghcr.io/$GHCR_USER/chemical-frontend:latest"

TARGET="${1:-all}"

# ---------- 登录 GHCR ----------
login_ghcr() {
  if [[ -z "${GHCR_TOKEN:-}" ]]; then
    echo "未设置 GHCR_TOKEN，尝试使用已登录的 docker 凭据..."
    docker login ghcr.io || {
      echo "请先执行: docker login ghcr.io -u $GHCR_USER"
      exit 1
    }
  else
    printf '%s' "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
  fi
}

# ---------- 构建后端 ----------
build_backend() {
  echo "==> 构建后端镜像: $BACKEND_IMAGE"
  docker build -f "$DEPLOY_DIR/Dockerfile.backend" -t "$BACKEND_IMAGE" "$PROJECT_ROOT"
  echo "==> 推送后端镜像..."
  docker push "$BACKEND_IMAGE"
  echo "==> 后端镜像推送完成"
}

# ---------- 构建前端 ----------
build_frontend() {
  echo "==> 构建前端镜像: $FRONTEND_IMAGE"
  docker build -f "$DEPLOY_DIR/Dockerfile.frontend" -t "$FRONTEND_IMAGE" "$PROJECT_ROOT"
  echo "==> 推送前端镜像..."
  docker push "$FRONTEND_IMAGE"
  echo "==> 前端镜像推送完成"
}

# ---------- 主流程 ----------
echo "========================================="
echo " 构建镜像并推送 GHCR"
echo " GHCR 用户: $GHCR_USER"
echo " 目标: $TARGET"
echo "========================================="

login_ghcr

case "$TARGET" in
  backend)
    build_backend
    ;;
  frontend)
    build_frontend
    ;;
  all)
    build_backend
    build_frontend
    ;;
  *)
    echo "用法: $0 [backend|frontend|all]"
    exit 1
    ;;
esac

echo ""
echo "全部完成！"
echo "  后端: $BACKEND_IMAGE"
echo "  前端: $FRONTEND_IMAGE"

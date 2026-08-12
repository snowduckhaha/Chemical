# 生产服务器源码构建与发布

本方案不使用 GHCR，也不需要 GitHub Actions runner。生产服务器保留唯一受控的 Git 工作副本；发布时服务器自行拉取 `origin/main`、构建后端、用内部公开 API 执行 Vite-SSG 预渲染，并重建容器。

## 发布链路

```text
CMS 发布任务 / 手工执行
  -> git fetch + fast-forward origin/main
  -> 构建后端并启动 backend
  -> Docker 私网 http://backend:8080/api/v1
  -> Vite-SSG 预渲染产品、应用、资讯等公共页
  -> 构建并替换 frontend、gateway
  -> 源站健康检查与 SSG 页面检查
```

`SSG_API_BASE` 固定为 `http://backend:8080/api/v1`，仅在 `qidian_private` Docker 网络内解析。它没有经过 CDN，也不会暴露给浏览器；客户端仍使用相对地址 `/api/v1`。

## 服务器准备

```bash
git clone <repository-url> /opt/chemical/app
cd /opt/chemical/app/site-mvp
cp deploy/.env.production.example .env
chmod 600 .env
```

填写 `.env` 中的域名、数据库密码、上传目录和数据目录。无需填写 GHCR 账号、令牌或镜像名。部署用户必须具备 Git 仓库只读权限、Docker 访问权限及私有仓库所需的 SSH 私钥。

首次部署：

```bash
APP_DIR=/opt/chemical/app/site-mvp bash /opt/chemical/app/site-mvp/deploy/scripts/first-deploy.sh
```

日常手工发布：

```bash
APP_DIR=/opt/chemical/app/site-mvp bash /opt/chemical/app/site-mvp/deploy/scripts/deploy.sh
```

部署脚本拒绝在工作副本存在已跟踪本地修改时运行，避免把服务器临时改动带入发布。请只通过 Git 提交变更。

## CMS 一键发布

在生产服务器、应用容器外启动唯一一个代理：

```bash
export DEPLOY_APP_DIR=/opt/chemical/app/site-mvp
export DEPLOY_ENVIRONMENT=PRODUCTION
export DEPLOY_LOG_DIR=/opt/chemical/deployer-logs
bash /opt/chemical/app/site-mvp/deploy/deployer/deployment-agent.sh
```

代理会从仅服务器可读的 `.env` 加载数据库配置。后台仅对 `ADMIN` 角色的 `zelin` 显示“发布最新代码”入口，且只能创建固定的 `PRODUCTION` / `origin/main` 任务；重复点击会复用当前等待中或执行中的任务。代理固定执行 `deploy/scripts/deploy.sh`，不会接受分支、命令、路径或环境变量等网页传入参数。

生产代理默认每 **10 分钟**查询一次 MySQL；因此后台点击后最长约 10 分钟开始执行。仓库提供的 systemd 单元将其作为唯一实例运行：`RuntimeDirectory` 下的 `flock` 锁覆盖代理的整个生命周期，避免手工重复启动的第二个进程消费同一个任务。

使用仓库提供的 systemd 单元保持唯一实例：

```bash
sudo cp deploy/deployer/qidian-deployment-agent.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now qidian-deployment-agent
sudo systemctl status qidian-deployment-agent
```

检查运行状态与最近日志：

```bash
systemctl is-active qidian-deployment-agent
journalctl -u qidian-deployment-agent -n 100 --no-pager
```

## 回滚和注意事项

- 默认 `AUTO_ROLLBACK=1`：构建或源站验证失败时，脚本会检出上一提交，并用当前运行容器的镜像引用重建后端、前端和网关。数据库变更不回退。
- 手动回退到已存在的 Git 提交：`DEPLOY_REF=<commit-sha> APP_DIR=... bash deploy/scripts/deploy.sh`。这会重新构建该提交；之后正常发布会重新切回 `main`。
- 数据库变更不可自动回滚。发布前仍应完成备份，并确保新增 SQL 是幂等或已有明确的升级步骤。
- 成功发布后按 CDN 控制台规则刷新受影响的 HTML 页面、`/sitemap.xml` 与 `/robots.txt`。源站的 SSG 内容已经是最新的，但 CDN 仍可能保留旧的公共页面缓存。
- 服务器至少应有 4 GiB 内存和充足磁盘空间；Docker 构建期间会同时存在新旧镜像，建议预留 10 GiB 以上可用空间。

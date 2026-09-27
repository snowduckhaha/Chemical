# 生产服务器源码构建与发布

本方案不使用 GHCR，也不需要 GitHub Actions runner。生产服务器保留唯一受控的 Git 工作副本；发布时服务器自行拉取 `origin/main`、构建后端、用内部公开 API 执行 Vite-SSG 预渲染，并重建容器。

## 发布链路

```text
CMS 发布任务 / 手工执行
  -> git fetch + fast-forward origin/main
  -> 构建后端并启动 backend（仅向宿主机回环地址暴露 8080）
  -> 宿主机回环 http://127.0.0.1:8080/api/v1
  -> Vite-SSG 预渲染产品、应用、资讯等公共页
  -> 构建并替换 frontend、gateway
  -> 源站健康检查与 SSG 页面检查
```

`SSG_API_BASE` 固定为 `http://127.0.0.1:8080/api/v1`。backend 通过 compose 的 `127.0.0.1:8080:8080` 端口映射仅向宿主机回环地址暴露，前端构建使用 host 网络读取该地址；BuildKit 不支持在构建阶段加入 compose 定义的自定义网络，因此不使用 Docker 私网方案。该地址不经过 CDN，也不对外网开放；客户端仍使用相对地址 `/api/v1`。

## 服务器准备

```bash
git clone https://github.com/snowduckhaha/Chemical.git /opt/chemical/app
cd /opt/chemical/app/site-mvp
cp deploy/.env.production.example .env
chmod 600 .env
```

填写 `.env` 中的域名、数据库密码及以下宿主机持久化目录。无需填写 GHCR 账号、令牌或镜像名。部署用户必须具备 Git 仓库只读权限、Docker 访问权限及私有仓库所需的 SSH 私钥。

`.env` 含数据库密码和部署令牌，权限保持 `600` 且归 root 所有。docker compose 只从**当前工作目录**自动加载 `.env`，而 compose 文件在 `deploy/` 子目录，因此手工执行 compose 命令时必须先 `cd /opt/chemical/app/site-mvp`，或显式加 `--env-file .env`；非 root 用户读不了该文件，需用 `sudo`。若看到大量 `variable is not set` 警告和 `invalid spec` 错误，就是 `.env` 没被加载。

```dotenv
UPLOADS_DIR=/opt/chemical/uploads
MYSQL_DATA_DIR=/opt/chemical/mysql-data
BACKUP_DIR=/opt/chemical/backups
BACKUP_RETENTION_DAYS=35
MYSQL_BINLOG_RETENTION_SECONDS=1296000
```

目录用途与容器挂载如下：

| 宿主机目录 | 容器路径 | 用途 |
|---|---|---|
| `/opt/chemical/uploads` | backend `/app/uploads`；gateway `/srv/uploads`（只读） | 后台上传图片，容器重建后保留 |
| `/opt/chemical/mysql-data` | mysql `/var/lib/mysql` | MySQL 数据及 binary log，容器重建后保留 |
| `/opt/chemical/backups` | 不直接挂载 | 周全量基线、每日数据库增量及每日上传图片快照 |

backend 容器的 `127.0.0.1:8080:8080` 端口映射仅绑定宿主机回环地址，供前端 SSG 构建读取发布内容；对外网和 CDN 均不可见，无需在安全组中放行 8080。

构建器要求：前端构建使用 `network: host`，必须使用 **默认的 buildx 构建器**（docker 驱动）。不要在服务器上创建 docker-container 驱动的自定义构建器并设为默认（例如 `docker buildx create ... --use`），否则 host 网络构建无法访问宿主机回环地址。检查与恢复：

```bash
docker buildx ls                  # 确认 * 在 default 上
docker buildx use default         # 若被切走，切回
```

首次部署脚本会创建这些目录；MySQL 数据目录应保持 `0700` 且归 MySQL 容器 UID 所有。不要执行 `docker compose down -v`，不要删除 `/opt/chemical/mysql-data`。

首次部署：

```bash
APP_DIR=/opt/chemical/app/site-mvp bash /opt/chemical/app/site-mvp/deploy/scripts/first-deploy.sh
```

日常手工发布：

```bash
APP_DIR=/opt/chemical/app/site-mvp bash /opt/chemical/app/site-mvp/deploy/scripts/deploy.sh
```

部署脚本拒绝在工作副本存在已跟踪本地修改时运行，避免把服务器临时改动带入发布。请只通过 Git 提交变更。

脚本的两个自愈机制，无需人工干预但值得了解：

1. **自我重执行**：`deploy.sh` 合并 `origin/main` 后若脚本自身被更新，会以新版本重新执行，保证“部署脚本的修改”也立即生效。
2. **冷启动等待**：backend 强制重建后需要 Spring Boot 冷启动，健康检查带 `--retry-all-errors` 最长等待 5 分钟；期间的 `connection reset / empty reply` 重试属正常现象。

手工补跑 compose 命令（如单独重建某个服务）时必须带上镜像标签变量，否则默认回退为 `local` 并报 `No such image`：

```bash
BUILD_TAG=$(git rev-parse --short=12 HEAD) \
docker compose --env-file .env -f deploy/docker-compose.prod.yml up -d --no-build --force-recreate frontend gateway
```

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

- 默认 `AUTO_ROLLBACK=1`：构建或源站验证失败时，脚本会检出上一提交，并用当前运行容器的镜像引用重建后端、前端和网关。数据库变更不回退。回滚后服务器代码停在旧提交的 detached HEAD，重跑 `deploy.sh` 会自动合并回 `main`。
- **首次部署无旧版本可回滚**：失败时现场会停在半成品状态（如只有 backend/mysql 在跑），需根据“故障排查”节定位后重跑。
- 手动回退到已存在的 Git 提交：`DEPLOY_REF=<commit-sha> APP_DIR=... bash deploy/scripts/deploy.sh`。这会重新构建该提交；之后正常发布会重新切回 `main`。
- 数据库变更不可自动回滚。发布前仍应完成备份，并确保新增 SQL 是幂等或已有明确的升级步骤。
- 成功发布后按 CDN 控制台规则刷新受影响的 HTML 页面、`/sitemap.xml` 与 `/robots.txt`。源站的 SSG 内容已经是最新的，但 CDN 仍可能保留旧的公共页面缓存。
- 服务器至少应有 4 GiB 内存和充足磁盘空间；Docker 构建期间会同时存在新旧镜像，建议预留 10 GiB 以上可用空间。

## 部署后验证

```bash
cd /opt/chemical/app/site-mvp
set -a; source .env; set +a

# ① 容器状态：4 个容器全 Up，backend 带 127.0.0.1:8080->8080/tcp 映射
docker compose --env-file .env -f deploy/docker-compose.prod.yml ps
docker ps --format "table {{.Names}}\t{{.Ports}}"

# ② 入口页 200 直出：无 Location 跳转、无 403
curl -sI -H "Host: $SITE_DOMAIN" http://127.0.0.1/zh | head -3
curl -sI -H "Host: $SITE_DOMAIN" http://127.0.0.1/en | head -3

# ③ 品牌与内容：前两项 > 0，第三项 = 0
curl -s -H "Host: $SITE_DOMAIN" http://127.0.0.1/zh | grep -o "起点化工" | wc -l
curl -s -H "Host: $SITE_DOMAIN" http://127.0.0.1/en | grep -io "origin chemical" | wc -l
curl -s -H "Host: $SITE_DOMAIN" http://127.0.0.1/en | grep -io "qidian" | wc -l

# ④ 子页面与尾斜杠列表页均 200
curl -sI -H "Host: $SITE_DOMAIN" http://127.0.0.1/zh/products/ | head -3
curl -sI -H "Host: $SITE_DOMAIN" http://127.0.0.1/en/about/ | head -3

# ⑤ IP 直连返回站点而非 nginx 欢迎页（结果为 0）
curl -s http://127.0.0.1/ | grep -c "Welcome to nginx"

# ⑥ 动态基础设施
curl -s -H "Host: $SITE_DOMAIN" http://127.0.0.1/api/v1/health
curl -sI -H "Host: $SITE_DOMAIN" http://127.0.0.1/robots.txt | head -3
```

注意验证中文页时用“起点化工”作关键词，英文页才用 `origin chemical`；`curl` 不带 `-L` 时 301 响应体极小，先确认状态码再看内容。

## 运维与故障排查

### 容器级排查命令

生产只有 80 端口对外，排查从 gateway 容器内部发起（它在同一私网，可直达 backend/frontend）：

```bash
cd /opt/chemical/app/site-mvp
docker compose --env-file .env -f deploy/docker-compose.prod.yml ps       # 容器状态
docker exec deploy-gateway-1 curl -s http://backend:8080/api/v1/health   # 私网验证后端
docker exec deploy-gateway-1 curl -sI http://frontend:8080/zh.html       # 私网验证前端静态
docker logs deploy-backend-1 --tail 50                                    # 后端日志
docker logs deploy-gateway-1 --tail 20                                    # 网关配置/启动错误
```

私网验证通过但公网不通时，检查腾讯云安全组入站 80 与 CDN 回源配置（回源地址 = 服务器公网 IP、回源端口 80、回源协议 HTTP、回源 Host = `www.${SITE_DOMAIN}`）。

### 常见故障与处理

| 症状 | 原因 | 处理 |
|---|---|---|
| 大量 `variable is not set` + `invalid spec` | compose 未加载 `.env` | 在 APP_DIR 目录执行或加 `--env-file .env`；非 root 用 `sudo` |
| `network mode "qidian_private" not supported by buildkit` | 构建阶段不支持自定义网络/非默认构建器 | 确认 compose 用 `network: host`；`docker buildx use default` 切回默认构建器 |
| 前端构建 `SSG route collection failed ... fetch failed` | 构建中访问不到 `127.0.0.1:8080` | `docker ps` 确认 backend 带 `127.0.0.1:8080->8080/tcp`；检查构建器是否为 default |
| `No such image: qidian-site-frontend:local` | 手工 compose 未设 `BUILD_TAG` | 命令前加 `BUILD_TAG=$(git rev-parse --short=12 HEAD)` |
| 健康检查报 `connection reset / empty reply` | Spring Boot 冷启动未完成 | 脚本已自动重试最长 5 分钟；持续失败看 `docker logs deploy-backend-1` |
| IP 访问显示 “Welcome to nginx” | 请求落入 nginx 内置默认 server | 已由 `site.conf.template` 的 `listen 80 default_server` + `blank.conf` 挂载修复；若复发检查 gateway 卷挂载 |
| `/zh/products/` 等尾斜杠 URL 403 | vite-ssg 产物目录内无 index.html | 已由 frontend.conf 尾斜杠回退 location 修复；若复发检查 frontend 镜像是否为最新 |
| 刚发布的资讯详情页 404 | 该文章发布在最近一次 Vite-SSG 构建之后，尚无对应静态 HTML | frontend.conf 会先向后端确认文章为已发布，再返回资讯页面外壳；访客可立即查看。下一次常规发布会生成完整静态 HTML；不存在、下线或定时未到的 URL 仍返回 404。 |
| 失败回滚后页面恢复旧版 | 自动回滚是保底，不是修复 | 按本表定位原因后重跑 `deploy.sh`，不要停留在旧版 |

### 关键 nginx 约束

- gateway 的 server 块必须是 `default_server`，并用 `blank.conf` 覆盖镜像自带的 `default.conf`，否则 IP 直连和未知 Host 会落入内置欢迎页。
- frontend 的 `try_files` 顺序必须是 `$uri $uri.html $uri/`：首页产物是根目录 `zh.html`/`en.html`，而 `zh/`、`en/` 目录存子页面且无 index.html，目录候选放前会 301 到 403。
- frontend 必须保持 `absolute_redirect off;`：它监听内部 8080，绝对跳转会把内部端口泄漏到公网 `Location` 头。
- 修改 `frontend.conf` 需重建 frontend 镜像才生效；`site.conf.template` 与 `blank.conf` 是卷挂载，`up -d --force-recreate gateway` 即可生效。

## 每日增量备份与恢复

`deploy/scripts/backup-local.sh` 实现的是 **周全量基线 + 每日 MySQL binary log 增量**，而不是每天重复全量导出：每周日创建一个一致性 `mysqldump` 基线；每日任务先轮转 binary log，再归档上一段已关闭的日志，同时保存上传图片快照。默认保留 35 天，足以保留多个完整恢复链。

在 `deploy` 用户的 crontab 中安装任务：

```cron
30 2 * * * APP_DIR=/opt/chemical/app/site-mvp BACKUP_DIR=/opt/chemical/backups RETENTION_DAYS=35 /opt/chemical/app/site-mvp/deploy/scripts/backup-local.sh >> /opt/chemical/backups/backup.log 2>&1
```

恢复必须在隔离环境演练，步骤为：选择故障时点之前最近的 `mysql/full/qidian_site_*.sql.gz`；导入该基线；按时间顺序解压并用 `mysqlbinlog` 回放对应 `mysql/incremental/` 日志，必要时以目标时间或位置停止；最后解压同一时点的 `uploads/uploads_*.tar.gz`。`mysql/manifests/` 保存每个基线和增量的日志坐标。恢复前禁止写入业务数据，并至少每月演练一次。

# Origin Chemical Vite-SSG 开发与发布指引（审阅优化版）

> 审阅日期：2026-08-08  
> 适用目录：site-mvp/frontend  
> 目标：把公开内容页构建为完整 HTML；管理后台继续作为不可收录 SPA。  
> 本指引以现有 Vue Router、Spring Boot API、MySQL 和 Docker Compose 为前提。

## 0. 先做架构决定

Vite-SSG 适合当前站点的前提是：产品、应用和资讯发布后可接受一次受控重建，通常应在 15 分钟内完成。

若业务要求每分钟改价、库存或参数并立即显示，或页面数量会急速增长到静态构建不可承受，应改用 SSR 或混合渲染，而不是强行 SSG。

本项目推荐如下边界：

| 区域 | 渲染方式 | 原因 |
|---|---|---|
| 首页、关于、产品、应用、资讯、联系 | SSG | 内容公开、结构稳定、需要被抓取。 |
| 管理后台 | SPA + noindex | 登录态和交互密集，无 SEO 价值。 |
| 站内搜索、询盘成功页 | SPA + noindex | 查询参数和转换状态不应收录。 |
| sitemap.xml、robots.txt | Spring Boot 动态输出 | 数据库驱动，现有后端已实现接口。 |

不得将公开 API 的 Docker 内部地址写入 VITE_* 环境变量。VITE_* 会被编译进浏览器代码；若设置为 localhost:8080，真实访客会访问其自己的电脑，而不是服务器。

## 1. 原指引的关键修正

| 原做法 | 风险 | 本指引的做法 |
|---|---|---|
| 引入 vite-plugin-pages 并迁移全部前台路由 | 与现有手写 Vue Router 重叠，动态产品参数不会自动生成。 | 复用现有路由表，仅补充构建期动态路由清单。 |
| 使用 vite-plugin-sitemap | 无法可靠发现数据库中的产品、应用和资讯 URL。 | 使用后端 /sitemap.xml，网关精确代理。 |
| 所有 onMounted 机械替换为顶层 await | 会破坏客户端导航；异步组件还需要 Suspense 和状态同步设计。 | 使用统一预取层：服务端预取、客户端 hydration 复用、客户端导航重新加载。 |
| API 失败时构建 fallback 内容 | 会把空白、过时或错误页面发布并被索引。 | 可索引页面的数据请求失败时必须令构建失败；不能静默降级。 |
| Admin HTTP 接口直接拼接并执行用户输入的 shell | 会形成远程命令执行风险。 | 保留后台一键拉取、重建、发布；页面只创建部署任务，服务器部署代理执行已审计的固定脚本。 |
| Product 默认 Offer/InStock | 会发布虚假的库存和商务数据。 | 只使用真实 Product 属性；报价型产品不输出 Offer。 |

Vite-SSG 支持通过 includedRoutes 包含构建期收集的动态路径。  
参考：[vite-ssg README](https://www.npmjs.com/package/vite-ssg)。  
Vue 的服务端渲染要求在服务端预取数据并处理客户端 hydration。  
参考：[Vue SSR 指南](https://vuejs.org/guide/scaling-up/ssr)。

## 2. 预实施检查

### 2.1 业务和数据检查

- [ ] 每条 PUBLISHED 产品、应用、新闻均有稳定 slug；
- [ ] 中英文内容使用同一实体记录，或有明确翻译映射；
- [ ] 所有公开图片和文档使用 HTTPS URL；
- [ ] 产品字段中的标题、摘要、参数、图片和发布状态可由公共 API 获取；
- [ ] 技术资料、证书、地址、电话和企业名称已经业务确认；
- [ ] 确认内容发布后允许等待构建完成才对公众可见。

### 2.2 环境检查

生产部署中 backend 仅通过 `127.0.0.1:8080:8080` 端口映射暴露给宿主机回环地址，前端构建使用 host 网络访问。构建器必须通过下列地址访问：

~~~text
SSG_API_BASE=http://127.0.0.1:8080/api/v1
PUBLIC_API_BASE=/api/v1
SITE_ORIGIN=https://www.origin-chemical.com
~~~

其中 SSG_API_BASE 只供构建器使用；PUBLIC_API_BASE 只供浏览器使用。

构建环境至少应验证：

~~~bash
node --version
npm --version
curl -fsS http://127.0.0.1:8080/api/v1/products/categories?lang=en
curl -fsS http://127.0.0.1:8080/api/v1/news?lang=en
~~~

### 2.3 一键发布专属账号

一键发布不是“所有 ADMIN 都可用”的功能。唯一允许发起、查看部署日志和回滚的后台用户名固定为 zelin；角色 ADMIN 仍是必要条件，但不是充分条件。

后续开发必须同时实现以下三层控制：

1. 后端部署接口使用方法级授权：hasRole('ADMIN') 且 authentication.name 等于 zelin；
2. 管理页面仅在当前用户名为 zelin 时显示部署入口；隐藏按钮不是权限控制，接口仍必须返回 403；
3. 部署代理不信任 HTTP 调用者，而只处理由后端写入的、已审计的 deployment_job。

推荐的接口注解：

~~~java
@PreAuthorize("hasRole('ADMIN') and authentication.name == 'zelin'")
~~~

账号创建不应把默认明文密码或固定 bcrypt hash 写入版本库。先用项目现有的密码哈希生成脚本生成一次性 bcrypt 值，再由生产运维在部署时执行以下 SQL；首次登录时强制修改密码：

~~~sql
-- 将 :ZELIN_BCRYPT_HASH 替换为离线生成的 bcrypt hash，不要提交真实值。
INSERT INTO admin_user
  (username, password_hash, role_code, enabled, must_change_password)
VALUES
  ('zelin', ':ZELIN_BCRYPT_HASH', 'ADMIN', 1, 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = 'ADMIN',
  enabled = 1,
  must_change_password = 1;
~~~

不要把这条含真实 hash 的 SQL 放进会自动执行的 Flyway 迁移。可将不含密钥的模板记录在运维说明中，由受控部署流程传入 hash 并执行。现有 V17 脚本包含硬编码账户信息，后续应避免仿照该方式新增 zelin 的真实密码。

## 3. 路由与静态文件策略

### 3.1 继续使用现有 Vue Router

不要创建 src/pages、不要安装 vite-plugin-pages、不要同时维护两套公开路由。将 [router/index.ts](/D:/Zelin/Chemical/site-mvp/frontend/src/router/index.ts) 中的 routes 改为具名导出，并为路由补充元信息：

~~~typescript
export const routes = [
  {
    path: "/:lang(zh|en)/products/:categorySlug/:seriesSlug/:productSlug",
    name: "product-detail",
    component: () => import("../views/ProductDetailView.vue"),
    meta: { indexable: true, dynamic: true }
  },
  {
    path: "/:lang(zh|en)/admin/products/categories",
    name: "admin-product-categories",
    component: () => import("../views/AdminCategoriesView.vue"),
    meta: { indexable: false, ssg: false, requiresAdmin: true }
  }
];
~~~

静态公开路由包括首页、关于、产品中心、应用中心、资讯中心和联系页。动态公开路由由数据生成：

~~~text
/{lang}/products/{categorySlug}
/{lang}/products/{categorySlug}/{seriesSlug}
/{lang}/products/{categorySlug}/{seriesSlug}/{productSlug}
/{lang}/applications/{applicationSlug}
/{lang}/news/{categorySlug}
/{lang}/news/{categorySlug}/{articleSlug}
~~~

### 3.2 构建期路由收集

新增 scripts/collect-ssg-routes.mjs。它只通过 SSG_API_BASE 调用发布态公共 API，并生成 src/generated/ssg-routes.json。

它必须：

1. 对 zh、en 分别读取分类、系列、产品、应用、新闻和新闻分类；
2. 去重、排序，并且只保留 PUBLISHED 数据；
3. 添加固定公开路径；
4. 不加入 admin、search、contact/success；
5. 任一公共 API 失败、响应结构异常或 slug 为空时退出码为 1；
6. 将生成的路径总数、各类型数和内容 revision 写入日志。

Vite-SSG 的 includedRoutes 必须使用这份清单。文件系统中的 [slug] 路由只定义路由模式，不会自动知道数据库里有哪些产品。

~~~typescript
// main.ts，示意代码：实际签名以锁定的 vite-ssg 版本文档为准
import { ViteSSG } from "vite-ssg";
import App from "./App.vue";
import { routes } from "./router";
import generatedRoutes from "./generated/ssg-routes.json";

export const createApp = ViteSSG(
  App,
  { routes, base: "/" },
  () => ({})
);

export function includedRoutes() {
  return generatedRoutes;
}
~~~

实施时先做一个产品和一篇资讯的最小验证，再扩展至全量路由。

### 3.3 Nginx：静态公开页必须返回真实 404

当前 frontend.conf 的 try_files 最后回退到 /index.html，会令不存在的公开 URL 返回 200 空壳页。公开目录的最终策略应为：

~~~nginx
location / {
    try_files $uri $uri/ $uri.html =404;
}

location ~ ^/(zh|en)/admin/ {
    try_files /admin-shell.html =404;
    add_header X-Robots-Tag "noindex, nofollow" always;
}

location = /robots.txt {
    proxy_pass http://backend:8080;
}

location = /sitemap.xml {
    proxy_pass http://backend:8080;
}
~~~

admin-shell.html 是构建时输出的最小 SPA 入口。浏览器保持原 URL，Vue Router 仍会接管后台深链接；公开目录则绝不能回退到这个 SPA 壳。

实际 Nginx 配置须在 staging 用所有公开路径、后台深链接和不存在路径验证后才能替换生产配置。

## 4. 依赖与入口改造

仅新增必要依赖，并将版本锁入 package-lock.json：

~~~bash
npm install -D vite-ssg
npm install @unhead/vue
~~~

不建议新增 vite-plugin-pages、vite-plugin-sitemap 或 html-minifier，除非已有明确、经测试的使用场景。

入口需要完成四件事：

1. 用 ViteSSG 创建 app 和 router；
2. 安装 Pinia；
3. 安装 Unhead；
4. 把现有全局样式、Element Plus、鉴权和分析逻辑改为服务端安全方式。

所有 document、window、localStorage、sessionStorage、navigator 和 sendBeacon 调用必须仅在客户端执行。当前 analytics.ts 使用 localStorage 和 navigator，应只在客户端注册 page view；它不能在预渲染阶段执行。

不要把所有 Element Plus 组件包进 ClientOnly。先运行完整构建和 Playwright 测试；只有确实访问浏览器 DOM 且没有服务端替代内容的交互控件才延迟挂载。导航、产品正文、链接和 CTA 必须保留在静态 HTML 内。

## 5. 数据预取与 hydration

### 5.1 统一的数据加载约定

每个公开页面使用同一个页面数据层，而不是在组件内复制 onMounted、watch 和错误处理。

推荐 API：

~~~text
load(route)       根据路由读取公开 API，返回页面实体或 NotFound
prefetch(route)   仅服务端构建时调用；失败必须中断构建
hydrate(route)    浏览器首次加载复用序列化状态
navigate(route)   浏览器站内导航时按 route key 刷新
~~~

建议以 Pinia 保存 pageKey → 数据，构建时把已获取的页面状态序列化到 Vite-SSG initialState；浏览器启动时恢复该状态。这样首屏不二次请求，站内导航仍可刷新。

### 5.2 页面改造规则

| 原模式 | 改造后 |
|---|---|
| onMounted(loadPage) | 服务端 prefetch + 客户端 hydration 后备用加载。 |
| watch([lang, slug], loadPage) | 仅在客户端导航时监听 route key 并刷新。 |
| getProductDetail 失败后显示空页面 | 服务端构建失败；运行时页面显示真实 404。 |
| SEO 在 router.afterEach 异步请求 | 页面数据就绪时同步设置 head，构建出的 HTML 已含 Meta。 |

不要把所有 onMounted 直接改为顶层 await。顶层 await 会让组件成为异步组件，必须同时处理 Suspense、失败状态和路由切换。对于现有项目，统一 prefetch store 的维护成本更低。

### 5.3 失败策略

| 场景 | 构建行为 | 线上行为 |
|---|---|---|
| 构建器无法连接后端 | 失败，保留当前线上版本 | 不切换版本。 |
| PUBLISHED 产品缺少必要数据 | 失败，并列出实体 slug | 管理后台阻止发布或标为 REVIEW。 |
| 产品不存在 | 不生成 URL | 真实 404。 |
| 单篇新闻已下线 | 从路由清单和 sitemap 移除 | 404 或已审核的 301。 |

绝不使用“fallback 数据继续构建”。它会产生错误索引和难以追溯的线上版本。

## 6. Head、双语和 Schema

### 6.1 Head 由单一服务生成

新增 useSeoDocument 或等价服务，输入页面实体并输出：

~~~text
title
description
canonical
robots
og:title / og:description / og:image / og:url
twitter:card
zh-CN / en / x-default alternate links
JSON-LD graph
~~~

使用 @unhead/vue 的 useHead 或 useSeoMeta。所有可索引页面在最终 HTML 中必须有一个 title、一个 description、一个 canonical。Unhead 会在 SSR/SSG 中汇总并去重 head 标签。  
参考：[Unhead 文档](https://unhead.unjs.io/)。

canonical 必须由 SITE_ORIGIN 加上无查询参数的规范路径生成；后台录入的 canonical 只允许本站 HTTPS 地址。

### 6.2 后端 SEO 解析补齐

当前 /api/v1/seo 对新闻详情有专属回退，但产品、系列、分类和应用需要补齐。新增统一的 SeoDocumentService：

~~~text
route key
  → 页面级 SEO 配置
  → 产品/分类/系列/应用/资讯自身 SEO 字段
  → 合法的站点默认值
  → 实体不存在时 NotFound
~~~

robots 和 schema_json 若继续保存在数据库，必须经过白名单验证并实际返回给渲染层；更推荐由可信的实体字段在服务端生成 Schema，而不是把管理员输入的原始 JSON 直接插入 head。

### 6.3 JSON-LD 规则

| 页面 | 标记 | 注意事项 |
|---|---|---|
| 首页/关于 | Organization、WebSite | 法定名称、地址、电话、logo、sameAs 必须经企业确认。 |
| 产品详情 | Product、BreadcrumbList | sku 使用内部型号；CAS 放 additionalProperty；报价型产品不输出 Offer。 |
| 资讯 | Article 或 NewsArticle、BreadcrumbList | 日期、图片、作者/审核机构必须真实。 |
| FAQ | FAQPage | 问题和答案必须实际展示，且经过技术审核。 |

JSON-LD 使用稳定的 @id，例如 https://www.origin-chemical.com/#organization。不要填充占位电话、地址、客户、库存或认证；不要把技术参数中的自由文本误写为 unitCode，无法映射标准单位时使用 unitText 或省略单位字段。

## 7. sitemap、robots 与旧 URL

### 7.1 使用现有 Spring Boot 接口

不引入静态 sitemap 插件。后端的 Sitemap 服务应输出：

- 绝对 canonical URL；
- PUBLISHED、未删除、可索引实体；
- 实体真实 updated_at 作为 lastmod；
- 每对翻译页面的 xhtml:link；
- 静态关于页面、分类、系列、产品、应用、新闻和新闻分类。

网关只代理 /sitemap.xml 与 /robots.txt 到 backend，设置合适的短缓存。内容发布后清除该缓存。

### 7.2 旧 URL 映射表

维护 deploy/redirects.map 或数据库表，字段为：

~~~text
old_path | target_path | status | approved_by | verified_at | note
~~~

所有 301 都必须有人审核等价性。无对应页面请返回 410/404，不要重定向到首页或随机产品。

## 8. 构建、验证、发布和回滚

### 8.1 推荐的 Compose 内构建流程

Dockerfile 的 frontend 构建阶段不能可靠访问运行中的 backend 数据库内容。因此不要在 docker build 阶段直接生成生产 SSG HTML。

推荐方式：

1. CI 构建并签名固定版本的前端构建器镜像；
2. 生产环境启动 backend 后，前端构建阶段（host 网络）使用 SSG_API_BASE=http://127.0.0.1:8080/api/v1；
3. builder 输出到不可变 releases/{build-id}；
4. 对候选版本执行静态和 HTTP 验证；
5. 仅验证通过后，原子切换 current 指针或网关挂载；
6. 清除 CDN 中受影响的 HTML、sitemap 和 robots 缓存；
7. 保留至少两个已验证版本供回滚。

内容发布和管理员“一键发布”都创建受限部署任务。任务参数仅包括内容 revision、已允许的发布环境和目标代码来源；它不得接收 shell 命令、任意 Git 分支、任意路径或 sudo 参数。

### 8.2 保留后台一键拉取、构建与发布

页面端可以完整触发以下流程：

~~~text
管理员点击“发布最新代码”
  → POST /api/v1/admin/deployments
  → 后端校验 ADMIN 身份、CSRF、二次确认和并发锁
  → 写入 deployment_job（QUEUED）
  → 同机部署代理领取任务
  → 固定脚本拉取允许的代码、构建、验证、生成 release
  → 原子切换 current release
  → 写入 SUCCESS / FAILED 与脱敏日志
  → 管理页面轮询或 SSE 展示进度，并允许回滚
~~~

这不是移除一键部署功能，而是把命令执行移到专用部署代理中。部署代理可运行固定、已审核的命令，其中可以包含 git reset --hard 和受限 sudo；HTTP 请求本身不携带命令文本。

建议的 deployment_job 字段：

~~~text
id | requested_by | requested_at | environment | source_ref
resolved_commit | content_revision | status | release_id
started_at | finished_at | log_path | error_summary | rollback_of
~~~

页面提交时只能选择预定义选项：

~~~text
environment: production 或 staging
source_ref: origin/main（或后台配置的允许分支）
mode: code-and-content 或 content-only
~~~

部署代理解析 source_ref 后记录不可变的 resolved_commit；后续构建、验证和回滚均以这个 commit 和 release_id 为准。

### 8.3 固定脚本与最小权限

允许在独立的部署工作目录运行固定脚本，例如：

~~~text
/srv/origin-chemical/deployer/
  releases/
  worktrees/
  logs/
  scripts/run-deployment.sh
  scripts/rollback-release.sh
~~~

脚本规则：

1. 使用专用 Unix 用户 origin-deploy，不使用应用进程账户；
2. Git 操作只针对专用部署 clone 或临时 worktree，绝不针对开发工作区；
3. 可执行 git fetch --prune origin、解析 origin/main 的 commit，并在专用 clone 中执行 git reset --hard 到该已解析 commit；
4. 不接受用户传入的 ref、remote、路径、命令行片段或环境变量；
5. build 使用锁定的 Node/Docker 镜像和 lockfile；
6. 生成候选 release 后必须先通过验证，才可切换 current；
7. 日志写入受限目录，页面端只读取脱敏后的任务日志。

如果 Nginx 的确需要 sudo，sudoers 只允许 origin-deploy 运行精确的运维命令，例如 nginx -t 和 systemctl reload nginx；禁止 NOPASSWD: ALL、通配符路径和由 Java 拼接的 sudo 参数。具体二进制绝对路径应由服务器管理员确认后写入 sudoers。

### 8.4 任务接口与页面

后端接口建议：

~~~text
POST /api/v1/admin/deployments
GET  /api/v1/admin/deployments/{id}
GET  /api/v1/admin/deployments/{id}/logs
POST /api/v1/admin/deployments/{id}/rollback
~~~

接口安全要求：

- 仅 ADMIN；使用现有 session、CSRF 和审计日志；
- 发布操作必须有二次确认，显示环境和将要发布的 commit；
- 同一环境只能有一个 RUNNING 任务；
- 生产发布要求最近 staging 验证通过，或记录具有原因的管理员豁免；
- 日志不可包含 token、密码、数据库连接串或完整环境变量；
- rollback 只能选择已验证 release，不能接受任意目录。

管理页面显示队列、阶段、目标 commit、release ID、开始/结束时间、验证结果与回滚按钮；不显示命令输入框。页面只对 zelin 显示入口；其他用户的部署相关 API 请求必须返回 403。

### 8.5 不允许的实现

- 不用 rsync --delete 覆盖当前线上目录后再验证；
- 不在构建失败时发布 fallback HTML；
- 不将未固定版本的 main 分支作为生产内容构建来源；
- 不让构建日志显示密钥、数据库密码或内部 API token。

### 8.6 原子回滚

回滚只能切换到最近一个已验证的静态 release；不可通过删除目录或重新构建来回滚。回滚后自动验证首页、一个产品页、一个资讯页、robots、sitemap 和后台深链接。

## 9. 自动化质量门禁

### 9.1 构建前

- 公共 API contract 测试：zh/en、空集合、缺失 slug、下线内容；
- 路由清单测试：无 admin/search/success、无重复、无参数占位符；
- SEO 数据测试：发布实体具备必填字段；
- 企业实体配置完整性测试。

### 9.2 静态产物

不要用 HTML 字节数作为通过标准。使用 HTML 解析器检查每一个生成页面：

~~~text
HTTP 路径和静态文件存在
单一 title、description、canonical
H1 存在且正文不是加载占位符
alternate 链接闭环
JSON-LD 可解析且无占位值
关键图片有 alt
链接为可抓取的 a[href]
页面不含 noindex（除非本应不可收录）
~~~

### 9.3 Staging 冒烟测试

对 staging 执行：

~~~text
首页、分类、系列、产品、应用、资讯：200
不存在产品：404
下线旧 URL：301、404 或 410，符合映射表
/robots.txt：200 text/plain
/sitemap.xml：200 XML，可解析
/zh/admin/任意深链接：200 + noindex，Vue Router 可打开
公开未知 URL：404，而不是首页 200
~~~

最后使用 Google Rich Results Test、Schema Validator 和 Search Console URL Inspection 抽检页面。结构化数据可辅助理解，但不保证富结果。

## 10. 交付拆分

| 任务 | 所有者 | 依赖 | 完成定义 |
|---|---|---|---|
| 企业事实表 | 业务/技术负责人 | 无 | 法定名称、联系方式、资质、产品公开边界确认。 |
| 路由清单脚本 | 前端/后端 | 公共 API | 动态路由可重复生成。 |
| Vite-SSG 最小 PoC | 前端 | 路由清单 | 首页、1 产品、1 资讯的 HTML 完整。 |
| 预取与 hydration | 前端 | PoC | 首屏不二次请求，站内导航正确刷新。 |
| SEO 解析服务 | 后端 | 企业事实表 | 所有页面类型返回专属 SEO。 |
| Schema 和 head | 前端/后端 | SEO 服务 | HTML 与可见内容一致。 |
| 网关与旧 URL | 运维/后端 | 映射表 | 无软 404，sitemap/robots 可访问。 |
| 构建 worker 与切换 | 运维 | PoC | 失败不影响线上，已演练回滚。 |
| 测试与站长平台 | QA/SEO | 全部 | Staging 和生产验收通过。 |

## 11. 完成定义

P0 不以“vite-ssg 已安装”作为完成。只有同时满足以下条件才可上线：

- [ ] 公开 URL 的原始 HTML 有真实正文和唯一 head；
- [ ] 动态产品、应用、资讯 URL 均已生成；
- [ ] API 失败会阻止发布，而不是生成空页；
- [ ] 未知公开 URL 返回 404；
- [ ] admin 深链接正常但不可收录；
- [ ] sitemap、robots、canonical、hreflang 已上线且可验证；
- [ ] Schema 无占位或商务虚构数据；
- [ ] 发布、缓存清除、原子切换和回滚演练完成；
- [ ] Search Console、Bing、百度已验证，sitemap 已提交。

## 12. CDN → Nginx → Spring Boot 的部署调整

Vite-SSG 后不需要增加 Node SSR 服务。部署拓扑仍是 CDN → Nginx → Spring Boot，但 Nginx 的职责需要拆分：公开 HTML 和静态资源由 Nginx 直接服务，动态 API 和动态 sitemap/robots 才转发给 Spring Boot。

~~~text
CDN
  → Nginx
      ├─ /、/zh/*、/en/*、/assets/*：Vite-SSG 静态 release
      ├─ /api/*：Spring Boot
      ├─ /sitemap.xml、/robots.txt：Spring Boot
      └─ /zh/admin/*、/en/admin/*：admin-shell.html（noindex）
  → Spring Boot
      └─ 内容 API、管理后台 API、询盘、分析、sitemap、robots
~~~

建议的 Nginx 路由顺序如下，实际文件路径和 upstream 名称应以生产环境为准：

~~~nginx
location ^~ /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location = /sitemap.xml {
    proxy_pass http://backend:8080;
    add_header Cache-Control "public, max-age=900" always;
}

location = /robots.txt {
    proxy_pass http://backend:8080;
    add_header Cache-Control "public, max-age=3600" always;
}

location ~ ^/(zh|en)/admin/ {
    root /srv/origin-chemical/releases/current;
    try_files /admin-shell.html =404;
    add_header X-Robots-Tag "noindex, nofollow" always;
}

location / {
    root /srv/origin-chemical/releases/current;
    try_files $uri $uri/ $uri.html =404;
}
~~~

缓存策略：

| 路径 | 浏览器缓存 | CDN 缓存 | 发布后动作 |
|---|---|---|---|
| HTML 页面 | no-cache 或短缓存 | 5–10 分钟 | 精确或全量 purge。 |
| /assets/ 带 hash 文件 | 1 年 immutable | 1 年 | 保留旧版本资源，不能发布后立即删除。 |
| /api/、后台、询盘 | private/no-store | 不缓存 | 无。 |
| sitemap.xml | 15 分钟 | 15 分钟 | 发布内容后 purge。 |
| robots.txt | 1 小时 | 1 小时 | 修改规则后 purge。 |

发布采用 releases/{release-id} 加 current 原子切换。由于 CDN 可能仍缓存旧 HTML，origin 必须保留旧 release 的带 hash assets 至少覆盖最大 CDN TTL；否则旧 HTML 会引用已经被删除的 JS/CSS。切换完成后清除 CDN 中 HTML、sitemap 和 robots 的缓存。

因此需要调整 Nginx 静态根目录、发布产物存放方式和 CDN 缓存清除流程；Spring Boot 的业务 API、后台认证、询盘和 sitemap/robots 逻辑不需要因为 Vite-SSG 改为 Node 服务。

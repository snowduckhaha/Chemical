# Origin Chemical SEO + GEO 优先级矩阵（审阅优化版）

> 审阅日期：2026-08-08  
> 适用域名：https://www.origin-chemical.com  
> 代码基线：site-mvp（Vue 3 + Vite + Spring Boot + MySQL + Docker Compose）

## 审阅结论

原方案的诊断方向是正确的：公开页面必须输出可抓取的 HTML，旧 URL 必须处理，双语、元数据和实体信息必须统一。

但原方案有五项会造成错误实施或不必要风险的问题，已在本版修正：

| 原方案表述 | 审阅结论 | 优化后的决定 |
|---|---|---|
| 所有爬虫都看不到 SPA 内容 | 表述过强。Google 能渲染部分 JavaScript，但不能以此作为收录保证；其他抓取器的能力也不应假定。 | P0 目标是让首个 HTML 就含正文、链接和 Meta，而不是猜测特定爬虫能力。 |
| sitemap 和 robots 不存在 | 对当前线上站成立；但本地后端已经实现了两个接口。 | P0 改为让网关正确代理现有接口，并修正 sitemap 的 lastmod 和双语链接。 |
| 用 vite-plugin-sitemap 生成 sitemap | 不适用于数据库驱动的动态产品、应用和新闻 URL，且会与后端现有接口重复。 | 保留 Spring Boot 动态 sitemap，作为唯一来源。 |
| Product 一律添加 Offer / InStock | B2B 报价型产品没有公开价格或库存时，此标记不真实。 | 输出 Product、manufacturer、additionalProperty；仅在价格和库存真实公开时才增加 Offer。 |
| GEO 是 P3 | 抓取可见性、企业实体一致性和事实数据是基础，不应延后。 | 可抓取 HTML、实体一致性、技术事实卡列为 P0/P1；内容扩展和引用监测列为 P2。 |

Google 建议采用服务端或静态渲染作为 JavaScript 网站的长期方案，而非依赖面向爬虫的动态渲染。  
参考：[Google JavaScript SEO](https://developers.google.com/search/docs/crawling-indexing/javascript/javascript-seo-basics)、[Google sitemap 指南](https://developers.google.com/search/docs/crawling-indexing/sitemaps/build-sitemap)。

## 优先级定义

| 级别 | 完成时限 | 定义 |
|---|---:|---|
| P0 | 上线前 | 不完成则可能产生空壳页、软 404、错误收录或错误实体信号。 |
| P1 | 上线后 2 周 | 影响页面理解、索引效率、展示质量和可信度。 |
| P2 | 上线后 1–3 月 | 增强非品牌流量、合格询盘与 AI 搜索引用概率。 |
| P3 | 持续 | 内容扩展、外部提及和实验性能力。 |

## P0：发布门槛

### P0-1：静态生成公开页面

| 项目 | 要求 |
|---|---|
| 问题 | 当前公开站是 SPA，正文和 Meta 在浏览器运行后才加载。 |
| 决定 | 使用 Vite-SSG 预生成公开路由；后台管理保持客户端 SPA。 |
| 范围 | /zh、/en、产品分类/系列/详情、应用详情、资讯详情、关于、联系。 |
| 非范围 | 管理后台、站内搜索、询盘成功页不生成可索引页面。 |
| 依赖 | 构建容器能经 Docker 私有网络读取 Spring Boot 公共 API。 |
| 验收 | 随机抽取 10 个产品/资讯 URL，直接请求 HTML 可看到 H1、正文、canonical、description 与 JSON-LD。 |
| 预计 | 5–8 个开发日；不含业务内容审核。 |

详见 [vite-ssg-development-guide.md](/D:/Zelin/Chemical/vite-ssg-development-guide.md)。

### P0-2：正确处理 sitemap、robots 和不可收录路由

| 项目 | 要求 |
|---|---|
| sitemap | 网关将 /sitemap.xml 代理到后端；只列出 PUBLISHED 且可索引的 canonical URL；lastmod 使用实体真实更新时间。 |
| robots | 网关将 /robots.txt 代理到后端；允许公开页面；列出 sitemap；仅在企业决定接受 ChatGPT Search 抓取时明确允许 OAI-SearchBot。 |
| noindex | /zh/admin、/en/admin、/zh/search、/en/search、/contact/success 响应 X-Robots-Tag: noindex, nofollow。 |
| 双语 | 每对翻译页面输出 rel=alternate 的 zh-CN、en 和 x-default；没有真实翻译的页面不互相标注。 |
| 验收 | XML 可解析；每个 sitemap URL 返回 200；robots 为 text/plain；后台和搜索页带 noindex。 |
| 预计 | 1–2 个开发日。 |

OpenAI 表明，若希望页面进入 ChatGPT Search 的摘要和引用范围，应避免阻止 OAI-SearchBot；这不是排名保证。  
参考：[OpenAI 发布者 FAQ](https://help.openai.com/en/articles/12627856-publishers-and-developers-faq)。

### P0-3：旧 URL 迁移和真实状态码

| 项目 | 要求 |
|---|---|
| 先做清单 | 从 Search Console、服务器日志、现有 sitemap 和外链整理旧 URL → 新 URL 映射；不得猜测映射。 |
| 有等价内容 | 网关层 301 到最接近的新页面；保留查询参数仅在业务需要时保留。 |
| 内容永久取消 | 返回 410；无法确定是否永久取消时返回 404。 |
| 禁止 | 不把所有未知 URL 返回 200 + 首页，也不把不相关产品一律重定向到首页。 |
| 验收 | 旧 URL 样本的 301、404、410 比例符合映射表；不存在软 404。 |
| 预计 | 1–3 个开发日，取决于历史 URL 数量。 |

移除请求是补充动作：应先上线 301/404/410，再在 Search Console 验证覆盖情况；不要用移除请求替代迁移。

### P0-4：品牌与页面事实统一

上线前由企业书面确认以下内容，并作为网站、Schema、资料 PDF 和第三方主页的唯一事实来源：

1. 法定中文名、法定英文名、品牌名及三者关系；
2. 办公/工厂地址、电话、销售邮箱；
3. 已获证书的名称、编号、认证范围、有效期；
4. 各型号允许公开的化学、性能、包装和应用信息；
5. 技术资料和安全资料的审核责任人。

不得把未证实的 Qidian、Origin Chemical、Start Point 等名称互相当作别名，也不得发布占位地址、电话、产能、客户名称或认证。

## P1：页面理解与索引质量

| 编号 | 工作项 | 优化要求 | 验收标准 | 预计 |
|---|---|---|---|---:|
| P1-1 | 动态 SEO 解析 | 后端按 route key 解析产品、分类、系列、应用、新闻的专属 title、description、canonical、OG 和 robots。产品字段不能再回退为通用首页 Meta。 | 每类页面至少 3 个样本有唯一 title 和 description。 | 1–2 天 |
| P1-2 | Head 与 hreflang | 使用 @unhead/vue 在 SSG 输出中写入 head；canonical 无参数且为 HTTPS 本站 URL。 | 原始 HTML 中仅一个 canonical；双语互相闭环。 | 1 天 |
| P1-3 | Schema | Organization、Product、BreadcrumbList、Article/NewsArticle 由经过验证的数据生成。 | Schema Validator 无语法错误；页面可见内容与 JSON-LD 一致。 | 2–3 天 |
| P1-4 | 图片与文档 | 产品图片有描述性 alt；首屏图不懒加载；TDS/SDS/证书有版本、语言、审核日期和 HTML 摘要。 | 页面无关键空 alt；PDF 不是唯一技术信息来源。 | 2–4 天 |
| P1-5 | 后台一键发布 | 仅用户名 zelin 可在页面触发“拉取代码 → 构建 → 验证 → 发布”；后台创建审计化部署任务，服务器部署代理执行固定脚本。 | 非 zelin 即使拥有 ADMIN 角色也返回 403；失败构建不会覆盖线上版本；页面可查看任务日志、目标 commit、发布版本和回滚记录。 | 2–3 天 |
| P1-6 | 搜索平台 | 验证 Google Search Console、Bing Webmaster、百度搜索资源平台，并提交 sitemap。 | 站点所有权、sitemap 抓取和首批 URL 检查完成。 | 0.5 天 |

结构化数据用于帮助搜索引擎理解页面，不保证富结果，也不能标记页面上不可见或不真实的信息。  
参考：[Google structured data 政策](https://developers.google.com/search/docs/appearance/structured-data/sd-policies)。

## P2：GEO 与内容转化

| 工作项 | 首批交付 | 衡量方法 |
|---|---|---|
| 产品事实卡 | 每个主推型号有用途、型号、关键指标、测试/资料状态、适用边界、最后审核日期、样品 CTA。 | 产品页到询盘/资料下载的转化率。 |
| 应用解决方案页 | 覆铜板、阻燃填料等经业务确认的应用页；包含选型因素、关联产品、常见问题。 | 非品牌词展示、点击和合格询盘。 |
| 技术文章 | 每月 2 篇，经技术审核，有作者、日期、引用来源、关联产品。 | 收录、外部引用、询盘辅助转化。 |
| 企业可信页 | 证书验证、实验室能力、质量流程、物流能力；只写可举证事实。 | 品牌查询质量和销售使用反馈。 |
| AI 可见性监测 | 建立 30 条中英文真实采购问题的月度基线，记录是否提及、引用 URL、事实是否正确。 | 引用覆盖率、引用准确率、来自 AI 的引荐询盘。 |

FAQ 和 How-To 只在确有真实问题或操作说明时创建；不要为 Schema 或 AI 搜索而批量制造重复问答。llms.txt 可作为可选说明文件，但不是收录或引用的 P0 依赖。

## 依赖与交付顺序

~~~text
P0-4 企业事实确认
  ├─ P0-1 Vite-SSG 验证与迁移
  │    ├─ P1-1 动态 SEO 解析
  │    ├─ P1-2 Head / canonical / hreflang
  │    └─ P1-3 Schema
  ├─ P0-2 sitemap / robots / noindex
  └─ P0-3 旧 URL 映射

P0 全部验收
  └─ P1 发布流程与站长平台验证
       └─ P2 内容、资料与 GEO 监测
~~~

## 上线后 30 天指标

不承诺排名或 AI 引用。以下指标用于判断改造是否真实生效：

| 指标 | 目标 |
|---|---|
| 关键公开 URL 的可抓取 HTML | 100% |
| sitemap URL 返回 200 且 canonical 自洽 | 100% |
| 旧 URL 映射样本的正确状态码 | 100% |
| 新发布内容从发布到静态版本可见 | 小于 15 分钟 |
| Search Console 中的软 404 | 持续下降，逐项处理 |
| 产品页唯一 title/description/canonical | 100% |
| 30 条 GEO 查询的月度可复测记录 | 100% 有基线 |

## P0 上线签字清单

- [ ] 企业事实清单已确认；
- [ ] 公开页面静态 HTML 已抽检；
- [ ] 后台、搜索、成功页不可收录；
- [ ] sitemap 与 robots 已通过线上测试；
- [ ] 旧 URL 映射已审核并上线；
- [ ] 产品和文章 Meta 不再回退为通用内容；
- [ ] Schema 仅含页面上真实可见的数据；
- [ ] 构建、验证、切换、回滚各执行过一次演练；
- [ ] Search Console、Bing、百度已验证并提交 sitemap。

# origin-chemical.com SEO + GEO 审计报告

> 审计日期：2026-07-25  
> 审计对象：https://www.origin-chemical.com  
> 网站类型：B2B 化工企业（氢氧化铝、氧化铝等材料供应商）  
> 技术栈：Vue.js 3 + Vite（SPA 单页应用）

---

## 一、执行摘要

**整体健康度：🔴 极差（Critical）**

当前网站处于"MVP"开发阶段，作为 Vue.js SPA 上线，但**完全没有针对搜索引擎进行优化**。静态 HTML 仅 420 字节，所有内容依赖客户端 JavaScript 渲染。更严重的是，**旧版中文网站的大量页面仍被 Google 索引，但访问后全部返回同一个空壳首页**，形成大规模"软 404"问题，正在持续损害域名权重。

**如果不立即修复，该网站几乎不可能从搜索引擎获得任何自然流量。**

---

## 二、P0 级问题 — 阻断索引与排名（立即处理）

### 1. SPA 客户端渲染导致搜索引擎无法抓取内容

| 项目 | 详情 |
|------|------|
| **问题** | 网站为纯 Vue.js SPA，服务端返回的 HTML 仅包含 `<div id="app"></div>`，所有内容（文字、产品、新闻）均由浏览器 JS 渲染 |
| **影响** | Google 虽具备 JS 渲染能力，但存在抓取配额限制、渲染延迟和索引不确定性；Bing、百度等搜索引擎基本无法索引内容 |
| **证据** | 首页 HTML 仅 420 字节；curl 获取不到任何正文内容；所有 `/zh/` 及子页面返回相同空壳 HTML |
| **解决方案** | **迁移至 SSR/SSG 架构**：<br>① 首选：将 Vue 项目迁移至 **Nuxt.js**（Nitro 引擎支持 SSR/SSG/ISR）<br>② 次选：使用 **Vite-SSG** 为关键页面生成静态 HTML<br>③ 过渡方案：部署 **Prerender.io** 或 Cloudflare 边缘渲染，为爬虫提供预渲染快照 |
| **工作量** | 2–4 周（视项目复杂度） |

### 2. 全站使用开发占位符标题 "Qidian Chemical - MVP"

| 项目 | 详情 |
|------|------|
| **问题** | 所有页面 `<title>` 均为 "Qidian Chemical - MVP"，无关键词、无描述、无差异化 |
| **影响** | 搜索引擎无法判断页面主题；SERP 中展示效果极差，点击率趋近于零； brand 名称 "Qidian" 与域名 "origin" 不一致，造成品牌认知混乱 |
| **证据** | `curl` 获取的首页及所有测试页面 title 完全一致 |
| **解决方案** | ① 为每个路由配置唯一、含关键词的 `<title>`（首页：Aluminum Hydroxide Supplier for CCL Industry \| Origin Chemical；产品页：High-Purity ATH Powder \| Origin Chemical）<br>② 统一品牌名称为 "Origin Chemical"（与域名一致）<br>③ 开发动态 title 注入机制（结合路由元信息或 CMS） |
| **工作量** | 2–3 天（开发）+ 1 天（内容撰写） |

### 3. 旧版 `/zh/` 页面大规模软 404

| 项目 | 详情 |
|------|------|
| **问题** | Google 索引了大量旧版中文页面（如 `/zh/applications/thermal-management-new-energy`、`/zh/news/industry-trends/ath-flame-retardant-advantages`、`/zh/about/certificates` 等），但这些 URL 现在全部返回 200 状态码的空壳 SPA，内容完全消失 |
| **影响** | 搜索引擎将判定这些页面为低质量/空页面，导致整站信任度下降；用户从搜索结果点击进入后看不到预期内容，跳出率 100% |
| **证据** | `site:origin-chemical.com` 显示 8+ 个 `/zh/` 索引结果；实际访问这些 URL 均返回 "Qidian Chemical - MVP" |
| **解决方案** | ① **立即**在服务端配置 301 重定向：将旧 `/zh/` URL 映射到新站对应页面（如有）<br>② 无对应新页面时返回 **410 Gone** 或 **404**，并提交 Google Search Console 移除请求<br>③ 创建自定义 404 页面，引导用户至首页或产品页 |
| **工作量** | 2–3 天 |

### 4. 完全没有 Meta Description

| 项目 | 详情 |
|------|------|
| **问题** | 全站无 `<meta name="description">` 标签 |
| **影响** | 搜索引擎随机抓取页面文本作为摘要，无法控制 SERP 展示文案，大幅降低点击率 |
| **解决方案** | 为每个页面编写 150–160 字符的独特 meta description，含核心关键词和行动号召 |
| **工作量** | 1–2 天（约 10–15 个核心页面） |

---

## 三、P1 级问题 — 严重影响排名（1 周内处理）

### 5. 缺少结构化数据（Schema.org / JSON-LD）

| 项目 | 详情 |
|------|------|
| **问题** | 全站无任何 JSON-LD 或 Microdata 标记 |
| **影响** | 搜索引擎无法理解实体关系；无法获得富媒体摘要（Rich Snippets）；GEO（AI 引擎优化）中 AI 模型无法提取结构化信息 |
| **必须实施的 Schema** | ① `Organization`：公司名称、logo、地址、联系方式<br>② `Product`：每种产品的名称、描述、规格参数、CAS 号<br>③ `BreadcrumbList`：面包屑导航<br>④ `WebSite`：站点名称、搜索框<br>⑤ `Article`（新闻/博客页）：作者、发布日期、修改日期 |
| **解决方案** | 在 SSR/SSG 渲染时注入对应 JSON-LD 脚本；产品数据可从 CMS/数据库动态生成 |
| **工作量** | 3–5 天 |

### 6. 没有 Sitemap.xml 和 Robots.txt

| 项目 | 详情 |
|------|------|
| **问题** | `/sitemap.xml` 和 `/robots.txt` 均返回首页 HTML（不存在） |
| **影响** | 搜索引擎无法高效发现所有页面；无法指导爬虫行为 |
| **解决方案** | ① 生成动态 `sitemap.xml`（含所有产品页、应用页、新闻页、分类页）<br>② 创建 `robots.txt`，引用 sitemap 并阻止后台/admin 路径<br>③ 提交至 Google Search Console 和 Bing Webmaster Tools |
| **工作量** | 1 天 |

### 7. 中英文版本 SEO 架构混乱

| 项目 | 详情 |
|------|------|
| **问题** | 旧版中文站被删除但仍在索引中；新版英文站内容极简；语言版本间无 `hreflang` 标签 |
| **影响** | 中英文用户都可能看到错误语言版本；国际化 SEO 完全失效 |
| **解决方案** | ① 如果保留中文内容，使用 `/zh/` 子目录并配置 `hreflang` 标签<br>② 如果专注英文市场，将中文页面 301 重定向至英文对应页或返回 404<br>③ 首页根路径 `/` 使用 `x-default` 指向主要目标市场版本 |
| **工作量** | 2–3 天 |

### 8. 缺少 Canonical 标签

| 项目 | 详情 |
|------|------|
| **问题** | 全站无 `<link rel="canonical">` |
| **影响** | SPA 中不同路由参数可能产生重复内容（如 `?id=1` 和 `?id=2`），导致搜索引擎分散权重 |
| **解决方案** | 每个页面添加自引用 canonical；产品变体页使用规范 URL |
| **工作量** | 0.5 天 |

---

## 四、P2 级问题 — 影响点击与转化（2 周内处理）

### 9. 缺少 Open Graph 与 Twitter Cards

| 项目 | 详情 |
|------|------|
| **问题** | 无 `og:title`、`og:description`、`og:image`、`twitter:card` 等标签 |
| **影响** | 社交媒体分享时展示效果极差，无法吸引点击 |
| **解决方案** | 为每个页面配置 OG 标签；产品页使用产品图片作为 `og:image`；使用 1200×630 尺寸图片 |
| **工作量** | 1 天 |

### 10. 图片 SEO 缺失

| 项目 | 详情 |
|------|------|
| **问题** | SPA 中图片通过 JS 动态加载，爬虫可能看不到；缺少描述性文件名和 alt 文本 |
| **影响** | 图片搜索流量为零；辅助功能（Accessibility）差 |
| **解决方案** | ① 使用语义化文件名（如 `aluminum-hydroxide-ath-powder-ccl.jpg`）<br>② 所有产品图片添加描述性 alt 文本<br>③ 实现懒加载（`loading="lazy"`）但确保首屏图片不使用懒加载 |
| **工作量** | 1–2 天 |

### 11. URL 结构缺乏关键词

| 项目 | 详情 |
|------|------|
| **问题** | 当前 URL 可能使用 hash 路由或简单 slug，缺少产品关键词 |
| **影响** | URL 是排名因素之一，关键词缺失削弱相关性信号 |
| **解决方案** | 使用语义化 URL 结构：<br>`/products/aluminum-hydroxide/ath-series`<br>`/applications/copper-clad-laminate`<br>`/news/flame-retardant-trends-2026` |
| **工作量** | 2–3 天 |

### 12. 缺乏 E-E-A-T 信号

| 项目 | 详情 |
|------|------|
| **问题** | 对于化工 B2B 网站，缺少专业资质、作者/团队介绍、引用来源等信任信号 |
| **影响** | Google 的 E-E-A-T（经验、专业、权威、可信）评估中得分低，尤其在 YMYL（Your Money Your Life）相关行业 |
| **解决方案** | ① 关于页面展示研发团队资历、实验室认证<br>② 新闻/技术文章添加作者署名和日期<br>③ 产品页面引用技术标准和测试数据<br>④ 添加客户案例和证书（ISO、RoHS 等） |
| **工作量** | 3–5 天 |

---

## 五、GEO（生成式引擎优化）问题

GEO 指优化内容以被 ChatGPT、Perplexity、Gemini 等 AI 搜索引擎引用和推荐。

### 13. 内容对 AI 爬虫不可见

| 项目 | 详情 |
|------|------|
| **问题** | AI 爬虫（如 OpenAI-GPTBot、PerplexityBot）通常不执行 JavaScript，SPA 内容对它们完全不可见 |
| **影响** | 在 AI 搜索中，用户查询 "aluminum hydroxide supplier for CCL"、"ATH flame retardant manufacturer" 时，该网站不可能被引用 |
| **解决方案** | **与 SEO 相同**：实施 SSR/SSG，确保爬虫获取完整 HTML |
| **工作量** | 已包含在 P0#1 中 |

### 14. 缺少 AI 友好的内容格式

| 项目 | 详情 |
|------|------|
| **问题** | 内容以营销文案为主，缺少 AI 模型容易提取的结构化信息（FAQ、对比表格、技术规格表、数据点） |
| **影响** | AI 搜索倾向于引用包含明确数据、对比信息、问答格式的内容 |
| **解决方案** | ① 在每个产品页添加 **FAQ 区块**（如 "What is the difference between ATH and MDH?"、"What particle size is suitable for CCL?"）<br>② 使用 **对比表格** 展示产品规格（粒径、白度、水分、磁性物质含量）<br>③ 添加 **How-To 内容**（如 "How to choose aluminum hydroxide for flame retardant applications"）<br>④ 使用清晰的 **H2/H3 标题层级**，便于 AI 提取章节结构 |
| **工作量** | 1–2 周（内容撰写） |

### 15. 缺少实体化描述

| 项目 | 详情 |
|------|------|
| **问题** | 网站未在结构化数据中明确定义企业实体和产品实体，AI 难以建立知识图谱关联 |
| **影响** | AI 模型无法将 "Origin Chemical" 与 "aluminum hydroxide supplier"、"CCL industry" 等概念关联 |
| **解决方案** | ① 在首页 JSON-LD 中定义 `Organization`，包含 `@id`（可解析的 URI）<br>② 产品页使用 `Product` + `Offer` schema，关联到组织实体<br>③ 在内容中自然提及行业实体（如 "copper clad laminate (CCL)"、"flame retardant filler"、"alumina trihydrate (ATH)"）<br>④ 添加 Wikipedia / Wikidata 风格的同义词和定义 |
| **工作量** | 3–5 天 |

---

## 六、核心页面 SEO 检查清单

| 页面 | Title（当前） | Title（建议） | Meta Description（建议） |
|------|--------------|--------------|------------------------|
| 首页 | Qidian Chemical - MVP | Aluminum Hydroxide & Alumina Supplier for CCL \| Origin Chemical | Origin Chemical specializes in high-purity aluminum hydroxide (ATH) and alumina products for copper clad laminate, flame retardant, and thermal management applications. ISO-certified manufacturing since 2010. |
| 产品首页 | Qidian Chemical - MVP | Aluminum Hydroxide & Specialty Alumina Products \| Origin Chemical | Explore our comprehensive range of aluminum hydroxide powders, nano-alumina, and silane coupling agents for CCL, wire & cable, and thermal management applications. |
| 关于我们 | Qidian Chemical - MVP | About Origin Chemical \| Aluminum Hydroxide Manufacturer Since 2010 | Shenzhen Start Point Chemical Co., Ltd. — a leading China-based manufacturer of specialty aluminum hydroxide for the CCL industry. 50+ professionals, in-house R&D lab. |
| 新闻页 | Qidian Chemical - MVP | Industry News & Flame Retardant Insights \| Origin Chemical | Stay updated on the latest trends in aluminum hydroxide applications, CCL industry developments, and flame retardant technology from Origin Chemical experts. |
| 联系页 | Qidian Chemical - MVP | Contact Origin Chemical \| Request a Quote or Sample | Get in touch with Origin Chemical for aluminum hydroxide inquiries, technical support, or sample requests. 72-hour domestic delivery, global shipping available. |

---

## 七、竞品 SEO 差距分析

通过搜索 "aluminum hydroxide CCL supplier China"、"ATH flame retardant manufacturer" 等关键词，竞品在以下方面领先：

| 维度 | 竞品做法 | 当前差距 |
|------|--------|--------|
| **B2B 平台** | 大量竞品在 Made-in-China、Alibaba 有完善产品页 | 起点化工无 B2B 平台页面（或无法搜索到） |
| **行业报告** | MarketsandMarkets、Grand View Research 等报告中提及头部企业 | 起点化工未被任何行业报告引用 |
| **技术内容** | 竞品网站有详细技术白皮书、应用指南、数据表（TDS/SDS） | 当前网站几乎无技术深度内容 |
| **结构化数据** | 大型化工平台使用完整 Product / Organization schema | 完全缺失 |

---

## 八、优先行动计划

| 阶段 | 时间 | 任务 | 工作量 | 预期效果 |
|------|------|------|--------|----------|
| **紧急** | 立即–3 天 | ① 配置旧 `/zh/` URL 的 301/404 响应<br>② 修复全站 title（移除 "MVP"）<br>③ 添加 meta description 到核心页面 | 3–4 天 | 停止权重流失，改善 SERP 展示 |
| **第一阶段** | 1–2 周 | ① 实施 SSR/SSG（迁移至 Nuxt.js 或 Vite-SSG）<br>② 生成 sitemap.xml 和 robots.txt<br>③ 添加 canonical 标签 | 1.5–2.5 周 | 确保内容可被索引 |
| **第二阶段** | 2–3 周 | ① 实施结构化数据（Organization + Product + BreadcrumbList + Article）<br>② 配置 hreflang 和语言版本架构<br>③ 添加 Open Graph / Twitter Cards | 1.5–2 周 | 获得富媒体摘要，提升点击率 |
| **第三阶段** | 1–2 个月 | ① 重建技术内容（FAQ、How-To、对比表格）<br>② 优化图片文件名和 alt 文本<br>③ 增强 E-E-A-T 信号（证书、案例、作者信息）<br>④ 注册并优化 Google Search Console / Bing Webmaster Tools | 3–4 周 | 提升排名和 AI 引用概率 |
| **持续** | 长期 | ① 定期发布行业洞察/技术文章<br>② 监控排名和索引状态<br>③ 收集客户评价并展示 | 持续 | 建立领域权威，获取自然外链 |

---

## 九、工作量总览

| 类别 | 预估工作量 | 备注 |
|------|-----------|------|
| 技术架构改造（SSR/SSG） | 2–4 周 | 最大投入，但为所有 SEO 工作的基础 |
| 重定向与 404 修复 | 2–3 天 | 必须立即执行 |
| Meta 标签与 Title | 2–3 天 | 含内容撰写 |
| 结构化数据实施 | 3–5 天 | 可随 SSR 同步进行 |
| Sitemap + Robots.txt | 1 天 | 简单但关键 |
| 图片 SEO | 1–2 天 | 含 alt 文本撰写 |
| Open Graph / Twitter Cards | 1 天 | 低投入高回报 |
| 内容重建（FAQ + 技术文章） | 1–2 周 | 可外包给行业写手 |
| E-E-A-T 增强 | 3–5 天 | 收集证书、案例、团队信息 |
| **总计** | **约 6–10 周** | 技术架构占 40%，内容占 35%，其余 25% |

---

## 十、关键建议

1. **不要继续在 SPA 模式下做 SEO**。在 SSR/SSG 完成前，任何内容优化、外链建设都效果甚微，因为搜索引擎根本看不到内容。

2. **品牌名称必须统一**。当前 "Qidian Chemical"（网站标题）与 "Origin Chemical"（域名/英文名）和 "起点化工"（中文名）不一致，应统一为 "Origin Chemical"（英文市场）。

3. **旧站内容不要直接丢弃**。旧版中文站有应用详情页、行业趋势文章、证书页等有价值内容，应在迁移时保留并翻译/重定向。

4. **化工 B2B SEO 的核心是技术内容**。目标客户（CCL 制造商、阻燃材料配方师）搜索的是技术参数和应用解决方案，而非营销话术。应优先创建 "ATH vs MDH 对比"、"CCL 用氢氧化铝粒径选择指南" 等技术深度内容。

5. **GEO 优化与 SEO 基础设施高度重合**。实施 SSR + 结构化数据 + FAQ 内容，同时满足传统搜索和 AI 搜索的需求。

-- 修复 seo_meta 表中 4 条被双重编码损坏的中文 SEO 记录。
--
-- 现象：/zh/about、/zh/about/culture、/zh/applications、/zh/contact 的
--       <title> 与 meta description 在线上为乱码（如 “åº”ç”¨é¢†åŸŸ”）。
-- 原因：这 4 行数据曾被以 latin1 会话字符集的客户端连接写入，
--       UTF-8 字节被当作 latin1 字符再次编码后存入。
--
-- 执行方式（必须显式指定 utf8mb4 客户端字符集）：
--   docker compose --env-file .env -f deploy/docker-compose.prod.yml \
--     exec -T mysql mysql --default-character-set=utf8mb4 \
--     -uroot -p"${MYSQL_ROOT_PASSWORD}" qidian_site \
--     < sql/ops_fix_seo_meta_encoding.sql
--
-- 执行后需要重新触发前端 SSG 构建（后台“部署”或重跑 deploy.sh），
-- 让静态 HTML 中的 title/description 重新生成，并刷新 CDN 缓存。

USE qidian_site;

UPDATE seo_meta SET
  title = '关于起点化工 | 深圳市起点化工有限公司',
  description = '深圳市起点化工有限公司专注氢氧化铝与先进无机材料，为覆铜板、无卤阻燃与功能材料应用提供可靠产品与技术支持。',
  og_title = '关于起点化工 | 深圳市起点化工有限公司',
  og_description = '深圳市起点化工有限公司专注氢氧化铝与先进无机材料，为覆铜板、无卤阻燃与功能材料应用提供可靠产品与技术支持。'
WHERE lang = 'zh' AND page_key = 'about';

UPDATE seo_meta SET
  title = '企业文化 | 起点化工',
  description = '了解起点化工的经营理念、质量方针与团队协作方式。',
  og_title = '企业文化 | 起点化工',
  og_description = '了解起点化工的经营理念、质量方针与团队协作方式。'
WHERE lang = 'zh' AND page_key = 'about.culture';

UPDATE seo_meta SET
  title = '应用领域 | 起点化工',
  description = '起点化工材料应用于覆铜板、无卤阻燃、工程塑料、电线电缆与功能复合等场景。',
  og_title = '应用领域 | 起点化工',
  og_description = '起点化工材料应用于覆铜板、无卤阻燃、工程塑料、电线电缆与功能复合等场景。'
WHERE lang = 'zh' AND page_key = 'applications';

UPDATE seo_meta SET
  title = '联系我们 | 起点化工',
  description = '联系起点化工获取产品报价、样品申请与技术支持。',
  og_title = '联系我们 | 起点化工',
  og_description = '联系起点化工获取产品报价、样品申请与技术支持。'
WHERE lang = 'zh' AND page_key = 'contact';

-- 验证：以下查询应全部返回正常中文。
SELECT page_key, title FROM seo_meta
WHERE lang = 'zh'
  AND page_key IN ('about', 'about.culture', 'applications', 'contact');

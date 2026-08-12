-- 为缺少专属 SEO 记录的公开页面补齐页面级 SEO（品牌统一为 Origin Chemical / 起点化工）。
-- page_key 与前端 pageKeyFor 生成的键一致；唯一键为 (page_key, lang)。
INSERT INTO seo_meta (entity_type, entity_id, lang, page_key, title, description, og_title, og_description, canonical, publish_status) VALUES
('PAGE', NULL, 'zh', 'about', '关于起点化工 | 深圳市起点化工有限公司', '深圳市起点化工有限公司专注氢氧化铝与先进无机材料，为覆铜板、无卤阻燃与功能材料应用提供可靠产品与技术支持。', '关于起点化工 | 深圳市起点化工有限公司', '深圳市起点化工有限公司专注氢氧化铝与先进无机材料，为覆铜板、无卤阻燃与功能材料应用提供可靠产品与技术支持。', '/zh/about', 'PUBLISHED'),
('PAGE', NULL, 'en', 'about', 'About Origin Chemical | Advanced Inorganic Materials', 'Origin Chemical focuses on aluminum hydroxide and advanced inorganic materials for copper clad laminate, halogen-free flame retardancy and functional material applications.', 'About Origin Chemical | Advanced Inorganic Materials', 'Origin Chemical focuses on aluminum hydroxide and advanced inorganic materials for copper clad laminate, halogen-free flame retardancy and functional material applications.', '/en/about', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'about.culture', '企业文化 | 起点化工', '了解起点化工的经营理念、质量方针与团队协作方式。', '企业文化 | 起点化工', '了解起点化工的经营理念、质量方针与团队协作方式。', '/zh/about/culture', 'PUBLISHED'),
('PAGE', NULL, 'en', 'about.culture', 'Company Culture | Origin Chemical', 'Learn about the values, quality policy and collaboration approach of Origin Chemical.', 'Company Culture | Origin Chemical', 'Learn about the values, quality policy and collaboration approach of Origin Chemical.', '/en/about/culture', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'about.certificates', '资质证书 | 起点化工', '查看起点化工的管理体系认证与产品检测证书。', '资质证书 | 起点化工', '查看起点化工的管理体系认证与产品检测证书。', '/zh/about/certificates', 'PUBLISHED'),
('PAGE', NULL, 'en', 'about.certificates', 'Certificates | Origin Chemical', 'View the management system certifications and product test certificates of Origin Chemical.', 'Certificates | Origin Chemical', 'View the management system certifications and product test certificates of Origin Chemical.', '/en/about/certificates', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'applications', '应用领域 | 起点化工', '起点化工材料应用于覆铜板、无卤阻燃、工程塑料、电线电缆与功能复合等场景。', '应用领域 | 起点化工', '起点化工材料应用于覆铜板、无卤阻燃、工程塑料、电线电缆与功能复合等场景。', '/zh/applications', 'PUBLISHED'),
('PAGE', NULL, 'en', 'applications', 'Applications | Origin Chemical', 'Origin Chemical materials serve copper clad laminate, halogen-free flame retardancy, engineering plastics, wire and cable and functional composite applications.', 'Applications | Origin Chemical', 'Origin Chemical materials serve copper clad laminate, halogen-free flame retardancy, engineering plastics, wire and cable and functional composite applications.', '/en/applications', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'news.list', '资讯中心 | 起点化工', '获取起点化工的行业动态、技术知识与企业新闻。', '资讯中心 | 起点化工', '获取起点化工的行业动态、技术知识与企业新闻。', '/zh/news', 'PUBLISHED'),
('PAGE', NULL, 'en', 'news.list', 'News Center | Origin Chemical', 'Follow industry trends, technical insights and company updates from Origin Chemical.', 'News Center | Origin Chemical', 'Follow industry trends, technical insights and company updates from Origin Chemical.', '/en/news', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'contact', '联系我们 | 起点化工', '联系起点化工获取产品报价、样品申请与技术支持。', '联系我们 | 起点化工', '联系起点化工获取产品报价、样品申请与技术支持。', '/zh/contact', 'PUBLISHED'),
('PAGE', NULL, 'en', 'contact', 'Contact Us | Origin Chemical', 'Contact Origin Chemical for product quotes, sample requests and technical support.', 'Contact Us | Origin Chemical', 'Contact Origin Chemical for product quotes, sample requests and technical support.', '/en/contact', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'products.home', '产品中心 | 起点化工', '氢氧化铝、硅粉、氧化铝与硅烷偶联剂等阻燃与功能材料产品矩阵。', '产品中心 | 起点化工', '氢氧化铝、硅粉、氧化铝与硅烷偶联剂等阻燃与功能材料产品矩阵。', '/zh/products', 'PUBLISHED'),
('PAGE', NULL, 'en', 'products.home', 'Product Center | Origin Chemical', 'Product matrix for flame-retardant and functional materials: aluminum hydroxide, silica powder, alumina and silane coupling agents.', 'Product Center | Origin Chemical', 'Product matrix for flame-retardant and functional materials: aluminum hydroxide, silica powder, alumina and silane coupling agents.', '/en/products', 'PUBLISHED')
ON DUPLICATE KEY UPDATE
title = VALUES(title),
description = VALUES(description),
og_title = VALUES(og_title),
og_description = VALUES(og_description),
canonical = VALUES(canonical),
publish_status = VALUES(publish_status);

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM application_series_link;
DELETE FROM seo_meta;
DELETE FROM page_content;
DELETE FROM news_article;
DELETE FROM news_category;
DELETE FROM product_parameter;
DELETE FROM product_image;
DELETE FROM product_series_image;
DELETE FROM media_asset;
DELETE FROM product;
DELETE FROM product_series;
DELETE FROM product_category;
DELETE FROM application_field;

INSERT INTO product_category (id, slug, name_zh, name_en, summary_zh, summary_en, sort_order, publish_status) VALUES
(1, 'aluminum-hydroxide', '氢氧化铝', 'Aluminum Hydroxide', '环保型功能性无机填料与阻燃原料。', 'Eco-friendly functional inorganic filler and flame retardant.', 10, 'PUBLISHED'),
(2, 'silica-powder-series', '硅粉系列', 'Silica Powder Series', '高纯结晶硅粉、熔融硅粉、球形硅粉与石英砂。', 'High-purity crystalline/fused/spherical silica powders and quartz sand.', 20, 'PUBLISHED'),
(3, 'alumina-powder', '氧化铝粉末', 'Alumina Powder', '高纯氧化铝粉末，导热与绝缘并重。', 'High-purity alumina powder balancing thermal conductivity and insulation.', 30, 'PUBLISHED'),
(4, 'silane-coupling-agent', '硅烷偶联剂', 'Silane Coupling Agent', '面向无机粉体改性的功能助剂系列。', 'Functional coupling agents for inorganic powder modification.', 40, 'PUBLISHED');

INSERT INTO product_series (id, category_id, slug, name_zh, name_en, summary_zh, summary_en, sort_order, publish_status) VALUES
(11, 1, 'qd-f-series', 'QD-F 系列', 'QD-F Series', 'ATH 标准系列，覆盖不同粒径规格。', 'Standard ATH series covering multiple particle sizes.', 10, 'PUBLISHED'),
(21, 2, 'fused-silica-series', '熔融硅粉系列', 'Fused Silica Series', '低膨胀、低介电损耗，适配 CCL 与电子封装。', 'Low expansion and dielectric loss for CCL and packaging.', 10, 'PUBLISHED'),
(31, 3, 'qd-ql-series', 'QD-QL 系列', 'QD-QL Series', '超细高纯氧化铝系列，适配导热与电子陶瓷。', 'Ultra-fine high-purity alumina for TIM and electronic ceramics.', 10, 'PUBLISHED'),
(41, 4, 'amino-silane-series', '氨基硅烷系列', 'Amino Silane Series', '提升无机粉体与树脂界面结合力。', 'Enhance interfacial bonding between fillers and resin.', 10, 'PUBLISHED');

INSERT INTO product (id, category_id, series_id, slug, name_zh, name_en, model, summary_zh, summary_en, detail_zh, detail_en, packaging_zh, packaging_en, sort_order, publish_status) VALUES
(101, 1, 11, 'qd-f02', 'QD-F02', 'QD-F02', 'QD-F02', '白度高、分散稳定，适配无卤阻燃体系。', 'High whiteness and stable dispersion for halogen-free systems.', '用于覆铜板、线缆及阻燃复合材料。', 'For CCL, cable and flame-retardant composites.', '25kg 袋装', '25kg bag', 10, 'PUBLISHED'),
(201, 2, 21, 'fs-3000', 'FS-3000', 'FS-3000', 'FS-3000', '熔融硅粉，低热膨胀，绝缘稳定。', 'Fused silica with low thermal expansion and stable insulation.', '适用于覆铜板、电子绝缘材料。', 'Suitable for CCL and electronic insulation materials.', '25kg 袋装', '25kg bag', 10, 'PUBLISHED'),
(301, 3, 31, 'qd-ql01', 'QD-QL01', 'QD-QL01', 'QD-QL01', '高纯超细氧化铝粉，导热绝缘平衡。', 'High-purity ultra-fine alumina balancing thermal and insulation.', '用于导热界面材料与电子陶瓷。', 'For thermal interface materials and electronic ceramics.', '25kg 袋装', '25kg bag', 10, 'PUBLISHED'),
(401, 4, 41, 'am-1100', 'AM-1100', 'AM-1100', 'AM-1100', '氨基硅烷偶联剂，提升界面相容性。', 'Amino silane coupling agent improving interfacial compatibility.', '用于 ATH、硅粉、氧化铝粉体改性。', 'For ATH, silica and alumina filler modification.', '5kg / 25kg 桶装', '5kg / 25kg drum', 10, 'PUBLISHED');

INSERT INTO media_asset (id, media_type, file_name, file_ext, mime_type, storage_url, crop_mode, crop_ratio, crop_focus) VALUES
(1001, 'IMAGE', 'aluminum-hydroxide.jpg', 'jpg', 'image/jpeg', '/products/aluminum-hydroxide.jpg', 'AUTO', '4:3', 'CENTER'),
(1002, 'IMAGE', 'silica-powder-series.jpg', 'jpg', 'image/jpeg', '/products/silica-powder-series.jpg', 'AUTO', '4:3', 'CENTER'),
(1003, 'IMAGE', 'alumina-powder.jpg', 'jpg', 'image/jpeg', '/products/alumina-powder.jpg', 'AUTO', '4:3', 'CENTER'),
(1004, 'IMAGE', 'silane-coupling-agent.jpg', 'jpg', 'image/jpeg', '/products/silane-coupling-agent.jpg', 'AUTO', '4:3', 'CENTER'),
(1101, 'IMAGE', 'aluminum-hydroxide.jpg', 'jpg', 'image/jpeg', '/products/aluminum-hydroxide.jpg', 'AUTO', '4:3', 'CENTER'),
(1102, 'IMAGE', 'silica-powder-series.jpg', 'jpg', 'image/jpeg', '/products/silica-powder-series.jpg', 'AUTO', '4:3', 'CENTER'),
(1103, 'IMAGE', 'alumina-powder.jpg', 'jpg', 'image/jpeg', '/products/alumina-powder.jpg', 'AUTO', '4:3', 'CENTER'),
(1104, 'IMAGE', 'silane-coupling-agent.jpg', 'jpg', 'image/jpeg', '/products/silane-coupling-agent.jpg', 'AUTO', '4:3', 'CENTER');

INSERT INTO product_series_image (series_id, media_id, alt_zh, alt_en, publish_status) VALUES
(11, 1001, 'QD-F 系列图片', 'QD-F series image', 'PUBLISHED'),
(21, 1002, '熔融硅粉系列图片', 'Fused silica series image', 'PUBLISHED'),
(31, 1003, 'QD-QL 系列图片', 'QD-QL series image', 'PUBLISHED'),
(41, 1004, '氨基硅烷系列图片', 'Amino silane series image', 'PUBLISHED');

INSERT INTO product_image (product_id, media_id, alt_zh, alt_en, publish_status) VALUES
(101, 1101, 'QD-F02 产品图', 'QD-F02 product image', 'PUBLISHED'),
(201, 1102, 'FS-3000 产品图', 'FS-3000 product image', 'PUBLISHED'),
(301, 1103, 'QD-QL01 产品图', 'QD-QL01 product image', 'PUBLISHED'),
(401, 1104, 'AM-1100 产品图', 'AM-1100 product image', 'PUBLISHED');

INSERT INTO product_parameter (product_id, param_key, param_name_zh, param_name_en, param_value_raw, unit, test_method, sort_order, publish_status) VALUES
(101, 'whiteness', '白度', 'Whiteness', '98', NULL, NULL, 10, 'PUBLISHED'),
(101, 'd50', '中位粒径', 'D50', '2', 'um', NULL, 20, 'PUBLISHED'),
(101, 'moisture', '附着水', 'Moisture', '0.15', '%', NULL, 30, 'PUBLISHED'),
(201, 'purity', '纯度', 'Purity', '99.9', '%', NULL, 10, 'PUBLISHED'),
(201, 'dielectric_loss', '介电损耗', 'Dielectric Loss', '低', NULL, NULL, 20, 'PUBLISHED'),
(301, 'al2o3', 'Al2O3', 'Al2O3', '99.6', '%', NULL, 10, 'PUBLISHED'),
(301, 'thermal', '导热性能', 'Thermal Conductivity', '优', NULL, NULL, 20, 'PUBLISHED'),
(401, 'type', '类型', 'Type', '氨基硅烷', NULL, NULL, 10, 'PUBLISHED'),
(401, 'feature', '核心特性', 'Core Feature', '增强界面结合', NULL, NULL, 20, 'PUBLISHED');

INSERT INTO application_field (id, slug, name_zh, name_en, overview_zh, overview_en, publish_status, sort_order) VALUES
(201, 'halogen-free-flame-retardancy', '无卤阻燃材料', 'Halogen-Free Flame Retardant Materials', '面向电线电缆、阻燃橡塑、树脂及复合材料体系，关注阻燃、抑烟、分散性与加工稳定性。', 'For wire and cable, flame-retardant rubber, plastics, resin and composite systems, focusing on flame retardancy, smoke suppression, dispersion and processing stability.', 'PUBLISHED', 10),
(202, 'copper-clad-laminate', '覆铜板与电子绝缘材料', 'Copper Clad Laminate & Electronic Insulation', '面向覆铜板、绝缘板材、环氧浇注与电子封装材料，关注低杂质、低介电、绝缘和尺寸稳定性。', 'For copper clad laminates, insulating boards, epoxy casting and electronic packaging materials, focusing on low impurities, low dielectric loss, insulation and dimensional stability.', 'PUBLISHED', 20),
(203, 'wire-cable', '电线电缆', 'Wire & Cable', '面向电缆料、线缆护套、光伏线缆及轨道交通线缆，关注无卤阻燃、低烟、电气绝缘和机械性能平衡。', 'For cable compounds, wire and cable sheaths, photovoltaic cables and rail-transit cables, balancing halogen-free flame retardancy, smoke suppression, electrical insulation and mechanical performance.', 'PUBLISHED', 30),
(204, 'thermal-management-new-energy', '导热材料与新能源', 'Thermal Management & New Energy', '面向电池散热、导热界面材料、导热塑料与电子热管理组件，关注导热、绝缘、耐温和填充效率。', 'For battery heat dissipation, thermal interface materials, thermal conductive plastics and electronic thermal-management components, focusing on thermal conductivity, insulation, heat resistance and filling efficiency.', 'PUBLISHED', 40),
(205, 'semiconductor-electronic-packaging', '半导体与电子封装', 'Semiconductor & Electronic Packaging', '面向 EMC 封装、电子元器件和精密电子材料，关注超低杂质、绝缘、低介电损耗和长期可靠性。', 'For EMC packaging, electronic components and precision electronic materials, focusing on ultra-low impurities, insulation, low dielectric loss and long-term reliability.', 'PUBLISHED', 50),
(206, 'electronic-ceramics', '电子陶瓷与精密电子', 'Electronic Ceramics & Precision Electronics', '面向陶瓷基板、绝缘陶瓷元器件及高精度电子制造，关注致密度、绝缘、耐压和尺寸稳定性。', 'For ceramic substrates, insulating ceramic components and high-precision electronics manufacturing, focusing on density, insulation, voltage resistance and dimensional stability.', 'PUBLISHED', 60),
(207, 'optical-materials', '光学材料与光纤', 'Optical Materials & Optical Fiber', '面向光学玻璃、微晶玻璃、光纤辅助材料和精密抛光，关注高纯度、透光性、表面质量和稳定性。', 'For optical glass, microcrystalline glass, optical-fiber auxiliary materials and precision polishing, focusing on high purity, light transmittance, surface quality and stability.', 'PUBLISHED', 70),
(208, 'ceramics-glass', '陶瓷与玻璃工业', 'Ceramics & Glass Industry', '面向工业陶瓷、电子陶瓷、玻璃和石英制品，关注白度、烧结性能、耐热冲击和理化稳定性。', 'For industrial ceramics, electronic ceramics, glass and quartz products, focusing on whiteness, sintering performance, thermal-shock resistance and chemical stability.', 'PUBLISHED', 80),
(209, 'refractory-high-temperature', '耐火材料与高温工业', 'Refractory Materials & High-Temperature Industry', '面向耐火浇注料、耐火砖、窑炉内衬及高温隔热材料，关注耐高温、抗热震、低收缩和强度。', 'For refractory castables, refractory bricks, furnace linings and high-temperature insulation, focusing on high-temperature resistance, thermal-shock resistance, low shrinkage and strength.', 'PUBLISHED', 90),
(210, 'rubber-plastics', '橡胶、塑料与复合材料', 'Rubber, Plastics & Composites', '面向工程塑料、硅橡胶、EVA、PVC、TPU、复合材料与人造石，关注相容性、补强、耐磨和加工性。', 'For engineering plastics, silicone rubber, EVA, PVC, TPU, composites and artificial stone, focusing on compatibility, reinforcement, wear resistance and processability.', 'PUBLISHED', 100),
(211, 'coatings-inks-adhesives', '涂料、油墨与胶粘剂', 'Coatings, Inks & Adhesives', '面向工业涂料、水性油墨、环氧胶、密封胶和地坪体系，关注分散、防沉、附着、耐候和耐腐蚀性能。', 'For industrial coatings, water-based inks, epoxy adhesives, sealants and flooring systems, focusing on dispersion, anti-settling, adhesion, weather resistance and corrosion resistance.', 'PUBLISHED', 110),
(212, 'industrial-paper-environmental', '工业用纸与环保处理', 'Industrial Paper & Environmental Treatment', '面向绝缘纸、阻燃纸、装饰原纸及工业废水和烟气辅助处理，关注白度、阻燃、吸附和环境适配性。', 'For insulating paper, flame-retardant paper, decorative base paper and industrial wastewater or flue-gas auxiliary treatment, focusing on whiteness, flame retardancy, adsorption and environmental compatibility.', 'PUBLISHED', 120),
(213, 'fine-chemicals-functional-materials', '精细化工与功能新材料', 'Fine Chemicals & Functional Materials', '面向催化剂载体、吸附填料、防腐复合材料与功能性填充体系，关注纯度、稳定性、相容性和可定制性。', 'For catalyst carriers, adsorbent fillers, anti-corrosion composites and functional filling systems, focusing on purity, stability, compatibility and customization.', 'PUBLISHED', 130);

INSERT INTO application_series_link (application_id, series_id, sort_order, publish_status) VALUES
(201, 11, 10, 'PUBLISHED'), (201, 41, 20, 'PUBLISHED'),
(202, 11, 10, 'PUBLISHED'), (202, 21, 20, 'PUBLISHED'), (202, 31, 30, 'PUBLISHED'), (202, 41, 40, 'PUBLISHED'),
(203, 11, 10, 'PUBLISHED'), (203, 41, 20, 'PUBLISHED'),
(204, 21, 10, 'PUBLISHED'), (204, 31, 20, 'PUBLISHED'), (204, 41, 30, 'PUBLISHED'),
(205, 21, 10, 'PUBLISHED'), (205, 31, 20, 'PUBLISHED'), (205, 41, 30, 'PUBLISHED'),
(206, 31, 10, 'PUBLISHED'), (206, 21, 20, 'PUBLISHED'),
(207, 21, 10, 'PUBLISHED'), (207, 31, 20, 'PUBLISHED'),
(208, 11, 10, 'PUBLISHED'), (208, 21, 20, 'PUBLISHED'), (208, 31, 30, 'PUBLISHED'), (208, 41, 40, 'PUBLISHED'),
(209, 11, 10, 'PUBLISHED'), (209, 21, 20, 'PUBLISHED'), (209, 31, 30, 'PUBLISHED'),
(210, 11, 10, 'PUBLISHED'), (210, 21, 20, 'PUBLISHED'), (210, 31, 30, 'PUBLISHED'), (210, 41, 40, 'PUBLISHED'),
(211, 11, 10, 'PUBLISHED'), (211, 21, 20, 'PUBLISHED'), (211, 31, 30, 'PUBLISHED'), (211, 41, 40, 'PUBLISHED'),
(212, 11, 10, 'PUBLISHED'),
(213, 11, 10, 'PUBLISHED'), (213, 21, 20, 'PUBLISHED'), (213, 31, 30, 'PUBLISHED'), (213, 41, 40, 'PUBLISHED');

INSERT INTO news_category (id, slug, name_zh, name_en, publish_status, sort_order) VALUES
(301, 'industry-trends', '行业趋势', 'Industry Trends', 'PUBLISHED', 10),
(302, 'events', '事件', 'Events', 'PUBLISHED', 20);

INSERT INTO news_article (id, category_id, slug, title_zh, title_en, summary_zh, summary_en, content_zh, content_en, cover_image_url, cover_alt_zh, cover_alt_en, published_at, publish_status) VALUES
(401, 301, 'ath-flame-retardant-advantages', '氢氧化铝阻燃应用的核心优势', 'Core Advantages of ATH in Flame Retardancy', '为什么 ATH 仍是无卤阻燃体系关键填料。', 'Why ATH remains a key filler in halogen-free systems.',
 '本文介绍 ATH 在阻燃体系中的关键优势，包括吸热分解、抑烟与成本平衡。',
 'This article introduces ATH advantages in flame-retardant systems, including endothermic decomposition, smoke suppression, and cost balance.',
 '/news/reference-news-article-cover.webp', '资讯测试封面图', 'Temporary news article cover',
 '2026-06-18 10:00:00', 'PUBLISHED');

INSERT INTO seo_meta (entity_type, entity_id, lang, page_key, title, description, canonical, publish_status) VALUES
('PAGE', NULL, 'zh', 'home', '先进材料解决方案 | 起点化工', '面向 B2B 客户提供 ATH、硅粉、氧化铝与硅烷偶联剂材料方案。', '/zh/', 'PUBLISHED'),
('PAGE', NULL, 'en', 'home', 'Advanced Material Solutions | Qidian Chemical', 'B2B material supplier for ATH, silica powder, alumina and silane agents.', '/en/', 'PUBLISHED'),
('PAGE', NULL, 'zh', 'products', '产品中心 | 起点化工', '阻燃与功能材料产品矩阵。', '/zh/products', 'PUBLISHED'),
('PAGE', NULL, 'en', 'products', 'Product Center | Qidian Chemical', 'Product matrix for flame-retardant and functional materials.', '/en/products', 'PUBLISHED');

INSERT INTO page_content (page_key, lang, payload_json, publish_status) VALUES
('nav', 'zh', '[{"key":"home","label":"首页","path":"/zh"},{"key":"about","label":"关于我们","path":"/zh/about"},{"key":"products","label":"产品中心","path":"/zh/products"},{"key":"applications","label":"应用领域","path":"/zh/applications"},{"key":"news","label":"资讯中心","path":"/zh/news"},{"key":"contact","label":"联系我们","path":"/zh/contact"}]', 'PUBLISHED'),
('nav', 'en', '[{"key":"home","label":"Home","path":"/en"},{"key":"about","label":"About","path":"/en/about"},{"key":"products","label":"Products","path":"/en/products"},{"key":"applications","label":"Applications","path":"/en/applications"},{"key":"news","label":"News","path":"/en/news"},{"key":"contact","label":"Contact","path":"/en/contact"}]', 'PUBLISHED'),
('home', 'zh', '{"heroTitle":"先进材料解决方案","heroSubtitle":"专注阻燃与功能性无机新材料，为全球 B2B 客户提供稳定供货与技术支持。","ctaText":"进入产品中心","ctaLink":"/zh/products","moduleOrder":["Hero Banner","公司定位","产品分类","应用入口","信任模块","交付能力","资讯入口","CTA 询盘","多触点联系","页脚导航"]}', 'PUBLISHED'),
('home', 'en', '{"heroTitle":"Advanced Material Solutions","heroSubtitle":"Focused on flame-retardant and functional inorganic materials for global B2B customers.","ctaText":"Explore Products","ctaLink":"/en/products","moduleOrder":["Hero Banner","Company Positioning","Product Categories","Application Entry","Trust Module","Delivery Capability","News Entry","CTA Inquiry","Contact Points","Footer Navigation"]}', 'PUBLISHED');

SET FOREIGN_KEY_CHECKS = 1;

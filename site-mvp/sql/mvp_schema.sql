-- MVP schema for independent station (MySQL 8)
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS product_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slug VARCHAR(128) NOT NULL UNIQUE,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  chemical_formula VARCHAR(128) NULL,
  summary_zh VARCHAR(1000) NULL,
  summary_en VARCHAR(1000) NULL,
  application_zh TEXT NULL,
  application_en TEXT NULL,
  seo_title VARCHAR(255) NULL,
  seo_description VARCHAR(1000) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_series (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  slug VARCHAR(128) NOT NULL,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  summary_zh VARCHAR(1000) NULL,
  summary_en VARCHAR(1000) NULL,
  application_zh TEXT NULL,
  application_en TEXT NULL,
  seo_title VARCHAR(255) NULL,
  seo_description VARCHAR(1000) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_series_category_slug (category_id, slug),
  CONSTRAINT fk_series_category FOREIGN KEY (category_id) REFERENCES product_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  series_id BIGINT NOT NULL,
  slug VARCHAR(128) NOT NULL,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  model VARCHAR(128) NOT NULL,
  summary_zh VARCHAR(1000) NULL,
  summary_en VARCHAR(1000) NULL,
  detail_zh TEXT NULL,
  detail_en TEXT NULL,
  application_scenario_zh TEXT NULL,
  application_scenario_en TEXT NULL,
  seo_title VARCHAR(255) NULL,
  seo_description VARCHAR(1000) NULL,
  parameters_json JSON NULL,
  packaging_zh VARCHAR(500) NULL,
  packaging_en VARCHAR(500) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_series_slug (series_id, slug),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(id),
  CONSTRAINT fk_product_series FOREIGN KEY (series_id) REFERENCES product_series(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS media_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  media_type VARCHAR(32) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_ext VARCHAR(32) NULL,
  mime_type VARCHAR(128) NULL,
  file_size BIGINT NULL,
  storage_url VARCHAR(500) NOT NULL,
  width INT NULL,
  height INT NULL,
  crop_mode VARCHAR(32) NOT NULL DEFAULT 'AUTO',
  crop_ratio VARCHAR(32) NULL,
  crop_focus VARCHAR(32) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_media_type (media_type),
  KEY idx_media_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_series_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  series_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL,
  alt_zh VARCHAR(255) NULL,
  alt_en VARCHAR(255) NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_series_image_series (series_id),
  KEY idx_series_image_media (media_id),
  CONSTRAINT fk_series_image_series FOREIGN KEY (series_id) REFERENCES product_series(id),
  CONSTRAINT fk_series_image_media FOREIGN KEY (media_id) REFERENCES media_asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL,
  alt_zh VARCHAR(255) NULL,
  alt_en VARCHAR(255) NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_image_product (product_id),
  KEY idx_product_image_media (media_id),
  CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_product_image_media FOREIGN KEY (media_id) REFERENCES media_asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_parameter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  param_key VARCHAR(128) NOT NULL,
  param_name_zh VARCHAR(255) NOT NULL,
  param_name_en VARCHAR(255) NOT NULL,
  param_value_raw VARCHAR(255) NOT NULL,
  unit VARCHAR(64) NULL,
  test_method VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_param_key (product_id, param_key),
  KEY idx_product_param_sort (product_id, sort_order),
  CONSTRAINT fk_product_parameter_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_recommend_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_type VARCHAR(32) NOT NULL,
  source_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_recommend_unique (source_type, source_id, target_type, target_id),
  KEY idx_recommend_source (source_type, source_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS content_translation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL,
  entity_id BIGINT NOT NULL,
  field_name VARCHAR(128) NOT NULL,
  source_lang VARCHAR(8) NOT NULL DEFAULT 'zh',
  target_lang VARCHAR(8) NOT NULL DEFAULT 'en',
  source_type VARCHAR(32) NOT NULL,
  translated_text LONGTEXT NOT NULL,
  review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  reviewer VARCHAR(128) NULL,
  reviewed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_translation_entity (entity_type, entity_id, field_name),
  KEY idx_translation_review (review_status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS application_field (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slug VARCHAR(128) NOT NULL UNIQUE,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  overview_zh TEXT NULL,
  overview_en TEXT NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  sort_order INT NOT NULL DEFAULT 0,
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_application_field_status_sort (publish_status, deleted_at, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS application_series_link (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  series_id BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_app_series (application_id, series_id),
  KEY idx_application_series_link_application_sort (application_id, publish_status, sort_order),
  CONSTRAINT fk_app_series_application FOREIGN KEY (application_id) REFERENCES application_field(id),
  CONSTRAINT fk_app_series_series FOREIGN KEY (series_id) REFERENCES product_series(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS news_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slug VARCHAR(128) NOT NULL UNIQUE,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  KEY idx_news_category_status_sort (publish_status, sort_order),
  KEY idx_news_category_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS news_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  slug VARCHAR(128) NOT NULL UNIQUE,
  title_zh VARCHAR(255) NOT NULL,
  title_en VARCHAR(255) NOT NULL,
  summary_zh VARCHAR(1000) NULL,
  summary_en VARCHAR(1000) NULL,
  content_zh LONGTEXT NULL,
  content_en LONGTEXT NULL,
  cover_image_url VARCHAR(500) NULL,
  cover_alt_zh VARCHAR(500) NULL,
  cover_alt_en VARCHAR(500) NULL,
  seo_title VARCHAR(255) NULL,
  seo_description VARCHAR(1000) NULL,
  published_at DATETIME NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  KEY idx_news_article_status_date (publish_status, published_at),
  KEY idx_news_article_category_status (category_id, publish_status),
  KEY idx_news_article_deleted (deleted_at),
  CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES news_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS news_article_product (
  article_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (article_id, product_id),
  KEY idx_news_article_product_sort (article_id, sort_order),
  CONSTRAINT fk_news_article_product_article FOREIGN KEY (article_id) REFERENCES news_article(id),
  CONSTRAINT fk_news_article_product_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS news_article_related (
  article_id BIGINT NOT NULL,
  related_article_id BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  PRIMARY KEY (article_id, related_article_id),
  KEY idx_news_article_related_sort (article_id, sort_order),
  CONSTRAINT fk_news_article_related_article FOREIGN KEY (article_id) REFERENCES news_article(id),
  CONSTRAINT fk_news_article_related_target FOREIGN KEY (related_article_id) REFERENCES news_article(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inquiry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inquiry_no VARCHAR(64) NOT NULL UNIQUE,
  lang VARCHAR(8) NOT NULL,
  name VARCHAR(255) NOT NULL,
  company VARCHAR(255) NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(64) NULL,
  country VARCHAR(128) NULL,
  interested_product VARCHAR(255) NULL,
  message TEXT NOT NULL,
  source_page VARCHAR(500) NULL,
  inquiry_status VARCHAR(32) NOT NULL DEFAULT 'NEW',
  internal_note TEXT NULL,
  first_contacted_at DATETIME NULL,
  lead_score INT NOT NULL DEFAULT 0,
  lead_level VARCHAR(16) NOT NULL DEFAULT 'LOW',
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inquiry_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inquiry_id BIGINT NOT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NOT NULL,
  operator_username VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_inquiry_history_inquiry (inquiry_id, created_at),
  CONSTRAINT fk_inquiry_history_inquiry FOREIGN KEY (inquiry_id) REFERENCES inquiry(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analytics_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id VARCHAR(64) NOT NULL UNIQUE,
  event_name VARCHAR(64) NOT NULL,
  occurred_at DATETIME(3) NOT NULL,
  visitor_id VARCHAR(64) NOT NULL,
  session_id VARCHAR(64) NOT NULL,
  page_key VARCHAR(255) NOT NULL,
  page_path VARCHAR(500) NOT NULL,
  page_type VARCHAR(64) NULL,
  referrer VARCHAR(500) NULL,
  utm_source VARCHAR(128) NULL,
  utm_medium VARCHAR(128) NULL,
  utm_campaign VARCHAR(255) NULL,
  source_channel VARCHAR(32) NOT NULL,
  lang VARCHAR(8) NULL,
  country VARCHAR(128) NULL,
  category_id BIGINT NULL,
  series_id BIGINT NULL,
  product_id BIGINT NULL,
  payload_json JSON NULL,
  user_agent VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_analytics_event_time_name (occurred_at, event_name),
  KEY idx_analytics_visitor_session (visitor_id, session_id),
  KEY idx_analytics_page_time (page_key, occurred_at),
  KEY idx_analytics_channel_time (source_channel, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analytics_daily_aggregate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stat_date DATE NOT NULL,
  dimension_type VARCHAR(32) NOT NULL,
  dimension_key VARCHAR(255) NOT NULL,
  lang VARCHAR(8) NOT NULL DEFAULT '',
  source_channel VARCHAR(32) NOT NULL DEFAULT '',
  metric_name VARCHAR(64) NOT NULL,
  metric_value DECIMAL(20,4) NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_analytics_daily (stat_date, dimension_type, dimension_key, lang, source_channel, metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS certificate (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  certificate_no VARCHAR(128) NOT NULL,
  name_zh VARCHAR(255) NOT NULL,
  name_en VARCHAR(255) NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  alt_zh VARCHAR(500) NULL,
  alt_en VARCHAR(500) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_certificate_no (certificate_no),
  KEY idx_certificate_status_sort (publish_status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seo_meta (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(64) NOT NULL,
  entity_id BIGINT NULL,
  lang VARCHAR(8) NOT NULL,
  page_key VARCHAR(128) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(1000) NULL,
  og_title VARCHAR(255) NULL,
  og_description VARCHAR(1000) NULL,
  canonical VARCHAR(500) NULL,
  og_image VARCHAR(500) NULL,
  robots VARCHAR(255) NULL,
  schema_json JSON NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_seo_page_lang (page_key, lang),
  KEY idx_seo_entity (entity_type, entity_id, lang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS page_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  page_key VARCHAR(128) NOT NULL,
  lang VARCHAR(8) NOT NULL,
  payload_json JSON NOT NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_page_content_key_lang (page_key, lang)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

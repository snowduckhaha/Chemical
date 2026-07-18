-- Product Center schema migration v2 (MySQL 8)
-- Goal:
-- 1) product 1:1 product_image
-- 2) product_series 1:1 product_series_image
-- 3) remove product_document / FAQ scope if legacy tables exist
-- 4) add structured parameter table and translation review log

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------------------
-- 0. Optional cleanup for legacy scope (safe drops)
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS product_faq_relation;
DROP TABLE IF EXISTS faq;
DROP TABLE IF EXISTS product_document;

-- --------------------------------------------------------------------
-- 1. Media asset table (image only for product center)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS media_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  media_type VARCHAR(32) NOT NULL COMMENT 'IMAGE',
  file_name VARCHAR(255) NOT NULL,
  file_ext VARCHAR(32) NULL,
  mime_type VARCHAR(128) NULL,
  file_size BIGINT NULL,
  storage_url VARCHAR(500) NOT NULL,
  width INT NULL,
  height INT NULL,
  crop_mode VARCHAR(32) NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO|MANUAL|NONE',
  crop_ratio VARCHAR(32) NULL COMMENT 'e.g. 4:3,16:9; follow target-site ratio strategy',
  crop_focus VARCHAR(32) NULL COMMENT 'CENTER|TOP|LEFT|RIGHT|BOTTOM',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_media_type (media_type),
  KEY idx_media_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------------------
-- 2. Series image (1:1)
-- --------------------------------------------------------------------
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

-- --------------------------------------------------------------------
-- 3. Product image (1:1)
-- --------------------------------------------------------------------
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

-- --------------------------------------------------------------------
-- 4. Structured parameters (keep source precision in raw text)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_parameter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  param_key VARCHAR(128) NOT NULL,
  param_name_zh VARCHAR(255) NOT NULL,
  param_name_en VARCHAR(255) NOT NULL,
  param_value_raw VARCHAR(255) NOT NULL COMMENT 'store source value as-is',
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

-- --------------------------------------------------------------------
-- 5. Translation review log (file/machine/manual + human review)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS content_translation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(32) NOT NULL COMMENT 'CATEGORY|SERIES|PRODUCT',
  entity_id BIGINT NOT NULL,
  field_name VARCHAR(128) NOT NULL,
  source_lang VARCHAR(8) NOT NULL DEFAULT 'zh',
  target_lang VARCHAR(8) NOT NULL DEFAULT 'en',
  source_type VARCHAR(32) NOT NULL COMMENT 'FILE|MACHINE|MANUAL',
  translated_text LONGTEXT NOT NULL,
  review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|REVIEWED|APPROVED|REJECTED',
  reviewer VARCHAR(128) NULL,
  reviewed_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_translation_entity (entity_type, entity_id, field_name),
  KEY idx_translation_review (review_status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------------------
-- 6. Enhance existing SEO index if missing
-- --------------------------------------------------------------------
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'seo_meta'
    AND index_name = 'idx_seo_entity'
);
SET @sql_idx := IF(
  @idx_exists = 0,
  'ALTER TABLE seo_meta ADD INDEX idx_seo_entity (entity_type, entity_id, lang)',
  'SELECT 1'
);
PREPARE stmt_idx FROM @sql_idx;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

-- --------------------------------------------------------------------
-- 7. (Optional) compatibility fields on product_series/product for sort
--    Existing schema already contains sort_order. Keep as no-op reminder.
-- --------------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 1;

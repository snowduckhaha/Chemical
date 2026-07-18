-- P0 uploads: complete-image (CONTAIN) display canvases and category image relation.

CREATE TABLE IF NOT EXISTS product_category_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL,
  alt_zh VARCHAR(255) NULL,
  alt_en VARCHAR(255) NULL,
  publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_category_image_category (category_id),
  KEY idx_category_image_media (media_id),
  CONSTRAINT fk_category_image_category FOREIGN KEY (category_id) REFERENCES product_category(id),
  CONSTRAINT fk_category_image_media FOREIGN KEY (media_id) REFERENCES media_asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'media_asset' AND column_name = 'source_url');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE media_asset ADD COLUMN source_url VARCHAR(500) NULL AFTER storage_url', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'media_asset' AND column_name = 'display_mode');
SET @sql := IF(@column_exists = 0, "ALTER TABLE media_asset ADD COLUMN display_mode VARCHAR(32) NOT NULL DEFAULT 'CONTAIN' AFTER crop_focus", 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'media_asset' AND column_name = 'display_canvas_ratio');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE media_asset ADD COLUMN display_canvas_ratio VARCHAR(32) NULL AFTER display_mode', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'media_asset' AND column_name = 'canvas_background');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE media_asset ADD COLUMN canvas_background VARCHAR(32) NULL AFTER display_canvas_ratio', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
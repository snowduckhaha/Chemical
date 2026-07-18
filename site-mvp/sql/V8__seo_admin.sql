-- Complete page-level SEO fields required by the administration system.

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'seo_meta' AND column_name = 'og_title');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE seo_meta ADD COLUMN og_title VARCHAR(255) NULL AFTER description', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'seo_meta' AND column_name = 'og_description');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE seo_meta ADD COLUMN og_description VARCHAR(1000) NULL AFTER og_title', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Soft deletion for the product hierarchy.

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product_category' AND column_name = 'deleted_at');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE product_category ADD COLUMN deleted_at DATETIME NULL AFTER publish_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product_series' AND column_name = 'deleted_at');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE product_series ADD COLUMN deleted_at DATETIME NULL AFTER publish_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product' AND column_name = 'deleted_at');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE product ADD COLUMN deleted_at DATETIME NULL AFTER publish_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

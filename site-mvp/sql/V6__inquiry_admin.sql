-- Inquiry follow-up fields and status audit history.

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'inquiry' AND column_name = 'internal_note');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE inquiry ADD COLUMN internal_note TEXT NULL AFTER inquiry_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'inquiry' AND column_name = 'first_contacted_at');
SET @sql := IF(@column_exists = 0, 'ALTER TABLE inquiry ADD COLUMN first_contacted_at DATETIME NULL AFTER internal_note', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

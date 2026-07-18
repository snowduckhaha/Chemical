-- Certificate content managed by the administration system.

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

INSERT INTO certificate
(certificate_no, name_zh, name_en, image_url, alt_zh, alt_en, sort_order, publish_status)
VALUES
('CERT-ISO9001-2024', CONVERT(0x49534F203930303120E8B4A8E9878FE7AEA1E79086E4BD93E7B3BBE8AEA4E8AF81 USING utf8mb4), 'ISO 9001 Quality Management System', '/about/iso9001.png', CONVERT(0x49534F203930303120E8B4A8E9878FE7AEA1E79086E4BD93E7B3BBE8AF81E4B9A6 USING utf8mb4), 'ISO 9001 quality management system certificate', 10, 'PUBLISHED'),
('CERT-ISO14001-2024', CONVERT(0x49534F20313430303120E78EAFE5A283E7AEA1E79086E4BD93E7B3BBE8AEA4E8AF81 USING utf8mb4), 'ISO 14001 Environmental Management System', '/about/iso14001.png', CONVERT(0x49534F20313430303120E78EAFE5A283E7AEA1E79086E4BD93E7B3BBE8AF81E4B9A6 USING utf8mb4), 'ISO 14001 environmental management system certificate', 20, 'PUBLISHED')
ON DUPLICATE KEY UPDATE
  name_zh = VALUES(name_zh), name_en = VALUES(name_en), image_url = VALUES(image_url),
  alt_zh = VALUES(alt_zh), alt_en = VALUES(alt_en);

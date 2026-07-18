-- P1 news administration fields and explicit content relations. Repeatable on existing databases.
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_category' AND column_name='deleted_at')=0, 'ALTER TABLE news_category ADD COLUMN deleted_at DATETIME NULL AFTER updated_at, ADD KEY idx_news_category_status_sort (publish_status, sort_order), ADD KEY idx_news_category_deleted (deleted_at)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='cover_image_url')=0, 'ALTER TABLE news_article ADD COLUMN cover_image_url VARCHAR(500) NULL AFTER content_en', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='cover_alt_zh')=0, 'ALTER TABLE news_article ADD COLUMN cover_alt_zh VARCHAR(500) NULL AFTER cover_image_url', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='cover_alt_en')=0, 'ALTER TABLE news_article ADD COLUMN cover_alt_en VARCHAR(500) NULL AFTER cover_alt_zh', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='seo_title')=0, 'ALTER TABLE news_article ADD COLUMN seo_title VARCHAR(255) NULL AFTER cover_alt_en', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='seo_description')=0, 'ALTER TABLE news_article ADD COLUMN seo_description VARCHAR(1000) NULL AFTER seo_title', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='sort_order')=0, 'ALTER TABLE news_article ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER published_at', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='news_article' AND column_name='deleted_at')=0, 'ALTER TABLE news_article ADD COLUMN deleted_at DATETIME NULL AFTER updated_at, ADD KEY idx_news_article_status_date (publish_status, published_at), ADD KEY idx_news_article_category_status (category_id, publish_status), ADD KEY idx_news_article_deleted (deleted_at)', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

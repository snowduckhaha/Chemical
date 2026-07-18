-- Ensure the initial news taxonomy is available in every environment. This migration is repeatable.
-- Preserve existing seed articles by renaming the historical category before inserting the new baseline.
UPDATE news_category
SET slug = 'industry-trends', name_zh = CONVERT(UNHEX('E8A18CE4B89AE8B68BE58ABF') USING utf8mb4), name_en = 'Industry Trends', publish_status = 'PUBLISHED', sort_order = 10, deleted_at = NULL
WHERE slug = 'industry-insight' AND NOT EXISTS (SELECT 1 FROM (SELECT id FROM news_category WHERE slug = 'industry-trends') AS existing_category);

INSERT INTO news_category (slug, name_zh, name_en, publish_status, sort_order)
VALUES ('industry-trends', CONVERT(UNHEX('E8A18CE4B89AE8B68BE58ABF') USING utf8mb4), 'Industry Trends', 'PUBLISHED', 10)
ON DUPLICATE KEY UPDATE
  name_zh = VALUES(name_zh), name_en = VALUES(name_en), publish_status = VALUES(publish_status), sort_order = VALUES(sort_order), deleted_at = NULL;

INSERT INTO news_category (slug, name_zh, name_en, publish_status, sort_order)
VALUES ('events', CONVERT(UNHEX('E4BA8BE4BBB6') USING utf8mb4), 'Events', 'PUBLISHED', 20)
ON DUPLICATE KEY UPDATE
  name_zh = VALUES(name_zh), name_en = VALUES(name_en), publish_status = VALUES(publish_status), sort_order = VALUES(sort_order), deleted_at = NULL;
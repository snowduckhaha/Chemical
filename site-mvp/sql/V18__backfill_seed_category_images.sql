-- Link the original published product categories to their existing public
-- media assets. The editor can then preserve the current image on a no-op
-- save while the published-content image requirement remains enforced.
-- Image descriptions remain editable in the admin UI after this backfill.
INSERT IGNORE INTO product_category_image
    (category_id, media_id, alt_zh, alt_en, publish_status)
SELECT
    c.id,
    media.media_id,
    NULL,
    NULL,
    'PUBLISHED'
FROM product_category c
JOIN (
    SELECT storage_url, MIN(id) AS media_id
    FROM media_asset
    GROUP BY storage_url
) media ON media.storage_url = CONCAT('/products/', c.slug, '.jpg')
WHERE c.deleted_at IS NULL
  AND c.slug IN (
      'aluminum-hydroxide',
      'silica-powder-series',
      'alumina-powder',
      'silane-coupling-agent'
  );

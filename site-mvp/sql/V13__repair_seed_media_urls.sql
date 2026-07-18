-- Repair legacy seed URLs that never corresponded to deployable static files.
-- Uploaded /uploads/* media is intentionally left untouched.
UPDATE media_asset
SET file_name = 'aluminum-hydroxide.jpg', file_ext = 'jpg', mime_type = 'image/jpeg',
    storage_url = '/products/aluminum-hydroxide.jpg'
WHERE id IN (1001, 1101) AND storage_url LIKE '/images/%';

UPDATE media_asset
SET file_name = 'silica-powder-series.jpg', file_ext = 'jpg', mime_type = 'image/jpeg',
    storage_url = '/products/silica-powder-series.jpg'
WHERE id IN (1002, 1102) AND storage_url LIKE '/images/%';

UPDATE media_asset
SET file_name = 'alumina-powder.jpg', file_ext = 'jpg', mime_type = 'image/jpeg',
    storage_url = '/products/alumina-powder.jpg'
WHERE id IN (1003, 1103) AND storage_url LIKE '/images/%';

UPDATE media_asset
SET file_name = 'silane-coupling-agent.jpg', file_ext = 'jpg', mime_type = 'image/jpeg',
    storage_url = '/products/silane-coupling-agent.jpg'
WHERE id IN (1004, 1104) AND storage_url LIKE '/images/%';
-- Temporary visual-test assets sourced from the user-approved reference site.
-- Replace these local files and values with licensed final assets before release.
UPDATE news_article
SET cover_image_url = '/news/reference-news-article-cover.webp',
    cover_alt_zh = CONVERT(UNHEX('E8B584E8AEAFE6B58BE8AF95E5B081E99DA2E59BBE') USING utf8mb4),
    cover_alt_en = 'Temporary news article cover'
WHERE slug = 'ath-flame-retardant-advantages';
-- Ensure every public SEO record has explicit Open Graph copy. When an editor
-- has supplied dedicated text it is retained; otherwise the page title and
-- description are the truthful share-card fallback.
UPDATE seo_meta
SET og_title = COALESCE(NULLIF(og_title, ''), title),
    og_description = COALESCE(NULLIF(og_description, ''), description)
WHERE publish_status = 'PUBLISHED';

-- Replace the temporary visual-test alt text with a factual, searchable
-- description of the published article image.  The filename is retained so
-- existing links remain stable; CMS editors can replace the licensed asset
-- later without reintroducing an empty or generic alt attribute.
UPDATE news_article
SET cover_alt_zh = '氢氧化铝 ATH 无卤阻燃材料技术文章封面图',
    cover_alt_en = 'Aluminum hydroxide ATH flame-retardant material article cover'
WHERE slug = 'ath-flame-retardant-advantages';

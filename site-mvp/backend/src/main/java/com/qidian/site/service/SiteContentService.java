package com.qidian.site.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.site.dto.SiteDtos.Application;
import com.qidian.site.dto.SiteDtos.HomeSection;
import com.qidian.site.dto.SiteDtos.InquiryRequest;
import com.qidian.site.dto.SiteDtos.InquiryResult;
import com.qidian.site.dto.SiteDtos.LinkedSeries;
import com.qidian.site.dto.SiteDtos.NavItem;
import com.qidian.site.dto.SiteDtos.News;
import com.qidian.site.dto.SiteDtos.NewsCategory;
import com.qidian.site.dto.SiteDtos.NewsDetail;
import com.qidian.site.dto.SiteDtos.NewsPage;
import com.qidian.site.dto.SiteDtos.ParameterItem;
import com.qidian.site.dto.SiteDtos.Product;
import com.qidian.site.dto.SiteDtos.ProductCategory;
import com.qidian.site.dto.SiteDtos.ProductSeries;
import com.qidian.site.dto.SiteDtos.SeoMeta;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class SiteContentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public SiteContentService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public String normalizeLang(String lang) {
        if (lang == null) {
            return "zh";
        }
        String normalized = lang.toLowerCase(Locale.ROOT);
        return normalized.equals("en") ? "en" : "zh";
    }

    public List<NavItem> getNav(String lang) {
        List<NavItem> configured = getConfiguredNav(lang);
        if (!configured.isEmpty()) {
            return configured;
        }

        if ("en".equals(lang)) {
            return List.of(
                new NavItem("home", "Home", "/en"),
                new NavItem("about", "About", "/en/about"),
                new NavItem("products", "Products", "/en/products"),
                new NavItem("applications", "Applications", "/en/applications"),
                new NavItem("news", "News", "/en/news"),
                new NavItem("contact", "Contact", "/en/contact")
            );
        }

        return List.of(
            new NavItem("home", "首页", "/zh"),
            new NavItem("about", "关于我们", "/zh/about"),
            new NavItem("products", "产品中心", "/zh/products"),
            new NavItem("applications", "应用领域", "/zh/applications"),
            new NavItem("news", "资讯中心", "/zh/news"),
            new NavItem("contact", "联系我们", "/zh/contact")
        );
    }

    public HomeSection getHomeSection(String lang) {
        HomeSection configured = getConfiguredHomeSection(lang);
        if (configured != null) {
            return configured;
        }

        if ("en".equals(lang)) {
            return new HomeSection(
                "Advanced Material Solutions",
                "Focused on flame-retardant and functional inorganic materials for global B2B customers.",
                "Explore Products",
                "/en/products",
                List.of(
                    "Hero Banner",
                    "Company Positioning",
                    "Product Categories",
                    "Application Entry",
                    "Trust Module",
                    "Delivery Capability",
                    "News Entry",
                    "CTA Inquiry",
                    "Contact Points",
                    "Footer Navigation"
                )
            );
        }

        return new HomeSection(
            "先进材料解决方案",
            "专注阻燃与功能性无机新材料，为全球 B2B 客户提供稳定供货与技术支持。",
            "进入产品中心",
            "/zh/products",
            List.of(
                "Hero Banner",
                "公司定位",
                "产品分类",
                "应用入口",
                "信任模块",
                "交付能力",
                "资讯入口",
                "CTA 询盘",
                "多触点联系",
                "页脚导航"
            )
        );
    }

    private List<NavItem> getConfiguredNav(String lang) {
        String sql = """
            SELECT payload_json
            FROM page_content
            WHERE page_key = 'nav'
              AND lang = ?
              AND publish_status = 'PUBLISHED'
            LIMIT 1
            """;
        List<String> payloads = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("payload_json"), lang);
        if (payloads.isEmpty()) {
            return List.of();
        }

        try {
            List<Map<String, Object>> rows = objectMapper.readValue(payloads.get(0), new TypeReference<>() {
            });
            List<NavItem> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String key = firstNonBlank(mapText(row, "key"), mapText(row, "id"));
                String label = mapText(row, "label");
                String path = mapText(row, "path");
                if (key != null && label != null && path != null) {
                    result.add(new NavItem(key, label, path));
                }
            }
            return result;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private HomeSection getConfiguredHomeSection(String lang) {
        String sql = """
            SELECT payload_json
            FROM page_content
            WHERE page_key = 'home'
              AND lang = ?
              AND publish_status = 'PUBLISHED'
            LIMIT 1
            """;
        List<String> payloads = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("payload_json"), lang);
        if (payloads.isEmpty()) {
            return null;
        }

        try {
            Map<String, Object> data = objectMapper.readValue(payloads.get(0), new TypeReference<>() {
            });
            String heroTitle = firstNonBlank(mapText(data, "heroTitle"), mapText(data, "hero_title"));
            String heroSubtitle = firstNonBlank(mapText(data, "heroSubtitle"), mapText(data, "hero_subtitle"));
            String ctaText = firstNonBlank(mapText(data, "ctaText"), mapText(data, "cta_text"));
            String ctaLink = firstNonBlank(mapText(data, "ctaLink"), mapText(data, "cta_link"));
            List<String> moduleOrder = firstNonEmptyList(mapStringList(data, "moduleOrder"), mapStringList(data, "module_order"));

            if (heroTitle == null || heroSubtitle == null || ctaText == null || ctaLink == null || moduleOrder.isEmpty()) {
                return null;
            }
            return new HomeSection(heroTitle, heroSubtitle, ctaText, ctaLink, moduleOrder);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<ProductCategory> getCategories(String lang) {
        String sql = """
            SELECT c.slug, c.name_zh, c.name_en, c.chemical_formula, c.summary_zh, c.summary_en,
                   c.application_zh, c.application_en, m.storage_url AS image_url
            FROM product_category c
            LEFT JOIN product_category_image ci
                   ON ci.category_id = c.id AND ci.publish_status = 'PUBLISHED'
            LEFT JOIN media_asset m ON m.id = ci.media_id
            WHERE c.publish_status = 'PUBLISHED'
              AND c.deleted_at IS NULL
            ORDER BY c.sort_order ASC, c.id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String slug = rs.getString("slug");
            String imageUrl = rs.getString("image_url");
            return new ProductCategory(
                slug,
                localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
                localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                imageUrl == null || imageUrl.isBlank() ? categoryImage(slug) : imageUrl,
                rs.getString("chemical_formula"),
                localizedList(lang, rs.getString("application_zh"), rs.getString("application_en"))
            );
        });
    }

    public List<ProductSeries> getSeriesByCategory(String lang, String categorySlug) {
        String sql = """
            SELECT s.id,
                   s.slug,
                   c.slug AS category_slug,
                   s.name_zh,
                   s.name_en,
                   s.summary_zh,
                   s.summary_en,
                   s.application_zh,
                   s.application_en
            FROM product_series s
            JOIN product_category c ON c.id = s.category_id
            WHERE c.slug = ?
              AND c.publish_status = 'PUBLISHED'
              AND s.publish_status = 'PUBLISHED'
                            AND c.deleted_at IS NULL
                            AND s.deleted_at IS NULL
            ORDER BY s.sort_order ASC, s.id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long seriesId = rs.getLong("id");
            return new ProductSeries(
                rs.getString("slug"),
                rs.getString("category_slug"),
                localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
                localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                getSeriesImageBySeriesId(seriesId),
                localizedList(lang, rs.getString("application_zh"), rs.getString("application_en"))
            );
        }, categorySlug);
    }

    public List<Product> getProductsBySeries(String lang, String categorySlug, String seriesSlug) {
        String sql = """
            SELECT p.id,
                   p.slug,
                   c.slug AS category_slug,
                   s.slug AS series_slug,
                   p.name_zh,
                   p.name_en,
                   p.model,
                   p.summary_zh,
                   p.summary_en,
                   p.detail_zh,
                   p.detail_en,
                   p.application_scenario_zh,
                   p.application_scenario_en,
                   p.packaging_zh,
                   p.packaging_en,
                   p.publish_status,
                   p.series_id
            FROM product p
            JOIN product_category c ON c.id = p.category_id
            JOIN product_series s ON s.id = p.series_id
            WHERE c.slug = ?
              AND s.slug = ?
              AND c.publish_status = 'PUBLISHED'
              AND s.publish_status = 'PUBLISHED'
              AND p.publish_status = 'PUBLISHED'
              AND c.deleted_at IS NULL
              AND s.deleted_at IS NULL
              AND p.deleted_at IS NULL
            ORDER BY p.sort_order ASC, p.id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long productId = rs.getLong("id");
            Long seriesId = rs.getLong("series_id");
            return new Product(
                rs.getString("slug"),
                rs.getString("category_slug"),
                rs.getString("series_slug"),
                localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
                rs.getString("model"),
                localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                localized(lang, rs.getString("detail_zh"), rs.getString("detail_en")),
                getProductImageByProductId(productId),
                getProductParameterItems(productId, lang),
                firstNonEmptyList(
                    localizedList(lang, rs.getString("application_scenario_zh"), rs.getString("application_scenario_en")),
                    getApplicationNamesBySeries(seriesId, lang)
                ),
                localized(lang, rs.getString("packaging_zh"), rs.getString("packaging_en")),
                rs.getString("publish_status")
            );
        }, categorySlug, seriesSlug);
    }

    public Product getProductDetail(String lang, String categorySlug, String seriesSlug, String productSlug) {
        return getProductsBySeries(lang, categorySlug, seriesSlug)
            .stream()
            .filter(item -> item.slug().equals(productSlug))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("product not found"));
    }

    public List<Product> searchProducts(String lang, String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        String like = "%" + normalized + "%";
        String sql = """
            SELECT p.id,
                   p.slug,
                   c.slug AS category_slug,
                   s.slug AS series_slug,
                   p.name_zh,
                   p.name_en,
                   p.model,
                   p.summary_zh,
                   p.summary_en,
                   p.detail_zh,
                   p.detail_en,
                   p.packaging_zh,
                   p.packaging_en,
                   p.publish_status,
                   p.series_id
            FROM product p
            JOIN product_category c ON c.id = p.category_id
            JOIN product_series s ON s.id = p.series_id
            WHERE p.publish_status = 'PUBLISHED'
              AND c.publish_status = 'PUBLISHED'
              AND s.publish_status = 'PUBLISHED'
                            AND p.deleted_at IS NULL
                            AND c.deleted_at IS NULL
                            AND s.deleted_at IS NULL
              AND (
                p.model LIKE ?
                OR p.slug LIKE ?
                OR p.name_zh LIKE ?
                OR p.name_en LIKE ?
                OR c.name_zh LIKE ?
                OR c.name_en LIKE ?
                OR s.name_zh LIKE ?
                OR s.name_en LIKE ?
              )
            ORDER BY p.sort_order ASC, p.id ASC
            LIMIT 50
            """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                long productId = rs.getLong("id");
                long seriesId = rs.getLong("series_id");
                return new Product(
                    rs.getString("slug"),
                    rs.getString("category_slug"),
                    rs.getString("series_slug"),
                    localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
                    rs.getString("model"),
                    localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                    localized(lang, rs.getString("detail_zh"), rs.getString("detail_en")),
                    getProductImageByProductId(productId),
                    getProductParameterItems(productId, lang),
                    getApplicationNamesBySeries(seriesId, lang),
                    localized(lang, rs.getString("packaging_zh"), rs.getString("packaging_en")),
                    rs.getString("publish_status")
                );
            }, like, like, like, like, like, like, like, like);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getRecommendations(String lang, String sourceType, long sourceId) {
        String normalizedSourceType = sourceType == null ? "" : sourceType.trim().toUpperCase(Locale.ROOT);
        if (!normalizedSourceType.equals("SERIES") && !normalizedSourceType.equals("PRODUCT")) {
            throw new IllegalArgumentException("sourceType must be SERIES or PRODUCT");
        }

        String sql = """
            SELECT r.target_type,
                   r.target_id,
                   p.slug AS product_slug,
                   p.name_zh AS product_name_zh,
                   p.name_en AS product_name_en,
                   s.slug AS series_slug,
                   s.name_zh AS series_name_zh,
                   s.name_en AS series_name_en
            FROM product_recommend_relation r
                        LEFT JOIN product p ON r.target_type = 'PRODUCT' AND p.id = r.target_id AND p.publish_status = 'PUBLISHED' AND p.deleted_at IS NULL
                        LEFT JOIN product_series s ON r.target_type = 'SERIES' AND s.id = r.target_id AND s.publish_status = 'PUBLISHED' AND s.deleted_at IS NULL
            WHERE r.publish_status = 'PUBLISHED'
              AND r.source_type = ?
              AND r.source_id = ?
                            AND ((r.target_type = 'PRODUCT' AND p.id IS NOT NULL)
                                OR (r.target_type = 'SERIES' AND s.id IS NOT NULL))
            ORDER BY r.sort_order ASC, r.id ASC
            """;

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String nameZh = firstNonBlank(rs.getString("product_name_zh"), rs.getString("series_name_zh"));
                String nameEn = firstNonBlank(rs.getString("product_name_en"), rs.getString("series_name_en"));
                String slug = firstNonBlank(rs.getString("product_slug"), rs.getString("series_slug"));

                Map<String, Object> row = new java.util.HashMap<>();
                row.put("targetType", rs.getString("target_type"));
                row.put("targetId", rs.getLong("target_id"));
                row.put("slug", slug);
                row.put("name", localized(lang, nameZh, nameEn));
                return row;
            }, normalizedSourceType, sourceId);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    public List<Application> getApplications(String lang) {
        String sql = """
            SELECT slug, name_zh, name_en, overview_zh, overview_en
            FROM application_field
            WHERE publish_status = 'PUBLISHED'
                            AND deleted_at IS NULL
            ORDER BY sort_order ASC, id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String appSlug = rs.getString("slug");
            return new Application(
                appSlug,
                localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
                localized(lang, rs.getString("overview_zh"), rs.getString("overview_en")),
                getApplicationImage(appSlug),
                defaultHighlights(lang),
                getLinkedSeries(appSlug, lang),
                defaultFaqs(lang)
            );
        });
    }

    public Application getApplicationDetail(String lang, String applicationSlug) {
        return getApplications(lang).stream()
            .filter(item -> item.slug().equals(applicationSlug))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("application not found"));
    }

    public List<News> getNews(String lang) {
        String sql = """
            SELECT n.slug,
                   n.title_zh,
                   n.title_en,
                   n.summary_zh,
                   n.summary_en,
                   c.slug AS category_slug,
                   c.name_zh AS category_name_zh,
                   c.name_en AS category_name_en,
                   n.published_at,
                   n.cover_image_url,
                   n.cover_alt_zh,
                   n.cover_alt_en
            FROM news_article n
            JOIN news_category c ON c.id = n.category_id
            WHERE n.publish_status = 'PUBLISHED'
              AND c.publish_status = 'PUBLISHED'
                            AND n.deleted_at IS NULL
                            AND c.deleted_at IS NULL
                            AND n.published_at IS NOT NULL
                            AND n.published_at <= CURRENT_TIMESTAMP
                        ORDER BY n.sort_order ASC, n.published_at DESC, n.id DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new News(
            rs.getString("slug"),
            localized(lang, rs.getString("title_zh"), rs.getString("title_en")),
            localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
            rs.getString("category_slug"),
            localized(lang, rs.getString("category_name_zh"), rs.getString("category_name_en")),
            rs.getTimestamp("published_at").toLocalDateTime().format(DATE_FMT),
            rs.getString("cover_image_url"),
            localized(lang, rs.getString("cover_alt_zh"), rs.getString("cover_alt_en"))
        ));
    }

    public List<NewsCategory> getNewsCategories(String lang) {
        String sql = """
            SELECT slug, name_zh, name_en
            FROM news_category
            WHERE publish_status = 'PUBLISHED'
                            AND deleted_at IS NULL
            ORDER BY sort_order ASC, id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new NewsCategory(
            rs.getString("slug"),
            localized(lang, rs.getString("name_zh"), rs.getString("name_en"))
        ));
    }

    public NewsPage getNewsByCategory(String lang, String categorySlug, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(50, Math.max(1, pageSize));
        Integer categoryCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM news_category
            WHERE slug = ? AND publish_status = 'PUBLISHED' AND deleted_at IS NULL
            """, Integer.class, categorySlug);
        if (categoryCount == null || categoryCount == 0) throw new IllegalArgumentException("news category not found");

        String where = """
            FROM news_article n JOIN news_category c ON c.id = n.category_id
            WHERE c.slug = ? AND n.publish_status = 'PUBLISHED' AND c.publish_status = 'PUBLISHED'
              AND n.deleted_at IS NULL AND c.deleted_at IS NULL
              AND n.published_at IS NOT NULL AND n.published_at <= CURRENT_TIMESTAMP
            """;
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + where, Long.class, categorySlug);
        List<News> items = jdbcTemplate.query("""
            SELECT n.slug, n.title_zh, n.title_en, n.summary_zh, n.summary_en,
                   c.slug AS category_slug, c.name_zh AS category_name_zh, c.name_en AS category_name_en,
                   n.published_at, n.cover_image_url, n.cover_alt_zh, n.cover_alt_en
            """ + where + " ORDER BY n.sort_order ASC, n.published_at DESC, n.id DESC LIMIT ? OFFSET ?",
            (rs, rowNum) -> new News(
                rs.getString("slug"), localized(lang, rs.getString("title_zh"), rs.getString("title_en")),
                localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                rs.getString("category_slug"), localized(lang, rs.getString("category_name_zh"), rs.getString("category_name_en")),
                rs.getTimestamp("published_at").toLocalDateTime().format(DATE_FMT), rs.getString("cover_image_url"),
                localized(lang, rs.getString("cover_alt_zh"), rs.getString("cover_alt_en"))
            ), categorySlug, safePageSize, (safePage - 1) * safePageSize);
        return new NewsPage(items, total == null ? 0 : total, safePage, safePageSize);
    }

    public NewsDetail getNewsDetail(String lang, String categorySlug, String articleSlug) {
        String sql = """
            SELECT n.id,
                   n.slug,
                   n.title_zh,
                   n.title_en,
                   n.content_zh,
                   n.content_en,
                   n.summary_zh,
                   n.summary_en,
                   n.cover_image_url,
                   n.cover_alt_zh,
                   n.cover_alt_en,
                   n.published_at,
                   c.id AS category_id,
                   c.slug AS category_slug,
                   c.name_zh AS category_name_zh,
                   c.name_en AS category_name_en
            FROM news_article n
            JOIN news_category c ON c.id = n.category_id
            WHERE n.slug = ? AND c.slug = ?
              AND n.publish_status = 'PUBLISHED'
              AND c.publish_status = 'PUBLISHED'
              AND n.deleted_at IS NULL
              AND c.deleted_at IS NULL
              AND n.published_at IS NOT NULL AND n.published_at <= CURRENT_TIMESTAMP
            LIMIT 1
            """;

        List<NewsDetail> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long categoryId = rs.getLong("category_id");
            Long articleId = rs.getLong("id");
            return new NewsDetail(
                rs.getString("slug"),
                rs.getString("category_slug"),
                localized(lang, rs.getString("title_zh"), rs.getString("title_en")),
                localized(lang, rs.getString("category_name_zh"), rs.getString("category_name_en")),
                localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
                rs.getTimestamp("published_at").toLocalDateTime().format(DATE_FMT),
                localized(lang, rs.getString("content_zh"), rs.getString("content_en")),
                rs.getString("cover_image_url"),
                localized(lang, rs.getString("cover_alt_zh"), rs.getString("cover_alt_en")),
                getRelatedNewsSlugs(categoryId, articleId),
                getRecommendedProductSlugs(articleId)
            );
        }, articleSlug, categorySlug);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("news not found");
        }
        return result.get(0);
    }

    public NewsDetail getNewsDetail(String lang, String articleSlug) {
        List<String> categories = jdbcTemplate.query("""
            SELECT c.slug FROM news_article n JOIN news_category c ON c.id = n.category_id
            WHERE n.slug = ? AND n.publish_status = 'PUBLISHED' AND n.deleted_at IS NULL
              AND c.publish_status = 'PUBLISHED' AND c.deleted_at IS NULL
              AND n.published_at IS NOT NULL AND n.published_at <= CURRENT_TIMESTAMP LIMIT 1
            """, (rs, rowNum) -> rs.getString("slug"), articleSlug);
        if (categories.isEmpty()) throw new IllegalArgumentException("news not found");
        return getNewsDetail(lang, categories.get(0), articleSlug);
    }

    public SeoMeta getSeoMeta(String lang, String pageKey) {
        String sql = """
            SELECT page_key, title, description, og_title, og_description, og_image, canonical
            FROM seo_meta
            WHERE page_key = ?
              AND lang = ?
              AND publish_status = 'PUBLISHED'
            LIMIT 1
            """;
        List<SeoMeta> result = jdbcTemplate.query(sql, (rs, rowNum) -> new SeoMeta(
            rs.getString("page_key"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("og_title"),
            rs.getString("og_description"),
            rs.getString("og_image"),
            rs.getString("canonical")
        ), pageKey, lang);

        if (!result.isEmpty()) {
            return result.get(0);
        }

        if (pageKey.startsWith("news.detail.")) {
            String slug = pageKey.substring("news.detail.".length());
            List<SeoMeta> articleSeo = jdbcTemplate.query("""
                SELECT slug, title_zh, title_en, summary_zh, summary_en, seo_title, seo_description, cover_image_url
                FROM news_article
                WHERE slug = ? AND publish_status = 'PUBLISHED' AND deleted_at IS NULL
                LIMIT 1
                """, (rs, rowNum) -> {
                String title = toText(rs.getString("seo_title"));
                if (title == null) title = localized(lang, rs.getString("title_zh"), rs.getString("title_en"));
                String description = toText(rs.getString("seo_description"));
                if (description == null) description = localized(lang, rs.getString("summary_zh"), rs.getString("summary_en"));
                String categorySlug = jdbcTemplate.queryForObject("""
                    SELECT c.slug FROM news_article n JOIN news_category c ON c.id = n.category_id WHERE n.slug = ? LIMIT 1
                    """, String.class, slug);
                return new SeoMeta(pageKey, title, description, title, description, rs.getString("cover_image_url"), "/" + lang + "/news/" + categorySlug + "/" + slug);
            }, slug);
            if (!articleSeo.isEmpty()) return articleSeo.get(0);
        }

        boolean en = "en".equals(lang);
        String fallbackTitle = en ? "Qidian Chemical" : "起点化工";
        String fallbackDescription = en
            ? "B2B material supplier for ATH, nano alumina and related product lines."
            : "面向 B2B 客户提供 ATH、纳米氧化铝等材料解决方案。";
        String fallbackCanonical = "/" + lang + "/" + ("home".equals(pageKey) ? "" : pageKey);
        return new SeoMeta(pageKey, fallbackTitle, fallbackDescription, fallbackTitle, fallbackDescription, null, fallbackCanonical);
    }

    public String createInquiryId() {
        return "INQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    public InquiryResult createInquiry(InquiryRequest request) {
        String lang = normalizeLang(request.getLang());
        String inquiryNo = createInquiryId();
        String sql = """
            INSERT INTO inquiry (
              inquiry_no,
              lang,
              name,
              company,
              email,
              phone,
              country,
              interested_product,
              message,
              source_page,
                            lead_score,
                            lead_level,
              inquiry_status,
              publish_status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', 'PUBLISHED')
            """;
                int leadScore = calculateLeadScore(request);
        jdbcTemplate.update(
            sql,
            inquiryNo,
            lang,
            request.getName(),
            emptyToNull(request.getCompany()),
            request.getEmail(),
            emptyToNull(request.getPhone()),
            emptyToNull(request.getCountry()),
            emptyToNull(request.getInterestedProduct()),
            request.getMessage(),
            emptyToNull(request.getSourcePage()),
            leadScore,
            leadScore >= 40 ? "HIGH" : leadScore >= 20 ? "MEDIUM" : "LOW"
        );
        return new InquiryResult(inquiryNo, "NEW");
    }

    private int calculateLeadScore(InquiryRequest request) {
        int score = 0;
        if (request.getCompany() != null && !request.getCompany().isBlank()) score += 10;
        if (request.getPhone() != null && !request.getPhone().isBlank()) score += 10;
        if (request.getInterestedProduct() != null && !request.getInterestedProduct().isBlank()) score += 15;
        if (request.getMessage() != null && request.getMessage().length() >= 30) score += 10;
        if (request.getSourcePage() != null && request.getSourcePage().contains("/products/")) score += 15;
        return score;
    }

    public List<String> getSitemapPaths(String lang) {
        List<String> staticPages = List.of(
            "/" + lang,
            "/" + lang + "/about",
            "/" + lang + "/products",
            "/" + lang + "/applications",
            "/" + lang + "/news",
            "/" + lang + "/contact"
        );

        Stream<String> categoryPaths = getCategories(lang).stream()
            .map(category -> "/" + lang + "/products/" + category.slug());

        Stream<String> seriesPaths = getCategories(lang).stream()
            .flatMap(category -> getSeriesByCategory(lang, category.slug()).stream()
                .map(series -> "/" + lang + "/products/" + category.slug() + "/" + series.slug()));

        Stream<String> productPaths = getCategories(lang).stream()
            .flatMap(category -> getSeriesByCategory(lang, category.slug()).stream()
                .flatMap(series -> getProductsBySeries(lang, category.slug(), series.slug()).stream()
                    .map(product -> "/" + lang + "/products/" + category.slug() + "/" + series.slug() + "/" + product.slug())));

        Stream<String> applicationPaths = getApplications(lang).stream()
            .map(application -> "/" + lang + "/applications/" + application.slug());

        Stream<String> newsPaths = getNews(lang).stream()
            .map(news -> "/" + lang + "/news/" + news.categorySlug() + "/" + news.slug());

        Stream<String> newsCategoryPaths = getNewsCategories(lang).stream()
            .map(category -> "/" + lang + "/news/" + category.slug());

        return Stream.concat(
            staticPages.stream(),
            Stream.concat(
                categoryPaths,
                Stream.concat(
                    seriesPaths,
                    Stream.concat(productPaths, Stream.concat(applicationPaths, Stream.concat(newsPaths, newsCategoryPaths)))
                )
            )
        ).distinct().toList();
    }

    private String localized(String lang, String zh, String en) {
        if ("en".equals(lang)) {
            return en == null || en.isBlank() ? zh : en;
        }
        return zh == null || zh.isBlank() ? en : zh;
    }

    private List<String> localizedList(String lang, String zh, String en) {
        String value = localized(lang, zh, en);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split("[\\r\\n,，;；]+"))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .distinct()
            .toList();
    }

    private String categoryImage(String slug) {
        Map<String, String> map = Map.of(
            "aluminum-hydroxide", "/products/aluminum-hydroxide.jpg",
            "silica-powder-series", "/products/silica-powder-series.jpg",
            "alumina-powder", "/products/alumina-powder.jpg",
            "silane-coupling-agent", "/products/silane-coupling-agent.jpg"
        );
        return map.getOrDefault(slug, "/home/hero-1.png");
    }

    private List<ParameterItem> getProductParameterItems(Long productId, String lang) {
        String sql = """
            SELECT param_name_zh, param_name_en, param_value_raw, unit
            FROM product_parameter
            WHERE product_id = ?
              AND publish_status = 'PUBLISHED'
            ORDER BY sort_order ASC, id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ParameterItem(
            localized(lang, rs.getString("param_name_zh"), rs.getString("param_name_en")),
            rs.getString("param_value_raw"),
            rs.getString("unit")
        ), productId);
    }

    private String getSeriesImageBySeriesId(Long seriesId) {
        String sql = """
            SELECT m.storage_url
            FROM product_series_image si
            JOIN media_asset m ON m.id = si.media_id
            WHERE si.series_id = ?
              AND si.publish_status = 'PUBLISHED'
            LIMIT 1
            """;
        List<String> urls = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("storage_url"), seriesId);
        return urls.isEmpty() ? "/home/hero-1.png" : urls.get(0);
    }

    private String getProductImageByProductId(Long productId) {
        String sql = """
            SELECT m.storage_url
            FROM product_image pi
            JOIN media_asset m ON m.id = pi.media_id
            WHERE pi.product_id = ?
              AND pi.publish_status = 'PUBLISHED'
            LIMIT 1
            """;
        List<String> urls = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("storage_url"), productId);
        return urls.isEmpty() ? "/home/hero-1.png" : urls.get(0);
    }

    private List<String> getApplicationNamesBySeries(Long seriesId, String lang) {
        String sql = """
            SELECT a.name_zh, a.name_en
            FROM application_series_link l
            JOIN application_field a ON a.id = l.application_id
            WHERE l.series_id = ?
              AND l.publish_status = 'PUBLISHED'
              AND a.publish_status = 'PUBLISHED'
                            AND a.deleted_at IS NULL
            ORDER BY l.sort_order ASC, l.id ASC
            """;
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
            seriesId);
    }

    private List<LinkedSeries> getLinkedSeries(String applicationSlug, String lang) {
        String sql = """
            SELECT c.slug AS category_slug,
                   s.slug AS series_slug,
                   s.name_zh,
                   s.name_en,
                   s.summary_zh,
                     s.summary_en,
                     m.storage_url AS image_url
            FROM application_field a
            JOIN application_series_link l ON l.application_id = a.id
            JOIN product_series s ON s.id = l.series_id
            JOIN product_category c ON c.id = s.category_id
                 LEFT JOIN product_series_image si ON si.series_id = s.id AND si.publish_status = 'PUBLISHED'
                 LEFT JOIN media_asset m ON m.id = si.media_id
            WHERE a.slug = ?
              AND a.publish_status = 'PUBLISHED'
                            AND a.deleted_at IS NULL
              AND l.publish_status = 'PUBLISHED'
              AND s.publish_status = 'PUBLISHED'
              AND c.publish_status = 'PUBLISHED'
              AND s.deleted_at IS NULL
              AND c.deleted_at IS NULL
            ORDER BY l.sort_order ASC, l.id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LinkedSeries(
            rs.getString("category_slug"),
            rs.getString("series_slug"),
            localized(lang, rs.getString("name_zh"), rs.getString("name_en")),
            localized(lang, rs.getString("summary_zh"), rs.getString("summary_en")),
            rs.getString("image_url") == null ? categoryImage(rs.getString("category_slug")) : rs.getString("image_url")
        ), applicationSlug);
    }

    private String getApplicationImage(String applicationSlug) {
        List<String> images = jdbcTemplate.query("""
            SELECT m.storage_url
            FROM application_field a
            JOIN application_series_link l ON l.application_id = a.id AND l.publish_status = 'PUBLISHED'
            JOIN product_series_image si ON si.series_id = l.series_id AND si.publish_status = 'PUBLISHED'
            JOIN media_asset m ON m.id = si.media_id
            WHERE a.slug = ?
              AND a.publish_status = 'PUBLISHED'
              AND a.deleted_at IS NULL
            ORDER BY l.sort_order ASC, l.id ASC
            LIMIT 1
            """, (rs, rowNum) -> rs.getString("storage_url"), applicationSlug);
        return images.isEmpty() ? "/products/aluminum-hydroxide.jpg" : images.get(0);
    }

    private List<String> defaultHighlights(String lang) {
        if ("en".equals(lang)) {
            return List.of("Low impurity control", "Stable dispersion", "Mass production consistency");
        }
        return List.of("低杂质控制", "稳定分散性", "批次一致性");
    }

    private List<String> defaultFaqs(String lang) {
        if ("en".equals(lang)) {
            return List.of(
                "How to select D50 for this application?",
                "How to balance whiteness and loading ratio?"
            );
        }
        return List.of(
            "该应用如何选择合适中位粒径？",
            "如何平衡白度与填充比例？"
        );
    }

    private List<String> getRelatedNewsSlugs(Long categoryId, Long currentArticleId) {
                List<String> configured = jdbcTemplate.query("""
                        SELECT target.slug
                        FROM news_article_related relation
                        JOIN news_article target ON target.id = relation.related_article_id
                        JOIN news_category category ON category.id = target.category_id
                        WHERE relation.article_id = ? AND target.publish_status = 'PUBLISHED'
                            AND target.deleted_at IS NULL AND category.publish_status = 'PUBLISHED' AND category.deleted_at IS NULL
                        ORDER BY relation.sort_order ASC, target.id DESC
                        """, (rs, rowNum) -> rs.getString("slug"), currentArticleId);
                if (!configured.isEmpty()) return configured;
        String sql = """
            SELECT slug
            FROM news_article
            WHERE category_id = ?
              AND id <> ?
              AND publish_status = 'PUBLISHED'
                            AND deleted_at IS NULL
            ORDER BY published_at DESC, id DESC
            LIMIT 3
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("slug"), categoryId, currentArticleId);
    }

    private List<String> getRecommendedProductSlugs(Long articleId) {
        return jdbcTemplate.query("""
            SELECT product.slug
            FROM news_article_product relation
            JOIN product ON product.id = relation.product_id
            WHERE relation.article_id = ? AND product.publish_status = 'PUBLISHED' AND product.deleted_at IS NULL
            ORDER BY relation.sort_order ASC, product.id ASC
            """, (rs, rowNum) -> rs.getString("slug"), articleId);
    }

    private String emptyToNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = Objects.toString(value, null);
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private String mapText(Map<String, Object> map, String key) {
        return toText(map.get(key));
    }

    private List<String> mapStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = toText(item);
            if (text != null) {
                result.add(text);
            }
        }
        return result;
    }

    private List<String> firstNonEmptyList(List<String> first, List<String> second) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        if (second != null && !second.isEmpty()) {
            return second;
        }
        return List.of();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

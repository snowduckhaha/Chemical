package com.qidian.site.service;

import com.qidian.site.dto.SeoAdminDtos.SeoUpsertRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SeoAdminService {

    private static final Set<String> STATUSES = Set.of("DRAFT", "PUBLISHED", "OFFLINE");
    private final JdbcTemplate jdbcTemplate;

    public SeoAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> listPageKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>(List.of(
            "home", "about", "about.culture", "about.certificates", "products.home", "applications", "news", "contact"
        ));
        jdbcTemplate.queryForList("SELECT page_key FROM seo_meta ORDER BY page_key", String.class).forEach(keys::add);
        jdbcTemplate.queryForList("SELECT slug FROM product_category WHERE deleted_at IS NULL ORDER BY sort_order, id", String.class)
            .forEach(slug -> keys.add("products.category." + slug));
        jdbcTemplate.queryForList("""
            SELECT c.slug AS category_slug, s.slug AS series_slug FROM product_series s JOIN product_category c ON c.id = s.category_id
            WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL ORDER BY s.sort_order, s.id
            """).forEach(row -> keys.add("products.series." + row.get("category_slug") + "." + row.get("series_slug")));
        jdbcTemplate.queryForList("""
            SELECT c.slug AS category_slug, s.slug AS series_slug, p.slug AS product_slug FROM product p
            JOIN product_category c ON c.id = p.category_id JOIN product_series s ON s.id = p.series_id
            WHERE p.deleted_at IS NULL AND c.deleted_at IS NULL AND s.deleted_at IS NULL ORDER BY p.sort_order, p.id
            """).forEach(row -> keys.add("products.detail." + row.get("category_slug") + "." + row.get("series_slug") + "." + row.get("product_slug")));
        return new ArrayList<>(keys);
    }

    public Map<String, Object> get(String pageKey, String lang) {
        validateLang(lang);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            SELECT page_key, lang, title, description, og_title, og_description, og_image, canonical,
                   publish_status, updated_at
            FROM seo_meta WHERE page_key = ? AND lang = ? LIMIT 1
            """, pageKey, lang);
        if (!rows.isEmpty()) return rows.get(0);
        return Map.of(
            "page_key", pageKey, "lang", lang, "title", "", "description", "", "og_title", "",
            "og_description", "", "og_image", "", "canonical", "", "publish_status", "DRAFT"
        );
    }

    public void upsert(String pageKey, String lang, SeoUpsertRequest request) {
        if (pageKey == null || pageKey.isBlank() || pageKey.length() > 128) throw new IllegalArgumentException("invalid page key");
        validateLang(lang);
        if (!STATUSES.contains(request.publishStatus())) throw new IllegalArgumentException("invalid publish status");
        jdbcTemplate.update("""
            INSERT INTO seo_meta
            (entity_type, entity_id, lang, page_key, title, description, og_title, og_description,
             og_image, canonical, publish_status)
            VALUES ('PAGE', NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description),
              og_title = VALUES(og_title), og_description = VALUES(og_description), og_image = VALUES(og_image),
              canonical = VALUES(canonical), publish_status = VALUES(publish_status)
            """, lang, pageKey, request.title(), emptyToNull(request.description()),
            emptyToNull(request.ogTitle()), emptyToNull(request.ogDescription()), emptyToNull(request.ogImage()),
            emptyToNull(request.canonical()), request.publishStatus());
    }

    private void validateLang(String lang) {
        if (!"zh".equals(lang) && !"en".equals(lang)) throw new IllegalArgumentException("invalid language");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

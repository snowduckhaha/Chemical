package com.qidian.site.service;

import com.qidian.site.dto.NewsAdminDtos.ArticleUpsertRequest;
import com.qidian.site.dto.NewsAdminDtos.CategoryUpsertRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NewsAdminService {
    private static final Set<String> STATUSES = Set.of("DRAFT", "PUBLISHED", "OFFLINE");
    private final JdbcTemplate jdbcTemplate;

    public NewsAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> listCategories(String keyword, String status, int page, int pageSize) {
        PageBounds bounds = pageBounds(page, pageSize);
        StringBuilder where = new StringBuilder(" WHERE deleted_at IS NULL");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (slug LIKE ? OR name_zh LIKE ? OR name_en LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like); args.add(like); args.add(like);
        }
        if (status != null && !status.isBlank()) {
            validateStatus(status);
            where.append(" AND publish_status = ?");
            args.add(status);
        }
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_category" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(bounds.pageSize());
        pageArgs.add(bounds.offset());
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
            SELECT id, slug, name_zh, name_en, sort_order, publish_status, created_at, updated_at
            FROM news_category
            """ + where + " ORDER BY sort_order ASC, id DESC LIMIT ? OFFSET ?", pageArgs.toArray());
        return pageResult(items, total, bounds);
    }

    public List<Map<String, Object>> categoryOptions() {
        return jdbcTemplate.queryForList("""
            SELECT id, slug, name_zh, name_en, publish_status
            FROM news_category WHERE deleted_at IS NULL ORDER BY sort_order ASC, id ASC
            """);
    }

    public Map<String, Object> listArticles(String keyword, String status, Long categoryId, int page, int pageSize) {
        PageBounds bounds = pageBounds(page, pageSize);
        StringBuilder where = new StringBuilder(" WHERE a.deleted_at IS NULL AND c.deleted_at IS NULL");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (a.slug LIKE ? OR a.title_zh LIKE ? OR a.title_en LIKE ? OR a.summary_zh LIKE ? OR a.summary_en LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            for (int index = 0; index < 5; index++) args.add(like);
        }
        if (status != null && !status.isBlank()) {
            validateStatus(status);
            where.append(" AND a.publish_status = ?");
            args.add(status);
        }
        if (categoryId != null) {
            where.append(" AND a.category_id = ?");
            args.add(categoryId);
        }
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_article a JOIN news_category c ON c.id = a.category_id" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(bounds.pageSize());
        pageArgs.add(bounds.offset());
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
            SELECT a.id, a.category_id, c.slug AS category_slug, c.name_zh AS category_name_zh, c.name_en AS category_name_en,
                   a.slug, a.title_zh, a.title_en, a.summary_zh, a.summary_en, a.content_zh, a.content_en,
                   a.cover_image_url, a.cover_alt_zh, a.cover_alt_en, a.seo_title, a.seo_description,
                   a.published_at, a.sort_order, a.publish_status, a.created_at, a.updated_at,
                   (SELECT GROUP_CONCAT(np.product_id ORDER BY np.sort_order, np.product_id) FROM news_article_product np WHERE np.article_id = a.id) AS recommended_product_ids,
                   (SELECT GROUP_CONCAT(nr.related_article_id ORDER BY nr.sort_order, nr.related_article_id) FROM news_article_related nr WHERE nr.article_id = a.id) AS related_article_ids
            FROM news_article a
            JOIN news_category c ON c.id = a.category_id
            """ + where + " ORDER BY COALESCE(a.published_at, a.created_at) DESC, a.sort_order ASC, a.id DESC LIMIT ? OFFSET ?", pageArgs.toArray());
        items.forEach(this::hydrateRelations);
        return pageResult(items, total, bounds);
    }

    public List<Map<String, Object>> articleOptions(Long excludeId) {
        if (excludeId == null) {
            return jdbcTemplate.queryForList("SELECT id, title_zh, title_en, publish_status FROM news_article WHERE deleted_at IS NULL ORDER BY id DESC");
        }
        return jdbcTemplate.queryForList("SELECT id, title_zh, title_en, publish_status FROM news_article WHERE deleted_at IS NULL AND id <> ? ORDER BY id DESC", excludeId);
    }

    public List<Map<String, Object>> productOptions() {
        return jdbcTemplate.queryForList("""
            SELECT id, model, name_zh, name_en, publish_status
            FROM product WHERE deleted_at IS NULL ORDER BY sort_order ASC, id ASC
            """);
    }

    public long createCategory(CategoryUpsertRequest request) {
        validateStatus(request.publishStatus());
        KeyHolder keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO news_category (slug, name_zh, name_en, publish_status, sort_order)
                VALUES (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.slug());
            statement.setString(2, request.nameZh());
            statement.setString(3, request.nameEn());
            statement.setString(4, request.publishStatus());
            statement.setInt(5, request.sortOrder());
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    public void updateCategory(long id, CategoryUpsertRequest request) {
        validateStatus(request.publishStatus());
        int affected = jdbcTemplate.update("""
            UPDATE news_category SET slug = ?, name_zh = ?, name_en = ?, publish_status = ?, sort_order = ?
            WHERE id = ? AND deleted_at IS NULL
            """, request.slug(), request.nameZh(), request.nameEn(), request.publishStatus(), request.sortOrder(), id);
        requireAffected(affected, "news category not found");
    }

    public void updateCategoryStatus(long id, String status) {
        validateStatus(status);
        int affected = jdbcTemplate.update("UPDATE news_category SET publish_status = ? WHERE id = ? AND deleted_at IS NULL", status, id);
        requireAffected(affected, "news category not found");
    }

    public void updateCategorySort(long id, int sortOrder) {
        int affected = jdbcTemplate.update("UPDATE news_category SET sort_order = ? WHERE id = ? AND deleted_at IS NULL", sortOrder, id);
        requireAffected(affected, "news category not found");
    }

    @Transactional
    public void deleteCategory(long id) {
        int affected = jdbcTemplate.update("UPDATE news_category SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL", id);
        requireAffected(affected, "news category not found");
        jdbcTemplate.update("UPDATE news_article SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE category_id = ? AND deleted_at IS NULL", id);
    }

    @Transactional
    public long createArticle(ArticleUpsertRequest request) {
        validateArticle(request);
        KeyHolder keys = new GeneratedKeyHolder();
        LocalDateTime publishedAt = publishedAt(request.publishStatus(), request.publishedAt(), null);
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO news_article
                (category_id, slug, title_zh, title_en, summary_zh, summary_en, content_zh, content_en,
                 cover_image_url, cover_alt_zh, cover_alt_en, seo_title, seo_description, published_at,
                 sort_order, publish_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, request.categoryId());
            statement.setString(2, request.slug());
            statement.setString(3, request.titleZh());
            statement.setString(4, request.titleEn());
            statement.setString(5, emptyToNull(request.summaryZh()));
            statement.setString(6, emptyToNull(request.summaryEn()));
            statement.setString(7, emptyToNull(request.contentZh()));
            statement.setString(8, emptyToNull(request.contentEn()));
            statement.setString(9, emptyToNull(request.coverImageUrl()));
            statement.setString(10, emptyToNull(request.coverAltZh()));
            statement.setString(11, emptyToNull(request.coverAltEn()));
            statement.setString(12, emptyToNull(request.seoTitle()));
            statement.setString(13, emptyToNull(request.seoDescription()));
            statement.setObject(14, publishedAt);
            statement.setInt(15, request.sortOrder());
            statement.setString(16, request.publishStatus());
            return statement;
        }, keys);
        long id = keys.getKey().longValue();
        replaceRelations(id, request.recommendedProductIds(), request.relatedArticleIds());
        return id;
    }

    @Transactional
    public void updateArticle(long id, ArticleUpsertRequest request) {
        validateArticle(request);
        LocalDateTime currentPublishedAt = jdbcTemplate.query("SELECT published_at FROM news_article WHERE id = ? AND deleted_at IS NULL", resultSet -> resultSet.next() ? resultSet.getObject(1, LocalDateTime.class) : null, id);
        if (currentPublishedAt == null && !articleExists(id)) throw new IllegalArgumentException("news article not found");
        LocalDateTime publishedAt = publishedAt(request.publishStatus(), request.publishedAt(), currentPublishedAt);
        int affected = jdbcTemplate.update("""
            UPDATE news_article
            SET category_id = ?, slug = ?, title_zh = ?, title_en = ?, summary_zh = ?, summary_en = ?,
                content_zh = ?, content_en = ?, cover_image_url = ?, cover_alt_zh = ?, cover_alt_en = ?,
                seo_title = ?, seo_description = ?, published_at = ?, sort_order = ?, publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """, request.categoryId(), request.slug(), request.titleZh(), request.titleEn(), emptyToNull(request.summaryZh()),
            emptyToNull(request.summaryEn()), emptyToNull(request.contentZh()), emptyToNull(request.contentEn()),
            emptyToNull(request.coverImageUrl()), emptyToNull(request.coverAltZh()), emptyToNull(request.coverAltEn()),
            emptyToNull(request.seoTitle()), emptyToNull(request.seoDescription()), publishedAt, request.sortOrder(),
            request.publishStatus(), id);
        requireAffected(affected, "news article not found");
        replaceRelations(id, request.recommendedProductIds(), request.relatedArticleIds());
    }

    public void updateArticleStatus(long id, String status) {
        validateStatus(status);
        if ("PUBLISHED".equals(status)) requireArticlePublishReady(id);
        int affected = jdbcTemplate.update("""
            UPDATE news_article
            SET publish_status = ?, published_at = CASE WHEN ? = 'PUBLISHED' AND published_at IS NULL THEN CURRENT_TIMESTAMP ELSE published_at END
            WHERE id = ? AND deleted_at IS NULL
            """, status, status, id);
        requireAffected(affected, "news article not found");
    }

    public void updateArticleSort(long id, int sortOrder) {
        int affected = jdbcTemplate.update("UPDATE news_article SET sort_order = ? WHERE id = ? AND deleted_at IS NULL", sortOrder, id);
        requireAffected(affected, "news article not found");
    }

    @Transactional
    public void deleteArticle(long id) {
        int affected = jdbcTemplate.update("UPDATE news_article SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL", id);
        requireAffected(affected, "news article not found");
        jdbcTemplate.update("DELETE FROM news_article_product WHERE article_id = ?", id);
        jdbcTemplate.update("DELETE FROM news_article_related WHERE article_id = ? OR related_article_id = ?", id, id);
    }

    private void validateArticle(ArticleUpsertRequest request) {
        validateStatus(request.publishStatus());
        requireCategory(request.categoryId());
        if ("PUBLISHED".equals(request.publishStatus())) {
            requirePublishedCategory(request.categoryId());
            if (blank(request.summaryZh()) && blank(request.summaryEn())) throw new IllegalArgumentException("news summary is required before publishing");
            if (blank(request.contentZh()) && blank(request.contentEn())) throw new IllegalArgumentException("news content is required before publishing");
            if (blank(request.coverImageUrl())) throw new IllegalArgumentException("news cover image is required before publishing");
        }
    }

    private void requireArticlePublishReady(long id) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM news_article a
            JOIN news_category c ON c.id = a.category_id AND c.deleted_at IS NULL AND c.publish_status = 'PUBLISHED'
            WHERE a.id = ? AND a.deleted_at IS NULL AND a.title_zh <> '' AND a.title_en <> ''
              AND a.cover_image_url IS NOT NULL AND a.cover_image_url <> ''
              AND (NULLIF(TRIM(a.summary_zh), '') IS NOT NULL OR NULLIF(TRIM(a.summary_en), '') IS NOT NULL)
              AND (NULLIF(TRIM(a.content_zh), '') IS NOT NULL OR NULLIF(TRIM(a.content_en), '') IS NOT NULL)
            """, Integer.class, id);
        if (count == null || count == 0) throw new IllegalArgumentException("news article or category is not ready for publishing");
    }

    private void replaceRelations(long articleId, List<Long> productIds, List<Long> relatedIds) {
        jdbcTemplate.update("DELETE FROM news_article_product WHERE article_id = ?", articleId);
        jdbcTemplate.update("DELETE FROM news_article_related WHERE article_id = ?", articleId);
        int sort = 0;
        for (Long productId : distinctIds(productIds)) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product WHERE id = ? AND deleted_at IS NULL", Integer.class, productId);
            if (count == null || count == 0) throw new IllegalArgumentException("recommended product not found");
            jdbcTemplate.update("INSERT INTO news_article_product (article_id, product_id, sort_order) VALUES (?, ?, ?)", articleId, productId, sort++);
        }
        sort = 0;
        for (Long relatedId : distinctIds(relatedIds)) {
            if (relatedId == articleId) continue;
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_article WHERE id = ? AND deleted_at IS NULL", Integer.class, relatedId);
            if (count == null || count == 0) throw new IllegalArgumentException("related article not found");
            jdbcTemplate.update("INSERT INTO news_article_related (article_id, related_article_id, sort_order) VALUES (?, ?, ?)", articleId, relatedId, sort++);
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        return ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    }

    private void hydrateRelations(Map<String, Object> item) {
        item.put("recommended_product_ids", parseIds(item.get("recommended_product_ids")));
        item.put("related_article_ids", parseIds(item.get("related_article_ids")));
    }

    private List<Long> parseIds(Object value) {
        if (value == null || value.toString().isBlank()) return List.of();
        return java.util.Arrays.stream(value.toString().split(",")).map(Long::valueOf).toList();
    }

    private Map<String, Object> pageResult(List<Map<String, Object>> items, long total, PageBounds bounds) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", bounds.page());
        result.put("pageSize", bounds.pageSize());
        return result;
    }

    private PageBounds pageBounds(int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(100, pageSize));
        return new PageBounds(safePage, safeSize, (safePage - 1) * safeSize);
    }

    private LocalDateTime publishedAt(String status, LocalDateTime requested, LocalDateTime current) {
        if (requested != null) return requested;
        return "PUBLISHED".equals(status) && current == null ? LocalDateTime.now() : current;
    }

    private boolean articleExists(long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_article WHERE id = ? AND deleted_at IS NULL", Integer.class, id);
        return count != null && count > 0;
    }

    private void requireCategory(long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_category WHERE id = ? AND deleted_at IS NULL", Integer.class, id);
        if (count == null || count == 0) throw new IllegalArgumentException("news category not found");
    }

    private void requirePublishedCategory(long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news_category WHERE id = ? AND deleted_at IS NULL AND publish_status = 'PUBLISHED'", Integer.class, id);
        if (count == null || count == 0) throw new IllegalArgumentException("news category must be published first");
    }

    private void validateStatus(String status) {
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("invalid publish status");
    }

    private void requireAffected(int affected, String message) {
        if (affected == 0) throw new IllegalArgumentException(message);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String emptyToNull(String value) {
        return blank(value) ? null : value.trim();
    }

    private record PageBounds(int page, int pageSize, int offset) {
    }
}

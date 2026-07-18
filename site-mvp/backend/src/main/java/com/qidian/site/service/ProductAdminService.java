package com.qidian.site.service;

import com.qidian.site.dto.AdminDtos.CategoryUpsertRequest;
import com.qidian.site.dto.AdminDtos.ParameterUpsertItem;
import com.qidian.site.dto.AdminDtos.ProductUpsertRequest;
import com.qidian.site.dto.AdminDtos.SeriesUpsertRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ProductAdminService {

    private final JdbcTemplate jdbcTemplate;

    public ProductAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listCategories() {
        String sql = """
                 SELECT c.id, c.slug, c.name_zh, c.name_en, c.chemical_formula, c.summary_zh, c.summary_en,
                     c.application_zh, c.application_en, c.seo_title, c.seo_description, c.sort_order, c.publish_status,
                   m.storage_url AS image_url, ci.alt_zh AS image_alt_zh, ci.alt_en AS image_alt_en
            FROM product_category c
            LEFT JOIN product_category_image ci ON ci.category_id = c.id
            LEFT JOIN media_asset m ON m.id = ci.media_id
            WHERE c.deleted_at IS NULL
            ORDER BY c.sort_order ASC, c.id ASC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> listSeries(Long categoryId) {
        String sql = """
            SELECT s.id, s.category_id, c.slug AS category_slug, s.slug, s.name_zh, s.name_en,
                 s.summary_zh, s.summary_en, s.application_zh, s.application_en, s.seo_title, s.seo_description,
                 s.sort_order, s.publish_status,
                 m.storage_url AS image_url, si.alt_zh AS image_alt_zh, si.alt_en AS image_alt_en
            FROM product_series s
            JOIN product_category c ON c.id = s.category_id
             LEFT JOIN product_series_image si ON si.series_id = s.id
             LEFT JOIN media_asset m ON m.id = si.media_id
            WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
            """;
        if (categoryId == null) {
            return jdbcTemplate.queryForList(sql + " ORDER BY s.sort_order ASC, s.id ASC");
        }
        return jdbcTemplate.queryForList(sql + " AND s.category_id = ? ORDER BY s.sort_order ASC, s.id ASC", categoryId);
    }

    public List<Map<String, Object>> listProducts(Long categoryId, Long seriesId) {
        String sql = """
            SELECT p.id, p.category_id, p.series_id, p.slug, p.model, p.name_zh, p.name_en,
                   p.summary_zh, p.summary_en, p.detail_zh, p.detail_en, p.application_scenario_zh,
                   p.application_scenario_en, p.seo_title, p.seo_description, p.packaging_zh, p.packaging_en,
                   p.sort_order, p.publish_status, m.storage_url AS image_url,
                   pi.alt_zh AS image_alt_zh, pi.alt_en AS image_alt_en
            FROM product p
            LEFT JOIN product_image pi ON pi.product_id = p.id
            LEFT JOIN media_asset m ON m.id = pi.media_id
            WHERE p.deleted_at IS NULL
            """;
        List<Map<String, Object>> products;
        if (seriesId != null) {
            products = jdbcTemplate.queryForList(sql + " AND p.series_id = ? ORDER BY p.sort_order ASC, p.id ASC", seriesId);
        } else if (categoryId != null) {
            products = jdbcTemplate.queryForList(sql + " AND p.category_id = ? ORDER BY p.sort_order ASC, p.id ASC", categoryId);
        } else {
            products = jdbcTemplate.queryForList(sql + " ORDER BY p.sort_order ASC, p.id ASC");
        }
        products.forEach(product -> product.put(
            "parameters",
            listProductParameters(((Number) product.get("id")).longValue())
        ));
        return products;
    }

    private List<Map<String, Object>> listProductParameters(long productId) {
        return jdbcTemplate.queryForList("""
            SELECT param_key, param_name_zh, param_name_en, param_value_raw, unit, test_method,
                   sort_order, publish_status
            FROM product_parameter
            WHERE product_id = ?
            ORDER BY sort_order ASC, id ASC
            """, productId);
    }

    public long createCategory(CategoryUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        validateCategoryPublish(req);
        String sql = """
            INSERT INTO product_category
            (slug, name_zh, name_en, chemical_formula, summary_zh, summary_en, application_zh, application_en,
             seo_title, seo_description, sort_order, publish_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.slug());
            ps.setString(2, req.nameZh());
            ps.setString(3, req.nameEn());
            ps.setString(4, emptyToNull(req.chemicalFormula()));
            ps.setString(5, emptyToNull(req.summaryZh()));
            ps.setString(6, emptyToNull(req.summaryEn()));
            ps.setString(7, emptyToNull(req.applicationZh()));
            ps.setString(8, emptyToNull(req.applicationEn()));
            ps.setString(9, emptyToNull(req.seoTitle()));
            ps.setString(10, emptyToNull(req.seoDescription()));
            ps.setInt(11, req.sortOrder());
            ps.setString(12, req.publishStatus());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();
        upsertCategoryImage(id, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
        return id;
    }

    public void updateCategory(long id, CategoryUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        validateCategoryPublish(req);
        String sql = """
            UPDATE product_category
            SET slug = ?,
                name_zh = ?,
                name_en = ?,
                chemical_formula = ?,
                summary_zh = ?,
                summary_en = ?,
                application_zh = ?,
                application_en = ?,
                seo_title = ?,
                seo_description = ?,
                sort_order = ?,
                publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """;
        int affected = jdbcTemplate.update(sql,
            req.slug(),
            req.nameZh(),
            req.nameEn(),
            emptyToNull(req.chemicalFormula()),
            emptyToNull(req.summaryZh()),
            emptyToNull(req.summaryEn()),
            emptyToNull(req.applicationZh()),
            emptyToNull(req.applicationEn()),
            emptyToNull(req.seoTitle()),
            emptyToNull(req.seoDescription()),
            req.sortOrder(),
            req.publishStatus(),
            id);
        if (affected == 0) {
            throw new IllegalArgumentException("category not found");
        }
        upsertCategoryImage(id, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
    }

    public void updateCategorySort(long id, int sortOrder) {
        int affected = jdbcTemplate.update("UPDATE product_category SET sort_order = ? WHERE id = ? AND deleted_at IS NULL", sortOrder, id);
        if (affected == 0) {
            throw new IllegalArgumentException("category not found");
        }
    }

    public void updateCategoryStatus(long id, String publishStatus) {
        validatePublishStatus(publishStatus);
        if ("PUBLISHED".equals(publishStatus)) requireCategoryPublishReady(id);
        int affected = jdbcTemplate.update("UPDATE product_category SET publish_status = ? WHERE id = ? AND deleted_at IS NULL", publishStatus, id);
        if (affected == 0) {
            throw new IllegalArgumentException("category not found");
        }
    }

    @Transactional
    public void deleteCategory(long id) {
        int affected = jdbcTemplate.update(
            "UPDATE product_category SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL",
            id);
        if (affected == 0) throw new IllegalArgumentException("category not found");
        jdbcTemplate.update(
            "UPDATE product_series SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE category_id = ? AND deleted_at IS NULL",
            id);
        jdbcTemplate.update(
            "UPDATE product SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE category_id = ? AND deleted_at IS NULL",
            id);
    }

    @Transactional
    public long createSeries(SeriesUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        requireCategory(req.categoryId());
        validateSeriesPublish(req);
        String sql = """
            INSERT INTO product_series
            (category_id, slug, name_zh, name_en, summary_zh, summary_en, application_zh, application_en,
             seo_title, seo_description, sort_order, publish_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.categoryId());
            ps.setString(2, req.slug());
            ps.setString(3, req.nameZh());
            ps.setString(4, req.nameEn());
            ps.setString(5, emptyToNull(req.summaryZh()));
            ps.setString(6, emptyToNull(req.summaryEn()));
            ps.setString(7, emptyToNull(req.applicationZh()));
            ps.setString(8, emptyToNull(req.applicationEn()));
            ps.setString(9, emptyToNull(req.seoTitle()));
            ps.setString(10, emptyToNull(req.seoDescription()));
            ps.setInt(11, req.sortOrder());
            ps.setString(12, req.publishStatus());
            return ps;
        }, keyHolder);

        long seriesId = keyHolder.getKey().longValue();
        upsertSeriesImage(seriesId, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
        return seriesId;
    }

    @Transactional
    public void updateSeries(long id, SeriesUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        requireCategory(req.categoryId());
        validateSeriesPublish(req);
        String sql = """
            UPDATE product_series
            SET category_id = ?,
                slug = ?,
                name_zh = ?,
                name_en = ?,
                summary_zh = ?,
                summary_en = ?,
                application_zh = ?,
                application_en = ?,
                seo_title = ?,
                seo_description = ?,
                sort_order = ?,
                publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """;
        int affected = jdbcTemplate.update(sql,
            req.categoryId(),
            req.slug(),
            req.nameZh(),
            req.nameEn(),
            emptyToNull(req.summaryZh()),
            emptyToNull(req.summaryEn()),
            emptyToNull(req.applicationZh()),
            emptyToNull(req.applicationEn()),
            emptyToNull(req.seoTitle()),
            emptyToNull(req.seoDescription()),
            req.sortOrder(),
            req.publishStatus(),
            id);
        if (affected == 0) {
            throw new IllegalArgumentException("series not found");
        }

        upsertSeriesImage(id, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
    }

    public void updateSeriesSort(long id, int sortOrder) {
        int affected = jdbcTemplate.update("UPDATE product_series SET sort_order = ? WHERE id = ? AND deleted_at IS NULL", sortOrder, id);
        if (affected == 0) {
            throw new IllegalArgumentException("series not found");
        }
    }

    public void updateSeriesStatus(long id, String publishStatus) {
        validatePublishStatus(publishStatus);
        if ("PUBLISHED".equals(publishStatus)) requireSeriesPublishReady(id);
        int affected = jdbcTemplate.update("UPDATE product_series SET publish_status = ? WHERE id = ? AND deleted_at IS NULL", publishStatus, id);
        if (affected == 0) {
            throw new IllegalArgumentException("series not found");
        }
    }

    @Transactional
    public void deleteSeries(long id) {
        int affected = jdbcTemplate.update(
            "UPDATE product_series SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL",
            id);
        if (affected == 0) throw new IllegalArgumentException("series not found");
        jdbcTemplate.update(
            "UPDATE product SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE series_id = ? AND deleted_at IS NULL",
            id);
    }

    @Transactional
    public long createProduct(ProductUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        requireSeriesBelongsToCategory(req.seriesId(), req.categoryId());
        validateProductPublish(req);
        String sql = """
            INSERT INTO product
            (category_id, series_id, slug, model, name_zh, name_en, summary_zh, summary_en, detail_zh, detail_en,
             application_scenario_zh, application_scenario_en, seo_title, seo_description,
             packaging_zh, packaging_en, sort_order, publish_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.categoryId());
            ps.setLong(2, req.seriesId());
            ps.setString(3, req.slug());
            ps.setString(4, req.model());
            ps.setString(5, req.nameZh());
            ps.setString(6, req.nameEn());
            ps.setString(7, emptyToNull(req.summaryZh()));
            ps.setString(8, emptyToNull(req.summaryEn()));
            ps.setString(9, emptyToNull(req.detailZh()));
            ps.setString(10, emptyToNull(req.detailEn()));
            ps.setString(11, emptyToNull(req.applicationScenarioZh()));
            ps.setString(12, emptyToNull(req.applicationScenarioEn()));
            ps.setString(13, emptyToNull(req.seoTitle()));
            ps.setString(14, emptyToNull(req.seoDescription()));
            ps.setString(15, emptyToNull(req.packagingZh()));
            ps.setString(16, emptyToNull(req.packagingEn()));
            ps.setInt(17, req.sortOrder());
            ps.setString(18, req.publishStatus());
            return ps;
        }, keyHolder);

        long productId = keyHolder.getKey().longValue();
        upsertProductImage(productId, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
        try {
            replaceProductParameters(productId, req.parameters());
        } catch (DataAccessException ex) {
            // Compatible with environments that have not applied product_parameter migration yet.
        }
        return productId;
    }

    @Transactional
    public void updateProduct(long id, ProductUpsertRequest req) {
        validatePublishStatus(req.publishStatus());
        requireSeriesBelongsToCategory(req.seriesId(), req.categoryId());
        validateProductPublish(req);
        String sql = """
            UPDATE product
            SET category_id = ?,
                series_id = ?,
                slug = ?,
                model = ?,
                name_zh = ?,
                name_en = ?,
                summary_zh = ?,
                summary_en = ?,
                detail_zh = ?,
                detail_en = ?,
                application_scenario_zh = ?,
                application_scenario_en = ?,
                seo_title = ?,
                seo_description = ?,
                packaging_zh = ?,
                packaging_en = ?,
                sort_order = ?,
                publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """;
        int affected = jdbcTemplate.update(sql,
            req.categoryId(), req.seriesId(), req.slug(), req.model(), req.nameZh(), req.nameEn(),
            emptyToNull(req.summaryZh()), emptyToNull(req.summaryEn()), emptyToNull(req.detailZh()),
            emptyToNull(req.detailEn()), emptyToNull(req.applicationScenarioZh()), emptyToNull(req.applicationScenarioEn()),
            emptyToNull(req.seoTitle()), emptyToNull(req.seoDescription()), emptyToNull(req.packagingZh()),
            emptyToNull(req.packagingEn()), req.sortOrder(), req.publishStatus(), id);
        if (affected == 0) {
            throw new IllegalArgumentException("product not found");
        }

        upsertProductImage(id, req.imageUrl(), req.imageAltZh(), req.imageAltEn());
        try {
            replaceProductParameters(id, req.parameters());
        } catch (DataAccessException ex) {
            // Compatible with environments that have not applied product_parameter migration yet.
        }
    }

    public void updateProductSort(long id, int sortOrder) {
        int affected = jdbcTemplate.update("UPDATE product SET sort_order = ? WHERE id = ? AND deleted_at IS NULL", sortOrder, id);
        if (affected == 0) {
            throw new IllegalArgumentException("product not found");
        }
    }

    public void updateProductStatus(long id, String publishStatus) {
        validatePublishStatus(publishStatus);
        if ("PUBLISHED".equals(publishStatus)) requireProductPublishReady(id);
        int affected = jdbcTemplate.update("UPDATE product SET publish_status = ? WHERE id = ? AND deleted_at IS NULL", publishStatus, id);
        if (affected == 0) {
            throw new IllegalArgumentException("product not found");
        }
    }

    public void deleteProduct(long id) {
        int affected = jdbcTemplate.update(
            "UPDATE product SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL",
            id);
        if (affected == 0) throw new IllegalArgumentException("product not found");
    }

    private void replaceProductParameters(long productId, List<ParameterUpsertItem> parameters) {
        jdbcTemplate.update("DELETE FROM product_parameter WHERE product_id = ?", productId);
        String sql = """
            INSERT INTO product_parameter
            (product_id, param_key, param_name_zh, param_name_en, param_value_raw, unit, test_method, sort_order, publish_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        for (ParameterUpsertItem item : parameters) {
            validatePublishStatus(item.publishStatus());
            jdbcTemplate.update(sql,
                productId,
                item.paramKey(),
                item.paramNameZh(),
                item.paramNameEn(),
                item.paramValueRaw(),
                emptyToNull(item.unit()),
                emptyToNull(item.testMethod()),
                item.sortOrder(),
                item.publishStatus());
        }
    }

    private void upsertSeriesImage(long seriesId, String imageUrl, String altZh, String altEn) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        long mediaId = upsertMedia(imageUrl);
        int affected = jdbcTemplate.update(
            "UPDATE product_series_image SET media_id = ?, alt_zh = ?, alt_en = ?, publish_status = 'PUBLISHED' WHERE series_id = ?",
            mediaId,
            emptyToNull(altZh),
            emptyToNull(altEn),
            seriesId
        );
        if (affected == 0) {
            jdbcTemplate.update(
                "INSERT INTO product_series_image (series_id, media_id, alt_zh, alt_en, publish_status) VALUES (?, ?, ?, ?, 'PUBLISHED')",
                seriesId,
                mediaId,
                emptyToNull(altZh),
                emptyToNull(altEn)
            );
        }
    }

    private void upsertCategoryImage(long categoryId, String imageUrl, String altZh, String altEn) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        long mediaId = upsertMedia(imageUrl);
        int affected = jdbcTemplate.update(
            "UPDATE product_category_image SET media_id = ?, alt_zh = ?, alt_en = ?, publish_status = 'PUBLISHED' WHERE category_id = ?",
            mediaId, emptyToNull(altZh), emptyToNull(altEn), categoryId);
        if (affected == 0) {
            jdbcTemplate.update(
                "INSERT INTO product_category_image (category_id, media_id, alt_zh, alt_en, publish_status) VALUES (?, ?, ?, ?, 'PUBLISHED')",
                categoryId, mediaId, emptyToNull(altZh), emptyToNull(altEn));
        }
    }

    private void upsertProductImage(long productId, String imageUrl, String altZh, String altEn) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        long mediaId = upsertMedia(imageUrl);
        int affected = jdbcTemplate.update(
            "UPDATE product_image SET media_id = ?, alt_zh = ?, alt_en = ?, publish_status = 'PUBLISHED' WHERE product_id = ?",
            mediaId,
            emptyToNull(altZh),
            emptyToNull(altEn),
            productId
        );
        if (affected == 0) {
            jdbcTemplate.update(
                "INSERT INTO product_image (product_id, media_id, alt_zh, alt_en, publish_status) VALUES (?, ?, ?, ?, 'PUBLISHED')",
                productId,
                mediaId,
                emptyToNull(altZh),
                emptyToNull(altEn)
            );
        }
    }

    private long upsertMedia(String url) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT id FROM media_asset WHERE storage_url = ? LIMIT 1",
            (rs, rowNum) -> rs.getLong("id"),
            url
        );
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : null;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO media_asset (media_type, file_name, file_ext, mime_type, storage_url, crop_mode) VALUES ('IMAGE', ?, ?, ?, ?, 'AUTO')",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, fileName);
            ps.setString(2, ext);
            ps.setString(3, ext == null ? null : "image/" + ext);
            ps.setString(4, url);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void validatePublishStatus(String publishStatus) {
        if (publishStatus == null) {
            throw new IllegalArgumentException("publishStatus is required");
        }
        String normalized = publishStatus.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("DRAFT") && !normalized.equals("PUBLISHED") && !normalized.equals("OFFLINE")) {
            throw new IllegalArgumentException("publishStatus must be DRAFT, PUBLISHED or OFFLINE");
        }
    }

    private void validateCategoryPublish(CategoryUpsertRequest request) {
        if ("PUBLISHED".equals(request.publishStatus()) && (request.imageUrl() == null || request.imageUrl().isBlank())) {
            throw new IllegalArgumentException("category image is required before publishing");
        }
    }

    private void validateSeriesPublish(SeriesUpsertRequest request) {
        if (!"PUBLISHED".equals(request.publishStatus())) return;
        requirePublishedCategory(request.categoryId());
        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new IllegalArgumentException("series image is required before publishing");
        }
    }

    private void validateProductPublish(ProductUpsertRequest request) {
        if (!"PUBLISHED".equals(request.publishStatus())) return;
        requirePublishedCategory(request.categoryId());
        requirePublishedSeries(request.seriesId());
        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new IllegalArgumentException("product image is required before publishing");
        }
        if ((request.summaryZh() == null || request.summaryZh().isBlank()) && (request.summaryEn() == null || request.summaryEn().isBlank())) {
            throw new IllegalArgumentException("product summary is required before publishing");
        }
        if (request.parameters() == null || request.parameters().stream().noneMatch(
            item -> "PUBLISHED".equalsIgnoreCase(item.publishStatus()))) {
            throw new IllegalArgumentException("at least one published product parameter is required before publishing");
        }
    }

    private void requireCategoryPublishReady(long categoryId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM product_category c
            JOIN product_category_image i ON i.category_id = c.id
            WHERE c.id = ? AND c.deleted_at IS NULL AND c.name_zh <> '' AND c.name_en <> ''
            """, Integer.class, categoryId);
        if (count == null || count == 0) throw new IllegalArgumentException("category is missing required publishing content");
    }

    private void requireSeriesPublishReady(long seriesId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM product_series s
            JOIN product_category c ON c.id = s.category_id AND c.publish_status = 'PUBLISHED' AND c.deleted_at IS NULL
            JOIN product_series_image i ON i.series_id = s.id
            WHERE s.id = ? AND s.deleted_at IS NULL
            """, Integer.class, seriesId);
        if (count == null || count == 0) throw new IllegalArgumentException("series or parent category is missing required publishing content");
    }

    private void requireProductPublishReady(long productId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(DISTINCT p.id) FROM product p
            JOIN product_category c ON c.id = p.category_id AND c.publish_status = 'PUBLISHED' AND c.deleted_at IS NULL
            JOIN product_series s ON s.id = p.series_id AND s.publish_status = 'PUBLISHED' AND s.deleted_at IS NULL
            JOIN product_image i ON i.product_id = p.id
            JOIN product_parameter pp ON pp.product_id = p.id
            WHERE p.id = ? AND p.deleted_at IS NULL AND (NULLIF(TRIM(p.summary_zh), '') IS NOT NULL OR NULLIF(TRIM(p.summary_en), '') IS NOT NULL)
            """, Integer.class, productId);
        if (count == null || count == 0) throw new IllegalArgumentException("product or parent content is not ready for publishing");
    }

    private void requirePublishedCategory(long categoryId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_category WHERE id = ? AND publish_status = 'PUBLISHED' AND deleted_at IS NULL", Integer.class, categoryId);
        if (count == null || count == 0) throw new IllegalArgumentException("parent category must be published first");
    }

    private void requirePublishedSeries(long seriesId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_series WHERE id = ? AND publish_status = 'PUBLISHED' AND deleted_at IS NULL", Integer.class, seriesId);
        if (count == null || count == 0) throw new IllegalArgumentException("parent series must be published first");
    }

    private void requireCategory(long categoryId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_category WHERE id = ? AND deleted_at IS NULL", Integer.class, categoryId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("category not found");
        }
    }

    private void requireSeriesBelongsToCategory(long seriesId, long categoryId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_series WHERE id = ? AND category_id = ? AND deleted_at IS NULL", Integer.class, seriesId, categoryId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("series does not belong to category");
        }
    }

    private String emptyToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }
}

package com.qidian.site.service;

import com.qidian.site.dto.ApplicationAdminDtos.ApplicationUpsertRequest;
import com.qidian.site.dto.ApplicationAdminDtos.SeriesRelationUpdateRequest;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ApplicationAdminService {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listApplications() {
        return jdbcTemplate.queryForList("""
            SELECT a.id, a.slug, a.name_zh, a.name_en, a.overview_zh, a.overview_en,
                   a.sort_order, a.publish_status, COUNT(l.id) AS linked_series_count
            FROM application_field a
            LEFT JOIN application_series_link l ON l.application_id = a.id
            WHERE a.deleted_at IS NULL
            GROUP BY a.id, a.slug, a.name_zh, a.name_en, a.overview_zh, a.overview_en,
                     a.sort_order, a.publish_status
            ORDER BY a.sort_order ASC, a.id ASC
            """);
    }

    public long createApplication(ApplicationUpsertRequest request) {
        String publishStatus = normalizePublishStatus(request.publishStatus());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO application_field
                (slug, name_zh, name_en, overview_zh, overview_en, sort_order, publish_status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.slug());
            statement.setString(2, request.nameZh());
            statement.setString(3, request.nameEn());
            statement.setString(4, emptyToNull(request.overviewZh()));
            statement.setString(5, emptyToNull(request.overviewEn()));
            statement.setInt(6, request.sortOrder());
            statement.setString(7, publishStatus);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void updateApplication(long id, ApplicationUpsertRequest request) {
        String publishStatus = normalizePublishStatus(request.publishStatus());
        int affected = jdbcTemplate.update("""
            UPDATE application_field
            SET slug = ?, name_zh = ?, name_en = ?, overview_zh = ?, overview_en = ?,
                sort_order = ?, publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """,
            request.slug(), request.nameZh(), request.nameEn(),
            emptyToNull(request.overviewZh()), emptyToNull(request.overviewEn()),
            request.sortOrder(), publishStatus, id);
        if (affected == 0) throw new IllegalArgumentException("application field not found");
    }

    @Transactional
    public void deleteApplication(long id) {
        requireApplication(id);
        jdbcTemplate.update("DELETE FROM application_series_link WHERE application_id = ?", id);
        int affected = jdbcTemplate.update("""
            UPDATE application_field
            SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE'
            WHERE id = ? AND deleted_at IS NULL
            """, id);
        if (affected == 0) throw new IllegalArgumentException("application field not found");
    }

    public List<Map<String, Object>> listSeriesOptions() {
        return jdbcTemplate.queryForList("""
            SELECT s.id, s.category_id, c.name_zh AS category_name_zh, c.name_en AS category_name_en,
                   s.slug, s.name_zh, s.name_en, s.publish_status, s.sort_order
            FROM product_series s
            JOIN product_category c ON c.id = s.category_id
            WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL
            ORDER BY c.sort_order ASC, c.id ASC, s.sort_order ASC, s.id ASC
            """);
    }

    public List<Long> getLinkedSeriesIds(long applicationId) {
        requireApplication(applicationId);
        return jdbcTemplate.query("""
            SELECT series_id
            FROM application_series_link
            WHERE application_id = ?
            ORDER BY sort_order ASC, id ASC
            """, (rs, rowNum) -> rs.getLong("series_id"), applicationId);
    }

    @Transactional
    public void replaceLinkedSeries(long applicationId, SeriesRelationUpdateRequest request) {
        requireApplication(applicationId);
        Set<Long> seriesIds = new LinkedHashSet<>(request.seriesIds());
        if (seriesIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("seriesIds contains an invalid series id");
        }
        if (!seriesIds.isEmpty()) {
            Integer validCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM product_series s
                JOIN product_category c ON c.id = s.category_id
                WHERE s.deleted_at IS NULL AND c.deleted_at IS NULL AND s.id IN (%s)
                """.formatted(placeholders(seriesIds.size())), Integer.class, seriesIds.toArray());
            if (validCount == null || validCount != seriesIds.size()) {
                throw new IllegalArgumentException("one or more product series do not exist or have been deleted");
            }
        }

        jdbcTemplate.update("DELETE FROM application_series_link WHERE application_id = ?", applicationId);
        int sortOrder = 10;
        for (Long seriesId : seriesIds) {
            jdbcTemplate.update("""
                INSERT INTO application_series_link (application_id, series_id, sort_order, publish_status)
                VALUES (?, ?, ?, 'PUBLISHED')
                """, applicationId, seriesId, sortOrder);
            sortOrder += 10;
        }
    }

    private void requireApplication(long applicationId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM application_field WHERE id = ? AND deleted_at IS NULL", Integer.class, applicationId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("application field not found");
        }
    }

    private String placeholders(int count) {
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }

    private String normalizePublishStatus(String publishStatus) {
        if (publishStatus == null) throw new IllegalArgumentException("publishStatus is required");
        String normalized = publishStatus.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("DRAFT") && !normalized.equals("PUBLISHED") && !normalized.equals("OFFLINE")) {
            throw new IllegalArgumentException("publishStatus must be DRAFT, PUBLISHED or OFFLINE");
        }
        return normalized;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

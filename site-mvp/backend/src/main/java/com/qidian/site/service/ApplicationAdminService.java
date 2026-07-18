package com.qidian.site.service;

import com.qidian.site.dto.ApplicationAdminDtos.SeriesRelationUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
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
            GROUP BY a.id, a.slug, a.name_zh, a.name_en, a.overview_zh, a.overview_en,
                     a.sort_order, a.publish_status
            ORDER BY a.sort_order ASC, a.id ASC
            """);
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
            "SELECT COUNT(*) FROM application_field WHERE id = ?", Integer.class, applicationId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("application field not found");
        }
    }

    private String placeholders(int count) {
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }
}

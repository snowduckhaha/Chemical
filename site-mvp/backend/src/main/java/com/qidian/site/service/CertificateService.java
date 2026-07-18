package com.qidian.site.service;

import com.qidian.site.dto.CertificateAdminDtos.CertificateUpsertRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CertificateService {

    private static final Set<String> STATUSES = Set.of("DRAFT", "PUBLISHED", "OFFLINE");
    private final JdbcTemplate jdbcTemplate;

    public CertificateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> listAdmin() {
        return jdbcTemplate.queryForList("""
            SELECT id, certificate_no, name_zh, name_en, image_url, alt_zh, alt_en, sort_order, publish_status,
                   created_at, updated_at
            FROM certificate WHERE deleted_at IS NULL ORDER BY sort_order ASC, id ASC
            """);
    }

    public List<Map<String, Object>> listPublished(String lang) {
        String nameColumn = "en".equalsIgnoreCase(lang) ? "name_en" : "name_zh";
        String altColumn = "en".equalsIgnoreCase(lang) ? "alt_en" : "alt_zh";
        return jdbcTemplate.queryForList("""
            SELECT certificate_no, %s AS name, image_url, COALESCE(NULLIF(%s, ''), %s) AS alt_text, sort_order
            FROM certificate
            WHERE deleted_at IS NULL AND publish_status = 'PUBLISHED'
            ORDER BY sort_order ASC, id ASC
            """.formatted(nameColumn, altColumn, nameColumn));
    }

    public long create(CertificateUpsertRequest request) {
        validate(request);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO certificate
                (certificate_no, name_zh, name_en, image_url, alt_zh, alt_en, sort_order, publish_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            setValues(statement, request);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void update(long id, CertificateUpsertRequest request) {
        validate(request);
        int affected = jdbcTemplate.update("""
            UPDATE certificate SET certificate_no = ?, name_zh = ?, name_en = ?, image_url = ?,
                alt_zh = ?, alt_en = ?, sort_order = ?, publish_status = ?
            WHERE id = ? AND deleted_at IS NULL
            """, request.certificateNo(), request.nameZh(), request.nameEn(), request.imageUrl(),
            emptyToNull(request.altZh()), emptyToNull(request.altEn()), request.sortOrder(), request.publishStatus(), id);
        requireAffected(affected);
    }

    public void updateStatus(long id, String status) {
        validateStatus(status);
        requireAffected(jdbcTemplate.update(
            "UPDATE certificate SET publish_status = ? WHERE id = ? AND deleted_at IS NULL", status, id));
    }

    public void delete(long id) {
        requireAffected(jdbcTemplate.update(
            "UPDATE certificate SET deleted_at = CURRENT_TIMESTAMP, publish_status = 'OFFLINE' WHERE id = ? AND deleted_at IS NULL", id));
    }

    private void validate(CertificateUpsertRequest request) {
        validateStatus(request.publishStatus());
    }

    private void validateStatus(String status) {
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("invalid publish status");
    }

    private void setValues(PreparedStatement statement, CertificateUpsertRequest request) throws java.sql.SQLException {
        statement.setString(1, request.certificateNo()); statement.setString(2, request.nameZh());
        statement.setString(3, request.nameEn()); statement.setString(4, request.imageUrl());
        statement.setString(5, emptyToNull(request.altZh())); statement.setString(6, emptyToNull(request.altEn()));
        statement.setInt(7, request.sortOrder()); statement.setString(8, request.publishStatus());
    }

    private void requireAffected(int affected) {
        if (affected == 0) throw new IllegalArgumentException("certificate not found");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

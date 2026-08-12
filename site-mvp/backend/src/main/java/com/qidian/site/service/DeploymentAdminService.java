package com.qidian.site.service;

import com.qidian.site.dto.DeploymentAdminDtos.DeploymentRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeploymentAdminService {
    private final JdbcTemplate jdbcTemplate;

    public DeploymentAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> create(DeploymentRequest request, String requestedBy) {
        List<Map<String, Object>> activeJobs = jdbcTemplate.queryForList("""
            SELECT id, environment, source_ref, requested_by, requested_at, status,
                   resolved_commit, started_at, finished_at, log_excerpt, error_summary
            FROM deployment_job
            WHERE environment = 'PRODUCTION' AND status IN ('QUEUED', 'RUNNING')
            ORDER BY id DESC
            LIMIT 1
            """);
        if (!activeJobs.isEmpty()) {
            return activeJobs.get(0);
        }
        jdbcTemplate.update("""
            INSERT INTO deployment_job (environment, source_ref, requested_by, status)
            VALUES ('PRODUCTION', 'origin/main', ?, 'QUEUED')
            """, requestedBy);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return getRequired(id == null ? 0 : id);
    }

    public List<Map<String, Object>> listRecent() {
        return jdbcTemplate.queryForList("""
            SELECT id, environment, source_ref, requested_by, requested_at, status,
                   resolved_commit, started_at, finished_at, log_excerpt, error_summary
            FROM deployment_job
            ORDER BY id DESC
            LIMIT 20
            """);
    }

    public Map<String, Object> getRequired(long id) {
        List<Map<String, Object>> jobs = jdbcTemplate.queryForList("""
            SELECT id, environment, source_ref, requested_by, requested_at, status,
                   resolved_commit, started_at, finished_at, log_excerpt, error_summary
            FROM deployment_job WHERE id = ?
            """, id);
        if (jobs.isEmpty()) throw new IllegalArgumentException("deployment job not found");
        return jobs.get(0);
    }

    public void updateFromWorker(long id, String status, String resolvedCommit, String message) {
        getRequired(id);
        String normalizedStatus = status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalizedStatus.equals("RUNNING") && !normalizedStatus.equals("SUCCESS") && !normalizedStatus.equals("FAILED")) {
            throw new IllegalArgumentException("unsupported deployment status");
        }
        boolean completed = normalizedStatus.equals("SUCCESS") || normalizedStatus.equals("FAILED");
        jdbcTemplate.update("""
            UPDATE deployment_job
            SET status = ?,
                resolved_commit = COALESCE(?, resolved_commit),
                started_at = CASE WHEN ? = 'RUNNING' AND started_at IS NULL THEN CURRENT_TIMESTAMP ELSE started_at END,
                finished_at = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE finished_at END,
                log_excerpt = COALESCE(?, log_excerpt),
                error_summary = CASE WHEN ? = 'FAILED' THEN ? ELSE NULL END
            WHERE id = ?
            """, normalizedStatus, emptyToNull(resolvedCommit), normalizedStatus, completed,
            emptyToNull(message), normalizedStatus, emptyToNull(message), id);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

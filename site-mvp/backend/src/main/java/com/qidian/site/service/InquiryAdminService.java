package com.qidian.site.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InquiryAdminService {

    private static final Set<String> STATUSES = Set.of("NEW", "CONTACTED", "FOLLOWING_UP", "CLOSED");
    private final JdbcTemplate jdbcTemplate;

    public InquiryAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list(String status, String keyword, String from, String to) {
        StringBuilder sql = new StringBuilder("""
            SELECT i.id, i.inquiry_no, i.lang, i.name, i.company, i.email, i.phone, i.country,
                   i.interested_product, i.message, i.source_page, i.inquiry_status, i.internal_note,
                     i.first_contacted_at, i.lead_score, i.lead_level, i.created_at, i.updated_at,
                   CASE WHEN i.first_contacted_at IS NULL AND i.inquiry_status <> 'CLOSED'
                          AND i.created_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)
                         AND i.lead_score >= 40
                        THEN TRUE ELSE FALSE END AS overdue_high_intent
            FROM inquiry i
            WHERE 1 = 1
            """);
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            validateStatus(status);
            sql.append(" AND i.inquiry_status = ?");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (i.inquiry_no LIKE ? OR i.name LIKE ? OR i.company LIKE ? OR i.email LIKE ? OR i.phone LIKE ? OR i.interested_product LIKE ? OR i.message LIKE ?)");
            for (int index = 0; index < 7; index++) args.add(like);
        }
        if (from != null && !from.isBlank()) {
            sql.append(" AND i.created_at >= ?");
            args.add(LocalDate.parse(from).atStartOfDay());
        }
        if (to != null && !to.isBlank()) {
            sql.append(" AND i.created_at < ?");
            args.add(LocalDate.parse(to).plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY overdue_high_intent DESC, i.created_at DESC LIMIT 500");
        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> detail(long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            SELECT id, inquiry_no, lang, name, company, email, phone, country, interested_product,
                     message, source_page, inquiry_status, internal_note, first_contacted_at, lead_score, lead_level,
                     created_at, updated_at
            FROM inquiry WHERE id = ?
            """, id);
        if (rows.isEmpty()) throw new IllegalArgumentException("inquiry not found");
        Map<String, Object> result = rows.get(0);
        result.put("status_history", jdbcTemplate.queryForList("""
            SELECT from_status, to_status, operator_username, created_at
            FROM inquiry_status_history WHERE inquiry_id = ? ORDER BY created_at DESC, id DESC
            """, id));
        return result;
    }

    public byte[] exportCsv(String status, String keyword, String from, String to) {
        StringBuilder sql = new StringBuilder("""
            SELECT inquiry_no, name, company, email, phone, country, interested_product, message,
                   source_page, lang, inquiry_status, internal_note, lead_score, lead_level,
                   first_contacted_at, created_at, updated_at
            FROM inquiry WHERE 1 = 1
            """);
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            validateStatus(status);
            sql.append(" AND inquiry_status = ?");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (inquiry_no LIKE ? OR name LIKE ? OR company LIKE ? OR email LIKE ? OR phone LIKE ? OR interested_product LIKE ? OR message LIKE ?)");
            for (int index = 0; index < 7; index++) args.add(like);
        }
        if (from != null && !from.isBlank()) {
            sql.append(" AND created_at >= ?");
            args.add(LocalDate.parse(from).atStartOfDay());
        }
        if (to != null && !to.isBlank()) {
            sql.append(" AND created_at < ?");
            args.add(LocalDate.parse(to).plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray());
        StringBuilder csv = new StringBuilder("\uFEFF询盘编号,姓名,公司,邮箱,电话或WhatsApp,国家或地区,感兴趣产品,留言内容,来源页面,语言,状态,内部备注,意向评分,意向等级,首次联系时间,提交时间,更新时间\r\n");
        String[] columns = { "inquiry_no", "name", "company", "email", "phone", "country", "interested_product", "message",
            "source_page", "lang", "inquiry_status", "internal_note", "lead_score", "lead_level", "first_contacted_at", "created_at", "updated_at" };
        for (Map<String, Object> row : rows) {
            for (int index = 0; index < columns.length; index++) {
                if (index > 0) csv.append(',');
                csv.append(csvCell(row.get(columns[index])));
            }
            csv.append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void updateStatus(long id, String status, String operatorUsername) {
        validateStatus(status);
        List<String> current = jdbcTemplate.query(
            "SELECT inquiry_status FROM inquiry WHERE id = ? FOR UPDATE",
            (rs, rowNum) -> rs.getString(1), id);
        if (current.isEmpty()) throw new IllegalArgumentException("inquiry not found");
        String fromStatus = current.get(0);
        if (fromStatus.equals(status)) return;
        jdbcTemplate.update("""
            UPDATE inquiry
            SET inquiry_status = ?,
                first_contacted_at = CASE WHEN first_contacted_at IS NULL AND ? <> 'NEW' THEN CURRENT_TIMESTAMP ELSE first_contacted_at END
            WHERE id = ?
            """, status, status, id);
        jdbcTemplate.update("""
            INSERT INTO inquiry_status_history (inquiry_id, from_status, to_status, operator_username)
            VALUES (?, ?, ?, ?)
            """, id, fromStatus, status, operatorUsername);
    }

    public void updateNote(long id, String note) {
        int affected = jdbcTemplate.update("UPDATE inquiry SET internal_note = ? WHERE id = ?", emptyToNull(note), id);
        if (affected == 0) throw new IllegalArgumentException("inquiry not found");
    }

    private void validateStatus(String status) {
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("invalid inquiry status");
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String csvCell(Object value) {
        String text = value == null ? "" : value.toString();
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) text = "'" + text;
        return "\"" + text.replace("\"", "\"\"").replace("\r\n", "\n").replace('\r', '\n') + "\"";
    }
}

package com.qidian.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qidian.site.dto.AnalyticsDtos.EventRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AnalyticsService {
    private static final Set<String> EVENTS = Set.of("page_view", "product_view", "inquiry_cta_click", "inquiry_form_open", "inquiry_submit_success");
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AnalyticsService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean collect(EventRequest event, String userAgent) {
        if (!EVENTS.contains(event.eventName())) throw new IllegalArgumentException("invalid analytics event");
        if (!event.pagePath().startsWith("/") || event.pagePath().contains("?") || event.pagePath().contains("<")) {
            throw new IllegalArgumentException("invalid page path");
        }
        Instant now = Instant.now();
        Instant occurredAt = event.occurredAt() == null ? now : event.occurredAt();
        if (occurredAt.isBefore(now.minusSeconds(86_400)) || occurredAt.isAfter(now.plusSeconds(600))) {
            throw new IllegalArgumentException("invalid event time");
        }
        String payload = serializePayload(event.payload());
        try {
            jdbcTemplate.update("""
                INSERT INTO analytics_event
                (event_id, event_name, occurred_at, visitor_id, session_id, page_key, page_path, page_type,
                 referrer, utm_source, utm_medium, utm_campaign, source_channel, lang, country,
                 category_id, series_id, product_id, payload_json, user_agent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), ?)
                """, event.eventId(), event.eventName(), Timestamp.from(occurredAt), event.visitorId(), event.sessionId(),
                event.pageKey(), event.pagePath(), clean(event.pageType()), clean(event.referrer()), clean(event.utmSource()),
                clean(event.utmMedium()), clean(event.utmCampaign()), classify(event.utmMedium(), event.referrer()),
                clean(event.lang()), clean(event.country()), event.categoryId(), event.seriesId(), event.productId(), payload,
                truncate(userAgent, 500));
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    public Map<String, Object> overview(String from, String to, String lang, String channel) {
        QueryFilter filter = filter(from, to, lang, channel, "occurred_at");
        long visits = count("SELECT COUNT(*) FROM analytics_event WHERE event_name = 'page_view'" + filter.sql(), filter.args());
        QueryFilter inquiryFilter = filter(from, to, lang, null, "created_at");
        long inquiries = count("SELECT COUNT(*) FROM inquiry WHERE 1=1" + inquiryFilter.sql(), inquiryFilter.args());
        long valid = count("""
            SELECT COUNT(*) FROM inquiry WHERE email LIKE '%@%'
              AND (NULLIF(TRIM(company), '') IS NOT NULL OR NULLIF(TRIM(phone), '') IS NOT NULL OR NULLIF(TRIM(interested_product), '') IS NOT NULL)
            """ + inquiryFilter.sql(), inquiryFilter.args());
        Double average = jdbcTemplate.queryForObject("""
            SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE, created_at, first_contacted_at)), 0) FROM inquiry
            WHERE first_contacted_at IS NOT NULL
            """ + inquiryFilter.sql(), Double.class, inquiryFilter.args().toArray());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("visits", visits); result.put("inquiries", inquiries); result.put("validInquiries", valid);
        result.put("conversionRate", visits == 0 ? 0 : inquiries * 100.0 / visits);
        result.put("averageFirstContactMinutes", average == null ? 0 : average);
        return result;
    }

    public List<Map<String, Object>> funnel(String from, String to, String lang, String channel) {
        QueryFilter productFilter = filter(from, to, lang, channel, "occurred_at");
        QueryFilter ctaFilter = filter(from, to, lang, channel, "c.occurred_at");
        QueryFilter formFilter = filter(from, to, lang, channel, "f.occurred_at");
        QueryFilter submitFilter = filter(from, to, lang, channel, "s.occurred_at");
        List<Map<String, Object>> rows = new ArrayList<>();
        List<Long> counts = List.of(
            count("SELECT COUNT(DISTINCT session_id) FROM analytics_event WHERE event_name = 'product_view' AND session_id IS NOT NULL" + productFilter.sql(), productFilter.args()),
            count("""
                SELECT COUNT(DISTINCT c.session_id) FROM analytics_event c
                WHERE c.event_name = 'inquiry_cta_click' AND c.session_id IS NOT NULL
                  AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.payload_json, '$.cta_key')), '') <> 'contact_submit'
                """ + ctaFilter.sql() + """
                  AND EXISTS (
                    SELECT 1 FROM analytics_event p
                    WHERE p.event_name = 'product_view' AND p.session_id = c.session_id
                      AND p.occurred_at <= c.occurred_at
                  )
                """, ctaFilter.args()),
            count("""
                SELECT COUNT(DISTINCT f.session_id) FROM analytics_event f
                WHERE f.event_name = 'inquiry_form_open' AND f.session_id IS NOT NULL
                """ + formFilter.sql() + """
                  AND EXISTS (
                    SELECT 1 FROM analytics_event c
                    WHERE c.event_name = 'inquiry_cta_click' AND c.session_id = f.session_id
                      AND c.occurred_at <= f.occurred_at
                      AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.payload_json, '$.cta_key')), '') <> 'contact_submit'
                      AND EXISTS (
                        SELECT 1 FROM analytics_event p
                        WHERE p.event_name = 'product_view' AND p.session_id = c.session_id
                          AND p.occurred_at <= c.occurred_at
                      )
                  )
                """, formFilter.args()),
            count("""
                SELECT COUNT(DISTINCT s.session_id) FROM analytics_event s
                WHERE s.event_name = 'inquiry_submit_success' AND s.session_id IS NOT NULL
                """ + submitFilter.sql() + """
                  AND EXISTS (
                    SELECT 1 FROM analytics_event f
                    WHERE f.event_name = 'inquiry_form_open' AND f.session_id = s.session_id
                      AND f.occurred_at <= s.occurred_at
                      AND EXISTS (
                        SELECT 1 FROM analytics_event c
                        WHERE c.event_name = 'inquiry_cta_click' AND c.session_id = f.session_id
                          AND c.occurred_at <= f.occurred_at
                          AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.payload_json, '$.cta_key')), '') <> 'contact_submit'
                          AND EXISTS (
                            SELECT 1 FROM analytics_event p
                            WHERE p.event_name = 'product_view' AND p.session_id = c.session_id
                              AND p.occurred_at <= c.occurred_at
                          )
                      )
                  )
                """, submitFilter.args())
        );
        long previous = 0;
        int index = 0;
        for (String event : List.of("product_view", "inquiry_cta_click", "inquiry_form_open", "inquiry_submit_success")) {
            long value = counts.get(index++);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("eventName", event); row.put("count", value); row.put("conversionRate", previous == 0 ? (rows.isEmpty() ? 100 : 0) : value * 100.0 / previous);
            rows.add(row); previous = value;
        }
        return rows;
    }

    public List<Map<String, Object>> attribution(String dimension, String from, String to, String lang, String channel) {
        String column = switch (dimension) {
            case "page" -> "page_key";
            case "category" -> "CAST(category_id AS CHAR)";
            case "series" -> "CAST(series_id AS CHAR)";
            case "product" -> "CAST(product_id AS CHAR)";
            default -> "source_channel";
        };
        QueryFilter filter = filter(from, to, lang, channel, "occurred_at");
        return jdbcTemplate.queryForList("""
            SELECT COALESCE(%s, 'unknown') AS dimension_key,
                   SUM(event_name = 'page_view') AS visits,
                   SUM(event_name = 'inquiry_submit_success') AS inquiries
            FROM analytics_event WHERE 1=1 %s
            GROUP BY COALESCE(%s, 'unknown') ORDER BY inquiries DESC, visits DESC LIMIT 30
            """.formatted(column, filter.sql(), column), filter.args().toArray());
    }

    public List<Map<String, Object>> highIntent(String from, String to, String lang) {
        QueryFilter filter = filter(from, to, lang, null, "created_at");
        return jdbcTemplate.queryForList("""
            SELECT id, inquiry_no, name, company, email, phone, interested_product, source_page, inquiry_status,
                     first_contacted_at, created_at, lead_score, lead_level,
                   (first_contacted_at IS NULL AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS overdue
            FROM inquiry WHERE inquiry_status <> 'CLOSED' %s
            HAVING lead_score >= 40 ORDER BY overdue DESC, lead_score DESC, created_at DESC LIMIT 100
            """.formatted(filter.sql()), filter.args().toArray());
    }

    @Transactional
    public int rebuild(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate) || toDate.isAfter(LocalDate.now()) ||
            fromDate.isBefore(LocalDate.now().minusYears(2)) || toDate.toEpochDay() - fromDate.toEpochDay() > 366) {
            throw new IllegalArgumentException("invalid aggregation date range");
        }
        int days = 0;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            jdbcTemplate.update("DELETE FROM analytics_daily_aggregate WHERE stat_date = ?", date);
            jdbcTemplate.update("""
                INSERT INTO analytics_daily_aggregate
                  (stat_date, dimension_type, dimension_key, lang, source_channel, metric_name, metric_value)
                SELECT ?, 'EVENT', 'ALL', COALESCE(lang, ''), source_channel, event_name, COUNT(*)
                FROM analytics_event WHERE occurred_at >= ? AND occurred_at < ?
                GROUP BY COALESCE(lang, ''), source_channel, event_name
                """, date, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            jdbcTemplate.update("""
                INSERT INTO analytics_daily_aggregate
                  (stat_date, dimension_type, dimension_key, lang, source_channel, metric_name, metric_value)
                SELECT ?, 'PAGE', page_key, COALESCE(lang, ''), source_channel, event_name, COUNT(*)
                FROM analytics_event WHERE occurred_at >= ? AND occurred_at < ?
                GROUP BY page_key, COALESCE(lang, ''), source_channel, event_name
                """, date, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            days++;
        }
        return days;
    }

    private QueryFilter filter(String from, String to, String lang, String channel, String dateColumn) {
        LocalDate fromDate = from == null || from.isBlank() ? LocalDate.now().minusDays(29) : LocalDate.parse(from);
        LocalDate toDate = to == null || to.isBlank() ? LocalDate.now() : LocalDate.parse(to);
        if (toDate.isBefore(fromDate) || fromDate.isBefore(LocalDate.now().minusYears(2))) throw new IllegalArgumentException("invalid date range");
        StringBuilder sql = new StringBuilder(" AND " + dateColumn + " >= ? AND " + dateColumn + " < ?");
        List<Object> args = new ArrayList<>(List.of(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay()));
        if (lang != null && !lang.isBlank()) { sql.append(" AND lang = ?"); args.add(lang); }
        if (channel != null && !channel.isBlank() && "occurred_at".equals(dateColumn)) { sql.append(" AND source_channel = ?"); args.add(channel); }
        return new QueryFilter(sql.toString(), args);
    }

    private long count(String sql, List<Object> args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return value == null ? 0 : value;
    }

    private String classify(String medium, String referrer) {
        String value = medium == null ? "" : medium.toLowerCase(Locale.ROOT);
        if (value.matches(".*(cpc|ppc|paid|display).*")) return "ADVERTISING";
        if (value.matches(".*(social|facebook|linkedin|twitter|wechat).*")) return "SOCIAL";
        if (value.matches(".*(email|newsletter).*")) return "EMAIL";
        if (referrer == null || referrer.isBlank()) return "DIRECT";
        try {
            String host = URI.create(referrer).getHost();
            if (host == null) return "UNKNOWN";
            if (host.matches(".*(google|bing|baidu|yahoo|duckduckgo).*")) return "ORGANIC_SEARCH";
            return "REFERRAL";
        } catch (IllegalArgumentException ignored) { return "UNKNOWN"; }
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
            if (json.length() > 4000 || json.contains("<script") || json.contains("javascript:")) throw new IllegalArgumentException("invalid event payload");
            return json;
        } catch (JsonProcessingException exception) { throw new IllegalArgumentException("invalid event payload", exception); }
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String truncate(String value, int length) { return value == null ? null : value.substring(0, Math.min(value.length(), length)); }
    private record QueryFilter(String sql, List<Object> args) { }
}

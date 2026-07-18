package com.qidian.site.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

public final class AnalyticsDtos {
    private AnalyticsDtos() {
    }

    public record EventRequest(
        @JsonProperty("event_id") @NotBlank @Size(max = 64) String eventId,
        @JsonProperty("event_name") @NotBlank @Size(max = 64) String eventName,
        @JsonProperty("occurred_at") Instant occurredAt,
        @JsonProperty("visitor_id") @NotBlank @Size(max = 64) String visitorId,
        @JsonProperty("session_id") @NotBlank @Size(max = 64) String sessionId,
        @JsonProperty("page_key") @NotBlank @Size(max = 255) String pageKey,
        @JsonProperty("page_path") @NotBlank @Size(max = 500) String pagePath,
        @JsonProperty("page_type") @Size(max = 64) String pageType,
        @Size(max = 500) String referrer,
        @JsonProperty("utm_source") @Size(max = 128) String utmSource,
        @JsonProperty("utm_medium") @Size(max = 128) String utmMedium,
        @JsonProperty("utm_campaign") @Size(max = 255) String utmCampaign,
        @Size(max = 8) String lang,
        @Size(max = 128) String country,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("series_id") Long seriesId,
        @JsonProperty("product_id") Long productId,
        Map<String, Object> payload
    ) {
    }
}

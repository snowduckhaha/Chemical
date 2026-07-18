package com.qidian.site.controller;

import com.qidian.site.dto.AnalyticsDtos.EventRequest;
import com.qidian.site.dto.ApiResponse;
import com.qidian.site.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.time.LocalDate;

@RestController
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/api/v1/analytics/events")
    public ApiResponse<?> event(@Valid @RequestBody EventRequest event, HttpServletRequest request) {
        boolean created = analyticsService.collect(event, request.getHeader("User-Agent"));
        return ApiResponse.ok(Map.of("accepted", true, "created", created));
    }

    @RestController
    @RequestMapping("/api/v1/admin/analytics")
    static class AdminAnalyticsController {
        private final AnalyticsService analyticsService;

        AdminAnalyticsController(AnalyticsService analyticsService) {
            this.analyticsService = analyticsService;
        }

        @GetMapping("/overview")
        ApiResponse<?> overview(@RequestParam(required = false) String from, @RequestParam(required = false) String to,
                                @RequestParam(required = false) String lang, @RequestParam(required = false) String channel) {
            return ApiResponse.ok(analyticsService.overview(from, to, lang, channel));
        }

        @GetMapping("/funnel")
        ApiResponse<?> funnel(@RequestParam(required = false) String from, @RequestParam(required = false) String to,
                              @RequestParam(required = false) String lang, @RequestParam(required = false) String channel) {
            return ApiResponse.ok(analyticsService.funnel(from, to, lang, channel));
        }

        @GetMapping("/attribution")
        ApiResponse<?> attribution(@RequestParam(defaultValue = "channel") String dimension,
                                   @RequestParam(required = false) String from, @RequestParam(required = false) String to,
                                   @RequestParam(required = false) String lang, @RequestParam(required = false) String channel) {
            return ApiResponse.ok(analyticsService.attribution(dimension, from, to, lang, channel));
        }

        @GetMapping("/high-intent-inquiries")
        ApiResponse<?> highIntent(@RequestParam(required = false) String from, @RequestParam(required = false) String to,
                                  @RequestParam(required = false) String lang) {
            return ApiResponse.ok(analyticsService.highIntent(from, to, lang));
        }

        @PostMapping("/rebuild")
        ApiResponse<?> rebuild(@RequestParam String from, @RequestParam String to) {
            int days = analyticsService.rebuild(LocalDate.parse(from), LocalDate.parse(to));
            return ApiResponse.ok(Map.of("rebuiltDays", days, "from", from, "to", to));
        }
    }
}

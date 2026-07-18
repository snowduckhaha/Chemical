package com.qidian.site.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AnalyticsAggregationJob {
    private final AnalyticsService analyticsService;

    public AnalyticsAggregationJob(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Shanghai")
    public void aggregateYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        analyticsService.rebuild(yesterday, yesterday);
    }
}
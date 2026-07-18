package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.ApplicationAdminDtos.SeriesRelationUpdateRequest;
import com.qidian.site.service.ApplicationAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/applications")
public class ApplicationAdminController {

    private final ApplicationAdminService applicationAdminService;

    public ApplicationAdminController(ApplicationAdminService applicationAdminService) {
        this.applicationAdminService = applicationAdminService;
    }

    @GetMapping
    public ApiResponse<?> applications() {
        return ApiResponse.ok(applicationAdminService.listApplications());
    }

    @GetMapping("/series-options")
    public ApiResponse<?> seriesOptions() {
        return ApiResponse.ok(applicationAdminService.listSeriesOptions());
    }

    @GetMapping("/{id}/series")
    public ApiResponse<?> linkedSeries(@PathVariable long id) {
        return ApiResponse.ok(Map.of("seriesIds", applicationAdminService.getLinkedSeriesIds(id)));
    }

    @PutMapping("/{id}/series")
    public ApiResponse<?> replaceLinkedSeries(
        @PathVariable long id,
        @Valid @RequestBody SeriesRelationUpdateRequest request
    ) {
        applicationAdminService.replaceLinkedSeries(id, request);
        List<Long> seriesIds = applicationAdminService.getLinkedSeriesIds(id);
        return ApiResponse.ok(Map.of("applicationId", id, "seriesIds", seriesIds, "updated", true));
    }
}

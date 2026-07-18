package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.SeoAdminDtos.SeoUpsertRequest;
import com.qidian.site.service.SeoAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/seo")
public class SeoAdminController {

    private final SeoAdminService seoAdminService;

    public SeoAdminController(SeoAdminService seoAdminService) {
        this.seoAdminService = seoAdminService;
    }

    @GetMapping("/pages")
    public ApiResponse<?> pages() {
        return ApiResponse.ok(seoAdminService.listPageKeys());
    }

    @GetMapping
    public ApiResponse<?> get(@RequestParam String pageKey, @RequestParam String lang) {
        return ApiResponse.ok(seoAdminService.get(pageKey, lang));
    }

    @PutMapping
    public ApiResponse<?> update(
        @RequestParam String pageKey,
        @RequestParam String lang,
        @Valid @RequestBody SeoUpsertRequest request
    ) {
        seoAdminService.upsert(pageKey, lang, request);
        return ApiResponse.ok(Map.of("pageKey", pageKey, "lang", lang, "updated", true));
    }
}

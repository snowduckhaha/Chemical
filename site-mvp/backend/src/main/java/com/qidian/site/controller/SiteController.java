package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.SiteDtos.InquiryRequest;
import com.qidian.site.dto.SiteDtos.InquiryResult;
import com.qidian.site.service.SiteContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SiteController {

    private final SiteContentService siteContentService;

    public SiteController(SiteContentService siteContentService) {
        this.siteContentService = siteContentService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP"));
    }

    @GetMapping("/nav")
    public ApiResponse<?> nav(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNav(finalLang));
    }

    @GetMapping("/home")
    public ApiResponse<?> home(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getHomeSection(finalLang));
    }

    @GetMapping("/products/categories")
    public ApiResponse<?> productCategories(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getCategories(finalLang));
    }

    @GetMapping("/products/categories/{categorySlug}/series")
    public ApiResponse<?> productSeries(@RequestParam(defaultValue = "zh") String lang,
                                        @PathVariable String categorySlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getSeriesByCategory(finalLang, categorySlug));
    }

    @GetMapping("/products/categories/{categorySlug}/series/{seriesSlug}/products")
    public ApiResponse<?> productsBySeries(@RequestParam(defaultValue = "zh") String lang,
                                           @PathVariable String categorySlug,
                                           @PathVariable String seriesSlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getProductsBySeries(finalLang, categorySlug, seriesSlug));
    }

    @GetMapping("/products/categories/{categorySlug}/series/{seriesSlug}/products/{productSlug}")
    public ApiResponse<?> productDetail(@RequestParam(defaultValue = "zh") String lang,
                                        @PathVariable String categorySlug,
                                        @PathVariable String seriesSlug,
                                        @PathVariable String productSlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getProductDetail(finalLang, categorySlug, seriesSlug, productSlug));
    }

    @GetMapping("/products/search")
    public ApiResponse<?> productSearch(@RequestParam(defaultValue = "zh") String lang,
                                        @RequestParam String keyword) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.searchProducts(finalLang, keyword));
    }

    @GetMapping("/products/recommendations")
    public ApiResponse<?> productRecommendations(@RequestParam(defaultValue = "zh") String lang,
                                                 @RequestParam String sourceType,
                                                 @RequestParam long sourceId) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getRecommendations(finalLang, sourceType, sourceId));
    }

    @GetMapping("/applications")
    public ApiResponse<?> applications(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getApplications(finalLang));
    }

    @GetMapping("/applications/{applicationSlug}")
    public ApiResponse<?> applicationDetail(@RequestParam(defaultValue = "zh") String lang,
                                            @PathVariable String applicationSlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getApplicationDetail(finalLang, applicationSlug));
    }

    @GetMapping("/news")
    public ApiResponse<?> news(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNews(finalLang));
    }

    @GetMapping("/news/categories")
    public ApiResponse<?> newsCategories(@RequestParam(defaultValue = "zh") String lang) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNewsCategories(finalLang));
    }

    @GetMapping("/news/categories/{categorySlug}")
    public ApiResponse<?> newsByCategory(@RequestParam(defaultValue = "zh") String lang,
                                         @PathVariable String categorySlug,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "8") int pageSize) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNewsByCategory(finalLang, categorySlug, page, pageSize));
    }

    @GetMapping("/news/{categorySlug}/{articleSlug}")
    public ApiResponse<?> newsDetailByCategory(@RequestParam(defaultValue = "zh") String lang,
                                               @PathVariable String categorySlug,
                                               @PathVariable String articleSlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNewsDetail(finalLang, categorySlug, articleSlug));
    }

    @GetMapping("/news/{articleSlug}")
    public ApiResponse<?> newsDetail(@RequestParam(defaultValue = "zh") String lang,
                                     @PathVariable String articleSlug) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getNewsDetail(finalLang, articleSlug));
    }

    @GetMapping("/seo")
    public ApiResponse<?> seo(@RequestParam(defaultValue = "zh") String lang,
                              @RequestParam(defaultValue = "home") String pageKey) {
        String finalLang = siteContentService.normalizeLang(lang);
        return ApiResponse.ok(siteContentService.getSeoMeta(finalLang, pageKey));
    }

    @PostMapping("/inquiries")
    public ApiResponse<?> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResult result = siteContentService.createInquiry(request);
        return ApiResponse.ok(result);
    }
}

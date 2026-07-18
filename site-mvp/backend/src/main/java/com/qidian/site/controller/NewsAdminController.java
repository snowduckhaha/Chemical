package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.NewsAdminDtos.ArticleUpsertRequest;
import com.qidian.site.dto.NewsAdminDtos.CategoryUpsertRequest;
import com.qidian.site.dto.NewsAdminDtos.SortUpdateRequest;
import com.qidian.site.dto.NewsAdminDtos.StatusUpdateRequest;
import com.qidian.site.service.NewsAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/news")
public class NewsAdminController {
    private final NewsAdminService newsAdminService;

    public NewsAdminController(NewsAdminService newsAdminService) {
        this.newsAdminService = newsAdminService;
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(newsAdminService.listCategories(keyword, status, page, pageSize));
    }

    @GetMapping("/categories/options")
    public ApiResponse<?> categoryOptions() {
        return ApiResponse.ok(newsAdminService.categoryOptions());
    }

    @PostMapping("/categories")
    public ApiResponse<?> createCategory(@Valid @RequestBody CategoryUpsertRequest request) {
        return ApiResponse.ok(Map.of("id", newsAdminService.createCategory(request)));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<?> updateCategory(@PathVariable long id, @Valid @RequestBody CategoryUpsertRequest request) {
        newsAdminService.updateCategory(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PostMapping("/categories/{id}/status")
    public ApiResponse<?> updateCategoryStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        newsAdminService.updateCategoryStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @PostMapping("/categories/{id}/sort")
    public ApiResponse<?> updateCategorySort(@PathVariable long id, @Valid @RequestBody SortUpdateRequest request) {
        newsAdminService.updateCategorySort(id, request.sortOrder());
        return ApiResponse.ok(Map.of("id", id, "sortOrder", request.sortOrder()));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<?> deleteCategory(@PathVariable long id) {
        newsAdminService.deleteCategory(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }

    @GetMapping("/articles")
    public ApiResponse<?> articles(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(newsAdminService.listArticles(keyword, status, categoryId, page, pageSize));
    }

    @GetMapping("/articles/options")
    public ApiResponse<?> articleOptions(@RequestParam(required = false) Long excludeId) {
        return ApiResponse.ok(newsAdminService.articleOptions(excludeId));
    }

    @GetMapping("/products/options")
    public ApiResponse<?> productOptions() {
        return ApiResponse.ok(newsAdminService.productOptions());
    }

    @PostMapping("/articles")
    public ApiResponse<?> createArticle(@Valid @RequestBody ArticleUpsertRequest request) {
        return ApiResponse.ok(Map.of("id", newsAdminService.createArticle(request)));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<?> updateArticle(@PathVariable long id, @Valid @RequestBody ArticleUpsertRequest request) {
        newsAdminService.updateArticle(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PostMapping("/articles/{id}/status")
    public ApiResponse<?> updateArticleStatus(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        newsAdminService.updateArticleStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @PostMapping("/articles/{id}/sort")
    public ApiResponse<?> updateArticleSort(@PathVariable long id, @Valid @RequestBody SortUpdateRequest request) {
        newsAdminService.updateArticleSort(id, request.sortOrder());
        return ApiResponse.ok(Map.of("id", id, "sortOrder", request.sortOrder()));
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<?> deleteArticle(@PathVariable long id) {
        newsAdminService.deleteArticle(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }
}

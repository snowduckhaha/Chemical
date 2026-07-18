package com.qidian.site.controller;

import com.qidian.site.dto.AdminDtos.CategoryUpsertRequest;
import com.qidian.site.dto.AdminDtos.ProductUpsertRequest;
import com.qidian.site.dto.AdminDtos.SeriesUpsertRequest;
import com.qidian.site.dto.AdminDtos.SortUpdateRequest;
import com.qidian.site.dto.AdminDtos.StatusUpdateRequest;
import com.qidian.site.dto.ApiResponse;
import com.qidian.site.service.ProductAdminService;
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
@RequestMapping("/api/v1/admin/products")
public class ProductAdminController {

    private final ProductAdminService productAdminService;

    public ProductAdminController(ProductAdminService productAdminService) {
        this.productAdminService = productAdminService;
    }

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        return ApiResponse.ok(productAdminService.listCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<?> createCategory(@Valid @RequestBody CategoryUpsertRequest request) {
        long id = productAdminService.createCategory(request);
        return ApiResponse.ok(Map.of("id", id));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<?> updateCategory(@PathVariable long id, @Valid @RequestBody CategoryUpsertRequest request) {
        productAdminService.updateCategory(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PostMapping("/categories/{id}/sort")
    public ApiResponse<?> sortCategory(@PathVariable long id, @Valid @RequestBody SortUpdateRequest request) {
        productAdminService.updateCategorySort(id, request.sortOrder());
        return ApiResponse.ok(Map.of("id", id, "sortOrder", request.sortOrder()));
    }

    @PostMapping("/categories/{id}/status")
    public ApiResponse<?> statusCategory(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        productAdminService.updateCategoryStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<?> deleteCategory(@PathVariable long id) {
        productAdminService.deleteCategory(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }

    @GetMapping("/series")
    public ApiResponse<?> series(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(productAdminService.listSeries(categoryId));
    }

    @PostMapping("/series")
    public ApiResponse<?> createSeries(@Valid @RequestBody SeriesUpsertRequest request) {
        long id = productAdminService.createSeries(request);
        return ApiResponse.ok(Map.of("id", id));
    }

    @PutMapping("/series/{id}")
    public ApiResponse<?> updateSeries(@PathVariable long id, @Valid @RequestBody SeriesUpsertRequest request) {
        productAdminService.updateSeries(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PostMapping("/series/{id}/sort")
    public ApiResponse<?> sortSeries(@PathVariable long id, @Valid @RequestBody SortUpdateRequest request) {
        productAdminService.updateSeriesSort(id, request.sortOrder());
        return ApiResponse.ok(Map.of("id", id, "sortOrder", request.sortOrder()));
    }

    @PostMapping("/series/{id}/status")
    public ApiResponse<?> statusSeries(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        productAdminService.updateSeriesStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @DeleteMapping("/series/{id}")
    public ApiResponse<?> deleteSeries(@PathVariable long id) {
        productAdminService.deleteSeries(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }

    @GetMapping
    public ApiResponse<?> products(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long seriesId
    ) {
        return ApiResponse.ok(productAdminService.listProducts(categoryId, seriesId));
    }

    @PostMapping
    public ApiResponse<?> createProduct(@Valid @RequestBody ProductUpsertRequest request) {
        long id = productAdminService.createProduct(request);
        return ApiResponse.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProduct(@PathVariable long id, @Valid @RequestBody ProductUpsertRequest request) {
        productAdminService.updateProduct(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PostMapping("/{id}/sort")
    public ApiResponse<?> sortProduct(@PathVariable long id, @Valid @RequestBody SortUpdateRequest request) {
        productAdminService.updateProductSort(id, request.sortOrder());
        return ApiResponse.ok(Map.of("id", id, "sortOrder", request.sortOrder()));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<?> statusProduct(@PathVariable long id, @Valid @RequestBody StatusUpdateRequest request) {
        productAdminService.updateProductStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable long id) {
        productAdminService.deleteProduct(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }
}

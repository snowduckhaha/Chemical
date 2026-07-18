package com.qidian.site.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AdminDtos {

    public record CategoryUpsertRequest(
        @NotBlank String slug,
        @NotBlank String nameZh,
        @NotBlank String nameEn,
        String chemicalFormula,
        String summaryZh,
        String summaryEn,
        String applicationZh,
        String applicationEn,
        String seoTitle,
        String seoDescription,
        @NotNull Integer sortOrder,
        @NotBlank String publishStatus,
        String imageUrl,
        String imageAltZh,
        String imageAltEn
    ) {
    }

    public record SeriesUpsertRequest(
        @NotNull Long categoryId,
        @NotBlank String slug,
        @NotBlank String nameZh,
        @NotBlank String nameEn,
        String summaryZh,
        String summaryEn,
        String applicationZh,
        String applicationEn,
        String seoTitle,
        String seoDescription,
        @NotNull Integer sortOrder,
        @NotBlank String publishStatus,
        String imageUrl,
        String imageAltZh,
        String imageAltEn
    ) {
    }

    public record ParameterUpsertItem(
        @NotBlank String paramKey,
        @NotBlank String paramNameZh,
        @NotBlank String paramNameEn,
        @NotBlank String paramValueRaw,
        String unit,
        String testMethod,
        @NotNull Integer sortOrder,
        @NotBlank String publishStatus
    ) {
    }

    public record ProductUpsertRequest(
        @NotNull Long categoryId,
        @NotNull Long seriesId,
        @NotBlank String slug,
        @NotBlank String model,
        @NotBlank String nameZh,
        @NotBlank String nameEn,
        String summaryZh,
        String summaryEn,
        String detailZh,
        String detailEn,
        String applicationScenarioZh,
        String applicationScenarioEn,
        String seoTitle,
        String seoDescription,
        String packagingZh,
        String packagingEn,
        @NotNull Integer sortOrder,
        @NotBlank String publishStatus,
        String imageUrl,
        String imageAltZh,
        String imageAltEn,
        @NotEmpty @Valid List<ParameterUpsertItem> parameters
    ) {
    }

    public record SortUpdateRequest(@NotNull Integer sortOrder) {
    }

    public record StatusUpdateRequest(@NotBlank String publishStatus) {
    }
}

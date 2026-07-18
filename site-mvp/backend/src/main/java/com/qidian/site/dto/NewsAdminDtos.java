package com.qidian.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class NewsAdminDtos {
    private NewsAdminDtos() {
    }

    public record CategoryUpsertRequest(
        @NotBlank @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Size(max = 128) String slug,
        @NotBlank @Size(max = 255) String nameZh,
        @NotBlank @Size(max = 255) String nameEn,
        int sortOrder,
        @NotBlank String publishStatus
    ) {
    }

    public record ArticleUpsertRequest(
        @NotNull Long categoryId,
        @NotBlank @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Size(max = 128) String slug,
        @NotBlank @Size(max = 255) String titleZh,
        @NotBlank @Size(max = 255) String titleEn,
        @Size(max = 1000) String summaryZh,
        @Size(max = 1000) String summaryEn,
        String contentZh,
        String contentEn,
        @Size(max = 500) String coverImageUrl,
        @Size(max = 500) String coverAltZh,
        @Size(max = 500) String coverAltEn,
        @Size(max = 255) String seoTitle,
        @Size(max = 1000) String seoDescription,
        LocalDateTime publishedAt,
        int sortOrder,
        @NotBlank String publishStatus,
        List<Long> recommendedProductIds,
        List<Long> relatedArticleIds
    ) {
    }

    public record StatusUpdateRequest(@NotBlank String publishStatus) {
    }

    public record SortUpdateRequest(int sortOrder) {
    }
}

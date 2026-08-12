package com.qidian.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ApplicationAdminDtos {

    public record ApplicationUpsertRequest(
        @NotBlank @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") @Size(max = 128) String slug,
        @NotBlank @Size(max = 255) String nameZh,
        @NotBlank @Size(max = 255) String nameEn,
        @Size(max = 5000) String overviewZh,
        @Size(max = 5000) String overviewEn,
        int sortOrder,
        @NotBlank String publishStatus
    ) {
    }

    public record SeriesRelationUpdateRequest(@NotNull List<Long> seriesIds) {
    }
}

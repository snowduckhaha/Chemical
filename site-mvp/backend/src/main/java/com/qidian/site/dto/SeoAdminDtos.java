package com.qidian.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class SeoAdminDtos {

    private SeoAdminDtos() {
    }

    public record SeoUpsertRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @Size(max = 255) String ogTitle,
        @Size(max = 1000) String ogDescription,
        @Size(max = 500) String ogImage,
        @Size(max = 500) String canonical,
        @NotBlank String publishStatus
    ) {
    }
}

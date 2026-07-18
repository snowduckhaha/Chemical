package com.qidian.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CertificateAdminDtos {

    private CertificateAdminDtos() {
    }

    public record CertificateUpsertRequest(
        @NotBlank String certificateNo,
        @NotBlank String nameZh,
        @NotBlank String nameEn,
        @NotBlank String imageUrl,
        String altZh,
        String altEn,
        @NotNull Integer sortOrder,
        @NotBlank String publishStatus
    ) {
    }

    public record CertificateStatusUpdateRequest(@NotBlank String publishStatus) {
    }
}

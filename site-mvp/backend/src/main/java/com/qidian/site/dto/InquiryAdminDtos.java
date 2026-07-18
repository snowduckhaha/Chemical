package com.qidian.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class InquiryAdminDtos {

    private InquiryAdminDtos() {
    }

    public record InquiryStatusUpdateRequest(@NotBlank String status) {
    }

    public record InquiryNoteUpdateRequest(@Size(max = 10000) String internalNote) {
    }
}

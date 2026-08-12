package com.qidian.site.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public final class DeploymentAdminDtos {
    private DeploymentAdminDtos() {
    }

    public record DeploymentRequest(
        @AssertTrue(message = "confirmation is required") boolean confirmed
    ) {
    }

    public record DeploymentWorkerUpdateRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Pattern(regexp = "RUNNING|SUCCESS|FAILED") String status,
        @jakarta.validation.constraints.Pattern(regexp = "^[0-9a-f]{40}$") String resolvedCommit,
        @Size(max = 1000) String message
    ) {
    }
}

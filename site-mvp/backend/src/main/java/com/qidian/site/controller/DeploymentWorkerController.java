package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.DeploymentAdminDtos.DeploymentWorkerUpdateRequest;
import com.qidian.site.service.DeploymentAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/internal/deployments")
public class DeploymentWorkerController {
    private final DeploymentAdminService deploymentAdminService;
    private final String callbackToken;

    public DeploymentWorkerController(
        DeploymentAdminService deploymentAdminService,
        @Value("${deployment.callback-token:}") String callbackToken
    ) {
        this.deploymentAdminService = deploymentAdminService;
        this.callbackToken = callbackToken;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
        @PathVariable long id,
        @RequestHeader(value = "X-Deployment-Token", required = false) String providedToken,
        @Valid @RequestBody DeploymentWorkerUpdateRequest request
    ) {
        if (callbackToken.isBlank() || providedToken == null || !MessageDigest.isEqual(
            callbackToken.getBytes(StandardCharsets.UTF_8), providedToken.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(403, "access denied"));
        }
        deploymentAdminService.updateFromWorker(id, request.status(), request.resolvedCommit(), request.message());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

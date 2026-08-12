package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.DeploymentAdminDtos.DeploymentRequest;
import com.qidian.site.service.DeploymentAdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/deployments")
@PreAuthorize("hasRole('ADMIN') and authentication.name == 'zelin'")
public class DeploymentAdminController {
    private final DeploymentAdminService deploymentAdminService;

    public DeploymentAdminController(DeploymentAdminService deploymentAdminService) {
        this.deploymentAdminService = deploymentAdminService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(deploymentAdminService.listRecent());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody DeploymentRequest request, Authentication authentication) {
        return ApiResponse.ok(deploymentAdminService.create(request, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable long id) {
        return ApiResponse.ok(deploymentAdminService.getRequired(id));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<?> logs(@PathVariable long id) {
        return ApiResponse.ok(deploymentAdminService.getRequired(id));
    }
}

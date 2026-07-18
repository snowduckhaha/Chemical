package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.CertificateAdminDtos.CertificateStatusUpdateRequest;
import com.qidian.site.dto.CertificateAdminDtos.CertificateUpsertRequest;
import com.qidian.site.service.CertificateService;
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
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/api/v1/certificates")
    public ApiResponse<?> published(@RequestParam(defaultValue = "zh") String lang) {
        return ApiResponse.ok(certificateService.listPublished("en".equalsIgnoreCase(lang) ? "en" : "zh"));
    }

    @GetMapping("/api/v1/admin/certificates")
    public ApiResponse<?> listAdmin() {
        return ApiResponse.ok(certificateService.listAdmin());
    }

    @PostMapping("/api/v1/admin/certificates")
    public ApiResponse<?> create(@Valid @RequestBody CertificateUpsertRequest request) {
        return ApiResponse.ok(Map.of("id", certificateService.create(request)));
    }

    @PutMapping("/api/v1/admin/certificates/{id}")
    public ApiResponse<?> update(@PathVariable long id, @Valid @RequestBody CertificateUpsertRequest request) {
        certificateService.update(id, request);
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }

    @PutMapping("/api/v1/admin/certificates/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable long id, @Valid @RequestBody CertificateStatusUpdateRequest request) {
        certificateService.updateStatus(id, request.publishStatus());
        return ApiResponse.ok(Map.of("id", id, "publishStatus", request.publishStatus()));
    }

    @DeleteMapping("/api/v1/admin/certificates/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        certificateService.delete(id);
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }
}

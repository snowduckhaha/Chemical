package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.dto.InquiryAdminDtos.InquiryNoteUpdateRequest;
import com.qidian.site.dto.InquiryAdminDtos.InquiryStatusUpdateRequest;
import com.qidian.site.service.InquiryAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/inquiries")
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;

    public InquiryAdminController(InquiryAdminService inquiryAdminService) {
        this.inquiryAdminService = inquiryAdminService;
    }

    @GetMapping
    public ApiResponse<?> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        return ApiResponse.ok(inquiryAdminService.list(status, keyword, from, to));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> export(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        byte[] csv = inquiryAdminService.exportCsv(status, keyword, from, to);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inquiries.csv")
            .body(csv);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable long id) {
        return ApiResponse.ok(inquiryAdminService.detail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(
        @PathVariable long id,
        @Valid @RequestBody InquiryStatusUpdateRequest request,
        Authentication authentication
    ) {
        inquiryAdminService.updateStatus(id, request.status(), authentication.getName());
        return ApiResponse.ok(Map.of("id", id, "status", request.status()));
    }

    @PutMapping("/{id}/note")
    public ApiResponse<?> updateNote(@PathVariable long id, @Valid @RequestBody InquiryNoteUpdateRequest request) {
        inquiryAdminService.updateNote(id, request.internalNote());
        return ApiResponse.ok(Map.of("id", id, "updated", true));
    }
}

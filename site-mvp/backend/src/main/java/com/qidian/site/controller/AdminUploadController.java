package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import com.qidian.site.service.AdminUploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/uploads")
public class AdminUploadController {
    private final AdminUploadService adminUploadService;

    public AdminUploadController(AdminUploadService adminUploadService) {
        this.adminUploadService = adminUploadService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam(defaultValue = "PRODUCT") String scene
    ) {
        return ApiResponse.ok(adminUploadService.storeImage(file, scene));
    }
}
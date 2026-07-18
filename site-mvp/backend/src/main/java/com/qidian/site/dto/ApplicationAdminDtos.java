package com.qidian.site.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ApplicationAdminDtos {

    public record SeriesRelationUpdateRequest(@NotNull List<Long> seriesIds) {
    }
}

package com.qidian.site.dto;

import java.util.List;

public class ProductDto {

    private String slug;
    private String name;
    private String model;
    private String categorySlug;
    private String seriesSlug;
    private String summary;
    private List<ParameterItemDto> parameters;
    private List<String> applications;

    public ProductDto() {
    }

    public ProductDto(
            String slug,
            String name,
            String model,
            String categorySlug,
            String seriesSlug,
            String summary,
            List<ParameterItemDto> parameters,
            List<String> applications
    ) {
        this.slug = slug;
        this.name = name;
        this.model = model;
        this.categorySlug = categorySlug;
        this.seriesSlug = seriesSlug;
        this.summary = summary;
        this.parameters = parameters;
        this.applications = applications;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public String getSeriesSlug() {
        return seriesSlug;
    }

    public void setSeriesSlug(String seriesSlug) {
        this.seriesSlug = seriesSlug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ParameterItemDto> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterItemDto> parameters) {
        this.parameters = parameters;
    }

    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        this.applications = applications;
    }
}

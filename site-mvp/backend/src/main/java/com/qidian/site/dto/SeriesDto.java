package com.qidian.site.dto;

public class SeriesDto {

    private String slug;
    private String categorySlug;
    private String name;
    private String summary;

    public SeriesDto() {
    }

    public SeriesDto(String slug, String categorySlug, String name, String summary) {
        this.slug = slug;
        this.categorySlug = categorySlug;
        this.name = name;
        this.summary = summary;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}

package com.qidian.site.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class SiteDtos {

    public record NavItem(String key, String label, String path) {
    }

    public record HomeSection(String heroTitle,
                              String heroSubtitle,
                              String ctaText,
                              String ctaLink,
                              List<String> moduleOrder) {
    }

    public record ProductCategory(String slug,
                                  String name,
                                  String summary,
                                  String image,
                                  String formula,
                                  List<String> applications) {
    }

    public record ProductSeries(String slug,
                                String categorySlug,
                                String name,
                                String summary,
                                String image,
                                List<String> applications) {
    }

    public record Product(String slug,
                          String categorySlug,
                          String seriesSlug,
                          String name,
                          String model,
                          String summary,
                          String detail,
                          String image,
                          List<ParameterItem> parameters,
                          List<String> applications,
                          String packaging,
                          String publishStatus) {
    }

    public record ParameterItem(String label, String value) {
    }

    public record LinkedSeries(String categorySlug, String seriesSlug, String title, String summary, String image) {
    }

    public record Application(String slug,
                              String name,
                              String overview,
                              String image,
                              List<String> highlights,
                              List<LinkedSeries> linkedSeries,
                              List<String> faqs) {
    }

    public record NewsCategory(String slug, String name) {
    }

    public record NewsPage(List<News> items, long total, int page, int pageSize) {
    }

    public record News(String slug,
                       String title,
                       String summary,
                       String categorySlug,
                       String category,
                       String publishedAt,
                       String coverImage,
                       String coverAlt) {
    }

    public record NewsDetail(String slug,
                             String categorySlug,
                             String title,
                             String category,
                             String summary,
                             String publishedAt,
                             String content,
                             String coverImage,
                             String coverAlt,
                             List<String> relatedSlugs,
                             List<String> recommendedProductSlugs) {
    }

    public record SeoMeta(String pageKey, String title, String description, String ogTitle,
                          String ogDescription, String ogImage, String canonical) {
    }

    public record InquiryResult(String inquiryId, String status) {
    }

    public static class InquiryRequest {
        @NotBlank
        private String lang;

        @NotBlank
        private String name;

        private String company;

        @NotBlank
        @Email
        private String email;

        private String phone;

        private String country;

        private String interestedProduct;

        @NotBlank
        private String message;

        private String sourcePage;

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getInterestedProduct() {
            return interestedProduct;
        }

        public void setInterestedProduct(String interestedProduct) {
            this.interestedProduct = interestedProduct;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSourcePage() {
            return sourcePage;
        }

        public void setSourcePage(String sourcePage) {
            this.sourcePage = sourcePage;
        }
    }
}

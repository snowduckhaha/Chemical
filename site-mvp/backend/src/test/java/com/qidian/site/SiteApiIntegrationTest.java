package com.qidian.site;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpSession;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SiteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void productsCategoriesEndpointShouldReturnPublishedData() throws Exception {
        mockMvc.perform(get("/api/v1/products/categories").param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].slug").value("aluminum-hydroxide"));
    }

    @Test
    void productsSearchEndpointShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("lang", "zh")
                .param("keyword", "QD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void productsRecommendationEndpointShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/products/recommendations")
                .param("lang", "zh")
                .param("sourceType", "SERIES")
                .param("sourceId", "11"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void newsEndpointShouldReturnPublishedData() throws Exception {
        mockMvc.perform(get("/api/v1/news").param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].slug").isNotEmpty());
    }

    @Test
    void categoryAwareNewsEndpointsShouldReturnPublishedArticle() throws Exception {
        mockMvc.perform(get("/api/v1/news/categories/industry-trends")
                .param("lang", "zh").param("page", "1").param("pageSize", "8"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.items[0].categorySlug").value("industry-trends"));

        mockMvc.perform(get("/api/v1/news/industry-trends/ath-flame-retardant-advantages").param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.categorySlug").value("industry-trends"));

        mockMvc.perform(get("/api/v1/news/events/ath-flame-retardant-advantages").param("lang", "zh"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void inquiryEndpointShouldPersistRecordToDatabase() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@example.com";
        String body = """
            {
              "lang": "zh",
              "name": "Integration Test",
              "company": "Qidian QA",
              "email": "%s",
              "phone": "13800000000",
              "country": "CN",
              "interestedProduct": "PF-1",
              "message": "Please send datasheet",
              "sourcePage": "/zh/contact"
            }
            """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/v1/inquiries")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.inquiryId").isNotEmpty())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String inquiryId = root.path("data").path("inquiryId").asText();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM inquiry WHERE inquiry_no = ? AND email = ?",
            Integer.class,
            inquiryId,
            email
        );

        assertThat(count).isNotNull();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void unauthenticatedAdminRequestShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/categories"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void authenticatedMultipartUploadMustUseCsrfTokenReturnedByLoginSession() throws Exception {
        MockHttpSession session = loginAsUniqueAdmin();
        MockMultipartFile image = new MockMultipartFile(
            "file", "representative.jpg", MediaType.IMAGE_JPEG_VALUE, jpegImageBytes(800, 800)
        );

        mockMvc.perform(multipart("/api/v1/admin/uploads/images")
                .file(image)
                .session(session)
                .param("scene", "CATEGORY"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));

        CsrfContext csrf = csrfFor(session);
        mockMvc.perform(multipart("/api/v1/admin/uploads/images")
                .file(image)
                .session(session)
            .cookie(csrf.cookie())
            .header("X-XSRF-TOKEN", csrf.token())
                .param("scene", "CATEGORY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/uploads/")))
            .andExpect(jsonPath("$.data.width").value(800))
            .andExpect(jsonPath("$.data.height").value(800))
            .andExpect(jsonPath("$.data.displayMode").value("CONTAIN"))
            .andExpect(jsonPath("$.data.canvasRatio").value("4:3"))
            .andExpect(jsonPath("$.data.displayBytes").value(org.hamcrest.Matchers.lessThanOrEqualTo(1024 * 1024)));
    }

    @Test
    void authenticatedMultipartUploadRejectsInvalidImageContent() throws Exception {
        MockHttpSession session = loginAsUniqueAdmin();
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "not-an-image.jpg", MediaType.IMAGE_JPEG_VALUE, "not an image".getBytes()
        );
        CsrfContext csrf = csrfFor(session);

        mockMvc.perform(multipart("/api/v1/admin/uploads/images")
                .file(invalidFile)
                .session(session)
            .cookie(csrf.cookie())
            .header("X-XSRF-TOKEN", csrf.token()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("invalid image file"));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void operatorAnalyticsRequestShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/overview"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void operatorInquiryExportShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inquiries/export"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminInquiryExportShouldReturnUtf8Csv() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inquiries/export"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inquiries.csv")))
            .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCategoryCrudEndpointsShouldWork() throws Exception {
        String slug = "it-cat-" + UUID.randomUUID().toString().substring(0, 8);
        String createBody = """
            {
              "slug": "%s",
              "nameZh": "测试分类",
              "nameEn": "Test Category",
              "summaryZh": "测试",
              "summaryEn": "test",
              "sortOrder": 99,
              "publishStatus": "DRAFT"
            }
            """.formatted(slug);

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/products/categories")
            .with(csrf())
                .contentType("application/json")
                .content(createBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").isNumber())
            .andReturn();

        JsonNode createRoot = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = createRoot.path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/admin/products/categories/{id}/sort", id)
            .with(csrf())
                .contentType("application/json")
                .content("{" + "\"sortOrder\": 77}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/v1/admin/products/categories/{id}/status", id)
            .with(csrf())
                .contentType("application/json")
                .content("{" + "\"publishStatus\": \"PUBLISHED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_category WHERE id = ? AND publish_status = 'PUBLISHED' AND sort_order = 77",
            Integer.class,
            id
        );

        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminSeriesAndProductCrudEndpointsShouldWork() throws Exception {
        String categorySlug = "it-parent-" + UUID.randomUUID().toString().substring(0, 8);
        String createCategoryBody = """
            {
              "slug": "%s",
              "nameZh": "父分类",
              "nameEn": "Parent Category",
              "summaryZh": "父分类",
              "summaryEn": "parent",
              "sortOrder": 88,
              "publishStatus": "PUBLISHED"
            }
            """.formatted(categorySlug);

        MvcResult categoryResult = mockMvc.perform(post("/api/v1/admin/products/categories")
            .with(csrf())
                .contentType("application/json")
                .content(createCategoryBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        String seriesSlug = "it-series-" + UUID.randomUUID().toString().substring(0, 8);
        String createSeriesBody = """
            {
              "categoryId": %d,
              "slug": "%s",
              "nameZh": "测试系列",
              "nameEn": "Test Series",
              "summaryZh": "系列摘要",
              "summaryEn": "series summary",
              "imageUrl": "/uploads/integration/series-preview.jpg",
              "imageAltZh": "系列预览图",
              "imageAltEn": "Series preview",
              "sortOrder": 30,
              "publishStatus": "DRAFT"
            }
            """.formatted(categoryId, seriesSlug);

        MvcResult seriesResult = mockMvc.perform(post("/api/v1/admin/products/series")
            .with(csrf())
                .contentType("application/json")
                .content(createSeriesBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        long seriesId = objectMapper.readTree(seriesResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/products/series").param("categoryId", String.valueOf(categoryId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.id == %d)].image_url".formatted(seriesId))
                .value(org.hamcrest.Matchers.hasItem("/uploads/integration/series-preview.jpg")))
            .andExpect(jsonPath("$.data[?(@.id == %d)].image_alt_zh".formatted(seriesId))
                .value(org.hamcrest.Matchers.hasItem("系列预览图")));

        String updateSeriesBody = """
            {
              "categoryId": %d,
              "slug": "%s-updated",
              "nameZh": "测试系列更新",
              "nameEn": "Test Series Updated",
              "summaryZh": "系列摘要更新",
              "summaryEn": "series summary updated",
              "sortOrder": 31,
                            "publishStatus": "PUBLISHED"
            }
            """.formatted(categoryId, seriesSlug);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/products/series/{id}", seriesId)
            .with(csrf())
                .contentType("application/json")
                .content(updateSeriesBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/v1/admin/products/series/{id}/sort", seriesId)
            .with(csrf())
                .contentType("application/json")
                .content("{" + "\"sortOrder\": 44}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/v1/admin/products/series/{id}/status", seriesId)
            .with(csrf())
                .contentType("application/json")
                .content("{" + "\"publishStatus\": \"OFFLINE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        Integer seriesCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_series WHERE id = ? AND publish_status = 'OFFLINE' AND sort_order = 44",
            Integer.class,
            seriesId
        );

        assertThat(seriesCount).isNotNull();
        assertThat(seriesCount).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void softDeleteEndpointsShouldHideRecordsAndCascadeToDescendants() throws Exception {
        ProductHierarchy categoryHierarchy = insertProductHierarchy("category-delete");
        mockMvc.perform(delete("/api/v1/admin/products/categories/{id}", categoryHierarchy.categoryId()).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
        assertSoftDeleted("product_category", categoryHierarchy.categoryId());
        assertSoftDeleted("product_series", categoryHierarchy.seriesId());
        assertSoftDeleted("product", categoryHierarchy.productId());

        ProductHierarchy seriesHierarchy = insertProductHierarchy("series-delete");
        mockMvc.perform(delete("/api/v1/admin/products/series/{id}", seriesHierarchy.seriesId()).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
        assertThat(jdbcTemplate.queryForObject(
            "SELECT deleted_at IS NULL FROM product_category WHERE id = ?", Boolean.class, seriesHierarchy.categoryId()))
            .isTrue();
        assertSoftDeleted("product_series", seriesHierarchy.seriesId());
        assertSoftDeleted("product", seriesHierarchy.productId());

        ProductHierarchy productHierarchy = insertProductHierarchy("product-delete");
        mockMvc.perform(delete("/api/v1/admin/products/{id}", productHierarchy.productId()).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.deleted").value(true));
        assertThat(jdbcTemplate.queryForObject(
            "SELECT deleted_at IS NULL FROM product_series WHERE id = ?", Boolean.class, productHierarchy.seriesId()))
            .isTrue();
        assertSoftDeleted("product", productHierarchy.productId());

        mockMvc.perform(get("/api/v1/admin/products/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].id").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.hasItem((int) categoryHierarchy.categoryId()))));
        mockMvc.perform(get("/api/v1/admin/products/series"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].id").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.hasItem((int) seriesHierarchy.seriesId()))));
        mockMvc.perform(get("/api/v1/admin/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].id").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.hasItem((int) productHierarchy.productId()))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void softDeleteEndpointShouldRejectRequestWithoutCsrf() throws Exception {
        ProductHierarchy hierarchy = insertProductHierarchy("csrf-delete");

        mockMvc.perform(delete("/api/v1/admin/products/{id}", hierarchy.productId()))
            .andExpect(status().isForbidden());

        assertThat(jdbcTemplate.queryForObject(
            "SELECT deleted_at IS NULL FROM product WHERE id = ?", Boolean.class, hierarchy.productId()))
            .isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminProductListShouldReturnEditableDetailsAndParameters() throws Exception {
        ProductHierarchy hierarchy = insertProductHierarchy("details-list");
        jdbcTemplate.update("""
            UPDATE product
            SET detail_zh = '产品详情', detail_en = 'Product details',
                packaging_zh = '包装说明', packaging_en = 'Packaging'
            WHERE id = ?
            """, hierarchy.productId());
        jdbcTemplate.update("""
            INSERT INTO product_parameter
            (product_id, param_key, param_name_zh, param_name_en, param_value_raw, unit, test_method, sort_order, publish_status)
            VALUES (?, 'purity', '纯度', 'Purity', '≥ 99.5', '%', 'GB/T TEST', 10, 'PUBLISHED')
            """, hierarchy.productId());

        mockMvc.perform(get("/api/v1/admin/products").param("seriesId", String.valueOf(hierarchy.seriesId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].detail_zh").value("产品详情"))
            .andExpect(jsonPath("$.data[0].packaging_zh").value("包装说明"))
            .andExpect(jsonPath("$.data[0].parameters[0].param_key").value("purity"))
            .andExpect(jsonPath("$.data[0].parameters[0].param_name_zh").value("纯度"))
            .andExpect(jsonPath("$.data[0].parameters[0].param_value_raw").value("≥ 99.5"));
    }

    @Test
    void publicProductDetailShouldReturnIntroductionApplicationsAndPublishedParameters() throws Exception {
        ProductHierarchy hierarchy = insertProductHierarchy("public-detail");
        String categorySlug = jdbcTemplate.queryForObject(
            "SELECT slug FROM product_category WHERE id = ?", String.class, hierarchy.categoryId());
        String seriesSlug = jdbcTemplate.queryForObject(
            "SELECT slug FROM product_series WHERE id = ?", String.class, hierarchy.seriesId());
        String productSlug = jdbcTemplate.queryForObject(
            "SELECT slug FROM product WHERE id = ?", String.class, hierarchy.productId());
        jdbcTemplate.update("""
            UPDATE product
            SET detail_zh = '用于电子封装的复合硅微粉。',
                application_scenario_zh = '电子封装\n绝缘材料'
            WHERE id = ?
            """, hierarchy.productId());
        jdbcTemplate.update("""
            INSERT INTO product_parameter
            (product_id, param_key, param_name_zh, param_name_en, param_value_raw, sort_order, publish_status)
            VALUES (?, 'silica-content', '二氧化硅', 'Silica content', '60', 10, 'PUBLISHED')
            """, hierarchy.productId());

        mockMvc.perform(get("/api/v1/products/categories/{categorySlug}/series/{seriesSlug}/products/{productSlug}",
                categorySlug, seriesSlug, productSlug).param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.detail").value("用于电子封装的复合硅微粉。"))
            .andExpect(jsonPath("$.data.applications[0]").value("电子封装"))
            .andExpect(jsonPath("$.data.applications[1]").value("绝缘材料"))
            .andExpect(jsonPath("$.data.parameters[0].label").value("二氧化硅"))
            .andExpect(jsonPath("$.data.parameters[0].value").value("60"));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void inquiryAdminShouldFilterViewAndTrackFollowUp() throws Exception {
        String inquiryNo = "INQ-ADMIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        jdbcTemplate.update("""
            INSERT INTO inquiry
            (inquiry_no, lang, name, company, email, phone, country, interested_product, message, source_page, inquiry_status, publish_status)
            VALUES (?, 'zh', '询盘测试客户', '测试公司', 'inquiry-admin@example.com', '+86 13800000000', '中国',
                    'QD-F02', '这是一条长度超过三十个字符的高意向产品询盘留言，用于验证后台跟进功能。',
                    '/zh/products/category/series/product', 'NEW', 'PUBLISHED')
            """, inquiryNo);
        long inquiryId = jdbcTemplate.queryForObject("SELECT id FROM inquiry WHERE inquiry_no = ?", Long.class, inquiryNo);

        mockMvc.perform(get("/api/v1/admin/inquiries").param("status", "NEW").param("keyword", "inquiry-admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].inquiry_no").value(inquiryNo))
            .andExpect(jsonPath("$.data[0].lead_score").value(60));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                "/api/v1/admin/inquiries/{id}/status", inquiryId)
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONTACTED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CONTACTED"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                "/api/v1/admin/inquiries/{id}/note", inquiryId)
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"internalNote\":\"已通过邮件发送资料。\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/inquiries/{id}", inquiryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.inquiry_status").value("CONTACTED"))
            .andExpect(jsonPath("$.data.internal_note").value("已通过邮件发送资料。"))
            .andExpect(jsonPath("$.data.first_contacted_at").isNotEmpty())
            .andExpect(jsonPath("$.data.status_history[0].operator_username").value("operator"));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void inquiryMutationShouldRequireCsrf() throws Exception {
        Long inquiryId = jdbcTemplate.queryForObject("SELECT id FROM inquiry ORDER BY id LIMIT 1", Long.class);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                "/api/v1/admin/inquiries/{id}/status", inquiryId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void certificateAdminShouldControlPublicCertificateContent() throws Exception {
        String certificateNo = "CERT-IT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String createBody = """
            {
              "certificateNo": "%s",
              "nameZh": "集成测试证书",
              "nameEn": "Integration Certificate",
              "imageUrl": "/about/iso9001.png",
              "altZh": "集成测试证书图片",
              "altEn": "Integration certificate image",
              "sortOrder": 99,
              "publishStatus": "PUBLISHED"
            }
            """.formatted(certificateNo);
        MvcResult result = mockMvc.perform(post("/api/v1/admin/certificates").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
            .andExpect(status().isOk()).andReturn();
        long certificateId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/certificates").param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.certificate_no == '%s')].name".formatted(certificateNo))
                .value(org.hamcrest.Matchers.hasItem("集成测试证书")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                "/api/v1/admin/certificates/{id}/status", certificateId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"publishStatus\":\"OFFLINE\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/certificates").param("lang", "zh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.certificate_no == '%s')]".formatted(certificateNo)).isEmpty());

        mockMvc.perform(delete("/api/v1/admin/certificates/{id}", certificateId).with(csrf()))
            .andExpect(status().isOk());
        assertThat(jdbcTemplate.queryForObject(
            "SELECT deleted_at IS NOT NULL FROM certificate WHERE id = ?", Boolean.class, certificateId)).isTrue();
    }

    private MockHttpSession loginAsUniqueAdmin() throws Exception {
        String username = "upload-it-" + UUID.randomUUID().toString().substring(0, 12);
        String password = "Integration-Test-Password";
        jdbcTemplate.update("""
            INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
            VALUES (?, ?, 'ADMIN', TRUE, FALSE)
            """, username, passwordEncoder.encode(password));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private CsrfContext csrfFor(MockHttpSession session) throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/v1/admin/auth/csrf").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();
        Cookie cookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).as("CSRF endpoint must initialize the XSRF cookie").isNotNull();
        String token = objectMapper.readTree(csrfResult.getResponse().getContentAsString()).path("data").path("token").asText();
        return new CsrfContext(token, cookie);
    }

    private byte[] jpegImageBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLUE);
        graphics.fillOval(width / 4, height / 4, width / 2, height / 2);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }

    private ProductHierarchy insertProductHierarchy(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String categorySlug = prefix + "-category-" + suffix;
        jdbcTemplate.update("""
            INSERT INTO product_category (slug, name_zh, name_en, sort_order, publish_status)
            VALUES (?, '待删除分类', 'Delete Category', 999, 'PUBLISHED')
            """, categorySlug);
        long categoryId = jdbcTemplate.queryForObject(
            "SELECT id FROM product_category WHERE slug = ?", Long.class, categorySlug);

        String seriesSlug = prefix + "-series-" + suffix;
        jdbcTemplate.update("""
            INSERT INTO product_series (category_id, slug, name_zh, name_en, sort_order, publish_status)
            VALUES (?, ?, '待删除系列', 'Delete Series', 999, 'PUBLISHED')
            """, categoryId, seriesSlug);
        long seriesId = jdbcTemplate.queryForObject(
            "SELECT id FROM product_series WHERE category_id = ? AND slug = ?", Long.class, categoryId, seriesSlug);

        String productSlug = prefix + "-product-" + suffix;
        jdbcTemplate.update("""
            INSERT INTO product (category_id, series_id, slug, model, name_zh, name_en, sort_order, publish_status)
            VALUES (?, ?, ?, ?, '待删除产品', 'Delete Product', 999, 'PUBLISHED')
            """, categoryId, seriesId, productSlug, "DELETE-" + suffix);
        long productId = jdbcTemplate.queryForObject(
            "SELECT id FROM product WHERE series_id = ? AND slug = ?", Long.class, seriesId, productSlug);
        return new ProductHierarchy(categoryId, seriesId, productId);
    }

    private void assertSoftDeleted(String table, long id) {
        assertThat(table).isIn("product_category", "product_series", "product");
        Boolean deleted = jdbcTemplate.queryForObject(
            "SELECT deleted_at IS NOT NULL AND publish_status = 'OFFLINE' FROM " + table + " WHERE id = ?",
            Boolean.class,
            id);
        assertThat(deleted).isTrue();
    }

    private record ProductHierarchy(long categoryId, long seriesId, long productId) {
    }

    private record CsrfContext(String token, Cookie cookie) {
    }
}

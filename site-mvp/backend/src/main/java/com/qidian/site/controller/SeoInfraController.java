package com.qidian.site.controller;

import com.qidian.site.service.SiteContentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeoInfraController {

    private final SiteContentService siteContentService;

    @Value("${site.base-url:http://localhost:5173}")
    private String siteBaseUrl;

    public SeoInfraController(SiteContentService siteContentService) {
        this.siteContentService = siteContentService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        List<String> pathsZh = siteContentService.getSitemapPaths("zh");
        List<String> pathsEn = siteContentService.getSitemapPaths("en");
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">");

        pathsZh.forEach(path -> {
            xml.append("<url>");
            appendUrl(xml, path, "/zh", "/en");
            xml.append("<changefreq>weekly</changefreq>");
            xml.append("<priority>0.7</priority>");
            xml.append("</url>");
        });

        pathsEn.forEach(path -> {
            xml.append("<url>");
            appendUrl(xml, path, "/en", "/zh");
            xml.append("<changefreq>weekly</changefreq>");
            xml.append("<priority>0.7</priority>");
            xml.append("</url>");
        });

        xml.append("</urlset>");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots() {
        return "User-agent: *\n"
            + "Allow: /\n"
            + "Disallow: /zh/admin/\n"
            + "Disallow: /en/admin/\n"
            + "Disallow: /zh/search\n"
            + "Disallow: /en/search\n"
            + "Disallow: /zh/contact/success\n"
            + "Disallow: /en/contact/success\n"
            + "Sitemap: " + siteBaseUrl + "/sitemap.xml\n";
    }

    private void appendUrl(StringBuilder xml, String path, String ownLanguagePrefix, String alternateLanguagePrefix) {
        String alternatePath = alternateLanguagePrefix + path.substring(ownLanguagePrefix.length());
        xml.append("<loc>").append(escapeXml(siteBaseUrl + path)).append("</loc>");
        xml.append("<xhtml:link rel=\"alternate\" hreflang=\"")
            .append("/zh".equals(ownLanguagePrefix) ? "zh-CN" : "en")
            .append("\" href=\"").append(escapeXml(siteBaseUrl + path)).append("\"/>");
        xml.append("<xhtml:link rel=\"alternate\" hreflang=\"")
            .append("/zh".equals(alternateLanguagePrefix) ? "zh-CN" : "en")
            .append("\" href=\"").append(escapeXml(siteBaseUrl + alternatePath)).append("\"/>");
        xml.append("<xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
            .append(escapeXml(siteBaseUrl + alternatePath)).append("\"/>");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

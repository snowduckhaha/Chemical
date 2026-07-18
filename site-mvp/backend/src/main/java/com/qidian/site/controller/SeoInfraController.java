package com.qidian.site.controller;

import com.qidian.site.service.SiteContentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
        String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        pathsZh.forEach(path -> {
            xml.append("<url>");
            xml.append("<loc>").append(siteBaseUrl).append(path).append("</loc>");
            xml.append("<lastmod>").append(now).append("</lastmod>");
            xml.append("<changefreq>weekly</changefreq>");
            xml.append("<priority>0.7</priority>");
            xml.append("</url>");
        });

        pathsEn.forEach(path -> {
            xml.append("<url>");
            xml.append("<loc>").append(siteBaseUrl).append(path).append("</loc>");
            xml.append("<lastmod>").append(now).append("</lastmod>");
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
            + "Sitemap: " + siteBaseUrl + "/sitemap.xml\n";
    }
}

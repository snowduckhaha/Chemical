import type { NewsDetail, Product } from "../types/site";

export type JsonLdNode = Record<string, unknown>;

export const siteOrigin = (import.meta.env.SITE_ORIGIN || "https://www.origin-chemical.com").replace(/\/$/, "");

/** Stable graph node ids shared by every page so entities resolve to one node. */
export const ORGANIZATION_ID = `${siteOrigin}/#organization`;
export const WEBSITE_ID = `${siteOrigin}/#website`;

export function absoluteUrl(path: string | null | undefined): string | null {
  if (!path) return null;
  if (/^https?:\/\//.test(path)) return path;
  return new URL(path, siteOrigin).href;
}

/** Drop null/undefined/empty values so emitted JSON-LD never carries placeholders. */
function clean(node: JsonLdNode): JsonLdNode {
  const result: JsonLdNode = {};
  for (const [key, value] of Object.entries(node)) {
    if (value === null || value === undefined || value === "") continue;
    if (Array.isArray(value) && value.length === 0) continue;
    result[key] = value;
  }
  return result;
}

/**
 * Organization facts mirror the contact block rendered in the site footer;
 * keep both in sync when the business confirms new company facts.
 */
export function organizationSchema(lang: string): JsonLdNode {
  const zh = lang !== "en";
  return clean({
    "@context": "https://schema.org",
    "@type": "Organization",
    "@id": ORGANIZATION_ID,
    name: zh ? "深圳市起点化工有限公司" : "Shenzhen Origin Chemical Industry Co., Ltd.",
    url: siteOrigin,
    logo: absoluteUrl("/products/logo-symbol-footer.png"),
    email: "renee957888@gmail.com",
    telephone: "+86-13580598793",
    faxNumber: "00886-0769-85166074",
    description: zh
      ? "起点化工面向覆铜板、无卤阻燃、电线电缆与功能复合材料行业，提供氢氧化铝、硅粉、氧化铝及硅烷偶联剂产品与技术支持。"
      : "Origin Chemical supplies aluminum hydroxide, silica powder, alumina and silane coupling agents for copper clad laminate, halogen-free flame retardant, wire and cable and functional composite applications.",
    knowsAbout: zh
      ? ["氢氧化铝（ATH）", "覆铜板（CCL）", "无卤阻燃填料", "氧化铝粉末", "硅烷偶联剂"]
      : ["aluminum hydroxide (ATH)", "copper clad laminate (CCL)", "halogen-free flame retardant fillers", "alumina powder", "silane coupling agents"],
    address: {
      "@type": "PostalAddress",
      streetAddress: zh
        ? "前进路兴业公司4号楼2层212室"
        : "No.212, 2nd Floor, Building 4, Xingye Company, Qianjin Road",
      addressLocality: zh ? "深圳市" : "Shenzhen",
      addressRegion: zh ? "广东省" : "Guangdong",
      addressCountry: "CN"
    }
  });
}

export function webSiteSchema(lang: string): JsonLdNode {
  return {
    "@context": "https://schema.org",
    "@type": "WebSite",
    "@id": WEBSITE_ID,
    name: lang === "en" ? "Origin Chemical" : "起点化工",
    url: siteOrigin,
    inLanguage: lang === "en" ? "en" : "zh-CN",
    publisher: { "@id": ORGANIZATION_ID }
  };
}

export type BreadcrumbItem = { name: string; url?: string };

/** Mirrors the breadcrumb strip actually rendered on the page. */
export function breadcrumbSchema(pageUrl: string, items: BreadcrumbItem[]): JsonLdNode {
  return {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    "@id": `${pageUrl}#breadcrumb`,
    itemListElement: items.map((item, index) =>
      clean({
        "@type": "ListItem",
        position: index + 1,
        name: item.name,
        item: item.url
      })
    )
  };
}

/**
 * Quote-based B2B products use an Offer without a fabricated price. This
 * connects the purchasable product entity to Origin Chemical while keeping
 * the commercial flow truthful: visitors request a quotation or sample.
 */
export function productSchema(product: Product, pageUrl: string): JsonLdNode {
  return clean({
    "@context": "https://schema.org",
    "@type": "Product",
    "@id": `${pageUrl}#product`,
    name: product.name,
    sku: product.model || product.slug,
    description: product.summary,
    image: absoluteUrl(product.image),
    brand: { "@id": ORGANIZATION_ID },
    offers: {
      "@type": "Offer",
      url: pageUrl,
      availability: "https://schema.org/InStock",
      seller: { "@id": ORGANIZATION_ID },
      businessFunction: "https://purl.org/goodrelations/v1#Sell"
    },
    additionalProperty: product.parameters.map((parameter) =>
      clean({
        "@type": "PropertyValue",
        name: parameter.label,
        value: parameter.value,
        unitText: parameter.unit
      })
    )
  });
}

function isoDate(publishedAt: string): string | undefined {
  const date = publishedAt.trim().slice(0, 10);
  return /^\d{4}-\d{2}-\d{2}$/.test(date) ? date : undefined;
}

export function articleSchema(article: NewsDetail, pageUrl: string, lang: string): JsonLdNode {
  return clean({
    "@context": "https://schema.org",
    "@type": "Article",
    "@id": `${pageUrl}#article`,
    headline: article.title,
    description: article.summary,
    datePublished: isoDate(article.publishedAt),
    dateModified: isoDate(article.publishedAt),
    image: absoluteUrl(article.coverImage),
    inLanguage: lang === "en" ? "en" : "zh-CN",
    mainEntityOfPage: pageUrl,
    author: { "@id": ORGANIZATION_ID },
    publisher: { "@id": ORGANIZATION_ID }
  });
}

export type FaqEntry = { question: string; answer: string };

/** FAQPage mirrors the visible Q&A block on application-detail pages. */
export function faqPageSchema(entries: FaqEntry[], pageUrl: string, lang: string): JsonLdNode {
  return {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    "@id": `${pageUrl}#faq`,
    inLanguage: lang === "en" ? "en" : "zh-CN",
    mainEntity: entries.map((entry) => ({
      "@type": "Question",
      name: entry.question,
      acceptedAnswer: { "@type": "Answer", text: entry.answer }
    }))
  };
}

export const COMPANY_VIDEO_URL = "/about/company-introduction-video.mp4";
export const COMPANY_VIDEO_POSTER = "/about/company-video-cover.webp";
export const COMPANY_VIDEO_DURATION = "PT2M20S";
export const COMPANY_VIDEO_UPLOAD_DATE = "2026-09-22";

/**
 * VideoObject for the self-hosted company introduction video embedded on the
 * home page. The English name keeps the legal entity name in the
 * first sentence pattern used by the page transcript.
 */
export function videoSchema(lang: string, pageUrl: string): JsonLdNode {
  const zh = lang !== "en";
  return clean({
    "@context": "https://schema.org",
    "@type": "VideoObject",
    "@id": `${siteOrigin}/about/company-introduction-video.mp4#video`,
    name: zh ? "起点化工公司与产品介绍" : "Origin Chemical Company and Product Introduction",
    description: zh
      ? "通过约两分钟的视频，了解起点化工的功能性无机材料产品、关联制造与检测场景，以及面向全球客户的技术和供应支持。"
      : "Watch this two-minute overview of Origin Chemical's functional inorganic materials, affiliated manufacturing and testing operations, and technical and supply support for global customers.",
    thumbnailUrl: [absoluteUrl(COMPANY_VIDEO_POSTER)],
    uploadDate: COMPANY_VIDEO_UPLOAD_DATE,
    duration: COMPANY_VIDEO_DURATION,
    contentUrl: absoluteUrl(COMPANY_VIDEO_URL),
    embedUrl: pageUrl,
    inLanguage: zh ? "zh-CN" : "en",
    publisher: { "@id": ORGANIZATION_ID }
  });
}

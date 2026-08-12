<template>
  <div class="about-page">
    <section class="about-hero" aria-label="About page banner"></section>

    <nav class="breadcrumb-bar" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <router-link :to="`/${lang}`" class="home-icon" aria-label="Home">⌂</router-link>
        <router-link :to="`/${lang}`">{{ text.breadcrumbHome }}</router-link>
        <span>/</span>
        <span>{{ text.breadcrumbCurrent }}</span>
      </div>
    </nav>

    <section class="profile-section section-shell">
      <div class="profile-grid">
        <div class="profile-media">
          <img src="/about/company-facility.png" :alt="text.profileImageAlt" />
        </div>
        <div class="profile-copy-panel">
          <p class="section-kicker">{{ text.profileKicker }}</p>
          <h1>{{ text.profileTitle }}</h1>
          <div class="profile-copy">
            <p v-for="paragraph in text.profileParagraphs" :key="paragraph">{{ paragraph }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="capability-section section-shell" aria-labelledby="capability-title">
      <div class="section-head">
        <p class="section-kicker">CAPABILITIES</p>
        <h2 id="capability-title">{{ text.capabilityTitle }}</h2>
      </div>

      <div class="capability-grid">
        <article v-for="item in text.capabilities" :key="item.title" class="capability-card">
          <span class="capability-mark">{{ item.index }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section class="quality-section section-shell">
      <div class="quality-media">
        <img src="/about/quality-lab.png" :alt="text.qualityImageAlt" />
      </div>
      <div class="quality-copy">
        <p class="section-kicker">QUALITY & DELIVERY</p>
        <h2>{{ text.qualityTitle }}</h2>
        <p v-for="paragraph in text.qualityParagraphs" :key="paragraph">{{ paragraph }}</p>
        <div class="quality-tags">
          <span v-for="tag in text.qualityTags" :key="tag">{{ tag }}</span>
        </div>
      </div>
    </section>

    <section class="about-cta">
      <div class="section-shell cta-inner">
        <div>
          <p class="section-kicker">CONTACT</p>
          <h2>{{ text.ctaTitle }}</h2>
          <p>{{ text.ctaDescription }}</p>
        </div>
        <div class="cta-actions">
          <router-link class="cta-primary" :to="`/${lang}/contact`">{{ text.ctaPrimary }}</router-link>
          <router-link class="cta-secondary" :to="`/${lang}/products`">{{ text.ctaSecondary }}</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useJsonLd } from "../composables/useJsonLd";
import { breadcrumbSchema, organizationSchema, siteOrigin } from "../lib/jsonLd";

const route = useRoute();
const lang = computed(() => (String(route.params.lang || "zh") === "en" ? "en" : "zh"));

const text = computed(() => {
  if (lang.value === "en") {
    return {
      breadcrumbHome: "Home",
      breadcrumbCurrent: "Company Profile",
      profileKicker: "INTRODUCTION",
      profileImageAlt: "Origin Chemical factory and production environment",
      profileTitle: "About Us",
      profileParagraphs: [
        "Shenzhen Origin Chemical Co., Ltd. focuses on aluminum hydroxide and related advanced inorganic materials for copper clad laminate and functional material applications. We provide reliable product options and technical support for halogen-free flame retardancy, functional fillers and stable sourcing.",
        "Our products serve electronic materials, engineering plastics, wire and cable, and functional composite applications. With coordinated manufacturing resources, quality inspection capability and transit warehouse coverage, we help customers reduce sourcing risk and improve material evaluation efficiency.",
        "For global B2B customers, Origin Chemical is committed to clear product structures, traceable quality control and timely business response, becoming a long-term partner in flame-retardant and functional inorganic materials."
      ],
      capabilityTitle: "Material Capability For Stable Sourcing",
      capabilities: [
        {
          index: "01",
          title: "Focused Material Portfolio",
          description: "Covering aluminum hydroxide, silica powder, alumina powder and silane coupling agent for flame-retardant and functional applications."
        },
        {
          index: "02",
          title: "Stable Quality Output",
          description: "We focus on key indicators, batch consistency and inspection records to support evaluation and repeat sourcing."
        },
        {
          index: "03",
          title: "Faster Delivery Response",
          description: "Transit warehouses in Changzhou, Yichun and Dongguan support faster fulfillment after order confirmation."
        },
        {
          index: "04",
          title: "Technical Communication",
          description: "We help match product series and documents based on application targets, formulation systems and performance needs."
        }
      ],
      qualityImageAlt: "Laboratory quality control environment",
      qualityTitle: "Quality Control And Supply Coordination",
      qualityParagraphs: [
        "We care about continuous stability from production and inspection to delivery. Key indicators such as particle size, whiteness, oil absorption, moisture and sieve residue are used as the basis for quality communication.",
        "With transit warehouse coverage in Changzhou, Yichun and Dongguan, Origin Chemical supports faster response for key regional customers while balancing stable supply and delivery efficiency."
      ],
      qualityTags: ["ISO 9001 / ISO 14001", "3 Transit Warehouses", "Fast Sample Response"],
      ctaTitle: "Need A Material Recommendation For Your Application?",
      ctaDescription: "Share your target application, performance requirements and expected quantity. Our team will help recommend suitable product series and documents.",
      ctaPrimary: "Get A Quote",
      ctaSecondary: "View Products"
    };
  }

  return {
    breadcrumbHome: "首页",
    breadcrumbCurrent: "公司简介",
    profileKicker: "介绍",
    profileImageAlt: "起点化工工厂与生产环境",
    profileTitle: "关于我们",
    profileParagraphs: [
      "深圳市起点化工有限公司专注于覆铜板行业用氢氧化铝及相关新材料，围绕无卤阻燃、功能性填料和稳定供应，为客户提供可靠的产品选择与技术支持。",
      "公司长期服务于电子材料、工程塑料、电线电缆及功能复合材料等应用场景，重视产品稳定性、批次一致性与交付效率。依托生产制造协同、质量检测能力和多地中转仓布局，我们帮助客户降低采购风险，提升材料评估与导入效率。",
      "面向全球 B2B 客户，起点化工坚持以清晰的产品体系、可追溯的质量控制和及时的业务响应，成为客户在阻燃与功能性无机材料领域的长期合作伙伴。"
    ],
    capabilityTitle: "稳定采购所需的材料能力",
    capabilities: [
      {
        index: "01",
        title: "专注材料方向",
        description: "围绕氢氧化铝、硅粉、氧化铝粉末、硅烷偶联剂等系列，服务多类阻燃与功能化应用。"
      },
      {
        index: "02",
        title: "稳定质量输出",
        description: "重视关键指标、批次稳定性与检测记录，为客户评估和持续采购提供基础保障。"
      },
      {
        index: "03",
        title: "快速交付响应",
        description: "中转仓覆盖江苏常州、江西宜春、广东东莞，正常接单后中国大陆三天内可送达客户仓库。"
      },
      {
        index: "04",
        title: "技术沟通支持",
        description: "根据目标应用、配方体系和性能需求，协助客户匹配合适产品系列与资料。"
      }
    ],
    qualityImageAlt: "实验室质量检测环境",
    qualityTitle: "质量控制与供应链协同",
    qualityParagraphs: [
      "我们关注产品从生产、检测到交付的连续稳定性，围绕粒径、白度、吸油值、附着水、筛余等关键指标建立质量沟通基础，帮助客户更高效地完成样品评估和批量导入。",
      "结合常州、宜春、东莞等中转仓布局，起点化工能够为重点区域客户提供更快的响应节奏，在稳定供应和交付效率之间取得平衡。"
    ],
    qualityTags: ["ISO 9001 / ISO 14001", "3 个中转仓", "快速样品响应"],
    ctaTitle: "需要了解适合您应用的材料方案？",
    ctaDescription: "告诉我们您的目标应用、性能要求和预计用量，我们将协助推荐合适的产品系列与资料。",
    ctaPrimary: "索取报价",
    ctaSecondary: "查看产品"
  };
});

const pageUrl = computed(() => `${siteOrigin}/${lang.value}/about`);
useJsonLd(computed(() => [
  organizationSchema(lang.value),
  breadcrumbSchema(pageUrl.value, [
    { name: text.value.breadcrumbHome, url: `${siteOrigin}/${lang.value}` },
    { name: text.value.breadcrumbCurrent }
  ])
]));
</script>

<style scoped>
:global(html),
:global(body) {
  overflow-x: hidden;
}

.about-page {
  width: 100%;
  color: var(--pf-ink);
}

.about-hero {
  width: 100vw;
  min-height: clamp(220px, 31vw, 430px);
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0)),
    url("/about/about-banner.png") center/cover no-repeat;
}

.breadcrumb-bar {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #169bd7;
  color: #ffffff;
}

.breadcrumb-inner {
  max-width: var(--site-width);
  min-height: 64px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
  font-weight: 600;
}

.breadcrumb-inner a {
  color: #ffffff;
  text-decoration: none;
}

.home-icon {
  width: 28px;
  height: 28px;
  border: 1px solid rgba(255, 255, 255, 0.52);
  display: inline-grid;
  place-items: center;
  line-height: 1;
}

.section-shell {
  width: min(100%, var(--site-width));
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.section-kicker {
  margin: 0 0 9px;
  color: #169bd7;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.section-head {
  display: grid;
  gap: 8px;
  margin-bottom: 26px;
}

.section-head h2,
.profile-section h1,
.quality-copy h2,
.about-cta h2 {
  margin: 0;
  color: #18314f;
  letter-spacing: 0;
}

.section-head h2,
.quality-copy h2,
.about-cta h2 {
  font-size: clamp(1.65rem, 2.3vw, 2.25rem);
  line-height: 1.22;
}

.profile-section {
  padding-top: clamp(72px, 8vw, 96px);
  padding-bottom: 66px;
  background: #ffffff;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.92fr) minmax(360px, 1fr);
  gap: clamp(32px, 5vw, 72px);
  align-items: center;
}

.profile-media {
  overflow: hidden;
  background: #eef4f9;
}

.profile-media img {
  width: 100%;
  aspect-ratio: 4 / 3;
  display: block;
  object-fit: cover;
}

.profile-section h1 {
  font-size: clamp(2rem, 3vw, 2.6rem);
  line-height: 1.18;
}

.profile-copy {
  max-width: 760px;
  margin-top: 26px;
  display: grid;
  gap: 16px;
}

.profile-copy p,
.quality-copy p,
.capability-card p,
.about-cta p {
  margin: 0;
  color: var(--pf-muted);
  line-height: 1.78;
  font-size: 1rem;
}

.capability-section {
  padding-top: 24px;
  padding-bottom: 68px;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.capability-card {
  min-height: 206px;
  padding: 23px 22px;
  background: #ffffff;
  border: 1px solid #dfe9f4;
  border-radius: 6px;
  display: grid;
  align-content: start;
  gap: 12px;
  transition: border-color 0.18s ease, transform 0.18s ease;
}

.capability-card:hover {
  border-color: #169bd7;
  transform: translateY(-2px);
}

.capability-mark {
  color: #169bd7;
  font-size: 0.82rem;
  font-weight: 700;
}

.capability-card h3 {
  margin: 0;
  color: #18314f;
  font-size: 1.08rem;
  line-height: 1.35;
}

.quality-section {
  padding-top: 28px;
  padding-bottom: 72px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.85fr);
  gap: clamp(26px, 4vw, 56px);
  align-items: center;
}

.quality-media {
  overflow: hidden;
  border-radius: 4px;
  border: 1px solid #dfe9f4;
  background: #ffffff;
}

.quality-media img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  display: block;
}

.quality-copy {
  display: grid;
  gap: 14px;
}

.quality-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 6px;
}

.quality-tags span {
  padding: 8px 11px;
  border: 1px solid #cfe0f1;
  background: #ffffff;
  color: #1e5d9c;
  font-size: 0.88rem;
  font-weight: 700;
}

.about-cta {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #0f4aa1;
  color: #ffffff;
}

.cta-inner {
  padding-top: 50px;
  padding-bottom: 52px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
}

.about-cta .section-kicker,
.about-cta h2,
.about-cta p {
  color: #ffffff;
}

.about-cta p {
  max-width: 760px;
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.82);
}

.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.cta-primary,
.cta-secondary {
  min-width: 120px;
  text-align: center;
  text-decoration: none;
  padding: 10px 15px;
  border-radius: 3px;
  font-size: 0.92rem;
  font-weight: 700;
}

.cta-primary {
  color: #0f4aa1;
  background: #ffffff;
}

.cta-secondary {
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.68);
}

@media (max-width: 980px) {
  .capability-grid,
  .quality-section,
  .profile-grid,
  .cta-inner {
    grid-template-columns: 1fr;
  }

  .cta-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 680px) {
  .about-hero {
    min-height: 230px;
  }

  .breadcrumb-inner {
    min-height: 48px;
    padding: 0 18px;
    font-size: 0.92rem;
  }

  .section-shell {
    padding-left: 18px;
    padding-right: 18px;
  }

  .profile-section {
    padding-top: 52px;
  }

  .capability-card {
    min-height: auto;
  }

  .quality-section {
    padding-top: 48px;
    padding-bottom: 52px;
  }

  .profile-grid {
    gap: 24px;
  }

  .cta-primary,
  .cta-secondary {
    width: 100%;
  }
}
</style>

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

    <section id="company-video" class="video-section section-shell" aria-labelledby="video-title">
      <div class="section-head">
        <p class="section-kicker">VIDEO</p>
        <h2 id="video-title">{{ text.videoTitle }}</h2>
        <p class="video-intro">{{ text.videoIntro }}</p>
      </div>
      <CompanyVideo class="about-company-video" :caption="text.videoCaption" />
    </section>

    <section class="faq-section section-shell" aria-labelledby="faq-title">
      <div class="section-head">
        <p class="section-kicker">FAQ</p>
        <h2 id="faq-title">{{ text.faqTitle }}</h2>
      </div>
      <div class="faq-list">
        <article v-for="item in text.faqEntries" :key="item.question" class="faq-item">
          <h3>{{ item.question }}</h3>
          <p>{{ item.answer }}</p>
        </article>
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
          <router-link class="cta-primary" :to="`/${lang}/contact?source=about-cta`" @click="trackInquiry">{{ text.ctaPrimary }}</router-link>
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
import { breadcrumbSchema, organizationSchema, siteOrigin, videoSchema, faqPageSchema } from "../lib/jsonLd";
import CompanyVideo from "../components/CompanyVideo.vue";
import { trackInquiryCta } from "../analytics";

const route = useRoute();
const lang = computed(() => (String(route.params.lang || "zh") === "en" ? "en" : "zh"));
const trackInquiry = () => trackInquiryCta("about", {
  cta_key: "about_contact",
  cta_position: "about_cta"
});

const text = computed(() => {
  if (lang.value === "en") {
    return {
      breadcrumbHome: "Home",
      breadcrumbCurrent: "Company Profile",
      profileKicker: "INTRODUCTION",
      profileImageAlt: "Origin Chemical factory and production environment",
      profileTitle: "About Us",
      profileParagraphs: [
        "Founded in January 2010, Shenzhen Origin Chemical Industry Co., Ltd. (registered as Shenzhen Qidian Chemical Industry Co., Ltd.) ranks among China's early professional manufacturers integrating independent R&D, mass production, and global sales of functional inorganic chemical raw materials for the electronics and new-material industries.",
        "Our core team boasts profound industry experience, allowing us to accurately capture diverse technical requirements from downstream manufacturers and provide targeted material solutions. Through over 16 years of persistent technical iteration and product upgrading, we have built a diversified product lineup meeting top-tier international standards.",
        "Our product range covers specialty aluminum hydroxide, high-purity silica powder, high-grade alumina, as well as a full series of silane coupling agents. These versatile materials are widely applied in copper-clad laminates, semiconductor packaging, flame-retardant plastics, ceramic manufacturing, and photovoltaic supporting sectors, serving multiple high-end industrial chains.",
        "Equipped with complete automated production lines and professional laboratory testing equipment, we realize large-scale, standardized manufacturing and full-index quality monitoring for all raw materials. We can flexibly respond to bulk orders and customized material demands, maintaining continuous, on-time, stable supply to numerous domestic and overseas manufacturers, helping partners stabilize production schedules and cut overall purchasing costs.",
        "Adhering to the core business philosophy of cooperation and win-win, we implement strict full-process quality inspection and offer complete pre-sales, technical consultation, and after-sales support. We keep deepening material innovation to serve global new-material manufacturers, and we sincerely invite worldwide partners to create long-term, mutually beneficial cooperation with us."
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
      videoTitle: "Discover Origin Chemical's Material and Supply Capabilities",
      videoIntro: "Watch this two-minute overview of Origin Chemical's functional inorganic materials, affiliated manufacturing and testing operations, and technical and supply support for global customers.",
      videoCaption: "The video shows production, laboratory testing, warehousing, and product scenes from Origin Chemical's affiliated manufacturer, Jiangxi Qise Electronic Materials Co., Ltd.",
      faqTitle: "Frequently Asked Questions About Origin Chemical",
      faqEntries: [
        {
          question: "What does the video show?",
          answer: "The video introduces Origin Chemical's functional inorganic material business and shows production, laboratory testing, warehousing, and product scenes from its affiliated manufacturer, Jiangxi Qise Electronic Materials Co., Ltd. It gives buyers a direct view of the coordinated manufacturing and supply capabilities behind the material portfolio."
        },
        {
          question: "Which materials do you supply?",
          answer: "The main portfolio includes specialty aluminum hydroxide, high-purity silica powder, high-grade alumina, and silane coupling agents. We can help identify a suitable product series based on the target application, key specifications, expected volume, and documentation requirements."
        },
        {
          question: "Which industries use these materials?",
          answer: "The materials support copper-clad laminates, semiconductor packaging, flame-retardant plastics, ceramic manufacturing, photovoltaic applications, electronic materials, and functional composites. Final product selection should be confirmed against the formulation, processing conditions, and target performance."
        },
        {
          question: "How do you support quality and batch consistency?",
          answer: "The manufacturing and supply system combines automated production equipment, laboratory testing, and full-process quality inspection. Quality communication can cover key indicators such as particle size, whiteness, oil absorption, moisture, and sieve residue, with relevant testing and technical documents provided for the selected product."
        },
        {
          question: "Can you support bulk supply and customized requirements?",
          answer: "Bulk supply and customized requirements can be evaluated according to order volume, delivery schedule, and technical specifications. For a faster assessment, include the application, target indicators, monthly demand, destination, and sample requirements in the inquiry."
        },
        {
          question: "How can I request a recommendation or sample?",
          answer: "Send us your target application, current material, key performance requirements, expected quantity, and delivery region. Our technical and sales teams can recommend a suitable series and confirm available samples, technical data sheets, test documents, and quotation details."
        }
      ],
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
      "深圳市起点化工有限公司成立于2010年1月，是中国较早集自主研发、规模化生产及全球销售于一体的功能性无机化工原料专业企业之一，服务于电子及新材料行业。",
      "公司的核心团队拥有深厚的行业经验，能够准确把握下游制造商多样化的技术需求，并提供针对性的材料解决方案。经过16年持续的技术迭代与产品升级，公司已建立符合国际高端标准的多元化产品体系。",
      "产品范围包括特种氢氧化铝、高纯硅微粉、高等级氧化铝以及全系列硅烷偶联剂。这些材料广泛应用于覆铜板、半导体封装、阻燃塑料、陶瓷制造及光伏配套等领域，服务多个高端产业链。",
      "依托完整的自动化生产线和专业实验室检测设备，公司实现规模化、标准化制造，并对所有原材料开展全指标质量监控。公司能够灵活响应大批量订单及定制化材料需求，为众多国内外制造商持续提供准时、稳定的供应，帮助合作伙伴稳定生产计划并降低综合采购成本。",
      "公司秉承“合作共赢”的核心经营理念，实施严格的全流程质量检验，并提供完整的售前支持、技术咨询和售后服务。我们持续深化材料创新，服务全球新材料制造商，并诚邀全球合作伙伴与我们建立长期互利的合作关系。"
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
    videoTitle: "了解起点化工的材料与供应能力",
    videoIntro: "通过约两分钟的视频，了解起点化工的功能性无机材料产品、关联制造与检测场景，以及面向全球客户的技术和供应支持。",
    videoCaption: "视频展示了关联生产企业江西起色电子材料有限公司的生产、实验室检测、仓储及产品场景。",
    faqTitle: "关于起点化工的常见问题",
    faqEntries: [
      {
        question: "视频展示了哪些内容？",
        answer: "视频介绍了起点化工的功能性无机材料业务，并展示关联生产企业江西起色电子材料有限公司的生产、实验室检测、仓储及产品场景，帮助客户直观了解材料制造与供应协同能力。"
      },
      {
        question: "主要提供哪些材料？",
        answer: "主要材料包括特种氢氧化铝、高纯硅微粉、高等级氧化铝和硅烷偶联剂，可根据目标应用、关键指标、预计用量及文件要求协助匹配合适的产品系列。"
      },
      {
        question: "产品应用于哪些行业？",
        answer: "相关材料可用于覆铜板、半导体封装、阻燃塑料、陶瓷制造、光伏配套，以及电子材料和功能复合材料等应用。具体选型需要结合配方体系、加工条件和目标性能确认。"
      },
      {
        question: "如何保障产品质量与批次稳定性？",
        answer: "生产与供应体系结合自动化生产设备、实验室检测和全流程质量检验，围绕粒径、白度、吸油值、附着水、筛余等关键指标进行质量沟通，并根据具体产品提供相应的检测及技术资料。"
      },
      {
        question: "是否支持批量供货和定制需求？",
        answer: "可以根据采购数量、交付周期和技术指标评估批量供应或定制化需求。为提高沟通效率，建议询盘时同时提供应用领域、目标指标、月度用量、目的地和样品需求。"
      },
      {
        question: "如何获得材料推荐和样品？",
        answer: "请提交目标应用、现用材料、关键性能要求、预计用量及交付地区。技术和销售团队会据此推荐产品系列，并确认可提供的样品、技术数据表、检测资料和报价信息。"
      }
    ],
    ctaTitle: "需要了解适合您应用的材料方案？",
    ctaDescription: "告诉我们您的目标应用、性能要求和预计用量，我们将协助推荐合适的产品系列与资料。",
    ctaPrimary: "索取报价",
    ctaSecondary: "查看产品"
  };
});

const pageUrl = computed(() => `${siteOrigin}/${lang.value}/about`);
useJsonLd(computed(() => [
  organizationSchema(lang.value),
  videoSchema(lang.value, pageUrl.value),
  faqPageSchema(text.value.faqEntries, pageUrl.value, lang.value),
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

/* Company video and FAQ blocks */
.video-section {
  padding-top: 26px;
  padding-bottom: 68px;
}

.video-intro {
  max-width: 760px;
  color: var(--pf-muted);
  line-height: 1.78;
}

.about-company-video {
  max-width: 980px;
}

.faq-section {
  padding-top: 24px;
  padding-bottom: 72px;
}

.faq-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.faq-item {
  padding: 22px;
  background: #ffffff;
  border: 1px solid #dfe9f4;
  border-radius: 6px;
  display: grid;
  align-content: start;
  gap: 10px;
}

.faq-item h3 {
  margin: 0;
  color: #18314f;
  font-size: 1.05rem;
  line-height: 1.4;
}

.faq-item p {
  margin: 0;
  color: var(--pf-muted);
  line-height: 1.7;
  font-size: 0.95rem;
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
  .cta-inner,
  .faq-list {
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

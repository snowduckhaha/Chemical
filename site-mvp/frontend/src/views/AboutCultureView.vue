<template>
  <div class="about-page">
    <section class="about-hero" aria-label="Company culture banner"></section>

    <nav class="breadcrumb-bar" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <router-link :to="`/${lang}`" class="home-icon" aria-label="Home">⌂</router-link>
        <router-link :to="`/${lang}`">{{ text.breadcrumbHome }}</router-link>
        <span>/</span>
        <span>{{ text.breadcrumbCurrent }}</span>
      </div>
    </nav>

    <section class="values-section">
      <div class="section-shell values-inner">
        <div class="section-head values-head">
          <p class="section-kicker">CULTURE</p>
          <h1>{{ text.pageTitle }}</h1>
        </div>

        <div class="culture-feature-grid">
          <article v-for="item in text.features" :key="item.title" class="culture-feature-card">
            <div class="value-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="culture-detail-section section-shell" aria-labelledby="culture-detail-title">
      <div class="section-head culture-detail-head">
        <p class="section-kicker">VALUES</p>
        <h2 id="culture-detail-title">{{ text.detailTitle }}</h2>
      </div>

      <div class="culture-group-list">
        <article v-for="group in text.groups" :key="group.title" class="culture-group">
          <div class="culture-group-title">
            <span>{{ group.index }}</span>
            <h3>{{ group.title }}</h3>
            <p v-if="group.summary">{{ group.summary }}</p>
          </div>

          <div class="culture-items">
            <div v-for="item in group.items" :key="item.title" class="culture-item">
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const lang = computed(() => (String(route.params.lang || "zh") === "en" ? "en" : "zh"));

const text = computed(() => {
  if (lang.value === "en") {
    return {
      breadcrumbHome: "Home",
      breadcrumbCurrent: "Company Culture",
      pageTitle: "Company Culture",
      features: [
        {
          title: "Mission",
          description: "Work together to build a safer, better and more sustainable future."
        },
        {
          title: "Vision",
          description: "Lead industry development through technological innovation and become a global leading supplier of halogen-free flame-retardant materials and specialty materials."
        }
      ],
      detailTitle: "Our Cultural Principles",
      groups: [
        {
          index: "01",
          title: "Cultural Philosophy",
          summary: "People-oriented, integrity-based, fact-seeking and scientific development.",
          items: [
            { title: "People-oriented", description: "Employees are the most important factor in business success, and we provide broad opportunities for development." },
            { title: "Integrity-based", description: "We uphold business ethics and earn customer trust through honesty." },
            { title: "Fact-seeking", description: "We respect objective laws and pursue a truthful, accurate working attitude." },
            { title: "Scientific development", description: "We follow scientific methods and promote sustainable business growth." }
          ]
        },
        {
          index: "02",
          title: "Enterprise Spirit",
          summary: "Self-improvement, hard work, unity, dedication and keeping pace with the times.",
          items: [
            { title: "Perseverance", description: "Stay resilient in the face of challenges and continue pursuing excellence." },
            { title: "Diligent entrepreneurship", description: "Maintain an entrepreneurial spirit, work diligently and frugally, and persevere." },
            { title: "Unity and collaboration", description: "Work together, make progress as a team and contribute to company development." },
            { title: "Keeping pace with the times", description: "Actively adapt to market changes, embrace innovation and keep improving." }
          ]
        },
        {
          index: "03",
          title: "Core Value Extensions",
          summary: "Responsibility, quality, innovation, safety and health shape our long-term operating standards.",
          items: [
            { title: "Environmental responsibility", description: "Operate responsibly, protect the environment and safeguard our shared home." },
            { title: "Quality philosophy", description: "Provide high-quality products and services for domestic and international customers, meeting and exceeding customer needs." },
            { title: "Innovation philosophy", description: "Value the power of new technologies and products, and regard innovation as the lifeline of the enterprise." },
            { title: "Safety and health", description: "Provide a safe and civilized working environment and protect employees' physical and mental health." }
          ]
        }
      ]
    };
  }

  return {
    breadcrumbHome: "首页",
    breadcrumbCurrent: "企业文化",
    pageTitle: "企业文化",
    features: [
      {
        title: "使命",
        description: "携手共建更安全、更美好、更可持续的未来。"
      },
      {
        title: "愿景",
        description: "以科技创新引领行业发展，成为全球领先的无卤阻燃材料和特种材料供应商。"
      }
    ],
    detailTitle: "价值观与企业精神",
    groups: [
      {
        index: "01",
        title: "文化理念",
        summary: "以人为本，诚信立足，实事求是，科学发展。",
        items: [
          { title: "以人为本", description: "员工是企业成功最重要的因素，提供广阔的发展机会。" },
          { title: "诚信立足", description: "坚守商业道德，以诚实赢得客户信任。" },
          { title: "实事求是", description: "尊重客观规律，追求真实、准确的工作态度。" },
          { title: "科学发展", description: "遵循科学方法，推动企业可持续增长。" }
        ]
      },
      {
        index: "02",
        title: "企业精神",
        summary: "自强不息，艰苦创业，团结奉献，与时俱进。",
        items: [
          { title: "坚韧不拔", description: "面对挑战保持韧性，不断追求卓越。" },
          { title: "勤奋创业", description: "秉持创业精神，勤勉节俭，坚持不懈。" },
          { title: "团结协作", description: "齐心协力，共同进步，为公司发展贡献力量。" },
          { title: "与时俱进", description: "积极适应市场变化，拥抱创新，持续改进。" }
        ]
      },
      {
        index: "03",
        title: "核心价值观的延伸",
        summary: "环境责任、质量理念、创新理念、安全与健康共同构成企业长期经营标准。",
        items: [
          { title: "环境责任", description: "以负责任的态度经营业务，保护环境，守护家园。" },
          { title: "质量理念", description: "为国内外客户提供高质量的产品和服务，满足并超越客户需求。" },
          { title: "创新理念", description: "重视新技术和新产品的力量，视创新为企业的生命线。" },
          { title: "安全与健康", description: "提供安全文明的工作环境，保障员工的身心健康。" }
        ]
      }
    ]
  };
});
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
}

.values-section {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #ffffff;
}

.values-inner {
  padding-top: 64px;
  padding-bottom: 70px;
}

.values-head {
  margin-bottom: 28px;
}

.values-head h1,
.culture-detail-head h2 {
  margin: 0;
  color: #18314f;
  font-size: clamp(1.65rem, 2.3vw, 2.25rem);
  line-height: 1.22;
  letter-spacing: 0;
}

.culture-feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  background: #d8e6f0;
}

.culture-feature-card {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  background: url("/about/technology-bg.png") center/cover no-repeat;
  color: #ffffff;
}

.culture-feature-card:nth-child(2) {
  background-position: 78% center;
}

.culture-feature-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 42, 68, 0.2), rgba(7, 28, 48, 0.84));
  transition: background 0.18s ease;
}

.culture-feature-card:hover::before {
  background: linear-gradient(180deg, rgba(16, 112, 173, 0.18), rgba(5, 35, 61, 0.9));
}

.value-content {
  position: relative;
  z-index: 1;
  min-height: 360px;
  padding: 34px 28px 30px;
  display: grid;
  align-content: end;
  gap: 12px;
}

.culture-feature-card h2 {
  margin: 0;
  color: #ffffff;
  font-size: clamp(1.8rem, 2.4vw, 2.6rem);
  line-height: 1.05;
}

.culture-feature-card p,
.culture-detail-head p,
.culture-group p,
.culture-item p {
  margin: 0;
  line-height: 1.78;
  font-size: 1rem;
}

.culture-feature-card p {
  color: rgba(255, 255, 255, 0.84);
  max-width: 660px;
  font-size: 1.02rem;
}

.culture-detail-section {
  padding-top: 70px;
  padding-bottom: 72px;
}

.culture-detail-head {
  margin-bottom: 28px;
}

.culture-detail-head p,
.culture-group p,
.culture-item p {
  color: var(--pf-muted);
}

.culture-group-list {
  display: grid;
  gap: 18px;
}

.culture-group {
  display: grid;
  grid-template-columns: minmax(240px, 0.42fr) minmax(0, 1fr);
  gap: clamp(24px, 4vw, 56px);
  padding: 28px;
  background: #ffffff;
  border: 1px solid #dfe9f4;
  border-radius: 6px;
}

.culture-group-title {
  display: grid;
  align-content: start;
  gap: 10px;
}

.culture-group-title span {
  color: #169bd7;
  font-size: 0.82rem;
  font-weight: 800;
}

.culture-group-title h3 {
  margin: 0;
  color: #18314f;
  font-size: clamp(1.22rem, 1.2vw, 1.55rem);
  line-height: 1.28;
}

.culture-items {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.culture-item {
  padding: 16px 18px;
  background: #f7fafc;
  border-left: 3px solid #169bd7;
  display: grid;
  gap: 7px;
}

.culture-item strong {
  color: #18314f;
  font-size: 1rem;
  line-height: 1.35;
}

@media (max-width: 980px) {
  .culture-group {
    grid-template-columns: 1fr;
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

  .culture-feature-grid,
  .culture-items {
    grid-template-columns: 1fr;
  }

  .values-inner,
  .culture-detail-section {
    padding-top: 48px;
    padding-bottom: 52px;
  }

  .culture-feature-card,
  .value-content {
    min-height: 300px;
  }

  .culture-group {
    padding: 22px 18px;
  }
}
</style>

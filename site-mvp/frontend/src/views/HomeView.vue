<template>
  <div class="home" v-if="homeSection">
    <section class="hero-wrap">
      <div class="hero-stage" :style="{ background: slides[activeSlide].image }">
        <div class="hero-overlay">
          <p class="hero-kicker">{{ lang === "en" ? "MATERIAL SOLUTIONS" : "材料解决方案" }}</p>
          <h1>{{ slides[activeSlide].title }}</h1>
          <p>{{ slides[activeSlide].subtitle }}</p>
        </div>
      </div>
      <div class="hero-index">
        <button
          v-for="(item, index) in slides"
          :key="item.title"
          :class="['hero-step', { active: index === activeSlide }]"
          @click="activeSlide = index"
        >
          <span class="sr-only">{{ String(index + 1).padStart(2, "0") }}</span>
        </button>
      </div>
    </section>

    <section class="solution-section">
      <div class="solution-head">
        <div class="section-copy">
          <h2>
            <span>{{ lang === "en" ? "Advanced" : "先进的" }}</span>
            <span>{{ lang === "en" ? "Material Solutions" : "材料解决方案" }}</span>
          </h2>
        </div>

        <div class="solution-desc">
          <p>
            {{
              lang === "en"
                ? "Our portfolio covers halogen-free flame retardant additives, alumina and boehmite related product lines."
                : "我们的产品线涵盖无卤阻燃添加剂、氧化铝及勃姆石等。"
            }}
          </p>
          <p>
            {{
              lang === "en"
                ? "We deliver stable quality, safer formulations and practical solutions for global B2B customers."
                : "所有产品均具备环保、安全特性，致力于为全球客户提供可靠解决方案。"
            }}
          </p>
        </div>
      </div>

      <div class="category-grid">
        <router-link
          v-for="item in displayCategories"
          :key="item.slug"
          :to="`/${lang}/products/${item.slug}`"
          class="category-card"
        >
          <div class="thumb" :style="thumbStyle(item)">
            <div class="info-row">
              <h3>{{ item.name }}</h3>
              <span class="arrow">›</span>
            </div>
          </div>
        </router-link>
      </div>

    </section>

    <section v-if="applications.length" class="applications-section">
      <div class="module-heading">
        <div>
          <h2>{{ lang === "en" ? "Explore Applications" : "探索应用领域" }}</h2>
        </div>
        <p>{{ lang === "en" ? "Match materials with real-world requirements across flame retardancy, electronics and functional material systems." : "围绕阻燃、电子电气与功能材料等真实需求，快速匹配适用的产品方案。" }}</p>
      </div>
      <div class="application-grid">
        <router-link v-for="item in applications.slice(0, 4)" :key="item.slug" :to="`/${lang}/applications/${item.slug}`" class="application-card">
          <img :src="item.image" :alt="item.name" />
          <span><strong>{{ item.name }}</strong><small>{{ lang === "en" ? "Explore solution" : "查看解决方案" }} <b>→</b></small></span>
        </router-link>
      </div>
    </section>

    <section class="company-section">
      <div class="company-copy">
        <h2>
          <span>{{ lang === "en" ? "Reliable Quality" : "可靠品质" }}</span>
          <span>{{ lang === "en" ? "Stable Supply" : "稳定供应" }}</span>
        </h2>
        <p>
          {{
            lang === "en"
              ? "From material development to quality control and delivery coordination, we support reliable sourcing for industrial customers."
              : "从材料研发、质量控制到交付协同，为工业客户提供稳定、可靠的采购支持。"
          }}
        </p>
        <router-link class="discover" :to="`/${lang}/about`">{{ lang === "en" ? "Discover More" : "发现更多" }} <span>→</span></router-link>
      </div>

      <div class="stat-list">
        <article>
          <img src="/about/quality-lab.png" alt="" />
          <div><h4>{{ lang === "en" ? "Quality Control" : "质量控制" }}</h4><p>{{ lang === "en" ? "Professional testing and traceable quality management." : "专业检测与可追溯的质量管理体系。" }}</p></div>
        </article>
        <article>
          <img src="/about/warehouse-delivery.png" alt="" />
          <div><h4>{{ lang === "en" ? "Delivery Support" : "交付保障" }}</h4><p>{{ lang === "en" ? "Coordinated supply and responsive service for your project." : "协同供应与快速响应，保障项目交付节奏。" }}</p></div>
        </article>
      </div>
    </section>

    <section class="project-cta">
      <div>
        <h2>
          <span>{{ lang === "en" ? "Custom Formula" : "为您量身定制" }}</span>
          <span>{{ lang === "en" ? "For Flame Retardancy" : "阻燃配方" }}</span>
        </h2>
        <p>
          {{
            lang === "en"
              ? "We support customized aluminum hydroxide and magnesium hydroxide formulas to improve compatibility and performance."
              : "我们支持氢氧化铝和氢氧化镁阻燃配方定制，兼顾树脂相容性与性能提升。"
          }}
        </p>
      </div>
      <router-link class="cta-link" :to="`/${lang}/contact`">{{ lang === "en" ? "Discuss Your Project" : "讨论你的项目" }} <span>→</span></router-link>
    </section>

    <section class="news-section">
      <div class="module-heading news-heading"><div><h2>{{ lang === "en" ? "News & Insights" : "新闻与洞察" }}</h2></div><router-link :to="`/${lang}/news`">{{ lang === "en" ? "Discover More" : "发现更多" }} <span>→</span></router-link></div>
      <div class="news-grid">
        <router-link v-for="item in newsList.slice(0, 3)" :key="item.slug" :to="`/${lang}/news/${item.categorySlug}/${item.slug}`" class="news-card">
          <img v-if="item.coverImage" :src="item.coverImage" :alt="item.coverAlt || item.title" /><span v-else class="news-placeholder"></span>
          <small>{{ item.category }} · {{ item.publishedAt }}</small><h3>{{ item.title }}</h3><p>{{ item.summary }}</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getApplications, getCategories, getHome, getNews } from "../api/site";
import type { Application, HomeSection, Category, News } from "../types/site";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const homeSection = ref<HomeSection>();
const categories = ref<Category[]>([]);
const newsList = ref<News[]>([]);
const applications = ref<Application[]>([]);
const activeSlide = ref(0);

const displayCategories = computed(() => {
  const source = [...categories.value];
  if (source.length >= 4) {
    return source.slice(0, 4);
  }

  const fallback: Category = {
    slug: "boehmite",
    name: lang.value === "en" ? "Boehmite" : "勃姆石",
    summary: lang.value === "en" ? "Battery and engineering plastics use cases." : "电池与工程塑料方向应用。",
    image: "/home/cat-boehmite.png"
  };

  if (!source.some((item) => item.slug === fallback.slug)) {
    source.push(fallback);
  }
  return source.slice(0, 4);
});

const slides = computed(() => {
  if (lang.value === "en") {
    return [
      {
        title: "Advanced Material Solutions",
        subtitle: "Focused on flame-retardant and functional inorganic materials for global B2B customers.",
        image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-1.png') center/cover no-repeat"
      },
      {
        title: "Stable Quality, Faster Delivery",
        subtitle: "Transit warehouse network improves response speed and order fulfillment efficiency.",
        image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-2.png') center/cover no-repeat"
      },
      {
        title: "From Category To Product Detail",
        subtitle: "Clear sourcing path from category to series and product technical review.",
        image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-3.png') center/cover no-repeat"
      }
    ];
  }

  return [
    {
      title: "先进材料解决方案",
      subtitle: "专注阻燃与功能性无机材料，为全球 B2B 客户提供稳定供货与技术支持。",
      image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-1.png') center/cover no-repeat"
    },
    {
      title: "质量稳定，交付更快",
      subtitle: "通过多地中转仓布局提升响应效率，降低交付不确定性。",
      image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-2.png') center/cover no-repeat"
    },
    {
      title: "从分类到详情的清晰路径",
      subtitle: "遵循分类-系列-详情结构，便于采购与技术评估快速定位。",
      image: "linear-gradient(115deg, rgba(12,45,90,0.72), rgba(24,92,161,0.45)), url('/home/hero-3.png') center/cover no-repeat"
    }
  ];
});

let slideTimer: ReturnType<typeof setInterval> | undefined;

const fallbackHome = (currentLang: string): HomeSection => ({
  ctaText: currentLang === "en" ? "Explore Products" : "探索产品",
  ctaLink: `/${currentLang}/products`
});

const fallbackCategories = (currentLang: string): Category[] => [
  {
    slug: "aluminum-hydroxide",
    name: currentLang === "en" ? "Aluminum Hydroxide" : "氢氧化铝",
    summary: currentLang === "en" ? "Halogen-free flame retardant filler." : "无卤阻燃填料。",
    image: "/home/cat-ath.png"
  },
  {
    slug: "silica-powder-series",
    name: currentLang === "en" ? "Silica Powder Series" : "硅粉系列",
    summary: currentLang === "en" ? "Functional filler systems for multiple resins." : "面向多种树脂体系的功能填料。",
    image: "/home/cat-na.png"
  },
  {
    slug: "alumina-powder",
    name: currentLang === "en" ? "Alumina Powder" : "氧化铝粉末",
    summary: currentLang === "en" ? "High purity alumina materials." : "高纯氧化铝材料。",
    image: "/home/cat-ath.png"
  },
  {
    slug: "silane-coupling-agent",
    name: currentLang === "en" ? "Silane Coupling Agent" : "硅烷偶联剂",
    summary: currentLang === "en" ? "Surface treatment and compatibility enhancement." : "提升界面相容性与分散稳定性。",
    image: "/home/cat-boehmite.png"
  }
];

const loadPage = async () => {
  try {
    const [home, categoryData, newsData, applicationData] = await Promise.all([
      getHome(lang.value),
      getCategories(lang.value),
      getNews(lang.value),
      getApplications(lang.value)
    ]);
    homeSection.value = home;
    categories.value = categoryData;
    newsList.value = newsData;
    applications.value = applicationData;
  } catch {
    homeSection.value = fallbackHome(lang.value);
    categories.value = fallbackCategories(lang.value);
    newsList.value = [];
    applications.value = [];
  }

  if (slideTimer) {
    clearInterval(slideTimer);
  }
  slideTimer = setInterval(() => {
    activeSlide.value = (activeSlide.value + 1) % slides.value.length;
  }, 4500);
};

onMounted(loadPage);
watch(lang, loadPage);

onUnmounted(() => {
  if (slideTimer) {
    clearInterval(slideTimer);
  }
});

const thumbStyle = (item: Category) => {
  const slug = item.slug;
  const map: Record<string, string> = {
    "aluminum-hydroxide": "/home/cat-ath.png",
    "magnesium-hydroxide": "/home/cat-mh.jpg",
    "nano-alumina": "/home/cat-na.png",
    boehmite: "/home/cat-boehmite.png"
  };
  const image = item.image || map[slug] || "/home/cat-ath.png";
  return {
    backgroundImage: `linear-gradient(115deg, rgba(12,45,90,0.12), rgba(24,92,161,0.06)), url('${image}')`
  };
};
</script>

<style scoped>
.home {
  display: grid;
  gap: 24px;
}

.hero-wrap {
  position: relative;
  width: 100%;
  margin: 0;
}

.hero-stage {
  min-height: clamp(340px, 46vw, 500px);
  border-radius: 0;
  position: relative;
  overflow: hidden;
}

.hero-stage::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 70% 20%, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0));
}

.hero-overlay {
  position: absolute;
  left: clamp(20px, 4vw, 54px);
  bottom: clamp(18px, 4.2vw, 48px);
  width: min(600px, calc(100% - 38px));
  color: #fff;
  display: grid;
  gap: 9px;
}

.hero-kicker {
  margin: 0;
  letter-spacing: 0.12em;
  font-size: 0.76rem;
  color: rgba(255, 255, 255, 0.86);
}

.hero-overlay h1 {
  margin: 0;
  font-size: clamp(1.9rem, 4vw, 3.05rem);
  line-height: 1.1;
}

.hero-overlay p {
  margin: 0;
  max-width: 560px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.55;
}

.hero-actions {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.btn-primary,
.btn-ghost {
  text-decoration: none;
  padding: 9px 15px;
  border-radius: 4px;
  font-weight: 700;
  font-size: 0.86rem;
}

.btn-primary {
  color: #113f77;
  background: #fff;
}

.btn-ghost {
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.hero-index {
  position: absolute;
  right: clamp(12px, 2.1vw, 26px);
  bottom: clamp(12px, 2.1vw, 24px);
  display: grid;
  gap: 6px;
  z-index: 2;
}

.hero-step {
  width: 46px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.96);
  color: #3e5f86;
  font-weight: 700;
  font-size: 0.8rem;
  cursor: pointer;
}

.hero-step.active {
  background: #0f4aa1;
  border-color: #0f4aa1;
  color: #fff;
}

.solution-section {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
  background: transparent;
  border: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.solution-head {
  display: grid;
  grid-template-columns: minmax(340px, 0.8fr) minmax(0, 1.2fr);
  align-items: start;
  gap: clamp(18px, 2.8vw, 40px);
}

.solution-section .section-copy h2 {
  color: #1296e1;
  font-size: clamp(2.15rem, 3.5vw, 3.125rem);
  line-height: 1.1667;
  gap: 0;
  letter-spacing: 0;
}

.solution-section .section-copy h2 span:first-child {
  color: #1296e1;
  font-weight: 500;
}

.solution-section .section-copy h2 span:last-child {
  color: #9ca3b0;
  font-weight: 500;
  margin-top: -2px;
}

.section-copy h2,
.company-copy h2,
.project-cta h2 {
  margin: 0;
  display: grid;
  gap: 4px;
  color: #1d467d;
  font-size: clamp(1.55rem, 2.9vw, 2.55rem);
  line-height: 1.08;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.section-copy p,
.company-copy p,
.project-cta p {
  margin: 10px 0 0;
  color: #55718f;
  line-height: 1.65;
}

.solution-desc {
  display: grid;
  gap: 0;
  max-width: 504px;
  justify-self: end;
  padding-top: 14px;
}

.solution-desc p {
  margin: 0;
  color: #777777;
  font-size: clamp(1.05rem, 1.4vw, 1.25rem);
  line-height: 31.1px;
  font-weight: 400;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  width: 100%;
  max-width: 1136px;
  align-items: start;
}

.category-card {
  text-decoration: none;
  display: block;
  border: 0;
  padding: 0;
  background: transparent;
  position: relative;
  overflow: hidden;
}

.thumb {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-size: cover;
  background-position: center;
  position: relative;
}

.info-row {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: linear-gradient(180deg, rgba(8, 24, 44, 0.06) 0%, rgba(8, 24, 44, 0.62) 70%, rgba(8, 24, 44, 0.76) 100%);
  padding: 12px 14px;
}

.info-row h3 {
  margin: 0;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 500;
}

.arrow {
  color: rgba(255, 255, 255, 0.92);
  font-size: 1rem;
  line-height: 1;
}

.company-section {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
  background: #fff;
  border: 1px solid #dde8f4;
  padding: clamp(18px, 2.8vw, 30px);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.discover {
  margin-top: 14px;
  display: inline-flex;
  text-decoration: none;
  color: #0f4aa1;
  font-weight: 700;
}

.stat-list {
  display: grid;
  gap: 12px;
}

.stat-list article {
  border: 1px solid #dce8f5;
  padding: 14px;
  background: #f9fcff;
  display: grid;
  gap: 8px;
}

.stat-list h4 {
  margin: 0;
  color: #143d73;
}

.stat-list p {
  margin: 0;
  color: #5d7998;
  line-height: 1.6;
}

.value {
  color: #0f4aa1;
  font-size: 2rem;
  font-weight: 800;
  line-height: 1;
}

.value span {
  margin-left: 6px;
  font-size: 1rem;
  font-weight: 600;
}

.project-cta {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
  background: linear-gradient(120deg, #f0f7ff 0%, #e2effd 100%);
  border: 1px solid #d0e3f8;
  padding: clamp(18px, 3.5vw, 30px);
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
}

.cta-link {
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  padding: 11px 16px;
  border-radius: 4px;
  color: #fff;
  background: #0f4aa1;
  font-weight: 700;
}

.news-section {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
  background: #fff;
  border: 1px solid #dde8f4;
  padding: clamp(16px, 2.4vw, 22px);
}

.news-section h2 {
  margin: 0;
  color: #143d73;
  font-size: clamp(1.35rem, 2.6vw, 1.95rem);
}

.news-section ul {
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.news-section li {
  border-bottom: 1px dashed #dbe7f4;
  padding-bottom: 9px;
}

.news-section a {
  text-decoration: none;
  color: #1e457c;
  line-height: 1.55;
}

.news-section a:hover {
  color: #0f4aa1;
}

@media (max-width: 1366px) {
  .hero-stage {
    min-height: 450px;
  }

  .hero-overlay h1 {
    font-size: clamp(1.85rem, 3.8vw, 2.95rem);
  }
}

@media (max-width: 1200px) {
  .solution-section,
  .company-section,
  .project-cta,
  .news-section {
    width: min(860px, calc(100vw - 40px));
  }

  .solution-head,
  .company-section {
    grid-template-columns: 1fr;
  }

  .solution-section .section-copy h2 {
    font-size: clamp(1.85rem, 4.5vw, 2.65rem);
    line-height: 1.14;
  }

  .category-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .solution-desc {
    max-width: none;
    justify-self: start;
    padding-top: 2px;
  }

  .solution-desc p {
    font-size: 1.08rem;
    line-height: 1.62;
  }

  .hero-overlay {
    width: min(520px, calc(100% - 36px));
  }
}

@media (max-width: 992px) {
  .solution-section,
  .project-cta {
    display: grid;
    grid-template-columns: 1fr;
  }

  .project-cta {
    align-items: start;
  }

  .hero-stage {
    min-height: 370px;
  }
}

@media (max-width: 768px) {
  .solution-section,
  .company-section,
  .project-cta,
  .news-section {
    width: calc(100vw - 30px);
  }

  .hero-index {
    right: 12px;
    bottom: 12px;
    grid-template-columns: repeat(3, 1fr);
    gap: 4px;
  }

  .hero-step {
    width: 100%;
    height: 28px;
  }

  .category-grid {
    grid-template-columns: 1fr;
  }

  .category-card {
    display: block;
  }

  .solution-section .section-copy h2 {
    font-size: clamp(1.65rem, 7vw, 2.25rem);
    line-height: 1.12;
  }

  .solution-desc p {
    font-size: 1rem;
    line-height: 1.6;
  }

  .hero-overlay {
    left: 16px;
    width: calc(100% - 30px);
  }
}

/* Homepage visual baseline: image-led reference layout. */
.home {
  gap: 0;
  overflow: hidden;
  background: #fff;
  font-family: "Inter Tight", Arial, "Noto Sans SC", "PingFang SC", sans-serif;
}

/* The banner is the only edge-to-edge homepage section. Every other block
   shares this container so their left and right whitespace stays aligned. */
.home > section:not(.hero-wrap) {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin-right: auto;
  margin-left: auto;
}

.hero-stage {
  min-height: 0;
  aspect-ratio: 1920 / 669;
}

.hero-stage::before {
  background: linear-gradient(90deg, rgba(12, 29, 45, 0.38) 0%, rgba(12, 29, 45, 0.16) 35%, transparent 66%);
}

.hero-overlay {
  left: max(52px, calc((100vw - 1200px) / 2));
  bottom: clamp(58px, 9vw, 138px);
  width: min(530px, calc(100% - 60px));
  gap: 8px;
}

.hero-kicker {
  font-size: 18px;
  line-height: 1.3;
  font-weight: 400;
  letter-spacing: .06em;
}

.hero-overlay h1 {
  max-width: 500px;
  font-size: clamp(42px, 4.7vw, 70px);
  line-height: 1.12;
  font-weight: 600;
  letter-spacing: .01em;
}

.hero-overlay p {
  max-width: 490px;
  font-size: 18px;
  line-height: 1.65;
}

.hero-index {
  right: max(36px, calc((100vw - 1200px) / 2));
  bottom: clamp(38px, 7vw, 106px);
  display: flex;
  align-items: center;
  gap: 20px;
}

.hero-step {
  position: relative;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: rgba(255, 255, 255, .7);
  font-size: 0;
}

.hero-step::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, .5);
  border-radius: 50%;
}

.hero-step.active::after { border: 2px solid #fff; }
.hero-step.active { background: transparent; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }

.solution-section,
.applications-section,
.company-section,
.project-cta,
.news-section {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
}

.solution-section { min-height: 100vh; padding: 0 0 28px; align-content: center; gap: 60px; }
.solution-head { grid-template-columns: minmax(400px, 1fr) minmax(360px, .92fr); align-items: center; gap: 100px; }
.solution-section .section-copy h2 { font-size: 50px; line-height: 58.3333px; letter-spacing: .8px; }
.solution-section .section-copy h2 span:last-child { margin-top: 0; color: #9ca3b0; }
.solution-desc { align-self: end; max-width: 475px; padding: 0 0 4px; }
.solution-desc p { font-size: 20px; line-height: 31.1111px; color: #777; letter-spacing: 1px; }
.category-grid { max-width: none; gap: 30px; }
.thumb { aspect-ratio: 1 / 1; background-position: center; transition: transform .45s ease; }
.category-card { background: #eef2f4; }
.category-card:hover .thumb { transform: scale(1.035); }
.info-row { min-height: 74px; padding: 18px 20px; background: linear-gradient(180deg, transparent, rgba(5, 20, 31, .76)); }
.info-row h3 { font-size: 20px; font-weight: 500; }
.arrow { font-size: 28px; font-weight: 300; }

.module-heading { display: flex; justify-content: space-between; gap: 80px; align-items: end; }
.module-heading h2 { margin: 0; color: #9ca3b0; font-size: 50px; font-weight: 500; line-height: 58.3333px; letter-spacing: .8px; }
.section-kicker { margin: 0 0 14px; color: #1296e1; font-size: 14px; font-weight: 500; letter-spacing: .08em; }
.module-heading > p { max-width: 475px; margin: 0; color: #777; font-size: 20px; line-height: 31.1111px; letter-spacing: 1px; }
.applications-section { padding: 0 0 116px; }
.application-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 46px; }
.application-card { position: relative; display: block; aspect-ratio: .9 / 1; overflow: hidden; background: #e7ecef; color: #fff; text-decoration: none; }
.application-card img { width: 100%; height: 100%; object-fit: cover; transition: transform .45s ease; }
.application-card::after { content: ""; position: absolute; inset: 0; background: linear-gradient(180deg, transparent 40%, rgba(4, 20, 32, .8)); }
.application-card > span { position: absolute; z-index: 1; right: 20px; bottom: 20px; left: 20px; display: grid; gap: 8px; }
.application-card strong { font-size: 23px; font-weight: 500; line-height: 1.25; }
.application-card small { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: rgba(255, 255, 255, .9); }
.application-card small b { font-size: 20px; font-weight: 400; }
.application-card:hover img { transform: scale(1.05); }

.company-section { min-height: 100vh; grid-template-columns: minmax(0, .93fr) minmax(0, 1.07fr); align-content: center; gap: 82px; padding: 0 0 28px; border: 0; background: transparent; }
.company-copy { align-self: center; }
.company-copy h2 { font-size: 50px; line-height: 58.3333px; color: #9ca3b0; font-weight: 500; letter-spacing: .8px; }
.company-copy h2 span:last-child { color: #1296e1; }
.company-copy p { max-width: 626px; margin-top: 26px; color: #777; font-size: 20px; line-height: 31.1111px; letter-spacing: 1px; }
.discover { margin-top: 28px; gap: 18px; color: #1296e1; font-size: 16px; font-weight: 500; }
.discover span, .cta-link span, .news-heading a span { font-size: 22px; font-weight: 400; }
.stat-list { grid-template-columns: repeat(2, 1fr); gap: 16px; }
.stat-list article { min-height: 365px; padding: 0; border: 0; background: #f2f5f7; overflow: hidden; display: grid; grid-template-rows: 218px 1fr; gap: 0; }
.stat-list article img { width: 100%; height: 218px; object-fit: cover; }
.stat-list article div { padding: 22px 23px; }
.stat-list h4 { color: #30343a; font-size: 21px; font-weight: 500; }
.stat-list p { margin-top: 11px; color: #777; font-size: 20px; line-height: 1.6667; }

.project-cta { min-height: 231px; margin-bottom: 0; padding: 38px 30px; border: 0; background: linear-gradient(104deg, rgba(9, 82, 141, .96), rgba(24, 161, 222, .86)), url('/about/company-facility.png') center / cover; color: #fff; align-items: center; }
.project-cta .section-kicker { color: rgba(255, 255, 255, .74); }
.project-cta h2 { font-size: 50px; line-height: 58.3333px; font-weight: 500; color: #fff; letter-spacing: .8px; }
.project-cta p { max-width: 650px; color: rgba(255, 255, 255, .85); font-size: 20px; line-height: 31.1111px; letter-spacing: 1px; }
.cta-link { min-height: 50px; padding: 0 23px; gap: 18px; border-radius: 0; background: #fff; color: #1296e1; font-size: 16px; font-weight: 500; }

.news-section { min-height: 100vh; padding: 0 0 28px; align-content: center; border: 0; background: transparent; }
.news-heading { margin-bottom: 46px; }
.news-heading a { display: inline-flex; align-items: center; gap: 18px; color: #1296e1; font-size: 16px; text-decoration: none; }
.news-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.news-card { display: block; color: #333; text-decoration: none; }
.news-card img, .news-placeholder { display: block; width: 100%; aspect-ratio: 1.45 / 1; object-fit: cover; background: #e9eef1; transition: transform .4s ease; }
.news-card:hover img { transform: scale(1.035); }
.news-card small { display: block; margin-top: 18px; color: #1296e1; font-size: 13px; letter-spacing: .03em; }
.news-card h3 { margin: 9px 0 0; color: #30343a; font-size: 22px; font-weight: 500; line-height: 1.4; }
.news-card p { display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; margin: 10px 0 0; color: #777; font-size: 15px; line-height: 1.65; }

@media (max-width: 1024px) {
  .home > section:not(.hero-wrap) { width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2))); }

  .solution-section, .applications-section, .company-section, .project-cta, .news-section { width: min(860px, calc(100vw - 40px)); }
  .solution-section, .company-section, .news-section { min-height: 0; }
  .hero-overlay { left: 40px; bottom: 68px; }
  .hero-index { right: 40px; bottom: 62px; }
  .solution-section { padding: 76px 0 82px; }
  .solution-head, .module-heading, .company-section { grid-template-columns: 1fr; display: grid; gap: 28px; }
  .solution-section .section-copy h2, .company-copy h2 { font-size: 48px; }
  .solution-desc, .module-heading > p { justify-self: start; max-width: 620px; }
  .application-grid { grid-template-columns: repeat(2, 1fr); }
  .company-section { padding: 78px 0; }
  .project-cta { margin-bottom: 82px; }
}

@media (max-width: 720px) {
  .home > section:not(.hero-wrap) { width: calc(100vw - 30px); }

  .hero-stage { min-height: clamp(455px, 125vw, 620px); aspect-ratio: auto; background-position: 63% center !important; }
  .hero-overlay { left: 22px; right: 22px; bottom: 76px; width: auto; }
  .hero-overlay h1 { font-size: 38px; }
  .hero-overlay p { font-size: 15px; line-height: 1.55; }
  .hero-kicker { font-size: 13px; }
  .hero-index { right: 22px; bottom: 24px; gap: 12px; }
  .hero-step { width: 24px; height: 24px; }
  .solution-section, .applications-section, .company-section, .project-cta, .news-section { width: calc(100vw - 30px); }
  .solution-section { padding: 58px 0 62px; gap: 30px; }
  .solution-section .section-copy h2, .company-copy h2 { font-size: 39px; }
  .solution-desc p, .module-heading > p, .company-copy p { font-size: 16px; }
  .category-grid, .application-grid, .stat-list, .news-grid { grid-template-columns: 1fr; }
  .category-grid { gap: 12px; }
  .thumb { aspect-ratio: 1.25 / 1; }
  .applications-section { padding-bottom: 64px; }
  .module-heading h2 { font-size: 34px; }
  .application-grid { margin-top: 26px; gap: 12px; }
  .application-card { aspect-ratio: 1.35 / 1; }
  .company-section { padding: 64px 0; gap: 38px; }
  .stat-list article { min-height: 0; grid-template-columns: 44% 1fr; grid-template-rows: 1fr; }
  .stat-list article img { height: 100%; min-height: 160px; }
  .project-cta { min-height: 0; margin-bottom: 64px; padding: 42px 26px; display: grid; align-items: start; }
  .project-cta h2 { font-size: 34px; }
  .news-section { padding-bottom: 64px; }
  .news-heading { margin-bottom: 27px; }
}
</style>

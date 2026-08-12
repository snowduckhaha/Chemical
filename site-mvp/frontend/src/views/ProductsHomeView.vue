<template>
  <div class="product-home page-shell">
    <section class="banner-strip">
      <div class="banner-card" aria-hidden="true"></div>
    </section>

    <section class="breadcrumb-strip" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <span class="is-current">{{ lang === "en" ? "Products" : "产品中心" }}</span>
      </div>
    </section>

    <section class="solution-intro">
      <div class="intro-title">
        <h1 class="solution-heading">
          <template v-if="lang === 'en'">
            <span class="solution-heading-primary">Advanced</span>
            <span class="solution-heading-secondary">Material Solutions</span>
          </template>
          <template v-else>
            <span class="solution-heading-primary">先进的</span>
            <span class="solution-heading-secondary">材料解决方案</span>
          </template>
        </h1>
      </div>
      <div class="intro-copy">
        <p class="intro-copy-emphasis">
          {{
            lang === "en"
              ? "We aim to provide products that closely match your application needs."
              : "我们拥有让您满意的产品。"
          }}
        </p>
      </div>
    </section>

    <section id="categories" class="feature-list" v-if="!loading">
      <article v-for="item in featureCategories" :key="item.slug" class="feature-block">
        <div class="feature-main-col">
          <div class="feature-name-col">
            <p class="feature-formula">{{ item.formula }}</p>
            <h3>{{ item.name }}</h3>
          </div>
          <div class="feature-content-col">
            <p class="feature-summary">{{ item.shortSummary }}</p>
            <ul class="feature-app-list">
              <li v-for="application in item.applications" :key="`${item.slug}-${application}`">
                <span class="app-dot" aria-hidden="true"></span>
                <span>{{ application }}</span>
              </li>
            </ul>
            <router-link :to="`/${lang}/products/${item.slug}`" class="feature-link">{{ lang === "en" ? "Learn More" : "了解更多" }}</router-link>
          </div>
        </div>
        <div class="feature-image-col">
          <img :src="item.image" :alt="item.name" class="feature-image" />
        </div>
      </article>
    </section>

    <section v-else class="loading-panel">Loading...</section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getCategories } from "../api/site";
import type { Category } from "../types/site";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const loading = ref(true);
const categories = ref<Category[]>([]);

const fallbackImageMap: Record<string, string> = {
  "aluminum-hydroxide": "/home/cat-ath.png",
  "silica-powder-series": "/home/hero-2.png",
  "alumina-powder": "/home/cat-na.png",
  "silane-coupling-agent": "/home/hero-3.png"
};

const fallbackCategories = computed<Category[]>(() => {
  if (lang.value === "en") {
    return [
      {
        slug: "aluminum-hydroxide",
        name: "Aluminum Hydroxide",
        summary: "Halogen-free flame-retardant filler for wire and cable, CCL, and polymer compound applications.",
        image: fallbackImageMap["aluminum-hydroxide"]
      },
      {
        slug: "silica-powder-series",
        name: "Silica Powder Series",
        summary: "Micronized silica materials for insulation, electronic packaging, and copper clad laminate systems.",
        image: fallbackImageMap["silica-powder-series"]
      },
      {
        slug: "alumina-powder",
        name: "Alumina Powder",
        summary: "Functional alumina powders for thermal conduction, electronic ceramic, and insulation filler scenarios.",
        image: fallbackImageMap["alumina-powder"]
      },
      {
        slug: "silane-coupling-agent",
        name: "Silane Coupling Agent",
        summary: "Surface treatment and compatibility enhancement materials for filler modification and resin systems.",
        image: fallbackImageMap["silane-coupling-agent"]
      }
    ];
  }

  return [
    {
      slug: "aluminum-hydroxide",
      name: "氢氧化铝",
      summary: "用于线缆、覆铜板和无卤阻燃体系的核心无机阻燃填料。",
      image: fallbackImageMap["aluminum-hydroxide"]
    },
    {
      slug: "silica-powder-series",
      name: "硅粉系列",
      summary: "面向电子封装、绝缘材料与覆铜板体系的功能性硅微粉材料。",
      image: fallbackImageMap["silica-powder-series"]
    },
    {
      slug: "alumina-powder",
      name: "氧化铝粉末",
      summary: "适用于导热、电子陶瓷和绝缘填料场景的功能性氧化铝粉体。",
      image: fallbackImageMap["alumina-powder"]
    },
    {
      slug: "silane-coupling-agent",
      name: "硅烷偶联剂",
      summary: "用于粉体表面处理、树脂相容提升和界面结合增强的偶联剂产品。",
      image: fallbackImageMap["silane-coupling-agent"]
    }
  ];
});

const applicationMap = computed<Record<string, string[]>>(() => {
  if (lang.value === "en") {
    return {
      "aluminum-hydroxide": ["Wire & Cable", "CCL", "Flame Retardant Compound"],
      "silica-powder-series": ["Electronic Packaging", "Insulation", "Copper Clad Laminate"],
      "alumina-powder": ["Thermal Interface Material", "Electronic Ceramic", "Insulation Filler"],
      "silane-coupling-agent": ["Filler Modification", "Resin Compatibility", "Surface Treatment"]
    };
  }

  return {
    "aluminum-hydroxide": ["线缆", "覆铜板", "无卤阻燃复合材料"],
    "silica-powder-series": ["电子封装", "绝缘材料", "覆铜板"],
    "alumina-powder": ["导热界面材料", "电子陶瓷", "绝缘填料"],
    "silane-coupling-agent": ["粉体改性", "树脂相容性", "表面处理"]
  };
});

const formulaMap = computed<Record<string, string>>(() => {
  if (lang.value === "en") {
    return {
      "aluminum-hydroxide": "[Al(OH)3]",
      "silica-powder-series": "[SiO2]",
      "alumina-powder": "[Al2O3]",
      "silane-coupling-agent": "[R-Si(OR')3]"
    };
  }

  return {
    "aluminum-hydroxide": "[Al(OH)3]",
    "silica-powder-series": "[SiO2]",
    "alumina-powder": "[Al2O3]",
    "silane-coupling-agent": "[R-Si(OR')3]"
  };
});

const truncateSummary = (text: string | null | undefined, limit = 80) => {
  if (!text) return "";
  const compact = text.replace(/\s+/g, " ").trim();
  if (compact.length <= limit) {
    return compact;
  }
  return `${compact.slice(0, limit).trimEnd()}...`;
};

const featureCategories = computed(() =>
  categories.value.map((item, index) => ({
    ...item,
    index: String(index + 1).padStart(2, "0"),
    formula: item.formula ? `[${item.formula}]` : formulaMap.value[item.slug] || "[Material]",
    shortSummary: truncateSummary(item.summary),
    applications: item.applications?.length ? item.applications : applicationMap.value[item.slug] || [],
    image: item.image || fallbackImageMap[item.slug] || "/home/hero-1.png"
  }))
);

const loadPage = async () => {
  loading.value = true;
  try {
    categories.value = await getCategories(lang.value);
  } catch (error) {
    if (import.meta.env.SSR) throw error;
    categories.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadPage);
onServerPrefetch(loadPage);
watch(lang, loadPage);
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 0;
  max-width: none;
  margin: 0 auto;
}

.banner-strip {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}

.banner-card {
  height: clamp(230px, 20vw, 330px);
  border-radius: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.08)),
    url('/home/hero-2.png') center center/cover no-repeat;
  position: relative;
  overflow: hidden;
}

.breadcrumb-strip {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #1296e1;
}

.breadcrumb-inner {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  line-height: 20px;
  letter-spacing: 0.05em;
}

.breadcrumb-inner a {
  text-decoration: none;
}

.breadcrumb-inner a:hover,
.breadcrumb-inner .is-current {
  text-decoration: underline;
}

.solution-intro,
.feature-list {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
}

.solution-intro {
  display: grid;
  grid-template-columns: minmax(420px, 620px) minmax(320px, 520px);
  justify-content: space-between;
  gap: 32px;
  align-items: start;
  padding: 72px 0 44px;
}

.solution-heading {
  margin: 0;
  font-size: 54px;
  line-height: 1.08;
  font-weight: 500;
}

.solution-heading {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.solution-heading-primary {
  color: #1296e1;
}

.solution-heading-secondary {
  color: #9ca3b0;
}

.intro-title,
.intro-copy {
  min-width: 0;
}

.intro-copy {
  display: grid;
  gap: 10px;
  color: #656565;
  font-size: 20px;
  line-height: 1.5;
  justify-items: end;
  text-align: right;
  padding-top: 12px;
}

.intro-copy p {
  margin: 0;
  max-width: 430px;
}

.intro-copy-emphasis {
  color: #1296e1;
}

.feature-list {
  display: grid;
  gap: 0;
  border-top: 1px solid #dcdcdc;
  padding-bottom: 100px;
  background: transparent;
}

.feature-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0;
  padding: 50px 0;
  border-bottom: 1px solid #dcdcdc;
}

.feature-main-col {
  width: 76.31%;
  padding-right: 50px;
  display: grid;
  grid-template-columns: 40.68% 59.32%;
  align-items: start;
}

.feature-name-col {
  padding-right: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-formula {
  margin: 0;
  color: #bb0807;
  font-size: 16px;
  line-height: calc(19 / 16);
  font-weight: 500;
  letter-spacing: 0.05em;
}

.feature-name-col h3 {
  margin: 0;
  color: #333333;
  font-size: 36px;
  line-height: calc(40 / 36);
  font-weight: 500;
  transition: color 0.3s ease;
}

.feature-content-col {
  display: grid;
  gap: 20px;
  align-content: start;
  width: 100%;
}

.feature-summary {
  margin: 0;
  color: #656565;
  font-size: 24px;
  line-height: 1.625;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.feature-app-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.feature-app-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #000000;
  font-size: 16px;
  font-weight: 600;
  line-height: calc(26 / 16);
}

.app-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex: 0 0 10px;
  margin-top: 8px;
  background: linear-gradient(180deg, #2ba3eb, #1577cf);
}

.feature-image-col {
  display: flex;
  justify-content: flex-end;
  width: 23.69%;
}

.feature-image {
  width: 100%;
  max-width: none;
  aspect-ratio: 595 / 630;
  object-fit: cover;
  border-radius: 8px;
  background: #f6f9fc;
  transition: transform 0.35s ease;
}

.feature-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
  font-weight: 700;
  text-decoration: none;
}

.feature-link {
  padding: 10px 66px;
  min-height: 50px;
  width: fit-content;
  background: linear-gradient(251deg, #1080c0 12.92%, #1296e1 87.08%);
  color: #fff;
  border-radius: 8px;
  font-size: 18px;
  line-height: calc(30 / 18);
}

.loading-panel {
  min-height: 220px;
  display: grid;
  place-items: center;
  border-radius: 20px;
  border: 1px solid #d8e4f1;
  background: #fff;
  color: var(--pf-muted);
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto 100px;
}

.feature-block:hover .feature-name-col h3 {
  color: #bb0807;
}

.feature-block:hover .feature-image {
  transform: scale(1.1);
}

@media (max-width: 1120px) {
  .solution-intro {
    width: min(860px, calc(100vw - 40px));
    grid-template-columns: 1fr;
    gap: 30px;
    padding-top: 54px;
    padding-bottom: 38px;
  }

  .feature-block {
    width: min(860px, calc(100vw - 40px));
    margin: 0 auto;
    flex-direction: column;
    align-items: stretch;
    gap: 40px;
    padding: 20px 0;
  }

  .feature-list,
  .loading-panel,
  .breadcrumb-inner {
    width: min(860px, calc(100vw - 40px));
  }

  .feature-main-col {
    width: 100%;
    padding-right: 0;
    grid-template-columns: 1fr;
  }

  .feature-name-col {
    padding-right: 0;
    padding-bottom: 30px;
  }

  .feature-summary {
    min-height: auto;
    max-width: none;
  }

  .feature-image-col {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 720px) {
  .breadcrumb-inner,
  .solution-intro,
  .feature-list,
  .loading-panel {
    width: calc(100vw - 30px);
  }

  .breadcrumb-inner {
    min-height: 40px;
    gap: 8px;
    font-size: 14px;
  }

  .solution-intro {
    padding-top: 36px;
    padding-bottom: 28px;
  }

  .solution-heading {
    font-size: 42px;
    line-height: 1.15;
  }

  .intro-copy,
  .feature-summary {
    font-size: 18px;
    line-height: 1.6;
  }

  .intro-copy {
    justify-items: start;
    text-align: left;
  }

  .feature-name-col h3 {
    font-size: 30px;
    line-height: 1.2;
  }

  .feature-link {
    width: 100%;
    padding-left: 20px;
    padding-right: 20px;
    font-size: 16px;
  }
}
</style>

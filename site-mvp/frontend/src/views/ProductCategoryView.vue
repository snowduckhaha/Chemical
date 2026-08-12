<template>
  <div class="category-page page-shell" v-if="category">
    <section class="banner-strip">
      <img src="/products/banner-target.png" alt="" class="banner-card" aria-hidden="true" />
    </section>

    <section class="breadcrumb-strip" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <span class="breadcrumb-home" aria-hidden="true">⌂</span>
        <router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <router-link :to="`/${lang}/products`">{{ lang === "en" ? "Products" : "产品中心" }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <span class="is-current">{{ category.name }}</span>
      </div>
    </section>

    <section class="intro-block">
      <h1 class="page-title">{{ category.name }}</h1>
      <div class="intro-copy">
        <p>{{ category.summary }}</p>
      </div>
    </section>

    <section v-if="!loading" class="series-list">
      <article v-for="item in seriesEntries" :key="item.slug" class="series-card">
        <router-link :to="seriesLink(item.slug)" class="series-image-wrap">
          <img :src="item.image" :alt="item.name" class="series-image" />
        </router-link>
        <div class="series-card-body">
          <div class="series-name-col">
            <h2>
              <router-link :to="seriesLink(item.slug)">{{ item.name }}</router-link>
            </h2>
          </div>
          <p class="series-summary">{{ item.summary }}</p>
          <router-link :to="seriesLink(item.slug)" class="series-cta">
            {{ lang === "en" ? "Learn More" : "了解更多" }}
          </router-link>
        </div>
      </article>
    </section>

    <section v-else class="loading-panel">Loading...</section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getCategories, getSeries } from "../api/site";
import { getFallbackSeriesApplications } from "../data/productFallback";
import type { Category, Series } from "../types/site";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const categorySlug = computed(() => String(route.params.categorySlug || ""));
const loading = ref(true);
const category = ref<Category>();
const seriesList = ref<Series[]>([]);

const seriesLink = (seriesSlug: string) => `/${lang.value}/products/${categorySlug.value}/${seriesSlug}`;

const seriesEntries = computed(() =>
  seriesList.value
    .map((item) => ({
      ...item,
      applications: getFallbackSeriesApplications(lang.value, categorySlug.value, item.slug)
    }))
);

const loadPage = async () => {
  loading.value = true;
  try {
    const [categories, series] = await Promise.all([getCategories(lang.value), getSeries(lang.value, categorySlug.value)]);
    category.value = categories.find((item) => item.slug === categorySlug.value);
    seriesList.value = series;
  } catch (error) {
    if (import.meta.env.SSR) throw error;
    category.value = undefined;
    seriesList.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadPage);
onServerPrefetch(loadPage);
watch([lang, categorySlug], loadPage);
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 0;
  max-width: none;
  margin: 0 auto;
}

.banner-strip,
.breadcrumb-strip {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}

.banner-card {
  height: clamp(230px, 20vw, 330px);
  width: 100%;
  display: block;
  object-fit: cover;
}

.breadcrumb-strip {
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

.breadcrumb-home {
  font-size: 18px;
  line-height: 1;
  margin-right: 4px;
}

.intro-block,
.series-list,
.loading-panel {
  width: min(1440px, calc(100vw - 160px));
  margin: 0 auto;
}

.intro-block {
  display: grid;
  gap: 20px;
  align-items: start;
  padding: 54px 0 34px;
}

.page-title {
  margin: 0;
  color: #000;
  font-size: 54px;
  line-height: 1.12;
  font-weight: 500;
  letter-spacing: 0.015em;
}

.intro-copy {
  display: grid;
  gap: 18px;
  color: #656565;
  font-size: 24px;
  line-height: calc(26 / 16);
}

.intro-copy p {
  margin: 0;
  max-width: 968px;
}

.series-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 26px 22px;
  padding-top: 8px;
  padding-bottom: 84px;
}

.series-card {
  display: grid;
  gap: 0;
  align-content: start;
  background: #fff;
  box-shadow: 0 3px 10px rgba(20, 63, 110, 0.08);
}

.series-image-wrap {
  overflow: hidden;
  display: block;
  border-radius: 0;
  position: relative;
}

.series-image-wrap::after {
  content: "";
  position: absolute;
  inset: auto 0 0;
  height: 38%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.92) 70%, #ffffff 100%);
  pointer-events: none;
}

.series-name-col {
  padding-right: 0;
}

.series-name-col a {
  color: inherit;
  text-decoration: none;
}

.series-name-col h2 {
  margin: 0;
  color: #333;
  font-size: 36px;
  line-height: calc(40 / 36);
  font-weight: 500;
  transition: color 0.3s ease;
}

.series-card-body {
  display: grid;
  gap: 12px;
  align-content: start;
  padding: 0 20px 20px;
  margin-top: -18px;
  position: relative;
  z-index: 1;
  background: #fff;
}

.series-summary {
  margin: 0;
  color: #656565;
  font-size: 18px;
  line-height: 1.67;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  min-height: calc(1.6em * 3);
}

.series-cta {
  justify-self: start;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 10px 28px;
  border-radius: 8px;
  background: linear-gradient(251deg, #1080c0 12.92%, #1296e1 87.08%);
  color: #fff;
  font-size: 18px;
  line-height: calc(30 / 18);
  font-weight: 500;
  text-decoration: none;
}

.series-image {
  width: 100%;
  aspect-ratio: 1.28 / 1;
  object-fit: cover;
  background: #f6f9fc;
  transition: transform 0.35s ease;
}

.series-card:hover .series-name-col h2 {
  color: #bb0807;
}

.series-card:hover .series-image {
  transform: scale(1.04);
}

.loading-panel {
  min-height: 220px;
  display: grid;
  place-items: center;
  border: 1px solid #d8e4f1;
  background: #fff;
  color: var(--pf-muted);
  margin-bottom: 100px;
}

@media (max-width: 1120px) {
  .intro-block,
  .series-list,
  .loading-panel,
  .breadcrumb-inner {
    width: min(860px, calc(100vw - 40px));
  }

  .intro-block {
    gap: 24px;
    padding-top: 40px;
    padding-bottom: 26px;
  }

  .page-title {
    font-size: 46px;
    letter-spacing: 0.01em;
  }

  .series-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
    padding-top: 8px;
  }
}

@media (max-width: 720px) {
  .breadcrumb-inner,
  .intro-block,
  .series-list,
  .loading-panel {
    width: calc(100vw - 30px);
  }

  .breadcrumb-inner {
    min-height: 40px;
    gap: 8px;
    font-size: 14px;
  }

  .page-title {
    font-size: 40px;
    line-height: 1.15;
    letter-spacing: 0.005em;
  }

  .intro-copy,
  .series-summary {
    font-size: 18px;
    line-height: 1.58;
  }

  .series-list {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .series-name-col h2 {
    font-size: 30px;
    line-height: 1.2;
  }
}
</style>

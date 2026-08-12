<template>
  <div class="series-page page-shell" v-if="seriesInfo">
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
        <router-link :to="`/${lang}/products/${categorySlug}`">{{ categoryName }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <span class="is-current">{{ seriesInfo.name }}</span>
      </div>
    </section>

    <section class="intro-block">
      <h1 class="page-title">{{ seriesInfo.name }}</h1>
      <div class="intro-copy">
        <p>{{ seriesInfo.summary }}</p>
        <ul class="app-list">
          <li v-for="item in seriesApplications" :key="`${seriesInfo.slug}-${item}`">
            <span class="app-dot" aria-hidden="true"></span>
            <span>{{ item }}</span>
          </li>
        </ul>
      </div>
    </section>

    <section v-if="!loading" class="product-list">
      <article v-for="item in products" :key="item.slug" class="product-card">
        <router-link :to="productLink(item.slug)" class="product-image-wrap">
          <img :src="item.image" :alt="item.name" class="product-image" />
        </router-link>
        <div class="product-card-body">
          <div class="product-name-col">
            <h2>
              <router-link :to="productLink(item.slug)">{{ item.name }}</router-link>
            </h2>
          </div>
          <p class="product-summary">{{ item.model }}</p>
        </div>
      </article>
    </section>

    <section v-else class="loading-panel">Loading...</section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getCategories, getProducts, getSeries } from "../api/site";
import {
  getFallbackSeriesApplications
} from "../data/productFallback";
import type { Product, Series } from "../types/site";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const categorySlug = computed(() => String(route.params.categorySlug || ""));
const seriesSlug = computed(() => String(route.params.seriesSlug || ""));
const loading = ref(true);
const products = ref<Product[]>([]);
const seriesInfo = ref<Series>();
const categoryName = ref("");

const productLink = (productSlug: string) =>
  `/${lang.value}/products/${categorySlug.value}/${seriesSlug.value}/${productSlug}`;

const seriesApplications = computed(() =>
  seriesInfo.value?.applications?.length
    ? seriesInfo.value.applications
    : getFallbackSeriesApplications(lang.value, categorySlug.value, seriesSlug.value)
);

const loadPage = async () => {
  loading.value = true;
  try {
    const [categories, seriesList, productList] = await Promise.all([
      getCategories(lang.value),
      getSeries(lang.value, categorySlug.value),
      getProducts(lang.value, categorySlug.value, seriesSlug.value)
    ]);
    categoryName.value =
      categories.find((item) => item.slug === categorySlug.value)?.name ||
      categorySlug.value;
    seriesInfo.value = seriesList.find((item) => item.slug === seriesSlug.value);
    products.value = productList;
  } catch (error) {
    if (import.meta.env.SSR) throw error;
    categoryName.value = categorySlug.value;
    seriesInfo.value = undefined;
    products.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadPage);
onServerPrefetch(loadPage);
watch([lang, categorySlug, seriesSlug], loadPage);
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
.product-list,
.parameter-summary-block,
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
  gap: 16px;
  color: #656565;
  font-size: 24px;
  line-height: calc(26 / 16);
}

.intro-copy p {
  margin: 0;
  max-width: 968px;
}

.app-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.app-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #000;
  font-size: 16px;
  line-height: calc(26 / 16);
  font-weight: 500;
}

.app-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex: 0 0 12px;
  margin-top: 7px;
  position: relative;
  background: linear-gradient(180deg, #2ba3eb, #1577cf);
}

.app-dot::after {
  content: "";
  position: absolute;
  left: 3px;
  top: 2px;
  width: 3px;
  height: 6px;
  border-right: 2px solid #fff;
  border-bottom: 2px solid #fff;
  transform: rotate(40deg);
}

.product-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 26px 18px;
  padding-top: 8px;
  padding-bottom: 84px;
}

.product-card {
  display: grid;
  gap: 14px;
  align-content: start;
}

.product-image-wrap {
  overflow: hidden;
  display: block;
  border-radius: 0;
}

.product-name-col {
  padding-right: 0;
  text-align: center;
}

.product-name-col a {
  color: inherit;
  text-decoration: none;
}

.product-name-col h2 {
  margin: 0;
  color: #333;
  font-size: 28px;
  line-height: calc(34 / 28);
  font-weight: 500;
  transition: color 0.3s ease;
}

.product-card-body {
  display: grid;
  gap: 4px;
  align-content: start;
}

.product-summary {
  margin: 0;
  color: #8b8b8b;
  font-size: 16px;
  line-height: 1.5;
  text-align: center;
}

.product-image {
  width: 100%;
  aspect-ratio: 1.08 / 1;
  object-fit: cover;
  background: #f6f9fc;
  transition: transform 0.35s ease;
}

.product-card:hover .product-name-col h2 {
  color: #bb0807;
}

.product-card:hover .product-image {
  transform: scale(1.04);
}

.loading-panel {
  min-height: 220px;
  display: grid;
  place-items: center;
  border: 1px solid #d8e4f1;
  background: #fff;
  color: var(--pf-muted);
  margin-bottom: 40px;
}

@media (max-width: 1120px) {
  .intro-block,
  .product-list,
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

  .product-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
    padding-top: 8px;
  }
}

@media (max-width: 720px) {
  .breadcrumb-inner,
  .intro-block,
  .product-list,
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

  .product-name-col h2 {
    font-size: 30px;
    line-height: 1.2;
  }

  .intro-copy,
  .product-summary {
    font-size: 18px;
    line-height: 1.58;
  }

  .product-list {
    grid-template-columns: 1fr;
    gap: 22px;
  }
}
</style>

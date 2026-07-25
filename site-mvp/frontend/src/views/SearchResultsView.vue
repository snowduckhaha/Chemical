<template>
  <div class="search-page page-shell">
    <section class="search-banner">
      <div class="banner-inner">
        <h1>{{ lang === "zh" ? "搜索结果" : "Search Results" }}</h1>
        <p class="search-query-text">
          {{ lang === "zh" ? "关键词：" : "Keyword: " }}
          <strong>"{{ keyword }}"</strong>
        </p>
      </div>
    </section>

    <section class="search-results">
      <p v-if="loading" class="state">{{ lang === "zh" ? "正在搜索..." : "Searching..." }}</p>
      <p v-else-if="error" class="state error">{{ lang === "zh" ? "搜索失败，请稍后重试。" : "Search failed. Please try again later." }}</p>
      <p v-else-if="results.length === 0" class="state empty">
        {{ lang === "zh" ? "未找到相关产品，请尝试其他关键词。" : "No products found. Please try a different keyword." }}
      </p>
      <div v-else class="results-grid">
        <router-link
          v-for="item in results"
          :key="item.id"
          :to="productLink(item)"
          class="result-card"
        >
          <div class="result-image-wrap">
            <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.imageAlt || item.name" class="result-image" />
            <span v-else class="result-placeholder"></span>
          </div>
          <div class="result-body">
            <h3>{{ item.name }}</h3>
            <p v-if="item.summary">{{ item.summary }}</p>
            <span class="result-link">{{ lang === "zh" ? "查看详情" : "View Details" }} →</span>
          </div>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { searchProducts } from "../api/site";
import type { Product } from "../types/site";

const route = useRoute();
const router = useRouter();

const lang = computed(() => String(route.params.lang || "zh"));
const keyword = computed(() => String(route.query.q || ""));

const results = ref<Product[]>([]);
const loading = ref(false);
const error = ref(false);

const productLink = (item: Product) => {
  if (item.categorySlug && item.seriesSlug && item.slug) {
    return `/${lang.value}/products/${item.categorySlug}/${item.seriesSlug}/${item.slug}`;
  }
  if (item.categorySlug && item.slug) {
    return `/${lang.value}/products/${item.categorySlug}?product=${item.slug}`;
  }
  return `/${lang.value}/products`;
};

const load = async () => {
  const kw = keyword.value.trim();
  if (!kw) {
    results.value = [];
    return;
  }

  loading.value = true;
  error.value = false;
  try {
    results.value = await searchProducts(lang.value, kw);
  } catch {
    error.value = true;
    results.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(load);
watch([lang, keyword], load);
</script>

<style scoped>
.search-page {
  padding-bottom: 80px;
}

.search-banner {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: linear-gradient(120deg, #f0f7ff 0%, #e2effd 100%);
  padding: 60px 0 48px;
}

.banner-inner {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
  padding: 0 24px;
}

.banner-inner h1 {
  margin: 0 0 12px;
  color: #1296e1;
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 600;
}

.search-query-text {
  margin: 0;
  color: #55718f;
  font-size: 1.05rem;
}

.search-query-text strong {
  color: #18314f;
  font-weight: 600;
}

.search-results {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 48px auto 0;
  padding: 0 24px;
}

.state {
  text-align: center;
  padding: 60px 20px;
  color: #60758f;
  font-size: 1.05rem;
}

.state.error {
  color: #b42318;
}

.state.empty {
  color: #8fa3bd;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.result-card {
  display: block;
  background: #fff;
  border: 1px solid #dce6f2;
  border-radius: 10px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.result-card:hover {
  box-shadow: 0 12px 32px rgba(13, 61, 132, 0.12);
  transform: translateY(-3px);
}

.result-image-wrap {
  width: 100%;
  aspect-ratio: 4 / 3;
  background: #f4f8fc;
  overflow: hidden;
}

.result-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.result-card:hover .result-image {
  transform: scale(1.04);
}

.result-placeholder {
  display: block;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e6eef8 0%, #f4f8fc 100%);
}

.result-body {
  padding: 18px 20px 22px;
}

.result-body h3 {
  margin: 0 0 8px;
  color: #18314f;
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.35;
}

.result-body p {
  margin: 0 0 12px;
  color: #60758f;
  font-size: 0.92rem;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.result-link {
  display: inline-block;
  color: #1296e1;
  font-size: 0.88rem;
  font-weight: 500;
}

@media (max-width: 1024px) {
  .results-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .search-banner {
    padding: 44px 0 36px;
  }

  .banner-inner h1 {
    font-size: 1.75rem;
  }

  .results-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .search-results {
    margin-top: 32px;
  }
}
</style>

<template>
  <section class="news-page">
    <div class="news-banner" aria-hidden="true"></div>
    <nav class="breadcrumb" :aria-label="lang === 'en' ? 'Breadcrumb' : '面包屑'"><router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link><span>/</span><span>{{ lang === "en" ? "News" : "资讯中心" }}</span></nav>
    <header class="title-block"><h1>{{ lang === "en" ? "News Center" : "资讯中心" }}</h1></header>
    <div v-if="categories.length > 0" class="category-picker">
      <label for="news-category">{{ lang === "en" ? "Browse by category" : "按分类浏览" }}</label>
      <select id="news-category" :value="selectedCategory" @change="goToCategory">
        <option value="" disabled>{{ lang === "en" ? "Select a category" : "请选择资讯分类" }}</option>
        <option v-for="category in categories" :key="category.slug" :value="category.slug">{{ category.name }}</option>
      </select>
    </div>
    <p v-if="loading" class="state">{{ lang === "en" ? "Loading articles…" : "正在加载资讯…" }}</p>
    <p v-else-if="error" class="state error">{{ lang === "en" ? "Unable to load articles." : "资讯加载失败，请稍后重试。" }}</p>
    <section v-else-if="newsList.length" class="latest"><h2>{{ lang === "en" ? "Latest Insights" : "最新资讯" }}</h2><div class="news-grid"><article v-for="item in newsList.slice(0, 4)" :key="item.slug" class="news-card"><router-link :to="`/${lang}/news/${item.categorySlug}/${item.slug}`"><img v-if="item.coverImage" :src="item.coverImage" :alt="item.coverAlt || item.title" /><span v-else class="image-placeholder"></span><h3>{{ item.title }}</h3></router-link></article></div></section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getNews, getNewsCategories } from "../api/site";
import type { News, NewsCategory } from "../types/site";

const route = useRoute();
const router = useRouter();
const lang = String(route.params.lang || "zh");
const loading = ref(true); const error = ref(false);
const newsList = ref<News[]>([]);
const categories = ref<NewsCategory[]>([]);
const selectedCategory = ref("");

const goToCategory = (event: Event) => {
  const categorySlug = (event.target as HTMLSelectElement).value;
  if (categorySlug) void router.push(`/${lang}/news/${categorySlug}`);
};

onMounted(async () => {
  try { const [allNews, categoryData] = await Promise.all([getNews(lang), getNewsCategories(lang)]); newsList.value = allNews; categories.value = categoryData; }
  catch { error.value = true; } finally { loading.value = false; }
});
</script>

<style scoped>
.news-page{padding-bottom:72px}.news-banner{height:clamp(210px,33vw,420px);background:linear-gradient(90deg,rgba(12,62,116,.2),rgba(12,62,116,.05)),url('/news/reference-news-banner.webp') center/cover}.breadcrumb{background:#0759a9;color:#fff;padding:17px max(20px,calc((100% - 1200px)/2));display:flex;gap:9px;font-size:.88rem}.breadcrumb a{color:#fff;text-decoration:none}.title-block{text-align:center;padding:68px 20px 38px}.title-block span{color:#a8b0b9;font-size:.8rem;letter-spacing:.12em}.title-block h1{margin:10px 0;color:#0759a9;font-size:clamp(2rem,4vw,3rem)}.category-picker,.latest{width:min(1200px,calc(100% - 40px));margin:0 auto}.category-picker{display:grid;gap:9px;max-width:720px}.category-picker label{color:#0759a9;font-weight:700}.category-picker select{width:100%;min-height:48px;border:1px solid #cad5e1;background:#fff;padding:0 14px;color:#1d2b3a;font:inherit}.latest{padding-top:68px}.latest h2{text-align:center;color:#0759a9;margin-bottom:28px}.news-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:36px}.news-card a{color:#1d2b3a;text-decoration:none}.news-card img,.image-placeholder{display:block;width:100%;aspect-ratio:16/9;object-fit:contain;background:#f5f7f9;transition:transform .25s}.news-card{overflow:hidden}.news-card:hover img{transform:scale(1.04)}.news-card h3{font-size:1.15rem;line-height:1.5;margin:14px 0 0}.state{text-align:center;padding:48px;color:#68778a}.error{color:#b42318}@media(max-width:767px){.news-grid{grid-template-columns:1fr}.title-block{padding:48px 20px 28px}.news-banner{height:260px}}
</style>

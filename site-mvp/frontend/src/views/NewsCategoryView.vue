<template>
  <section class="news-page">
    <div class="news-banner" aria-hidden="true"></div><nav class="breadcrumb"><router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link><span>/</span><router-link :to="`/${lang}/news`">{{ lang === "en" ? "News" : "资讯中心" }}</router-link><span>/</span><span>{{ categoryName }}</span></nav>
    <header class="title-block"><h1>{{ categoryName }}</h1></header>
    <p v-if="loading" class="state">{{ lang === "en" ? "Loading articles…" : "正在加载资讯…" }}</p><p v-else-if="error" class="state error">{{ lang === "en" ? "Sorry, this news category was not found." : "抱歉，未找到该资讯分类。" }}</p>
    <template v-else><p v-if="!newsPage.items.length" class="state">{{ lang === "en" ? "No articles are available in this category yet." : "暂无该分类资讯，欢迎浏览其他资讯内容。" }}</p><div v-else class="news-grid"><article v-for="item in newsPage.items" :key="item.slug" class="news-card"><router-link :to="`/${lang}/news/${item.categorySlug}/${item.slug}`"><img v-if="item.coverImage" :src="item.coverImage" :alt="item.coverAlt || item.title" /><span v-else class="image-placeholder"></span><h2>{{ item.title }}</h2></router-link></article></div><nav v-if="totalPages > 1" class="pager"><button :disabled="page === 1" @click="load(page - 1)">←</button><span>{{ page }} / {{ totalPages }}</span><button :disabled="page === totalPages" @click="load(page + 1)">→</button></nav></template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getLegacyNewsDetail, getNewsCategories, getNewsPageByCategory } from "../api/site";
import type { NewsPage } from "../types/site";

const route = useRoute();
const router = useRouter();
const lang = computed(() => String(route.params.lang || "zh")); const categorySlug = computed(() => String(route.params.categorySlug || "")); const loading = ref(true); const error = ref(false); const page = ref(1); const newsPage = ref<NewsPage>({ items: [], total: 0, page: 1, pageSize: 8 }); const categoryName = ref(""); const totalPages = computed(() => Math.max(1, Math.ceil(newsPage.value.total / newsPage.value.pageSize)));

const load = async (nextPage = 1) => { loading.value = true; error.value = false; try { const categories = await getNewsCategories(lang.value); const category = categories.find(item => item.slug === categorySlug.value); if (!category) { const legacyArticle = await getLegacyNewsDetail(lang.value, categorySlug.value); await router.replace(`/${lang.value}/news/${legacyArticle.categorySlug}/${legacyArticle.slug}`); return; } const result = await getNewsPageByCategory(lang.value, categorySlug.value, nextPage); categoryName.value = category.name; newsPage.value = result; page.value = result.page; } catch { error.value = true; } finally { loading.value = false; } };
onMounted(load); watch([lang, categorySlug], () => load(1));
</script>

<style scoped>
.news-page{padding-bottom:72px}.news-banner{height:clamp(210px,33vw,420px);background:url('/news/reference-news-banner.webp') center/cover}.breadcrumb{background:#0759a9;color:#fff;padding:17px max(20px,calc((100% - 1200px)/2));display:flex;gap:9px;font-size:.88rem}.breadcrumb a{color:#fff;text-decoration:none}.title-block{text-align:center;padding:68px 20px 38px}.title-block h1{margin:10px 0;color:#0759a9;font-size:clamp(2rem,4vw,3rem)}.news-grid{width:min(1200px,calc(100% - 40px));margin:auto;display:grid;grid-template-columns:repeat(2,1fr);gap:42px 32px}.news-card{overflow:hidden}.news-card a{color:#1d2b3a;text-decoration:none}.news-card img,.image-placeholder{display:block;width:100%;aspect-ratio:16/9;object-fit:contain;background:#f5f7f9;transition:transform .25s}.news-card:hover img{transform:scale(1.04)}.news-card h2{font-size:1.15rem;line-height:1.5;margin:14px 0 0}.state{text-align:center;padding:48px;color:#68778a}.error{color:#b42318}.pager{display:flex;justify-content:center;gap:18px;align-items:center;padding:50px 0}.pager button{border:1px solid #cad5e1;background:#fff;color:#0759a9;padding:7px 12px;cursor:pointer}.pager button:disabled{opacity:.4;cursor:not-allowed}@media(max-width:767px){.news-grid{grid-template-columns:1fr}.title-block{padding:48px 20px 28px}.news-banner{height:260px}}
</style>

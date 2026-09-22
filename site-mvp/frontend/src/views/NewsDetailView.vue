<template>
  <section v-if="news" class="article-page">
    <div class="news-banner" aria-hidden="true"></div><nav class="breadcrumb"><router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link><span>/</span><router-link :to="`/${lang}/news`">{{ lang === "en" ? "News" : "资讯中心" }}</router-link><span>/</span><router-link :to="`/${lang}/news/${news.categorySlug}`">{{ news.category }}</router-link><span>/</span><span>{{ news.title }}</span></nav>
    <article class="article"><p class="category">{{ news.category }}</p><h1>{{ news.title }}</h1><time>{{ news.publishedAt }}</time><img v-if="news.coverImage" class="cover" :src="news.coverImage" :alt="news.coverAlt || news.title" /><p v-if="news.summary" class="summary">{{ news.summary }}</p><div class="content" v-html="safeContent"></div>
      <section v-if="news.recommendedProductSlugs.length" class="related"><h2>{{ lang === "en" ? "Recommended Products" : "推荐产品" }}</h2><div class="tags"><router-link v-for="slug in news.recommendedProductSlugs" :key="slug" :to="`/${lang}/products`">{{ slug }}</router-link></div></section>
      <section v-if="news.relatedSlugs.length" class="related"><h2>{{ lang === "en" ? "Related Articles" : "相关文章" }}</h2><ul><li v-for="slug in news.relatedSlugs" :key="slug"><router-link :to="`/${lang}/news/${news.categorySlug}/${slug}`">{{ slug }}</router-link></li></ul></section>
      <section class="cta"><h2>{{ lang === "en" ? "Discuss Your Project" : "讨论您的项目" }}</h2><router-link :to="`/${lang}/contact?source=news-article-cta`" @click="trackInquiry">{{ lang === "en" ? "Request a Quote" : "索取报价" }}</router-link></section>
    </article>
  </section>
  <section v-else-if="error" class="state error">{{ lang === "en" ? "Sorry, this article was not found." : "抱歉，未找到该资讯。" }}</section>
  <section v-else class="state">{{ lang === "en" ? "Loading article…" : "正在加载资讯…" }}</section>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getNewsDetail } from "../api/site";
import type { NewsDetail } from "../types/site";
import { useJsonLd } from "../composables/useJsonLd";
import { articleSchema, breadcrumbSchema, siteOrigin } from "../lib/jsonLd";
import { trackInquiryCta } from "../analytics";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const articleSlug = computed(() => String(route.params.articleSlug || "")); const categorySlug = computed(() => String(route.params.categorySlug || "")); const news = ref<NewsDetail>(); const error = ref(false);
const trackInquiry = () => trackInquiryCta("news.detail", {
  cta_key: "news_article_contact",
  cta_position: "news_article_cta",
  article_slug: articleSlug.value
});
const sanitizeContent = (value: string) => value
  .replace(/<\s*(script|style|iframe|object|embed)[^>]*>[\s\S]*?<\/\s*\1\s*>/gi, "")
  .replace(/<\s*(script|style|iframe|object|embed)[^>]*\/?>/gi, "")
  .replace(/\son\w+\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]+)/gi, "")
  .replace(/\s(?:href|src)\s*=\s*(?:"\s*javascript:[^"]*"|'\s*javascript:[^']*'|javascript:[^\s>]*)/gi, "");
const safeContent = computed(() => sanitizeContent(news.value?.content || ""));

const pageUrl = computed(() => `${siteOrigin}/${lang.value}/news/${categorySlug.value}/${articleSlug.value}`);
useJsonLd(computed(() => {
  if (!news.value) return [];
  return [
    articleSchema(news.value, pageUrl.value, lang.value),
    breadcrumbSchema(pageUrl.value, [
      { name: lang.value === "en" ? "Home" : "首页", url: `${siteOrigin}/${lang.value}` },
      { name: lang.value === "en" ? "News" : "资讯中心", url: `${siteOrigin}/${lang.value}/news` },
      { name: news.value.category, url: `${siteOrigin}/${lang.value}/news/${news.value.categorySlug}` },
      { name: news.value.title }
    ])
  ];
}));

const loadNews = async () => {
  error.value = false; news.value = undefined; try { news.value = await getNewsDetail(lang.value, categorySlug.value, articleSlug.value); } catch (loadError) { if (import.meta.env.SSR) throw loadError; error.value = true; }
};

onMounted(loadNews);
onServerPrefetch(loadNews);
watch([lang, categorySlug, articleSlug], loadNews);
</script>

<style scoped>
.news-banner{height:clamp(190px,28vw,360px);background:url('/news/reference-news-banner.webp') center/cover}.breadcrumb{background:#0759a9;color:#fff;padding:17px max(20px,calc((100% - 1200px)/2));display:flex;gap:9px;font-size:.84rem;white-space:nowrap;overflow:hidden}.breadcrumb a{color:#fff;text-decoration:none}.article{width:min(860px,calc(100% - 40px));margin:0 auto;padding:58px 0 72px}.category{color:#0759a9;font-weight:700}.article h1{font-size:clamp(2rem,4vw,2.7rem);line-height:1.25;margin:.4rem 0}.article time{color:#718096}.cover{display:block;width:100%;aspect-ratio:16/9;object-fit:contain;background:#f5f7f9;margin:32px 0}.summary{font-size:1.1rem;line-height:1.8;color:#425466;border-left:3px solid #0759a9;padding-left:18px}.content{line-height:1.85}.content :deep(p){margin:0 0 1em}.content :deep(img){max-width:100%;height:auto}.content :deep(table){width:100%;margin:1.5em 0;border-collapse:collapse;font-size:.95em}.content :deep(th),.content :deep(td){border:1px solid #cbd5e1;padding:.7em .8em;text-align:left;vertical-align:top}.content :deep(th){background:#edf5fd;color:#1d4e89;font-weight:700}.content :deep(tbody tr:nth-child(even)){background:#f8fafc}.related{margin-top:48px}.related h2{color:#0759a9;font-size:1.35rem}.tags{display:flex;flex-wrap:wrap;gap:8px}.tags a,.related li a{color:#0759a9}.tags a{background:#edf5fd;padding:7px 11px;text-decoration:none}.cta{margin-top:48px;padding:28px;background:#f3f7fb}.cta h2{margin-top:0}.cta a{display:inline-block;background:#0759a9;color:#fff;padding:11px 18px;text-decoration:none}.state{text-align:center;padding:100px 20px;color:#68778a}.error{color:#b42318}@media(max-width:650px){.content :deep(table){display:block;overflow-x:auto;white-space:nowrap}.content :deep(th),.content :deep(td){min-width:110px}}
</style>

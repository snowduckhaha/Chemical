<template>
  <div v-if="detail" class="detail-page">
    <section class="banner-strip"><img :src="detail.image" :alt="detail.name" /></section>
    <section class="breadcrumb-strip"><div class="breadcrumb-inner"><router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link><span>/</span><router-link :to="`/${lang}/applications`">{{ lang === "en" ? "Applications" : "应用领域" }}</router-link><span>/</span><span>{{ detail.name }}</span></div></section>
    <section class="intro"><h1>{{ detail.name }}</h1><div class="intro-grid"><p class="overview">{{ detail.overview }}</p><ul><li v-for="item in detail.highlights" :key="item"><span></span>{{ item }}</li></ul></div></section>
    <section v-if="detail.linkedSeries.length" class="series-section"><div class="section-heading"><h2>{{ lang === "en" ? "Suitable Product Series" : "适用产品系列" }}</h2></div><div class="series-list"><article v-for="item in detail.linkedSeries" :key="item.seriesSlug" class="series-card"><router-link :to="`/${lang}/products/${item.categorySlug}/${item.seriesSlug}`" class="series-image"><img :src="item.image" :alt="item.title" loading="lazy" /></router-link><div class="series-body"><h3><router-link :to="`/${lang}/products/${item.categorySlug}/${item.seriesSlug}`">{{ item.title }}</router-link></h3><p>{{ item.summary }}</p><router-link :to="`/${lang}/products/${item.categorySlug}/${item.seriesSlug}`" class="series-cta">{{ lang === "en" ? "Learn More" : "了解更多" }}</router-link></div></article></div></section>
    <section v-if="faqEntries.length" class="faq-section" aria-labelledby="faq-title"><h2 id="faq-title">{{ lang === "en" ? "Application Selection FAQ" : "应用选型常见问题" }}</h2><dl><div v-for="entry in faqEntries" :key="entry.question"><dt>{{ entry.question }}</dt><dd>{{ entry.answer }}</dd></div></dl></section>
    <section class="inquiry-cta"><p>{{ lang === "en" ? "Need help matching materials to your application?" : "需要根据应用场景匹配合适材料？" }}</p><router-link :to="`/${lang}/contact?application=${detail.slug}&source=application-detail`" @click="trackInquiry">{{ lang === "en" ? "Request a Recommendation" : "获取选型建议" }}</router-link></section>
  </div>
  <section v-else class="loading-panel">{{ lang === "en" ? "Loading..." : "正在加载…" }}</section>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getApplicationDetail } from "../api/site";
import { trackInquiryCta } from "../analytics";
import { useJsonLd } from "../composables/useJsonLd";
import { breadcrumbSchema, faqPageSchema, siteOrigin } from "../lib/jsonLd";
import type { Application } from "../types/site";
const route = useRoute(); const lang = computed(() => String(route.params.lang || "zh")); const slug = computed(() => String(route.params.applicationSlug || "")); const detail = ref<Application>();
const pageUrl = computed(() => `${siteOrigin}/${lang.value}/applications/${slug.value}`);
const faqEntries = computed(() => (detail.value?.faqs || []).map((question) => ({
  question,
  answer: lang.value === "en"
    ? `For ${detail.value?.name || "this application"}, select materials against your required particle size, dispersion, flame-retardant performance and processing conditions. Contact Origin Chemical for a formulation-specific recommendation.`
    : `针对${detail.value?.name || "该应用"}，应结合目标粒径、分散性、阻燃性能与加工条件进行选型；可联系起点化工获取配方级建议。`
})));
useJsonLd(computed(() => {
  if (!detail.value) return [];
  return [
    breadcrumbSchema(pageUrl.value, [
      { name: lang.value === "en" ? "Home" : "首页", url: `${siteOrigin}/${lang.value}` },
      { name: lang.value === "en" ? "Applications" : "应用领域", url: `${siteOrigin}/${lang.value}/applications` },
      { name: detail.value.name }
    ]),
    faqPageSchema(faqEntries.value, pageUrl.value, lang.value)
  ];
}));
const load = async () => { detail.value = undefined; try { detail.value = await getApplicationDetail(lang.value, slug.value); } catch (error) { if (import.meta.env.SSR) throw error; detail.value = undefined; } };
const trackInquiry = () => trackInquiryCta("application.detail", {
  cta_key: "application_inquiry",
  cta_position: "application_detail",
  application_slug: slug.value
});
onMounted(load); onServerPrefetch(load); watch([lang, slug], load);
</script>

<style scoped>
.detail-page{background:#fff}.banner-strip,.breadcrumb-strip{width:100vw;margin-left:calc(50% - 50vw)}.banner-strip img{width:100%;height:clamp(230px,20vw,330px);display:block;object-fit:cover}.breadcrumb-strip{background:#1296e1}.breadcrumb-inner,.intro,.series-section,.inquiry-cta,.loading-panel{width:min(1440px,calc(100vw - 160px));margin:0 auto}.breadcrumb-inner{min-height:42px;display:flex;align-items:center;gap:10px;color:#fff;font-size:16px}.breadcrumb-inner a{text-decoration:none}.breadcrumb-inner a:hover{text-decoration:underline}.intro{padding:76px 0 64px;border-bottom:1px solid #dcdcdc}.intro>p,.section-heading>p{margin:0 0 16px;color:#1296e1;font-size:18px;letter-spacing:.05em}.intro h1,.section-heading h2{margin:0;color:#333;font-size:54px;font-weight:500;line-height:1.15}.intro-grid{display:grid;grid-template-columns:minmax(0,968px) 280px;justify-content:space-between;gap:44px;margin-top:30px}.overview{margin:0;color:#656565;font-size:23px;line-height:1.55}.intro ul{margin:0;padding:0;list-style:none;display:grid;gap:10px}.intro li{display:flex;gap:10px;align-items:center;color:#333;font-size:16px}.intro li span{width:8px;height:8px;border-radius:50%;background:#1296e1}.series-section{padding:70px 0 84px}.section-heading{margin-bottom:32px}.section-heading h2{font-size:42px}.series-list{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:22px}.series-card{background:#fff;box-shadow:0 3px 10px rgba(20,63,110,.08)}.series-image{display:block;overflow:hidden;position:relative}.series-image::after{content:"";position:absolute;inset:auto 0 0;height:38%;background:linear-gradient(180deg,transparent,rgba(255,255,255,.95),#fff)}.series-image img{width:100%;aspect-ratio:1.45/1;object-fit:cover;display:block;transition:transform .35s}.series-body{padding:0 20px 22px;margin-top:-18px;position:relative;z-index:1;background:#fff}.series-body h3{margin:0 0 12px;font-size:30px;font-weight:500}.series-body h3 a{color:#333;text-decoration:none}.series-body p{min-height:4.8em;margin:0 0 17px;color:#656565;font-size:17px;line-height:1.6;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:3;overflow:hidden}.series-cta,.inquiry-cta a{display:inline-flex;padding:10px 28px;border-radius:8px;background:linear-gradient(251deg,#1080c0,#1296e1);color:#fff;text-decoration:none;font-size:17px}.series-card:hover img{transform:scale(1.04)}.series-card:hover h3 a{color:#bb0807}.inquiry-cta{display:flex;align-items:center;justify-content:space-between;gap:30px;padding:38px 0 96px;border-top:1px solid #dcdcdc}.inquiry-cta p{margin:0;color:#333;font-size:22px}.loading-panel{min-height:300px;display:grid;place-items:center;color:#687586}@media(max-width:1120px){.breadcrumb-inner,.intro,.series-section,.inquiry-cta,.loading-panel{width:min(860px,calc(100vw - 40px))}.intro-grid{grid-template-columns:1fr}.series-list{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:720px){.breadcrumb-inner,.intro,.series-section,.inquiry-cta,.loading-panel{width:calc(100vw - 30px)}.breadcrumb-inner{font-size:14px}.intro{padding:50px 0}.intro h1{font-size:38px}.overview{font-size:18px}.series-section{padding:50px 0}.section-heading h2{font-size:32px}.series-list{grid-template-columns:1fr}.inquiry-cta{display:grid;padding:30px 0 64px}.inquiry-cta p{font-size:19px}}
.faq-section{width:min(1440px,calc(100vw - 160px));margin:0 auto;padding:18px 0 54px}.faq-section h2{margin:0 0 20px;color:#333;font-size:32px}.faq-section dl{margin:0;display:grid;gap:14px}.faq-section dl>div{padding:18px 22px;background:#f4f9fc;border-left:3px solid #1296e1}.faq-section dt{font-weight:700;color:#263349}.faq-section dd{margin:10px 0 0;color:#526579;line-height:1.7}@media(max-width:1120px){.faq-section{width:min(860px,calc(100vw - 40px))}}@media(max-width:720px){.faq-section{width:calc(100vw - 30px)}.faq-section h2{font-size:30px}}
</style>

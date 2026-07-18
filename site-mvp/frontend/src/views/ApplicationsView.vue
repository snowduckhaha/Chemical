<template>
  <div class="applications-page">
    <section class="banner-strip" aria-hidden="true"><img src="/products/banner-target.png" alt="" /></section>
    <section class="breadcrumb-strip" aria-label="Breadcrumb"><div class="breadcrumb-inner"><span aria-hidden="true">⌂</span><router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link><span>/</span><span class="is-current">{{ lang === "en" ? "Applications" : "应用领域" }}</span></div></section>
    <section class="applications-heading"><h1>{{ lang === "en" ? "Industries & Applications" : "行业及应用" }}</h1></section>
    <section v-if="loading" class="loading-panel">{{ lang === "en" ? "Loading..." : "正在加载应用领域…" }}</section>
    <section v-else-if="applications.length" class="carousel-section" aria-roledescription="carousel" :aria-label="lang === 'en' ? 'Application domains' : '应用领域'">
      <button class="carousel-arrow previous" type="button" :aria-label="lang === 'en' ? 'Previous application' : '上一个应用领域'" @click="goTo(activeIndex - 1)">←</button>
      <div ref="viewport" class="carousel-viewport" @scroll.passive="syncIndex">
        <router-link v-for="item in applications" :key="item.slug" class="application-card" :to="`/${lang}/applications/${item.slug}`" @click="trackCard(item.slug)">
          <img :src="item.image" :alt="item.name" /><span class="card-shade" aria-hidden="true"></span>
          <span class="card-content"><strong>{{ item.name }}</strong><small>{{ item.overview }}</small></span>
        </router-link>
      </div>
      <button class="carousel-arrow next" type="button" :aria-label="lang === 'en' ? 'Next application' : '下一个应用领域'" @click="goTo(activeIndex + 1)">→</button>
      <p class="carousel-count" aria-live="polite">{{ String(activeIndex + 1).padStart(2, "0") }} / {{ String(applications.length).padStart(2, "0") }}</p>
    </section>
    <section v-else class="loading-panel">{{ lang === "en" ? "No application domains are published." : "暂无已发布的应用领域。" }}</section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getApplications } from "../api/site";
import { trackEvent } from "../analytics";
import type { Application } from "../types/site";
const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const applications = ref<Application[]>([]); const loading = ref(true); const viewport = ref<HTMLElement>(); const activeIndex = ref(0); let timer: number | undefined;
const cardStep = () => { const card = viewport.value?.querySelector<HTMLElement>(".application-card"); const gap = viewport.value ? Number.parseFloat(getComputedStyle(viewport.value).columnGap) || 0 : 0; return card ? card.offsetWidth + gap : 0; };
const goTo = (index: number) => { if (!viewport.value || !applications.value.length) return; const next = (index + applications.value.length) % applications.value.length; viewport.value.scrollTo({ left: next * cardStep(), behavior: "smooth" }); activeIndex.value = next; };
const syncIndex = () => { if (timer) clearTimeout(timer); timer = window.setTimeout(() => { const step = cardStep(); if (step) activeIndex.value = Math.max(0, Math.min(applications.value.length - 1, Math.round((viewport.value?.scrollLeft || 0) / step))); }, 80); };
const trackCard = (slug: string) => trackEvent("application_card_click", "applications", { payload: { application_slug: slug, position: activeIndex.value + 1 } });
const load = async () => { loading.value = true; try { applications.value = await getApplications(lang.value); activeIndex.value = 0; await nextTick(); viewport.value?.scrollTo({ left: 0 }); } finally { loading.value = false; } };
onMounted(load); watch(lang, load); onUnmounted(() => { if (timer) clearTimeout(timer); });
</script>

<style scoped>
.applications-page{display:grid;background:#fff}.banner-strip,.breadcrumb-strip{width:100vw;margin-left:calc(50% - 50vw)}.banner-strip img{width:100%;height:clamp(230px,20vw,330px);display:block;object-fit:cover}.breadcrumb-strip{background:#1296e1}.breadcrumb-inner,.applications-heading{width:min(1440px,calc(100vw - 160px));margin:0 auto}.breadcrumb-inner{min-height:42px;display:flex;align-items:center;gap:10px;color:#fff;font-size:16px;letter-spacing:.05em}.breadcrumb-inner a{text-decoration:none}.breadcrumb-inner a:hover,.breadcrumb-inner .is-current{text-decoration:underline}.applications-heading{padding:92px 0 52px}.applications-heading p{margin:0 0 18px;color:#1296e1;font-size:18px;font-weight:500;letter-spacing:.05em}.applications-heading h1{margin:0;color:#333;font-size:48px;font-weight:500;line-height:1.2}.carousel-section{position:relative;padding:0 0 112px;overflow:hidden}.carousel-viewport{display:flex;gap:28px;overflow-x:auto;padding:0 calc((100vw - min(1440px,calc(100vw - 160px)))/2 + 96px);scroll-snap-type:x mandatory;scrollbar-width:none}.carousel-viewport::-webkit-scrollbar{display:none}.application-card{position:relative;flex:0 0 min(62vw,880px);aspect-ratio:1.72/1;overflow:hidden;scroll-snap-align:center;text-decoration:none;background:#dfe8ee}.application-card img{width:100%;height:100%;object-fit:cover;transition:transform .5s ease}.card-shade{position:absolute;inset:0;background:linear-gradient(0deg,rgba(11,28,45,.83),rgba(11,28,45,.08) 66%)}.card-content{position:absolute;left:48px;right:48px;bottom:38px;display:grid;gap:14px;color:#fff}.card-content strong{font-size:36px;line-height:1.25;font-weight:500}.card-content small{max-width:760px;font-size:16px;line-height:1.7;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:3;overflow:hidden}.application-card:hover img{transform:scale(1.045)}.carousel-arrow{position:absolute;z-index:2;top:calc(50% - 48px);width:52px;height:52px;border:0;background:transparent;color:#263349;font-size:38px;font-weight:300;cursor:pointer}.carousel-arrow:hover{color:#1296e1}.previous{left:max(18px,calc((100vw - min(1440px,calc(100vw - 160px)))/2))}.next{right:max(18px,calc((100vw - min(1440px,calc(100vw - 160px)))/2))}.carousel-count{position:absolute;right:max(30px,calc((100vw - min(1440px,calc(100vw - 160px)))/2 + 8px));bottom:72px;margin:0;color:#687586;font-size:13px;letter-spacing:.14em}.loading-panel{width:min(1440px,calc(100vw - 160px));min-height:280px;margin:0 auto 100px;display:grid;place-items:center;color:#687586;border-top:1px solid #dcdcdc}@media(max-width:1120px){.breadcrumb-inner,.applications-heading,.loading-panel{width:min(860px,calc(100vw - 40px))}.carousel-viewport{padding-left:9vw;padding-right:9vw}.applications-heading{padding-top:64px}.application-card{flex-basis:78vw}.carousel-arrow{display:none}}@media(max-width:767px){.breadcrumb-inner,.applications-heading,.loading-panel{width:calc(100vw - 30px)}.breadcrumb-inner{font-size:14px}.applications-heading{padding:52px 0 30px}.applications-heading p{font-size:15px;margin-bottom:12px}.applications-heading h1{font-size:34px}.carousel-section{padding-bottom:70px}.carousel-viewport{gap:16px;padding:0 5vw}.application-card{flex-basis:90vw;aspect-ratio:1.38/1}.card-content{left:24px;right:24px;bottom:24px;gap:9px}.card-content strong{font-size:28px}.card-content small{font-size:14px;line-height:1.55}.carousel-count{bottom:37px;right:5vw}.loading-panel{min-height:180px}}
.carousel-section{--carousel-card-width:min(62vw,880px);--carousel-gap:96px;--carousel-side-padding:calc((100vw - var(--carousel-card-width))/2)}
.carousel-viewport{gap:var(--carousel-gap);padding:0 var(--carousel-side-padding)}
.application-card{flex-basis:var(--carousel-card-width)}
.previous{left:calc(50% - var(--carousel-card-width)/2 - var(--carousel-gap)/2 - 26px)}
.next{left:calc(50% + var(--carousel-card-width)/2 + var(--carousel-gap)/2 - 26px);right:auto}
</style>

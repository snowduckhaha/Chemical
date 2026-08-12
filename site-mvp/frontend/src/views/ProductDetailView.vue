<template>
  <div class="detail-page page-shell" v-if="detail">
    <section class="banner-strip" aria-hidden="true">
      <img class="banner-card" src="/products/banner-target.png" alt="" />
    </section>

    <section class="breadcrumb-strip" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <span class="breadcrumb-home" aria-hidden="true">⌂</span>
        <router-link :to="`/${lang}`">{{ lang === "en" ? "Home" : "首页" }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <router-link :to="`/${lang}/products`">{{ lang === "en" ? "Products" : "产品中心" }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <router-link :to="`/${lang}/products/${detail.categorySlug}`">{{ categoryName }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <router-link :to="`/${lang}/products/${detail.categorySlug}/${detail.seriesSlug}`">{{ seriesName }}</router-link>
        <span class="breadcrumb-separator">/</span>
        <span class="is-current">{{ detail.name }}</span>
      </div>
    </section>

    <section class="product-overview">
      <div v-if="detail.image" class="media-block">
        <img :src="detail.image" :alt="detail.name" class="detail-image" />
      </div>
      <div class="product-copy">
        <h1 class="page-title">{{ detail.name }}</h1>
        <p class="hero-summary">{{ detail.summary }}</p>
        <router-link
          class="inquiry-link"
          :to="`/${lang}/contact?product=${encodeURIComponent(detail.slug)}&source=product-detail`"
          @click="trackInquiry"
        >
          <span aria-hidden="true">◌</span>
          {{ lang === "en" ? "Inquire Now" : "立即咨询" }}
        </router-link>
        <div class="share-row" :aria-label="lang === 'en' ? 'Share this product' : '分享产品'">
          <span class="share-label">{{ lang === "en" ? "Share To:" : "分享到：" }}</span>
          <a class="share-icon share-facebook" href="https://www.facebook.com/sharer/sharer.php" target="_blank" rel="noreferrer" aria-label="Facebook">f</a>
          <a class="share-icon share-x" href="https://x.com/intent/post" target="_blank" rel="noreferrer" aria-label="X">𝕏</a>
          <a class="share-icon share-linkedin" href="https://www.linkedin.com/sharing/share-offsite/" target="_blank" rel="noreferrer" aria-label="LinkedIn">in</a>
          <a class="share-icon share-whatsapp" href="https://api.whatsapp.com" target="_blank" rel="noreferrer" aria-label="WhatsApp">◔</a>
        </div>
      </div>
    </section>

    <section class="detail-tabs">
      <div class="tab-content-wrapper">
      <div class="tab-list" role="tablist" :aria-label="lang === 'en' ? 'Product information' : '产品信息'">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-button"
          :class="{ 'is-active': activeTab === tab.key }"
          type="button"
          role="tab"
          :aria-selected="activeTab === tab.key"
          @click="activeTab = tab.key"
        >{{ tab.label }}</button>
      </div>

      <div v-if="activeTab === 'applications'" class="tab-panel" role="tabpanel">
        <ul class="application-list">
          <li v-for="item in detail.applications" :key="item"><span class="app-dot" aria-hidden="true"></span><span>{{ item }}</span></li>
        </ul>
      </div>
      <div v-else-if="activeTab === 'parameters'" class="tab-panel parameter-table" role="tabpanel">
        <div class="parameter-row parameter-head-row"><span>{{ lang === "en" ? "Parameter" : "参数项" }}</span><span>{{ lang === "en" ? "Value" : "参数值" }}</span><span>{{ lang === "en" ? "Unit" : "单位" }}</span></div>
        <div v-for="item in detail.parameters" :key="item.label" class="parameter-row"><span>{{ item.label }}</span><strong>{{ item.value }}</strong><span class="param-unit">{{ item.unit || '-' }}</span></div>
      </div>
      <div v-else class="tab-panel statement-panel" role="tabpanel">
        <p>{{ lang === "en" ? "The information provided here is believed to be accurate and reliable, but is not intended to meet any specific specification, nor does it constitute any warranty or guarantee. All data listed here are reference values and subject to production tolerances. These values are for product description purposes only and do not constitute any warranty regarding their performance. Users are responsible for testing the product themselves to determine its suitability for their application." : "此处提供的信息据信准确可靠，但并非旨在满足任何特定规格，也不构成任何保证或担保。此处列出的所有数据均为参考值，并受生产公差的影响。这些数值仅用于产品描述，不对其性能做出任何保证。用户有责任自行测试产品是否适用于其应用。" }}</p>
      </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getCategories, getProductDetail, getSeries } from "../api/site";
import type { Product, Series } from "../types/site";
import { trackEvent } from "../analytics";
import { useJsonLd } from "../composables/useJsonLd";
import { breadcrumbSchema, productSchema, siteOrigin } from "../lib/jsonLd";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
const categorySlug = computed(() => String(route.params.categorySlug || ""));
const seriesSlug = computed(() => String(route.params.seriesSlug || ""));
const productSlug = computed(() => String(route.params.productSlug || ""));
const detail = ref<Product>();
const categoryName = ref("");
const seriesName = ref("");
const activeTab = ref<"applications" | "parameters" | "statement">("applications");
const tabs = computed(() => [
  { key: "applications" as const, label: lang.value === "en" ? "Product Applications" : "产品应用" },
  { key: "parameters" as const, label: lang.value === "en" ? "Product Indicators" : "产品指标" },
  { key: "statement" as const, label: lang.value === "en" ? "Disclaimer" : "免责声明" }
]);

const pageUrl = computed(() =>
  `${siteOrigin}/${lang.value}/products/${categorySlug.value}/${seriesSlug.value}/${productSlug.value}`
);
useJsonLd(computed(() => {
  if (!detail.value) return [];
  return [
    productSchema(detail.value, pageUrl.value),
    breadcrumbSchema(pageUrl.value, [
      { name: lang.value === "en" ? "Home" : "首页", url: `${siteOrigin}/${lang.value}` },
      { name: lang.value === "en" ? "Products" : "产品中心", url: `${siteOrigin}/${lang.value}/products` },
      { name: categoryName.value, url: `${siteOrigin}/${lang.value}/products/${categorySlug.value}` },
      { name: seriesName.value, url: `${siteOrigin}/${lang.value}/products/${categorySlug.value}/${seriesSlug.value}` },
      { name: detail.value.name }
    ])
  ];
}));

const trackInquiry = () => {
  trackEvent("inquiry_cta_click", "product.detail", {
    payload: {
      cta_key: "product_inquiry",
      cta_position: "product_detail_hero",
      product_slug: productSlug.value
    }
  });
};

const loadPage = async () => {
  activeTab.value = "applications";
  try {
    const [detailData, categories, series] = await Promise.all([
      getProductDetail(lang.value, categorySlug.value, seriesSlug.value, productSlug.value),
      getCategories(lang.value),
      getSeries(lang.value, categorySlug.value)
    ]);
    detail.value = detailData;
    trackEvent("product_view", `products.detail.${categorySlug.value}.${seriesSlug.value}.${productSlug.value}`, {
      payload: { category_slug: categorySlug.value, series_slug: seriesSlug.value, product_slug: productSlug.value }
    });
    categoryName.value =
      categories.find((item) => item.slug === categorySlug.value)?.name ||
      categorySlug.value;
    seriesName.value = (series as Series[]).find((item) => item.slug === seriesSlug.value)?.name || seriesSlug.value;
  } catch (error) {
    if (import.meta.env.SSR) throw error;
    detail.value = undefined;
    categoryName.value = categorySlug.value;
    seriesName.value = seriesSlug.value;
  }
};

onMounted(loadPage);
onServerPrefetch(loadPage);
watch([lang, categorySlug, seriesSlug, productSlug], loadPage);
</script>

<style scoped>
.page-shell {
  display: grid;
  gap: 0;
  max-width: none;
  margin: 0 auto;
  background: #fff;
}

.banner-strip {
  width: 100%;
}

.banner-card {
  display: block;
  width: 100%;
  height: clamp(230px, 20vw, 330px);
  object-fit: cover;
}

.breadcrumb-strip {
  width: 100%;
}

.breadcrumb-strip {
  background: #1296e1;
}

.breadcrumb-inner {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
  min-height: 44px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 15px;
  line-height: 20px;
  letter-spacing: 0.02em;
  white-space: nowrap;
  overflow: hidden;
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

.product-overview,
.detail-tabs {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
}

.product-overview {
  display: grid;
  grid-template-columns: minmax(380px, 476px) minmax(0, 1fr);
  align-items: start;
  gap: clamp(44px, 5.5vw, 78px);
  padding: 64px 0 62px;
}

.media-block { width: 100%; }

.detail-image {
  display: block;
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 0;
  background: #f6f9fc;
}

.product-copy {
  padding-top: 96px;
  max-width: 630px;
  display: grid;
  justify-items: start;
  gap: 22px;
}

.page-title {
  margin: 0;
  color: #000;
  font-size: 29px;
  line-height: 1.2;
  font-weight: 600;
  letter-spacing: 0;
}

.hero-summary,
.section-copy {
  margin: 0;
  color: #656565;
  font-size: 18px;
  line-height: 1.72;
  letter-spacing: 0;
  max-width: 100%;
}

.application-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.application-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #444;
  font-size: 16px;
  line-height: 1.55;
  font-weight: 400;
}

.app-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 8px;
  margin-top: 7px;
  background: #1296e1;
}

.inquiry-link {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 24px;
  border-radius: 7px;
  background: #1296e1;
  color: #fff;
  gap: 9px;
  font-size: 15px;
  font-weight: 500;
  line-height: 1;
  text-decoration: none;
  transition: background-color 0.2s ease;
}

.inquiry-link:hover { background: #087fc2; }

.share-row {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 38px;
}

.share-label { margin-right: 10px; color: #333; font-size: 14px; }
.share-icon { width: 30px; height: 30px; display: inline-flex; align-items: center; justify-content: center; border-radius: 4px; color: #fff; font-size: 19px; font-weight: 700; line-height: 1; text-decoration: none; }
.share-facebook { background: #1877f2; }
.share-x { background: #202124; font-size: 16px; }
.share-linkedin { background: #0a66c2; font-size: 16px; }
.share-whatsapp { background: #20b735; }

.detail-tabs { padding-bottom: 70px; }
.tab-list { display: grid; grid-template-columns: repeat(3, 1fr); border-bottom: 1px solid #e9e9e9; }
.tab-button { position: relative; min-height: 58px; border: 0; background: transparent; color: #777; font: inherit; font-size: 16px; letter-spacing: .04em; cursor: pointer; }
.tab-button::after { content: ""; position: absolute; right: 0; bottom: -1px; left: 0; height: 2px; background: transparent; }
.tab-button.is-active { color: #1296e1; }
.tab-button.is-active::after { background: #1296e1; }
.tab-panel { min-height: 190px; padding: 34px 0 10px; }

.tab-content-wrapper {
  max-width: 860px;
  margin: 0 auto;
}

.parameter-table {
  display: grid;
  border-top: 1px solid #dcdcdc;
  border-radius: 6px;
  overflow: hidden;
}

.parameter-row {
  display: grid;
  grid-template-columns: minmax(220px, 0.7fr) minmax(0, 0.8fr) minmax(0, 0.5fr);
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  color: #333;
  font-size: 15px;
  line-height: 1.5;
  transition: background-color 0.15s ease;
}

.parameter-row:last-child {
  border-bottom: none;
}

.parameter-row:not(.parameter-head-row):hover {
  background-color: #f7f9fb;
}

.parameter-head-row {
  padding-top: 0;
  color: #888;
  font-weight: 600;
  font-size: 14px;
  background-color: #fafbfc;
  border-bottom: 2px solid #e0e0e0;
  letter-spacing: 0.02em;
}

.parameter-row strong {
  font-weight: 600;
  color: #1a1a1a;
}

.param-unit {
  color: #999;
  font-size: 14px;
}

.statement-panel p { max-width: 100%; margin: 0; color: #666; font-size: 16px; line-height: 1.8; }

@media (max-width: 1120px) {
  .product-overview,
  .detail-tabs,
  .breadcrumb-inner {
    width: min(860px, calc(100vw - 40px));
  }

  .product-overview {
    grid-template-columns: minmax(330px, 420px) minmax(0, 1fr);
    gap: 42px;
    padding-top: 52px;
  }

  .page-title {
    font-size: 28px;
  }

  .product-copy {
    padding-top: 50px;
  }
}

@media (max-width: 720px) {
  .breadcrumb-inner,
  .product-overview,
  .detail-tabs {
    width: calc(100vw - 30px);
  }

  .breadcrumb-inner {
    min-height: 40px;
    gap: 8px;
    font-size: 14px;
  }

  .page-title {
    font-size: 26px;
    line-height: 1.25;
  }

  .hero-summary,
  .section-copy {
    font-size: 16px;
    line-height: 1.65;
  }

  .parameter-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .product-overview {
    grid-template-columns: 1fr;
    gap: 30px;
    padding-top: 34px;
    padding-bottom: 38px;
  }

  .media-block { max-width: 480px; }

  .product-copy {
    padding-top: 0;
    gap: 18px;
  }

  .share-row { margin-top: 22px; }

  .tab-button { min-height: 52px; font-size: 14px; letter-spacing: 0; }

  .tab-panel { padding-top: 26px; }

  .detail-image {
    width: 100%;
  }
}
</style>

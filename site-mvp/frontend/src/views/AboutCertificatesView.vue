<template>
  <div class="about-page">
    <section class="about-hero" aria-label="Certificates banner"></section>

    <nav class="breadcrumb-bar" aria-label="Breadcrumb">
      <div class="breadcrumb-inner">
        <router-link :to="`/${lang}`" class="home-icon" aria-label="Home">⌂</router-link>
        <router-link :to="`/${lang}`">{{ text.breadcrumbHome }}</router-link>
        <span>/</span>
        <span>{{ text.breadcrumbCurrent }}</span>
      </div>
    </nav>

    <section class="certificate-section section-shell" aria-labelledby="certificate-title">
      <div class="section-head certificate-head">
        <div>
          <p class="section-kicker">CERTIFICATES</p>
          <h1 id="certificate-title">{{ text.certificateTitle }}</h1>
        </div>
      </div>

      <div v-if="publishedCertificates.length > 0" class="certificate-marquee" aria-label="Certificate carousel">
        <div class="certificate-track">
          <button
            v-for="(certificate, index) in loopCertificates"
            :key="`${certificate.certificateNo}-${index}`"
            class="certificate-card"
            type="button"
            @pointerdown="openPreview(certificate)"
            @click="openPreview(certificate)"
          >
            <figure>
              <img :src="certificate.imageUrl" :alt="certificate.altText" />
            </figure>
            <span class="certificate-no">{{ certificate.certificateNo }}</span>
            <strong>{{ certificate.name }}</strong>
          </button>
        </div>
      </div>

      <p v-else class="certificate-empty">{{ text.certificateEmpty }}</p>
    </section>

    <Teleport to="body">
      <div v-if="activeCertificate" class="certificate-lightbox" role="dialog" aria-modal="true" @click.self="closePreview">
        <button class="lightbox-close" type="button" :aria-label="text.closePreview" @click="closePreview">×</button>
        <figure class="lightbox-panel">
          <img :src="activeCertificate.imageUrl" :alt="activeCertificate.altText" />
          <figcaption>
            <strong>{{ activeCertificate.name }}</strong>
            <span>{{ activeCertificate.certificateNo }}</span>
          </figcaption>
        </figure>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getCertificates } from "../api/site";

type Certificate = {
  certificateNo: string;
  name: string;
  imageUrl: string;
  altText: string;
  sortOrder: number;
};

const route = useRoute();
const lang = computed(() => (String(route.params.lang || "zh") === "en" ? "en" : "zh"));
const activeCertificate = ref<Certificate | null>(null);
const publishedCertificates = ref<Certificate[]>([]);
const loadCertificates = async () => {
  try {
    const rows = await getCertificates(lang.value);
    publishedCertificates.value = rows.map(item => ({ certificateNo: item.certificate_no, name: item.name, imageUrl: item.image_url, altText: item.alt_text || item.name, sortOrder: item.sort_order }));
  } catch {
    publishedCertificates.value = [];
  }
};

const loopCertificates = computed(() => {
  return Array.from({ length: 6 }, () => publishedCertificates.value).flat();
});

const text = computed(() => {
  if (lang.value === "en") {
    return {
      breadcrumbHome: "Home",
      breadcrumbCurrent: "Certificates",
      certificateTitle: "Certificates",
      certificateEmpty: "Certificates will be updated soon.",
      closePreview: "Close certificate preview"
    };
  }

  return {
    breadcrumbHome: "首页",
    breadcrumbCurrent: "荣誉证书",
    certificateTitle: "荣誉证书",
    certificateEmpty: "荣誉证书将陆续更新。",
    closePreview: "关闭证书预览"
  };
});

const openPreview = (certificate: Certificate) => {
  activeCertificate.value = certificate;
};

const closePreview = () => {
  activeCertificate.value = null;
};

onMounted(loadCertificates);
watch(lang, loadCertificates);
</script>

<style scoped>
:global(html),
:global(body) {
  overflow-x: hidden;
}

.about-page {
  width: 100%;
  color: var(--pf-ink);
}

.about-hero {
  width: 100vw;
  min-height: clamp(220px, 31vw, 430px);
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0)),
    url("/about/about-banner.png") center/cover no-repeat;
}

.breadcrumb-bar {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  background: #169bd7;
  color: #ffffff;
}

.breadcrumb-inner {
  max-width: var(--site-width);
  min-height: 64px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
  font-weight: 600;
}

.breadcrumb-inner a {
  color: #ffffff;
  text-decoration: none;
}

.home-icon {
  width: 28px;
  height: 28px;
  border: 1px solid rgba(255, 255, 255, 0.52);
  display: inline-grid;
  place-items: center;
  line-height: 1;
}

.section-shell {
  width: min(100%, var(--site-width));
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.section-kicker {
  margin: 0 0 9px;
  color: #169bd7;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.certificate-section {
  padding-top: 70px;
  padding-bottom: 86px;
}

.certificate-head {
  display: grid;
  grid-template-columns: minmax(260px, 0.7fr) minmax(0, 1fr);
  gap: 28px;
  align-items: end;
  margin-bottom: 34px;
}

.certificate-head h1 {
  margin: 0;
  color: #18314f;
  font-size: clamp(1.65rem, 2.3vw, 2.25rem);
  line-height: 1.22;
  letter-spacing: 0;
}

.certificate-head p {
  margin: 0;
  color: var(--pf-muted);
  line-height: 1.78;
  font-size: 1rem;
}

.certificate-marquee {
  position: relative;
  width: 100%;
  overflow: hidden;
  padding: 8px 0 18px;
}

.certificate-marquee::before,
.certificate-marquee::after {
  content: "";
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  width: 84px;
  pointer-events: none;
}

.certificate-marquee::before {
  left: 0;
  background: linear-gradient(90deg, var(--pf-bg), rgba(255, 255, 255, 0));
}

.certificate-marquee::after {
  right: 0;
  background: linear-gradient(270deg, var(--pf-bg), rgba(255, 255, 255, 0));
}

.certificate-track {
  width: max-content;
  display: flex;
  gap: 22px;
  animation: certificate-scroll 32s linear infinite;
}

.certificate-marquee:hover .certificate-track {
  animation-play-state: paused;
}

.certificate-card {
  width: clamp(240px, 20vw, 310px);
  flex: 0 0 auto;
  display: grid;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #dfe9f4;
  border-radius: 6px;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, transform 0.18s ease;
}

.certificate-card:hover {
  border-color: #169bd7;
  transform: translateY(-2px);
}

.certificate-card figure {
  margin: 0;
  background: #f5f8fb;
  border: 1px solid #edf2f7;
  overflow: hidden;
}

.certificate-card img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: contain;
  display: block;
}

.certificate-no {
  color: #66819f;
  font-size: 0.78rem;
  font-weight: 700;
}

.certificate-card strong {
  color: #18314f;
  font-size: 1.08rem;
  line-height: 1.35;
}

.certificate-empty {
  padding: 22px;
  background: #ffffff;
  border: 1px solid #dfe9f4;
  color: var(--pf-muted);
}

.certificate-lightbox {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: grid;
  place-items: center;
  padding: 32px;
  background: rgba(5, 24, 42, 0.78);
}

.lightbox-close {
  position: fixed;
  top: 22px;
  right: 28px;
  z-index: 201;
  width: 42px;
  height: 42px;
  border: 1px solid rgba(255, 255, 255, 0.58);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.lightbox-panel {
  width: min(92vw, 880px);
  max-height: 88vh;
  margin: 0;
  display: grid;
  gap: 14px;
}

.lightbox-panel img {
  max-width: 100%;
  max-height: 76vh;
  object-fit: contain;
  display: block;
  margin: 0 auto;
  background: #ffffff;
}

.lightbox-panel figcaption {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px 18px;
  color: #ffffff;
}

.lightbox-panel span {
  color: rgba(255, 255, 255, 0.72);
}

@keyframes certificate-scroll {
  from {
    transform: translateX(0);
  }

  to {
    transform: translateX(calc(-50% - 11px));
  }
}

@media (max-width: 980px) {
  .certificate-head {
    grid-template-columns: 1fr;
    align-items: start;
  }
}

@media (max-width: 680px) {
  .about-hero {
    min-height: 230px;
  }

  .breadcrumb-inner {
    min-height: 48px;
    padding: 0 18px;
    font-size: 0.92rem;
  }

  .section-shell {
    padding-left: 18px;
    padding-right: 18px;
  }

  .certificate-section {
    padding-top: 48px;
    padding-bottom: 56px;
  }

  .certificate-card {
    width: 230px;
  }

  .certificate-marquee::before,
  .certificate-marquee::after {
    width: 34px;
  }
}
</style>

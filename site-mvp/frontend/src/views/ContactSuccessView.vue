<template>
  <section class="success-page">
    <h1>{{ lang === "en" ? "Inquiry Submitted" : "询盘提交成功" }}</h1>
    <p>{{ lang === "en" ? "Thanks. Our team will contact you shortly." : "感谢提交，我们会尽快与你联系。" }}</p>
    <router-link :to="`/${lang}/products`">{{ lang === "en" ? "Back to Products" : "返回产品中心" }}</router-link>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const lang = computed(() => String(route.params.lang || "zh"));
</script>

<style scoped>
.success-page {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  min-height: 412px;
  margin: 0 auto;
  padding: 72px 0 88px;
  display: grid;
  align-content: start;
  gap: 18px;
}

.success-page h1,
.success-page p {
  margin: 0;
}

.success-page h1 {
  color: var(--pf-ink);
  font-size: clamp(2rem, 3vw, 3rem);
}

.success-page p {
  color: var(--pf-muted);
  font-size: 1.125rem;
}

.success-page a {
  width: fit-content;
  margin-top: 10px;
  color: var(--pf-blue);
  font-weight: 700;
}

@media (max-width: 720px) {
  .success-page {
    min-height: 340px;
    padding: 52px 0 64px;
  }
}
</style>

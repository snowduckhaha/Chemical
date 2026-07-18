<template>
  <section class="page">
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
.page {
  display: grid;
  gap: 12px;
}
</style>

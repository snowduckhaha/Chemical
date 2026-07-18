<template><p class="state">{{ lang === "en" ? "Redirecting…" : "正在跳转…" }}</p></template>
<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getLegacyNewsDetail } from "../api/site";
const route = useRoute(); const router = useRouter();
const lang = computed(() => String(route.params.lang || "zh"));
onMounted(async () => { try { const article = await getLegacyNewsDetail(lang.value, String(route.params.articleSlug || "")); await router.replace(`/${lang.value}/news/${article.categorySlug}/${article.slug}`); } catch { await router.replace(`/${lang.value}/news`); } });
</script>
<style scoped>.state{text-align:center;padding:100px 20px;color:#68778a}</style>
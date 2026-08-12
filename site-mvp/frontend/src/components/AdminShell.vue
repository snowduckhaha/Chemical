<template>
  <section class="admin-shell">
    <AdminFeedback />
    <aside class="admin-sidebar" :class="{ open: mobileMenuOpen }">
      <div class="admin-brand">
        <span class="brand-mark" aria-hidden="true">Q</span>
        <span><strong>起点化工</strong><small>QIDIAN CMS</small></span>
      </div>
      <nav aria-label="后台导航" @click="mobileMenuOpen = false">
        <p class="nav-group">产品管理</p>
        <RouterLink :to="`/${lang}/admin/products/categories`"><span aria-hidden="true">▦</span>产品分类</RouterLink>
        <RouterLink :to="`/${lang}/admin/products/series`"><span aria-hidden="true">◇</span>产品系列</RouterLink>
        <RouterLink :to="`/${lang}/admin/products/details`"><span aria-hidden="true">▤</span>产品详情</RouterLink>
        <p class="nav-group">内容管理</p>
        <RouterLink :to="`/${lang}/admin/applications`"><span aria-hidden="true">◌</span>应用领域与系列</RouterLink>
        <RouterLink :to="`/${lang}/admin/certificates`"><span aria-hidden="true">☆</span>荣誉证书</RouterLink>
        <RouterLink :to="`/${lang}/admin/news/categories`"><span aria-hidden="true">⌗</span>资讯分类</RouterLink>
        <RouterLink :to="`/${lang}/admin/news/articles`"><span aria-hidden="true">≡</span>资讯列表</RouterLink>
        <RouterLink :to="`/${lang}/admin/seo`"><span aria-hidden="true">◎</span>SEO 管理</RouterLink>
        <p class="nav-group">客户与数据</p>
        <RouterLink :to="`/${lang}/admin/inquiries`"><span aria-hidden="true">✉</span>询盘管理</RouterLink>
        <RouterLink v-if="auth.isAdmin" :to="`/${lang}/admin/analytics`"><span aria-hidden="true">⌁</span>数据概览</RouterLink>
        <RouterLink v-if="auth.session?.username === 'zelin'" :to="`/${lang}/admin/deployments`"><span aria-hidden="true">⇧</span>发布最新代码</RouterLink>
        <p class="nav-group">账户</p>
        <RouterLink :to="`/${lang}/admin/account/password`"><span aria-hidden="true">⚿</span>修改密码</RouterLink>
      </nav>
      <div class="sidebar-user">
        <span class="avatar">{{ userInitial }}</span>
        <span><strong>{{ auth.session?.username }}</strong><small>{{ auth.isAdmin ? "管理员" : "运营编辑" }}</small></span>
        <button type="button" title="退出登录" aria-label="退出登录" @click="logout">↪</button>
      </div>
    </aside>
    <button v-if="mobileMenuOpen" class="sidebar-scrim" aria-label="关闭菜单" @click="mobileMenuOpen = false" />
    <div class="admin-workspace">
      <header class="admin-topbar">
        <button class="menu-toggle" type="button" aria-label="打开后台菜单" @click="mobileMenuOpen = !mobileMenuOpen">☰</button>
        <div class="page-heading">
          <p class="breadcrumb">内容管理后台 <span>/</span> {{ title }}</p>
          <h1>{{ title }}</h1>
          <p>{{ subtitle }}</p>
        </div>
        <div class="topbar-actions"><span class="role-pill">{{ auth.isAdmin ? "ADMIN" : "OPERATOR" }}</span><RouterLink :to="`/${lang}/admin/account/password`">修改密码</RouterLink><RouterLink :to="`/${lang}`" target="_blank">查看网站 ↗</RouterLink></div>
      </header>
      <main class="admin-page"><slot /></main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { useAdminAuthStore } from "../stores/adminAuth";
import AdminFeedback from "./AdminFeedback.vue";

defineProps<{ title: string; subtitle: string }>();
const route = useRoute();
const router = useRouter();
const auth = useAdminAuthStore();
const lang = computed(() => String(route.params.lang || "zh"));
const mobileMenuOpen = ref(false);
const userInitial = computed(() => String(auth.session?.username || "A").slice(0, 1).toUpperCase());

const logout = async () => {
  await auth.logout();
  await router.replace({ name: "admin-login", params: { lang: lang.value } });
};
</script>

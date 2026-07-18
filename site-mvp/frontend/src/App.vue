<template>
  <div class="app-shell">
    <header v-if="!isAdminRoute" class="header">
      <div class="header-inner">
        <router-link class="brand" :to="`/${lang}`">
          <span class="brand-logo-wrap" aria-hidden="true">
            <img src="/products/logo-symbol.png" alt="" class="brand-logo" />
          </span>
          <span class="brand-meta">
            <span class="brand-title">{{ lang === "zh" ? "起点化工" : "START POINT CHEMICAL" }}</span>
          </span>
        </router-link>

        <nav v-if="navItems.length > 0" class="main-nav" :class="{ 'is-mobile-open': mobileNavOpen }">
          <div
            v-for="item in headerNavItems"
            :key="item.key"
            class="nav-item"
            :class="{ 'has-dropdown': isAboutNav(item) || isProductsNav(item) || isApplicationsNav(item) || isNewsNav(item), 'is-about': isAboutNav(item), 'is-products': isProductsNav(item), 'is-applications': isApplicationsNav(item), 'is-news': isNewsNav(item), 'is-open': openDropdownKey === item.key }"
            @mouseenter="openDropdown(item)"
            @mouseleave="scheduleCloseDropdown"
            @focusin="openDropdown(item)"
            @focusout="scheduleCloseDropdown"
          >
            <router-link class="nav-link" :to="item.path">{{ item.label }}</router-link>
            <NavMegaDropdown
              v-if="isAboutNav(item)"
              :items="aboutSubnav"
              :open="openDropdownKey === item.key"
              @mouseenter="keepDropdownOpen"
              @mouseleave="scheduleCloseDropdown"
              @select="handleDropdownSelect"
            />
            <NavMegaDropdown
              v-if="isProductsNav(item)"
              :items="productSubnav"
              :open="openDropdownKey === item.key"
              background-image="/home/hero-1.png"
              @mouseenter="keepDropdownOpen"
              @mouseleave="scheduleCloseDropdown"
              @select="handleDropdownSelect"
            />
            <NavMegaDropdown
              v-if="isApplicationsNav(item)"
              :items="applicationSubnav"
              :open="openDropdownKey === item.key"
              background-image="/products/banner-target.png"
              @mouseenter="keepDropdownOpen"
              @mouseleave="scheduleCloseDropdown"
              @select="handleDropdownSelect"
            />
            <NavMegaDropdown
              v-if="isNewsNav(item)"
              :items="newsSubnav"
              :open="openDropdownKey === item.key"
              background-image="/news/reference-news-banner.webp"
              @mouseenter="keepDropdownOpen"
              @mouseleave="scheduleCloseDropdown"
              @select="handleDropdownSelect"
            />
          </div>
        </nav>

        <div class="header-actions">
          <button class="lang-btn" @click="switchLang">
            <span class="lang-icon" aria-hidden="true">◎</span>
            <span>{{ lang === "zh" ? "EN" : "中文" }}</span>
            <span class="lang-caret" aria-hidden="true">▾</span>
          </button>
          <button class="search-btn" type="button" :aria-label="lang === 'zh' ? '搜索' : 'Search'">
            <span aria-hidden="true">⌕</span>
          </button>
          <router-link class="quote-btn" :to="`/${lang}/contact`">{{ lang === "zh" ? "索取报价" : "Get Quote" }}</router-link>
          <button class="mobile-menu-btn" type="button" :aria-expanded="mobileNavOpen" :aria-label="lang === 'zh' ? '切换导航菜单' : 'Toggle navigation'" @click="mobileNavOpen = !mobileNavOpen">
            <span></span><span></span><span></span>
          </button>
        </div>
      </div>
    </header>

    <main id="top" class="main-content" :class="{ 'admin-main-content': isAdminRoute, 'home-main-content': isHomeRoute }">
      <router-view />
    </main>

    <footer v-if="!isAdminRoute" class="footer">
      <div class="footer-inner">
        <section class="footer-brand">
          <div class="footer-logo-wrap" aria-hidden="true">
            <span class="footer-logo-symbol-wrap">
              <img src="/products/logo-symbol-footer.png" alt="" class="footer-logo-symbol" />
            </span>
            <span class="footer-logo-title">{{ lang === "zh" ? "起点化工" : "START POINT CHEMICAL" }}</span>
          </div>
          <p>
            {{
              lang === "zh"
                ? "专注先进无机材料，服务阻燃与功能化应用场景。"
                : "Focused on advanced inorganic materials for flame-retardant and functional applications."
            }}
          </p>
          <div class="socials">
            <a href="https://api.whatsapp.com" target="_blank" rel="noreferrer" aria-label="WhatsApp">◔</a>
            <a href="https://www.linkedin.com" target="_blank" rel="noreferrer" aria-label="LinkedIn">in</a>
            <a href="https://www.x.com" target="_blank" rel="noreferrer" aria-label="X">X</a>
            <a href="https://www.facebook.com" target="_blank" rel="noreferrer" aria-label="Facebook">f</a>
          </div>
        </section>

        <section>
          <h4>{{ lang === "zh" ? "联系我们" : "Contact" }}</h4>
          <a href="tel:+8619063971053">{{ lang === "zh" ? "电话: 19063971053" : "Tel: +86 19063971053" }}</a>
          <a href="tel:+865336535999">{{ lang === "zh" ? "电话: 533-6535999" : "Tel: +86 533-6535999" }}</a>
          <a href="mailto:info@qidian-chemical.com">{{ lang === "zh" ? "电子邮件: info@qidian-chemical.com" : "Email: info@qidian-chemical.com" }}</a>
          <p>
            {{
              lang === "zh"
                ? "地址: 山东省淄博经济开发区天浩路388号, 邮编: 255300"
                : "Address: Tianhao Road 388, Zibo Economic Development Zone, Shandong 255300"
            }}
          </p>
        </section>

        <section>
          <h4>{{ lang === "zh" ? "产品" : "Products" }}</h4>
          <router-link :to="`/${lang}/products/aluminum-hydroxide`">{{ lang === "zh" ? "氢氧化铝" : "Aluminum Hydroxide" }}</router-link>
          <router-link :to="`/${lang}/products/silica-powder-series`">{{ lang === "zh" ? "硅粉系列" : "Silica Powder Series" }}</router-link>
          <router-link :to="`/${lang}/products/alumina-powder`">{{ lang === "zh" ? "氧化铝粉末" : "Alumina Powder" }}</router-link>
          <router-link :to="`/${lang}/products/silane-coupling-agent`">{{ lang === "zh" ? "硅烷偶联剂" : "Silane Coupling Agent" }}</router-link>
        </section>

        <section>
          <h4>{{ lang === "zh" ? "快速入口" : "Quick Links" }}</h4>
          <router-link :to="`/${lang}`">{{ lang === "zh" ? "首页" : "Home" }}</router-link>
          <router-link :to="`/${lang}/news`">{{ lang === "zh" ? "资讯中心" : "News" }}</router-link>
          <router-link :to="`/${lang}/contact`">{{ lang === "zh" ? "联系我们" : "Contact" }}</router-link>
        </section>
      </div>

      <div class="footer-meta">
        <span>{{ lang === "zh" ? "©2025 淄博博丰新材料科技有限公司版权所有。鲁ICP备14031418号" : "©2025 Zibo Bofeng New Material Technology Co., Ltd. All rights reserved." }}</span>
        <span>{{ lang === "zh" ? "隐私协议" : "Privacy Agreement" }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useSiteStore } from "./stores/site";
import { getApplications, getNav, getNewsCategories } from "./api/site";
import type { Application, NavItem, NewsCategory } from "./types/site";
import type { NavMegaDropdownItem } from "./plugins/navMegaDropdown";

const route = useRoute();
const router = useRouter();
const siteStore = useSiteStore();
const navItems = ref<NavItem[]>([]);
const applications = ref<Application[]>([]);
const newsCategories = ref<NewsCategory[]>([]);
const openDropdownKey = ref<string | null>(null);
const mobileNavOpen = ref(false);
let dropdownCloseTimer: number | undefined;

const lang = computed(() => {
  const current = String(route.params.lang || "zh");
  return current === "en" ? "en" : "zh";
});

const isAdminRoute = computed(() => route.path.includes("/admin/"));
const isHomeRoute = computed(() => /^\/(zh|en)\/?$/.test(route.path));

const headerNavItems = computed(() => {
  return navItems.value.filter((item) => {
    const key = String(item.key || "").toLowerCase();
    const path = String(item.path || "").toLowerCase();
    const label = String(item.label || "");
    if (key.includes("contact") || path.endsWith("/contact") || label === "联系我们" || label === "Contact") {
      return false;
    }
    return true;
  });
});

const aboutSubnav = computed<NavMegaDropdownItem[]>(() => {
  if (lang.value === "en") {
    return [
      { label: "Company Profile", path: "/en/about" },
      { label: "Company Culture", path: "/en/about/culture" },
      { label: "Certificates", path: "/en/about/certificates" }
    ];
  }

  return [
    { label: "公司简介", path: "/zh/about" },
    { label: "企业文化", path: "/zh/about/culture" },
    { label: "荣誉证书", path: "/zh/about/certificates" }
  ];
});

const productSubnav = computed<NavMegaDropdownItem[]>(() => {
  if (lang.value === "en") {
    return [
      {
        label: "Aluminum Hydroxide",
        path: "/en/products/aluminum-hydroxide"
      },
      {
        label: "Silica Powder Series",
        path: "/en/products/silica-powder-series"
      },
      {
        label: "Alumina Powder",
        path: "/en/products/alumina-powder"
      },
      {
        label: "Silane Coupling Agent",
        path: "/en/products/silane-coupling-agent"
      }
    ];
  }

  return [
    {
      label: "氢氧化铝",
      path: "/zh/products/aluminum-hydroxide"
    },
    {
      label: "硅粉系列",
      path: "/zh/products/silica-powder-series"
    },
    {
      label: "氧化铝粉末",
      path: "/zh/products/alumina-powder"
    },
    {
      label: "硅烷偶联剂",
      path: "/zh/products/silane-coupling-agent"
    }
  ];
});

const applicationSubnav = computed<NavMegaDropdownItem[]>(() =>
  applications.value.map((item) => ({
    label: item.name,
    path: `/${lang.value}/applications/${item.slug}`
  }))
);

const newsSubnav = computed<NavMegaDropdownItem[]>(() =>
  newsCategories.value.map((item) => ({
    label: item.name,
    path: `/${lang.value}/news/${item.slug}`
  }))
);

const isAboutNav = (item: NavItem) => {
  const key = String(item.key || "").toLowerCase();
  const path = String(item.path || "").toLowerCase();
  const label = String(item.label || "").toLowerCase();
  return key.includes("about") || path.includes("/about") || label.includes("关于") || label.includes("about");
};

const isProductsNav = (item: NavItem) => {
  const key = String(item.key || "").toLowerCase();
  const path = String(item.path || "").toLowerCase();
  const label = String(item.label || "").toLowerCase();
  return key.includes("products") || path.includes("/products") || label.includes("产品") || label.includes("products");
};

const isApplicationsNav = (item: NavItem) => {
  const key = String(item.key || "").toLowerCase();
  const path = String(item.path || "").toLowerCase();
  const label = String(item.label || "").toLowerCase();
  return key.includes("application") || path.includes("/applications") || label.includes("应用") || label.includes("application");
};

const isNewsNav = (item: NavItem) => {
  const key = String(item.key || "").toLowerCase();
  const path = String(item.path || "").toLowerCase();
  const label = String(item.label || "").toLowerCase();
  return key.includes("news") || path.includes("/news") || label.includes("资讯") || label.includes("news");
};

const openDropdown = (item: NavItem) => {
  keepDropdownOpen();
  openDropdownKey.value = isAboutNav(item) || isProductsNav(item) || isApplicationsNav(item) || isNewsNav(item) ? item.key : null;
};

const closeDropdown = () => {
  keepDropdownOpen();
  openDropdownKey.value = null;
};

const keepDropdownOpen = () => {
  if (dropdownCloseTimer) {
    window.clearTimeout(dropdownCloseTimer);
    dropdownCloseTimer = undefined;
  }
};

const scheduleCloseDropdown = () => {
  keepDropdownOpen();
  dropdownCloseTimer = window.setTimeout(() => {
    closeDropdown();
    dropdownCloseTimer = undefined;
  }, 360);
};

const handleDropdownSelect = () => {
  mobileNavOpen.value = false;
  keepDropdownOpen();
  dropdownCloseTimer = window.setTimeout(() => {
    closeDropdown();
    dropdownCloseTimer = undefined;
  }, 120);
};

const fallbackNav = (currentLang: "zh" | "en"): NavItem[] => {
  if (currentLang === "en") {
    return [
      { key: "home", label: "Home", path: "/en" },
      { key: "about", label: "About Us", path: "/en/about" },
      { key: "products", label: "Products", path: "/en/products" },
      { key: "applications", label: "Applications", path: "/en/applications" },
      { key: "news", label: "News", path: "/en/news" },
      { key: "contact", label: "Contact", path: "/en/contact" }
    ];
  }

  return [
    { key: "home", label: "首页", path: "/zh" },
    { key: "about", label: "关于我们", path: "/zh/about" },
    { key: "products", label: "产品中心", path: "/zh/products" },
    { key: "applications", label: "应用领域", path: "/zh/applications" },
    { key: "news", label: "资讯中心", path: "/zh/news" },
    { key: "contact", label: "联系我们", path: "/zh/contact" }
  ];
};

const syncNav = async () => {
  mobileNavOpen.value = false;
  siteStore.setLang(lang.value);
  try {
    const [nav, applicationList, categoryList] = await Promise.all([getNav(lang.value), getApplications(lang.value), getNewsCategories(lang.value)]);
    navItems.value = nav;
    applications.value = applicationList;
    newsCategories.value = categoryList;
  } catch {
    navItems.value = fallbackNav(lang.value);
    applications.value = [];
    newsCategories.value = [];
  }
};

const switchLang = async () => {
  const nextLang = lang.value === "zh" ? "en" : "zh";
  const currentPath = route.fullPath;
  const nextPath = currentPath.replace(/^\/(zh|en)/, `/${nextLang}`);
  await router.push(nextPath);
};

onMounted(syncNav);
watch(() => route.fullPath, syncNav);
watch(
  () => route.hash,
  async (hash) => {
    if (!hash) {
      return;
    }

    await nextTick();
    const target = document.querySelector(hash);
    if (target) {
      const headerOffset = 96;
      const targetTop = target.getBoundingClientRect().top + window.scrollY - headerOffset;
      window.scrollTo({ top: targetTop, behavior: "auto" });
    }
  },
  { flush: "post" }
);
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr auto;
  background: var(--pf-bg);
  color: var(--pf-ink);
  overflow-x: hidden;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 10000;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.header-inner {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  min-height: 100px;
  padding: 0;
  column-gap: 18px;
  font-family: "Inter Tight", Arial, "Noto Sans SC", "PingFang SC", sans-serif;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: var(--pf-blue);
  text-decoration: none;
  min-width: 0;
}

.brand-logo-wrap {
  width: 68px;
  height: 68px;
  overflow: hidden;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
}

.brand-logo-wrap::after {
  content: "";
  display: none;
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 18px;
  background: #fff;
  pointer-events: none;
}

.brand-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center center;
}

.brand-meta {
  display: grid;
  gap: 0;
  min-width: 0;
}

.brand-title {
  color: #1296e1;
  font-size: 16px;
  line-height: 19.5556px;
  font-weight: 700;
  letter-spacing: 0.8px;
  white-space: nowrap;
}

.main-nav {
  display: flex;
  gap: 0;
  align-items: center;
  justify-content: center;
  padding-left: 8px;
  min-width: 0;
  overflow: visible;
  scrollbar-width: none;
}

.main-nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 100%;
  margin-left: 16px;
}

.nav-link {
  color: #1296e1;
  font-family: inherit;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  text-decoration: none;
  padding: 10px 20px;
  border-bottom: 0;
  line-height: 27px;
  border-radius: 0;
  border: 1px solid transparent;
  white-space: nowrap;
  transition: color 0.18s ease, border-color 0.18s ease, background-color 0.18s ease, box-shadow 0.18s ease;
}

.nav-link:hover {
  color: #0f78bf;
}

.nav-link.router-link-active {
  color: #1296e1;
  font-weight: 700;
}

.nav-item.is-products .nav-link.router-link-active,
.nav-item.is-products:hover .nav-link {
  color: #1296e1;
}

.nav-item.is-products .nav-link.router-link-active {
  border-color: transparent;
  background: transparent;
  box-shadow: none;
}

.nav-item.is-applications .nav-link.router-link-active,
.nav-item.is-applications:hover .nav-link {
  color: #1296e1;
}

.nav-item.is-applications .nav-link.router-link-active {
  border-color: transparent;
  background: transparent;
  box-shadow: none;
}

.nav-item.is-about .nav-link.router-link-active,
.nav-item.is-about:hover .nav-link {
  color: #1296e1;
}

.nav-item.is-about .nav-link.router-link-active {
  border-color: transparent;
  background: transparent;
}

.lang-btn {
  border: 0;
  background: transparent;
  color: #1296e1;
  border-radius: 0;
  padding: 12px 10px;
  font-size: 14px;
  font-weight: 400;
  line-height: 1;
  letter-spacing: 0.8px;
  cursor: pointer;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.lang-icon {
  font-size: 0.78rem;
  color: #1296e1;
}

.lang-caret {
  font-size: 0.74rem;
  color: #7d93ad;
}

.search-btn {
  width: 44px;
  height: 44px;
  border: 0;
  border-radius: 8px;
  background: linear-gradient(251deg, #1080c0 12.92%, #1296e1 87.08%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(19, 135, 220, 0.18);
}

.search-btn span {
  font-size: 1rem;
  line-height: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quote-btn {
  text-decoration: none;
  border: 0;
  background: linear-gradient(250.78deg, #1080c0 12.92%, #1296e1 87.08%);
  color: #ffffff;
  border-radius: 0;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: 400;
  line-height: 19.5556px;
  letter-spacing: 0.8px;
  white-space: nowrap;
  box-shadow: 0 10px 20px rgba(18, 150, 225, 0.16);
}

/* The reference site increases its header scale on wide desktop layouts. */
@media (min-width: 1600px) {
  .header-inner {
    min-height: 130px;
    column-gap: 26px;
  }

  .brand-logo-wrap {
    width: 100px;
    height: 100px;
  }

  .brand-title {
    font-size: 28px;
    line-height: 34px;
    letter-spacing: 1.2px;
  }

  .nav-item { margin-left: 16px; }

  .nav-link {
    padding: 10px 20px;
    font-size: 22px;
    line-height: 27px;
    letter-spacing: 1px;
  }

  .quote-btn {
    padding: 10px 24px;
    font-size: 18px;
    line-height: 22px;
    letter-spacing: 0.9px;
  }

  .main-content { padding-top: 130px; }
  .main-content.home-main-content { padding-top: 0; }
}

.mobile-menu-btn {
  display: none;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.mobile-menu-btn span {
  display: block;
  width: 25px;
  height: 2px;
  margin: 5px auto;
  background: #243442;
}

.main-content {
  max-width: none;
  width: 100%;
  margin: 0 auto;
  padding: 100px 0 56px;
}

/* The target site's homepage slider begins at the top of the viewport and
   sits behind its fixed header. Other pages retain header clearance. */
.main-content.home-main-content {
  padding-top: 0;
}

/* Public-page content uses the same target-derived desktop container as the shell. */
@media (min-width: 1121px) {
  :global(.page-shell .breadcrumb-inner),
  :global(.page-shell .solution-intro),
  :global(.page-shell .feature-list),
  :global(.page-shell .intro-block),
  :global(.page-shell .series-list),
  :global(.page-shell .product-list),
  :global(.page-shell .product-overview),
  :global(.page-shell .detail-tabs),
  :global(.page-shell .loading-panel),
  :global(.applications-page .breadcrumb-inner),
  :global(.applications-page .applications-heading),
  :global(.applications-page .loading-panel),
  :global(.detail-page .breadcrumb-inner),
  :global(.detail-page .intro),
  :global(.detail-page .series-section),
  :global(.detail-page .inquiry-cta),
  :global(.detail-page .loading-panel) {
    width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2))) !important;
  }

  :global(.about-page .breadcrumb-inner),
  :global(.about-page .section-shell) {
    width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2))) !important;
    max-width: none !important;
    padding-left: 0 !important;
    padding-right: 0 !important;
  }

  :global(.news-page .breadcrumb) {
    padding-right: max(var(--site-gutter), calc((100% - var(--site-width)) / 2)) !important;
    padding-left: max(var(--site-gutter), calc((100% - var(--site-width)) / 2)) !important;
  }

  :global(.news-page .latest),
  :global(.news-page .news-grid) {
    width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2))) !important;
    max-width: none !important;
  }
}

.footer {
  background: #e8eef3;
  color: #1296e1;
  padding: 56px 24px 16px;
}

.footer-inner {
  max-width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2.15fr 1.1fr 1fr 1fr;
  gap: 36px;
}

.footer section {
  display: grid;
  align-content: start;
  gap: 14px;
}

.footer h4 {
  margin: 0;
  color: #1296e1;
  font-size: 18px;
  font-weight: 700;
}

.footer a {
  color: #1296e1;
  text-decoration: none;
  line-height: 1.8;
}

.footer-brand p {
  margin: 0;
  max-width: 420px;
  line-height: 1.8;
  color: #5f7ea2;
}

.footer-logo-wrap {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.footer-logo-symbol-wrap {
  width: 68px;
  height: 68px;
  overflow: hidden;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.footer-logo-symbol-wrap::after {
  content: "";
  display: none;
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 18px;
  background: #e8eef3;
  pointer-events: none;
}

.footer-logo-symbol {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center center;
}

.footer-logo-title {
  color: #1296e1;
  font-size: 20px;
  line-height: 1;
  font-weight: 700;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.socials {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.socials a {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.32);
  color: #6d7483;
  font-size: 18px;
  line-height: 1;
}

.footer-meta {
  max-width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 20px auto 0;
  border-top: 1px solid rgba(18, 150, 225, 0.18);
  padding-top: 18px;
  display: flex;
  justify-content: space-between;
  color: #1296e1;
  font-size: 16px;
}

@media (max-width: 1366px) {
  .footer-inner {
    grid-template-columns: 2fr 1fr 1fr 1fr;
  }
}

@media (max-width: 1200px) {
  .header-inner { column-gap: 12px; }

  .nav-link {
    font-size: 14px;
    padding: 10px 12px;
  }

  .nav-item {
    margin-left: 10px;
  }

  .brand-logo-wrap {
    width: 58px;
    height: 58px;
  }

  .brand-logo-wrap::after {
    height: 16px;
  }

  .brand-title {
    font-size: 17px;
  }

  .header-actions {
    gap: 8px;
  }

  .quote-btn {
    padding: 12px 16px;
    font-size: 16px;
  }

  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .footer-logo-symbol-wrap {
    width: 58px;
    height: 58px;
  }

  .footer-logo-symbol-wrap::after {
    height: 16px;
  }

  .footer-logo-title {
    font-size: 17px;
  }
}

@media (max-width: 992px) {
  .header-inner {
    grid-template-columns: 1fr auto;
    position: relative;
    display: grid;
    width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
    column-gap: 10px;
    min-height: 78px;
  }

  .main-content {
    padding-top: 78px;
  }

  .main-nav {
    position: absolute;
    top: calc(100% + 1px);
    right: -12px;
    left: -12px;
    display: none;
    flex-direction: column;
    align-items: stretch;
    padding: 10px 18px 16px;
    background: #fff;
    border-top: 1px solid #edf1f4;
    border-bottom: 1px solid #edf1f4;
    box-shadow: 0 12px 22px rgba(21, 42, 61, .08);
  }

  .main-nav.is-mobile-open { display: flex; }

  .nav-item { width: 100%; min-height: auto; margin: 0; }

  .nav-link,
  .nav-item.is-products .nav-link.router-link-active,
  .nav-item.is-about .nav-link.router-link-active,
  .nav-item.is-applications .nav-link.router-link-active {
    width: 100%;
    padding: 12px 4px;
    border: 0;
    border-radius: 0;
    box-shadow: none;
    font-size: 16px;
  }

  .nav-item.is-about .nav-link.router-link-active::before { display: none; }
  .quote-btn { display: none; }
  .mobile-menu-btn { display: block; }

  .header-actions {
    justify-self: end;
  }

}

@media (max-width: 768px) {
  .header-inner {
    min-height: 78px;
  }

  .main-content {
    padding-top: 78px;
  }

  .brand-logo-wrap {
    width: 46px;
    height: 46px;
  }

  .brand-logo-wrap::after {
    height: 12px;
  }

  .brand {
    gap: 8px;
  }

  .brand-title {
    font-size: 16px;
  }

  .footer-logo-symbol-wrap {
    width: 46px;
    height: 46px;
  }

  .footer-logo-symbol-wrap::after {
    height: 12px;
  }

  .footer-logo-title {
    font-size: 14px;
  }

  .footer-logo-image-wrap {
    width: 210px;
    height: 106px;
  }

  .header-inner {
    width: calc(100vw - 30px);
    padding-right: 0;
  }

  .lang-btn { min-height: 42px; padding: 0 12px; }
  .search-btn { width: 42px; height: 42px; }

  .footer-inner {
    grid-template-columns: 1fr;
  }

  .footer-meta {
    flex-direction: column;
    gap: 6px;
  }
}
</style>

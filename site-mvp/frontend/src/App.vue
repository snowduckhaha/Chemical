<template>
  <div class="app-shell">
    <header v-if="!isAdminRoute" class="header">
      <div class="header-inner">
        <router-link class="brand" :to="`/${lang}`">
          <span class="brand-logo-wrap" aria-hidden="true">
            <img src="/products/logo-symbol.png" alt="" class="brand-logo" />
          </span>
          <span class="brand-meta">
            <span class="brand-title">{{ lang === "zh" ? "起点化工" : "ORIGIN CHEMICAL" }}</span>
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
          <button class="search-btn" type="button" :aria-label="lang === 'zh' ? '搜索' : 'Search'" @click="openSearch">
            <span aria-hidden="true">⌕</span>
          </button>
          <div v-if="searchOpen" class="search-overlay" @click.self="closeSearch">
            <div class="search-panel">
              <form class="search-form" @submit.prevent="submitSearch">
                <input
                  ref="searchInputRef"
                  v-model="searchKeyword"
                  type="text"
                  :placeholder="lang === 'zh' ? '搜索产品...' : 'Search products...'"
                  class="search-input"
                />
                <button type="submit" class="search-submit">{{ lang === 'zh' ? '搜索' : 'Search' }}</button>
              </form>
              <button class="search-close" type="button" @click="closeSearch" :aria-label="lang === 'zh' ? '关闭搜索' : 'Close search'">✕</button>
            </div>
          </div>
          <router-link class="quote-btn" :to="`/${lang}/contact?source=header-quote`" @click="trackHeaderInquiry">{{ lang === "zh" ? "索取报价" : "Get Quote" }}</router-link>
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
            <span class="footer-logo-title">{{ lang === "zh" ? "起点化工" : "ORIGIN CHEMICAL" }}</span>
          </div>
          <p>
            {{
              lang === "zh"
                ? "专注先进无机材料，服务阻燃与功能化应用场景。"
                : "Focused on advanced inorganic materials for flame-retardant and functional applications."
            }}
          </p>
          <div class="socials">
            <a href="https://api.whatsapp.com/send?phone=8613728111347" target="_blank" rel="noreferrer" aria-label="WhatsApp">
              <svg viewBox="0 0 1024 1024" width="20" height="20" fill="currentColor"><path d="M254-59L0-128L69 126Q35 185 17.5 250.5Q0 316 0 384Q0 488 40 583Q79 675 150 746Q221 817 313 856Q408 896 512 896Q616 896 711 856Q803 817 874 746Q945 675 984 583Q1024 488 1024 384Q1024 280 984 185Q945 93 874 22Q803-49 711-88Q616-128 512-128Q444-128 378.5-110.5Q313-93 254-59Z M327 624Q317 624 308.5 619Q300 614 293 607Q288 603 282 594L280 592Q251 554 251 506Q251 471 268 433Q301 361 369 293L374 288Q392 270 402 261Q488 185 598 156L628 152Q636 151 652 152L656 153Q679 154 699 164Q709 170 718 176L725 180Q734 187 741 194.5Q748 202 752 210Q759 224 762 248Q763 258 763 266L763 267Q763 271 760 275Q757 279 753 281L651 326Q647 328 642 328Q631 329 623 321Q622 321 582 274Q579 269 574 267.5Q569 266 563.5 267Q558 268 554 270L541 276Q496 295 460 327L442 345Q411 374 389 410L386 415Q383 420 381.5 425Q380 430 382 435L384 439L402 460Q410 469 416 479Q426 495 421 506Q399 559 376 611Q374 616 368.5 619.5Q363 623 356 624L348 624Q338 625 327 624Z"/></svg>
            </a>
            <a href="https://www.linkedin.com" target="_blank" rel="noreferrer" aria-label="LinkedIn">
              <svg viewBox="0 0 1024 1024" width="20" height="20" fill="currentColor"><path d="M366-91L366 567L568 567L568 463L571 463Q595 506 643 534Q699 567 771 567Q873 567 931 526Q984 489 1006 417Q1024 357 1024 261L1024-91L813-91L813 221Q813 272 809 298Q801 341 780 364Q753 391 703 391Q628 391 599 340Q577 302 577 226L577-91L366-91Z M0-91L0 567L219 567L219-91L0-91Z M219 750Q219 720 204.5 694.5Q190 669 164.5 654.5Q139 640 109.5 640Q80 640 54.5 654.5Q29 669 14.5 694.5Q0 720 0 749.5Q0 779 15 804.5Q30 830 55 844.5Q80 859 110 859Q140 859 165 844.5Q190 830 204.5 804.5Q219 779 219 750Z"/></svg>
            </a>
            <a href="https://www.x.com" target="_blank" rel="noreferrer" aria-label="X">
              <svg viewBox="0 0 1024 1024" width="20" height="20" fill="currentColor"><path d="M548 551L806 847L963 847L620 455L1024-79L708-79L461 245L177-79L20-79L387 341L0 847L324 847L548 551Z M183 758L751 15L838 15L277 758L183 758Z"/></svg>
            </a>
            <a href="https://www.facebook.com/share/1EFonQQsgZ/?mibextid=wwXIfr" target="_blank" rel="noreferrer" aria-label="Facebook">
              <svg viewBox="0 0 1024 1024" width="20" height="20" fill="currentColor"><path d="M593 384L593-128L401-128L401 384L273 384L273 560L401 560L400 664Q400 742 417 788Q436 842 480 868Q527 896 609 896L750 896L750 720L662 720Q630 720 615.5 712Q601 704 596 688Q593 676 593 649L593 561L751 561L733 384L593 384Z"/></svg>
            </a>
          </div>
        </section>

        <section>
          <h4>{{ lang === "zh" ? "联系我们" : "Contact" }}</h4>
          <a href="tel:+8613580598793">{{ lang === "zh" ? "电话: +86-13580598793" : "Tel: +86-13580598793" }}</a>
          <a href="tel:+886076985166074">{{ lang === "zh" ? "电话/传真: 00886-0769-85166074" : "Tel&Fax: 00886-0769-85166074" }}</a>
          <a href="mailto:admin@origin-chemical.com">{{ lang === "zh" ? "电子邮件: admin@origin-chemical.com" : "Email: admin@origin-chemical.com" }}</a>
          <p>
            {{
              lang === "zh"
                ? "工厂地址: 江西省宜春市袁州区工业大道67号"
                : "Factory Address: No.67, Gongye Avenue, Yuanzhou District, Yichun City, Jiangxi Province, China"
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
        <span>{{ lang === "zh" ? "©2026 深圳市起点化工有限公司版权所有。" : "©2026 Shenzhen Origin Chemical Industry Co., Ltd. All rights reserved." }}</span>
        <span>{{ lang === "zh" ? "隐私协议" : "Privacy Agreement" }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useSeoDocument } from "./composables/useSeoDocument";

useSeoDocument();
import { computed, nextTick, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useSiteStore } from "./stores/site";
import { getApplications, getNav, getNewsCategories } from "./api/site";
import type { Application, NavItem, NewsCategory } from "./types/site";
import type { NavMegaDropdownItem } from "./plugins/navMegaDropdown";
import { trackInquiryCta } from "./analytics";

const route = useRoute();
const router = useRouter();
const siteStore = useSiteStore();
const navItems = ref<NavItem[]>([]);
const applications = ref<Application[]>([]);
const newsCategories = ref<NewsCategory[]>([]);
const openDropdownKey = ref<string | null>(null);
const mobileNavOpen = ref(false);
const searchOpen = ref(false);
const searchKeyword = ref("");
const searchInputRef = ref<HTMLInputElement | null>(null);
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
  const nextPath = currentPath.replace(/^(\/zh|\/en)/, `/${nextLang}`);
  await router.push(nextPath);
};

const openSearch = () => {
  searchOpen.value = true;
  nextTick(() => {
    searchInputRef.value?.focus();
  });
};

const closeSearch = () => {
  searchOpen.value = false;
  searchKeyword.value = "";
};

const trackHeaderInquiry = () => trackInquiryCta(String(route.name || "header"), {
  cta_key: "header_quote",
  cta_position: "header"
});

const submitSearch = () => {
  const keyword = searchKeyword.value.trim();
  if (!keyword) return;
  closeSearch();
  router.push(`/${lang.value}/search?q=${encodeURIComponent(keyword)}`);
};

onMounted(syncNav);
onServerPrefetch(syncNav);
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
  font-size: 1.35rem;
  line-height: 1;
}

.search-overlay {
  position: fixed;
  inset: 0;
  background: rgba(10, 30, 50, 0.55);
  z-index: 20000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 140px;
}

.search-panel {
  width: min(640px, calc(100% - 40px));
  background: #fff;
  border-radius: 12px;
  padding: 20px 20px 18px;
  box-shadow: 0 20px 50px rgba(10, 30, 50, 0.25);
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-form {
  flex: 1;
  display: flex;
  gap: 10px;
  min-width: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 46px;
  padding: 0 16px;
  border: 1px solid #dce6f2;
  border-radius: 6px;
  font-size: 15px;
  color: #18314f;
  background: #fff;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.search-input:focus {
  border-color: #1296e1;
  box-shadow: 0 0 0 3px rgba(18, 150, 225, 0.15);
}

.search-input::placeholder {
  color: #8fa3bd;
}

.search-submit {
  height: 46px;
  padding: 0 22px;
  border: 0;
  border-radius: 6px;
  background: linear-gradient(251deg, #1080c0 12.92%, #1296e1 87.08%);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.8px;
  cursor: pointer;
  white-space: nowrap;
}

.search-submit:hover {
  box-shadow: 0 6px 14px rgba(18, 150, 225, 0.25);
}

.search-close {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 50%;
  background: #f4f8fc;
  color: #60758f;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.search-close:hover {
  background: #e6eef8;
  color: #18314f;
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
  border-radius: 6px;
}

.socials svg {
  transform: scaleY(-1);
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

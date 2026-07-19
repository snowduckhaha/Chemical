import { createRouter, createWebHistory } from "vue-router";
import { useAdminAuthStore } from "../stores/adminAuth";
import { pageKeyFor, trackPageView } from "../analytics";
import { getSeo } from "../api/site";

const routes = [
  { path: "/", redirect: "/zh" },
  {
    path: "/:lang(zh|en)",
    name: "home",
    component: () => import("../views/HomeView.vue")
  },
  {
    path: "/:lang(zh|en)/about",
    name: "about",
    component: () => import("../views/AboutView.vue")
  },
  {
    path: "/:lang(zh|en)/about/culture",
    name: "about-culture",
    component: () => import("../views/AboutCultureView.vue")
  },
  {
    path: "/:lang(zh|en)/about/certificates",
    name: "about-certificates",
    component: () => import("../views/AboutCertificatesView.vue")
  },
  {
    path: "/:lang(zh|en)/products",
    name: "products-home",
    component: () => import("../views/ProductsHomeView.vue")
  },
  {
    path: "/:lang(zh|en)/products/:categorySlug",
    name: "product-category",
    component: () => import("../views/ProductCategoryView.vue")
  },
  {
    path: "/:lang(zh|en)/products/:categorySlug/:seriesSlug",
    name: "product-series",
    component: () => import("../views/ProductSeriesView.vue")
  },
  {
    path: "/:lang(zh|en)/products/:categorySlug/:seriesSlug/:productSlug",
    name: "product-detail",
    component: () => import("../views/ProductDetailView.vue")
  },
  {
    path: "/:lang(zh|en)/admin/login",
    name: "admin-login",
    component: () => import("../views/AdminLoginView.vue")
  },
  {
    path: "/:lang(zh|en)/admin/account/password",
    name: "admin-change-password",
    component: () => import("../views/AdminChangePasswordView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/products",
    redirect: (to) => `/${to.params.lang}/admin/products/categories`
  },
  {
    path: "/:lang(zh|en)/admin/products/categories",
    name: "admin-product-categories",
    component: () => import("../views/AdminCategoriesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/products/series",
    name: "admin-product-series",
    component: () => import("../views/AdminSeriesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/products/details",
    name: "admin-product-details",
    component: () => import("../views/AdminProductDetailsView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/analytics",
    name: "admin-analytics",
    component: () => import("../views/AdminAnalyticsView.vue"),
    meta: { requiresAdmin: true, requiresRole: "ADMIN" }
  },
  {
    path: "/:lang(zh|en)/admin/inquiries",
    name: "admin-inquiries",
    component: () => import("../views/AdminInquiriesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/certificates",
    name: "admin-certificates",
    component: () => import("../views/AdminCertificatesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/seo",
    name: "admin-seo",
    component: () => import("../views/AdminSeoView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/applications",
    name: "admin-application-series",
    component: () => import("../views/AdminApplicationSeriesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/news/categories",
    name: "admin-news-categories",
    component: () => import("../views/AdminNewsCategoriesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/admin/news/articles",
    name: "admin-news-articles",
    component: () => import("../views/AdminNewsArticlesView.vue"),
    meta: { requiresAdmin: true }
  },
  {
    path: "/:lang(zh|en)/applications",
    name: "applications",
    component: () => import("../views/ApplicationsView.vue")
  },
  {
    path: "/:lang(zh|en)/applications/:applicationSlug",
    name: "application-detail",
    component: () => import("../views/ApplicationDetailView.vue")
  },
  {
    path: "/:lang(zh|en)/news",
    name: "news-list",
    component: () => import("../views/NewsListView.vue")
  },
  {
    path: "/:lang(zh|en)/news/category/:categorySlug",
    redirect: (to) => `/${to.params.lang}/news/${to.params.categorySlug}`
  },
  {
    path: "/:lang(zh|en)/news/:categorySlug/:articleSlug",
    name: "news-detail",
    component: () => import("../views/NewsDetailView.vue")
  },
  {
    path: "/:lang(zh|en)/news/:categorySlug",
    name: "news-category",
    component: () => import("../views/NewsCategoryView.vue")
  },
  {
    path: "/:lang(zh|en)/contact",
    name: "contact",
    component: () => import("../views/ContactView.vue")
  },
  {
    path: "/:lang(zh|en)/contact/success",
    name: "contact-success",
    component: () => import("../views/ContactSuccessView.vue")
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    if (to.hash) {
      return {
        el: to.hash,
        top: 96,
        behavior: "auto"
      };
    }

    if (savedPosition) {
      return savedPosition;
    }

    return { top: 0 };
  }
});

router.beforeEach(async (to) => {
  if (!to.meta.requiresAdmin) return true;

  const auth = useAdminAuthStore();
  await auth.restore();
  if (!auth.isAuthenticated) {
    return { name: "admin-login", params: { lang: to.params.lang || "zh" }, query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresRole === "ADMIN" && !auth.isAdmin) {
    return { name: "admin-product-categories", params: { lang: to.params.lang || "zh" } };
  }
  return true;
});

router.afterEach((to) => {
  trackPageView(to);
  if (String(to.name || "").startsWith("admin")) return;
  void getSeo(String(to.params.lang || "zh"), pageKeyFor(to)).then((seo) => {
    document.title = seo.title;
    const setMeta = (selector: string, attribute: "name" | "property", key: string, content: string) => {
      let element = document.head.querySelector<HTMLMetaElement>(selector);
      if (!element) { element = document.createElement("meta"); element.setAttribute(attribute, key); document.head.appendChild(element); }
      element.content = content;
    };
    setMeta('meta[name="description"]', "name", "description", seo.description || "");
    setMeta('meta[property="og:title"]', "property", "og:title", seo.ogTitle || seo.title);
    setMeta('meta[property="og:description"]', "property", "og:description", seo.ogDescription || seo.description || "");
    if (seo.ogImage) setMeta('meta[property="og:image"]', "property", "og:image", new URL(seo.ogImage, location.origin).href);
    else document.head.querySelector('meta[property="og:image"]')?.remove();
    let canonical = document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!canonical) { canonical = document.createElement("link"); canonical.rel = "canonical"; document.head.appendChild(canonical); }
    canonical.href = new URL(seo.canonical || to.path, location.origin).href;
  }).catch(() => undefined);
});

export default router;

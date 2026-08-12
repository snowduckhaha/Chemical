import { computed, onMounted, onServerPrefetch, ref, watch } from "vue";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import { getSeo } from "../api/site";
import { pageKeyFor } from "../analytics";
import { siteOrigin as origin } from "../lib/jsonLd";

function normalizedPath(path: string) {
  return path.split("?")[0].split("#")[0] || "/";
}

/**
 * Resolves one canonical SEO document for both SSG and client navigation.
 * The server request intentionally throws: an indexable page must never be
 * emitted with a silent fallback when its public SEO contract is unavailable.
 */
export function useSeoDocument() {
  const route = useRoute();
  const title = ref("Origin Chemical");
  const description = ref("");
  const image = ref<string | null>(null);
  const canonical = computed(() => `${origin}${normalizedPath(route.path)}`);
  const indexable = computed(() => route.meta.indexable !== false);

  const load = async () => {
    if (!indexable.value) return;
    const seo = await getSeo(String(route.params.lang || "zh"), pageKeyFor(route));
    title.value = seo.title;
    description.value = seo.description || "";
    ogTitle.value = seo.ogTitle || seo.title;
    ogDescription.value = seo.ogDescription || seo.description || "";
    image.value = seo.ogImage || null;
  };

  const ogTitle = ref("Origin Chemical");
  const ogDescription = ref("");

  useHead(computed(() => ({
    title: title.value,
    htmlAttrs: { lang: String(route.params.lang || "zh") === "en" ? "en" : "zh-CN" },
    meta: [
      { name: "description", content: description.value },
      { name: "robots", content: indexable.value ? "index, follow" : "noindex, nofollow" },
      { property: "og:title", content: ogTitle.value },
      { property: "og:description", content: ogDescription.value },
      { property: "og:url", content: canonical.value },
      ...(image.value ? [{ property: "og:image", content: new URL(image.value, origin).href }] : []),
      { name: "twitter:card", content: image.value ? "summary_large_image" : "summary" }
    ],
    link: [
      { rel: "canonical", href: canonical.value },
      { rel: "alternate", hreflang: "zh-CN", href: `${origin}/zh${normalizedPath(route.path).replace(/^\/(zh|en)/, "")}` },
      { rel: "alternate", hreflang: "en", href: `${origin}/en${normalizedPath(route.path).replace(/^\/(zh|en)/, "")}` },
      { rel: "alternate", hreflang: "x-default", href: `${origin}/en${normalizedPath(route.path).replace(/^\/(zh|en)/, "")}` }
    ]
  })));

  onServerPrefetch(load);
  onMounted(() => { void load().catch(() => undefined); });
  watch(() => route.fullPath, () => { void load().catch(() => undefined); });
  return { load };
}

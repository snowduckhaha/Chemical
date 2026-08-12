import { mkdir, writeFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const output = resolve(root, "src/generated/ssg-routes.json");
let apiBase = (process.env.SSG_API_BASE || "").replace(/\/$/, "");

const fixedPaths = [
  "/about", "/about/culture", "/about/certificates", "/products", "/applications", "/news", "/contact"
];

function fail(message) {
  throw new Error(`SSG route collection failed: ${message}`);
}

function published(item) {
  const status = item.publishStatus ?? item.publish_status;
  return status === undefined || status === "PUBLISHED";
}

function slugOf(item, label) {
  const slug = item?.slug;
  if (typeof slug !== "string" || !slug.trim()) fail(`${label} contains an empty slug`);
  if (slug.includes("/") || /\s/.test(slug)) fail(`${label} has an invalid slug: ${slug}`);
  return slug;
}

async function api(path) {
  let response;
  try {
    response = await fetch(`${apiBase}${path}`, { headers: { accept: "application/json" } });
  } catch (error) {
    fail(`request ${path} could not be completed (${error instanceof Error ? error.message : String(error)})`);
  }
  if (!response.ok) fail(`request ${path} returned HTTP ${response.status}`);
  let payload;
  try {
    payload = await response.json();
  } catch {
    fail(`request ${path} did not return JSON`);
  }
  if (!payload || payload.code !== 0 || !("data" in payload)) fail(`request ${path} returned an invalid API envelope`);
  return payload.data;
}

function assertArray(value, label) {
  if (!Array.isArray(value)) fail(`${label} response data must be an array`);
  return value.filter(published);
}

export async function collectRoutes({ baseUrl = apiBase } = {}) {
  if (!baseUrl) fail("SSG_API_BASE is required and must point to the private public-API endpoint");
  if (!/^https?:\/\//.test(baseUrl)) fail("SSG_API_BASE must be an absolute http(s) URL");
  // api() deliberately reads this scoped value so tests can inject a disposable server.
  apiBase = baseUrl.replace(/\/$/, "");
  const paths = new Set();
  const counts = { fixed: 0, categories: 0, series: 0, products: 0, applications: 0, newsCategories: 0, news: 0 };

  for (const lang of ["zh", "en"]) {
    for (const path of fixedPaths) {
      paths.add(`/${lang}${path}`);
      counts.fixed++;
    }
    paths.add(`/${lang}`);
    counts.fixed++;

    const categories = assertArray(await api(`/products/categories?lang=${lang}`), `categories (${lang})`);
    for (const category of categories) {
      const categorySlug = slugOf(category, `category (${lang})`);
      paths.add(`/${lang}/products/${categorySlug}`);
      counts.categories++;
      const series = assertArray(await api(`/products/categories/${encodeURIComponent(categorySlug)}/series?lang=${lang}`), `series ${categorySlug} (${lang})`);
      for (const seriesItem of series) {
        const seriesSlug = slugOf(seriesItem, `series ${categorySlug} (${lang})`);
        paths.add(`/${lang}/products/${categorySlug}/${seriesSlug}`);
        counts.series++;
        const products = assertArray(await api(`/products/categories/${encodeURIComponent(categorySlug)}/series/${encodeURIComponent(seriesSlug)}/products?lang=${lang}`), `products ${categorySlug}/${seriesSlug} (${lang})`);
        for (const product of products) {
          const productSlug = slugOf(product, `product ${categorySlug}/${seriesSlug} (${lang})`);
          paths.add(`/${lang}/products/${categorySlug}/${seriesSlug}/${productSlug}`);
          counts.products++;
        }
      }
    }

    for (const application of assertArray(await api(`/applications?lang=${lang}`), `applications (${lang})`)) {
      paths.add(`/${lang}/applications/${slugOf(application, `application (${lang})`)}`);
      counts.applications++;
    }
    for (const category of assertArray(await api(`/news/categories?lang=${lang}`), `news categories (${lang})`)) {
      paths.add(`/${lang}/news/${slugOf(category, `news category (${lang})`)}`);
      counts.newsCategories++;
    }
    for (const article of assertArray(await api(`/news?lang=${lang}`), `news (${lang})`)) {
      const categorySlug = slugOf({ slug: article.categorySlug ?? article.category_slug }, `news category for ${article.slug ?? "unknown"} (${lang})`);
      paths.add(`/${lang}/news/${categorySlug}/${slugOf(article, `news article (${lang})`)}`);
      counts.news++;
    }
  }

  const routes = [...paths].sort();
  if (routes.some((path) => /\/(admin|search)(\/|$)|\/contact\/success$|:[A-Za-z]/.test(path))) fail("generated list contains a non-indexable or parameterized route");
  return { routes, counts };
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const { routes, counts } = await collectRoutes();
    await mkdir(dirname(output), { recursive: true });
    await writeFile(output, `${JSON.stringify(routes, null, 2)}\n`);
    const revision = process.env.CONTENT_REVISION || "unknown";
    console.log(`SSG routes: total=${routes.length} revision=${revision} ${Object.entries(counts).map(([key, value]) => `${key}=${value}`).join(" ")}`);
  } catch (error) {
    console.error(error instanceof Error ? error.message : error);
    process.exitCode = 1;
  }
}

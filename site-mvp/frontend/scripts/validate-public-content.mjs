import { chromium } from "playwright";

const baseUrl = (process.env.PUBLIC_BASE_URL || "http://localhost:5173").replace(/\/$/, "");
const lang = process.env.PUBLIC_LANG || "zh";
const failures = [];
const checkedAssets = new Set();
const routes = new Set([`/${lang}/products`, `/${lang}/news`, `/${lang}/about/certificates`]);

const api = async (path) => {
  const response = await fetch(`${baseUrl}/api/v1${path}`);
  if (!response.ok) throw new Error(`API ${path} returned ${response.status}`);
  const payload = await response.json();
  if (payload.code !== 0) throw new Error(`API ${path}: ${payload.message}`);
  return payload.data;
};

const checkAsset = async (url, source) => {
  if (!url || checkedAssets.has(url)) return;
  checkedAssets.add(url);
  try {
    const response = await fetch(new URL(url, baseUrl));
    const contentType = response.headers.get("content-type") || "";
    if (!response.ok || !contentType.startsWith("image/")) {
      failures.push(`${source}: ${url} returned ${response.status} (${contentType || "no content-type"})`);
    }
  } catch (error) {
    failures.push(`${source}: ${url} could not be loaded (${error.message})`);
  }
};

const categories = await api(`/products/categories?lang=${lang}`);
for (const category of categories) {
  await checkAsset(category.image, `category ${category.slug}`);
  routes.add(`/${lang}/products/${category.slug}`);
  const seriesList = await api(`/products/categories/${encodeURIComponent(category.slug)}/series?lang=${lang}`);
  for (const series of seriesList) {
    await checkAsset(series.image, `series ${series.slug}`);
    routes.add(`/${lang}/products/${category.slug}/${series.slug}`);
    const products = await api(`/products/categories/${encodeURIComponent(category.slug)}/series/${encodeURIComponent(series.slug)}/products?lang=${lang}`);
    for (const product of products) {
      await checkAsset(product.image, `product ${product.slug}`);
      routes.add(`/${lang}/products/${category.slug}/${series.slug}/${product.slug}`);
      if (!product.parameters?.length) failures.push(`product ${product.slug}: no visible parameters`);
    }
  }
}

const applications = await api(`/applications?lang=${lang}`);
for (const application of applications) {
  await checkAsset(application.image, `application ${application.slug}`);
  routes.add(`/${lang}/applications/${application.slug}`);
  if (!application.faqs?.length) failures.push(`application ${application.slug}: no visible FAQ entries`);
}

const news = await api(`/news?lang=${lang}`);
for (const article of news) {
  await checkAsset(article.coverImage, `news ${article.slug}`);
  if (!article.categorySlug) failures.push(`news ${article.slug}: missing category slug`);
  else routes.add(`/${lang}/news/${article.categorySlug}/${article.slug}`);
}

const certificates = await api(`/certificates?lang=${lang}`);
for (const certificate of certificates) {
  await checkAsset(certificate.image_url, `certificate ${certificate.certificate_no}`);
}

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
let currentRoute = "";
page.on("pageerror", (error) => failures.push(`${currentRoute}: page error: ${error.message}`));
page.on("response", (response) => {
  if (response.request().resourceType() === "image" && !response.ok() && response.status() !== 304) {
    failures.push(`${currentRoute}: image response ${response.status()} ${response.url()}`);
  }
});

for (const route of routes) {
  currentRoute = route;
  const response = await page.goto(`${baseUrl}${route}`, { waitUntil: "networkidle" });
  if (!response?.ok()) failures.push(`${route}: page returned ${response?.status() || "no response"}`);
  const brokenImages = await page.locator("img").evaluateAll((images) => images
    .filter((image) => !image.complete || image.naturalWidth === 0 || image.naturalHeight === 0)
    .map((image) => ({ src: image.currentSrc || image.src, alt: image.alt })));
  for (const image of brokenImages) failures.push(`${route}: broken DOM image ${image.src} (alt=${image.alt})`);
  const emptyAlt = await page.locator("img").evaluateAll((images) => images
    .filter((image) => image.getAttribute("aria-hidden") !== "true" && !image.closest('[aria-hidden="true"]') && image.alt.trim() === "")
    .map((image) => image.currentSrc || image.src));
  for (const image of emptyAlt) failures.push(`${route}: non-decorative image has empty alt (${image})`);
}

await browser.close();
console.log(`Validated ${categories.length} categories, ${applications.length} applications, ${news.length} news articles, ${certificates.length} certificates, ${checkedAssets.size} unique images and ${routes.size} public routes.`);
if (failures.length) {
  console.error(`Public content validation failed (${failures.length}):`);
  failures.forEach((failure) => console.error(`- ${failure}`));
  process.exit(1);
}
console.log("Public content validation passed: all API media and rendered images loaded successfully.");

import type { RouteLocationNormalizedLoaded } from "vue-router";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "/api/v1";
const SESSION_TTL = 30 * 60 * 1000;

type AnalyticsFields = Record<string, unknown>;

function randomId() {
  return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function visitorId() {
  let value = localStorage.getItem("qidian_visitor_id");
  if (!value) { value = randomId(); localStorage.setItem("qidian_visitor_id", value); }
  return value;
}

function sessionId() {
  const now = Date.now();
  const last = Number(sessionStorage.getItem("qidian_session_last") || 0);
  let value = sessionStorage.getItem("qidian_session_id");
  if (!value || now - last > SESSION_TTL) { value = randomId(); sessionStorage.setItem("qidian_session_id", value); }
  sessionStorage.setItem("qidian_session_last", String(now));
  return value;
}

export function pageKeyFor(route: RouteLocationNormalizedLoaded) {
  const p = route.params;
  if (route.name === "home") return "home";
  if (route.name === "product-category") return `products.category.${p.categorySlug}`;
  if (route.name === "product-series") return `products.series.${p.categorySlug}.${p.seriesSlug}`;
  if (route.name === "product-detail") return `products.detail.${p.categorySlug}.${p.seriesSlug}.${p.productSlug}`;
  if (route.name === "application-detail") return `applications.detail.${p.applicationSlug}`;
  if (route.name === "news-category") return `news.category.${p.categorySlug}`;
  if (route.name === "news-detail") return `news.detail.${p.categorySlug}.${p.articleSlug}`;
  return String(route.name || route.path).replaceAll("-", ".");
}

export function trackEvent(eventName: string, pageKey: string, fields: AnalyticsFields = {}) {
  if (typeof window === "undefined") return;
  if (localStorage.getItem("qidian_analytics_consent") === "denied" || pageKey.startsWith("admin")) return;
  const query = new URLSearchParams(location.search);
  const body = {
    event_id: randomId(), event_name: eventName, occurred_at: new Date().toISOString(),
    visitor_id: visitorId(), session_id: sessionId(), page_key: pageKey, page_path: location.pathname,
    page_type: fields.page_type || pageKey.split(".")[0], referrer: document.referrer || undefined,
    utm_source: query.get("utm_source") || undefined, utm_medium: query.get("utm_medium") || undefined,
    utm_campaign: query.get("utm_campaign") || undefined, lang: location.pathname.split("/")[1] || "zh",
    ...fields
  };
  const json = JSON.stringify(body);
  try {
    if (navigator.sendBeacon && navigator.sendBeacon(`${API_BASE}/analytics/events`, new Blob([json], { type: "application/json" }))) return;
    void fetch(`${API_BASE}/analytics/events`, { method: "POST", headers: { "Content-Type": "application/json" }, body: json, keepalive: true }).catch(() => undefined);
  } catch { /* Analytics must never block business actions. */ }
}

export function trackPageView(route: RouteLocationNormalizedLoaded) {
  const key = `${sessionId()}:${route.path}`;
  const storageKey = `qidian_page_view:${key}`;
  const last = Number(sessionStorage.getItem(storageKey) || 0);
  if (Date.now() - last < SESSION_TTL) return;
  sessionStorage.setItem(storageKey, String(Date.now()));
  trackEvent("page_view", pageKeyFor(route), { page_type: String(route.name || "page") });
}

import type {
  ApiResponse,
  Application,
  Category,
  HomeSection,
  InquiryPayload,
  InquiryResult,
  NavItem,
  News,
  NewsCategory,
  NewsDetail,
  NewsPage,
  Product,
  SeoMeta,
  Series
} from "../types/site";

declare const __SSG_API_BASE__: string;

// The private build endpoint exists only in the SSR bundle.  The client build
// is constant-folded to the relative public endpoint, so Docker service names
// and internal ports can never be exposed to visitors.
const API_BASE = import.meta.env.SSR
  ? (__SSG_API_BASE__ || "/api/v1")
  : (import.meta.env.VITE_API_BASE_URL || "/api/v1");
let csrfToken: string | null = null;
let csrfHeaderName = "X-XSRF-TOKEN";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const { headers: initialHeaders, ...requestInit } = init || {};
  const response = await fetch(`${API_BASE}${path}`, {
    ...requestInit,
    headers: {
      "Content-Type": "application/json",
      ...(csrfToken ? { [csrfHeaderName]: csrfToken } : {}),
      ...(initialHeaders || {})
    },
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error(`request failed: ${response.status}`);
  }

  const payload = (await response.json()) as ApiResponse<T>;
  if (payload.code !== 0) {
    throw new Error(payload.message || "api error");
  }
  return payload.data;
}

export type AdminSession = {
  username: string;
  role: "ADMIN" | "OPERATOR";
};

export const adminLogin = (username: string, password: string) =>
  request<AdminSession>("/admin/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });

export const adminLogout = () =>
  request<null>("/admin/auth/logout", { method: "POST" });

export const adminMe = () => request<AdminSession>("/admin/auth/me");

export const adminChangePassword = (currentPassword: string, newPassword: string) =>
  request<{ updated: boolean }>("/admin/auth/change-password", {
    method: "POST",
    body: JSON.stringify({ currentPassword, newPassword })
  });

export const adminCsrf = async () => {
  const token = await request<{ headerName: string; token: string }>("/admin/auth/csrf");
  csrfToken = token.token;
  csrfHeaderName = token.headerName || csrfHeaderName;
  return token;
};

export type UploadedImage = {
  url: string;
  sourceBytes: number;
  displayBytes: number;
  width: number;
  height: number;
  displayMode: "CONTAIN" | "ORIGINAL";
  canvasRatio: string;
  canvasBackground: string;
};

export async function adminUploadImage(file: File, scene = "PRODUCT"): Promise<UploadedImage> {
  if (!csrfToken) await adminCsrf();
  const body = new FormData();
  body.append("file", file);
  body.append("scene", scene);
  const response = await fetch(`${API_BASE}/admin/uploads/images`, {
    method: "POST",
    body,
    credentials: "include",
    headers: csrfToken ? { [csrfHeaderName]: csrfToken } : {}
  });
  const payload = (await response.json()) as ApiResponse<UploadedImage>;
  if (!response.ok || payload.code !== 0) throw new Error(payload.message || `upload failed: ${response.status}`);
  return payload.data;
}

export const getNav = (lang: string) => request<NavItem[]>(`/nav?lang=${lang}`);
export const getHome = (lang: string) => request<HomeSection>(`/home?lang=${lang}`);
export const getCategories = (lang: string) => request<Category[]>(`/products/categories?lang=${lang}`);
export const getSeries = (lang: string, categorySlug: string) =>
  request<Series[]>(`/products/categories/${categorySlug}/series?lang=${lang}`);
export const getProducts = (lang: string, categorySlug: string, seriesSlug: string) =>
  request<Product[]>(`/products/categories/${categorySlug}/series/${seriesSlug}/products?lang=${lang}`);
export const getProductDetail = (lang: string, categorySlug: string, seriesSlug: string, productSlug: string) =>
  request<Product>(
    `/products/categories/${categorySlug}/series/${seriesSlug}/products/${productSlug}?lang=${lang}`
  );
export const searchProducts = (lang: string, keyword: string) =>
  request<Product[]>(`/products/search?lang=${lang}&keyword=${encodeURIComponent(keyword)}`);
export const getProductRecommendations = (lang: string, sourceType: "SERIES" | "PRODUCT", sourceId: number) =>
  request<Array<{ targetType: string; targetId: number; slug: string | null; name: string | null }>>(
    `/products/recommendations?lang=${lang}&sourceType=${sourceType}&sourceId=${sourceId}`
  );
export const getApplications = (lang: string) => request<Application[]>(`/applications?lang=${lang}`);
export const getApplicationDetail = (lang: string, applicationSlug: string) =>
  request<Application>(`/applications/${applicationSlug}?lang=${lang}`);
export const getNews = (lang: string) => request<News[]>(`/news?lang=${lang}`);
export const getNewsCategories = (lang: string) => request<NewsCategory[]>(`/news/categories?lang=${lang}`);
export const getNewsByCategory = (lang: string, categorySlug: string) =>
  request<NewsPage>(`/news/categories/${categorySlug}?lang=${lang}&page=1&pageSize=8`);
export const getNewsPageByCategory = (lang: string, categorySlug: string, page = 1, pageSize = 8) =>
  request<NewsPage>(`/news/categories/${categorySlug}?lang=${lang}&page=${page}&pageSize=${pageSize}`);
export const getNewsDetail = (lang: string, categorySlug: string, articleSlug: string) =>
  request<NewsDetail>(`/news/${categorySlug}/${articleSlug}?lang=${lang}`);
export const getLegacyNewsDetail = (lang: string, articleSlug: string) =>
  request<NewsDetail>(`/news/${articleSlug}?lang=${lang}`);
export const getSeo = (lang: string, pageKey: string) =>
  request<SeoMeta>(`/seo?lang=${lang}&pageKey=${pageKey}`);
export const createInquiry = (body: InquiryPayload) =>
  request<InquiryResult>("/inquiries", {
    method: "POST",
    body: JSON.stringify(body)
  });

export const getCaptcha = () =>
  request<{ image: string; sessionId: string }>("/captcha");

export const adminListCategories = () => request<Array<Record<string, unknown>>>("/admin/products/categories");
export const adminCreateCategory = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/products/categories", {
    method: "POST",
    body: JSON.stringify(body)
  });
export const adminUpdateCategory = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/products/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify(body)
  });
export const adminUpdateCategorySort = (id: number, sortOrder: number) =>
  request<{ id: number; sortOrder: number }>(`/admin/products/categories/${id}/sort`, {
    method: "POST",
    body: JSON.stringify({ sortOrder })
  });
export const adminUpdateCategoryStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/products/categories/${id}/status`, {
    method: "POST",
    body: JSON.stringify({ publishStatus })
  });
export const adminDeleteCategory = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/products/categories/${id}`, { method: "DELETE" });

export const adminListSeries = (categoryId?: number) =>
  request<Array<Record<string, unknown>>>(`/admin/products/series${categoryId ? `?categoryId=${categoryId}` : ""}`);
export const adminCreateSeries = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/products/series", {
    method: "POST",
    body: JSON.stringify(body)
  });
export const adminUpdateSeries = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/products/series/${id}`, {
    method: "PUT",
    body: JSON.stringify(body)
  });
export const adminUpdateSeriesSort = (id: number, sortOrder: number) =>
  request<{ id: number; sortOrder: number }>(`/admin/products/series/${id}/sort`, {
    method: "POST",
    body: JSON.stringify({ sortOrder })
  });
export const adminUpdateSeriesStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/products/series/${id}/status`, {
    method: "POST",
    body: JSON.stringify({ publishStatus })
  });
export const adminDeleteSeries = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/products/series/${id}`, { method: "DELETE" });

export const adminListApplications = () => request<Array<Record<string, unknown>>>("/admin/applications");
export const adminCreateApplication = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/applications", { method: "POST", body: JSON.stringify(body) });
export const adminUpdateApplication = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/applications/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const adminDeleteApplication = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/applications/${id}`, { method: "DELETE" });
export const adminListDeployments = () => request<Array<Record<string, unknown>>>("/admin/deployments");
export const adminCreateDeployment = () =>
  request<Record<string, unknown>>("/admin/deployments", {
    method: "POST",
    body: JSON.stringify({ confirmed: true })
  });
export const adminApplicationSeriesOptions = () =>
  request<Array<Record<string, unknown>>>("/admin/applications/series-options");
export const adminGetApplicationSeries = (id: number) =>
  request<{ seriesIds: number[] }>(`/admin/applications/${id}/series`);
export const adminUpdateApplicationSeries = (id: number, seriesIds: number[]) =>
  request<{ applicationId: number; seriesIds: number[]; updated: boolean }>(`/admin/applications/${id}/series`, {
    method: "PUT",
    body: JSON.stringify({ seriesIds })
  });

export const adminListProducts = (categoryId?: number, seriesId?: number) => {
  const params = new URLSearchParams();
  if (categoryId) params.set("categoryId", String(categoryId));
  if (seriesId) params.set("seriesId", String(seriesId));
  const query = params.toString();
  return request<Array<Record<string, unknown>>>(`/admin/products${query ? `?${query}` : ""}`);
};
export const adminCreateProduct = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/products", {
    method: "POST",
    body: JSON.stringify(body)
  });
export const adminUpdateProduct = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/products/${id}`, {
    method: "PUT",
    body: JSON.stringify(body)
  });
export const adminUpdateProductSort = (id: number, sortOrder: number) =>
  request<{ id: number; sortOrder: number }>(`/admin/products/${id}/sort`, {
    method: "POST",
    body: JSON.stringify({ sortOrder })
  });
export const adminUpdateProductStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/products/${id}/status`, {
    method: "POST",
    body: JSON.stringify({ publishStatus })
  });
export const adminDeleteProduct = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/products/${id}`, { method: "DELETE" });

export type AdminInquiryFilters = {
  status?: string;
  keyword?: string;
  from?: string;
  to?: string;
};

export const adminListInquiries = (filters: AdminInquiryFilters = {}) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
  const query = params.toString();
  return request<Array<Record<string, unknown>>>(`/admin/inquiries${query ? `?${query}` : ""}`);
};
export const adminGetInquiry = (id: number) => request<Record<string, unknown>>(`/admin/inquiries/${id}`);
export const adminUpdateInquiryStatus = (id: number, status: string) =>
  request<{ id: number; status: string }>(`/admin/inquiries/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) });
export const adminUpdateInquiryNote = (id: number, internalNote: string) =>
  request<{ id: number; updated: boolean }>(`/admin/inquiries/${id}/note`, { method: "PUT", body: JSON.stringify({ internalNote }) });
export async function adminExportInquiries(filters: AdminInquiryFilters = {}) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
  const response = await fetch(`${API_BASE}/admin/inquiries/export${params.size ? `?${params}` : ""}`, { credentials: "include" });
  if (!response.ok) throw new Error(`export failed: ${response.status}`);
  return response.blob();
}

export type AdminPage<T> = { items: T[]; total: number; page: number; pageSize: number };
const newsQuery = (filters: Record<string, string | number | undefined>) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => { if (value !== undefined && value !== "") params.set(key, String(value)); });
  return params.toString();
};
export const adminListNewsCategories = (filters: Record<string, string | number | undefined> = {}) =>
  request<AdminPage<Record<string, unknown>>>(`/admin/news/categories?${newsQuery(filters)}`);
export const adminNewsCategoryOptions = () => request<Array<Record<string, unknown>>>("/admin/news/categories/options");
export const adminCreateNewsCategory = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/news/categories", { method: "POST", body: JSON.stringify(body) });
export const adminUpdateNewsCategory = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/news/categories/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const adminUpdateNewsCategoryStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/news/categories/${id}/status`, { method: "POST", body: JSON.stringify({ publishStatus }) });
export const adminDeleteNewsCategory = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/news/categories/${id}`, { method: "DELETE" });
export const adminListNewsArticles = (filters: Record<string, string | number | undefined> = {}) =>
  request<AdminPage<Record<string, unknown>>>(`/admin/news/articles?${newsQuery(filters)}`);
export const adminNewsArticleOptions = (excludeId?: number) =>
  request<Array<Record<string, unknown>>>(`/admin/news/articles/options${excludeId ? `?excludeId=${excludeId}` : ""}`);
export const adminNewsProductOptions = () => request<Array<Record<string, unknown>>>("/admin/news/products/options");
export const adminCreateNewsArticle = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/news/articles", { method: "POST", body: JSON.stringify(body) });
export const adminUpdateNewsArticle = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/news/articles/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const adminUpdateNewsArticleStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/news/articles/${id}/status`, { method: "POST", body: JSON.stringify({ publishStatus }) });
export const adminDeleteNewsArticle = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/news/articles/${id}`, { method: "DELETE" });

export type PublishedCertificate = { certificate_no: string; name: string; image_url: string; alt_text: string; sort_order: number };
export const getCertificates = (lang: string) => request<PublishedCertificate[]>(`/certificates?lang=${lang}`);
export const adminListCertificates = () => request<Array<Record<string, unknown>>>("/admin/certificates");
export const adminCreateCertificate = (body: Record<string, unknown>) =>
  request<{ id: number }>("/admin/certificates", { method: "POST", body: JSON.stringify(body) });
export const adminUpdateCertificate = (id: number, body: Record<string, unknown>) =>
  request<{ id: number; updated: boolean }>(`/admin/certificates/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const adminUpdateCertificateStatus = (id: number, publishStatus: string) =>
  request<{ id: number; publishStatus: string }>(`/admin/certificates/${id}/status`, { method: "PUT", body: JSON.stringify({ publishStatus }) });
export const adminDeleteCertificate = (id: number) =>
  request<{ id: number; deleted: boolean }>(`/admin/certificates/${id}`, { method: "DELETE" });

export type AdminSeoRecord = {
  page_key: string; lang: "zh" | "en"; title: string; description?: string; og_title?: string;
  og_description?: string; og_image?: string; canonical?: string; publish_status: string; updated_at?: string;
};
export const adminListSeoPages = () => request<string[]>("/admin/seo/pages");
export const adminGetSeo = (pageKey: string, lang: string) =>
  request<AdminSeoRecord>(`/admin/seo?pageKey=${encodeURIComponent(pageKey)}&lang=${encodeURIComponent(lang)}`);
export const adminUpdateSeo = (pageKey: string, lang: string, body: Record<string, unknown>) =>
  request<{ pageKey: string; lang: string; updated: boolean }>(`/admin/seo?pageKey=${encodeURIComponent(pageKey)}&lang=${encodeURIComponent(lang)}`, { method: "PUT", body: JSON.stringify(body) });

export type AnalyticsOverview = { visits: number; inquiries: number; validInquiries: number; conversionRate: number; averageFirstContactMinutes: number };
const analyticsQuery = (filters: Record<string, string>, extra?: Record<string, string>) => {
  const params = new URLSearchParams({ ...filters, ...extra });
  for (const [key, value] of [...params.entries()]) if (!value) params.delete(key);
  return params.toString();
};
export const adminAnalyticsOverview = (filters: Record<string, string>) => request<AnalyticsOverview>(`/admin/analytics/overview?${analyticsQuery(filters)}`);
export const adminAnalyticsFunnel = (filters: Record<string, string>) => request<Array<Record<string, unknown>>>(`/admin/analytics/funnel?${analyticsQuery(filters)}`);
export const adminAnalyticsAttribution = (filters: Record<string, string>, dimension: string) => request<Array<Record<string, unknown>>>(`/admin/analytics/attribution?${analyticsQuery(filters, { dimension })}`);
export const adminAnalyticsHighIntent = (filters: Record<string, string>) => request<Array<Record<string, unknown>>>(`/admin/analytics/high-intent-inquiries?${analyticsQuery(filters)}`);
export const adminAnalyticsRebuild = (from: string, to: string) => request<{ rebuiltDays: number; from: string; to: string }>(`/admin/analytics/rebuild?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`, { method: "POST" });

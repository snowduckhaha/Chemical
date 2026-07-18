export type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

export type NavItem = {
  key: string;
  label: string;
  path: string;
};

export type HomeSection = {
  heroTitle: string;
  heroSubtitle: string;
  ctaText: string;
  ctaLink: string;
  moduleOrder: string[];
};

export type Category = {
  slug: string;
  name: string;
  summary: string;
  image: string;
  formula?: string;
  applications?: string[];
};

export type Series = {
  slug: string;
  categorySlug: string;
  name: string;
  summary: string;
  image: string;
  applications?: string[];
};

export type ParameterItem = {
  label: string;
  value: string;
};

export type Product = {
  slug: string;
  categorySlug: string;
  seriesSlug: string;
  name: string;
  model: string;
  summary: string;
  detail: string;
  image: string;
  parameters: ParameterItem[];
  applications: string[];
  packaging: string;
  publishStatus: string;
};

export type LinkedSeries = {
  categorySlug: string;
  seriesSlug: string;
  title: string;
  summary: string;
  image: string;
};

export type Application = {
  slug: string;
  name: string;
  overview: string;
  image: string;
  highlights: string[];
  linkedSeries: LinkedSeries[];
  faqs: string[];
};

export type News = {
  slug: string;
  title: string;
  summary: string;
  categorySlug: string;
  category: string;
  publishedAt: string;
  coverImage?: string;
  coverAlt?: string;
};

export type NewsCategory = {
  slug: string;
  name: string;
};

export type NewsPage = {
  items: News[];
  total: number;
  page: number;
  pageSize: number;
};

export type NewsDetail = {
  slug: string;
  categorySlug: string;
  title: string;
  category: string;
  summary: string;
  publishedAt: string;
  content: string;
  coverImage?: string;
  coverAlt?: string;
  relatedSlugs: string[];
  recommendedProductSlugs: string[];
};

export type SeoMeta = {
  pageKey: string;
  title: string;
  description: string;
  ogTitle: string;
  ogDescription: string;
  ogImage: string | null;
  canonical: string;
};

export type InquiryPayload = {
  lang: string;
  name: string;
  company?: string;
  email: string;
  phone?: string;
  country?: string;
  interestedProduct?: string;
  message: string;
  sourcePage?: string;
};

export type InquiryResult = {
  inquiryId: string;
  status: string;
};

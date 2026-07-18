import type { Category, Product, Series } from "../types/site";

const PRODUCT_IMAGES = {
  ath: "/products/aluminum-hydroxide.jpg",
  silica: "/products/silica-powder-series.jpg",
  alumina: "/products/alumina-powder.jpg",
  silane: "/products/silane-coupling-agent.jpg"
} as const;

type Localized = {
  zh: string;
  en: string;
};

type BaseCategory = {
  slug: string;
  name: Localized;
  summary: Localized;
  image: string;
  applications: Localized[];
};

type BaseSeries = {
  slug: string;
  categorySlug: string;
  name: Localized;
  summary: Localized;
  image: string;
  applications: Localized[];
};

type BaseParameter = {
  label: Localized;
  value: string;
};

type BaseProduct = {
  slug: string;
  categorySlug: string;
  seriesSlug: string;
  name: Localized;
  model: string;
  summary: Localized;
  image: string;
  applications: Localized[];
  parameters: BaseParameter[];
  packaging: Localized;
  publishStatus: string;
};

const categoryItems: BaseCategory[] = [
  {
    slug: "aluminum-hydroxide",
    name: { zh: "氢氧化铝", en: "Aluminum Hydroxide" },
    summary: {
      zh: "我们的氢氧化铝产品具有高纯度、优异白度和可控粒径分布，适用于线缆、覆铜板与无卤阻燃体系。",
      en: "High-purity ATH materials with stable whiteness and controlled particle distribution for cable, CCL, and halogen-free flame-retardant systems."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "电线电缆", en: "Wire & Cable" },
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "热塑性材料", en: "Thermoplastics" }
    ]
  },
  {
    slug: "silica-powder-series",
    name: { zh: "硅粉系列", en: "Silica Powder Series" },
    summary: {
      zh: "面向电子封装、绝缘材料与覆铜板体系的功能性硅微粉材料，兼顾介电性能与尺寸稳定性。",
      en: "Functional silica materials for electronic packaging, insulation, and CCL systems, balancing dielectric performance and dimensional stability."
    },
    image: PRODUCT_IMAGES.silica,
    applications: [
      { zh: "电子封装", en: "Electronic Packaging" },
      { zh: "绝缘材料", en: "Insulation" },
      { zh: "覆铜板", en: "Copper Clad Laminate" }
    ]
  },
  {
    slug: "alumina-powder",
    name: { zh: "氧化铝粉末", en: "Alumina Powder" },
    summary: {
      zh: "高纯氧化铝粉末兼顾导热、绝缘与粒径稳定性，适配导热界面材料与电子陶瓷场景。",
      en: "High-purity alumina powders balancing thermal conductivity, insulation, and particle-size stability for TIM and electronic ceramics."
    },
    image: PRODUCT_IMAGES.alumina,
    applications: [
      { zh: "导热界面材料", en: "Thermal Interface Material" },
      { zh: "电子陶瓷", en: "Electronic Ceramic" },
      { zh: "精密抛光", en: "Precision Polishing" }
    ]
  },
  {
    slug: "silane-coupling-agent",
    name: { zh: "硅烷偶联剂", en: "Silane Coupling Agent" },
    summary: {
      zh: "用于无机粉体表面改性与树脂相容增强的功能助剂系列，帮助提升界面结合与加工稳定性。",
      en: "Functional coupling agents for inorganic filler surface treatment and resin compatibility enhancement."
    },
    image: PRODUCT_IMAGES.silane,
    applications: [
      { zh: "粉体改性", en: "Filler Modification" },
      { zh: "树脂相容性", en: "Resin Compatibility" },
      { zh: "表面处理", en: "Surface Treatment" }
    ]
  }
];

const seriesItems: BaseSeries[] = [
  {
    slug: "qd-f-series",
    categorySlug: "aluminum-hydroxide",
    name: { zh: "PF系列", en: "PF Series" },
    summary: {
      zh: "细沉淀氢氧化铝系列，具有低电解质、窄粒径分布和高白度特点，适用于阻燃与增强体系。",
      en: "Fine precipitated ATH series featuring low electrolyte content, narrow particle distribution, and high whiteness."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "电线电缆", en: "Wire & Cable" },
      { zh: "橡胶应用", en: "Rubber Applications" },
      { zh: "热塑性材料", en: "Thermoplastics" }
    ]
  },
  {
    slug: "modified-series",
    categorySlug: "aluminum-hydroxide",
    name: { zh: "改性系列", en: "Modified Series" },
    summary: {
      zh: "通过表面改性提升 ATH 与树脂体系的相容性、分散性和加工稳定性。",
      en: "Surface-modified ATH series improving compatibility, dispersion, and processing stability in resin systems."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "线缆料", en: "Cable Compounds" },
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "工程塑料", en: "Engineering Plastics" }
    ]
  },
  {
    slug: "tc-series",
    categorySlug: "aluminum-hydroxide",
    name: { zh: "TC系列", en: "TC Series" },
    summary: {
      zh: "面向阻燃填充与机械增强平衡的 ATH 规格系列，适配复合材料场景。",
      en: "ATH grades balancing flame retardancy and reinforcement for composite material systems."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "无卤阻燃", en: "Halogen-Free Flame Retardancy" },
      { zh: "复合材料", en: "Composites" },
      { zh: "片材板材", en: "Sheet Materials" }
    ]
  },
  {
    slug: "cop-series",
    categorySlug: "aluminum-hydroxide",
    name: { zh: "COP系列", en: "COP Series" },
    summary: {
      zh: "强调白度与粒径一致性的 ATH 系列，适用于对外观和稳定性有要求的体系。",
      en: "ATH grades emphasizing whiteness and particle consistency for systems requiring stable appearance and performance."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "外观件", en: "Appearance Parts" },
      { zh: "电子绝缘", en: "Electrical Insulation" },
      { zh: "通用阻燃", en: "General Flame Retardancy" }
    ]
  },
  {
    slug: "na-series",
    categorySlug: "aluminum-hydroxide",
    name: { zh: "NA系列", en: "NA Series" },
    summary: {
      zh: "兼顾成本、白度与填充效率的 ATH 系列，适合常规批量应用。",
      en: "ATH grades balancing cost, whiteness, and loading efficiency for standard-volume applications."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "批量应用", en: "Volume Applications" },
      { zh: "阻燃填料", en: "Flame-Retardant Filler" },
      { zh: "聚合物体系", en: "Polymer Systems" }
    ]
  },
  {
    slug: "fused-silica-series",
    categorySlug: "silica-powder-series",
    name: { zh: "熔融硅粉系列", en: "Fused Silica Series" },
    summary: {
      zh: "低热膨胀、低介电损耗的熔融硅粉系列，适配覆铜板和电子封装体系。",
      en: "Low-expansion and low-dielectric-loss fused silica materials for CCL and electronic packaging."
    },
    image: PRODUCT_IMAGES.silica,
    applications: [
      { zh: "电子封装", en: "Electronic Packaging" },
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "绝缘填充", en: "Insulation Filler" }
    ]
  },
  {
    slug: "qd-ql-series",
    categorySlug: "alumina-powder",
    name: { zh: "QD-QL 系列", en: "QD-QL Series" },
    summary: {
      zh: "高纯超细氧化铝系列，兼顾导热绝缘性能与粒径一致性，适配导热和电子陶瓷。",
      en: "Ultra-fine high-purity alumina series for thermal conductivity, insulation, and electronic ceramic systems."
    },
    image: PRODUCT_IMAGES.alumina,
    applications: [
      { zh: "导热材料", en: "Thermal Materials" },
      { zh: "电子陶瓷", en: "Electronic Ceramics" },
      { zh: "精密抛光", en: "Precision Polishing" }
    ]
  },
  {
    slug: "amino-silane-series",
    categorySlug: "silane-coupling-agent",
    name: { zh: "氨基硅烷系列", en: "Amino Silane Series" },
    summary: {
      zh: "面向无机粉体与树脂界面增强的氨基硅烷体系，用于提升润湿、分散和结合强度。",
      en: "Amino silane systems for improving wetting, dispersion, and interfacial bonding between fillers and resins."
    },
    image: PRODUCT_IMAGES.silane,
    applications: [
      { zh: "界面增强", en: "Interfacial Enhancement" },
      { zh: "表面处理", en: "Surface Treatment" },
      { zh: "树脂改性", en: "Resin Modification" }
    ]
  }
];

const productItems: BaseProduct[] = [
  {
    slug: "qd-f02",
    categorySlug: "aluminum-hydroxide",
    seriesSlug: "qd-f-series",
    name: { zh: "QD-F02", en: "QD-F02" },
    model: "QD-F02",
    summary: {
      zh: "白度高、分散稳定，适用于覆铜板、线缆及无卤阻燃复合材料。",
      en: "High-whiteness and stable-dispersion ATH grade for CCL, cable, and halogen-free compounds."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "电线电缆", en: "Wire & Cable" },
      { zh: "阻燃复合材料", en: "Flame-Retardant Compound" }
    ],
    parameters: [
      { label: { zh: "白度", en: "Whiteness" }, value: "98" },
      { label: { zh: "中位粒径", en: "D50" }, value: "2 um" },
      { label: { zh: "附着水", en: "Moisture" }, value: "0.15%" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "qd-f05",
    categorySlug: "aluminum-hydroxide",
    seriesSlug: "qd-f-series",
    name: { zh: "QD-F05", en: "QD-F05" },
    model: "QD-F05",
    summary: {
      zh: "粒径更细、分散性更好的 ATH 规格，适用于高填充无卤阻燃配方。",
      en: "Finer ATH grade with improved dispersion for high-loading halogen-free formulations."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "高填充阻燃", en: "High-Loading Flame Retardancy" },
      { zh: "电线电缆", en: "Wire & Cable" },
      { zh: "橡胶应用", en: "Rubber Applications" }
    ],
    parameters: [
      { label: { zh: "白度", en: "Whiteness" }, value: "97" },
      { label: { zh: "中位粒径", en: "D50" }, value: "1.5 um" },
      { label: { zh: "附着水", en: "Moisture" }, value: "0.18%" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "qd-f08",
    categorySlug: "aluminum-hydroxide",
    seriesSlug: "qd-f-series",
    name: { zh: "QD-F08", en: "QD-F08" },
    model: "QD-F08",
    summary: {
      zh: "用于覆铜板和绝缘填料场景的 ATH 牌号，兼顾加工稳定与阻燃效率。",
      en: "ATH grade for CCL and insulation filler scenarios, balancing process stability and flame-retardant efficiency."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "绝缘填料", en: "Insulation Filler" },
      { zh: "热塑性材料", en: "Thermoplastics" }
    ],
    parameters: [
      { label: { zh: "白度", en: "Whiteness" }, value: "96" },
      { label: { zh: "中位粒径", en: "D50" }, value: "3 um" },
      { label: { zh: "附着水", en: "Moisture" }, value: "0.12%" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "qd-f10",
    categorySlug: "aluminum-hydroxide",
    seriesSlug: "qd-f-series",
    name: { zh: "QD-F10", en: "QD-F10" },
    model: "QD-F10",
    summary: {
      zh: "面向通用阻燃和增强用途的 ATH 规格，适用于稳定量产体系。",
      en: "General-purpose ATH grade for flame retardancy and reinforcement in stable mass-production systems."
    },
    image: PRODUCT_IMAGES.ath,
    applications: [
      { zh: "通用阻燃", en: "General Flame Retardancy" },
      { zh: "增强填充", en: "Reinforcement Filling" },
      { zh: "聚合物体系", en: "Polymer Systems" }
    ],
    parameters: [
      { label: { zh: "白度", en: "Whiteness" }, value: "95" },
      { label: { zh: "中位粒径", en: "D50" }, value: "4 um" },
      { label: { zh: "附着水", en: "Moisture" }, value: "0.10%" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "fs-3000",
    categorySlug: "silica-powder-series",
    seriesSlug: "fused-silica-series",
    name: { zh: "FS-3000", en: "FS-3000" },
    model: "FS-3000",
    summary: {
      zh: "低热膨胀、绝缘稳定的熔融硅粉牌号，适用于覆铜板和电子绝缘材料。",
      en: "Fused silica grade with low thermal expansion and stable insulation for CCL and electrical materials."
    },
    image: PRODUCT_IMAGES.silica,
    applications: [
      { zh: "覆铜板", en: "Copper Clad Laminate" },
      { zh: "电子绝缘", en: "Electrical Insulation" },
      { zh: "封装填料", en: "Packaging Filler" }
    ],
    parameters: [
      { label: { zh: "纯度", en: "Purity" }, value: "99.9%" },
      { label: { zh: "介电损耗", en: "Dielectric Loss" }, value: "低" },
      { label: { zh: "热膨胀", en: "Thermal Expansion" }, value: "低" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "qd-ql01",
    categorySlug: "alumina-powder",
    seriesSlug: "qd-ql-series",
    name: { zh: "QD-QL01", en: "QD-QL01" },
    model: "QD-QL01",
    summary: {
      zh: "高纯超细氧化铝粉，适配导热界面材料与电子陶瓷，兼顾导热与绝缘平衡。",
      en: "High-purity ultra-fine alumina for TIM and electronic ceramics, balancing thermal and insulation performance."
    },
    image: PRODUCT_IMAGES.alumina,
    applications: [
      { zh: "导热界面材料", en: "Thermal Interface Material" },
      { zh: "电子陶瓷", en: "Electronic Ceramics" },
      { zh: "精密抛光", en: "Precision Polishing" }
    ],
    parameters: [
      { label: { zh: "Al2O3", en: "Al2O3" }, value: "99.6%" },
      { label: { zh: "导热性能", en: "Thermal Conductivity" }, value: "优" },
      { label: { zh: "形貌", en: "Morphology" }, value: "球形" }
    ],
    packaging: { zh: "25kg 袋装", en: "25kg bag" },
    publishStatus: "PUBLISHED"
  },
  {
    slug: "am-1100",
    categorySlug: "silane-coupling-agent",
    seriesSlug: "amino-silane-series",
    name: { zh: "AM-1100", en: "AM-1100" },
    model: "AM-1100",
    summary: {
      zh: "氨基硅烷偶联剂，适用于 ATH、硅粉与氧化铝粉体改性，提升界面相容与润湿表现。",
      en: "Amino silane coupling agent for ATH, silica, and alumina filler modification with stronger interfacial compatibility."
    },
    image: PRODUCT_IMAGES.silane,
    applications: [
      { zh: "粉体改性", en: "Filler Modification" },
      { zh: "树脂相容性", en: "Resin Compatibility" },
      { zh: "表面处理", en: "Surface Treatment" }
    ],
    parameters: [
      { label: { zh: "类型", en: "Type" }, value: "氨基硅烷" },
      { label: { zh: "核心特性", en: "Core Feature" }, value: "增强界面结合" },
      { label: { zh: "包装", en: "Packaging" }, value: "5kg / 25kg" }
    ],
    packaging: { zh: "5kg / 25kg 桶装", en: "5kg / 25kg drum" },
    publishStatus: "PUBLISHED"
  }
];

const text = (lang: string, localized: Localized) => (lang === "en" ? localized.en : localized.zh);

export const getFallbackCategories = (lang: string): Category[] =>
  categoryItems.map((item) => ({
    slug: item.slug,
    name: text(lang, item.name),
    summary: text(lang, item.summary),
    image: item.image
  }));

export const getFallbackCategory = (lang: string, slug: string): Category | undefined =>
  getFallbackCategories(lang).find((item) => item.slug === slug);

export const getFallbackCategoryApplications = (lang: string, slug: string): string[] =>
  categoryItems
    .find((item) => item.slug === slug)
    ?.applications.map((item) => text(lang, item)) || [];

export const getFallbackSeries = (lang: string, categorySlug: string): Series[] =>
  seriesItems
    .filter((item) => item.categorySlug === categorySlug)
    .map((item) => ({
      slug: item.slug,
      categorySlug: item.categorySlug,
      name: text(lang, item.name),
      summary: text(lang, item.summary),
      image: item.image
    }));

export const getFallbackSeriesInfo = (lang: string, categorySlug: string, seriesSlug: string): Series | undefined =>
  getFallbackSeries(lang, categorySlug).find((item) => item.slug === seriesSlug);

export const getFallbackSeriesApplications = (lang: string, categorySlug: string, seriesSlug: string): string[] =>
  seriesItems
    .find((item) => item.categorySlug === categorySlug && item.slug === seriesSlug)
    ?.applications.map((item) => text(lang, item)) || [];

export const getFallbackProducts = (lang: string, categorySlug: string, seriesSlug: string): Product[] =>
  productItems
    .filter((item) => item.categorySlug === categorySlug && item.seriesSlug === seriesSlug)
    .map((item) => ({
      slug: item.slug,
      categorySlug: item.categorySlug,
      seriesSlug: item.seriesSlug,
      name: text(lang, item.name),
      model: item.model,
      summary: text(lang, item.summary),
      image: item.image,
      applications: item.applications.map((application) => text(lang, application)),
      parameters: item.parameters.map((parameter) => ({
        label: text(lang, parameter.label),
        value: parameter.value
      })),
      packaging: text(lang, item.packaging),
      publishStatus: item.publishStatus
    }));

export const getFallbackProductDetail = (
  lang: string,
  categorySlug: string,
  seriesSlug: string,
  productSlug: string
): Product | undefined =>
  getFallbackProducts(lang, categorySlug, seriesSlug).find((item) => item.slug === productSlug);
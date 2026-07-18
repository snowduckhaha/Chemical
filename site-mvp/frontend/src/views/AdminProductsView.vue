<template>
  <section class="admin-page">
    <header class="admin-header">
      <div>
        <h1>{{ pageTitle }}</h1>
        <p>{{ lang === "en" ? "Product content maintenance" : "产品内容维护" }}</p>
      </div>
      <button type="button" class="logout" @click="logout">退出登录</button>
    </header>

    <nav class="admin-nav" aria-label="后台导航">
      <RouterLink :to="`/${lang}/admin/products/categories`">分类</RouterLink>
      <RouterLink :to="`/${lang}/admin/products/series`">系列</RouterLink>
      <RouterLink :to="`/${lang}/admin/products/details`">产品详情</RouterLink>
      <RouterLink v-if="auth.isAdmin" :to="`/${lang}/admin/analytics`">数据概览</RouterLink>
    </nav>

    <div class="grid">
      <article v-if="section === 'categories'" class="panel">
        <h2>{{ lang === "en" ? "Categories" : "分类" }}</h2>
        <form class="form" @submit.prevent="submitCategory">
          <input v-model.trim="categoryForm.slug" placeholder="slug" required />
          <input v-model.trim="categoryForm.nameZh" placeholder="name_zh" required />
          <input v-model.trim="categoryForm.nameEn" placeholder="name_en" required />
          <input v-model.number="categoryForm.sortOrder" type="number" placeholder="sort_order" required />
          <select v-model="categoryForm.publishStatus">
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="OFFLINE">OFFLINE</option>
          </select>
          <button type="submit">{{ editingCategoryId ? (lang === "en" ? "Update" : "更新") : (lang === "en" ? "Create" : "新增") }}</button>
          <button v-if="editingCategoryId" type="button" @click="resetCategoryForm">{{ lang === "en" ? "Cancel" : "取消" }}</button>
        </form>
        <ul>
          <li v-for="item in categories" :key="Number(item.id)">
            <strong>{{ String(item.slug) }}</strong>
            <span> #{{ Number(item.sort_order) }}</span>
            <button @click="editCategory(item)">edit</button>
            <button @click="setCategorySort(Number(item.id), Number(item.sort_order) + 1)">+sort</button>
            <button @click="setCategoryStatus(Number(item.id), 'PUBLISHED')">publish</button>
            <button @click="setCategoryStatus(Number(item.id), 'OFFLINE')">offline</button>
          </li>
        </ul>
      </article>

      <article v-if="section === 'series'" class="panel">
        <h2>{{ lang === "en" ? "Series" : "系列" }}</h2>
        <form class="form" @submit.prevent="submitSeries">
          <input v-model.number="seriesForm.categoryId" type="number" placeholder="category_id" required />
          <input v-model.trim="seriesForm.slug" placeholder="slug" required />
          <input v-model.trim="seriesForm.nameZh" placeholder="name_zh" required />
          <input v-model.trim="seriesForm.nameEn" placeholder="name_en" required />
          <input v-model.number="seriesForm.sortOrder" type="number" placeholder="sort_order" required />
          <input v-model.trim="seriesForm.imageUrl" placeholder="series image url" />
          <select v-model="seriesForm.publishStatus">
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="OFFLINE">OFFLINE</option>
          </select>
          <button type="submit">{{ editingSeriesId ? (lang === "en" ? "Update" : "更新") : (lang === "en" ? "Create" : "新增") }}</button>
          <button v-if="editingSeriesId" type="button" @click="resetSeriesForm">{{ lang === "en" ? "Cancel" : "取消" }}</button>
        </form>
        <ul>
          <li v-for="item in seriesList" :key="Number(item.id)">
            <strong>{{ String(item.slug) }}</strong>
            <span> #{{ Number(item.sort_order) }}</span>
            <button @click="editSeries(item)">edit</button>
            <button @click="setSeriesSort(Number(item.id), Number(item.sort_order) + 1)">+sort</button>
            <button @click="setSeriesStatus(Number(item.id), 'PUBLISHED')">publish</button>
            <button @click="setSeriesStatus(Number(item.id), 'OFFLINE')">offline</button>
          </li>
        </ul>
      </article>

      <article v-if="section === 'details'" class="panel panel-wide">
        <h2>{{ lang === "en" ? "Products" : "产品" }}</h2>
        <form class="form" @submit.prevent="submitProduct">
          <input v-model.number="productForm.categoryId" type="number" placeholder="category_id" required />
          <input v-model.number="productForm.seriesId" type="number" placeholder="series_id" required />
          <input v-model.trim="productForm.slug" placeholder="slug" required />
          <input v-model.trim="productForm.model" placeholder="model" required />
          <input v-model.trim="productForm.nameZh" placeholder="name_zh" required />
          <input v-model.trim="productForm.nameEn" placeholder="name_en" required />
          <input v-model.number="productForm.sortOrder" type="number" placeholder="sort_order" required />
          <input v-model.trim="productForm.imageUrl" placeholder="product image url" />
          <select v-model="productForm.publishStatus">
            <option value="DRAFT">DRAFT</option>
            <option value="PUBLISHED">PUBLISHED</option>
            <option value="OFFLINE">OFFLINE</option>
          </select>
          <button type="submit">{{ editingProductId ? (lang === "en" ? "Update" : "更新") : (lang === "en" ? "Create" : "新增") }}</button>
          <button v-if="editingProductId" type="button" @click="resetProductForm">{{ lang === "en" ? "Cancel" : "取消" }}</button>
        </form>
        <ul>
          <li v-for="item in products" :key="Number(item.id)">
            <strong>{{ String(item.slug) }}</strong>
            <span> #{{ Number(item.sort_order) }}</span>
            <button @click="editProduct(item)">edit</button>
            <button @click="setProductSort(Number(item.id), Number(item.sort_order) + 1)">+sort</button>
            <button @click="setProductStatus(Number(item.id), 'PUBLISHED')">publish</button>
            <button @click="setProductStatus(Number(item.id), 'OFFLINE')">offline</button>
          </li>
        </ul>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
  adminCreateCategory,
  adminCreateProduct,
  adminCreateSeries,
  adminListCategories,
  adminListProducts,
  adminListSeries,
  adminUpdateCategory,
  adminUpdateProduct,
  adminUpdateCategorySort,
  adminUpdateCategoryStatus,
  adminUpdateProductSort,
  adminUpdateProductStatus,
  adminUpdateSeries,
  adminUpdateSeriesSort,
  adminUpdateSeriesStatus
} from "../api/site";
import { useAdminAuthStore } from "../stores/adminAuth";

const route = useRoute();
const router = useRouter();
const auth = useAdminAuthStore();
const lang = computed(() => String(route.params.lang || "zh"));
const section = computed(() => {
  if (route.name === "admin-product-series") return "series";
  if (route.name === "admin-product-details") return "details";
  return "categories";
});
const pageTitle = computed(() => {
  const titles = {
    categories: lang.value === "en" ? "Category Management" : "分类管理",
    series: lang.value === "en" ? "Series Management" : "系列管理",
    details: lang.value === "en" ? "Product Management" : "产品详情管理"
  };
  return titles[section.value];
});

const categories = ref<Array<Record<string, unknown>>>([]);
const seriesList = ref<Array<Record<string, unknown>>>([]);
const products = ref<Array<Record<string, unknown>>>([]);
const editingCategoryId = ref<number | null>(null);
const editingSeriesId = ref<number | null>(null);
const editingProductId = ref<number | null>(null);

const categoryForm = reactive({
  slug: "",
  nameZh: "",
  nameEn: "",
  summaryZh: "",
  summaryEn: "",
  sortOrder: 10,
  publishStatus: "DRAFT"
});

const seriesForm = reactive({
  categoryId: 1,
  slug: "",
  nameZh: "",
  nameEn: "",
  summaryZh: "",
  summaryEn: "",
  sortOrder: 10,
  publishStatus: "DRAFT",
  imageUrl: "",
  imageAltZh: "",
  imageAltEn: ""
});

const productForm = reactive({
  categoryId: 1,
  seriesId: 11,
  slug: "",
  model: "",
  nameZh: "",
  nameEn: "",
  summaryZh: "",
  summaryEn: "",
  detailZh: "",
  detailEn: "",
  packagingZh: "",
  packagingEn: "",
  sortOrder: 10,
  publishStatus: "DRAFT",
  imageUrl: "",
  imageAltZh: "",
  imageAltEn: "",
  parameters: [
    {
      paramKey: "spec-1",
      paramNameZh: "参数",
      paramNameEn: "Parameter",
      paramValueRaw: "value",
      unit: "",
      testMethod: "",
      sortOrder: 10,
      publishStatus: "PUBLISHED"
    }
  ]
});

const loadAll = async () => {
  categories.value = await adminListCategories();
  seriesList.value = await adminListSeries();
  products.value = await adminListProducts();
};

const createCategory = async () => {
  await adminCreateCategory({ ...categoryForm });
  resetCategoryForm();
  await loadAll();
};

const submitCategory = async () => {
  if (editingCategoryId.value) {
    await adminUpdateCategory(editingCategoryId.value, { ...categoryForm });
    resetCategoryForm();
    await loadAll();
    return;
  }
  await createCategory();
};

const createSeries = async () => {
  await adminCreateSeries({ ...seriesForm });
  resetSeriesForm();
  await loadAll();
};

const submitSeries = async () => {
  if (editingSeriesId.value) {
    await adminUpdateSeries(editingSeriesId.value, { ...seriesForm });
    resetSeriesForm();
    await loadAll();
    return;
  }
  await createSeries();
};

const createProduct = async () => {
  await adminCreateProduct({ ...productForm });
  resetProductForm();
  await loadAll();
};

const submitProduct = async () => {
  if (editingProductId.value) {
    await adminUpdateProduct(editingProductId.value, { ...productForm });
    resetProductForm();
    await loadAll();
    return;
  }
  await createProduct();
};

const editCategory = (item: Record<string, unknown>) => {
  editingCategoryId.value = Number(item.id);
  categoryForm.slug = String(item.slug ?? "");
  categoryForm.nameZh = String(item.name_zh ?? "");
  categoryForm.nameEn = String(item.name_en ?? "");
  categoryForm.summaryZh = String(item.summary_zh ?? "");
  categoryForm.summaryEn = String(item.summary_en ?? "");
  categoryForm.sortOrder = Number(item.sort_order ?? 10);
  categoryForm.publishStatus = String(item.publish_status ?? "DRAFT");
};

const editSeries = (item: Record<string, unknown>) => {
  editingSeriesId.value = Number(item.id);
  seriesForm.categoryId = Number(item.category_id ?? 1);
  seriesForm.slug = String(item.slug ?? "");
  seriesForm.nameZh = String(item.name_zh ?? "");
  seriesForm.nameEn = String(item.name_en ?? "");
  seriesForm.summaryZh = String(item.summary_zh ?? "");
  seriesForm.summaryEn = String(item.summary_en ?? "");
  seriesForm.sortOrder = Number(item.sort_order ?? 10);
  seriesForm.publishStatus = String(item.publish_status ?? "DRAFT");
};

const editProduct = (item: Record<string, unknown>) => {
  editingProductId.value = Number(item.id);
  productForm.categoryId = Number(item.category_id ?? 1);
  productForm.seriesId = Number(item.series_id ?? 11);
  productForm.slug = String(item.slug ?? "");
  productForm.model = String(item.model ?? "");
  productForm.nameZh = String(item.name_zh ?? "");
  productForm.nameEn = String(item.name_en ?? "");
  productForm.summaryZh = String(item.summary_zh ?? "");
  productForm.summaryEn = String(item.summary_en ?? "");
  productForm.sortOrder = Number(item.sort_order ?? 10);
  productForm.publishStatus = String(item.publish_status ?? "DRAFT");
};

const resetCategoryForm = () => {
  editingCategoryId.value = null;
  categoryForm.slug = "";
  categoryForm.nameZh = "";
  categoryForm.nameEn = "";
  categoryForm.summaryZh = "";
  categoryForm.summaryEn = "";
  categoryForm.sortOrder = 10;
  categoryForm.publishStatus = "DRAFT";
};

const resetSeriesForm = () => {
  editingSeriesId.value = null;
  seriesForm.categoryId = 1;
  seriesForm.slug = "";
  seriesForm.nameZh = "";
  seriesForm.nameEn = "";
  seriesForm.summaryZh = "";
  seriesForm.summaryEn = "";
  seriesForm.sortOrder = 10;
  seriesForm.publishStatus = "DRAFT";
  seriesForm.imageUrl = "";
  seriesForm.imageAltZh = "";
  seriesForm.imageAltEn = "";
};

const resetProductForm = () => {
  editingProductId.value = null;
  productForm.categoryId = 1;
  productForm.seriesId = 11;
  productForm.slug = "";
  productForm.model = "";
  productForm.nameZh = "";
  productForm.nameEn = "";
  productForm.summaryZh = "";
  productForm.summaryEn = "";
  productForm.detailZh = "";
  productForm.detailEn = "";
  productForm.packagingZh = "";
  productForm.packagingEn = "";
  productForm.sortOrder = 10;
  productForm.publishStatus = "DRAFT";
  productForm.imageUrl = "";
  productForm.imageAltZh = "";
  productForm.imageAltEn = "";
  productForm.parameters = [
    {
      paramKey: "spec-1",
      paramNameZh: "参数",
      paramNameEn: "Parameter",
      paramValueRaw: "value",
      unit: "",
      testMethod: "",
      sortOrder: 10,
      publishStatus: "PUBLISHED"
    }
  ];
};

const setCategorySort = async (id: number, sortOrder: number) => {
  await adminUpdateCategorySort(id, sortOrder);
  await loadAll();
};

const setCategoryStatus = async (id: number, status: string) => {
  await adminUpdateCategoryStatus(id, status);
  await loadAll();
};

const setSeriesSort = async (id: number, sortOrder: number) => {
  await adminUpdateSeriesSort(id, sortOrder);
  await loadAll();
};

const setSeriesStatus = async (id: number, status: string) => {
  await adminUpdateSeriesStatus(id, status);
  await loadAll();
};

const setProductSort = async (id: number, sortOrder: number) => {
  await adminUpdateProductSort(id, sortOrder);
  await loadAll();
};

const setProductStatus = async (id: number, status: string) => {
  await adminUpdateProductStatus(id, status);
  await loadAll();
};

const logout = async () => {
  await auth.logout();
  await router.replace({ name: "admin-login", params: { lang: lang.value } });
};

onMounted(loadAll);
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.admin-header h1, .admin-header p { margin: 0; }
.admin-header p { margin-top: 6px; color: #64748b; }
.logout { border: 1px solid #cbd5e1; border-radius: 6px; padding: 7px 12px; background: #fff; color: #334155; }
.admin-nav { display: flex; flex-wrap: wrap; gap: 8px; }
.admin-nav a { padding: 7px 11px; border-radius: 6px; color: #475569; text-decoration: none; }
.admin-nav a.router-link-active { background: #eff6ff; color: #1d4ed8; font-weight: 700; }

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  border: 1px solid #e4e9f3;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
}

.panel-wide {
  grid-column: 1 / -1;
}

.form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

input,
select,
button {
  min-height: 34px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 8px;
}

li {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px dashed #d6deed;
  border-radius: 8px;
  padding: 8px;
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .form {
    grid-template-columns: 1fr 1fr;
  }
}
</style>

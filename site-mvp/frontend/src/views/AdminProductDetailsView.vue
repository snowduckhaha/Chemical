<template>
  <AdminShell title="产品详情管理" subtitle="按分类和系列维护产品详情、技术参数及发布状态。">
    <div class="toolbar">
      <label>分类
        <select v-model.number="categoryFilter" @change="categoryChanged">
          <option :value="0">全部分类</option>
          <option v-for="item in categories" :key="Number(item.id)" :value="Number(item.id)">{{ item.name_zh }}</option>
        </select>
      </label>
      <label>系列
        <select v-model.number="seriesFilter" @change="load">
          <option :value="0">全部系列</option>
          <option v-for="item in seriesList" :key="Number(item.id)" :value="Number(item.id)">{{ item.name_zh }}</option>
        </select>
      </label>
      <button @click="startNew">新建产品</button>
    </div>

    <section class="card">
      <h2>产品列表</h2>
      <table><thead><tr><th>型号 / 产品</th><th>参数</th><th>排序</th><th>发布状态</th><th>操作</th></tr></thead>
        <tbody><tr v-for="item in products" :key="Number(item.id)">
          <td><small>{{ item.model }}</small><strong>{{ item.name_zh }}</strong></td>
          <td>{{ parameterCount(item) }} 项</td><td>{{ item.sort_order }}</td><td><span class="status">{{ statusLabel(item.publish_status) }}</span></td>
          <td class="actions"><button @click="edit(item)">编辑</button><button @click="toggle(item)">{{ item.publish_status === "PUBLISHED" ? "下线" : "发布" }}</button><button class="danger" @click="remove(item)">删除</button></td>
        </tr></tbody>
      </table>
    </section>

    <AdminDrawer :open="drawerOpen" :title="editingId ? '编辑产品' : '新建产品'" subtitle="带 * 为必填；产品参数至少一项。" width="920px" @close="cancelEdit">
      <form class="form" @submit.prevent="submit" @invalid.capture="onInvalid">
        <nav class="tabs">
          <button type="button" :class="{ active: activeTab === 'base' }" @click="activeTab = 'base'">基础信息</button>
          <button type="button" :class="{ active: activeTab === 'content' }" @click="activeTab = 'content'">图文详情</button>
          <button type="button" :class="{ active: activeTab === 'media' }" @click="activeTab = 'media'">图片与 SEO</button>
          <button type="button" :class="{ active: activeTab === 'params' }" @click="activeTab = 'params'">产品参数</button>
        </nav>

        <section v-show="activeTab === 'base'" class="tab-panel" data-tab="base">
          <div class="two"><label><span>分类 <span class="required-mark">*</span></span>
            <select v-model.number="form.categoryId" required @change="loadFormSeries">
              <option v-for="item in categories" :key="Number(item.id)" :value="Number(item.id)">{{ item.name_zh }}</option>
            </select>
          </label>
          <label><span>系列 <span class="required-mark">*</span></span>
            <select v-model.number="form.seriesId" required>
              <option v-for="item in formSeries" :key="Number(item.id)" :value="Number(item.id)">{{ item.name_zh }}</option>
            </select>
          </label></div>
          <div class="two"><label><span>产品型号 <span class="required-mark">*</span></span><input v-model.trim="form.model" required /></label><label><span>产品排序 <span class="required-mark">*</span></span><input v-model.number="form.sortOrder" type="number" required /></label></div>
          <div class="two"><label><span>产品名称（中文） <span class="required-mark">*</span></span><input v-model.trim="form.nameZh" required /></label><label><span>产品名称（英文） <span class="required-mark">*</span></span><input v-model.trim="form.nameEn" required /></label></div>
          <div class="two"><label><span>产品发布状态 <span class="required-mark">*</span></span>
            <select v-model="form.publishStatus" required><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select>
          </label></div>
        </section>

        <section v-show="activeTab === 'content'" class="tab-panel" data-tab="content">
          <div class="two"><label>产品简介（中文）<textarea v-model.trim="form.summaryZh" /></label><label>产品简介（英文）<textarea v-model.trim="form.summaryEn" /></label></div>
          <div class="two"><label>产品详情（中文）<textarea v-model.trim="form.detailZh" /></label><label>产品详情（英文）<textarea v-model.trim="form.detailEn" /></label></div>
          <div class="two"><label>应用场景（中文）<textarea v-model.trim="form.applicationScenarioZh" /></label><label>应用场景（英文）<textarea v-model.trim="form.applicationScenarioEn" /></label></div>
          <div class="two"><label>包装说明（中文）<textarea v-model.trim="form.packagingZh" /></label><label>包装说明（英文）<textarea v-model.trim="form.packagingEn" /></label></div>
        </section>

        <section v-show="activeTab === 'media'" class="tab-panel" data-tab="media">
          <ImageUploadField :key="imageFieldKey" v-model="form.imageUrl" label="产品主图" scene="PRODUCT" />
          <div class="two"><label>图片说明（中文）<input v-model.trim="form.imageAltZh" placeholder="用于中文页面的图片说明" /></label><label>图片说明（英文）<input v-model.trim="form.imageAltEn" placeholder="用于英文页面的图片说明" /></label></div>
          <label>SEO Title<input v-model.trim="form.seoTitle" maxlength="255" /></label>
          <label>SEO Description<textarea v-model.trim="form.seoDescription" maxlength="1000" /></label>
        </section>

        <section v-show="activeTab === 'params'" class="tab-panel" data-tab="params">
          <div class="parameters">
            <div class="section-heading">
              <div><h3>产品参数 <span class="required-mark">*</span></h3><p>参数值将按原文保存，不会自动修改精度。</p></div>
              <button type="button" @click="addParameter">新增参数</button>
            </div>
            <article v-for="(item, index) in form.parameters" :key="item.rowKey" class="parameter-card">
              <div class="parameter-title"><strong>参数 {{ index + 1 }}</strong><button v-if="form.parameters.length > 1" type="button" class="danger" @click="removeParameter(index)">移除</button></div>
              <div class="two"><label><span>参数名称（中文） <span class="required-mark">*</span></span><input v-model.trim="item.paramNameZh" required /></label><label><span>参数名称（英文） <span class="required-mark">*</span></span><input v-model.trim="item.paramNameEn" required /></label></div>
              <div class="two"><label><span>参数值 <span class="required-mark">*</span></span><input v-model="item.paramValueRaw" required placeholder="例如：≥ 99.5" /></label><label><span>排序 <span class="required-mark">*</span></span><input v-model.number="item.sortOrder" type="number" required /></label></div>
              <div class="parameter-grid">
                <label>单位<input v-model.trim="item.unit" placeholder="例如：%" /></label>
                <label>测试方法<input v-model.trim="item.testMethod" /></label>
                <label><span>发布状态 <span class="required-mark">*</span></span>
                  <select v-model="item.publishStatus" required>
                    <option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option>
                  </select>
                </label>
              </div>
            </article>
          </div>
        </section>

        <div class="actions"><button class="primary">{{ editingId ? "保存" : "新增产品" }}</button><button v-if="editingId" type="button" @click="cancelEdit">取消</button></div>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </AdminDrawer>
  </AdminShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import AdminDrawer from "../components/AdminDrawer.vue";
import AdminShell from "../components/AdminShell.vue";
import ImageUploadField from "../components/ImageUploadField.vue";
import { adminCreateProduct, adminDeleteProduct, adminListCategories, adminListProducts, adminListSeries, adminUpdateProduct, adminUpdateProductStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { createUrlIdentifier } from "../utils/adminSlug";

type AdminRow = Record<string, unknown>;
type ParameterForm = { rowKey: string; paramKey: string; paramNameZh: string; paramNameEn: string; paramValueRaw: string; unit: string; testMethod: string; sortOrder: number; publishStatus: string };

const route = useRoute();
const categories = ref<AdminRow[]>([]); const seriesList = ref<AdminRow[]>([]); const formSeries = ref<AdminRow[]>([]); const products = ref<AdminRow[]>([]);
const categoryFilter = ref(Number(route.query.categoryId || 0)); const seriesFilter = ref(Number(route.query.seriesId || 0));
const editingId = ref<number | null>(null); const imageFieldKey = ref(0); const error = ref(""); const feedback = useAdminFeedback();
const drawerOpen = ref(false); const activeTab = ref("base");
let parameterSequence = 0;
const parameter = (source?: AdminRow): ParameterForm => ({
  rowKey: `parameter-${++parameterSequence}`,
  paramKey: String(source?.param_key ?? source?.paramKey ?? ""),
  paramNameZh: String(source?.param_name_zh ?? source?.paramNameZh ?? ""),
  paramNameEn: String(source?.param_name_en ?? source?.paramNameEn ?? ""),
  paramValueRaw: String(source?.param_value_raw ?? source?.paramValueRaw ?? ""),
  unit: String(source?.unit ?? ""), testMethod: String(source?.test_method ?? source?.testMethod ?? ""),
  sortOrder: Number(source?.sort_order ?? source?.sortOrder ?? 10), publishStatus: String(source?.publish_status ?? source?.publishStatus ?? "PUBLISHED")
});
const initial = () => ({ categoryId: categoryFilter.value || Number(categories.value[0]?.id || 0), seriesId: seriesFilter.value || 0, slug: "", model: "", nameZh: "", nameEn: "", summaryZh: "", summaryEn: "", detailZh: "", detailEn: "", applicationScenarioZh: "", applicationScenarioEn: "", seoTitle: "", seoDescription: "", packagingZh: "", packagingEn: "", sortOrder: 10, publishStatus: "DRAFT", imageUrl: "", imageAltZh: "", imageAltEn: "", parameters: [parameter()] });
const form = reactive(initial());

const load = async () => { products.value = await adminListProducts(categoryFilter.value || undefined, seriesFilter.value || undefined); };
const loadFormSeries = async () => { formSeries.value = await adminListSeries(form.categoryId); if (!formSeries.value.some(item => Number(item.id) === form.seriesId)) form.seriesId = Number(formSeries.value[0]?.id || 0); };
const categoryChanged = async () => { seriesList.value = await adminListSeries(categoryFilter.value || undefined); if (!seriesList.value.some(item => Number(item.id) === seriesFilter.value)) seriesFilter.value = 0; await load(); };
const reset = async () => { editingId.value = null; Object.assign(form, initial()); imageFieldKey.value += 1; activeTab.value = "base"; await loadFormSeries(); };
const statusLabel = (status: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(status)] || "未知");
const parameterCount = (item: AdminRow) => Array.isArray(item.parameters) ? item.parameters.length : 0;
const addParameter = () => form.parameters.push(parameter());
const removeParameter = (index: number) => form.parameters.splice(index, 1);
const onInvalid = (event: Event) => { const panel = (event.target as HTMLElement).closest("[data-tab]"); if (panel?.dataset.tab) activeTab.value = panel.dataset.tab; };
const startNew = async () => { await reset(); drawerOpen.value = true; feedback.info("已切换到新建产品。"); };
const cancelEdit = async () => { drawerOpen.value = false; await reset(); };
const edit = async (item: AdminRow) => {
  editingId.value = Number(item.id);
  const savedParameters = Array.isArray(item.parameters) ? item.parameters as AdminRow[] : [];
  Object.assign(form, {
    categoryId: Number(item.category_id), seriesId: Number(item.series_id), slug: String(item.slug ?? ""), model: String(item.model ?? ""),
    nameZh: String(item.name_zh ?? ""), nameEn: String(item.name_en ?? item.name_zh ?? ""), summaryZh: String(item.summary_zh ?? ""), summaryEn: String(item.summary_en ?? item.summary_zh ?? ""),
    detailZh: String(item.detail_zh ?? ""), detailEn: String(item.detail_en ?? item.detail_zh ?? ""), applicationScenarioZh: String(item.application_scenario_zh ?? ""), applicationScenarioEn: String(item.application_scenario_en ?? item.application_scenario_zh ?? ""), seoTitle: String(item.seo_title ?? ""), seoDescription: String(item.seo_description ?? ""), packagingZh: String(item.packaging_zh ?? ""), packagingEn: String(item.packaging_en ?? item.packaging_zh ?? ""),
    sortOrder: Number(item.sort_order), publishStatus: String(item.publish_status), imageUrl: String(item.image_url ?? ""), imageAltZh: String(item.image_alt_zh ?? ""), imageAltEn: String(item.image_alt_en ?? item.image_alt_zh ?? ""),
    parameters: savedParameters.length ? savedParameters.map(parameter) : [parameter()]
  });
  imageFieldKey.value += 1; activeTab.value = "base"; await loadFormSeries(); drawerOpen.value = true; feedback.info("已进入产品编辑状态。");
};
const submit = async () => {
  error.value = "";
  try {
    const wasEditing = Boolean(editingId.value); form.nameEn ||= form.nameZh; form.summaryEn ||= form.summaryZh; form.detailEn ||= form.detailZh; form.applicationScenarioEn ||= form.applicationScenarioZh; form.packagingEn ||= form.packagingZh; form.imageAltEn ||= form.imageAltZh;
    const usedKeys = new Set<string>();
    form.parameters.forEach((item, index) => { item.paramNameEn ||= item.paramNameZh; const base = item.paramKey || createUrlIdentifier(item.paramNameEn) || `parameter-${index + 1}`; let key = base; let suffix = 2; while (usedKeys.has(key)) key = `${base}-${suffix++}`; item.paramKey = key; usedKeys.add(key); });
    const payload = { ...form, parameters: form.parameters.map(({ rowKey, ...item }) => item) };
    if (editingId.value) await adminUpdateProduct(editingId.value, payload); else { form.slug = createUrlIdentifier(form.nameEn); if (!form.slug) throw new Error("invalid English name"); payload.slug = form.slug; await adminCreateProduct(payload); }
    drawerOpen.value = false; await reset(); await load(); feedback.success(wasEditing ? "产品保存成功。" : "产品新增成功。");
  } catch { error.value = "保存失败，请确认分类、系列、产品信息和至少一项产品参数填写完整。"; feedback.error(error.value); }
};
const toggle = async (item: AdminRow) => { try { const publishing = item.publish_status !== "PUBLISHED"; await adminUpdateProductStatus(Number(item.id), publishing ? "PUBLISHED" : "OFFLINE"); await load(); feedback.success(publishing ? "产品发布成功。" : "产品下线成功。"); } catch { feedback.error("状态更新失败，请稍后重试。"); } };
const remove = async (item: AdminRow) => { if (!globalThis.confirm(`确定删除产品“${String(item.name_zh)}”吗？`)) return; try { await adminDeleteProduct(Number(item.id)); if (editingId.value === Number(item.id)) { drawerOpen.value = false; await reset(); } await load(); feedback.success("产品删除成功。"); } catch { feedback.error("产品删除失败，请稍后重试。"); } };
onMounted(async () => { categories.value = await adminListCategories(); await categoryChanged(); await loadFormSeries(); });
</script>

<style scoped>
.toolbar,.actions{display:flex;flex-wrap:wrap;gap:8px;align-items:center}.toolbar label{display:flex;gap:8px;align-items:center}.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.card+.card{margin-top:18px}.form{display:grid;gap:11px}.two{display:grid;grid-template-columns:1fr 1fr;gap:12px}.tab-panel{display:grid;gap:11px}.tabs{display:flex;flex-wrap:wrap;gap:6px;border-bottom:1px solid #e2e8f0;padding-bottom:10px}.tabs button{border-color:transparent;background:transparent;color:#64748b;font-weight:700}.tabs button.active{border-color:#2563eb;background:#eff6ff;color:#2563eb}.form label{display:grid;gap:5px;color:#334155;font-size:.88rem;font-weight:600}.required-mark{margin-left:2px;color:#dc2626}input,select,textarea{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;font:inherit}textarea{min-height:68px;resize:vertical}button{border:1px solid #cbd5e1;border-radius:6px;padding:7px 9px;background:#fff;cursor:pointer}.primary{background:#2563eb;border-color:#2563eb;color:#fff}.danger{border-color:#fecaca;color:#b91c1c}.error{color:#b91c1c}.parameters{display:grid;gap:12px}.section-heading,.parameter-title{display:flex;justify-content:space-between;gap:12px;align-items:center}.section-heading h3,.section-heading p{margin:0}.section-heading p{margin-top:4px;color:#64748b;font-size:.78rem}.parameter-card{display:grid;gap:10px;padding:14px;border:1px solid #cbd5e1;border-radius:10px;background:#f8fafc}.parameter-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}table{width:100%;border-collapse:collapse}th,td{border-top:1px solid #e2e8f0;padding:11px 8px;text-align:left;vertical-align:top}th,small{color:#64748b;font-size:.8rem}strong,small{display:block;margin-top:3px}.status{color:#1d4ed8;font-size:.8rem;font-weight:700}@media(max-width:720px){.two,.parameter-grid{grid-template-columns:1fr}}
</style>

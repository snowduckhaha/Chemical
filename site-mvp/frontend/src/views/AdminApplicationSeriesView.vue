<template>
  <AdminShell title="应用领域与产品系列" subtitle="维护应用领域资料，并为每个领域配置前台推荐展示的产品系列及顺序。">
    <div class="layout">
      <section class="card application-list">
        <div class="section-heading"><h2>应用领域</h2><button class="primary compact" @click="startCreate">新增领域</button></div>
        <p class="count">{{ applications.length }} 个领域</p>
        <button
          v-for="item in applications"
          :key="Number(item.id)"
          type="button"
          class="application-item"
          :class="{ active: selectedId === Number(item.id) && !formMode }"
          @click="selectApplication(item)"
        >
          <strong>{{ item.name_zh }}</strong>
          <small>{{ item.name_en }}</small>
          <span>{{ Number(item.linked_series_count || 0) }} 个已关联系列</span>
        </button>
      </section>

      <section class="card relation-editor">
        <form v-if="formMode" class="application-form" @submit.prevent="saveApplication">
          <div class="section-heading"><h2>{{ formMode === "create" ? "新增应用领域" : "编辑应用领域" }}</h2></div>
          <div class="field-grid">
            <label><span>领域名称（中文）<b>*</b></span><input v-model.trim="form.nameZh" required /></label>
            <label><span>领域名称（英文）<b>*</b></span><input v-model.trim="form.nameEn" required /></label>
            <label class="wide"><span>URL 标识</span><input :value="previewSlug" readonly /></label>
            <label><span>排序<b>*</b></span><input v-model.number="form.sortOrder" type="number" required /></label>
            <label><span>发布状态<b>*</b></span><select v-model="form.publishStatus" required><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label>
            <label class="wide"><span>领域简介（中文）</span><textarea v-model.trim="form.overviewZh" rows="4" /></label>
            <label class="wide"><span>领域简介（英文）</span><textarea v-model.trim="form.overviewEn" rows="4" /></label>
          </div>
          <div class="actions"><button class="primary" :disabled="saving">{{ saving ? "保存中…" : "保存领域" }}</button><button type="button" @click="cancelForm">取消</button></div>
        </form>

        <template v-else-if="selectedApplication">
          <div class="section-heading">
            <div><p class="eyebrow">当前领域</p><h2>{{ selectedApplication.name_zh }}</h2><p>{{ selectedApplication.overview_zh }}</p></div>
            <div class="heading-actions"><span class="status">{{ statusLabel(selectedApplication.publish_status) }}</span><button @click="startEdit">编辑领域</button><button class="danger" @click="removeApplication">删除领域</button></div>
          </div>
          <p class="help">勾选后可通过上下箭头调整排序，决定前台“适用产品系列”的展示顺序；已下线系列可保留关联，但不会在前台展示。</p>
          <div v-if="loadingRelations" class="loading">正在载入系列配置…</div>
          <div v-else class="series-options">
            <label v-for="series in orderedSeriesOptions" :key="Number(series.id)" class="series-option" :class="{ selected: selectedSeriesIds.includes(Number(series.id)) }">
              <input v-model="selectedSeriesIds" type="checkbox" :value="Number(series.id)" />
              <span class="series-info"><strong>{{ series.name_zh }}</strong><small>{{ series.category_name_zh }} · {{ series.name_en }}</small></span>
              <span class="series-status">{{ statusLabel(series.publish_status) }}</span>
              <span v-if="selectedSeriesIds.includes(Number(series.id))" class="sort-actions">
                <button type="button" title="上移" aria-label="上移" :disabled="seriesIndex(Number(series.id)) === 0" @click.stop.prevent="moveSeries(Number(series.id), -1)">↑</button>
                <button type="button" title="下移" aria-label="下移" :disabled="seriesIndex(Number(series.id)) === selectedSeriesIds.length - 1" @click.stop.prevent="moveSeries(Number(series.id), 1)">↓</button>
              </span>
            </label>
          </div>
          <div class="actions"><button class="primary" :disabled="saving" @click="saveSeries">{{ saving ? "保存中…" : "保存系列关联" }}</button><span>{{ selectedSeriesIds.length }} 个系列将被关联</span></div>
        </template>
        <p v-else class="empty">请新增或从左侧选择一个应用领域。</p>
      </section>
    </div>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import { adminApplicationSeriesOptions, adminCreateApplication, adminDeleteApplication, adminGetApplicationSeries, adminListApplications, adminUpdateApplication, adminUpdateApplicationSeries } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { createUrlIdentifier } from "../utils/adminSlug";

type ApplicationRow = Record<string, unknown>;
type FormMode = "create" | "edit" | null;

const applications = ref<ApplicationRow[]>([]);
const seriesOptions = ref<ApplicationRow[]>([]);
const selectedId = ref<number | null>(null);
const selectedSeriesIds = ref<number[]>([]);
const loadingRelations = ref(false);
const saving = ref(false);
const formMode = ref<FormMode>(null);
const feedback = useAdminFeedback();
const initial = () => ({ slug: "", nameZh: "", nameEn: "", overviewZh: "", overviewEn: "", sortOrder: 10, publishStatus: "DRAFT" });
const form = reactive(initial());
const selectedApplication = computed(() => applications.value.find((item) => Number(item.id) === selectedId.value));
const previewSlug = computed(() => form.slug || createUrlIdentifier(form.nameEn));
const orderedSeriesOptions = computed(() => {
  const optionsById = new Map(seriesOptions.value.map((series) => [Number(series.id), series]));
  const selected = selectedSeriesIds.value
    .map((id) => optionsById.get(id))
    .filter((series): series is ApplicationRow => Boolean(series));
  const selectedIdSet = new Set(selectedSeriesIds.value);
  return [...selected, ...seriesOptions.value.filter((series) => !selectedIdSet.has(Number(series.id)))];
});

const statusLabel = (status: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(status)] || "未知");
const seriesIndex = (id: number) => selectedSeriesIds.value.indexOf(id);

const selectApplication = async (item: ApplicationRow) => {
  formMode.value = null;
  selectedId.value = Number(item.id);
  loadingRelations.value = true;
  try {
    const relation = await adminGetApplicationSeries(selectedId.value);
    selectedSeriesIds.value = relation.seriesIds.map(Number);
  } catch {
    selectedSeriesIds.value = [];
    feedback.error("领域系列关联载入失败，请稍后重试。");
  } finally {
    loadingRelations.value = false;
  }
};

const loadApplications = async (preferredId = selectedId.value) => {
  applications.value = await adminListApplications();
  const next = applications.value.find((item) => Number(item.id) === preferredId) || applications.value[0];
  if (next) await selectApplication(next);
  else {
    selectedId.value = null;
    selectedSeriesIds.value = [];
  }
};

const startCreate = () => {
  Object.assign(form, initial());
  formMode.value = "create";
  selectedId.value = null;
  selectedSeriesIds.value = [];
};
const startEdit = () => {
  if (!selectedApplication.value) return;
  const item = selectedApplication.value;
  Object.assign(form, { slug: item.slug ?? "", nameZh: item.name_zh ?? "", nameEn: item.name_en ?? "", overviewZh: item.overview_zh ?? "", overviewEn: item.overview_en ?? "", sortOrder: item.sort_order ?? 10, publishStatus: item.publish_status ?? "DRAFT" });
  formMode.value = "edit";
};
const cancelForm = async () => {
  formMode.value = null;
  if (!selectedId.value && applications.value[0]) await selectApplication(applications.value[0]);
};

const saveApplication = async () => {
  try {
    saving.value = true;
    form.nameEn ||= form.nameZh;
    form.overviewEn ||= form.overviewZh;
    form.slug = previewSlug.value;
    if (!form.slug) throw new Error("invalid slug");
    const body = { ...form };
    const applicationId = formMode.value === "edit" && selectedId.value
      ? (await adminUpdateApplication(selectedId.value, body), selectedId.value)
      : (await adminCreateApplication(body)).id;
    formMode.value = null;
    await loadApplications(applicationId);
    feedback.success("应用领域保存成功。");
  } catch {
    feedback.error("保存失败，请检查必填字段或 URL 标识是否重复。");
  } finally {
    saving.value = false;
  }
};

const removeApplication = async () => {
  if (!selectedApplication.value || !confirm(`确定删除“${selectedApplication.value.name_zh}”吗？关联的系列配置也会一并移除。`)) return;
  try {
    await adminDeleteApplication(Number(selectedApplication.value.id));
    selectedId.value = null;
    selectedSeriesIds.value = [];
    await loadApplications();
    feedback.success("应用领域已删除。");
  } catch {
    feedback.error("应用领域删除失败，请稍后重试。");
  }
};

const moveSeries = (id: number, direction: -1 | 1) => {
  const index = selectedSeriesIds.value.indexOf(id);
  const nextIndex = index + direction;
  if (index < 0 || nextIndex < 0 || nextIndex >= selectedSeriesIds.value.length) return;
  const next = [...selectedSeriesIds.value];
  [next[index], next[nextIndex]] = [next[nextIndex], next[index]];
  selectedSeriesIds.value = next;
};

const saveSeries = async () => {
  if (!selectedId.value) return;
  saving.value = true;
  try {
    await adminUpdateApplicationSeries(selectedId.value, selectedSeriesIds.value);
    await loadApplications(selectedId.value);
    feedback.success("领域与产品系列关联已保存并按当前顺序生效。");
  } catch {
    feedback.error("保存失败，请确认关联的产品系列仍存在。");
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  try {
    seriesOptions.value = await adminApplicationSeriesOptions();
    await loadApplications();
  } catch {
    feedback.error("应用领域配置载入失败，请刷新后重试。");
  }
});
</script>

<style scoped>
.layout{display:grid;grid-template-columns:330px minmax(0,1fr);gap:18px;align-items:start}.card{border:1px solid #e2e8f0;border-radius:12px;background:#fff;padding:18px}.section-heading{display:flex;justify-content:space-between;gap:12px;align-items:flex-start;border-bottom:1px solid #e2e8f0;padding-bottom:13px;margin-bottom:12px}h2,p{margin:0}.count{margin:-5px 0 12px;color:#64748b;font-size:.82rem}.section-heading h2{color:#1e293b;font-size:1.15rem}.section-heading p:not(.eyebrow){color:#64748b;font-size:.86rem;line-height:1.6;margin-top:6px;max-width:620px}.eyebrow{color:#2563eb;font-size:.78rem;font-weight:700;margin-bottom:4px}.application-list{display:grid;gap:7px}.application-item{display:grid;grid-template-columns:1fr auto;gap:2px 8px;width:100%;padding:11px;text-align:left;border:1px solid #e2e8f0;border-radius:8px;background:#fff;cursor:pointer}.application-item:hover,.application-item.active{border-color:#2563eb;background:#eff6ff}.application-item strong{color:#1e293b}.application-item small{color:#64748b;font-size:.75rem}.application-item span{grid-column:2;grid-row:1 / span 2;align-self:center;color:#2563eb;font-size:.75rem;white-space:nowrap}.heading-actions,.actions{display:flex;flex-wrap:wrap;gap:8px;align-items:center;justify-content:flex-end}.heading-actions button,.actions button,.sort-actions button{border:1px solid #cbd5e1;border-radius:6px;padding:7px 9px;background:#fff;cursor:pointer;font:inherit}.primary{border-color:#2563eb!important;background:#2563eb!important;color:#fff}.compact{padding:6px 9px!important;font-size:.82rem}.danger{color:#b91c1c;border-color:#fecaca!important}.status,.series-status{padding:3px 6px;border-radius:99px;background:#f1f5f9;color:#64748b;font-size:.78rem}.help{margin:0 0 14px;color:#64748b;font-size:.85rem;line-height:1.65}.series-options{display:grid;gap:8px}.series-option{display:flex;align-items:center;gap:10px;min-height:58px;padding:9px 11px;border:1px solid #e2e8f0;border-radius:8px;cursor:pointer}.series-option.selected{border-color:#93c5fd;background:#f8fbff}.series-option input{width:16px;height:16px;accent-color:#2563eb}.series-info{display:grid;gap:3px;flex:1}.series-info strong{color:#1e293b}.series-info small{color:#64748b;font-size:.78rem}.sort-actions{display:flex;gap:4px}.sort-actions button{width:28px;height:28px;padding:0}.sort-actions button:disabled{cursor:not-allowed;opacity:.4}.actions{justify-content:flex-start;margin-top:16px}.actions span{color:#64748b;font-size:.82rem}.primary:disabled{cursor:wait;opacity:.7}.application-form{display:grid;gap:14px}.field-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.field-grid label{display:grid;gap:5px;color:#334155;font-size:.85rem;font-weight:700}.field-grid .wide{grid-column:1 / -1}.field-grid b{color:#dc2626;margin-left:3px}input,select,textarea{min-width:0;border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;background:#fff;font:inherit}.loading,.empty{padding:28px 0;color:#64748b;text-align:center}@media(max-width:850px){.layout{grid-template-columns:1fr}.application-list{max-height:310px;overflow:auto}.field-grid{grid-template-columns:1fr}}
</style>

<template>
  <AdminShell title="应用领域与产品系列" subtitle="领域分类来自产品资料，可在此配置每个领域推荐展示的产品系列及顺序。">
    <div class="layout">
      <section class="card application-list">
        <div class="section-heading"><h2>领域分类</h2><span>{{ applications.length }} 个</span></div>
        <button
          v-for="item in applications"
          :key="Number(item.id)"
          type="button"
          class="application-item"
          :class="{ active: selectedId === Number(item.id) }"
          @click="selectApplication(item)"
        >
          <strong>{{ item.name_zh }}</strong>
          <small>{{ item.name_en }}</small>
          <span>{{ Number(item.linked_series_count || 0) }} 个已关联系列</span>
        </button>
      </section>

      <section class="card relation-editor">
        <template v-if="selectedApplication">
          <div class="section-heading">
            <div><p class="eyebrow">当前领域</p><h2>{{ selectedApplication.name_zh }}</h2><p>{{ selectedApplication.overview_zh }}</p></div>
            <span class="status">{{ statusLabel(selectedApplication.publish_status) }}</span>
          </div>
          <p class="help">勾选后拖动排序可决定前台“适用产品系列”的展示顺序；已下线系列可保留关联，但不会在前台展示。</p>
          <div v-if="loadingRelations" class="loading">正在载入系列配置…</div>
          <div v-else class="series-options">
            <label v-for="series in seriesOptions" :key="Number(series.id)" class="series-option" :class="{ selected: selectedSeriesIds.includes(Number(series.id)) }">
              <input v-model="selectedSeriesIds" type="checkbox" :value="Number(series.id)" />
              <span class="series-info"><strong>{{ series.name_zh }}</strong><small>{{ series.category_name_zh }} · {{ series.name_en }}</small></span>
              <span class="series-status">{{ statusLabel(series.publish_status) }}</span>
              <span v-if="selectedSeriesIds.includes(Number(series.id))" class="sort-actions">
                <button type="button" title="上移" aria-label="上移" @click.prevent="moveSeries(Number(series.id), -1)">↑</button>
                <button type="button" title="下移" aria-label="下移" @click.prevent="moveSeries(Number(series.id), 1)">↓</button>
              </span>
            </label>
          </div>
          <div class="actions"><button class="primary" :disabled="saving" @click="save">{{ saving ? "保存中…" : "保存系列关联" }}</button><span>{{ selectedSeriesIds.length }} 个系列将被关联</span></div>
        </template>
        <p v-else class="empty">请从左侧选择一个应用领域。</p>
      </section>
    </div>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import { adminApplicationSeriesOptions, adminGetApplicationSeries, adminListApplications, adminUpdateApplicationSeries } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";

const applications = ref<Array<Record<string, unknown>>>([]);
const seriesOptions = ref<Array<Record<string, unknown>>>([]);
const selectedId = ref<number | null>(null);
const selectedSeriesIds = ref<number[]>([]);
const loadingRelations = ref(false);
const saving = ref(false);
const feedback = useAdminFeedback();
const selectedApplication = computed(() => applications.value.find((item) => Number(item.id) === selectedId.value));

const statusLabel = (status: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(status)] || "未知");

const selectApplication = async (item: Record<string, unknown>) => {
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

const moveSeries = (id: number, direction: -1 | 1) => {
  const index = selectedSeriesIds.value.indexOf(id);
  const nextIndex = index + direction;
  if (index < 0 || nextIndex < 0 || nextIndex >= selectedSeriesIds.value.length) return;
  const next = [...selectedSeriesIds.value];
  [next[index], next[nextIndex]] = [next[nextIndex], next[index]];
  selectedSeriesIds.value = next;
};

const save = async () => {
  if (!selectedId.value) return;
  saving.value = true;
  try {
    await adminUpdateApplicationSeries(selectedId.value, selectedSeriesIds.value);
    applications.value = await adminListApplications();
    feedback.success("领域与产品系列关联已保存并按当前顺序生效。");
  } catch {
    feedback.error("保存失败，请确认关联的产品系列仍存在。");
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  try {
    [applications.value, seriesOptions.value] = await Promise.all([adminListApplications(), adminApplicationSeriesOptions()]);
    if (applications.value[0]) await selectApplication(applications.value[0]);
  } catch {
    feedback.error("应用领域配置载入失败，请刷新后重试。");
  }
});
</script>

<style scoped>
.layout { display:grid; grid-template-columns:330px minmax(0, 1fr); gap:18px; align-items:start; }
.card { border:1px solid #e2e8f0; border-radius:12px; background:#fff; padding:18px; }
.section-heading { display:flex; justify-content:space-between; gap:12px; align-items:flex-start; border-bottom:1px solid #e2e8f0; padding-bottom:13px; margin-bottom:12px; }
h2,p { margin:0; }.section-heading > span { color:#64748b; font-size:.82rem; }.eyebrow { color:#2563eb; font-size:.78rem; font-weight:700; margin-bottom:4px; }.section-heading h2 { color:#1e293b; font-size:1.15rem; }.section-heading p:not(.eyebrow) { color:#64748b; font-size:.86rem; line-height:1.6; margin-top:6px; max-width:640px; }.application-list { display:grid; gap:7px; }.application-item { display:grid; grid-template-columns:1fr auto; gap:2px 8px; width:100%; padding:11px; text-align:left; border:1px solid #e2e8f0; border-radius:8px; background:#fff; cursor:pointer; }.application-item:hover,.application-item.active { border-color:#2563eb; background:#eff6ff; }.application-item strong { color:#1e293b; }.application-item small { color:#64748b; font-size:.75rem; }.application-item span { grid-column:2; grid-row:1 / span 2; align-self:center; color:#2563eb; font-size:.75rem; white-space:nowrap; }.help { margin:0 0 14px; color:#64748b; font-size:.85rem; line-height:1.65; }.series-options { display:grid; gap:8px; }.series-option { display:flex; align-items:center; gap:10px; min-height:58px; padding:9px 11px; border:1px solid #e2e8f0; border-radius:8px; cursor:pointer; }.series-option.selected { border-color:#93c5fd; background:#f8fbff; }.series-option input { width:16px; height:16px; accent-color:#2563eb; }.series-info { display:grid; gap:3px; flex:1; }.series-info strong { color:#1e293b; }.series-info small,.series-status { color:#64748b; font-size:.78rem; }.series-status { padding:3px 6px; border-radius:99px; background:#f1f5f9; }.sort-actions { display:flex; gap:4px; }.sort-actions button { width:28px; height:28px; border:1px solid #cbd5e1; border-radius:5px; background:#fff; cursor:pointer; }.actions { display:flex; flex-wrap:wrap; align-items:center; gap:10px; margin-top:16px; }.actions span { color:#64748b; font-size:.82rem; }.primary { border:1px solid #2563eb; border-radius:6px; padding:8px 12px; color:#fff; background:#2563eb; cursor:pointer; }.primary:disabled { cursor:wait; opacity:.7; }.status { color:#1d4ed8; font-size:.8rem; font-weight:700; }.loading,.empty { padding:28px 0; color:#64748b; text-align:center; } @media(max-width:850px){.layout{grid-template-columns:1fr;}.application-list{max-height:310px;overflow:auto;}}
</style>

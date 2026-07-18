<template>
  <AdminShell title="数据概览" subtitle="查看访问到询盘的转化表现、来源贡献及高意向待跟进线索。">
    <section class="card filters">
      <label>开始日期<input v-model="filters.from" type="date" /></label><label>结束日期<input v-model="filters.to" type="date" /></label>
      <label>语言<select v-model="filters.lang"><option value="">全部</option><option value="zh">中文</option><option value="en">英文</option></select></label>
      <label>来源<select v-model="filters.channel"><option value="">全部</option><option v-for="item in channels" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
      <div class="filter-actions"><button class="primary" :disabled="loading" @click="load">{{ loading ? "加载中…" : "查询" }}</button><button :disabled="rebuilding" @click="rebuild">{{ rebuilding ? "重算中…" : "重算聚合" }}</button></div>
    </section>
    <p v-if="error" class="error">{{ error }}</p>

    <section class="metrics">
      <article v-for="metric in metrics" :key="metric.label" class="card metric"><span>{{ metric.label }}</span><strong>{{ metric.value }}</strong><small>{{ metric.help }}</small></article>
    </section>

    <div class="two-columns">
      <section class="card"><div class="heading"><h2>转化漏斗</h2><span>同一筛选周期</span></div>
        <div class="funnel"><article v-for="(step, index) in funnel" :key="String(step.eventName)"><div class="bar" :style="{ width: funnelWidth(step) }"><span>{{ funnelLabel(step.eventName) }}</span><strong>{{ step.count }}</strong></div><small>{{ index ? `上一步转化 ${formatPercent(step.conversionRate)}` : "漏斗起点" }}</small></article></div>
      </section>
      <section class="card"><div class="heading"><h2>来源归因</h2><select v-model="dimension" @change="loadAttribution"><option value="channel">来源渠道</option><option value="page">来源页面</option><option value="category">产品分类</option><option value="series">产品系列</option><option value="product">产品</option></select></div>
        <table><thead><tr><th>维度</th><th>访问</th><th>询盘</th><th>转化率</th></tr></thead><tbody><tr v-for="row in attribution" :key="String(row.dimension_key)"><td>{{ dimensionLabel(row.dimension_key) }}</td><td>{{ row.visits }}</td><td>{{ row.inquiries }}</td><td>{{ rate(row) }}</td></tr><tr v-if="!attribution.length"><td colspan="4">暂无归因数据</td></tr></tbody></table>
      </section>
    </div>

    <section class="card"><div class="heading"><h2>高意向待跟进</h2><span>评分 ≥ 40，实时数据</span></div>
      <table><thead><tr><th>询盘</th><th>客户/公司</th><th>感兴趣产品</th><th>评分</th><th>状态</th><th>提交时间</th><th></th></tr></thead><tbody><tr v-for="row in highIntent" :key="Number(row.id)" :class="{ overdue: Boolean(row.overdue) }"><td>{{ row.inquiry_no }}</td><td>{{ row.name }}<small>{{ row.company || row.email }}</small></td><td>{{ row.interested_product || "未指定" }}</td><td><b>{{ row.lead_score }}</b><em v-if="row.overdue">超 24 小时</em></td><td>{{ statusLabel(row.inquiry_status) }}</td><td>{{ formatDate(row.created_at) }}</td><td><RouterLink :to="`/${lang}/admin/inquiries?selected=${row.id}`">跟进</RouterLink></td></tr><tr v-if="!highIntent.length"><td colspan="7">当前没有高意向待跟进询盘</td></tr></tbody></table>
    </section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import AdminShell from "../components/AdminShell.vue";
import { adminAnalyticsAttribution, adminAnalyticsFunnel, adminAnalyticsHighIntent, adminAnalyticsOverview, adminAnalyticsRebuild, type AnalyticsOverview } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";

const route = useRoute(); const lang = computed(() => String(route.params.lang || "zh"));
const iso = (date: Date) => date.toISOString().slice(0, 10); const start = new Date(); start.setDate(start.getDate() - 29);
const filters = reactive({ from: iso(start), to: iso(new Date()), lang: "", channel: "" });
const overview = ref<AnalyticsOverview>({ visits: 0, inquiries: 0, validInquiries: 0, conversionRate: 0, averageFirstContactMinutes: 0 });
const funnel = ref<Array<Record<string, unknown>>>([]); const attribution = ref<Array<Record<string, unknown>>>([]); const highIntent = ref<Array<Record<string, unknown>>>([]);
const dimension = ref("channel"); const loading = ref(false); const rebuilding = ref(false); const error = ref(""); const feedback = useAdminFeedback();
const channels = [{ value: "ORGANIC_SEARCH", label: "自然搜索" }, { value: "DIRECT", label: "直接访问" }, { value: "REFERRAL", label: "外部推荐" }, { value: "ADVERTISING", label: "广告" }, { value: "SOCIAL", label: "社交媒体" }, { value: "EMAIL", label: "邮件" }, { value: "UNKNOWN", label: "未知" }];
const metrics = computed(() => [{ label: "访问量", value: overview.value.visits.toLocaleString(), help: "去重规则内页面浏览" }, { label: "询盘提交量", value: overview.value.inquiries.toLocaleString(), help: "业务库成功提交" }, { label: "有效询盘", value: overview.value.validInquiries.toLocaleString(), help: "有效邮箱且含关键线索" }, { label: "询盘转化率", value: formatPercent(overview.value.conversionRate), help: "询盘数 / 访问量" }, { label: "平均首次跟进", value: `${Math.round(overview.value.averageFirstContactMinutes)} 分钟`, help: "已首次联系询盘" }]);
const loadAttribution = async () => { attribution.value = await adminAnalyticsAttribution({ ...filters }, dimension.value); };
const load = async () => { loading.value = true; error.value = ""; try { const [summary, steps, leads] = await Promise.all([adminAnalyticsOverview({ ...filters }), adminAnalyticsFunnel({ ...filters }), adminAnalyticsHighIntent({ from: filters.from, to: filters.to, lang: filters.lang })]); overview.value = summary; funnel.value = steps; highIntent.value = leads; await loadAttribution(); } catch { error.value = "数据加载失败，请检查日期范围或稍后重试。"; } finally { loading.value = false; } };
const rebuild = async () => { rebuilding.value = true; try { const result = await adminAnalyticsRebuild(filters.from, filters.to); feedback.success(`已完成 ${result.rebuiltDays} 天的数据聚合。`); await load(); } catch { feedback.error("聚合重算失败，请检查日期范围。"); } finally { rebuilding.value = false; } };
const maxFunnel = computed(() => Math.max(1, ...funnel.value.map(row => Number(row.count)))); const funnelWidth = (row: Record<string, unknown>) => `${Math.max(28, Number(row.count) / maxFunnel.value * 100)}%`;
const funnelLabel = (value: unknown) => ({ product_view: "访问产品页", inquiry_cta_click: "点击询盘 CTA", inquiry_form_open: "打开询盘表单", inquiry_submit_success: "成功提交询盘" }[String(value)] || String(value));
const formatPercent = (value: unknown) => `${Number(value || 0).toFixed(1)}%`; const rate = (row: Record<string, unknown>) => Number(row.visits) ? formatPercent(Number(row.inquiries) * 100 / Number(row.visits)) : "0.0%";
const dimensionLabel = (value: unknown) => channels.find(item => item.value === value)?.label || String(value || "未知");
const statusLabel = (value: unknown) => ({ NEW: "新建", CONTACTED: "已联系", FOLLOWING_UP: "跟进中", CLOSED: "已关闭" }[String(value)] || String(value));
const formatDate = (value: unknown) => value ? new Date(String(value)).toLocaleString("zh-CN", { hour12: false }) : "-";
onMounted(load);
</script>

<style scoped>
.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.filters{display:grid;grid-template-columns:repeat(4,minmax(130px,1fr)) auto;gap:12px;align-items:end}.filters label{display:grid;gap:5px;color:#475569;font-size:.8rem;font-weight:700}.filter-actions{display:flex;gap:7px}input,select,button{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;background:#fff;font:inherit}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.metrics{display:grid;grid-template-columns:repeat(5,1fr);gap:12px}.metric{display:grid;gap:8px}.metric span,.heading span{color:#64748b;font-size:.78rem}.metric strong{color:#0f172a;font-size:1.65rem}.metric small,td small{display:block;color:#94a3b8;font-size:.72rem}.two-columns{display:grid;grid-template-columns:1fr 1fr;gap:16px}.heading{display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:14px}.heading h2{margin:0;color:#0f172a;font-size:1.05rem}.funnel{display:grid;gap:12px}.funnel article{display:grid;gap:4px}.bar{box-sizing:border-box;display:flex;justify-content:space-between;min-width:180px;border-radius:6px;padding:10px 12px;background:linear-gradient(90deg,#dbeafe,#bfdbfe);color:#1e3a8a}.funnel small{color:#64748b}table{width:100%;border-collapse:collapse;font-size:.82rem}th,td{border-bottom:1px solid #e2e8f0;padding:9px 7px;text-align:left}th{color:#64748b;font-size:.72rem}td a{color:#2563eb;font-weight:700}.overdue{background:#fff7ed}.overdue em{display:block;color:#c2410c;font-size:.68rem;font-style:normal}.error{color:#b91c1c}@media(max-width:980px){.metrics{grid-template-columns:repeat(2,1fr)}.two-columns{grid-template-columns:1fr}.filters{grid-template-columns:repeat(2,1fr)}}@media(max-width:560px){.filters,.metrics{grid-template-columns:1fr}.card{overflow:auto}}
</style>
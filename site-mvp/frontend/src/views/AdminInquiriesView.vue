<template>
  <AdminShell title="询盘管理" subtitle="查看客户询盘、记录跟进状态并维护内部备注。">
    <section class="card filters">
      <label>关键词<input v-model.trim="filters.keyword" placeholder="编号、姓名、公司、邮箱、产品" @keyup.enter="load" /></label>
      <label>状态<select v-model="filters.status"><option value="">全部状态</option><option value="NEW">新建</option><option value="CONTACTED">已联系</option><option value="FOLLOWING_UP">已跟进</option><option value="CLOSED">已关闭</option></select></label>
      <label>开始日期<input v-model="filters.from" type="date" /></label>
      <label>结束日期<input v-model="filters.to" type="date" /></label>
      <button class="primary" @click="load">查询</button><button @click="resetFilters">重置</button><button v-if="auth.isAdmin" :disabled="exporting" @click="exportCsv">{{ exporting ? "导出中…" : "导出询盘 CSV" }}</button>
    </section>

    <div class="summary">
      <div><strong>{{ inquiries.length }}</strong><span>当前结果</span></div>
      <div><strong>{{ newCount }}</strong><span>新建询盘</span></div>
      <div><strong>{{ highIntentCount }}</strong><span>高意向</span></div>
      <div><strong>{{ overdueCount }}</strong><span>超时待跟进</span></div>
    </div>

    <div class="layout">
      <section class="card list-card">
        <div class="section-title"><h2>询盘列表</h2><span>最多显示 500 条</span></div>
        <p v-if="loading" class="muted">正在加载询盘…</p>
        <p v-else-if="!inquiries.length" class="empty">没有符合条件的询盘。</p>
        <div v-else class="table-wrap"><table><thead><tr><th>客户</th><th>感兴趣产品</th><th>意向</th><th>状态</th><th>提交时间</th><th>操作</th></tr></thead><tbody>
          <tr v-for="item in inquiries" :key="Number(item.id)" :class="{ selected: Number(selected?.id) === Number(item.id) }">
            <td><strong>{{ item.name }}</strong><small>{{ item.company || "未填写公司" }}</small><small>{{ item.email }}</small></td>
            <td>{{ item.interested_product || "未指定" }}</td>
            <td><span class="score" :class="{ high: Number(item.lead_score) >= 40 }">{{ item.lead_score }} 分</span><span v-if="item.overdue_high_intent" class="overdue">超过 24 小时未跟进</span></td>
            <td><span class="status" :class="String(item.inquiry_status).toLowerCase()">{{ statusLabel(item.inquiry_status) }}</span></td>
            <td>{{ formatDate(item.created_at) }}</td>
            <td><button @click="openDetail(item)">查看详情</button></td>
          </tr>
        </tbody></table></div>
      </section>

      <aside v-if="selected" class="card detail">
        <div class="section-title"><div><small>{{ selected.inquiry_no }}</small><h2>{{ selected.name }}</h2></div><button aria-label="关闭详情" @click="selected = null">×</button></div>
        <dl><div><dt>公司</dt><dd>{{ selected.company || "未填写" }}</dd></div><div><dt>邮箱</dt><dd><a :href="`mailto:${selected.email}`">{{ selected.email }}</a></dd></div><div><dt>电话 / WhatsApp</dt><dd>{{ selected.phone || "未填写" }}</dd></div><div><dt>国家或地区</dt><dd>{{ selected.country || "未填写" }}</dd></div><div><dt>感兴趣产品</dt><dd>{{ selected.interested_product || "未指定" }}</dd></div><div><dt>语言</dt><dd>{{ selected.lang === "en" ? "英文" : "中文" }}</dd></div><div><dt>来源页面</dt><dd class="break">{{ selected.source_page || "未知" }}</dd></div><div><dt>提交时间</dt><dd>{{ formatDate(selected.created_at) }}</dd></div><div><dt>首次联系</dt><dd>{{ selected.first_contacted_at ? formatDate(selected.first_contacted_at) : "尚未联系" }}</dd></div></dl>
        <section><h3>客户留言</h3><p class="message">{{ selected.message }}</p></section>
        <section><h3>跟进状态</h3><select v-model="editingStatus"><option value="NEW">新建</option><option value="CONTACTED">已联系</option><option value="FOLLOWING_UP">已跟进</option><option value="CLOSED">已关闭</option></select><button class="primary block" @click="saveStatus">保存状态</button></section>
        <section><h3>内部备注</h3><textarea v-model="internalNote" maxlength="10000" placeholder="仅后台人员可见，可记录沟通结果和下一步计划。" /><button class="primary block" @click="saveNote">保存备注</button></section>
        <section v-if="history.length"><h3>状态记录</h3><ol class="history"><li v-for="(item, index) in history" :key="index"><span>{{ statusLabel(item.from_status) }} → {{ statusLabel(item.to_status) }}</span><small>{{ item.operator_username }} · {{ formatDate(item.created_at) }}</small></li></ol></section>
      </aside>
    </div>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import { adminExportInquiries, adminGetInquiry, adminListInquiries, adminUpdateInquiryNote, adminUpdateInquiryStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { useAdminAuthStore } from "../stores/adminAuth";

type Row = Record<string, unknown>;
const feedback = useAdminFeedback(); const auth = useAdminAuthStore(); const inquiries = ref<Row[]>([]); const selected = ref<Row | null>(null); const loading = ref(false); const exporting = ref(false);
const editingStatus = ref("NEW"); const internalNote = ref(""); const filters = reactive({ keyword: "", status: "", from: "", to: "" });
const history = computed(() => Array.isArray(selected.value?.status_history) ? selected.value.status_history as Row[] : []);
const newCount = computed(() => inquiries.value.filter(item => item.inquiry_status === "NEW").length);
const highIntentCount = computed(() => inquiries.value.filter(item => Number(item.lead_score) >= 40).length);
const overdueCount = computed(() => inquiries.value.filter(item => Boolean(item.overdue_high_intent)).length);
const statusLabel = (status: unknown) => ({ NEW: "新建", CONTACTED: "已联系", FOLLOWING_UP: "已跟进", CLOSED: "已关闭" }[String(status)] || "无");
const formatDate = (value: unknown) => value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(String(value))) : "—";
const load = async () => { loading.value = true; try { inquiries.value = await adminListInquiries({ ...filters }); } catch { feedback.error("询盘加载失败，请稍后重试。"); } finally { loading.value = false; } };
const resetFilters = async () => { Object.assign(filters, { keyword: "", status: "", from: "", to: "" }); await load(); };
const exportCsv = async () => { exporting.value = true; try { const blob = await adminExportInquiries({ ...filters }); const url = URL.createObjectURL(blob); const link = document.createElement("a"); link.href = url; link.download = `inquiries-${new Date().toISOString().slice(0, 10)}.csv`; link.click(); URL.revokeObjectURL(url); feedback.success("询盘导出成功。"); } catch { feedback.error("询盘导出失败，仅管理员可以执行该操作。"); } finally { exporting.value = false; } };
const openDetail = async (item: Row) => { try { const detail = await adminGetInquiry(Number(item.id)); selected.value = { ...item, ...detail }; editingStatus.value = String(detail.inquiry_status); internalNote.value = String(detail.internal_note || ""); } catch { feedback.error("询盘详情加载失败。"); } };
const refreshSelected = async () => { if (!selected.value) return; const listItem = inquiries.value.find(item => Number(item.id) === Number(selected.value?.id)); const detail = await adminGetInquiry(Number(selected.value.id)); selected.value = { ...(listItem || {}), ...detail }; editingStatus.value = String(detail.inquiry_status); internalNote.value = String(detail.internal_note || ""); };
const saveStatus = async () => { if (!selected.value) return; try { await adminUpdateInquiryStatus(Number(selected.value.id), editingStatus.value); await load(); await refreshSelected(); feedback.success("询盘状态保存成功。"); } catch { feedback.error("询盘状态保存失败。"); } };
const saveNote = async () => { if (!selected.value) return; try { await adminUpdateInquiryNote(Number(selected.value.id), internalNote.value); await refreshSelected(); feedback.success("内部备注保存成功。"); } catch { feedback.error("内部备注保存失败。"); } };
onMounted(load);
</script>

<style scoped>
.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.filters{display:flex;flex-wrap:wrap;gap:12px;align-items:end}.filters label{display:grid;gap:5px;color:#475569;font-size:.82rem;font-weight:600}.filters input,.filters select{min-width:150px}.summary{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.summary div{display:grid;gap:3px;border:1px solid #e2e8f0;border-radius:10px;padding:14px;background:#fff}.summary strong{color:#0f172a;font-size:1.5rem}.summary span{color:#64748b;font-size:.8rem}.layout{display:grid;grid-template-columns:minmax(0,1fr) 390px;gap:18px;align-items:start}.section-title{display:flex;justify-content:space-between;align-items:center;gap:12px}.section-title h2,.section-title small{margin:0}.section-title>span{color:#64748b;font-size:.78rem}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse}th,td{border-top:1px solid #e2e8f0;padding:11px 8px;text-align:left;vertical-align:top}th{color:#64748b;font-size:.78rem;white-space:nowrap}td small{display:block;margin-top:3px;color:#64748b}tr.selected{background:#eff6ff}.score{display:inline-block;border-radius:999px;padding:3px 7px;background:#f1f5f9;color:#475569;font-size:.76rem;font-weight:700}.score.high{background:#fff7ed;color:#c2410c}.overdue{display:block;margin-top:5px;color:#dc2626;font-size:.72rem;font-weight:700}.status{display:inline-block;border-radius:999px;padding:3px 7px;background:#e0f2fe;color:#075985;font-size:.76rem;font-weight:700}.status.closed{background:#f1f5f9;color:#475569}.status.following_up{background:#fef3c7;color:#92400e}.detail{position:sticky;top:16px;display:grid;gap:17px;max-height:calc(100vh - 32px);overflow:auto}.detail section{display:grid;gap:8px;border-top:1px solid #e2e8f0;padding-top:14px}.detail h3{margin:0;color:#334155;font-size:.92rem}.detail textarea{min-height:110px;resize:vertical}dl{display:grid;gap:9px;margin:0}dl div{display:grid;grid-template-columns:105px 1fr;gap:10px}dt{color:#64748b;font-size:.8rem}dd{margin:0;color:#1e293b}.break{overflow-wrap:anywhere}.message{margin:0;white-space:pre-wrap;color:#334155;line-height:1.65}.history{display:grid;gap:9px;margin:0;padding-left:20px}.history li span,.history li small{display:block}.history li small{margin-top:2px;color:#64748b}input,select,textarea{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;font:inherit}button{border:1px solid #cbd5e1;border-radius:7px;padding:8px 11px;background:#fff;cursor:pointer}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.block{width:100%}.empty,.muted{color:#64748b;text-align:center;padding:30px}@media(max-width:1050px){.layout{grid-template-columns:1fr}.detail{position:static;max-height:none}}@media(max-width:700px){.summary{grid-template-columns:1fr 1fr}}
</style>

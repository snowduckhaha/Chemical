<template>
  <AdminShell title="发布最新代码" subtitle="仅发布 origin/main；生产服务器将拉取代码、重新构建并部署。">
    <section v-if="authorized">
      <form class="card deploy-form" @submit.prevent="submit">
        <div class="deploy-copy">
          <h2>生产环境日常发布</h2>
          <p class="notice">将发布固定代码来源 <code>origin/main</code>。服务器会在私有网络直接读取已发布的产品、应用领域和资讯内容，生成 SSG 静态页，不经过 CDN。</p>
          <p class="warning">发布期间会重建后端和前端服务；失败时将自动恢复上一版容器。</p>
        </div>
        <div class="deploy-action">
          <label class="confirm"><input v-model="confirmed" type="checkbox" /> 我已确认发布生产环境最新代码</label>
          <button class="primary" :disabled="submitting || !confirmed">{{ submitting ? "正在创建发布任务…" : "发布最新代码" }}</button>
        </div>
      </form>
      <section class="card job-list">
        <div class="heading"><h2>最近发布任务</h2><button type="button" @click="load">刷新</button></div>
        <table><thead><tr><th>ID</th><th>状态</th><th>提交</th><th>时间</th><th>结果</th></tr></thead><tbody>
          <tr v-for="job in jobs" :key="Number(job.id)"><td>#{{ job.id }}</td><td><span :class="['status', String(job.status).toLowerCase()]">{{ statusLabel(job.status) }}</span></td><td><code>{{ job.resolved_commit || job.source_ref }}</code></td><td>{{ formatDate(job.finished_at || job.started_at || job.requested_at) }}</td><td><details v-if="job.log_excerpt || job.error_summary"><summary>{{ job.error_summary || "查看日志" }}</summary><pre>{{ job.log_excerpt }}</pre></details><span v-else>—</span></td></tr>
          <tr v-if="!jobs.length"><td colspan="5" class="empty">暂无发布任务</td></tr>
        </tbody></table>
      </section>
    </section>
    <section v-else class="card denied">此功能仅允许 zelin 管理员账号使用。</section>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import { adminCreateDeployment, adminListDeployments } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { useAdminAuthStore } from "../stores/adminAuth";

const auth = useAdminAuthStore();
const feedback = useAdminFeedback();
const authorized = computed(() => auth.isAdmin && auth.session?.username === "zelin");
const confirmed = ref(false);
const submitting = ref(false);
const jobs = ref<Array<Record<string, unknown>>>([]);
let timer: ReturnType<typeof setInterval> | undefined;
const load = async () => { if (!authorized.value) return; try { jobs.value = await adminListDeployments(); } catch { feedback.error("发布任务加载失败。"); } };
const submit = async () => {
  if (!confirmed.value || submitting.value) return;
  try { submitting.value = true; await adminCreateDeployment(); confirmed.value = false; await load(); feedback.success("生产发布任务已创建，等待服务器部署代理处理。"); }
  catch { feedback.error("创建发布任务失败，请确认 zelin 账号为 ADMIN 并且部署代理正在运行。"); }
  finally { submitting.value = false; }
};
const statusLabel = (value: unknown) => ({ QUEUED: "等待中", RUNNING: "执行中", SUCCESS: "成功", FAILED: "失败" }[String(value)] || "未知");
const formatDate = (value: unknown) => value ? new Date(String(value)).toLocaleString("zh-CN", { hour12: false }) : "—";
onMounted(async () => { await load(); timer = setInterval(() => { void load(); }, 5000); });
onBeforeUnmount(() => { if (timer) clearInterval(timer); });
</script>

<style scoped>
.card{padding:18px;border:1px solid #e2e8f0;border-radius:12px;background:#fff}.deploy-form{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:20px;align-items:center;margin-bottom:18px}.deploy-copy{display:grid;gap:8px}.deploy-action{display:grid;gap:10px;justify-items:start}.deploy-form h2,.heading h2{margin:0;color:#1e293b;font-size:1.1rem}.confirm{display:grid;grid-template-columns:auto 1fr;gap:7px;align-items:center;color:#334155;font-weight:700;font-size:.88rem}.notice,.warning{margin:0;font-size:.86rem;line-height:1.65}.notice{color:#64748b}.warning{padding:9px 10px;border-radius:7px;background:#fff7ed;color:#9a3412}.notice code,td code{color:#1d4ed8}.primary,button{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;background:#fff;font:inherit;cursor:pointer}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.primary:disabled{cursor:not-allowed;opacity:.6}.heading{display:flex;justify-content:space-between;gap:12px;align-items:center;margin-bottom:12px}table{width:100%;border-collapse:collapse;font-size:.85rem}th,td{padding:10px 7px;border-bottom:1px solid #e2e8f0;text-align:left;vertical-align:top}th{color:#64748b;font-size:.76rem}pre{max-width:360px;max-height:180px;overflow:auto;white-space:pre-wrap;word-break:break-word}summary{cursor:pointer;color:#2563eb}.status{display:inline-block;padding:3px 6px;border-radius:99px;background:#f1f5f9;color:#475569}.status.running{background:#eff6ff;color:#1d4ed8}.status.success{background:#ecfdf5;color:#047857}.status.failed{background:#fef2f2;color:#b91c1c}.empty,.denied{color:#64748b;text-align:center}@media(max-width:900px){.deploy-form{grid-template-columns:1fr}}
</style>

<template>
  <AdminShell title="荣誉证书管理" subtitle="维护关于我们页面展示的证书名称、图片、排序及发布状态。">
    <div class="layout">
      <form class="card form" @submit.prevent="submit">
        <h2>{{ editingId ? "编辑证书" : "新增证书" }}</h2>
        <label><span>证书编号 <span class="required-mark">*</span></span><input v-model.trim="form.certificateNo" required placeholder="例如：CERT-ISO9001-2024" /></label>
        <label><span>名称（中文） <span class="required-mark">*</span></span><input v-model.trim="form.nameZh" required /></label>
        <label><span>名称（英文） <span class="required-mark">*</span></span><input v-model.trim="form.nameEn" required /></label>
        <ImageUploadField :key="imageFieldKey" v-model="form.imageUrl" label="证书图片 *" scene="CERTIFICATE" />
        <label>图片说明（中文）<input v-model.trim="form.altZh" placeholder="描述证书类型与内容" /></label>
        <label>图片说明（英文）<input v-model.trim="form.altEn" placeholder="Certificate image description" /></label>
        <label><span>排序 <span class="required-mark">*</span></span><input v-model.number="form.sortOrder" type="number" required /></label>
        <label><span>发布状态 <span class="required-mark">*</span></span><select v-model="form.publishStatus" required><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label>
        <div class="actions"><button class="primary">{{ editingId ? "保存" : "新增证书" }}</button><button v-if="editingId" type="button" @click="cancelEdit">取消</button></div>
        <p v-if="error" class="error">{{ error }}</p>
      </form>

      <section class="card">
        <div class="heading"><h2>证书列表</h2><button @click="startNew">新建证书</button></div>
        <p v-if="!certificates.length" class="empty">暂无证书。</p>
        <div v-else class="certificate-grid">
          <article v-for="item in certificates" :key="Number(item.id)" class="certificate-card">
            <button class="preview" type="button" @click="preview = item"><img :src="String(item.image_url)" :alt="String(item.alt_zh || item.name_zh)" /></button>
            <div class="content"><small>{{ item.certificate_no }}</small><strong>{{ item.name_zh }}</strong><span>{{ statusLabel(item.publish_status) }} · 排序 {{ item.sort_order }}</span></div>
            <div class="actions"><button @click="edit(item)">编辑</button><button @click="toggle(item)">{{ item.publish_status === "PUBLISHED" ? "下线" : "发布" }}</button><button class="danger" @click="remove(item)">删除</button></div>
          </article>
        </div>
      </section>
    </div>

    <Teleport to="body"><div v-if="preview" class="lightbox" role="dialog" aria-modal="true" @click.self="preview = null"><button class="close" aria-label="关闭预览" @click="preview = null">×</button><figure><img :src="String(preview.image_url)" :alt="String(preview.alt_zh || preview.name_zh)" /><figcaption>{{ preview.name_zh }}</figcaption></figure></div></Teleport>
  </AdminShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import ImageUploadField from "../components/ImageUploadField.vue";
import { adminCreateCertificate, adminDeleteCertificate, adminListCertificates, adminUpdateCertificate, adminUpdateCertificateStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";

type Row = Record<string, unknown>;
const certificates = ref<Row[]>([]); const editingId = ref<number | null>(null); const imageFieldKey = ref(0); const preview = ref<Row | null>(null); const error = ref(""); const feedback = useAdminFeedback();
const initial = () => ({ certificateNo: "", nameZh: "", nameEn: "", imageUrl: "", altZh: "", altEn: "", sortOrder: 10, publishStatus: "DRAFT" });
const form = reactive(initial());
const load = async () => { certificates.value = await adminListCertificates(); };
const reset = () => { editingId.value = null; Object.assign(form, initial()); imageFieldKey.value += 1; error.value = ""; };
const startNew = () => { reset(); feedback.info("已切换到新增证书。"); };
const cancelEdit = () => { reset(); feedback.info("已取消编辑。"); };
const edit = (item: Row) => { editingId.value = Number(item.id); Object.assign(form, { certificateNo: item.certificate_no, nameZh: item.name_zh, nameEn: item.name_en, imageUrl: item.image_url, altZh: item.alt_zh || "", altEn: item.alt_en || "", sortOrder: item.sort_order, publishStatus: item.publish_status }); imageFieldKey.value += 1; feedback.info("已进入证书编辑状态。"); };
const submit = async () => { error.value = ""; try { const wasEditing = Boolean(editingId.value); form.altEn ||= form.altZh; if (editingId.value) await adminUpdateCertificate(editingId.value, { ...form }); else await adminCreateCertificate({ ...form }); reset(); await load(); feedback.success(wasEditing ? "证书保存成功。" : "证书新增成功。"); } catch { error.value = "保存失败，请确认必填字段完整且证书编号不重复。"; feedback.error(error.value); } };
const toggle = async (item: Row) => { try { const publishing = item.publish_status !== "PUBLISHED"; await adminUpdateCertificateStatus(Number(item.id), publishing ? "PUBLISHED" : "OFFLINE"); await load(); feedback.success(publishing ? "证书发布成功。" : "证书下线成功。"); } catch { feedback.error("证书状态更新失败。"); } };
const remove = async (item: Row) => { if (!globalThis.confirm(`确定删除证书“${String(item.name_zh)}”吗？删除后前台将不再显示。`)) return; try { await adminDeleteCertificate(Number(item.id)); if (editingId.value === Number(item.id)) reset(); await load(); feedback.success("证书删除成功。"); } catch { feedback.error("证书删除失败。"); } };
const statusLabel = (status: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(status)] || "未知");
onMounted(load);
</script>

<style scoped>
.layout{display:grid;grid-template-columns:380px 1fr;gap:18px;align-items:start}.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.form{display:grid;gap:11px}.form label{display:grid;gap:5px;color:#334155;font-size:.88rem;font-weight:600}.required-mark{color:#dc2626}input,select{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;font:inherit}.heading,.actions{display:flex;flex-wrap:wrap;gap:8px;align-items:center}.heading{justify-content:space-between}.heading h2{margin:0}.certificate-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:14px}.certificate-card{display:grid;gap:10px;border:1px solid #e2e8f0;border-radius:10px;padding:10px}.preview{display:grid;width:100%;height:190px;padding:8px;place-items:center;background:#f8fafc}.preview img{max-width:100%;max-height:100%;object-fit:contain}.content{display:grid;gap:4px}.content small,.content span{color:#64748b;font-size:.78rem}.content strong{color:#1e293b}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.danger{border-color:#fecaca;color:#b91c1c}.error{color:#b91c1c}.empty{padding:30px;text-align:center;color:#64748b}button{border:1px solid #cbd5e1;border-radius:7px;padding:7px 9px;background:#fff;cursor:pointer}.lightbox{position:fixed;z-index:5000;inset:0;display:grid;padding:40px;place-items:center;background:rgb(15 23 42 / 82%)}.lightbox figure{display:grid;gap:12px;max-width:min(900px,90vw);max-height:88vh;margin:0;padding:18px;border-radius:12px;background:#fff}.lightbox img{max-width:100%;max-height:75vh;object-fit:contain}.lightbox figcaption{text-align:center;font-weight:700}.close{position:fixed;top:22px;right:26px;border:0;background:transparent;color:#fff;font-size:2rem}@media(max-width:850px){.layout{grid-template-columns:1fr}}
</style>

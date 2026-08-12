<template>
  <AdminShell title="产品分类管理" subtitle="维护分类信息、排序及发布状态。">
    <div class="layout">
      <form class="card form" @submit.prevent="submit">
        <h2>{{ editingId ? "编辑分类" : "新建分类" }}</h2>
        <label><span>分类名称（中文） <span class="required-mark" aria-hidden="true">*</span></span><input v-model.trim="form.nameZh" required /></label>
        <label><span>分类名称（英文） <span class="required-mark" aria-hidden="true">*</span></span><input v-model.trim="form.nameEn" required /></label>
        <label>化学式或分类标识 <input v-model.trim="form.chemicalFormula" placeholder="例如：Al(OH)₃" /></label>
        <label>分类简介（中文） <textarea v-model.trim="form.summaryZh" /></label>
        <label>分类简介（英文） <textarea v-model.trim="form.summaryEn" /></label>
        <label>应用领域（中文） <textarea v-model.trim="form.applicationZh" /></label>
        <label>应用领域（英文） <textarea v-model.trim="form.applicationEn" /></label>
        <ImageUploadField v-model="form.imageUrl" label="分类主图" scene="CATEGORY" />
        <label>图片说明（中文） <input v-model.trim="form.imageAltZh" placeholder="用于中文页面的图片说明" /></label>
        <label>图片说明（英文） <input v-model.trim="form.imageAltEn" placeholder="用于英文页面的图片说明" /></label>
        <label>SEO Title <input v-model.trim="form.seoTitle" maxlength="255" /></label>
        <label>SEO Description <textarea v-model.trim="form.seoDescription" maxlength="1000" /></label>
        <label><span>排序 <span class="required-mark" aria-hidden="true">*</span></span><input v-model.number="form.sortOrder" type="number" required /></label>
        <label><span>发布状态 <span class="required-mark" aria-hidden="true">*</span></span><select v-model="form.publishStatus" required><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label>
        <div class="actions"><button class="primary">{{ editingId ? "保存" : "新增分类" }}</button><button v-if="editingId" type="button" @click="cancelEdit">取消</button></div>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
      <section class="card">
        <h2>分类列表</h2>
        <p class="muted">点击“管理系列”进入该分类的产品系列。</p>
        <table><thead><tr><th>分类</th><th>排序</th><th>发布状态</th><th>操作</th></tr></thead><tbody>
          <tr v-for="item in categories" :key="Number(item.id)"><td><strong>{{ item.name_zh }}</strong></td><td>{{ item.sort_order }}</td><td><span class="status">{{ statusLabel(item.publish_status) }}</span></td><td class="row-actions"><button @click="edit(item)">编辑</button><button @click="manageSeries(item)">管理系列</button><button @click="toggle(item)">{{ item.publish_status === "PUBLISHED" ? "下线" : "发布" }}</button><button class="danger" @click="remove(item)">删除</button></td></tr>
        </tbody></table>
      </section>
    </div>
  </AdminShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import AdminShell from "../components/AdminShell.vue";
import ImageUploadField from "../components/ImageUploadField.vue";
import { adminCreateCategory, adminDeleteCategory, adminListCategories, adminUpdateCategory, adminUpdateCategoryStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { createUrlIdentifier } from "../utils/adminSlug";

const route = useRoute(); const router = useRouter();
const feedback = useAdminFeedback();
const categories = ref<Array<Record<string, unknown>>>([]); const editingId = ref<number | null>(null); const error = ref("");
const initial = () => ({ slug: "", nameZh: "", nameEn: "", chemicalFormula: "", summaryZh: "", summaryEn: "", applicationZh: "", applicationEn: "", seoTitle: "", seoDescription: "", sortOrder: 10, publishStatus: "DRAFT", imageUrl: "", imageAltZh: "", imageAltEn: "" });
const form = reactive(initial());
const load = async () => { categories.value = await adminListCategories(); };
const statusLabel = (status: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(status)] || "未知");
const reset = () => { editingId.value = null; Object.assign(form, initial()); };
const cancelEdit = () => { reset(); feedback.info("已取消编辑。"); };
const edit = (item: Record<string, unknown>) => { editingId.value = Number(item.id); Object.assign(form, { slug: item.slug, nameZh: item.name_zh, nameEn: item.name_en || item.name_zh, chemicalFormula: item.chemical_formula ?? "", summaryZh: item.summary_zh ?? "", summaryEn: item.summary_en ?? item.summary_zh ?? "", applicationZh: item.application_zh ?? "", applicationEn: item.application_en ?? item.application_zh ?? "", seoTitle: item.seo_title ?? "", seoDescription: item.seo_description ?? "", sortOrder: item.sort_order, publishStatus: item.publish_status, imageUrl: item.image_url ?? "", imageAltZh: item.image_alt_zh ?? "", imageAltEn: item.image_alt_en ?? item.image_alt_zh ?? "" }); feedback.info("已进入分类编辑状态。"); };
const submit = async () => { error.value = ""; try { const wasEditing = Boolean(editingId.value); form.nameEn ||= form.nameZh; form.summaryEn ||= form.summaryZh; form.applicationEn ||= form.applicationZh; form.imageAltEn ||= form.imageAltZh; if (editingId.value) await adminUpdateCategory(editingId.value, { ...form }); else { form.slug = createUrlIdentifier(form.nameEn); if (!form.slug) throw new Error("invalid English name"); await adminCreateCategory({ ...form }); } reset(); await load(); feedback.success(wasEditing ? "分类保存成功。" : "分类新增成功。"); } catch (caught) { const message = caught instanceof Error ? caught.message : "保存失败，请稍后重试。"; error.value = message === "category image is required before publishing" ? "已发布分类必须设置主图，请上传图片后再保存。" : message; feedback.error(error.value); } };
const toggle = async (item: Record<string, unknown>) => { try { const publishing = item.publish_status !== "PUBLISHED"; await adminUpdateCategoryStatus(Number(item.id), publishing ? "PUBLISHED" : "OFFLINE"); await load(); feedback.success(publishing ? "分类发布成功。" : "分类下线成功。"); } catch { feedback.error("状态更新失败，请稍后重试。"); } };
const remove = async (item: Record<string, unknown>) => { if (!globalThis.confirm(`确定删除分类“${String(item.name_zh)}”吗？该分类下的系列和产品也将被删除。`)) return; try { await adminDeleteCategory(Number(item.id)); if (editingId.value === Number(item.id)) reset(); await load(); feedback.success("分类删除成功。"); } catch { feedback.error("分类删除失败，请稍后重试。"); } };
const manageSeries = (item: Record<string, unknown>) => router.push({ name: "admin-product-series", params: { lang: route.params.lang }, query: { categoryId: String(item.id) } });
onMounted(load);
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 360px 1fr; gap: 18px; align-items: start; }.card { border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; background: #fff; }.form { display: grid; gap: 12px; }.form label { display: grid; gap: 5px; color: #334155; font-size: .88rem; font-weight: 600; }.required-mark { display: inline; margin-left: 2px; color: #dc2626; } input, select, textarea { border: 1px solid #cbd5e1; border-radius: 7px; padding: 8px 10px; font: inherit; } textarea { min-height: 64px; resize: vertical; }.actions, .row-actions { display: flex; flex-wrap: wrap; gap: 7px; } button { border: 1px solid #cbd5e1; border-radius: 6px; padding: 7px 9px; background: #fff; cursor: pointer; }.primary { border-color: #2563eb; background: #2563eb; color: #fff; }.danger { border-color: #fecaca; color: #b91c1c; }.error { color: #b91c1c; }.muted, small { color: #64748b; } small { display: block; margin-top: 3px; } table { width: 100%; border-collapse: collapse; } th, td { border-top: 1px solid #e2e8f0; padding: 11px 8px; text-align: left; vertical-align: top; } th { color: #64748b; font-size: .8rem; }.status { color: #1d4ed8; font-size: .8rem; font-weight: 700; } @media (max-width: 800px) { .layout { grid-template-columns: 1fr; } }
</style>

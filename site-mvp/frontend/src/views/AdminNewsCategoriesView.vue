<template>
  <AdminShell title="资讯分类" subtitle="维护资讯分类的中英文名称、排序和发布状态。">
    <section class="card list">
      <div class="filters"><input v-model.trim="filters.keyword" placeholder="搜索名称或 URL 标识" @keyup.enter="search" /><select v-model="filters.status" @change="search"><option value="">全部状态</option><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select><button @click="search">查询</button><button @click="startNew">新建分类</button><RouterLink :to="`/${lang}/admin/news/articles`">管理资讯</RouterLink></div>
      <table><thead><tr><th>分类</th><th>排序</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead><tbody>
        <tr v-for="item in items" :key="Number(item.id)"><td><strong>{{ item.name_zh }}</strong><small>{{ item.name_en }} · {{ item.slug }}</small></td><td>{{ item.sort_order }}</td><td>{{ statusLabel(item.publish_status) }}</td><td>{{ formatDate(item.updated_at) }}</td><td class="actions"><button @click="edit(item)">编辑</button><a v-if="item.publish_status === 'PUBLISHED' && (item.slug === 'industry-trends' || item.slug === 'events')" :href="`/${lang}/news/${item.slug}`" target="_blank">预览</a><button @click="toggle(item)">{{ item.publish_status === "PUBLISHED" ? "下线" : "发布" }}</button><button class="danger" @click="remove(item)">删除</button></td></tr>
        <tr v-if="!items.length"><td colspan="5" class="empty">暂无符合条件的分类</td></tr>
      </tbody></table>
      <div class="pager"><span>共 {{ total }} 条</span><button :disabled="page === 1" @click="changePage(page - 1)">上一页</button><span>第 {{ page }} / {{ totalPages }} 页</span><button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button></div>
    </section>

    <AdminDrawer :open="drawerOpen" :title="editingId ? '编辑分类' : '新建分类'" subtitle="带 * 为必填。" width="620px" @close="closeEditor">
      <form class="form" @submit.prevent="submit">
        <div class="two"><label>分类名称（中文） *<input v-model.trim="form.nameZh" required /></label><label>分类名称（英文） *<input v-model.trim="form.nameEn" required /></label></div>
        <label>URL 标识<input :value="previewSlug" readonly /></label>
        <div class="two"><label>排序 *<input v-model.number="form.sortOrder" type="number" required /></label><label>发布状态 *<select v-model="form.publishStatus"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label></div>
        <div class="actions"><button class="primary">{{ editingId ? "保存修改" : "创建分类" }}</button><button v-if="editingId" type="button" @click="cancelEdit">取消编辑</button></div>
      </form>
    </AdminDrawer>
  </AdminShell>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import AdminDrawer from "../components/AdminDrawer.vue";
import AdminShell from "../components/AdminShell.vue";
import { adminCreateNewsCategory, adminDeleteNewsCategory, adminListNewsCategories, adminUpdateNewsCategory, adminUpdateNewsCategoryStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { createUrlIdentifier } from "../utils/adminSlug";

type Row = Record<string, unknown>;
const route = useRoute(); const lang = computed(() => String(route.params.lang || "zh")); const feedback = useAdminFeedback();
const items = ref<Row[]>([]); const total = ref(0); const page = ref(1); const pageSize = 15; const editingId = ref<number | null>(null);
const drawerOpen = ref(false);
const filters = reactive({ keyword: "", status: "" });
const initial = () => ({ slug: "", nameZh: "", nameEn: "", sortOrder: 10, publishStatus: "DRAFT" }); const form = reactive(initial());
const previewSlug = computed(() => form.slug || createUrlIdentifier(form.nameEn)); const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
const load = async () => { try { const result = await adminListNewsCategories({ ...filters, page: page.value, pageSize }); items.value = result.items; total.value = result.total; } catch { feedback.error("资讯分类加载失败。"); } };
const search = async () => { page.value = 1; await load(); }; const changePage = async (next: number) => { page.value = next; await load(); };
const reset = () => { editingId.value = null; Object.assign(form, initial()); };
const closeEditor = () => { drawerOpen.value = false; reset(); };
const cancelEdit = () => { closeEditor(); feedback.info("已取消编辑。"); };
const startNew = () => { reset(); drawerOpen.value = true; };
const edit = (item: Row) => { editingId.value = Number(item.id); Object.assign(form, { slug: item.slug, nameZh: item.name_zh, nameEn: item.name_en, sortOrder: item.sort_order, publishStatus: item.publish_status }); drawerOpen.value = true; };
const submit = async () => { try { form.slug = previewSlug.value; if (!form.slug) throw new Error(); if (editingId.value) await adminUpdateNewsCategory(editingId.value, { ...form }); else await adminCreateNewsCategory({ ...form }); closeEditor(); await load(); feedback.success("资讯分类保存成功。"); } catch { feedback.error("保存失败，请检查名称或 URL 标识是否重复。"); } };
const toggle = async (item: Row) => { const publishing = item.publish_status !== "PUBLISHED"; if (!publishing && !confirm(`确定下线“${item.name_zh}”吗？`)) return; try { await adminUpdateNewsCategoryStatus(Number(item.id), publishing ? "PUBLISHED" : "OFFLINE"); await load(); feedback.success(publishing ? "分类已发布。" : "分类已下线。"); } catch { feedback.error("状态更新失败。"); } };
const remove = async (item: Row) => { if (!confirm(`确定删除“${item.name_zh}”吗？该分类下的资讯也会下线并删除。`)) return; try { await adminDeleteNewsCategory(Number(item.id)); if (editingId.value === Number(item.id)) closeEditor(); await load(); feedback.success("分类已删除。"); } catch { feedback.error("分类删除失败。"); } };
const statusLabel = (value: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(value)] || "未知");
const formatDate = (value: unknown) => value ? new Date(String(value)).toLocaleString("zh-CN", { hour12: false }) : "-";
onMounted(load);
</script>
<style scoped>
.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.form{display:grid;gap:12px}.two{display:grid;grid-template-columns:1fr 1fr;gap:12px}.form label{display:grid;gap:5px;color:#334155;font-size:.85rem;font-weight:700}.filters,.actions,.pager{display:flex;flex-wrap:wrap;gap:8px;align-items:center}.filters{margin-bottom:14px}.filters input{min-width:230px}.filters a,.actions a{border:1px solid #cbd5e1;border-radius:7px;padding:7px 9px;color:#2563eb;text-decoration:none}input,select,button{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;background:#fff;font:inherit}button{cursor:pointer}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.danger{color:#b91c1c;border-color:#fecaca}table{width:100%;border-collapse:collapse}th,td{border-bottom:1px solid #e2e8f0;padding:10px 7px;text-align:left;vertical-align:top}th{color:#64748b;font-size:.75rem}td small{display:block;color:#64748b;margin-top:3px}.pager{justify-content:flex-end;margin-top:14px;color:#64748b}.empty{text-align:center;color:#94a3b8}.list{overflow:auto}@media(max-width:720px){.two{grid-template-columns:1fr}}
</style>

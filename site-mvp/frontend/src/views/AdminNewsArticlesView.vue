<template>
  <AdminShell title="资讯列表" subtitle="创建和维护双语资讯、封面、关联内容、SEO 与发布状态。">
    <section class="card filters">
      <input v-model.trim="filters.keyword" placeholder="搜索标题、摘要或 URL 标识" @keyup.enter="search" />
      <select v-model="filters.categoryId" @change="search"><option value="">全部分类</option><option v-for="item in categories" :key="Number(item.id)" :value="String(item.id)">{{ item.name_zh }}</option></select>
      <select v-model="filters.status" @change="search"><option value="">全部状态</option><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select>
      <button class="primary" @click="search">查询</button><button @click="startNew">新建资讯</button><RouterLink :to="`/${lang}/admin/news/categories`">管理分类</RouterLink>
    </section>
    <div class="layout">
      <form class="card form" @submit.prevent="submit">
        <div class="heading"><h2>{{ editingId ? "编辑资讯" : "新建资讯" }}</h2><button v-if="editingId" type="button" @click="reset">取消编辑</button></div>
        <div class="two"><label>所属分类 *<select v-model.number="form.categoryId" required><option :value="0" disabled>请选择</option><option v-for="item in categories" :key="Number(item.id)" :value="Number(item.id)">{{ item.name_zh }}（{{ statusLabel(item.publish_status) }}）</option></select></label><label>发布时间<input v-model="form.publishedAt" type="datetime-local" /></label></div>
        <div class="two"><label>标题（中文） *<input v-model.trim="form.titleZh" required /></label><label>标题（英文） *<input v-model.trim="form.titleEn" required /></label></div>
        <label>URL 标识<input :value="previewSlug" readonly /></label>
        <div class="two"><label>摘要（中文）<textarea v-model.trim="form.summaryZh" /></label><label>摘要（英文）<textarea v-model.trim="form.summaryEn" /></label></div>
        <div class="two"><label>正文（中文）<textarea v-model="form.contentZh" class="content" /></label><label>正文（英文）<textarea v-model="form.contentEn" class="content" /></label></div>
        <ImageUploadField :key="imageKey" v-model="form.coverImageUrl" label="资讯封面图" scene="NEWS_COVER" />
        <div class="two"><label>封面 Alt（中文）<input v-model.trim="form.coverAltZh" /></label><label>封面 Alt（英文）<input v-model.trim="form.coverAltEn" /></label></div>
        <div class="two"><label>推荐产品<select v-model="form.recommendedProductIds" multiple><option v-for="item in products" :key="Number(item.id)" :value="Number(item.id)">{{ item.model }} · {{ item.name_zh }}</option></select></label><label>相关文章<select v-model="form.relatedArticleIds" multiple><option v-for="item in relatedOptions" :key="Number(item.id)" :value="Number(item.id)">{{ item.title_zh }}</option></select></label></div>
        <div class="two"><label>SEO Title<input v-model.trim="form.seoTitle" maxlength="255" /></label><label>SEO Description<textarea v-model.trim="form.seoDescription" maxlength="1000" /></label></div>
        <div class="two"><label>排序 *<input v-model.number="form.sortOrder" type="number" required /></label><label>发布状态 *<select v-model="form.publishStatus"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label></div>
        <div class="actions"><button type="button" @click="saveAsDraft">保存草稿</button><button class="primary">{{ form.publishStatus === "PUBLISHED" ? "保存并发布" : "保存" }}</button><a v-if="editingId && categorySlugFor(form.categoryId)" :href="`/${lang}/news/${categorySlugFor(form.categoryId)}/${form.slug}`" target="_blank">预览页面</a></div>
      </form>
      <section class="card list-card">
        <table><thead><tr><th>资讯</th><th>分类</th><th>状态</th><th>发布时间</th><th>更新时间</th><th>操作</th></tr></thead><tbody>
          <tr v-for="item in items" :key="Number(item.id)"><td><strong>{{ item.title_zh }}</strong><small>{{ item.title_en }}</small><small>{{ item.slug }} · 排序 {{ item.sort_order }}</small></td><td>{{ item.category_name_zh }}</td><td>{{ statusLabel(item.publish_status) }}</td><td>{{ formatDate(item.published_at) }}</td><td>{{ formatDate(item.updated_at) }}</td><td class="row-actions"><button @click="edit(item)">编辑</button><a v-if="item.publish_status === 'PUBLISHED' && item.category_slug" :href="`/${lang}/news/${item.category_slug}/${item.slug}`" target="_blank">预览</a><button @click="toggle(item)">{{ item.publish_status === "PUBLISHED" ? "下线" : "发布" }}</button><button class="danger" @click="remove(item)">删除</button></td></tr>
          <tr v-if="!items.length"><td colspan="6" class="empty">暂无符合条件的资讯</td></tr>
        </tbody></table>
        <div class="pager"><span>共 {{ total }} 条</span><button :disabled="page === 1" @click="changePage(page - 1)">上一页</button><span>第 {{ page }} / {{ totalPages }} 页</span><button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button></div>
      </section>
    </div>
  </AdminShell>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import AdminShell from "../components/AdminShell.vue";
import ImageUploadField from "../components/ImageUploadField.vue";
import { adminCreateNewsArticle, adminDeleteNewsArticle, adminListNewsArticles, adminNewsArticleOptions, adminNewsCategoryOptions, adminNewsProductOptions, adminUpdateNewsArticle, adminUpdateNewsArticleStatus } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { createUrlIdentifier } from "../utils/adminSlug";

type Row = Record<string, any>;
const route = useRoute(); const lang = computed(() => String(route.params.lang || "zh")); const feedback = useAdminFeedback();
const items = ref<Row[]>([]); const categories = ref<Row[]>([]); const products = ref<Row[]>([]); const relatedOptions = ref<Row[]>([]);
const total = ref(0); const page = ref(1); const pageSize = 12; const editingId = ref<number | null>(null); const imageKey = ref(0);
const filters = reactive({ keyword: "", status: "", categoryId: "" });
const initial = () => ({ categoryId: 0, slug: "", titleZh: "", titleEn: "", summaryZh: "", summaryEn: "", contentZh: "", contentEn: "", coverImageUrl: "", coverAltZh: "", coverAltEn: "", seoTitle: "", seoDescription: "", publishedAt: "", sortOrder: 10, publishStatus: "DRAFT", recommendedProductIds: [] as number[], relatedArticleIds: [] as number[] });
const form = reactive(initial()); const previewSlug = computed(() => form.slug || createUrlIdentifier(form.titleEn)); const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
const categorySlugFor = (categoryId: number) => String(categories.value.find(item => Number(item.id) === Number(categoryId))?.slug || "");
const loadOptions = async () => { [categories.value, products.value, relatedOptions.value] = await Promise.all([adminNewsCategoryOptions(), adminNewsProductOptions(), adminNewsArticleOptions(editingId.value || undefined)]); };
const load = async () => { try { const result = await adminListNewsArticles({ ...filters, categoryId: filters.categoryId || undefined, page: page.value, pageSize }); items.value = result.items; total.value = result.total; } catch { feedback.error("资讯列表加载失败。"); } };
const search = async () => { page.value = 1; await load(); }; const changePage = async (next: number) => { page.value = next; await load(); };
const reset = async () => { editingId.value = null; Object.assign(form, initial()); imageKey.value += 1; await loadOptions(); };
const startNew = async () => { await reset(); feedback.info("已切换到新建资讯。"); };
const dateInput = (value: unknown) => value ? new Date(String(value)).toISOString().slice(0, 16) : "";
const edit = async (item: Row) => { editingId.value = Number(item.id); Object.assign(form, { categoryId: Number(item.category_id), slug: item.slug, titleZh: item.title_zh, titleEn: item.title_en, summaryZh: item.summary_zh || "", summaryEn: item.summary_en || "", contentZh: item.content_zh || "", contentEn: item.content_en || "", coverImageUrl: item.cover_image_url || "", coverAltZh: item.cover_alt_zh || "", coverAltEn: item.cover_alt_en || "", seoTitle: item.seo_title || "", seoDescription: item.seo_description || "", publishedAt: dateInput(item.published_at), sortOrder: Number(item.sort_order), publishStatus: item.publish_status, recommendedProductIds: [...(item.recommended_product_ids || [])].map(Number), relatedArticleIds: [...(item.related_article_ids || [])].map(Number) }); imageKey.value += 1; await loadOptions(); scrollTo({ top: 0, behavior: "smooth" }); };
const persist = async () => { form.slug = previewSlug.value; if (!form.categoryId || !form.slug) throw new Error(); form.summaryEn ||= form.summaryZh; form.contentEn ||= form.contentZh; form.coverAltEn ||= form.coverAltZh; if (editingId.value) await adminUpdateNewsArticle(editingId.value, { ...form }); else await adminCreateNewsArticle({ ...form }); await reset(); await load(); };
const submit = async () => { try { await persist(); feedback.success("资讯保存成功。"); } catch { feedback.error("保存失败；发布前请填写分类、双语标题、摘要、正文和封面图。"); } };
const saveAsDraft = async () => { form.publishStatus = "DRAFT"; await submit(); };
const toggle = async (item: Row) => { const publishing = item.publish_status !== "PUBLISHED"; if (!publishing && !confirm(`确定下线“${item.title_zh}”吗？`)) return; try { await adminUpdateNewsArticleStatus(Number(item.id), publishing ? "PUBLISHED" : "OFFLINE"); await load(); feedback.success(publishing ? "资讯已发布。" : "资讯已下线。"); } catch { feedback.error("状态更新失败，请检查分类、摘要、正文和封面图。"); } };
const remove = async (item: Row) => { if (!confirm(`确定删除资讯“${item.title_zh}”吗？`)) return; try { await adminDeleteNewsArticle(Number(item.id)); if (editingId.value === Number(item.id)) await reset(); await load(); feedback.success("资讯已删除。"); } catch { feedback.error("资讯删除失败。"); } };
const statusLabel = (value: unknown) => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[String(value)] || "未知");
const formatDate = (value: unknown) => value ? new Date(String(value)).toLocaleString("zh-CN", { hour12: false }) : "-";
onMounted(async () => { try { await Promise.all([loadOptions(), load()]); } catch { feedback.error("资讯管理初始化失败。"); } });
</script>
<style scoped>
.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.filters,.actions,.row-actions,.pager,.heading{display:flex;flex-wrap:wrap;gap:8px;align-items:center}.filters input{min-width:280px}.filters a,.actions a,.row-actions a{border:1px solid #cbd5e1;border-radius:7px;padding:7px 9px;color:#2563eb;text-decoration:none}.layout{display:grid;grid-template-columns:minmax(460px,1fr) minmax(520px,1.25fr);gap:18px;align-items:start}.form{display:grid;gap:13px}.heading{justify-content:space-between}.heading h2{margin:0}.two{display:grid;grid-template-columns:1fr 1fr;gap:12px}.form label{display:grid;gap:5px;color:#334155;font-size:.83rem;font-weight:700}input,select,textarea,button{border:1px solid #cbd5e1;border-radius:7px;padding:8px 10px;background:#fff;font:inherit}select[multiple]{min-height:120px}textarea{min-height:74px;resize:vertical}.content{min-height:210px}button{cursor:pointer}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.danger{border-color:#fecaca;color:#b91c1c}table{width:100%;border-collapse:collapse}th,td{border-bottom:1px solid #e2e8f0;padding:9px 6px;text-align:left;vertical-align:top}th{color:#64748b;font-size:.72rem}td{font-size:.8rem}td small{display:block;margin-top:3px;color:#64748b}.pager{justify-content:flex-end;margin-top:14px;color:#64748b}.empty{text-align:center;color:#94a3b8}@media(max-width:1100px){.layout{grid-template-columns:1fr}.list-card{overflow:auto}}@media(max-width:650px){.two{grid-template-columns:1fr}}
</style>

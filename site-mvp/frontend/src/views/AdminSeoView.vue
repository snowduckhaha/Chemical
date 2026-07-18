<template>
  <AdminShell title="SEO 管理" subtitle="按页面与语言维护搜索摘要、Open Graph 分享信息及 Canonical 地址。">
    <section class="card selector">
      <label>页面 Key
        <input v-model.trim="pageKey" list="seo-page-keys" placeholder="例如：home" @change="load" />
        <datalist id="seo-page-keys"><option v-for="key in pageKeys" :key="key" :value="key" /></datalist>
      </label>
      <label>语言
        <select v-model="lang" @change="load"><option value="zh">中文</option><option value="en">English</option></select>
      </label>
      <button type="button" @click="load">加载配置</button>
    </section>

    <form class="card form" @submit.prevent="save">
      <div class="heading"><div><h2>{{ pageKey || "请选择页面" }}</h2><p>{{ lang === "zh" ? "中文" : "英文" }} SEO 配置</p></div><span :class="['status', form.publishStatus.toLowerCase()]">{{ statusLabel }}</span></div>
      <div class="grid">
        <label>SEO Title <span>*</span><input v-model.trim="form.title" maxlength="255" required /><small>{{ form.title.length }}/255</small></label>
        <label>SEO Description<textarea v-model.trim="form.description" maxlength="1000" rows="4" /><small>{{ form.description.length }}/1000</small></label>
        <label>OG Title<input v-model.trim="form.ogTitle" maxlength="255" placeholder="留空时前台可回退 SEO Title" /></label>
        <label>OG Description<textarea v-model.trim="form.ogDescription" maxlength="1000" rows="4" placeholder="留空时前台可回退 SEO Description" /></label>
      </div>
      <ImageUploadField :key="imageFieldKey" v-model="form.ogImage" label="OG Image" scene="SEO_OG" />
      <button v-if="form.ogImage" class="remove-image" type="button" @click="clearImage">移除 OG Image</button>
      <label>Canonical URL<input v-model.trim="form.canonical" maxlength="500" placeholder="例如：https://www.example.com/zh/products" /></label>
      <label>发布状态<select v-model="form.publishStatus"><option value="DRAFT">草稿</option><option value="PUBLISHED">已发布</option><option value="OFFLINE">已下线</option></select></label>
      <div class="actions"><button class="primary" :disabled="saving || !pageKey">{{ saving ? "保存中…" : "保存 SEO 配置" }}</button><button type="button" @click="load">取消修改</button></div>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AdminShell from "../components/AdminShell.vue";
import ImageUploadField from "../components/ImageUploadField.vue";
import { adminGetSeo, adminListSeoPages, adminUpdateSeo } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";

const pageKeys = ref<string[]>([]); const pageKey = ref("home"); const lang = ref<"zh" | "en">("zh");
const imageFieldKey = ref(0); const saving = ref(false); const error = ref(""); const feedback = useAdminFeedback();
const initial = () => ({ title: "", description: "", ogTitle: "", ogDescription: "", ogImage: "", canonical: "", publishStatus: "DRAFT" });
const form = reactive(initial());
const statusLabel = computed(() => ({ DRAFT: "草稿", PUBLISHED: "已发布", OFFLINE: "已下线" }[form.publishStatus] || form.publishStatus));
const load = async () => { if (!pageKey.value) return; error.value = ""; try { const data = await adminGetSeo(pageKey.value, lang.value); Object.assign(form, { title: data.title || "", description: data.description || "", ogTitle: data.og_title || "", ogDescription: data.og_description || "", ogImage: data.og_image || "", canonical: data.canonical || "", publishStatus: data.publish_status || "DRAFT" }); imageFieldKey.value += 1; } catch { error.value = "SEO 配置加载失败。"; feedback.error(error.value); } };
const save = async () => { error.value = ""; if (!pageKey.value) { error.value = "请输入页面 Key。"; return; } saving.value = true; try { await adminUpdateSeo(pageKey.value, lang.value, { ...form }); if (!pageKeys.value.includes(pageKey.value)) pageKeys.value.push(pageKey.value); feedback.success("SEO 配置保存成功。"); await load(); } catch { error.value = "保存失败，请检查标题、页面 Key 与字段长度。"; feedback.error(error.value); } finally { saving.value = false; } };
const clearImage = () => { form.ogImage = ""; imageFieldKey.value += 1; };
onMounted(async () => { try { pageKeys.value = await adminListSeoPages(); await load(); } catch { error.value = "SEO 页面列表加载失败。"; } });
</script>

<style scoped>
.card{border:1px solid #e2e8f0;border-radius:12px;padding:18px;background:#fff}.selector{display:grid;grid-template-columns:minmax(280px,1fr) 160px auto;gap:14px;align-items:end}.form{display:grid;gap:16px;max-width:900px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:16px}label{display:grid;gap:6px;color:#334155;font-size:.88rem;font-weight:650}label span,.error{color:#b91c1c}input,select,textarea{box-sizing:border-box;width:100%;border:1px solid #cbd5e1;border-radius:7px;padding:9px 10px;font:inherit;resize:vertical}small,.heading p{margin:0;color:#64748b;font-size:.76rem}.heading,.actions{display:flex;justify-content:space-between;gap:10px;align-items:center}.heading h2{margin:0;color:#0f172a}.status{border-radius:999px;padding:5px 10px;background:#f1f5f9;font-size:.76rem;font-weight:800}.status.published{background:#dcfce7;color:#166534}.status.offline{background:#fee2e2;color:#991b1b}button{border:1px solid #cbd5e1;border-radius:7px;padding:9px 12px;background:#fff;cursor:pointer}.primary{border-color:#2563eb;background:#2563eb;color:#fff}.primary:disabled{opacity:.55}.actions{justify-content:flex-start}.remove-image{justify-self:start;color:#b91c1c}@media(max-width:750px){.selector,.grid{grid-template-columns:1fr}}
</style>

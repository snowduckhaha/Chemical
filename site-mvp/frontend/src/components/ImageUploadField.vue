<template>
  <div class="image-field">
    <label>{{ label }}<input ref="fileInput" accept="image/jpeg,image/png" type="file" @change="upload" /></label>
    <p class="hint">JPG/PNG，最大 3 MB。{{ hintText }}</p>
    <p v-if="uploading">正在生成展示图…</p><p v-if="error" class="error">{{ error }}</p>
    <figure v-if="modelValue" :class="{ certificate: scene === 'CERTIFICATE', seo: scene === 'SEO_OG', news: scene === 'NEWS_COVER' }"><img :src="modelValue" alt="上传预览" /><figcaption>{{ resultText }}</figcaption></figure>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { adminUploadImage } from "../api/site";
import { useAdminFeedback } from "../composables/useAdminFeedback";
const props = defineProps<{ modelValue: string; label?: string; scene?: string }>();
const emit = defineEmits<{ "update:modelValue": [value: string] }>();
const feedback = useAdminFeedback();
const hintText = computed(() => props.scene === "CERTIFICATE" ? "按原比例完整展示，不裁切证书内容。" : props.scene === "SEO_OG" ? "生成 1200 × 630（1.91:1）白底完整展示画布，不裁切内容。" : props.scene === "NEWS_COVER" ? "生成 1200 × 675（16:9）完整展示画布，不裁切内容。" : "生成 4:3 白底完整展示画布，不裁切内容。");
const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false); const error = ref(""); const resultText = ref("图片完整展示预览");
watch(() => props.modelValue, (nextValue) => {
  if (!nextValue && fileInput.value) {
    fileInput.value.value = "";
    error.value = "";
    resultText.value = "图片完整展示预览";
  }
});
const upload = async (event: Event) => { const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return; uploading.value = true; error.value = ""; try { const result = await adminUploadImage(file, props.scene || "PRODUCT"); emit("update:modelValue", result.url); resultText.value = `${result.width} × ${result.height} · ${Math.round(result.sourceBytes / 1024)} KB → ${Math.round(result.displayBytes / 1024)} KB · 完整展示`; feedback.success("图片上传成功，请继续保存当前内容。"); } catch { error.value = "图片上传失败，请检查文件格式、大小或登录状态。"; feedback.error(error.value); } finally { uploading.value = false; } };
</script>
<style scoped>
.image-field{display:grid;gap:6px}.hint,figcaption{margin:0;color:#64748b;font-size:.78rem}.error{margin:0;color:#b91c1c;font-size:.82rem}figure{margin:0;border:1px solid #e2e8f0;border-radius:8px;padding:8px}img{display:block;width:100%;aspect-ratio:4/3;object-fit:contain;background:#fff}.certificate img{max-height:420px;aspect-ratio:auto}.seo img{aspect-ratio:1200/630}.news img{aspect-ratio:16/9}input{margin-top:5px;font:inherit}
</style>
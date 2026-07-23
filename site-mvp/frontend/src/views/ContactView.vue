<template>
  <div class="contact-page">
    <section class="page-shell">
      <h1>{{ lang === "en" ? "Contact Us" : "联系我们" }}</h1>
      <p>{{ lang === "en" ? "Leave your inquiry and we will reply soon." : "提交你的需求，我们会尽快联系。" }}</p>
      <p v-if="applicationSlug" class="application-context">
        {{ lang === "en" ? `Inquiry source: ${applicationSlug}` : `咨询来源：${applicationSlug}` }}
      </p>

      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item :label="lang === 'en' ? 'Name' : '姓名'">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Company' : '公司'">
          <el-input v-model="form.company" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Email' : '邮箱'">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Message' : '留言'">
          <el-input v-model="form.message" type="textarea" :rows="4" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="submitting">
          {{ lang === "en" ? "Submit Inquiry" : "提交询盘" }}
        </el-button>
      </el-form>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { createInquiry } from "../api/site";
import { trackEvent } from "../analytics";

const route = useRoute();
const router = useRouter();
const lang = computed(() => String(route.params.lang || "zh"));
const applicationSlug = computed(() => typeof route.query.application === "string" ? route.query.application : "");

const submitting = ref(false);
const errorMessage = ref("");
const form = reactive({
  name: "",
  company: "",
  email: "",
  message: "",
  interestedProduct: ""
});

const submit = async () => {
  errorMessage.value = "";
  submitting.value = true;
  try {
    trackEvent("inquiry_cta_click", "contact", { payload: { cta_key: "contact_submit", cta_position: "contact_form" } });
    const result = await createInquiry({
      lang: lang.value,
      name: form.name,
      company: form.company,
      email: form.email,
      message: form.message,
      interestedProduct: form.interestedProduct,
      sourcePage: applicationSlug.value
        ? `/${lang.value}/applications/${applicationSlug.value}`
        : `/${lang.value}/contact`
    });
    trackEvent("inquiry_submit_success", "contact", { payload: { inquiry_id: result.inquiryId } });
    await router.push(`/${lang.value}/contact/success`);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : "submit failed";
  } finally {
    submitting.value = false;
  }
};

onMounted(() => trackEvent("inquiry_form_open", "contact", { payload: { form_key: "contact", open_method: "page" } }));
</script>

<style scoped>
.contact-page {
  width: 100%;
}

.page-shell {
  width: min(var(--site-width), calc(100vw - (var(--site-gutter) * 2)));
  margin: 0 auto;
  display: grid;
  gap: 12px;
  max-width: 720px;
  padding: 40px 0;
}

.error {
  color: #d92d20;
}

.application-context {
  color: #1688ca;
  font-weight: 600;
}
</style>

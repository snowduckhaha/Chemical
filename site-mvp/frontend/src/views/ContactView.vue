<template>
  <div class="contact-page">
    <section class="page-shell">
      <h1>{{ lang === "en" ? "Contact Us" : "联系我们" }}</h1>
      <p>{{ lang === "en" ? "Leave your inquiry and we will reply soon." : "提交你的需求，我们会尽快联系。" }}</p>
      <p v-if="applicationSlug" class="application-context">
        {{ lang === "en" ? `Inquiry source: ${applicationSlug}` : `咨询来源：${applicationSlug}` }}
      </p>

      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item :label="lang === 'en' ? 'Name' : '姓名'" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Company' : '公司'">
          <el-input v-model="form.company" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Email' : '邮箱'" required>
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Phone' : '手机号码'">
          <el-input v-model="form.phone" :placeholder="lang === 'en' ? 'Optional' : '非必填'" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Interested Product' : '感兴趣产品'">
          <el-input v-model="form.interestedProduct" :placeholder="lang === 'en' ? 'Optional' : '非必填'" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Message' : '留言'" required>
          <el-input v-model="form.message" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="lang === 'en' ? 'Captcha' : '验证码'" required>
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              :placeholder="lang === 'en' ? 'Enter captcha' : '请输入验证码'"
              style="max-width: 200px"
              @focus="loadCaptcha"
            />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              :alt="lang === 'en' ? 'Captcha' : '验证码'"
              class="captcha-img"
              @click="loadCaptcha"
              :title="lang === 'en' ? 'Click to refresh' : '点击刷新'"
            />
          </div>
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="submitting" :disabled="!form.captchaCode">
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
import { createInquiry, getCaptcha } from "../api/site";
import { trackEvent } from "../analytics";

const route = useRoute();
const router = useRouter();
const lang = computed(() => String(route.params.lang || "zh"));
const applicationSlug = computed(() => typeof route.query.application === "string" ? route.query.application : "");

const submitting = ref(false);
const errorMessage = ref("");
const captchaImage = ref("");
const captchaLoading = ref(false);
const form = reactive({
  name: "",
  company: "",
  email: "",
  phone: "",
  message: "",
  interestedProduct: "",
  captchaCode: ""
});

const loadCaptcha = async () => {
  if (captchaLoading.value) return;
  captchaLoading.value = true;
  try {
    const data = await getCaptcha();
    captchaImage.value = data.image;
  } catch {
    captchaImage.value = "";
  } finally {
    captchaLoading.value = false;
  }
};

const submit = async () => {
  errorMessage.value = "";
  if (!form.name.trim()) { errorMessage.value = lang.value === "en" ? "Name is required" : "姓名不能为空"; return; }
  if (!form.email.trim()) { errorMessage.value = lang.value === "en" ? "Email is required" : "邮箱不能为空"; return; }
  if (!form.message.trim()) { errorMessage.value = lang.value === "en" ? "Message is required" : "留言不能为空"; return; }
  if (!form.captchaCode.trim()) { errorMessage.value = lang.value === "en" ? "Captcha is required" : "验证码不能为空"; return; }

  submitting.value = true;
  try {
    trackEvent("inquiry_cta_click", "contact", { payload: { cta_key: "contact_submit", cta_position: "contact_form" } });
    const result = await createInquiry({
      lang: lang.value,
      name: form.name,
      company: form.company,
      email: form.email,
      phone: form.phone,
      message: form.message,
      interestedProduct: form.interestedProduct,
      sourcePage: applicationSlug.value
        ? `/${lang.value}/applications/${applicationSlug.value}`
        : `/${lang.value}/contact`,
      captchaCode: form.captchaCode
    });
    trackEvent("inquiry_submit_success", "contact", { payload: { inquiry_id: result.inquiryId } });
    await router.push(`/${lang.value}/contact/success`);
  } catch (error) {
    const msg = error instanceof Error ? error.message : "submit failed";
    if (msg.includes("captcha")) {
      errorMessage.value = lang.value === "en" ? "Invalid captcha, please try again" : "验证码错误，请重新输入";
      form.captchaCode = "";
      await loadCaptcha();
    } else {
      errorMessage.value = msg;
    }
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

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-img {
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  user-select: none;
}
</style>

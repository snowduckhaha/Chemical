<template>
  <main class="login-page">
    <form class="login-card" @submit.prevent="submit">
      <p class="eyebrow">起点化工</p>
      <h1>后台登录</h1>
      <p class="hint">使用管理员或运营编辑账号登录。</p>

      <label>
        <span>用户名 <span class="required-mark" aria-hidden="true">*</span></span>
        <input v-model.trim="username" autocomplete="username" required />
      </label>
      <label>
        <span>密码 <span class="required-mark" aria-hidden="true">*</span></span>
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>
      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <button :disabled="submitting" type="submit">
        {{ submitting ? "登录中…" : "登录" }}
      </button>
    </form>
  </main>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminAuthStore } from "../stores/adminAuth";

const router = useRouter();
const route = useRoute();
const auth = useAdminAuthStore();
const username = ref("");
const password = ref("");
const submitting = ref(false);
const errorMessage = ref("");

const submit = async () => {
  submitting.value = true;
  errorMessage.value = "";
  try {
    await auth.login(username.value, password.value);
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : `/${route.params.lang || "zh"}/admin/products/categories`;
    await router.replace(redirect);
  } catch {
    errorMessage.value = "用户名或密码错误，或账号未启用。";
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.login-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: #f5f8fc; }
.login-card { width: min(100%, 390px); padding: 36px; border: 1px solid #e2e8f0; border-radius: 18px; background: #fff; box-shadow: 0 18px 45px rgb(15 23 42 / 8%); }
.eyebrow { margin: 0; color: #2563eb; font-size: .8rem; font-weight: 700; letter-spacing: .12em; }
h1 { margin: 8px 0; color: #0f172a; font-size: 1.7rem; }
.hint { margin: 0 0 24px; color: #64748b; font-size: .9rem; }
label { display: grid; gap: 7px; margin: 15px 0; color: #334155; font-size: .9rem; font-weight: 600; }
.required-mark { margin-left: 2px; color: #dc2626; }
input { border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px 12px; font: inherit; }
button { width: 100%; margin-top: 10px; border: 0; border-radius: 8px; padding: 11px; background: #2563eb; color: #fff; font: inherit; font-weight: 700; cursor: pointer; }
button:disabled { cursor: wait; opacity: .7; }
.error { margin: 10px 0 0; color: #b91c1c; font-size: .85rem; }
</style>
<template>
  <AdminShell title="修改密码" subtitle="定期更新后台登录密码，保障账号安全。">
    <form class="password-card" @submit.prevent="submit">
      <div class="account-summary">
        <span class="avatar" aria-hidden="true">{{ userInitial }}</span>
        <div><strong>{{ auth.session?.username }}</strong><p>{{ auth.isAdmin ? "管理员账号" : "运营编辑账号" }}</p></div>
      </div>

      <label>
        当前密码 <span>*</span>
        <input v-model="currentPassword" type="password" autocomplete="current-password" required />
      </label>
      <label>
        新密码 <span>*</span>
        <input v-model="newPassword" type="password" autocomplete="new-password" minlength="8" maxlength="72" required />
        <small>密码长度为 8–72 个字符，且不能与当前密码相同。</small>
      </label>
      <label>
        确认新密码 <span>*</span>
        <input v-model="confirmation" type="password" autocomplete="new-password" minlength="8" maxlength="72" required />
      </label>

      <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
      <div class="actions">
        <button class="primary" :disabled="submitting" type="submit">{{ submitting ? "保存中…" : "保存新密码" }}</button>
        <button type="button" :disabled="submitting" @click="reset">重置</button>
      </div>
    </form>
  </AdminShell>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { adminChangePassword } from "../api/site";
import AdminShell from "../components/AdminShell.vue";
import { useAdminFeedback } from "../composables/useAdminFeedback";
import { useAdminAuthStore } from "../stores/adminAuth";

const auth = useAdminAuthStore();
const feedback = useAdminFeedback();
const currentPassword = ref("");
const newPassword = ref("");
const confirmation = ref("");
const submitting = ref(false);
const errorMessage = ref("");
const userInitial = computed(() => String(auth.session?.username || "A").slice(0, 1).toUpperCase());

const reset = () => {
  currentPassword.value = "";
  newPassword.value = "";
  confirmation.value = "";
  errorMessage.value = "";
};

const submit = async () => {
  errorMessage.value = "";
  if (newPassword.value !== confirmation.value) {
    errorMessage.value = "两次输入的新密码不一致。";
    return;
  }
  if (newPassword.value.length < 8) {
    errorMessage.value = "新密码至少需要 8 个字符。";
    return;
  }

  submitting.value = true;
  try {
    await adminChangePassword(currentPassword.value, newPassword.value);
    reset();
    feedback.success("密码修改成功，请使用新密码登录。");
  } catch {
    errorMessage.value = "修改失败，请确认当前密码正确，且新密码与当前密码不同。";
    feedback.error(errorMessage.value);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.password-card { display: grid; gap: 18px; max-width: 560px; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; }
.account-summary { display: flex; align-items: center; gap: 12px; padding-bottom: 18px; border-bottom: 1px solid #e2e8f0; }
.avatar { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 50%; background: #dbeafe; color: #1d4ed8; font-weight: 800; }
.account-summary strong { color: #0f172a; }.account-summary p, small { margin: 3px 0 0; color: #64748b; font-size: .8rem; }
label { display: grid; gap: 7px; color: #334155; font-size: .9rem; font-weight: 650; } label span, .error { color: #b91c1c; }
input { width: 100%; box-sizing: border-box; border: 1px solid #cbd5e1; border-radius: 7px; padding: 10px; font: inherit; } input:focus { outline: 2px solid #93c5fd; border-color: #2563eb; }
.actions { display: flex; gap: 10px; } button { border: 1px solid #cbd5e1; border-radius: 7px; padding: 10px 14px; background: #fff; font: inherit; cursor: pointer; } .primary { border-color: #2563eb; background: #2563eb; color: #fff; } button:disabled { cursor: wait; opacity: .6; }
.error { margin: -4px 0 0; font-size: .85rem; }
</style>
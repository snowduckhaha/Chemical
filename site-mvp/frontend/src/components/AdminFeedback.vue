<template>
  <Transition name="feedback">
    <div v-if="feedback.message.value" class="feedback" :class="feedback.type.value" role="status" aria-live="polite">
      <span class="icon" aria-hidden="true">{{ feedback.type.value === "success" ? "✓" : feedback.type.value === "error" ? "!" : "i" }}</span>
      <span>{{ feedback.message.value }}</span>
      <button type="button" aria-label="关闭提示" @click="feedback.clear">×</button>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useAdminFeedback } from "../composables/useAdminFeedback";

const feedback = useAdminFeedback();
</script>

<style scoped>
.feedback { position: fixed; z-index: 3000; top: 50%; left: 50%; display: flex; align-items: center; gap: 12px; min-width: 320px; max-width: min(560px, calc(100vw - 32px)); padding: 18px 20px; border: 1px solid; border-radius: 14px; background: #fff; box-shadow: 0 20px 60px rgb(15 23 42 / 28%); transform: translate(-50%, -50%); color: #334155; font-size: 1rem; font-weight: 600; }
.feedback.success { border-color: #86efac; background: #f0fdf4; color: #166534; }
.feedback.error { border-color: #fca5a5; background: #fef2f2; color: #991b1b; }
.feedback.info { border-color: #93c5fd; background: #eff6ff; color: #1e40af; }
.icon { display: grid; flex: 0 0 28px; width: 28px; height: 28px; place-items: center; border-radius: 50%; background: currentColor; color: #fff; font-size: .88rem; font-weight: 800; }
button { margin-left: auto; border: 0; padding: 0 2px; background: transparent; color: currentColor; font-size: 1.25rem; cursor: pointer; }
.feedback-enter-active, .feedback-leave-active { transition: opacity .18s ease, transform .18s ease; }
.feedback-enter-from, .feedback-leave-to { opacity: 0; transform: translate(-50%, calc(-50% - 12px)) scale(.96); }
</style>

<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div v-if="open" class="drawer-root" role="dialog" aria-modal="true" :aria-label="title">
        <div class="drawer-mask" @click="emit('close')"></div>
        <div class="drawer-panel" :style="{ maxWidth: width }">
          <header class="drawer-head">
            <div class="drawer-title">
              <h2>{{ title }}</h2>
              <p v-if="subtitle">{{ subtitle }}</p>
            </div>
            <button type="button" class="drawer-close" aria-label="关闭" @click="emit('close')">✕</button>
          </header>
          <div class="drawer-body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="drawer-footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from "vue";

const props = defineProps<{
  open: boolean;
  title: string;
  subtitle?: string;
  width?: string;
}>();

const emit = defineEmits<{ close: [] }>();

const onKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape" && props.open) emit("close");
};

onMounted(() => window.addEventListener("keydown", onKeydown));
onUnmounted(() => window.removeEventListener("keydown", onKeydown));

watch(
  () => props.open,
  (open) => {
    document.body.classList.toggle("admin-drawer-lock", open);
  }
);
</script>

<style scoped>
.drawer-root {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: flex;
  justify-content: flex-end;
}

.drawer-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.42);
}

.drawer-panel {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  background: #f6f8fb;
  box-shadow: -18px 0 48px rgba(15, 23, 42, 0.22);
  display: flex;
  flex-direction: column;
}

.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 16px 22px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
}

.drawer-title h2 {
  margin: 0;
  color: #0f172a;
  font-size: 1.12rem;
}

.drawer-title p {
  margin: 3px 0 0;
  color: #64748b;
  font-size: 0.82rem;
}

.drawer-close {
  border: 1px solid #cbd5e1;
  background: #ffffff;
  color: #475569;
  min-width: 34px;
  min-height: 34px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
}

.drawer-close:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 22px 28px;
}

.drawer-footer {
  padding: 13px 22px;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.18s ease;
}

.drawer-enter-active .drawer-panel,
.drawer-leave-active .drawer-panel {
  transition: transform 0.22s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .drawer-panel,
.drawer-leave-to .drawer-panel {
  transform: translateX(40px);
}
</style>

<style>
/* Global lock: the drawer is teleported outside the admin shell. */
body.admin-drawer-lock {
  overflow: hidden;
}
</style>

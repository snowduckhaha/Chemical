<template>
  <div class="nav-mega-dropdown" :class="{ 'is-open': open }" :style="panelStyle" role="menu">
    <router-link v-for="item in items" :key="item.path" :to="item.path" role="menuitem" @click="emit('select')">
      <span>{{ item.label }}</span>
    </router-link>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

type MegaDropdownItem = {
  label: string;
  path: string;
};

const props = withDefaults(
  defineProps<{
    items: MegaDropdownItem[];
    open: boolean;
    backgroundImage?: string;
    topOffset?: number;
  }>(),
  {
    backgroundImage: "/about/about-banner.png",
    topOffset: 100
  }
);

const emit = defineEmits<{
  select: [];
}>();

const panelStyle = computed(() => ({
  "--nav-mega-top": `${props.topOffset}px`,
  "--nav-mega-bg": `url("${props.backgroundImage}")`
}));
</script>

<style scoped>
.nav-mega-dropdown {
  position: fixed;
  top: 100px;
  left: 0;
  z-index: 50;
  width: 100vw;
  min-height: 180px;
  transform: translateY(0);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, calc(var(--site-width) / 2)));
  column-gap: 0;
  row-gap: 22px;
  align-content: start;
  padding: 52px max(var(--site-gutter), calc((100vw - var(--site-width)) / 2)) 38px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.95)),
    var(--nav-mega-bg) center 38% / cover no-repeat;
  border: 0;
  box-shadow: 0 2px 10px 1px rgba(0, 0, 0, 0.08);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.5s ease, visibility 0.5s ease;
}

.nav-mega-dropdown.is-open {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}

@media (min-width: 1600px) {
  .nav-mega-dropdown {
    top: 130px;
    min-height: 230px;
    padding-top: 58px;
  }

  .nav-mega-dropdown a {
    font-size: 22px;
    line-height: 26px;
  }
}

.nav-mega-dropdown a {
  position: relative;
  min-height: 26px;
  padding: 0 48px 10px 24px;
  color: #2c364c;
  text-align: left;
  text-decoration: none;
  font-family: "Inter Tight", Arial, "Noto Sans SC", "PingFang SC", sans-serif;
  font-size: 20px;
  font-weight: 400;
  line-height: 24.1667px;
  letter-spacing: 1px;
  white-space: nowrap;
  display: block;
  border-bottom: 0;
  transition: color 0.18s ease, background 0.18s ease;
}

.nav-mega-dropdown a::after {
  content: "↗";
  position: absolute;
  top: -1px;
  right: 24px;
  color: #263349;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 20px;
  font-weight: 300;
  line-height: 1;
  transition: color 0.18s ease, transform 0.18s ease;
}

.nav-mega-dropdown a:hover {
  color: #1296e1;
  background: transparent;
}

.nav-mega-dropdown a:hover::after {
  color: #1296e1;
  transform: translate(3px, -3px);
}

@media (min-width: 1600px) {
  .nav-mega-dropdown a {
    font-size: 22px;
    line-height: 26px;
  }
}

@media (max-width: 992px) {
  .nav-mega-dropdown {
    top: 100px;
    grid-template-columns: 1fr;
    row-gap: 18px;
    min-height: auto;
    padding: 26px 18px 30px;
  }
}

@media (max-width: 767px) {
  .nav-mega-dropdown {
    top: 70px;
  }
}
</style>

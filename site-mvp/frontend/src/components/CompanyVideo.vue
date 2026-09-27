<template>
  <figure class="company-video">
    <video
      class="company-video-player"
      :poster="poster"
      preload="none"
      controls
      playsinline
      :aria-label="playerLabel"
    >
      <source :src="src" type="video/mp4" />
      {{ fallbackText }}
    </video>
    <figcaption v-if="caption" class="company-video-caption">{{ caption }}</figcaption>
  </figure>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import { COMPANY_VIDEO_POSTER, COMPANY_VIDEO_URL } from "../lib/jsonLd";

withDefaults(
  defineProps<{
    caption?: string;
  }>(),
  { caption: "" }
);

const route = useRoute();
const isEn = computed(() => String(route.params.lang || "zh") === "en");
const src = COMPANY_VIDEO_URL;
const poster = COMPANY_VIDEO_POSTER;
const playerLabel = computed(() =>
  isEn.value
    ? "Origin Chemical company introduction video"
    : "起点化工公司与产品介绍视频"
);
const fallbackText = computed(() =>
  isEn.value
    ? "Your browser does not support embedded video. Download the video instead."
    : "您的浏览器不支持内嵌视频，可直接下载视频观看。"
);
</script>

<style scoped>
.company-video {
  margin: 0;
  display: grid;
  gap: 10px;
}

.company-video-player {
  width: 100%;
  aspect-ratio: 16 / 9;
  display: block;
  background: #0b1f33;
  border: 1px solid #d5e3f2;
  object-fit: cover;
}

.company-video-caption {
  color: var(--pf-muted, #55718f);
  font-size: 0.92rem;
  line-height: 1.6;
}
</style>

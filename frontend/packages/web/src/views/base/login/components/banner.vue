<template>
  <img class="banner-img" :class="{ 'banner-img-preview': props.isPreview }" :src="innerBanner" />
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { defaultLoginImage } from '@/config/business';
  import { useAppStore } from '@/store';

  const props = defineProps<{
    isPreview?: boolean;
    banner?: string;
  }>();

  const appStore = useAppStore();

  const innerBanner = computed(() =>
    props.banner ? props.banner : appStore.pageConfig.loginImage[0]?.url ?? defaultLoginImage
  );
</script>

<style lang="less" scoped>
  .banner-img {
    @apply absolute inset-0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
  }
  .banner-img-preview {
    @apply static;
    height: 100%;
  }
</style>

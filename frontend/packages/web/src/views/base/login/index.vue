<template>
  <div v-if="!props.isPreview" class="login-page">
    <banner />
    <div class="login-overlay">
      <div class="login-content">
        <div class="login-header">
          <div class="logo-title">
            <img :src="innerLogo" class="logo-icon" />
            <img :src="loginTextSrc" class="login-text-img" />
          </div>
          <div class="subtitle">{{ t(innerSlogan || '') || t('login.form.title') }}</div>
        </div>
        <div class="login-card">
          <n-spin :show="appStore.getLoginLoadingStatus">
            <loginForm :is-preview="props.isPreview" />
          </n-spin>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="login-page-preview">
    <banner :is-preview="true" :banner="props.banner" />
    <n-spin :show="appStore.getLoginLoadingStatus" class="flex-1">
      <loginForm :is-preview="props.isPreview" :slogan="props.slogan" :logo="props.logo" />
    </n-spin>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { NSpin } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import banner from './components/banner.vue';
  import loginForm from './components/login-form.vue';

  import { defaultLoginLogo } from '@/config/business';
  import useAppStore from '@/store/modules/app';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    isPreview?: boolean;
    slogan?: string;
    logo?: string;
    banner?: string;
  }>();

  const innerLogo = computed(() => {
    return props.isPreview && props.logo ? props.logo : appStore.pageConfig.loginLogo[0]?.url ?? defaultLoginLogo;
  });

  const innerSlogan = computed(() => {
    return props.isPreview && props.slogan ? props.slogan : appStore.pageConfig.slogan;
  });

  const loginTextSrc = computed(() => {
    return `${import.meta.env.BASE_URL}images/stariver-login-text.svg`;
  });
</script>

<style lang="less" scoped>
  .login-page {
    @apply relative flex h-screen w-screen overflow-hidden;
  }
  .login-overlay {
    @apply absolute right-0 top-0 flex h-full items-center justify-center;
    width: 55%;
    min-width: 600px;
    background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, #ffffff 100%);
  }
  .login-content {
    @apply flex flex-col items-center;
    gap: 32px;
  }
  .login-header {
    @apply flex flex-col items-center;
    gap: 8px;
  }
  .logo-title {
    @apply flex items-center;
    gap: 12px;
  }
  .logo-icon {
    width: 50px;
    height: 50px;
  }
  .login-text-img {
    height: 32px;
    width: auto;
  }
  .subtitle {
    font-size: 18px;
    color: #64748b;
  }
  .login-card {
    width: 480px;
    padding: 32px 40px;
    border-radius: 12px;
    background: #ffffff;
    box-shadow: 0 6px 7.5px rgba(50, 50, 51, 0.05), 0 16px 6px rgba(50, 50, 51, 0.05),
      0 8px 2.5px rgba(50, 50, 51, 0.05);
  }
  .login-page-preview {
    @apply flex items-center;
    min-width: 1200px;
  }
</style>

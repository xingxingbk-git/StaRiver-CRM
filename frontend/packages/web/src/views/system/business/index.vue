<template>
  <n-scrollbar
    class="business"
    :content-class="activeTab === 'syncOrganization' ? 'overflow-auto' : 'h-full overflow-hidden'"
  >
    <div class="business-container">
      <CrmCard no-content-padding hide-footer auto-height class="mb-[16px]">
        <CrmTab v-model:active-tab="activeTab" no-content :tab-list="tabList" type="line" />
      </CrmCard>
      <MailSettings v-if="activeTab === 'mailSettings'" />
      <IntegrationList v-if="activeTab === 'syncOrganization'" />
    </div>
  </n-scrollbar>
</template>

<script setup lang="ts">
  import { NScrollbar } from 'naive-ui';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmCard from '@/components/pure/crm-card/index.vue';
  import CrmTab from '@/components/pure/crm-tab/index.vue';
  import IntegrationList from './components/integrationList.vue';

  const MailSettings = defineAsyncComponent(() => import('./components/mailSettings.vue'));
  const { t } = useI18n();

  const activeTab = ref('syncOrganization');

  const initTabList = [
    { name: 'syncOrganization', tab: t('system.business.tab.third') },
    { name: 'mailSettings', tab: t('system.business.tab.mailSettings') },
  ];

  const tabList = ref([...initTabList]);
</script>

<style lang="less" scoped>
  :deep(.n-tabs-scroll-padding) {
    width: 16px !important;
  }
</style>

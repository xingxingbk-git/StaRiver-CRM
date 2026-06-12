<template>
  <n-layout-header class="stariver-layout-header" bordered>
    <div class="stariver-layout-header__breadcrumb">
      <span>{{ currentModuleTitle }}</span>
      <span class="stariver-layout-header__slash">/</span>
      <span class="stariver-layout-header__current">{{ currentPageTitle }}</span>
    </div>
    <div v-if="!props.isPreview" class="stariver-layout-header__actions">
      <button class="stariver-layout-header__search" :disabled="!showSearch" @click="openDuplicateCheck">
        <CrmIcon type="iconicon_search-outline_outlined" :size="15" />
        <span>搜索工单 · 客户 · 合同 …</span>
        <kbd>⌘ K</kbd>
      </button>
      <n-button class="stariver-layout-header__icon" quaternary circle title="我的任务" @click="openTask">
        <template #icon>
          <n-badge value="1" dot :show="appStore.todoStatistic.total > 0">
            <CrmIcon type="iconicon_contract" :size="16" />
          </n-badge>
        </template>
      </n-button>
      <n-button
        v-permission="['CUSTOMER_MANAGEMENT:READ', 'CLUE_MANAGEMENT:READ', 'OPPORTUNITY_MANAGEMENT:READ']"
        class="stariver-layout-header__icon"
        quaternary
        circle
        title="跟进日程"
        @click="openFollow"
      >
        <template #icon>
          <CrmIcon type="iconicon_data_plan" :size="16" />
        </template>
      </n-button>
      <n-popselect
        v-model:value="currentLocale"
        :options="LOCALE_OPTIONS"
        trigger="hover"
        @update-value="changeLanguage"
      >
        <n-button class="stariver-layout-header__icon" quaternary circle title="切换语言">
          <template #icon>
            <LanguageOutline />
          </template>
        </n-button>
      </n-popselect>
      <n-button class="stariver-layout-header__icon" quaternary circle title="消息通知" @click="showMessage">
        <template #icon>
          <n-badge value="1" dot :show="showBadge">
            <CrmIcon type="iconicon-alarmclock" :size="16" />
          </n-badge>
        </template>
      </n-button>
      <CrmAvatar :size="28" />
    </div>
    <MessageDrawer v-model:show="showMessageDrawer" />
    <Suspense>
      <CrmDuplicateCheckDrawer v-if="initDuplicateCheckDrawer" v-model:visible="showDuplicateCheckDrawer" />
    </Suspense>
  </n-layout-header>
  <CrmFollowDrawer v-if="initFollowDrawer" v-model:visible="showFollowDrawer" />
  <CrmTaskDrawer v-if="initTaskDrawer" v-model:show="showTaskDrawer" />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { NBadge, NButton, NLayoutHeader, NPopselect, useMessage } from 'naive-ui';
  import { LanguageOutline } from '@vicons/ionicons5';

  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { LOCALE_OPTIONS } from '@lib/shared/locale';
  import useLocale from '@lib/shared/locale/useLocale';
  import { LocaleType } from '@lib/shared/types/global';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import CrmAvatar from '@/components/business/crm-avatar/index.vue';
  import { lastScopedOptions } from '@/components/business/crm-duplicate-check-drawer/config';
  import CrmTaskDrawer from '@/components/business/crm-task-drawer/index.vue';
  import MessageDrawer from '@/views/system/message/components/messageDrawer.vue';

  import { changeLocaleBackEnd } from '@/api/modules';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';

  import {
    ClueRouteEnum,
    ContractRouteEnum,
    CustomerRouteEnum,
    DashboardRouteEnum,
    OpportunityRouteEnum,
    ProductRouteEnum,
    SalesRouteEnum,
    SystemRouteEnum,
    WorkbenchRouteEnum,
  } from '@/enums/routeEnum';

  const CrmFollowDrawer = defineAsyncComponent(() => import('@/components/business/crm-follow-drawer/index.vue'));
  const CrmDuplicateCheckDrawer = defineAsyncComponent(
    () => import('@/components/business/crm-duplicate-check-drawer/index.vue')
  );

  const route = useRoute();
  const { loading } = useMessage();
  const { t } = useI18n();
  const { changeLocale, currentLocale } = useLocale(loading);
  const appStore = useAppStore();
  const userStore = useUserStore();

  const props = defineProps<{
    isPreview?: boolean;
    logo?: string;
  }>();

  const salesRouteRoots: string[] = [
    CustomerRouteEnum.CUSTOMER,
    ClueRouteEnum.CLUE_MANAGEMENT,
    OpportunityRouteEnum.OPPORTUNITY,
    ContractRouteEnum.CONTRACT,
    SalesRouteEnum.SALES_TEAM_ROOT,
    SalesRouteEnum.SALES_ANALYTICS_ROOT,
  ];

  const pageTitleMap: Record<string, string> = {
    [WorkbenchRouteEnum.WORKBENCH_INDEX]: '工作台',
    [DashboardRouteEnum.DASHBOARD_INDEX]: '数据洞察',
    [CustomerRouteEnum.CUSTOMER_INDEX]: '客户管理',
    [CustomerRouteEnum.CUSTOMER_CONTACT]: '联系人',
    [CustomerRouteEnum.CUSTOMER_OPEN_SEA]: '客户公海',
    [ClueRouteEnum.CLUE_MANAGEMENT_CLUE]: '销售线索',
    [ClueRouteEnum.CLUE_MANAGEMENT_POOL]: '线索池',
    [OpportunityRouteEnum.OPPORTUNITY_OPT]: '商机管理',
    [OpportunityRouteEnum.OPPORTUNITY_QUOTATION]: '报价',
    [ContractRouteEnum.CONTRACT_INDEX]: '合同',
    [ContractRouteEnum.CONTRACT_PAYMENT]: '回款计划',
    [ContractRouteEnum.CONTRACT_PAYMENT_RECORD]: '回款记录',
    [ProductRouteEnum.PRODUCT_PRO]: '产品',
    [ProductRouteEnum.PRODUCT_REQUIREMENT]: '需求管理',
    [ProductRouteEnum.PRODUCT_PRICE]: '价格表',
    [SalesRouteEnum.SALES_TEAM]: '团队与业绩',
    [SalesRouteEnum.SALES_ANALYTICS]: '数据分析',
    [SystemRouteEnum.SYSTEM_ORG]: '组织架构',
    [SystemRouteEnum.SYSTEM_ROLE]: '角色权限',
    [SystemRouteEnum.SYSTEM_MESSAGE]: '消息设置',
    [SystemRouteEnum.SYSTEM_PROCESS_INDEX]: '流程设置',
    [SystemRouteEnum.SYSTEM_BUSINESS]: '企业设置',
    [SystemRouteEnum.SYSTEM_LOG]: '系统日志',
  };

  const currentModuleTitle = computed(() => {
    const rootName = route.matched[0]?.name?.toString() ?? '';
    if (salesRouteRoots.includes(rootName)) {
      return '销售 CRM';
    }
    if ([ProductRouteEnum.PRODUCT].includes(rootName as ProductRouteEnum)) {
      return '产品需求';
    }
    if ([WorkbenchRouteEnum.WORKBENCH, DashboardRouteEnum.DASHBOARD].includes(rootName as any)) {
      return '主导航';
    }
    const locale = route.matched[0]?.meta?.locale as string | undefined;
    return locale ? t(locale) : 'StaRiver CRM';
  });

  const currentPageTitle = computed(() => {
    const routeName = route.name?.toString() ?? '';
    const locale = route.meta?.locale as string | undefined;
    return pageTitleMap[routeName] || (locale ? t(locale) : '工作台');
  });

  const showSearch = computed(() => lastScopedOptions.value.length > 0);
  const showBadge = computed(() => !appStore.messageInfo.read);
  const showMessageDrawer = ref(false);
  const initDuplicateCheckDrawer = ref(false);
  const showDuplicateCheckDrawer = ref(false);
  const initFollowDrawer = ref(false);
  const showFollowDrawer = ref(false);
  const initTaskDrawer = ref(false);
  const showTaskDrawer = ref(false);

  function changeLanguage(locale: LocaleType) {
    changeLocaleBackEnd(locale);
    changeLocale(locale);
  }

  function openDuplicateCheck() {
    if (!showSearch.value) return;
    initDuplicateCheckDrawer.value = true;
    showDuplicateCheckDrawer.value = true;
  }

  function openTask() {
    initTaskDrawer.value = true;
    showTaskDrawer.value = true;
  }

  function openFollow() {
    initFollowDrawer.value = true;
    showFollowDrawer.value = true;
  }

  function showMessage() {
    showMessageDrawer.value = true;
  }

  onBeforeMount(() => {
    appStore.getVersion();
    if (route.name !== WorkbenchRouteEnum.WORKBENCH_INDEX) {
      appStore.initMessage();
    }
    appStore.connectSystemMessageSSE(userStore.showSystemNotify);
    appStore.showSQLBot();
    userStore.initApiKeyList();
  });
</script>

<style lang="less" scoped>
  .stariver-layout-header {
    display: flex;
    height: 50px;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px 0 18px;
    background: #ffffff;
    border-bottom: 1px solid #e2e8f0;
  }

  .stariver-layout-header__breadcrumb {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 8px;
    color: #64748b;
    font-size: 13px;
    line-height: 20px;
  }

  .stariver-layout-header__slash {
    color: #cbd5e1;
  }

  .stariver-layout-header__current {
    color: #0f172a;
    font-weight: 600;
  }

  .stariver-layout-header__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stariver-layout-header__search {
    display: flex;
    width: 278px;
    height: 32px;
    align-items: center;
    gap: 8px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 8px 0 10px;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    cursor: pointer;
  }

  .stariver-layout-header__search:disabled {
    cursor: default;
    opacity: 0.65;
  }

  .stariver-layout-header__search span {
    flex: 1;
    overflow: hidden;
    text-align: left;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .stariver-layout-header__search kbd {
    border: 1px solid #cbd5e1;
    border-radius: 4px;
    padding: 1px 5px;
    background: #ffffff;
    color: #94a3b8;
    font-size: 11px;
    font-family: inherit;
    line-height: 16px;
  }

  .stariver-layout-header__icon {
    --n-width: 32px !important;
    --n-height: 32px !important;
    color: #475569;
  }
</style>

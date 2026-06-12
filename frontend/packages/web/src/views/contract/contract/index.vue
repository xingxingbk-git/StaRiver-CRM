<template>
  <StariverModulePage title="合同管理" count-label="履约" eyebrow="销售 CRM">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">全部</button>
        <button class="stariver-tab">待提审</button>
        <button class="stariver-tab">审批中</button>
        <button class="stariver-tab">履约中</button>
        <button class="stariver-tab">已完结</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">合同类型：全部</button>
        <button class="stariver-filter">负责人：全部</button>
      </div>
    </template>

    <div class="stariver-sales-stack">
      <StariverInsightStrip
        :metrics="contractMetrics"
        :process="contractProcess"
        :fields="contractFields"
        :rules="contractRules"
      />
      <div ref="contractCardRef" class="stariver-sales-table-panel">
        <CrmContractTable :fullscreen-target-ref="contractCardRef" @open-customer-drawer="handleOpenCustomerDrawer" />
      </div>
    </div>
  </StariverModulePage>
  <customerOverviewDrawer
    v-model:show="showCustomerOverviewDrawer"
    :source-id="activeSourceId"
    :readonly="isCustomerReadonly"
  />
  <openSeaOverviewDrawer
    v-model:show="showCustomerOpenseaOverviewDrawer"
    :source-id="activeSourceId"
    :readonly="isCustomerReadonly"
    :pool-id="poolId"
    :hidden-columns="hiddenColumns"
  />
  <businessTitleDrawer v-model:visible="showBusinessTitleDetailDrawer" :source-id="activeBusinessTitleSourceId" />
</template>

<script setup lang="ts">
  import { CluePoolItem } from '@lib/shared/models/system/module';

  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import businessTitleDrawer from '../businessTitle/components/detail.vue';
  import CrmContractTable from './components/contractTable.vue';
  import customerOverviewDrawer from '@/views/customer/components/customerOverviewDrawer.vue';
  import openSeaOverviewDrawer from '@/views/customer/components/openSeaOverviewDrawer.vue';

  import { getOpenSeaOptions } from '@/api/modules';
  import { hasAnyPermission } from '@/utils/permission';

  const contractCardRef = ref<HTMLElement | null>(null);

  const contractMetrics = [
    { label: '待提审', value: '6', hint: '合同主体和回款计划待补齐', tone: 'amber' },
    { label: '审批中', value: '9', hint: '法务/财务节点流转中', tone: 'blue' },
    { label: '履约中', value: '18', hint: '交付、开票、回款同步跟进', tone: 'emerald' },
    { label: '本月到期', value: '4', hint: '提前提醒负责人续签', tone: 'rose' },
  ];

  const contractProcess = ['合同草稿', '提交审批', '审批通过', '履约交付', '回款登记', '完结/到期'];
  const contractFields = ['合同类型', '付款条件', '交付周期', '回款计划', '特殊条款', '关联报价'];
  const contractRules = [
    '回款记录必须绑定回款计划',
    '合同履约状态围绕交付和回款推进',
    '合同客户、商机和报价保持链路追溯',
  ];

  const showCustomerOverviewDrawer = ref(false);
  const showCustomerOpenseaOverviewDrawer = ref(false);
  const poolId = ref<string>('');
  const activeSourceId = ref<string>('');
  const isCustomerReadonly = ref(false);
  function handleOpenCustomerDrawer(
    params: { customerId: string; inCustomerPool: boolean; poolId: string },
    readonly = false
  ) {
    activeSourceId.value = params.customerId;
    if (params.inCustomerPool) {
      showCustomerOpenseaOverviewDrawer.value = true;
      poolId.value = params.poolId;
    } else {
      showCustomerOverviewDrawer.value = true;
    }
    isCustomerReadonly.value = readonly;
  }

  const openSeaOptions = ref<CluePoolItem[]>([]);

  async function initOpenSeaOptions() {
    if (hasAnyPermission(['CUSTOMER_MANAGEMENT_POOL:READ'])) {
      const res = await getOpenSeaOptions();
      openSeaOptions.value = res;
    }
  }

  const showBusinessTitleDetailDrawer = ref(false);
  const activeBusinessTitleSourceId = ref<string>('');
  function handleOpenBusinessTitleDrawer(params: { id: string }) {
    activeBusinessTitleSourceId.value = params.id;
    showBusinessTitleDetailDrawer.value = true;
  }

  const hiddenColumns = computed<string[]>(() => {
    const openSeaSetting = openSeaOptions.value.find((item) => item.id === poolId.value);
    return openSeaSetting?.fieldConfigs.filter((item) => !item.enable).map((item) => item.fieldId) || [];
  });

  onBeforeMount(() => {
    initOpenSeaOptions();
  });
</script>

<style lang="less" scoped>
  .stariver-sales-stack {
    display: flex;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    gap: 12px;
  }

  .stariver-sales-table-panel {
    min-height: 320px;
    flex: 1;
    min-height: 0;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 16px;
  }

  .stariver-tabs,
  .stariver-filters {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stariver-tab,
  .stariver-filter {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 10px;
    background: #ffffff;
    color: #64748b;
    font-size: 13px;
    line-height: 30px;
    cursor: pointer;
  }

  .stariver-tab {
    border-color: transparent;
    background: transparent;
  }

  .stariver-tab--active {
    background: #eef2ff;
    color: #4f46e5;
    font-weight: 700;
  }

  .stariver-filter {
    background: #f8fafc;
    color: #475569;
  }
</style>

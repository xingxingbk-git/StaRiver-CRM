<template>
  <StariverModulePage title="商机管理" count-label="24个" eyebrow="销售 CRM">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">全部 · 24</button>
        <button class="stariver-tab">我负责的</button>
        <button class="stariver-tab">重点商机</button>
        <button class="stariver-tab">已成交</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">阶段：全部</button>
        <button class="stariver-filter">竞争强度：全部</button>
      </div>
    </template>

    <div class="stariver-sales-stack">
      <StariverInsightStrip
        :metrics="opportunityMetrics"
        :process="opportunityProcess"
        :fields="opportunityFields"
        :rules="opportunityRules"
      />
      <div ref="opportunityCardRef" class="stariver-sales-table-panel">
        <CrmOpportunityTable
          :fullscreen-target-ref="opportunityCardRef"
          :opensea-hidden-columns="hiddenColumns"
          :form-key="FormDesignKeyEnum.BUSINESS"
          @open-customer-drawer="handleOpenCustomerDrawer"
        />
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
</template>

<script setup lang="ts">
  import { useMessage } from 'naive-ui';

  import { FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { CluePoolItem } from '@lib/shared/models/system/module';

  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import CrmOpportunityTable from './components/opportunityTable.vue';
  import customerOverviewDrawer from '@/views/customer/components/customerOverviewDrawer.vue';
  import openSeaOverviewDrawer from '@/views/customer/components/openSeaOverviewDrawer.vue';

  import { getOpenSeaOptions } from '@/api/modules';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const Message = useMessage();
  const opportunityCardRef = ref<HTMLElement | null>(null);

  const opportunityMetrics = [
    { label: '推进中商机', value: '24', hint: '需求确认到商务谈判阶段', tone: 'blue' },
    { label: '重点商机', value: '8', hint: 'KA、战略客户优先跟进', tone: 'rose' },
    { label: '高竞争强度', value: '6', hint: '已记录竞品与差异化策略', tone: 'amber' },
    { label: '本月预计金额', value: '¥ 1,240w', hint: '基于阶段和报价估算', tone: 'emerald' },
  ];

  const opportunityProcess = ['线索转入', '需求确认', '方案沟通', '报价审批', '商务谈判', '赢单/输单'];
  const opportunityFields = ['主要竞品', '竞争强度', '差异化策略', '下一步行动', '预计金额', '预计签约'];
  const opportunityRules = ['商机阶段只允许顺序推进', '失败结项必须填写原因', '终态商机不可直接改回在途状态'];

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
      if (hasAnyPermission(['CUSTOMER_MANAGEMENT_POOL:READ'])) {
        showCustomerOpenseaOverviewDrawer.value = true;
        poolId.value = params.poolId;
      } else {
        Message.warning(t('opportunity.noOpenSeaPermission'));
      }
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

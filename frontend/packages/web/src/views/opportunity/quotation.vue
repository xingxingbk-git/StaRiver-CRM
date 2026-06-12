<template>
  <StariverModulePage title="报价管理" count-label="报价单" eyebrow="销售 CRM">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">全部</button>
        <button class="stariver-tab">待审批</button>
        <button class="stariver-tab">客户确认中</button>
        <button class="stariver-tab">已作废</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">客户：全部</button>
        <button class="stariver-filter">商机：全部</button>
      </div>
    </template>

    <div class="stariver-sales-stack">
      <StariverInsightStrip
        :metrics="quotationMetrics"
        :process="quotationProcess"
        :fields="quotationFields"
        :rules="quotationRules"
      />
      <div class="stariver-sales-table-panel">
        <quotationTable :formKey="FormDesignKeyEnum.OPPORTUNITY_QUOTATION" />
      </div>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import { FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';

  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import quotationTable from './components/quotation/quotationTable.vue';

  const quotationMetrics = [
    { label: '待审批', value: '7', hint: '报价金额和产品明细待确认', tone: 'amber' },
    { label: '客户确认中', value: '11', hint: '已发客户等待回签意见', tone: 'blue' },
    { label: '已通过', value: '15', hint: '可进入合同创建', tone: 'emerald' },
    { label: '作废/驳回', value: '3', hint: '保留版本记录便于追溯', tone: 'slate' },
  ];

  const quotationProcess = ['选择商机', '带出客户', '添加产品', '价格核算', '审批', '客户确认'];
  const quotationFields = ['关联商机', '客户名称', '付款条件', '交付周期', '产品数量', '单价/小计'];
  const quotationRules = ['报价必须关联商机', '客户字段由商机自动同步', '产品行按无税单价、数量和折扣核算'];
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

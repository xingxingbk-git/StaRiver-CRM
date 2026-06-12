<template>
  <StariverModulePage title="销售线索" count-label="18条" eyebrow="销售 CRM">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">全部 · 18</button>
        <button class="stariver-tab">待分配</button>
        <button class="stariver-tab">我负责的</button>
        <button class="stariver-tab">已转商机</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">来源：全部</button>
        <button class="stariver-filter">预算：全部</button>
      </div>
    </template>

    <div class="stariver-sales-stack">
      <StariverInsightStrip :metrics="clueMetrics" :process="clueProcess" :fields="clueFields" :rules="clueRules" />
      <div class="stariver-sales-table-panel">
        <ClueTable :table-form-key="FormDesignKeyEnum.CLUE" />
      </div>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import { FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';

  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import ClueTable from './components/clueTable.vue';

  const clueMetrics = [
    { label: '待分配线索', value: '5', hint: '官网表单、展会名片优先处理', tone: 'amber' },
    { label: '跟进中', value: '9', hint: '已建立联系人与预算信息', tone: 'blue' },
    { label: '已转商机', value: '4', hint: '完成客户与商机建档', tone: 'emerald' },
    { label: '本周新增', value: '18', hint: '按来源和负责人追踪质量', tone: 'indigo' },
  ];

  const clueProcess = ['线索录入', '线索分配', '初步跟进', '客户确认', '转商机'];
  const clueFields = ['线索来源', '行业', '职务', '邮箱', '预算范围', '跟进方式'];
  const clueRules = ['跟进记录提交后不可修改', '转商机时必须补齐客户与联系人', '预算范围用于后续报价和商机优先级判断'];
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

  :deep(.n-tabs-scroll-padding) {
    width: 16px !important;
  }
</style>

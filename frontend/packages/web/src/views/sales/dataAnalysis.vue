<template>
  <StariverModulePage title="数据分析" count-label="实时" eyebrow="销售 CRM">
    <template #actions>
      <button class="sr-btn sr-btn--ghost">导出图表</button>
      <button class="sr-btn sr-btn--primary">新建看板</button>
    </template>

    <template #toolbar>
      <div class="sr-tabs">
        <button class="sr-tab sr-tab--active">销售总览</button>
        <button class="sr-tab">客户分析</button>
        <button class="sr-tab">商机漏斗</button>
        <button class="sr-tab">合同回款</button>
      </div>
      <div class="sr-filters">
        <button class="sr-filter">周期：近 30 天</button>
        <button class="sr-filter">大区：全部</button>
      </div>
    </template>

    <div class="analysis-page">
      <section class="metric-grid">
        <article v-for="item in metrics" :key="item.label" class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </article>
      </section>

      <section class="content-grid">
        <div class="sr-panel funnel-panel">
          <div class="sr-panel__head">
            <div>
              <h2>商机转化漏斗</h2>
              <p>按 StaRiver 第一阶段销售流程统计阶段推进情况</p>
            </div>
          </div>
          <div class="funnel-list">
            <div v-for="item in funnel" :key="item.label" class="funnel-row">
              <span>{{ item.label }}</span>
              <div class="bar-track">
                <i :style="{ width: item.width }"></i>
              </div>
              <strong>{{ item.value }}</strong>
              <small>{{ item.rate }}</small>
            </div>
          </div>
        </div>

        <div class="sr-panel">
          <div class="sr-panel__head">
            <div>
              <h2>线索来源质量</h2>
              <p>关注客户、售前、内部、竞品四类来源表现</p>
            </div>
          </div>
          <div class="source-list">
            <article v-for="item in sources" :key="item.label">
              <div class="source-title">
                <span>{{ item.label }}</span>
                <strong>{{ item.count }}</strong>
              </div>
              <div class="bar-track">
                <i :style="{ width: item.width }"></i>
              </div>
              <small>转商机率 {{ item.rate }} · 成交金额 {{ item.amount }}</small>
            </article>
          </div>
        </div>
      </section>

      <section class="sr-panel">
        <div class="sr-panel__head">
          <div>
            <h2>重点客户洞察</h2>
            <p>结合客户等级、商机金额、合同状态和最近跟进判断优先级</p>
          </div>
        </div>
        <div class="customer-table">
          <div class="customer-table__row customer-table__row--head">
            <span>客户</span>
            <span>等级</span>
            <span>行业</span>
            <span>活跃商机</span>
            <span>报价金额</span>
            <span>合同状态</span>
            <span>下一步行动</span>
          </div>
          <div v-for="item in customers" :key="item.name" class="customer-table__row">
            <div class="customer-cell">
              <strong>{{ item.name }}</strong>
              <small>{{ item.owner }}</small>
            </div>
            <span
              ><em class="sr-tag">{{ item.level }}</em></span
            >
            <span>{{ item.industry }}</span>
            <span>{{ item.opportunities }}</span>
            <span>{{ item.quote }}</span>
            <span>{{ item.contract }}</span>
            <span>{{ item.action }}</span>
          </div>
        </div>
      </section>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  const metrics = [
    { label: '销售线索', value: '118', hint: '有效线索 84 条' },
    { label: '商机金额', value: '¥928万', hint: '重点商机 24 个' },
    { label: '报价金额', value: '¥572万', hint: '待客户确认 6 份' },
    { label: '合同金额', value: '¥486万', hint: '履约中 12 份' },
  ];

  const funnel = [
    { label: '需求明确', value: 64, rate: '100%', width: '100%' },
    { label: '方案验证', value: 46, rate: '72%', width: '72%' },
    { label: '立项汇报', value: 31, rate: '48%', width: '48%' },
    { label: '商务采购', value: 19, rate: '30%', width: '30%' },
    { label: '履约合同', value: 12, rate: '19%', width: '19%' },
  ];

  const sources = [
    { label: '客户', count: 46, rate: '38%', amount: '¥212万', width: '86%' },
    { label: '售前', count: 31, rate: '29%', amount: '¥146万', width: '62%' },
    { label: '内部', count: 24, rate: '21%', amount: '¥88万', width: '48%' },
    { label: '竞品', count: 17, rate: '16%', amount: '¥41万', width: '34%' },
  ];

  const customers = [
    {
      name: '华东能源集团',
      owner: '陆明',
      level: 'A',
      industry: '能源',
      opportunities: 3,
      quote: '¥126万',
      contract: '审批中',
      action: '补充付款条件',
    },
    {
      name: '南方智造科技',
      owner: '周雨',
      level: 'A',
      industry: '制造',
      opportunities: 2,
      quote: '¥94万',
      contract: '履约中',
      action: '确认交付周期',
    },
    {
      name: '北辰云计算',
      owner: '江一',
      level: 'B',
      industry: '互联网',
      opportunities: 2,
      quote: '¥78万',
      contract: '待提审',
      action: '推进方案验证',
    },
  ];
</script>

<style lang="less" scoped>
  .analysis-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .metric-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .metric-card,
  .sr-panel {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 1px 2px rgb(15 23 42 / 4%);
  }

  .metric-card {
    display: flex;
    min-height: 86px;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    padding: 14px 16px;
  }

  .metric-card span,
  .metric-card small,
  .sr-panel__head p,
  .source-list small,
  .customer-cell small,
  .funnel-row small {
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }

  .metric-card strong {
    color: #0f172a;
    font-size: 24px;
    line-height: 30px;
  }

  .content-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 380px;
    gap: 16px;
  }

  .sr-panel {
    min-width: 0;
    overflow: hidden;
  }

  .sr-panel__head {
    display: flex;
    height: 64px;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    border-bottom: 1px solid #e2e8f0;
  }

  .sr-panel__head h2 {
    margin: 0;
    color: #0f172a;
    font-size: 15px;
    font-weight: 700;
    line-height: 22px;
  }

  .sr-panel__head p {
    margin: 2px 0 0;
  }

  .sr-btn,
  .sr-filter,
  .sr-tab {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 12px;
    background: #ffffff;
    color: #334155;
    font-size: 13px;
    line-height: 30px;
    cursor: pointer;
  }

  .sr-btn--primary {
    border-color: #4f46e5;
    background: #4f46e5;
    color: #ffffff;
  }

  .sr-btn--ghost,
  .sr-filter {
    background: #f8fafc;
  }

  .sr-tabs,
  .sr-filters {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .sr-tab {
    border-color: transparent;
    background: transparent;
    color: #64748b;
  }

  .sr-tab--active {
    background: #eef2ff;
    color: #4f46e5;
    font-weight: 700;
  }

  .funnel-list,
  .source-list {
    display: flex;
    flex-direction: column;
  }

  .funnel-list {
    gap: 14px;
    padding: 18px 16px 20px;
  }

  .funnel-row {
    display: grid;
    grid-template-columns: 80px minmax(0, 1fr) 48px 44px;
    align-items: center;
    gap: 12px;
    color: #334155;
    font-size: 13px;
  }

  .funnel-row strong,
  .source-title strong {
    color: #0f172a;
    font-size: 14px;
    line-height: 20px;
  }

  .bar-track {
    height: 8px;
    overflow: hidden;
    border-radius: 999px;
    background: #e2e8f0;
  }

  .bar-track i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, #4f46e5, #06b6d4);
  }

  .source-list {
    gap: 12px;
    padding: 14px;
  }

  .source-list article {
    display: flex;
    min-height: 86px;
    flex-direction: column;
    justify-content: center;
    gap: 8px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 12px;
    background: #f8fafc;
  }

  .source-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #334155;
    font-size: 13px;
  }

  .customer-table {
    min-width: 860px;
  }

  .customer-table__row {
    display: grid;
    min-height: 56px;
    grid-template-columns: minmax(180px, 1.5fr) 70px 110px 90px 110px 100px minmax(140px, 1fr);
    align-items: center;
    gap: 12px;
    padding: 0 16px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
    font-size: 13px;
  }

  .customer-table__row--head {
    min-height: 44px;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
  }

  .customer-cell {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 2px;
  }

  .customer-cell strong {
    color: #0f172a;
    font-size: 13px;
    line-height: 18px;
  }

  .sr-tag {
    display: inline-flex;
    height: 22px;
    align-items: center;
    border-radius: 999px;
    padding: 0 8px;
    background: #eef2ff;
    color: #4f46e5;
    font-style: normal;
    font-size: 12px;
    font-weight: 700;
  }

  @media (max-width: 1180px) {
    .metric-grid,
    .content-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

<template>
  <StariverModulePage title="团队与业绩" count-label="8人" eyebrow="销售 CRM">
    <template #actions>
      <button class="sr-btn sr-btn--ghost">导出报表</button>
      <button class="sr-btn sr-btn--primary">设置目标</button>
    </template>

    <template #toolbar>
      <div class="sr-tabs">
        <button class="sr-tab sr-tab--active">本月</button>
        <button class="sr-tab">本季度</button>
        <button class="sr-tab">本年度</button>
      </div>
      <div class="sr-filters">
        <button class="sr-filter">团队：全部</button>
        <button class="sr-filter">负责人：全部</button>
      </div>
    </template>

    <div class="team-page">
      <section class="metric-grid">
        <article v-for="item in metrics" :key="item.label" class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </article>
      </section>

      <section class="content-grid">
        <div class="sr-panel">
          <div class="sr-panel__head">
            <div>
              <h2>销售负责人看板</h2>
              <p>客户、线索、商机、报价和合同转化概览</p>
            </div>
          </div>

          <div class="team-table">
            <div class="team-table__row team-table__row--head">
              <span>负责人</span>
              <span>客户</span>
              <span>线索</span>
              <span>商机</span>
              <span>报价金额</span>
              <span>合同金额</span>
              <span>目标完成</span>
            </div>
            <div v-for="item in owners" :key="item.name" class="team-table__row">
              <div class="owner-cell">
                <span>{{ item.avatar }}</span>
                <div>
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.team }}</small>
                </div>
              </div>
              <span>{{ item.customers }}</span>
              <span>{{ item.leads }}</span>
              <span>{{ item.opportunities }}</span>
              <span>{{ item.quoteAmount }}</span>
              <span>{{ item.contractAmount }}</span>
              <div class="progress-cell">
                <div class="progress-track">
                  <i :style="{ width: item.progress }"></i>
                </div>
                <span>{{ item.progress }}</span>
              </div>
            </div>
          </div>
        </div>

        <aside class="sr-panel pipeline-panel">
          <div class="sr-panel__head sr-panel__head--compact">
            <div>
              <h2>关键动作</h2>
              <p>需要团队经理关注的阶段变化</p>
            </div>
          </div>
          <div class="action-list">
            <article v-for="item in actions" :key="item.title">
              <span class="sr-status">{{ item.type }}</span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.note }}</small>
            </article>
          </div>
        </aside>
      </section>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  const metrics = [
    { label: '新增客户', value: '32', hint: '较上月 +12%' },
    { label: '新增线索', value: '118', hint: '待分配 18 条' },
    { label: '活跃商机', value: '64', hint: '重点商机 24 个' },
    { label: '合同金额', value: '¥486万', hint: '目标完成 72%' },
  ];

  const owners = [
    {
      avatar: '陆',
      name: '陆明',
      team: '华东大区',
      customers: 18,
      leads: 42,
      opportunities: 16,
      quoteAmount: '¥126万',
      contractAmount: '¥98万',
      progress: '78%',
    },
    {
      avatar: '周',
      name: '周雨',
      team: '华南大区',
      customers: 15,
      leads: 36,
      opportunities: 14,
      quoteAmount: '¥94万',
      contractAmount: '¥82万',
      progress: '69%',
    },
    {
      avatar: '江',
      name: '江一',
      team: '行业客户组',
      customers: 11,
      leads: 23,
      opportunities: 12,
      quoteAmount: '¥78万',
      contractAmount: '¥63万',
      progress: '61%',
    },
    {
      avatar: '林',
      name: '林岚',
      team: '生态伙伴组',
      customers: 9,
      leads: 17,
      opportunities: 8,
      quoteAmount: '¥52万',
      contractAmount: '¥39万',
      progress: '48%',
    },
  ];

  const actions = [
    { type: '线索', title: '18 条线索待分配', note: '其中客户来源 9 条，售前来源 5 条' },
    { type: '商机', title: '4 个商机超过 7 天未跟进', note: '需要补充下一步行动和跟进时间' },
    { type: '合同', title: '3 份合同本周进入审批', note: '履约计划和付款计划已提交' },
    { type: '报价', title: '2 份报价等待客户确认', note: '付款条件和交付周期已补齐' },
  ];
</script>

<style lang="less" scoped>
  .team-page {
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
  .owner-cell small,
  .action-list small {
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
    grid-template-columns: minmax(0, 1fr) 330px;
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

  .sr-panel__head--compact {
    height: 62px;
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

  .team-table {
    min-width: 780px;
  }

  .team-table__row {
    display: grid;
    min-height: 58px;
    grid-template-columns: minmax(170px, 1.5fr) 70px 70px 70px 110px 110px minmax(130px, 1fr);
    align-items: center;
    gap: 12px;
    padding: 0 16px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
    font-size: 13px;
  }

  .team-table__row--head {
    min-height: 44px;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
  }

  .owner-cell {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 10px;
  }

  .owner-cell > span {
    display: inline-flex;
    width: 32px;
    height: 32px;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: #eef2ff;
    color: #4f46e5;
    font-size: 13px;
    font-weight: 700;
  }

  .owner-cell strong,
  .action-list strong {
    display: block;
    color: #0f172a;
    font-size: 13px;
    line-height: 18px;
  }

  .progress-cell {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 38px;
    align-items: center;
    gap: 8px;
  }

  .progress-track {
    height: 6px;
    overflow: hidden;
    border-radius: 999px;
    background: #e2e8f0;
  }

  .progress-track i {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: #4f46e5;
  }

  .pipeline-panel {
    align-self: start;
  }

  .action-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px;
  }

  .action-list article {
    display: flex;
    min-height: 84px;
    flex-direction: column;
    gap: 7px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 12px;
    background: #f8fafc;
  }

  .sr-status {
    display: inline-flex;
    width: fit-content;
    height: 22px;
    align-items: center;
    border-radius: 999px;
    padding: 0 8px;
    background: #eef2ff;
    color: #4f46e5;
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

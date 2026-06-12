<template>
  <StariverModulePage title="需求管理" count-label="6条" eyebrow="产品需求">
    <template #actions>
      <button class="sr-btn sr-btn--ghost">批量导入</button>
      <button class="sr-btn sr-btn--primary">+ 提报需求</button>
    </template>

    <template #toolbar>
      <div class="sr-tabs">
        <button class="sr-tab sr-tab--active">全部 · 6</button>
        <button class="sr-tab">待评审 · 2</button>
        <button class="sr-tab">开发中 · 2</button>
        <button class="sr-tab">待发布 · 1</button>
      </div>
      <div class="sr-filters">
        <button class="sr-filter">产品：全部</button>
        <button class="sr-filter">优先级：全部</button>
        <button class="sr-filter">来源：全部</button>
      </div>
    </template>

    <div class="requirement-page">
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
              <h2>需求池</h2>
              <p>按产品、来源、优先级和交付阶段统一跟踪</p>
            </div>
            <button class="sr-link-btn">查看路线图</button>
          </div>

          <div class="requirement-table">
            <div class="requirement-table__row requirement-table__row--head">
              <span>需求标题</span>
              <span>目标产品</span>
              <span>来源</span>
              <span>优先级</span>
              <span>状态</span>
              <span>期望上线</span>
            </div>
            <div v-for="item in requirements" :key="item.title" class="requirement-table__row">
              <div class="requirement-title">
                <strong>{{ item.title }}</strong>
                <small>{{ item.type }} · {{ item.owner }}</small>
              </div>
              <span>{{ item.product }}</span>
              <span>{{ item.source }}</span>
              <span>
                <em class="sr-tag" :class="`sr-tag--${item.priorityType}`">{{ item.priority }}</em>
              </span>
              <span>
                <em class="sr-status">{{ item.status }}</em>
              </span>
              <span>{{ item.release }}</span>
            </div>
          </div>
        </div>

        <aside class="sr-panel requirement-detail">
          <div class="detail-head">
            <span class="sr-tag sr-tag--high">高优先级</span>
            <h2>客户成功看板增加合同风险提醒</h2>
            <p>来源客户：华东能源集团；关联销售线索 L-2026-018。</p>
          </div>

          <div class="detail-section">
            <h3>验收标准</h3>
            <p>合同到期前 30 天自动提醒负责人，并在客户详情页展示风险等级、下一步行动和最近跟进记录。</p>
          </div>

          <div class="flow-list">
            <div v-for="step in flow" :key="step.label" class="flow-step" :class="{ 'flow-step--done': step.done }">
              <span></span>
              <div>
                <strong>{{ step.label }}</strong>
                <small>{{ step.note }}</small>
              </div>
            </div>
          </div>
        </aside>
      </section>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  const metrics = [
    { label: '总需求', value: '36', hint: '本月新增 6 条' },
    { label: '待评审', value: '8', hint: '客户来源 5 条' },
    { label: '开发中', value: '11', hint: '关联版本 3 个' },
    { label: '待发布', value: '4', hint: '下个版本合并' },
  ];

  const requirements = [
    {
      title: '客户成功看板增加合同风险提醒',
      product: 'StaRiver AI 中台',
      source: '客户',
      priority: '高',
      priorityType: 'high',
      status: '产品设计',
      release: '2026-07-30',
      type: '功能优化',
      owner: '陆明',
    },
    {
      title: '销售线索转换时保留预算范围',
      product: 'StaRiver CRM',
      source: '售前',
      priority: '高',
      priorityType: 'high',
      status: '技术评审',
      release: '2026-07-15',
      type: '体验优化',
      owner: '周雨',
    },
    {
      title: '报价单支持交付周期和付款条件模板',
      product: 'StaRiver CRM',
      source: '内部',
      priority: '中',
      priorityType: 'medium',
      status: '开发中',
      release: '2026-08-12',
      type: '功能优化',
      owner: '江一',
    },
    {
      title: '需求池列表增加竞品来源标识',
      product: 'OptiQA 智能质检',
      source: '竞品',
      priority: '中',
      priorityType: 'medium',
      status: '需求评审',
      release: '2026-08-28',
      type: '性能改进',
      owner: '林岚',
    },
  ];

  const flow = [
    { label: '提交', note: '华东能源集团 · 2026-06-01', done: true },
    { label: '需求池', note: '已完成初筛并补充客户场景', done: true },
    { label: '需求评审', note: '确认进入 2.4 版本候选', done: true },
    { label: '产品设计', note: '正在完善交互与字段定义', done: true },
    { label: '技术评审', note: '待研发负责人评估影响范围', done: false },
    { label: '开发', note: '未开始', done: false },
    { label: '测试', note: '未开始', done: false },
    { label: '发布', note: '未开始', done: false },
  ];
</script>

<style lang="less" scoped>
  .requirement-page {
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
  .detail-head p,
  .detail-section p,
  .flow-step small {
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
    grid-template-columns: minmax(0, 1fr) 340px;
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

  .sr-panel__head h2,
  .detail-head h2,
  .detail-section h3 {
    margin: 0;
    color: #0f172a;
    font-weight: 700;
  }

  .sr-panel__head h2 {
    font-size: 15px;
    line-height: 22px;
  }

  .sr-panel__head p {
    margin: 2px 0 0;
  }

  .sr-btn,
  .sr-link-btn,
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

  .sr-link-btn {
    border-color: transparent;
    background: transparent;
    color: #4f46e5;
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

  .requirement-table {
    min-width: 760px;
  }

  .requirement-table__row {
    display: grid;
    min-height: 56px;
    grid-template-columns: minmax(230px, 1.7fr) minmax(130px, 1fr) 86px 86px 96px 110px;
    align-items: center;
    gap: 12px;
    padding: 0 16px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
    font-size: 13px;
  }

  .requirement-table__row--head {
    min-height: 44px;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
  }

  .requirement-title {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 2px;
  }

  .requirement-title strong {
    overflow: hidden;
    color: #0f172a;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .requirement-title small {
    color: #94a3b8;
    font-size: 12px;
  }

  .sr-tag,
  .sr-status {
    display: inline-flex;
    height: 22px;
    align-items: center;
    border-radius: 999px;
    padding: 0 8px;
    font-style: normal;
    font-size: 12px;
    font-weight: 700;
  }

  .sr-tag--high {
    background: #fee2e2;
    color: #b91c1c;
  }

  .sr-tag--medium {
    background: #fef3c7;
    color: #b45309;
  }

  .sr-status {
    background: #e0f2fe;
    color: #0369a1;
  }

  .requirement-detail {
    padding: 16px;
  }

  .detail-head {
    border-bottom: 1px solid #e2e8f0;
    padding-bottom: 16px;
  }

  .detail-head h2 {
    margin-top: 12px;
    font-size: 17px;
    line-height: 24px;
  }

  .detail-head p,
  .detail-section p {
    margin: 8px 0 0;
  }

  .detail-section {
    padding: 16px 0;
    border-bottom: 1px solid #e2e8f0;
  }

  .detail-section h3 {
    font-size: 13px;
    line-height: 20px;
  }

  .flow-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding-top: 16px;
  }

  .flow-step {
    display: grid;
    grid-template-columns: 14px minmax(0, 1fr);
    gap: 10px;
  }

  .flow-step > span {
    width: 10px;
    height: 10px;
    margin-top: 4px;
    border: 2px solid #cbd5e1;
    border-radius: 999px;
    background: #ffffff;
  }

  .flow-step--done > span {
    border-color: #4f46e5;
    background: #4f46e5;
  }

  .flow-step strong {
    display: block;
    color: #0f172a;
    font-size: 13px;
    line-height: 18px;
  }

  @media (max-width: 1180px) {
    .metric-grid,
    .content-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

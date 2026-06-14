<template>
  <section class="sr-product-detail">
    <div v-if="product.id" class="sr-product-detail__inner">
      <header class="sr-product-hero">
        <div class="sr-product-hero__main">
          <div class="sr-product-hero__tags">
            <span class="sr-tag sr-tag--brand">{{ product.code || 'StaRiver' }}</span>
            <span class="sr-tag sr-tag--success">当前 {{ product.version || 'v3.8.2' }}</span>
            <span class="sr-tag sr-tag--primary">→ {{ product.nextVersion || 'v4.0' }}</span>
          </div>
          <div class="sr-product-hero__title-row">
            <h1>{{ product.name || 'StaRiver AI 中台' }}</h1>
            <button class="sr-icon-action" title="编辑产品" @click="handleEdit">
              <span v-html="editIcon"></span>
            </button>
            <button class="sr-icon-action" title="删除产品">
              <span v-html="deleteIcon"></span>
            </button>
          </div>
          <p class="sr-product-hero__desc">
            {{ product.description || '面向工业场景的 AI 基础设施平台，提供数据接入、特征管理、模型训练与推理编排' }}
          </p>
        </div>
        <button class="sr-primary-action">+ 提报需求</button>
      </header>

      <nav class="sr-tabs" aria-label="产品详情页签">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['sr-tab', { 'sr-tab--active': activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>

      <div class="sr-tab-content">
        <div v-if="activeTab === 'overview'" class="sr-overview">
          <div class="sr-stat-grid">
            <div v-for="item in statCards" :key="item.label" class="sr-stat-card">
              <div class="sr-stat-card__label">{{ item.label }}</div>
              <div class="sr-stat-card__value" :class="item.tone ? `sr-stat-card__value--${item.tone}` : ''">
                {{ item.value }}
              </div>
            </div>
          </div>

          <section class="sr-panel">
            <h2>使用客户</h2>
            <div class="sr-customer-table">
              <div class="sr-customer-table__head">
                <span>标题</span>
                <span>行业</span>
                <span>联系人</span>
                <span>关联项目</span>
                <span>合作起始</span>
              </div>
              <div v-for="row in customerRows" :key="row.name" class="sr-customer-table__row">
                <strong>{{ row.name }}</strong>
                <span
                  ><em class="sr-chip">{{ row.industry }}</em></span
                >
                <span>{{ row.contact }}</span>
                <span>{{ row.project }}</span>
                <span class="sr-customer-table__date">{{ row.start }}</span>
              </div>
            </div>
          </section>
        </div>

        <section v-else-if="activeTab === 'modules'" class="sr-panel">
          <h2>模块架构</h2>
          <div class="sr-module-grid">
            <div v-for="item in moduleRows" :key="item.name" class="sr-module-card">
              <div class="sr-module-card__name">{{ item.name }}</div>
              <div class="sr-module-card__desc">{{ item.desc }}</div>
            </div>
          </div>
        </section>

        <section v-else-if="activeTab === 'requirements'" class="sr-panel">
          <h2>需求池</h2>
          <div class="sr-simple-table">
            <div class="sr-simple-table__head">
              <span>需求</span>
              <span>来源客户</span>
              <span>状态</span>
            </div>
            <div v-for="item in requirementRows" :key="item.name" class="sr-simple-table__row">
              <span>{{ item.name }}</span>
              <span>{{ item.customer }}</span>
              <span
                ><em class="sr-chip">{{ item.status }}</em></span
              >
            </div>
          </div>
        </section>

        <section v-else-if="activeTab === 'roadmap'" class="sr-panel">
          <div class="sr-panel__header">
            <h2>版本路线图</h2>
            <button class="sr-secondary-action">+ 新增版本</button>
          </div>
          <div class="sr-simple-table">
            <div class="sr-simple-table__head sr-simple-table__head--roadmap">
              <span>版本</span>
              <span>发布日期</span>
              <span>状态</span>
              <span>待发布需求</span>
            </div>
            <div v-for="row in productRoadmap" :key="row.id" class="sr-simple-table__row sr-simple-table__row--roadmap">
              <span>{{ row.version }}</span>
              <span>{{ row.releaseDate }}</span>
              <span
                ><em class="sr-chip">{{ row.status }}</em></span
              >
              <span>{{ row.pendingCount }}</span>
            </div>
          </div>
        </section>

        <section v-else class="sr-panel">
          <div class="sr-panel__header">
            <h2>产品文档</h2>
            <button class="sr-secondary-action">上传文档</button>
          </div>
          <div class="sr-doc-list">
            <div v-for="doc in docRows" :key="doc.name" class="sr-doc-item">
              <span>{{ doc.name }}</span>
              <em>{{ doc.updated }}</em>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div v-else class="sr-detail-loading">加载中...</div>
  </section>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';

  import deleteIcon from '@/assets/icons/project/delete.svg?raw';
  import editIcon from '@/assets/icons/project/edit.svg?raw';

  import { getProductDetail, getRoadmap } from '@/api/modules/productMock';

  const route = useRoute();
  const router = useRouter();

  const product = ref<any>({});
  const activeTab = ref('overview');
  const productRoadmap = ref<any[]>([]);

  const tabs = [
    { key: 'overview', label: '总览' },
    { key: 'modules', label: '模块架构' },
    { key: 'requirements', label: '需求池' },
    { key: 'roadmap', label: '路线图' },
    { key: 'docs', label: '产品文档' },
  ];

  const statCards = computed(() => [
    { label: '功能模块', value: product.value.moduleCount ?? 12 },
    { label: '需求池', value: product.value.requirementCount ?? 25, tone: 'warning' },
    { label: '待发版本', value: product.value.nextVersion || 'v4.0', tone: 'primary' },
    { label: '产品负责人', value: product.value.productOwner || '陈立文', tone: 'name' },
    { label: '研发负责人', value: product.value.devOwner || '周志远', tone: 'name' },
  ]);

  const customerRows = [
    {
      name: '中国工商银行总行',
      industry: '金融',
      contact: '郑经理 · 科技部',
      project: '智能运营监控中心',
      start: '2026-01',
    },
    {
      name: '东方电气集团有限公司',
      industry: '能源 · 装备制造',
      contact: '刘总 · IT 总监',
      project: '智慧运维平台',
      start: '2025-11',
    },
    {
      name: '宝钢集团股份有限公司',
      industry: '钢铁',
      contact: '李工 · 质检主管',
      project: '智能质检平台',
      start: '2025-08',
    },
    {
      name: '国家电网华东分部',
      industry: '能源',
      contact: '王处',
      project: '预测性维护试点',
      start: '2024-01',
    },
  ];

  const moduleRows = [
    { name: '数据接入', desc: '统一采集工业设备、业务系统与文件数据。' },
    { name: '特征管理', desc: '沉淀可复用的指标、标签与模型特征。' },
    { name: '模型训练', desc: '支持训练任务编排、评估和版本归档。' },
    { name: '推理服务', desc: '面向业务场景提供在线推理与服务监控。' },
  ];

  const requirementRows = [
    { name: '设备异常预测模型可配置', customer: '国家电网华东分部', status: '规划中' },
    { name: '质检图片批量标注流程', customer: '宝钢集团股份有限公司', status: '开发中' },
    { name: '模型训练任务审计', customer: '中国工商银行总行', status: '待评审' },
  ];

  const docRows = [
    { name: 'StaRiver AI 中台产品白皮书.pdf', updated: '2026-05-18' },
    { name: 'v4.0 版本规划说明.docx', updated: '2026-05-22' },
  ];

  function getFallbackProduct(id: string) {
    return {
      id,
      name: 'StaRiver AI 中台',
      code: 'STARIVER',
      description: '面向工业场景的 AI 基础设施平台，提供数据接入、特征管理、模型训练与推理编排',
      version: 'v3.8.2',
      nextVersion: 'v4.0',
      productOwner: '陈立文',
      devOwner: '周志远',
      moduleCount: 12,
      requirementCount: 25,
    };
  }

  onMounted(async () => {
    const id = route.params.id as string;
    const tabParam = route.query.tab as string;
    if (tabParam && tabs.some((t) => t.key === tabParam)) {
      activeTab.value = tabParam;
    }
    if (id) {
      try {
        const [data, roadmap] = await Promise.all([getProductDetail(id), getRoadmap()]);
        product.value = data || getFallbackProduct(id);
        const matchedRoadmap = (roadmap || []).filter((r: any) => r.productId === id);
        productRoadmap.value = matchedRoadmap.length
          ? matchedRoadmap
          : (roadmap || []).filter((r: any) => r.productId === 'p1');
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('获取产品详情失败', e);
        product.value = getFallbackProduct(id);
      }
    }
  });

  function handleEdit() {
    router.push({
      name: 'productCreate',
      query: {
        mode: 'edit',
        id: product.value.id,
      },
    });
  }
</script>

<style lang="less" scoped>
  .sr-product-detail {
    height: 100%;
    min-height: 0;
    overflow: auto;
    background: #f3f4f6;
    color: #0f172a;
  }

  .sr-product-detail__inner {
    width: 100%;
    padding: 0 24px 24px;
  }

  .sr-product-hero {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 24px;
    margin: 0 -24px;
    padding: 18px 24px 10px;
    background: #ffffff;
  }

  .sr-product-hero__main {
    min-width: 0;
  }

  .sr-product-hero__tags {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  .sr-tag,
  .sr-chip {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    padding: 0 6px;
    height: 18px;
    font-size: 11px;
    font-weight: 600;
    font-style: normal;
    line-height: 14px;
    white-space: nowrap;
  }

  .sr-tag--brand {
    background: #eef2ff;
    color: #4f46e5;
  }

  .sr-tag--success {
    background: rgba(22, 163, 74, 0.1);
    color: #16a34a;
  }

  .sr-tag--primary {
    background: rgba(79, 70, 229, 0.1);
    color: #4f46e5;
  }

  .sr-product-hero__title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .sr-product-hero__title-row h1 + .sr-icon-action {
    margin-left: 2px;
  }

  .sr-product-hero__title-row h1 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
  }

  .sr-icon-action {
    display: inline-flex;
    width: 16px;
    height: 16px;
    align-items: center;
    justify-content: center;
    border: 0;
    background: transparent;
    color: #64748b;
    padding: 0;
    cursor: pointer;
    --stroke-0: currentColor;
    --fill-0: currentColor;
    transition: color 0.16s ease;
  }

  .sr-icon-action:hover {
    color: #4f46e5;
  }

  .sr-icon-action span,
  .sr-icon-action svg {
    display: block;
    width: 16px;
    height: 16px;
  }

  .sr-icon-action svg [stroke]:not([stroke='none']) {
    stroke: currentColor !important;
  }

  .sr-icon-action svg [fill]:not([fill='none']) {
    fill: currentColor !important;
  }

  .sr-product-hero__desc {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 13px;
    line-height: 18px;
  }

  .sr-primary-action,
  .sr-secondary-action {
    display: inline-flex;
    height: 32px;
    align-items: center;
    justify-content: center;
    border: 1px solid #0f172a;
    border-radius: 6px;
    padding: 0 14px;
    background: #0f172a;
    color: #ffffff;
    font-size: 12px;
    font-weight: 600;
    white-space: nowrap;
    cursor: pointer;
  }

  .sr-secondary-action {
    height: 28px;
    padding: 0 12px;
  }

  .sr-tabs {
    display: flex;
    height: 42px;
    align-items: flex-end;
    gap: 0;
    margin: 0 -24px;
    border-bottom: 1px solid #e2e8f0;
    padding: 0 24px;
    background: #ffffff;
  }

  .sr-tab {
    height: 40px;
    border: 0;
    border-bottom: 2px solid transparent;
    padding: 0 16px;
    background: transparent;
    color: #64748b;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
  }

  .sr-tab--active {
    border-bottom-color: #4f46e5;
    color: #4f46e5;
  }

  .sr-tab-content {
    padding-top: 14px;
  }

  .sr-overview {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .sr-stat-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 10px;
  }

  .sr-stat-card {
    display: flex;
    height: 72px;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 0 16px;
  }

  .sr-stat-card__label {
    color: #64748b;
    font-size: 11px;
    line-height: 14px;
  }

  .sr-stat-card__value {
    color: #0f172a;
    font-size: 20px;
    font-weight: 700;
    line-height: 26px;
  }

  .sr-stat-card__value--warning {
    color: #d97706;
  }

  .sr-stat-card__value--primary {
    color: #4f46e5;
  }

  .sr-stat-card__value--name {
    font-size: 16px;
    font-weight: 600;
    line-height: 21px;
  }

  .sr-panel {
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
    padding: 16px 17px;
  }

  .sr-panel h2 {
    margin: 0 0 10px;
    color: #0f172a;
    font-size: 13px;
    font-weight: 600;
    line-height: 18px;
  }

  .sr-panel__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .sr-panel__header h2 {
    margin: 0;
  }

  .sr-customer-table,
  .sr-simple-table {
    overflow: hidden;
    background: #ffffff;
  }

  .sr-customer-table__head,
  .sr-customer-table__row {
    display: grid;
    grid-template-columns: minmax(220px, 1fr) 180px 180px 180px 180px;
  }

  .sr-customer-table__head,
  .sr-simple-table__head {
    min-height: 36px;
    align-items: center;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
  }

  .sr-customer-table__head span,
  .sr-simple-table__head span {
    padding: 0 14px;
    color: #64748b;
    font-size: 11px;
    font-weight: 600;
    line-height: 13px;
  }

  .sr-customer-table__row,
  .sr-simple-table__row {
    min-height: 44px;
    align-items: center;
    border-bottom: 1px solid #e2e8f0;
  }

  .sr-customer-table__row > *,
  .sr-simple-table__row > * {
    min-width: 0;
    padding: 0 14px;
    overflow: hidden;
    color: #64748b;
    font-size: 11px;
    line-height: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sr-customer-table__row strong {
    color: #0f172a;
    font-size: 12px;
    font-weight: 600;
    line-height: 15px;
  }

  .sr-customer-table__date {
    color: #4f46e5;
    font-size: 12px;
    line-height: 16px;
  }

  .sr-chip {
    background: rgba(100, 116, 139, 0.1);
    color: #64748b;
  }

  .sr-module-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
  }

  .sr-module-card {
    min-height: 86px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #f8fafc;
    padding: 12px;
  }

  .sr-module-card__name {
    color: #0f172a;
    font-size: 13px;
    font-weight: 600;
    line-height: 18px;
  }

  .sr-module-card__desc {
    margin-top: 6px;
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }

  .sr-simple-table__head,
  .sr-simple-table__row {
    display: grid;
    grid-template-columns: 1.4fr 1fr 140px;
  }

  .sr-simple-table__head--roadmap,
  .sr-simple-table__row--roadmap {
    grid-template-columns: 1fr 1fr 1fr 1fr;
  }

  .sr-doc-list {
    display: flex;
    flex-direction: column;
    border-top: 1px solid #e2e8f0;
  }

  .sr-doc-item {
    display: flex;
    min-height: 44px;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #e2e8f0;
    color: #0f172a;
    font-size: 12px;
  }

  .sr-doc-item em {
    color: #64748b;
    font-style: normal;
  }

  .sr-detail-loading {
    display: flex;
    min-height: 240px;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    font-size: 14px;
  }

  @media (max-width: 1180px) {
    .sr-stat-grid,
    .sr-module-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .sr-customer-table {
      overflow-x: auto;
    }

    .sr-customer-table__head,
    .sr-customer-table__row {
      min-width: 940px;
    }
  }

  @media (max-width: 768px) {
    .sr-product-detail__inner {
      padding: 14px 16px 20px;
    }

    .sr-product-hero {
      flex-direction: column;
      gap: 12px;
    }

    .sr-stat-grid,
    .sr-module-grid {
      grid-template-columns: 1fr;
    }

    .sr-simple-table {
      overflow-x: auto;
    }

    .sr-simple-table__head,
    .sr-simple-table__row {
      min-width: 620px;
    }
  }
</style>

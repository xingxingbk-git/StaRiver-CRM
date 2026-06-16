<template>
  <section class="sr-product-detail">
    <div v-if="product.id" class="sr-product-detail__inner">
      <header class="sr-product-hero">
        <div class="sr-product-hero__body">
          <div class="sr-product-hero__chips">
            <span class="sr-chip sr-chip--brand">{{ product.code || 'StaRiver' }}</span>
            <span class="sr-chip sr-chip--success">当前 {{ product.version || 'v1.0' }}</span>
            <span class="sr-chip sr-chip--primary">→ {{ product.nextVersion || 'v4.0' }}</span>
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
          <p>{{
            product.description || '面向工业场景的 AI 基础设施平台，提供数据接入、特征管理、模型训练与推理编排'
          }}</p>
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
              <span>{{ item.label }}</span>
              <strong :class="item.tone ? `sr-stat-card__value--${item.tone}` : ''">{{ item.value }}</strong>
            </div>
          </div>

          <section class="sr-panel">
            <div class="sr-section-title">使用客户</div>
            <div class="sr-table sr-table--customers">
              <div class="sr-table__head">
                <span>客户名称</span>
                <span>行业</span>
                <span>联系人</span>
                <span>关联项目</span>
                <span>合作起始</span>
              </div>
              <div v-for="row in customerRows" :key="row.name" class="sr-table__row">
                <strong>{{ row.name }}</strong>
                <span
                  ><em class="sr-chip">{{ row.industry }}</em></span
                >
                <span>{{ row.contact }}</span>
                <span>{{ row.project }}</span>
                <span class="sr-text-primary">{{ row.start }}</span>
              </div>
            </div>
          </section>
        </div>

        <section v-else-if="activeTab === 'modules'" class="sr-panel">
          <div class="sr-section-title">模块架构</div>
          <div class="sr-module-grid">
            <article v-for="item in moduleRows" :key="item.id" class="sr-module-card">
              <div class="sr-module-card__main">
                <div class="sr-module-card__title-row">
                  <strong>{{ item.name }}</strong>
                  <button
                    v-if="item.children.length"
                    class="sr-module-card__toggle"
                    :aria-label="isModuleExpanded(item.id) ? '收起子模块' : '展开子模块'"
                    @click="toggleModule(item.id)"
                  >
                    <span>{{ item.children.length }}个子模块</span>
                    <i :class="{ 'is-open': isModuleExpanded(item.id) }"></i>
                  </button>
                  <span v-else class="sr-module-card__toggle-placeholder"></span>
                </div>
                <p>负责人 {{ item.ownerName || '' }}</p>
                <em v-if="item.pendingCount" class="sr-pending-pill">📝 {{ item.pendingCount }} 个需求待发布</em>
              </div>
              <div v-if="item.children.length && isModuleExpanded(item.id)" class="sr-module-card__children">
                <div v-for="child in item.children" :key="child.id" class="sr-submodule-row">
                  <span class="sr-submodule-row__mark"></span>
                  <strong>{{ child.name }}</strong>
                  <span>{{ child.ownerName }}</span>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section v-else-if="activeTab === 'requirements'" class="sr-panel">
          <div class="sr-panel__header">
            <div class="sr-section-title">需求池 · {{ requirementTotal }}条</div>
            <div class="sr-filter-group">
              <button class="sr-filter sr-filter--active">全部</button>
              <button class="sr-filter">待发布</button>
              <button class="sr-filter">已上线</button>
            </div>
          </div>
          <div class="sr-table sr-table--requirements">
            <div class="sr-table__head">
              <span>ID</span>
              <span>标题</span>
              <span>所属模块</span>
              <span>来源</span>
              <span>优先级</span>
              <span>状态</span>
              <span>目标版本</span>
            </div>
            <div v-for="row in requirementRows" :key="row.id" class="sr-table__row">
              <span class="sr-text-primary">{{ row.id }}</span>
              <strong>{{ row.name }}</strong>
              <span
                ><em class="sr-chip">{{ row.module }}</em></span
              >
              <span>{{ row.source }}</span>
              <span
                ><em :class="['sr-priority', `sr-priority--${row.priorityType}`]">{{ row.priority }}</em></span
              >
              <span
                ><em :class="['sr-status', `sr-status--${row.statusType}`]">{{ row.status }}</em></span
              >
              <span class="sr-text-primary">{{ row.version }}</span>
            </div>
          </div>
        </section>

        <section v-else-if="activeTab === 'roadmap'" class="sr-panel">
          <div class="sr-panel__header">
            <div class="sr-section-title">版本路线图</div>
            <button class="sr-secondary-action">+ 新增版本</button>
          </div>
          <div class="sr-roadmap-timeline">
            <article v-for="row in roadmapRows" :key="row.id" class="sr-roadmap-item">
              <span :class="['sr-roadmap-item__dot', `sr-roadmap-item__dot--${row.statusType}`]"></span>
              <div class="sr-roadmap-item__card">
                <div class="sr-roadmap-item__meta">
                  <div>
                    <strong>{{ row.version }}</strong>
                    <em :class="['sr-status', `sr-status--${row.statusType}`]">{{ row.status }}</em>
                  </div>
                  <span
                    >需求项：<b>{{ row.pendingCount }}</b></span
                  >
                </div>
                <span class="sr-roadmap-item__date">{{ row.releaseDate }}</span>
                <div class="sr-roadmap-item__desc" :class="{ 'is-expanded': isRoadmapExpanded(row.id) }">
                  <p>{{ row.description }}</p>
                  <button
                    class="sr-roadmap-expand"
                    :class="{ 'is-open': isRoadmapExpanded(row.id) }"
                    :aria-label="isRoadmapExpanded(row.id) ? '收起版本说明' : '展开版本说明'"
                    @click="toggleRoadmap(row.id)"
                  ></button>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section v-else class="sr-panel">
          <div class="sr-panel__header">
            <div class="sr-section-title">产品文档 · {{ docRows.length }}份</div>
            <button class="sr-secondary-action">+上传文档</button>
          </div>
          <div class="sr-table sr-table--docs">
            <div class="sr-table__head">
              <span>文档名</span>
              <span>类型</span>
              <span>大小</span>
              <span>更新时间</span>
              <span></span>
            </div>
            <div v-for="doc in docRows" :key="doc.name" class="sr-table__row">
              <strong class="sr-doc-name">{{ doc.name }}</strong>
              <span
                ><em class="sr-chip">{{ doc.type }}</em></span
              >
              <span class="sr-text-primary">{{ doc.size }}</span>
              <span>{{ doc.updated }}</span>
              <span class="sr-doc-actions">
                <button class="sr-mini-action">查看</button>
                <button class="sr-mini-action">下载</button>
                <button class="sr-mini-action sr-mini-action--danger">删除</button>
              </span>
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

  import { getProductDetail, getRoadmap } from '@/api/modules/productManagement';

  const route = useRoute();
  const router = useRouter();

  const product = ref<any>({});
  const activeTab = ref('overview');
  const productRoadmap = ref<any[]>([]);
  const expandedModules = reactive<Record<string, boolean>>({});
  const expandedRoadmap = reactive<Record<string, boolean>>({});

  const tabs = [
    { key: 'overview', label: '总览' },
    { key: 'modules', label: '模块架构' },
    { key: 'requirements', label: '需求池' },
    { key: 'roadmap', label: '路线图' },
    { key: 'docs', label: '产品文档' },
  ];

  const statCards = computed(() => [
    { label: '功能模块', value: product.value.moduleCount ?? 0 },
    { label: '需求池', value: product.value.requirementCount ?? 0, tone: 'warning' },
    { label: '待发版本', value: product.value.nextVersion || product.value.version || '--', tone: 'primary' },
    { label: '产品负责人', value: product.value.productOwner || '--', tone: 'name' },
    { label: '研发负责人', value: product.value.devOwner || '--', tone: 'name' },
  ]);

  const customerRows = computed(() => (Array.isArray(product.value.customers) ? product.value.customers : []));

  const requirementRows = computed(() =>
    Array.isArray(product.value.requirements)
      ? product.value.requirements.map((row: any) => ({
          id: row.id,
          name: row.title || row.name,
          module: row.module || '--',
          source: row.source,
          priority: row.priority,
          priorityType: row.priorityType || 'normal',
          status: row.status,
          statusType: row.statusType || 'pending',
          version: row.version || '--',
        }))
      : []
  );

  const docRows = computed(() =>
    Array.isArray(product.value.documents)
      ? product.value.documents.map((doc: any) => ({
          id: doc.id,
          type: doc.type || '--',
          name: doc.name,
          size: doc.size || '--',
          updated: doc.updated || '--',
        }))
      : []
  );

  function normalizeModuleChild(child: any, index: number, moduleId: string) {
    if (typeof child === 'string') {
      return {
        id: `${moduleId}-child-${index}`,
        name: child,
        ownerName: '',
        pendingCount: 0,
      };
    }

    return {
      id: child.id || `${moduleId}-child-${index}`,
      name: child.name || '未命名子模块',
      ownerName: child.ownerName || '',
      pendingCount: Number(child.pendingCount || 0),
    };
  }

  const moduleRows = computed(() => {
    if (Array.isArray(product.value.modules) && product.value.modules.length) {
      return product.value.modules.map((module: any, index: number) => {
        const id = module.id || `module-${index}`;
        const children = Array.isArray(module.children)
          ? module.children.map((child: any, childIndex: number) => normalizeModuleChild(child, childIndex, id))
          : [];
        return {
          id,
          name: module.name || '未命名模块',
          ownerName: module.ownerName || '',
          pendingCount: Number(module.pendingCount || 0),
          children,
        };
      });
    }

    return [];
  });

  const requirementTotal = computed(() => requirementRows.value.length);

  function isModuleExpanded(id: string) {
    return expandedModules[id] !== false;
  }

  function toggleModule(id: string) {
    expandedModules[id] = !isModuleExpanded(id);
  }

  function isRoadmapExpanded(id: string) {
    return expandedRoadmap[id] === true;
  }

  function toggleRoadmap(id: string) {
    expandedRoadmap[id] = !isRoadmapExpanded(id);
  }

  function getRoadmapStatusType(status: string) {
    if (status === '已发布') {
      return 'online';
    }
    if (status === '开发中') {
      return 'developing';
    }
    if (status === '已上线') {
      return 'online';
    }
    return 'planning';
  }

  const roadmapRows = computed(() => {
    const rows: any[] =
      Array.isArray(product.value.roadmap) && product.value.roadmap.length
        ? product.value.roadmap
        : productRoadmap.value;

    return rows.map((row) => ({
      ...row,
      status: row.status === '已发布' ? '已上线' : row.status,
      statusType: getRoadmapStatusType(row.status),
      description: row.description || '',
    }));
  });

  onMounted(async () => {
    const id = route.params.id as string;
    const tabParam = route.query.tab as string;
    if (tabParam && tabs.some((t) => t.key === tabParam)) {
      activeTab.value = tabParam;
    }
    if (id) {
      try {
        const [data, roadmap] = await Promise.all([getProductDetail(id), getRoadmap()]);
        product.value = data || {};
        const matchedRoadmap = (roadmap || []).filter((r: any) => r.productId === id);
        productRoadmap.value = matchedRoadmap;
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('获取产品详情失败', e);
        product.value = {};
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
    overflow: auto;
    height: 100%;
    min-height: 0;
    color: #0f172a;
    background: #f3f4f6;
  }
  .sr-product-detail__inner {
    padding: 0 24px 24px;
    width: 100%;
  }
  .sr-product-hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin: 0 -24px;
    padding: 20px 24px 8px;
    min-height: 96px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
    gap: 24px;
  }
  .sr-product-hero__body {
    min-width: 0;
    p {
      margin: 4px 0 0;
      font-size: 12px;
      color: #64748b;
      line-height: 18px;
    }
  }
  .sr-product-hero__chips,
  .sr-product-hero__title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .sr-product-hero__chips {
    margin-bottom: 4px;
  }
  .sr-product-hero__title-row h1 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
    line-height: 24px;
  }
  .sr-icon-action {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0;
    width: 16px;
    height: 16px;
    border: 0;
    color: #64748b;
    background: transparent;
    cursor: pointer;

    --stroke-0: currentcolor;
    --fill-0: currentcolor;
    &:hover {
      color: #4f46e5;
    }
    span,
    svg {
      display: block;
      width: 16px;
      height: 16px;
    }
    svg [stroke]:not([stroke='none']) {
      stroke: currentcolor !important;
    }
    svg [fill]:not([fill='none']) {
      fill: currentcolor !important;
    }
  }
  .sr-primary-action,
  .sr-secondary-action {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 14px;
    height: 32px;
    font-size: 12px;
    font-weight: 600;
    border: 1px solid #0f172a;
    border-radius: 6px;
    white-space: nowrap;
    color: #ffffff;
    background: #0f172a;
    cursor: pointer;
  }
  .sr-secondary-action {
    height: 28px;
    border-color: #e2e8f0;
    color: #0f172a;
    background: #ffffff;
  }
  .sr-tabs {
    display: flex;
    align-items: center;
    margin: 0 -24px;
    padding: 0 24px;
    height: 40px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
    gap: 4px;
  }
  .sr-tab {
    padding: 0 13px;
    height: 36px;
    font-size: 12px;
    font-weight: 600;
    border: 0;
    border-bottom: 2px solid transparent;
    color: #64748b;
    background: transparent;
    cursor: pointer;
  }
  .sr-tab--active {
    border-bottom-color: #4f46e5;
    color: #4f46e5;
  }
  .sr-tab-content {
    padding-top: 20px;
  }
  .sr-overview {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }
  .sr-panel {
    padding: 16px 17px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
  }
  .sr-panel__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    gap: 16px;
  }
  .sr-section-title {
    display: flex;
    align-items: center;
    height: 28px;
    font-size: 13px;
    font-weight: 600;
    color: #0f172a;
    line-height: 16px;
  }
  .sr-stat-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 10px;
  }
  .sr-stat-card {
    display: flex;
    justify-content: center;
    padding: 0 16px;
    height: 72px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    flex-direction: column;
    gap: 4px;
  }
  .sr-stat-card span {
    font-size: 11px;
    color: #64748b;
    line-height: 14px;
  }
  .sr-stat-card strong {
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
    line-height: 24px;
  }
  .sr-stat-card__value--warning {
    color: #d97706 !important;
  }
  .sr-stat-card__value--primary {
    color: #4f46e5 !important;
  }
  .sr-stat-card__value--name {
    font-size: 16px !important;
    font-weight: 600 !important;
  }
  .sr-module-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }
  .sr-module-card {
    overflow: hidden;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
  }
  .sr-module-card__main {
    display: flex;
    align-items: flex-start;
    padding: 12px;
    min-height: 94px;
    flex-direction: column;
    gap: 6px;
    p {
      margin: 0;
      font-size: 11px;
      color: #64748b;
      line-height: 14px;
    }
  }
  .sr-module-card__title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    height: 18px;
    gap: 12px;
    strong {
      overflow: hidden;
      min-width: 0;
      font-size: 13px;
      font-weight: 600;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #0f172a;
      line-height: 16px;
    }
  }
  .sr-module-card__toggle {
    display: inline-flex;
    align-items: center;
    padding: 0;
    font-size: 11px;
    border: 0;
    color: #4f46e5;
    background: transparent;
    flex-shrink: 0;
    gap: 8px;
    line-height: 14px;
    cursor: pointer;
    i {
      width: 7px;
      height: 7px;
      border-right: 1.5px solid currentcolor;
      border-bottom: 1.5px solid currentcolor;
      transform: rotate(45deg);
      transition: transform 0.16s ease;
    }
    i.is-open {
      transform: rotate(225deg);
    }
  }
  .sr-module-card__toggle-placeholder {
    width: 10px;
    height: 10px;
  }
  .sr-pending-pill {
    display: inline-flex;
    align-items: center;
    padding: 0 6px;
    height: 24px;
    font-size: 11px;
    border-radius: 4px;
    color: #d97706;
    background: #fff7ed;
    font-style: normal;
    line-height: 14px;
  }
  .sr-module-card__children {
    display: flex;
    padding: 12px;
    border-top: 1px solid #eef2f7;
    flex-direction: column;
    gap: 4px;
  }
  .sr-submodule-row {
    display: grid;
    align-items: center;
    padding: 0 6px;
    height: 24px;
    border-radius: 4px;
    background: #f8fafc;
    grid-template-columns: 12px minmax(0, 1fr) 40px;
    gap: 6px;
  }
  .sr-submodule-row__mark {
    width: 12px;
    height: 2px;
    border-radius: 1px;
    background: rgb(79 70 229 / 40%);
  }
  .sr-submodule-row strong {
    overflow: hidden;
    font-size: 11px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #0f172a;
    line-height: 14px;
  }
  .sr-submodule-row span:last-child {
    overflow: hidden;
    font-size: 11px;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #64748b;
    line-height: 14px;
  }
  .sr-filter-group {
    display: flex;
    gap: 10px;
  }
  .sr-filter {
    padding: 0 12px;
    height: 28px;
    font-size: 12px;
    font-weight: 600;
    border: 1px solid #e2e8f0;
    border-radius: 999px;
    color: #64748b;
    background: #ffffff;
    cursor: pointer;
  }
  .sr-filter--active {
    border-color: #0f172a;
    color: #ffffff;
    background: #0f172a;
  }
  .sr-table {
    overflow: hidden;
    border-top: 1px solid #eef2f7;
    background: #ffffff;
  }
  .sr-table__head,
  .sr-table__row {
    display: grid;
    align-items: center;
  }
  .sr-table--customers .sr-table__head,
  .sr-table--customers .sr-table__row {
    grid-template-columns: minmax(220px, 1fr) 180px 180px 180px 180px;
  }
  .sr-table--requirements .sr-table__head,
  .sr-table--requirements .sr-table__row {
    grid-template-columns: 160px minmax(260px, 1fr) 160px 200px 100px 100px 100px;
  }
  .sr-table--docs .sr-table__head,
  .sr-table--docs .sr-table__row {
    grid-template-columns: minmax(320px, 1fr) 160px 160px 160px 180px;
  }
  .sr-table__head {
    min-height: 36px;
    border-bottom: 1px solid #e2e8f0;
    background: #f8fafc;
  }
  .sr-table__head span {
    padding: 0 14px;
    font-size: 11px;
    font-weight: 600;
    color: #64748b;
    line-height: 13px;
  }
  .sr-table__row {
    min-height: 44px;
    border-bottom: 1px solid #eef2f7;
  }
  .sr-table__row > * {
    overflow: hidden;
    padding: 0 14px;
    min-width: 0;
    font-size: 11px;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #64748b;
    line-height: 14px;
  }
  .sr-table__row strong {
    font-size: 12px;
    font-weight: 600;
    color: #0f172a;
    line-height: 16px;
  }
  .sr-chip,
  .sr-status,
  .sr-priority {
    display: inline-flex;
    align-items: center;
    padding: 0 6px;
    height: 18px;
    font-size: 11px;
    font-weight: 600;
    border-radius: 4px;
    white-space: nowrap;
    color: #64748b;
    background: rgb(100 116 139 / 10%);
    font-style: normal;
    line-height: 14px;
  }
  .sr-chip--brand {
    color: #4f46e5;
    background: #eef2ff;
  }
  .sr-chip--success {
    color: #16a34a;
    background: rgb(22 163 74 / 10%);
  }
  .sr-chip--primary {
    color: #4f46e5;
    background: rgb(79 70 229 / 10%);
  }
  .sr-status {
    position: relative;
    gap: 5px;
    &::before {
      width: 6px;
      height: 6px;
      border-radius: 999px;
      background: currentcolor;
      content: '';
    }
  }
  .sr-status--planning {
    color: #64748b;
    background: rgb(100 116 139 / 10%);
  }
  .sr-status--pending,
  .sr-status--developing {
    color: #4f46e5;
    background: #eef2ff;
  }
  .sr-status--review {
    color: #d97706;
    background: #fff7ed;
  }
  .sr-status--done,
  .sr-status--online {
    color: #16a34a;
    background: rgb(22 163 74 / 10%);
  }
  .sr-priority {
    gap: 4px;
    border-radius: 12px;
    &::before {
      width: 6px;
      height: 6px;
      border-radius: 3px;
      background: currentcolor;
      content: '';
    }
  }
  .sr-priority--normal {
    color: #64748b;
    background: rgb(100 116 139 / 10%);
  }
  .sr-priority--middle {
    color: #d97706;
    background: rgb(217 119 6 / 10%);
  }
  .sr-priority--high {
    color: #dc2626;
    background: rgb(220 38 38 / 10%);
  }
  .sr-text-primary,
  .sr-link-action {
    color: #4f46e5;
  }
  .sr-link-action {
    padding: 0 14px;
    font-size: 11px;
    font-weight: 600;
    border: 0;
    text-align: left;
    background: transparent;
    cursor: pointer;
  }
  .sr-roadmap-timeline {
    position: relative;
    display: flex;
    overflow: hidden;
    padding-left: 48px;
    width: 100%;
    max-height: 602px;
    flex-direction: column;
    gap: 24px;
    &::before {
      position: absolute;
      top: 0;
      bottom: 0;
      left: 14px;
      width: 2px;
      background: #eef2f7;
      content: '';
    }
  }
  .sr-roadmap-item {
    position: relative;
  }
  .sr-roadmap-item__dot {
    position: absolute;
    top: 6px;
    left: -40px;
    width: 14px;
    height: 14px;
    border: 3px solid #ffffff;
    border-radius: 999px;
    background: #64748b;
  }
  .sr-roadmap-item__dot--developing,
  .sr-roadmap-item__dot--pending {
    background: #4f46e5;
  }
  .sr-roadmap-item__dot--online {
    background: #16a34a;
  }
  .sr-roadmap-item__card {
    display: flex;
    padding: 17px;
    min-height: 102px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #f8fafc;
    flex-direction: column;
    gap: 4px;
  }
  .sr-roadmap-item__meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    div {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    strong {
      font-size: 15px;
      font-weight: 700;
      color: #4f46e5;
      line-height: 20px;
    }
    span {
      font-size: 12px;
      color: #64748b;
      line-height: 18px;
    }
    b {
      color: #0f172a;
    }
  }
  .sr-roadmap-item__date {
    font-size: 11px;
    color: #64748b;
    line-height: 14px;
  }
  .sr-roadmap-item__desc {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    gap: 12px;
  }
  .sr-roadmap-item__desc p {
    overflow: hidden;
    margin: 0;
    min-width: 0;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #64748b;
    flex: 1 1 auto;
    line-height: 18px;
  }
  .sr-roadmap-item__desc.is-expanded {
    align-items: flex-start;
  }
  .sr-roadmap-item__desc.is-expanded p {
    overflow: visible;
    white-space: normal;
  }
  .sr-roadmap-expand {
    position: relative;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0;
    width: 16px;
    height: 16px;
    border: 0;
    background: transparent;
    flex: 0 0 auto;
    cursor: pointer;
    &::before {
      width: 7px;
      height: 7px;
      border-right: 1.5px solid #0f172a;
      border-bottom: 1.5px solid #0f172a;
      transform: rotate(45deg);
      content: '';
    }
    &.is-open::before {
      transform: rotate(225deg);
    }
  }
  .sr-doc-name {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    &::before {
      width: 10px;
      height: 12px;
      border: 1px solid #cbd5e1;
      border-radius: 2px;
      background: linear-gradient(135deg, #f8fafc 0 70%, #e2e8f0 70%);
      content: '';
    }
  }
  .sr-doc-actions {
    display: inline-flex;
    justify-content: flex-end;
    gap: 10px;
  }
  .sr-mini-action {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 10px;
    height: 24px;
    font-size: 11px;
    font-weight: 600;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    color: #334155;
    background: #ffffff;
    cursor: pointer;
  }
  .sr-mini-action--danger {
    border-color: transparent;
    color: #dc2626;
    background: rgb(220 38 38 / 10%);
  }
  .sr-doc-actions .sr-mini-action {
    padding: 0;
    min-width: 42px;
  }
  .sr-detail-loading {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 240px;
    font-size: 14px;
    color: #94a3b8;
  }

  @media (max-width: 1280px) {
    .sr-module-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .sr-stat-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .sr-table {
      overflow-x: auto;
    }
    .sr-table__head,
    .sr-table__row {
      min-width: 920px;
    }
  }

  @media (max-width: 768px) {
    .sr-product-detail__inner {
      padding: 0 16px 20px;
    }
    .sr-product-hero {
      flex-direction: column;
      gap: 12px;
      margin: 0 -16px;
      padding-right: 16px;
      padding-left: 16px;
    }
    .sr-tabs {
      overflow-x: auto;
      margin: 0 -16px;
      padding: 0 16px;
    }
    .sr-module-grid,
    .sr-stat-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

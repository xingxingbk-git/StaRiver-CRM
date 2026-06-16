<template>
  <StariverModulePage title="产品集">
    <template #actions>
      <n-button v-permission="['PRODUCT_MANAGEMENT:ADD']" type="primary" class="sr-btn-create" @click="handleCreate">
        + 新建产品
      </n-button>
    </template>
    <template #subtitle>
      <span class="sr-product-badge">{{ productList.length }} 款产品</span>
      <span class="sr-product-subtitle"
        >覆盖 {{ productCount.industry }} 个行业 · 服务 {{ productCount.customer }} 家客户</span
      >
    </template>

    <div class="stariver-product-stack">
      <!-- 产品卡片网格 -->
      <div class="stariver-product-cards">
        <div
          v-for="product in productList"
          :key="product.id"
          class="stariver-product-card"
          @click="handleCardClick(product)"
        >
          <div class="stariver-product-card__head">
            <div class="stariver-product-card__icon" :style="{ background: product.iconBg }">
              <span :style="{ color: product.iconColor }">{{ product.iconText }}</span>
            </div>
            <div class="stariver-product-card__info">
              <div class="stariver-product-card__code">{{ product.code }}</div>
              <div class="stariver-product-card__name">{{ product.name }}</div>
            </div>
            <span
              v-if="product.version"
              class="stariver-product-card__pill"
              :style="{ background: product.statusBg, color: product.statusColor }"
            >
              {{ product.version }}
            </span>
          </div>
          <div class="stariver-product-card__desc">{{ product.description }}</div>
          <div class="stariver-product-card__meta">
            <span>产品负责人 · {{ product.productOwner }}</span>
            <span>研发负责人 · {{ product.devOwner }}</span>
          </div>
          <div class="stariver-product-card__stats">
            <div class="stariver-product-card__stat">
              <div class="stariver-product-card__stat-label">模块</div>
              <div class="stariver-product-card__stat-value">{{ product.moduleCount }}</div>
            </div>
            <div class="stariver-product-card__stat">
              <div class="stariver-product-card__stat-label">需求池</div>
              <div class="stariver-product-card__stat-value stariver-product-card__stat-value--accent">
                {{ product.requirementCount }}
              </div>
            </div>
            <div class="stariver-product-card__stat">
              <div class="stariver-product-card__stat-label">待发版</div>
              <div class="stariver-product-card__stat-value stariver-product-card__stat-value--accent">
                {{ product.nextVersion }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 版本路线图面板 -->
      <div class="stariver-roadmap-panel">
        <div class="stariver-roadmap-panel__header">
          <div class="stariver-roadmap-panel__title">版本路线图</div>
          <div class="stariver-roadmap-panel__pills">
            <button
              v-for="filter in productFilters"
              :key="filter.key"
              :class="['stariver-roadmap-pill', { 'stariver-roadmap-pill--active': activeFilter === filter.key }]"
              @click="activeFilter = filter.key"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>
        <div class="stariver-roadmap-table">
          <div class="stariver-roadmap-table__head">
            <span>产品</span>
            <span>版本</span>
            <span>发布日期</span>
            <span>状态</span>
            <span>待发布需求</span>
            <span>操作</span>
          </div>
          <div v-for="row in filteredRoadmap" :key="row.id" class="stariver-roadmap-table__row">
            <span>
              <em class="stariver-product-chip" :class="`stariver-product-chip--${row.productType}`">
                {{ row.product }}
              </em>
            </span>
            <span>{{ row.version }}</span>
            <span>{{ row.releaseDate }}</span>
            <span>
              <em class="stariver-status-pill" :class="`stariver-status-pill--${row.statusType}`">{{ row.status }}</em>
            </span>
            <span>{{ row.pendingCount }}</span>
            <span>
              <button class="stariver-link-btn" @click="goToRoadmap(row.productId)">查看路线图</button>
            </span>
          </div>
        </div>
      </div>
    </div>
  </StariverModulePage>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';
  import { NButton } from 'naive-ui';

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  import { getProductList, getRoadmap } from '@/api/modules/productManagement';

  const router = useRouter();

  const activeFilter = ref('all');

  const productFilters = [
    { key: 'all', label: '全部' },
    { key: 'stariver', label: 'StaRiver' },
    { key: 'optiqa', label: 'OptiQA' },
  ];

  // 产品列表数据，通过 API 获取
  const productList = ref<any[]>([]);
  // 版本路线图数据，通过 API 获取
  const roadmapData = ref<any[]>([]);

  const productCount = computed(() => ({
    industry: productList.value.length ? 2 : 0,
    customer: productList.value.length * 18,
  }));

  // 根据筛选条件过滤路线图
  const filteredRoadmap = computed(() => {
    if (activeFilter.value === 'all') return roadmapData.value;
    return roadmapData.value.filter((r) => r.productType === activeFilter.value);
  });

  // 页面加载时通过 API 获取数据
  onMounted(async () => {
    try {
      const [productRes, roadmapRes] = await Promise.all([getProductList(), getRoadmap()]);
      productList.value = productRes?.list || [];
      roadmapData.value = roadmapRes || [];
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('获取产品数据失败', e);
    }
  });

  // 跳转到新建产品页
  function handleCreate() {
    router.push({ name: 'productCreate' });
  }

  // 跳转到产品详情页
  function handleCardClick(product: { id: string }) {
    router.push({ name: 'productDetail', params: { id: product.id } });
  }

  // 跳转到产品详情页的路线图 tab
  function goToRoadmap(productId: string) {
    router.push({ name: 'productDetail', params: { id: productId }, query: { tab: 'roadmap' } });
  }
</script>

<style lang="less" scoped>
  .sr-btn-create {
    padding: 0 14px;
    height: 32px;
    font-size: 12px;
    font-weight: 500;
    border-color: #0f172a;
    border-radius: 6px;
    color: #ffffff;
    background: #0f172a;
    line-height: 30px;
    &:hover,
    &:focus {
      border-color: #1e293b;
      color: #ffffff;
      background: #1e293b;
    }
  }
  .stariver-product-stack {
    display: flex;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    gap: 14px;
  }

  // 产品卡片网格
  .stariver-product-cards {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  .stariver-product-card {
    display: flex;
    padding: 15px;
    width: 400px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
    transition: box-shadow 0.2s;
    flex-direction: column;
    gap: 10px;
    cursor: pointer;
    &:hover {
      box-shadow: 0 2px 8px rgb(15 23 42 / 8%);
    }
  }
  .stariver-product-card__head {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .stariver-product-card__icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 34px;
    height: 34px;
    border-radius: 7px;
    flex-shrink: 0;
    span {
      font-size: 13px;
      font-weight: 700;
      line-height: 17px;
    }
  }
  .stariver-product-card__info {
    display: flex;
    min-width: 0;
    flex: 1;
    flex-direction: column;
    gap: 2px;
  }
  .stariver-product-card__code {
    font-size: 11px;
    font-weight: 700;
    color: #4f46e5;
    line-height: 14px;
  }
  .stariver-product-card__name {
    font-size: 14px;
    font-weight: 700;
    color: #0f172a;
    line-height: 17px;
  }
  .stariver-product-card__pill {
    display: inline-flex;
    align-items: center;
    padding: 0 6px;
    height: 18px;
    font-size: 11px;
    font-weight: 500;
    border-radius: 12px;
    flex-shrink: 0;
    line-height: 13px;
  }
  .stariver-product-card__desc {
    /* stylelint-disable-next-line value-no-vendor-prefix */
    display: -webkit-box;
    overflow: hidden;
    height: 30px;
    font-size: 12px;
    color: #64748b;
    -webkit-box-orient: vertical;
    line-height: 15px;
    -webkit-line-clamp: 2;
  }
  .stariver-product-card__meta {
    display: flex;
    padding-top: 8px;
    border-top: 1px solid #eef2f7;
    gap: 24px;
    span {
      font-size: 11px;
      color: #94a3b8;
      line-height: 14px;
    }
  }
  .stariver-product-card__stats {
    display: flex;
    padding-top: 12px;
    border-top: 1px solid #e2e8f0;
    gap: 8px;
  }
  .stariver-product-card__stat {
    display: flex;
    padding: 4px 8px;
    border-radius: 4px;
    background: #f8fafc;
    flex: 1;
    flex-direction: column;
    gap: 2px;
  }
  .stariver-product-card__stat-label {
    font-size: 10px;
    color: #94a3b8;
    line-height: 12px;
  }
  .stariver-product-card__stat-value {
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
    line-height: 20px;
    &--accent {
      color: #4f46e5;
    }
  }

  // 版本路线图面板
  .stariver-roadmap-panel {
    padding: 16px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
  }
  .stariver-roadmap-panel__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;
  }
  .stariver-roadmap-panel__title {
    font-size: 13px;
    font-weight: 500;
    color: #0f172a;
    line-height: 18px;
  }
  .stariver-roadmap-panel__pills {
    display: flex;
    gap: 8px;
  }
  .stariver-roadmap-pill {
    padding: 0 10px;
    height: 26px;
    font-size: 12px;
    border: 1px solid #e2e8f0;
    border-radius: 99px;
    color: #64748b;
    background: #ffffff;
    line-height: 24px;
    cursor: pointer;
    &--active {
      border-color: #0f172a;
      color: #ffffff;
      background: #0f172a;
    }
  }
  .stariver-roadmap-table__head {
    display: grid;
    align-items: center;
    padding: 0 12px;
    min-height: 36px;
    border-bottom: 1px solid #e2e8f0;
    background: #f8fafc;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 100px;
    gap: 12px;
    span {
      font-size: 12px;
      font-weight: 500;
      color: #64748b;
    }
  }
  .stariver-roadmap-table__row {
    display: grid;
    align-items: center;
    padding: 0 12px;
    min-height: 44px;
    border-bottom: 1px solid #f1f5f9;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 100px;
    gap: 12px;
    span {
      font-size: 13px;
      color: #0f172a;
    }
  }

  // 产品标签
  .stariver-product-chip {
    display: inline-flex;
    align-items: center;
    padding: 0 8px;
    height: 22px;
    font-size: 12px;
    font-weight: 500;
    border-radius: 4px;
    font-style: normal;
    &--stariver {
      color: #4f46e5;
      background: #eef2ff;
    }
    &--optiqa {
      color: #ea580c;
      background: #fff7ed;
    }
  }

  // 状态标签
  .stariver-status-pill {
    display: inline-flex;
    align-items: center;
    padding: 0 8px;
    height: 20px;
    font-size: 12px;
    font-weight: 500;
    border-radius: 12px;
    font-style: normal;
    &--released {
      color: #16a34a;
      background: rgb(22 163 74 / 10%);
    }
    &--developing {
      color: #4f46e5;
      background: rgb(79 70 229 / 10%);
    }
    &--planning {
      color: #64748b;
      background: #f1f5f9;
    }
  }

  // 查看路线图链接按钮
  .stariver-link-btn {
    padding: 0;
    font-size: 12px;
    font-weight: 500;
    border: none;
    color: #4f46e5;
    background: transparent;
    cursor: pointer;
    &:hover {
      text-decoration: underline;
    }
  }
</style>

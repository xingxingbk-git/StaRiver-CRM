<template>
  <section class="prm-page">
    <template v-if="viewMode === 'list'">
      <StariverModulePage title="PRM需求池" class="prm-module-page">
        <template #subtitle>
          <span>产品需求主池 · 跨产品 · 跨版本 · 按 RICE 优先级排序</span>
        </template>
        <template #actions>
          <button class="sr-btn sr-btn--primary" @click="goCreate">+ 提报需求</button>
        </template>

        <main class="prm-list-body">
          <section class="prm-stat-grid">
            <article v-for="item in statCards" :key="item.label" class="prm-stat-card">
              <span>{{ item.label }}</span>
              <strong :class="item.colorClass">{{ item.value }}</strong>
            </article>
          </section>

          <section class="prm-table-card">
            <div class="prm-table-toolbar">
              <div class="prm-chip-group">
                <button
                  v-for="filter in productFilters"
                  :key="filter"
                  class="prm-filter-chip"
                  :class="{ 'prm-filter-chip--active': productFilter === filter }"
                  @click="productFilter = filter"
                >
                  {{ filter }}
                </button>
              </div>
              <select v-model="statusFilter" class="prm-native-select" aria-label="全部状态">
                <option value="全部状态">全部状态</option>
                <option v-for="status in requirementStatuses" :key="status" :value="status">{{ status }}</option>
              </select>
            </div>

            <div class="prm-table" role="table" aria-label="产品需求列表">
              <div class="prm-table__row prm-table__row--head" role="row">
                <span>产品需求</span>
                <span>产品</span>
                <span>版本</span>
                <span>来源</span>
                <span>优先级</span>
                <span>状态</span>
                <span>负责人</span>
              </div>
              <button
                v-for="item in filteredRequirements"
                :key="item.id"
                class="prm-table__row prm-table__row--body"
                type="button"
                role="row"
                @click="goDetail(item.id)"
              >
                <span class="prm-demand-title">
                  <em>{{ item.id }}</em>
                  <strong>{{ item.title }}</strong>
                </span>
                <span
                  ><i class="prm-product-tag" :class="`prm-product-tag--${item.productKey}`">{{
                    item.product
                  }}</i></span
                >
                <span class="prm-version">{{ item.version }}</span>
                <span class="prm-muted">{{ item.source }}</span>
                <span
                  ><i class="prm-pill" :class="pillClass(item.priority)">{{ item.priority }}</i></span
                >
                <span
                  ><i class="prm-pill" :class="pillClass(item.status)">{{ item.status }}</i></span
                >
                <span>{{ item.owner }}</span>
              </button>
            </div>
          </section>
        </main>
      </StariverModulePage>
    </template>

    <template v-else-if="viewMode === 'create'">
      <StariverModulePage title="新建产品需求">
        <template #subtitle>
          <div class="sr-create-page__subtitle">填写需求信息和描述，提交后进入产品需求池</div>
        </template>
        <template #actions>
          <div class="sr-create-actions">
            <button class="sr-btn sr-btn--ghost" @click="goList">取消</button>
            <button class="sr-btn sr-btn--primary" @click="submitRequirement">提交 →</button>
          </div>
        </template>

        <div class="sr-create-page">
          <section class="sr-panel">
            <div class="sr-form-grid">
              <label class="sr-field">
                <span class="sr-field__label">需求标题 <span class="sr-field__req">*</span></span>
                <input v-model="draft.title" class="sr-input" placeholder="如：BI 看板支持多维钻取" />
              </label>
              <div class="sr-field">
                <span class="sr-field__label">需求类型</span>
                <component
                  :is="NSelect"
                  v-model:value="draft.type"
                  class="sr-native-control"
                  :options="typeOptionItems"
                />
              </div>
              <div class="sr-field">
                <span class="sr-field__label">需求来源</span>
                <component
                  :is="NSelect"
                  v-model:value="draft.source"
                  class="sr-native-control"
                  :options="sourceOptionItems"
                />
              </div>
              <div class="sr-field">
                <span class="sr-field__label">目标产品</span>
                <component
                  :is="NSelect"
                  v-model:value="draft.product"
                  class="sr-native-control"
                  :options="productOptionItems"
                />
              </div>
              <label class="sr-field">
                <span class="sr-field__label">期望上线</span>
                <input v-model="draft.release" class="sr-input" placeholder="2026-Q3" />
              </label>
              <div class="sr-field">
                <span class="sr-field__label">优先级</span>
                <component
                  :is="NSelect"
                  v-model:value="draft.priority"
                  class="sr-native-control"
                  :options="priorityOptionItems"
                />
              </div>
            </div>
          </section>

          <section class="sr-panel">
            <h2>需求描述</h2>
            <component
              :is="StariverRichEditor"
              v-model="draft.description"
              min-height="large"
              :placeholder="requirementDescriptionPlaceholder"
            />
          </section>

          <section class="sr-panel">
            <h2>验收标准 (AC)</h2>
            <component :is="StariverRichEditor" v-model="draft.acceptance" :placeholder="acceptancePlaceholder" />
          </section>
        </div>
      </StariverModulePage>
    </template>

    <template v-else>
      <header class="prm-hero prm-hero--detail">
        <div>
          <div class="prm-detail-code">
            <span>{{ activeRequirement.id }}</span>
            <i class="prm-pill prm-pill--p2">P2中</i>
          </div>
          <h1>{{ activeRequirement.detailTitle }}</h1>
          <p>
            {{ activeRequirement.product }} · {{ activeRequirement.type }} · 负责人 {{ activeRequirement.owner }} ·
            {{ activeRequirement.source }}需求 · 期望上线
            {{ activeRequirement.expectedRelease || activeRequirement.version }}
          </p>
        </div>
      </header>

      <main class="prm-body">
        <section class="prm-panel prm-flow-panel">
          <h2>交付流程 · BPMN</h2>
          <div class="prm-flow-board">
            <div class="prm-flow">
              <div
                v-for="(step, index) in flowSteps"
                :key="step.label"
                class="prm-flow-step"
                :class="[`prm-flow-step--${step.state}`, { 'prm-flow-step--diamond': step.shape === 'diamond' }]"
              >
                <div class="prm-flow-node">
                  <span v-if="step.shape === 'start'" class="prm-flow-dot"></span>
                  <span v-else-if="step.shape === 'end'" class="prm-flow-stop"></span>
                  <span v-else>{{ step.text }}</span>
                </div>
                <small v-if="['start', 'diamond', 'end'].includes(step.shape)">{{ step.label }}</small>
                <i v-if="index < flowSteps.length - 1" class="prm-flow-line"></i>
              </div>
            </div>
          </div>
        </section>

        <section class="prm-detail-grid">
          <div class="prm-detail-left">
            <section class="prm-panel">
              <h2>需求描述</h2>
              <div class="prm-story-list">
                <article
                  class="prm-story prm-rich-display"
                  v-html="activeRequirement.description || '暂无需求描述'"
                ></article>
              </div>
            </section>

            <section class="prm-panel prm-ac-panel">
              <h2>验收标准 (AC)</h2>
              <div class="prm-ac-box" v-html="activeRequirement.acceptance || '暂无验收标准'"></div>
            </section>
          </div>

          <aside class="prm-panel prm-record-panel">
            <h2>流程记录</h2>
            <div class="prm-record-list">
              <article v-for="record in records" :key="record.title" class="prm-record">
                <span :class="record.done ? 'prm-record__icon prm-record__icon--done' : 'prm-record__icon'">
                  {{ record.done ? '✓' : '...' }}
                </span>
                <div>
                  <strong>{{ record.title }}</strong>
                  <p>
                    <span>{{ record.owner }}</span>
                    <time v-if="record.time">{{ record.time }}</time>
                  </p>
                </div>
              </article>
            </div>
          </aside>
        </section>
      </main>
    </template>
  </section>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NSelect } from 'naive-ui';

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import StariverRichEditor from '@/components/business/stariver-rich-editor/index.vue';

  import { addRequirement, getProductList, getRequirementList } from '@/api/modules/productManagement';

  export default defineComponent({
    name: 'ProductRequirements',
    components: {
      NSelect,
      StariverRichEditor,
      StariverModulePage,
    },
    setup() {
      const route = useRoute();
      const router = useRouter();
      const productFilter = ref('全部');
      const statusFilter = ref('全部状态');

      const typeOptions = ['功能新增', '体验优化', '性能改进'];
      const sourceOptions = ['客户', '售前', '内部', '竞品'];
      const productOptions = ref(['StaRiver', 'OptiQA']);
      const priorityOptions = ['P0', 'P1', 'P2'];
      const toSelectOptions = (options: string[]) => options.map((option) => ({ label: option, value: option }));
      const typeOptionItems = toSelectOptions(typeOptions);
      const sourceOptionItems = toSelectOptions(sourceOptions);
      const productOptionItems = computed(() => toSelectOptions(productOptions.value));
      const priorityOptionItems = toSelectOptions(priorityOptions);
      const productFilters = computed(() => ['全部', ...productOptions.value]);
      const requirementStatuses = [
        '需求池',
        '需求评估',
        '产品设计',
        '技术评审',
        '开发中',
        '测试中',
        '产品验收',
        '待发布',
        '已上线',
      ];

      const draft = reactive({
        title: '',
        type: '功能新增',
        source: '客户',
        product: 'StaRiver',
        release: '2026-Q3',
        priority: 'P2',
        description: '作为 用户角色，我希望 [功能/能力]，以便 业务价值。',
        acceptance: 'Given 前置条件：\nWhen 操作动作：\nThen 预期结果：',
      });

      const requirementDescriptionPlaceholder = '请输入需求描述，支持后续扩展加粗、附件等富文本能力';
      const acceptancePlaceholder = '请输入验收标准，例如 Given / When / Then';

      const requirements = ref<any[]>([]);

      const statCards = computed(() => [
        { label: '总需求', value: String(requirements.value.length), colorClass: '' },
        {
          label: '设计中',
          value: String(requirements.value.filter((item) => ['产品设计', '需求评估'].includes(item.status)).length),
          colorClass: 'is-indigo',
        },
        {
          label: '开发中',
          value: String(requirements.value.filter((item) => ['开发中', '测试中'].includes(item.status)).length),
          colorClass: 'is-cyan',
        },
        {
          label: '已上线',
          value: String(requirements.value.filter((item) => item.status === '已上线').length),
          colorClass: 'is-green',
        },
      ]);

      const flowSteps = [
        { label: '提交', text: '', state: 'done', shape: 'start' },
        { label: '需求池', text: '需求池', state: 'done', shape: 'pill' },
        { label: '需求评审', text: '?', state: 'current', shape: 'diamond' },
        { label: '产品设计', text: '产品设计', state: 'todo', shape: 'rect' },
        { label: '技术评审', text: '技术评审', state: 'todo', shape: 'rect' },
        { label: '开发', text: '开发', state: 'todo', shape: 'rect' },
        { label: '测试', text: '测试', state: 'todo', shape: 'rect' },
        { label: '产品验收', text: '产品验收', state: 'todo', shape: 'rect' },
        { label: '发布', text: '发布', state: 'todo', shape: 'rect' },
        { label: '完成', text: '', state: 'todo', shape: 'end' },
      ];

      const records = [
        { title: '创建需求', owner: '业务人员·陈思远', time: '2026-04-10 09:00', done: true },
        { title: '需求评审', owner: '产品经理·陈立文', time: '', done: false },
      ];

      const viewMode = computed(() => {
        if (route.query.mode === 'create') return 'create';
        if (route.query.mode === 'detail') return 'detail';
        return 'list';
      });

      const filteredRequirements = computed(() =>
        requirements.value.filter((item) => {
          const matchProduct = productFilter.value === '全部' || item.product === productFilter.value;
          const matchStatus = statusFilter.value === '全部状态' || item.status === statusFilter.value;
          return matchProduct && matchStatus;
        })
      );

      const activeRequirement = computed(() => {
        const id = String(route.query.id || '');
        return (
          requirements.value.find((item) => item.id === id) ||
          requirements.value[0] || {
            id: '',
            detailTitle: '',
            product: '',
            type: '',
            owner: '',
            source: '',
            version: '',
            description: '',
            acceptance: '',
          }
        );
      });

      const goCreate = () => router.push({ path: route.path, query: { mode: 'create' } });
      const goDetail = (id: string) => router.push({ path: route.path, query: { mode: 'detail', id } });
      const goList = () => router.push({ path: route.path });
      const normalizeProductLabel = (name: string) => {
        if (name === 'STARIVER') return 'StaRiver';
        if (name === 'OPTIQA') return 'OptiQA';
        return name;
      };
      const resetDraft = () => {
        draft.title = '';
        draft.type = '功能新增';
        draft.source = '客户';
        draft.product = 'StaRiver';
        draft.release = '2026-Q3';
        draft.priority = 'P2';
        draft.description = '作为 用户角色，我希望 [功能/能力]，以便 业务价值。';
        draft.acceptance = 'Given 前置条件：\nWhen 操作动作：\nThen 预期结果：';
      };
      const loadProducts = async () => {
        const response = await getProductList();
        const names = (response?.list || [])
          .map((product: any) => product.code || product.product || product.name)
          .filter(Boolean);
        if (names.length) {
          productOptions.value = [...new Set(names.map(normalizeProductLabel))];
          if (!productOptions.value.includes(draft.product)) {
            [draft.product] = productOptions.value;
          }
        }
      };

      const loadRequirements = async () => {
        const response = await getRequirementList();
        requirements.value = response?.list || [];
      };

      const submitRequirement = async () => {
        const title = draft.title.trim() || '未命名产品需求';
        await addRequirement({
          title,
          type: draft.type,
          source: draft.source,
          product: draft.product,
          release: draft.release,
          priority: draft.priority,
          description: draft.description,
          acceptance: draft.acceptance,
        });
        await loadRequirements();
        productFilter.value = '全部';
        statusFilter.value = '全部状态';
        resetDraft();
        goList();
      };
      const pillClass = (value: string) => {
        const map: Record<string, string> = {
          P0: 'prm-pill--p0',
          P1: 'prm-pill--p1',
          P2: 'prm-pill--p2',
          需求池: 'prm-pill--pool',
          需求评估: 'prm-pill--orange',
          产品设计: 'prm-pill--purple',
          技术评审: 'prm-pill--orange',
          开发中: 'prm-pill--cyan',
          测试中: 'prm-pill--cyan',
          产品验收: 'prm-pill--cyan',
          待发布: 'prm-pill--purple',
          已上线: 'prm-pill--green',
        };
        return map[value] || 'prm-pill--pool';
      };

      onMounted(async () => {
        await Promise.all([loadProducts(), loadRequirements()]);
      });

      return {
        activeRequirement,
        acceptancePlaceholder,
        draft,
        filteredRequirements,
        flowSteps,
        goCreate,
        goDetail,
        goList,
        pillClass,
        priorityOptionItems,
        priorityOptions,
        productFilter,
        productFilters,
        productOptionItems,
        productOptions,
        records,
        requirementDescriptionPlaceholder,
        requirementStatuses,
        NSelect,
        sourceOptionItems,
        sourceOptions,
        StariverRichEditor,
        statCards,
        statusFilter,
        submitRequirement,
        typeOptionItems,
        typeOptions,
        viewMode,
      };
    },
  });
</script>

<style lang="less" scoped>
  .prm-page {
    display: flex;
    overflow: hidden;
    height: 100%;
    min-height: 0;
    font-family: 'Source Han Sans CN', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    color: #0f172a;
    background: #f3f4f6;
    flex-direction: column;
  }
  .prm-hero {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    min-height: 72px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
    flex-shrink: 0;
  }
  .prm-hero--list {
    min-height: 92px;
  }
  .prm-hero--detail {
    min-height: 96px;
  }
  .prm-hero h1 {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: #0f172a;
    line-height: 28px;
  }
  .prm-hero--detail h1 {
    font-size: 18px;
    line-height: 24px;
  }
  .prm-hero p {
    margin: 4px 0 0;
    font-size: 13px;
    color: #64748b;
    line-height: 18px;
  }
  .prm-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .prm-body {
    overflow: auto;
    padding: 20px 24px 24px;
    min-height: 0;
    flex: 1;
  }
  .prm-body > .prm-panel + .prm-panel {
    margin-top: 20px;
  }
  .prm-stat-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    margin-bottom: 14px;
  }
  .prm-stat-card,
  .prm-panel,
  .prm-table-card {
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
  }
  .prm-stat-card {
    display: flex;
    justify-content: center;
    padding: 0 16px;
    height: 72px;
    flex-direction: column;
    gap: 6px;
  }
  .prm-stat-card span {
    font-size: 12px;
    font-weight: 600;
    color: #64748b;
    line-height: 16px;
  }
  .prm-stat-card strong {
    font-size: 18px;
    color: #0f172a;
    line-height: 24px;
  }
  .prm-stat-card .is-indigo {
    color: #4f46e5;
  }
  .prm-stat-card .is-cyan {
    color: #0891b2;
  }
  .prm-stat-card .is-green {
    color: #16a34a;
  }
  .prm-table-card {
    overflow: hidden;
  }
  .prm-table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 14px;
    height: 52px;
    border-bottom: 1px solid #e2e8f0;
  }
  .prm-chip-group {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .prm-filter-chip {
    padding: 0 14px;
    height: 30px;
    font-size: 13px;
    font-weight: 600;
    border: 1px solid #e2e8f0;
    border-radius: 999px;
    color: #334155;
    background: #ffffff;
    cursor: pointer;
  }
  .prm-filter-chip--active {
    border-color: #0f172a;
    color: #ffffff;
    background: #0f172a;
  }
  .prm-native-select {
    padding: 0 10px;
    width: 140px;
    height: 30px;
    font-size: 13px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    color: #334155;
    background: #ffffff;
  }
  .prm-table {
    overflow-x: auto;
    min-width: 980px;
  }
  .prm-table__row {
    display: grid;
    align-items: center;
    width: 100%;
    border: 0;
    border-bottom: 1px solid #e2e8f0;
    text-align: left;
    color: #0f172a;
    background: #ffffff;
    grid-template-columns: minmax(320px, 1.8fr) minmax(140px, 1fr) minmax(100px, 0.8fr) 100px 100px 160px 80px;
  }
  .prm-table__row > span {
    padding: 0 14px;
    min-width: 0;
  }
  .prm-table__row--head {
    height: 36px;
    font-size: 11px;
    font-weight: 700;
    color: #64748b;
    background: #f8fafc;
  }
  .prm-table__row--body {
    height: 60px;
    font-size: 12px;
    cursor: pointer;
  }
  .prm-table__row--body:hover {
    background: #f8fafc;
  }
  .prm-demand-title {
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  .prm-demand-title em {
    font-size: 11px;
    color: #4f46e5;
    font-style: normal;
    line-height: 12px;
  }
  .prm-demand-title strong {
    overflow: hidden;
    font-size: 14px;
    font-weight: 700;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .prm-product-tag {
    display: inline-flex;
    align-items: center;
    padding: 0 7px;
    height: 20px;
    font-size: 12px;
    font-weight: 700;
    border-radius: 4px;
    font-style: normal;
  }
  .prm-product-tag--stariver {
    color: #4f46e5;
    background: #eef2ff;
  }
  .prm-product-tag--optiqa {
    color: #0891b2;
    background: #e0f7fb;
  }
  .prm-version {
    color: #4f46e5;
  }
  .prm-muted {
    color: #64748b;
  }
  .prm-pill {
    display: inline-flex;
    align-items: center;
    padding: 0 8px;
    height: 20px;
    font-size: 12px;
    font-weight: 700;
    border-radius: 999px;
    white-space: nowrap;
    gap: 5px;
    font-style: normal;
    line-height: 20px;
  }
  .prm-pill::before {
    width: 6px;
    height: 6px;
    border-radius: 999px;
    content: '';
  }
  .prm-pill--p0,
  .prm-pill--p0::before {
    color: #dc2626;
  }
  .prm-pill--p0 {
    background: rgb(220 38 38 / 10%);
  }
  .prm-pill--p0::before {
    background: #dc2626;
  }
  .prm-pill--p1,
  .prm-pill--p1::before,
  .prm-pill--orange,
  .prm-pill--orange::before {
    color: #d97706;
  }
  .prm-pill--p1,
  .prm-pill--orange {
    background: rgb(217 119 6 / 10%);
  }
  .prm-pill--p1::before,
  .prm-pill--orange::before {
    background: #d97706;
  }
  .prm-pill--p2,
  .prm-pill--p2::before,
  .prm-pill--pool,
  .prm-pill--pool::before {
    color: #64748b;
  }
  .prm-pill--p2,
  .prm-pill--pool {
    background: rgb(100 116 139 / 10%);
  }
  .prm-pill--p2::before,
  .prm-pill--pool::before {
    background: #64748b;
  }
  .prm-pill--purple {
    color: #4f46e5;
    background: rgb(79 70 229 / 10%);
  }
  .prm-pill--purple::before {
    background: #4f46e5;
  }
  .prm-pill--cyan {
    color: #0891b2;
    background: rgb(8 145 178 / 10%);
  }
  .prm-pill--cyan::before {
    background: #0891b2;
  }
  .prm-pill--green {
    color: #16a34a;
    background: rgb(22 163 74 / 10%);
  }
  .prm-pill--green::before {
    background: #16a34a;
  }
  .prm-detail-code {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
  }
  .prm-detail-code span {
    font-size: 13px;
    font-weight: 700;
    color: #4f46e5;
  }
  .prm-panel {
    padding: 16px;
    min-width: 0;
  }
  .prm-panel h2,
  .prm-section-title h2 {
    margin: 0;
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
    line-height: 20px;
  }
  .prm-section-title p {
    margin: 4px 0 14px;
    font-size: 12px;
    color: #94a3b8;
    line-height: 17px;
  }
  .prm-flow-panel {
    margin-bottom: 14px;
  }
  .prm-flow-board {
    overflow-x: auto;
    margin-top: 14px;
    padding: 46px 64px 34px;
    border-radius: 8px;
    background: #f8fafc;
  }
  .prm-flow {
    display: flex;
    align-items: flex-start;
    width: 100%;
    min-width: 840px;
  }
  .prm-flow-step {
    position: relative;
    display: flex;
    align-items: center;
    min-width: 84px;
    flex: 1 0 84px;
    flex-direction: column;
    gap: 8px;
  }
  .prm-flow-node {
    position: relative;
    z-index: 2;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80px;
    height: 50px;
    font-size: 12px;
    border: 1.2px solid #94a3b8;
    border-radius: 8px;
    color: #0f172a;
    background: #ffffff;
    line-height: 16px;
  }
  .prm-flow-step small {
    font-size: 10px;
    color: #334155;
    line-height: 12px;
  }
  .prm-flow-line {
    position: absolute;
    top: 25px;
    left: 50%;
    z-index: 1;
    width: 100%;
    height: 2px;
    background: #94a3b8;
  }
  .prm-flow-step--done .prm-flow-node {
    border-color: #16a34a;
    color: #0f172a;
    background: #ecfdf5;
  }
  .prm-flow-step--done .prm-flow-line {
    background: #16a34a;
  }
  .prm-flow-step--current .prm-flow-node {
    font-size: 18px;
    font-weight: 700;
    border-color: #4f46e5;
    color: #4f46e5;
  }
  .prm-flow-step--current .prm-flow-line {
    background: #94a3b8;
  }
  .prm-flow-step--diamond .prm-flow-node {
    margin-top: 8px;
    width: 34px;
    height: 34px;
    border-radius: 0;
    transform: rotate(45deg);
  }
  .prm-flow-step--diamond .prm-flow-node span {
    transform: rotate(-45deg);
  }
  .prm-flow-step--done:first-child .prm-flow-node,
  .prm-flow-step:last-child .prm-flow-node {
    margin-top: 7px;
    width: 36px;
    height: 36px;
    border-radius: 999px;
  }
  .prm-flow-dot {
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: #16a34a;
  }
  .prm-flow-stop {
    width: 10px;
    height: 10px;
    border-radius: 2px;
    background: #94a3b8;
  }
  .prm-detail-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 340px;
    gap: 16px;
  }
  .prm-detail-left {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 14px;
  }
  .prm-story-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 10px;
  }
  .prm-story,
  .prm-ac-box {
    padding: 12px 10px;
    border-radius: 6px;
    background: #f8fafc;
  }
  .prm-story p,
  .prm-ac-box p {
    display: flex;
    margin: 0 0 6px;
    font-size: 13px;
    color: #334155;
    gap: 8px;
    line-height: 18px;
  }
  .prm-story p:last-child,
  .prm-ac-box p:last-child {
    margin-bottom: 0;
  }
  .prm-story strong {
    color: #7c3aed;
  }
  .prm-ac-panel {
    min-height: 276px;
  }
  .prm-ac-box {
    margin-top: 10px;
  }
  .prm-ac-box p::before {
    margin-top: 6px;
    color: #334155;
    content: '•';
  }
  .prm-ac-box span {
    flex-shrink: 0;
    color: #334155;
  }
  .prm-ac-box strong {
    font-weight: 500;
    color: #16a34a;
  }
  .prm-record-panel {
    min-height: 538px;
  }
  .prm-record-list {
    margin-top: 10px;
  }
  .prm-record {
    display: flex;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #eef2f7;
  }
  .prm-record__icon {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 28px;
    height: 28px;
    font-size: 12px;
    border-radius: 999px;
    color: #4f46e5;
    background: #e9efff;
    flex-shrink: 0;
  }
  .prm-record__icon--done {
    color: #16a34a;
    background: #ecfdf5;
  }
  .prm-record strong {
    display: block;
    font-size: 13px;
    color: #0f172a;
    line-height: 18px;
  }
  .prm-record p {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 2px 0 0;
    min-width: 0;
    font-size: 12px;
    color: #0f172a;
    gap: 12px;
    line-height: 18px;
  }
  .prm-record time {
    white-space: nowrap;
    color: #94a3b8;
  }

  @media (max-width: 1280px) {
    .prm-form-grid,
    .prm-stat-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .prm-detail-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 760px) {
    .prm-hero,
    .prm-table-toolbar {
      align-items: flex-start;
      flex-direction: column;
      gap: 12px;
      padding-top: 14px;
      padding-bottom: 14px;
    }
    .prm-stat-grid {
      grid-template-columns: 1fr;
    }
  }
</style>

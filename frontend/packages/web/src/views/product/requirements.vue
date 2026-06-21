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
            <i class="prm-pill" :class="pillClass(activeRequirement.status)">{{ activeRequirement.status }}</i>
          </div>
          <div class="sr-product-hero__title-row">
            <h1>{{ activeRequirement.detailTitle }}</h1>
            <button v-if="showEditIcon" class="sr-icon-action" title="编辑需求" @click="enterEditMode">
              <span v-html="editIcon"></span>
            </button>
            <button v-if="showDeleteIcon" class="sr-icon-action" title="删除需求" @click="handleDelete">
              <span v-html="deleteIcon"></span>
            </button>
          </div>
          <p>
            {{ activeRequirement.product }} · {{ activeRequirement.type }} · 负责人 {{ activeRequirement.owner }} ·
            {{ activeRequirement.source }}需求 · 期望上线
            {{ activeRequirement.expectedRelease || activeRequirement.version }}
          </p>
        </div>
        <div class="prm-hero-actions">
          <button v-if="showRevokeBtn" class="sr-btn-revoke" @click="handleRevoke">撤回</button>
          <button v-if="showResubmitBtn" class="prm-detail-primary-btn" @click="handleResubmit">重新提交评审 →</button>
          <button v-if="showReturnBtn" class="prm-detail-return-btn" @click="handleReturnStage">退回上一阶段</button>
          <button v-if="showAdvanceBtn" class="prm-detail-primary-btn" @click="handleAdvanceStage">推进阶段 →</button>
          <button v-if="showPublishBtn" class="prm-detail-primary-btn" @click="goProductRelease">前往产品发布 →</button>
        </div>
      </header>

      <main class="prm-body">
        <template v-if="editMode">
          <section class="sr-panel">
            <h2>编辑产品需求</h2>
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

          <div class="sr-create-actions" style="display: flex; justify-content: flex-end; margin-top: 16px; gap: 8px">
            <button class="sr-btn sr-btn--ghost" @click="cancelEditMode">取消</button>
            <button class="sr-btn sr-btn--primary" @click="handleEditSubmit">保存修改</button>
          </div>
        </template>

        <template v-else>
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
                    <template v-else>
                      <span>{{ step.text }}</span>
                      <em v-if="step.state === 'current'" class="prm-flow-current-dot"></em>
                      <small v-if="step.state === 'current'" class="prm-flow-current-label">当前</small>
                    </template>
                  </div>
                  <small v-if="['start', 'diamond', 'end'].includes(step.shape)">{{ step.label }}</small>
                  <span v-if="step.transitionLabel && index < flowSteps.length - 1" class="prm-flow-transition-label">
                    {{ step.transitionLabel }}
                  </span>
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
                <article
                  v-for="record in detailRecords"
                  :key="`${record.title}-${record.time || record.owner}`"
                  class="prm-record"
                >
                  <span :class="recordIconClass(record.state)">
                    {{ recordIconText(record.state) }}
                  </span>
                  <div>
                    <strong>{{ record.title }}</strong>
                    <div v-if="record.content" class="prm-record__content" v-html="record.content"></div>
                    <div v-if="record.attachments?.length" class="prm-record__attachments">
                      <span v-for="attachment in record.attachments" :key="attachment.id">{{ attachment.name }}</span>
                    </div>
                    <p>
                      <span>{{ record.owner }}</span>
                      <time v-if="record.time">{{ formatRecordTime(record.time) }}</time>
                    </p>
                  </div>
                </article>
              </div>
            </aside>
          </section>
        </template>
      </main>
    </template>

    <component
      :is="NModal"
      v-model:show="designSubmitVisible"
      preset="card"
      class="prm-stage-modal"
      :mask-closable="false"
    >
      <template #header>
        <div class="prm-stage-modal__title">{{ stageModalTitle }}</div>
      </template>
      <div class="prm-stage-modal__body">
        <component
          :is="StariverRichEditor"
          v-model="designAdvanceForm.content"
          v-model:attachment-files="designAdvanceAttachments"
          min-height="large"
          placeholder="输入内容或上传文件"
        />
        <div v-if="requiresProductLink" class="prm-stage-link-grid">
          <label>
            <span>关联产品模块</span>
            <component
              :is="NSelect"
              v-model:value="designAdvanceForm.moduleId"
              :options="moduleOptions"
              placeholder="请选择模块"
            />
          </label>
          <label>
            <span>预发布版本</span>
            <component
              :is="NSelect"
              v-model:value="designAdvanceForm.versionId"
              :options="versionOptions"
              placeholder="请选择版本"
            />
          </label>
        </div>
      </div>
      <template #footer>
        <div class="prm-stage-modal__actions">
          <button class="sr-btn sr-btn--ghost" @click="closeAdvanceModal">取消</button>
          <button class="sr-btn sr-btn--primary" @click="submitAdvanceStage">确定</button>
        </div>
      </template>
    </component>
  </section>
</template>

<script lang="ts">
  import { computed, defineComponent, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { NModal, NSelect, useDialog, useMessage } from 'naive-ui';
  import dayjs from 'dayjs';

  import deleteIcon from '@/assets/icons/project/delete.svg?raw';
  import editIcon from '@/assets/icons/project/edit.svg?raw';

  import { ProcessStatusEnum } from '@lib/shared/enums/process';

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import StariverRichEditor from '@/components/business/stariver-rich-editor/index.vue';

  import {
    addRequirement,
    advanceRequirementStage,
    deleteRequirement,
    getProductDetail,
    getProductList,
    getRequirementDetail,
    getRequirementList,
    returnRequirementStage,
    revokeRequirementReview,
    submitRequirementForReview,
    updateRequirement,
  } from '@/api/modules/productManagement';
  import { useUserStore } from '@/store';

  import { ProductRouteEnum } from '@/enums/routeEnum';

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
      const message = useMessage();
      const dialog = useDialog();
      const userStore = useUserStore();
      const productFilter = ref('全部');
      const statusFilter = ref('全部状态');
      const loadingDetail = ref(false);
      const editMode = ref(false);
      const designSubmitVisible = ref(false);
      const designAdvanceForm = reactive({
        content: '',
        moduleId: null as string | null,
        versionId: null as string | null,
      });
      const designAdvanceAttachments = ref<Array<{ id: string; name: string; size?: number }>>([]);
      const stageActionMode = ref<'advance' | 'return'>('advance');
      const moduleOptions = ref<Array<{ label: string; value: string }>>([]);
      const versionOptions = ref<Array<{ label: string; value: string }>>([]);

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
        '需求评审',
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
          value: String(requirements.value.filter((item) => ['需求评审', '产品设计'].includes(item.status)).length),
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

      const requirementDetail = ref<any>(null);

      const activeRequirement = computed(() => {
        if (requirementDetail.value) return requirementDetail.value;
        const id = String(route.query.id || '');
        return (
          requirements.value.find((item) => item.id === id) ||
          requirements.value.find((item) => item.rawId === id) ||
          requirements.value[0] || {
            id: '',
            rawId: '',
            detailTitle: '',
            product: '',
            type: '',
            owner: '',
            source: '',
            version: '',
            description: '',
            acceptance: '',
            status: '',
            stage: '',
            approvalStatus: '',
            createUser: '',
            expectedRelease: '',
            records: [],
          }
        );
      });

      const showEditIcon = computed(() => activeRequirement.value.availableActions?.edit ?? false);

      const showDeleteIcon = computed(() => activeRequirement.value.availableActions?.delete ?? false);

      const showRevokeBtn = computed(() => activeRequirement.value.availableActions?.revoke ?? false);

      const showResubmitBtn = computed(() => activeRequirement.value.availableActions?.resubmit ?? false);

      const currentWorkflowStage = computed(() =>
        (activeRequirement.value.workflowStages || []).find(
          (stage: any) => stage.name === (activeRequirement.value.stage || activeRequirement.value.status)
        )
      );
      const showPublishBtn = computed(() => currentWorkflowStage.value?.key === 'RELEASE');
      const showAdvanceBtn = computed(
        () => (activeRequirement.value.availableActions?.advance ?? false) && !showPublishBtn.value
      );
      const showReturnBtn = computed(() => activeRequirement.value.availableActions?.return ?? false);
      const requiresProductLink = computed(
        () => stageActionMode.value === 'advance' && Boolean(currentWorkflowStage.value?.requiresProductLink)
      );
      const stageModalTitle = computed(() =>
        stageActionMode.value === 'return'
          ? '退回原因'
          : (
              {
                产品设计: '提交产品设计',
                技术评审: '提交产品详设',
                开发: '提交开发说明',
                测试: '提交测试结果',
                产品验收: '提交验收说明',
              } as Record<string, string>
            )[activeRequirement.value.stage] || `提交${activeRequirement.value.stage || ''}`
      );

      const detailRecords = computed(() => activeRequirement.value.records || []);

      const flowSteps = computed(() => {
        const configuredStages = (activeRequirement.value.workflowStages || []).map((stage: any) => ({
          key: stage.name,
          label: stage.name,
          text: stage.name,
          shape: stage.key === 'COMPLETED' ? 'end' : 'rect',
        }));
        const stages = [
          { key: '提交', label: '提交', text: '', shape: 'start' },
          { key: '需求池', label: '需求池', text: '需求池', shape: 'pill' },
          { key: '需求评审', label: '需求评审', text: '?', shape: 'diamond' },
          ...configuredStages,
        ];
        const currentStage = activeRequirement.value.stage || activeRequirement.value.status;
        let found = false;
        return stages.map((stage) => {
          if (found) return { ...stage, state: 'todo' };
          if (stage.key === currentStage || stage.label === currentStage) {
            found = true;
            return { ...stage, state: 'current' };
          }
          return {
            ...stage,
            state: 'done',
            transitionLabel:
              ['需求评审', '技术评审'].includes(stage.key) &&
              activeRequirement.value.approvalStatus === ProcessStatusEnum.APPROVED
                ? '通过'
                : '',
          };
        });
      });

      const goCreate = () => router.push({ path: route.path, query: { mode: 'create' } });
      const goList = () => {
        requirementDetail.value = null;
        editMode.value = false;
        router.push({ path: route.path });
      };
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

      const loadRequirementDetail = async (id: string) => {
        loadingDetail.value = true;
        try {
          const data = await getRequirementDetail(id);
          if (data) {
            requirementDetail.value = data;
          }
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error('加载需求详情失败', e);
        } finally {
          loadingDetail.value = false;
        }
      };

      const goDetail = async (id: string) => {
        router.push({ path: route.path, query: { mode: 'detail', id } });
        await loadRequirementDetail(id);
      };

      const enterEditMode = () => {
        const item = activeRequirement.value;
        draft.title = item.detailTitle || item.title || '';
        draft.type = item.type || '功能新增';
        draft.source = item.source || '客户';
        draft.product = item.product || 'StaRiver';
        draft.release = item.expectedRelease || item.version || '';
        draft.priority = item.priority || 'P2';
        draft.description = item.description || '';
        draft.acceptance = item.acceptance || '';
        editMode.value = true;
      };

      const cancelEditMode = () => {
        editMode.value = false;
      };

      const handleEditSubmit = async () => {
        const item = activeRequirement.value;
        const title = draft.title.trim() || '未命名产品需求';
        await updateRequirement({
          id: item.rawId,
          title,
          type: draft.type,
          source: draft.source,
          product: draft.product,
          release: draft.release,
          priority: draft.priority,
          description: draft.description,
          acceptance: draft.acceptance,
        });
        editMode.value = false;
        requirementDetail.value = null;
        await Promise.all([loadRequirements(), loadRequirementDetail(item.rawId)]);
        message.success('需求已更新');
      };

      const handleDelete = () => {
        dialog.warning({
          title: '确认删除',
          content: '确定要删除该需求吗？删除后不可恢复。',
          positiveText: '确认删除',
          negativeText: '取消',
          onPositiveClick: async () => {
            await deleteRequirement(activeRequirement.value.rawId);
            message.success('需求已删除');
            await loadRequirements();
            goList();
          },
        });
      };

      const handleRevoke = () => {
        dialog.warning({
          title: '确认撤回',
          content: '撤回后需求将回到需求池，不再需要审批。',
          positiveText: '确认撤回',
          negativeText: '取消',
          onPositiveClick: async () => {
            const item = activeRequirement.value;
            await revokeRequirementReview(item.rawId);
            requirementDetail.value = null;
            await loadRequirements();
            await loadRequirementDetail(item.rawId);
            message.success('已撤回');
          },
        });
      };

      const handleResubmit = async () => {
        const item = activeRequirement.value;
        await submitRequirementForReview(item.rawId);
        requirementDetail.value = null;
        await loadRequirements();
        await loadRequirementDetail(item.rawId);
        message.success('已重新提交评审');
      };

      const loadStageLinkOptions = async () => {
        const { productId } = activeRequirement.value;
        if (!productId) return;
        const product = await getProductDetail(productId);
        moduleOptions.value = (product?.modules || []).map((item: any) => ({ label: item.name, value: item.id }));
        versionOptions.value = (product?.versions || []).map((item: any) => ({
          label: item.version,
          value: item.id,
        }));
      };

      const handleAdvanceStage = async () => {
        stageActionMode.value = 'advance';
        if (currentWorkflowStage.value?.requiresProductLink) await loadStageLinkOptions();
        designSubmitVisible.value = true;
      };

      const handleReturnStage = () => {
        stageActionMode.value = 'return';
        designSubmitVisible.value = true;
      };

      const goProductRelease = () => {
        router.push({ name: ProductRouteEnum.PRODUCT_DETAIL, params: { id: activeRequirement.value.productId } });
      };

      const closeAdvanceModal = () => {
        designSubmitVisible.value = false;
        designAdvanceForm.content = '';
        designAdvanceForm.moduleId = null;
        designAdvanceForm.versionId = null;
        designAdvanceAttachments.value = [];
      };

      const submitAdvanceStage = async () => {
        const item = activeRequirement.value;
        const hasAttachments = designAdvanceAttachments.value.length > 0;
        const plainText = designAdvanceForm.content.replace(/<[^>]+>/g, '').trim();
        if (!plainText && !hasAttachments) {
          message.warning('请填写阶段说明或上传附件');
          return;
        }
        if (requiresProductLink.value && (!designAdvanceForm.moduleId || !designAdvanceForm.versionId)) {
          message.warning('请选择关联产品模块和预发布版本');
          return;
        }
        const payload = {
          content: designAdvanceForm.content,
          attachmentIds: designAdvanceAttachments.value.map((file) => file.id),
          moduleId: designAdvanceForm.moduleId || undefined,
          versionId: designAdvanceForm.versionId || undefined,
        };
        if (stageActionMode.value === 'return') await returnRequirementStage(item.rawId, payload);
        else await advanceRequirementStage(item.rawId, payload);
        requirementDetail.value = null;
        await loadRequirements();
        await loadRequirementDetail(item.rawId);
        closeAdvanceModal();
        message.success(stageActionMode.value === 'return' ? '已退回上一阶段' : '阶段已推进');
      };

      const recordIconClass = (state: string) => {
        if (state === 'done') return 'prm-record__icon prm-record__icon--done';
        if (state === 'rejected') return 'prm-record__icon prm-record__icon--rejected';
        return 'prm-record__icon';
      };

      const recordIconText = (state: string) => {
        if (state === 'done') return '✓';
        if (state === 'rejected') return '×';
        return '...';
      };

      const formatRecordTime = (value: number | string) => dayjs(value).format('YYYY-MM-DD HH:mm');
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
        if (route.query.mode === 'detail' && route.query.id) {
          await loadRequirementDetail(String(route.query.id));
        }
      });

      return {
        activeRequirement,
        acceptancePlaceholder,
        cancelEditMode,
        closeAdvanceModal,
        designAdvanceAttachments,
        designAdvanceForm,
        designSubmitVisible,
        deleteIcon,
        detailRecords,
        draft,
        editIcon,
        editMode,
        enterEditMode,
        filteredRequirements,
        flowSteps,
        formatRecordTime,
        goCreate,
        goDetail,
        goList,
        goProductRelease,
        handleAdvanceStage,
        handleReturnStage,
        handleDelete,
        handleEditSubmit,
        handleResubmit,
        handleRevoke,
        loadingDetail,
        pillClass,
        priorityOptionItems,
        priorityOptions,
        productFilter,
        productFilters,
        productOptionItems,
        productOptions,
        recordIconClass,
        recordIconText,
        requirementDescriptionPlaceholder,
        requirementStatuses,
        NModal,
        NSelect,
        moduleOptions,
        versionOptions,
        requiresProductLink,
        stageModalTitle,
        showAdvanceBtn,
        showPublishBtn,
        showReturnBtn,
        showDeleteIcon,
        showEditIcon,
        showResubmitBtn,
        showRevokeBtn,
        sourceOptionItems,
        sourceOptions,
        StariverRichEditor,
        statCards,
        statusFilter,
        submitAdvanceStage,
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
    border-width: 2px;
    border-color: #5b5bf7;
    color: #0f172a;
    background: #ffffff;
    box-shadow: 0 0 0 3px rgb(91 91 247 / 8%);
  }
  .prm-flow-step--current .prm-flow-node span:not(.prm-flow-dot):not(.prm-flow-stop) {
    font-size: 12px;
    font-weight: 600;
    color: #0f172a;
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
  .prm-flow-current-dot {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: #5b5bf7;
    box-shadow: 0 0 0 2px #eef2ff;
  }
  .prm-flow-current-label {
    position: absolute;
    right: 10px;
    bottom: 6px;
    font-size: 11px;
    font-weight: 600;
    color: #5b5bf7;
    line-height: 12px;
    letter-spacing: 0;
  }
  .prm-flow-transition-label {
    position: absolute;
    top: 16px;
    right: -16px;
    z-index: 3;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 8px;
    height: 24px;
    font-size: 12px;
    font-weight: 600;
    border: 1px solid #818cf8;
    border-radius: 999px;
    color: #4f46e5;
    background: #ffffff;
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
  .prm-record__icon--rejected {
    color: #dc2626;
    background: #fee2e2;
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
  .prm-record__content {
    margin-top: 4px;
    padding: 6px 8px;
    font-size: 12px;
    border-radius: 4px;
    color: #64748b;
    background: #f8fafc;
    line-height: 18px;
  }
  .prm-record__attachments {
    display: flex;
    margin-top: 4px;
    flex-wrap: wrap;
    gap: 4px;
  }
  .prm-record__attachments span {
    padding: 2px 6px;
    font-size: 11px;
    border-radius: 4px;
    color: #4f46e5;
    background: #eef2ff;
  }
  .prm-hero-actions {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    gap: 8px;
  }
  .prm-detail-primary-btn {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 18px;
    height: 42px;
    font-size: 14px;
    font-weight: 600;
    border: 0;
    border-radius: 8px;
    white-space: nowrap;
    color: #ffffff;
    background: #0f172a;
    box-shadow: 0 10px 24px rgb(15 23 42 / 12%);
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  }
  .prm-detail-primary-btn:hover {
    background: #111827;
    box-shadow: 0 12px 28px rgb(15 23 42 / 18%);
    transform: translateY(-1px);
  }
  .prm-detail-return-btn {
    padding: 0 16px;
    height: 42px;
    font-size: 14px;
    font-weight: 600;
    border: 1px solid #ef4444;
    border-radius: 8px;
    color: #ef4444;
    background: #ffffff;
    cursor: pointer;
  }
  .prm-detail-return-btn:hover {
    color: #ffffff;
    background: #ef4444;
  }
  .sr-btn-revoke {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 18px;
    height: 42px;
    font-size: 14px;
    font-weight: 600;
    border: 1px solid #dc2626;
    border-radius: 8px;
    white-space: nowrap;
    color: #dc2626;
    background: #ffffff;
    box-shadow: 0 4px 14px rgb(220 38 38 / 8%);
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
  }
  .sr-btn-revoke:hover {
    color: #ffffff;
    background: #dc2626;
    transform: translateY(-1px);
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
    stroke: currentcolor !important;
  }
  .sr-icon-action svg [fill]:not([fill='none']) {
    fill: currentcolor !important;
  }
  .sr-product-hero__title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  :deep(.prm-stage-modal) {
    width: min(1120px, calc(100vw - 64px));
    border-radius: 14px;
  }
  .prm-stage-modal__title {
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
  }
  .prm-stage-modal__body {
    padding-top: 8px;
  }
  .prm-stage-link-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
    margin-top: 16px;
  }
  .prm-stage-link-grid label {
    display: flex;
    gap: 8px;
    flex-direction: column;
  }
  .prm-stage-link-grid label > span {
    font-size: 13px;
    font-weight: 600;
    color: #334155;
  }
  .prm-stage-modal__actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
  .sr-form-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 18px;
  }
  .sr-field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .sr-field__label {
    font-size: 13px;
    font-weight: 600;
    color: #334155;
    line-height: 18px;
  }
  .sr-field__req {
    color: #dc2626;
  }
  .sr-input {
    padding: 0 12px;
    min-height: 38px;
    font-size: 14px;
    border: 1px solid #dbe3ef;
    border-radius: 6px;
    color: #0f172a;
    background: #ffffff;
    outline: none;
  }
  .sr-native-control {
    min-height: 38px;
  }
  .sr-btn {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0 18px;
    height: 36px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 6px;
    cursor: pointer;
  }
  .sr-btn--ghost {
    border: 1px solid #dbe3ef;
    color: #334155;
    background: #ffffff;
  }
  .sr-btn--primary {
    border: 1px solid #0f172a;
    color: #ffffff;
    background: #0f172a;
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

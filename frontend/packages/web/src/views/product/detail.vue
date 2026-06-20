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
            <button class="sr-secondary-action" @click="openVersionCreate">+ 新增版本</button>
          </div>
          <div class="sr-roadmap-timeline">
            <article v-for="row in roadmapRows" :key="row.id" class="sr-roadmap-item" @click="openVersionDetail(row)">
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
                  <p>{{ plainText(row.description) }}</p>
                  <button
                    class="sr-roadmap-expand"
                    :class="{ 'is-open': isRoadmapExpanded(row.id) }"
                    :aria-label="isRoadmapExpanded(row.id) ? '收起版本说明' : '展开版本说明'"
                    @click.stop="toggleRoadmap(row.id)"
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

    <div v-if="versionCreateVisible" class="sr-modal-mask">
      <div class="sr-version-modal sr-version-modal--form">
        <header class="sr-version-modal__header">
          <h2>新增版本 · {{ product.code || product.name }}</h2>
          <button class="sr-version-modal__close" @click="versionCreateVisible = false">×</button>
        </header>
        <div class="sr-version-modal__body">
          <div class="sr-version-form">
            <label>
              <span>版本号</span>
              <input v-model="versionForm.version" class="sr-input" placeholder="v4.2" />
            </label>
            <label>
              <span>产品状态</span>
              <select v-model="versionForm.status" class="sr-input">
                <option value="规划中">规划中</option>
                <option value="开发中">开发中</option>
                <option value="已发布">已发布</option>
              </select>
            </label>
            <label>
              <span>计划发布日期</span>
              <input v-model="versionForm.releaseDate" class="sr-input" type="date" />
            </label>
            <label>
              <span>产品经理（PD）</span>
              <select v-model="versionForm.productOwnerId" class="sr-input" @change="syncVersionOwner('productOwner')">
                <option v-for="option in userOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <label>
              <span>研发负责人</span>
              <select v-model="versionForm.devOwnerId" class="sr-input" @change="syncVersionOwner('devOwner')">
                <option v-for="option in userOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
            <label class="sr-version-form__full">
              <span>版本说明</span>
              <StariverRichEditor
                v-model="versionForm.description"
                v-model:attachment-files="versionForm.attachmentFiles"
                min-height="large"
                placeholder="请输入版本说明，例如：[新功能]、[修复]"
              />
            </label>
          </div>
        </div>
        <footer class="sr-version-modal__footer">
          <button class="sr-btn sr-btn--ghost" @click="versionCreateVisible = false">取消</button>
          <button class="sr-btn sr-btn--primary" :disabled="versionSaving" @click="handleVersionSave">
            {{ versionSaving ? '保存中...' : '保存' }}
          </button>
        </footer>
      </div>
    </div>

    <div v-if="versionDetailVisible && selectedVersion" class="sr-modal-mask">
      <div class="sr-version-modal sr-version-modal--detail">
        <header class="sr-version-modal__header">
          <div>
            <h2>{{ selectedVersion.version }}</h2>
            <div class="sr-version-meta">
              <span>产品经理：{{ ownerLabel(selectedVersion.productOwner, selectedVersion.productOwnerActive) }}</span>
              <span>研发负责人：{{ ownerLabel(selectedVersion.devOwner, selectedVersion.devOwnerActive) }}</span>
              <span>发布日期：{{ selectedVersion.releaseDate || '--' }}</span>
              <em :class="['sr-status', `sr-status--${selectedVersion.statusType}`]">{{ selectedVersion.status }}</em>
            </div>
          </div>
          <div class="sr-version-modal__actions">
            <button
              v-if="selectedVersion.status === '规划中'"
              class="sr-version-action sr-version-action--success"
              @click="handleVersionStatus('开发中')"
            >
              → 开发
            </button>
            <button
              v-if="selectedVersion.status === '开发中'"
              class="sr-version-action sr-version-action--success"
              @click="handleVersionStatus('已发布')"
            >
              → 发布
            </button>
            <button class="sr-version-action sr-version-action--danger" @click="handleVersionDelete">删除</button>
            <button class="sr-version-modal__close" @click="versionDetailVisible = false">×</button>
          </div>
        </header>
        <div class="sr-version-modal__body">
          <div class="sr-version-detail-block">
            <div class="sr-version-detail-title">版本说明</div>
            <div
              v-if="selectedVersion.description"
              class="sr-version-detail-desc"
              v-html="selectedVersion.description"
            ></div>
            <div v-else class="sr-version-detail-desc sr-version-detail-desc--empty">暂无版本说明</div>
            <div v-if="selectedVersion.attachments?.length" class="sr-version-attachments">
              <a
                v-for="file in selectedVersion.attachments"
                :key="file.id"
                :href="`/attachment/download/${file.id}`"
                target="_blank"
                rel="noreferrer"
              >
                {{ file.name || file.id }}
              </a>
            </div>
          </div>
          <div class="sr-version-detail-block">
            <div class="sr-version-detail-title">关联需求</div>
            <div class="sr-table sr-table--requirements">
              <div class="sr-table__head">
                <span>ID</span>
                <span>标题</span>
                <span>模块</span>
                <span>来源</span>
                <span>优先级</span>
                <span>状态</span>
              </div>
              <div v-for="row in selectedVersionRequirements" :key="row.id" class="sr-table__row">
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
              </div>
              <div v-if="!selectedVersionRequirements.length" class="sr-version-empty">暂无关联需求</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
  /* eslint-disable no-use-before-define */
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from 'naive-ui';

  import deleteIcon from '@/assets/icons/project/delete.svg?raw';
  import editIcon from '@/assets/icons/project/edit.svg?raw';

  import StariverRichEditor from '@/components/business/stariver-rich-editor/index.vue';

  import {
    addProductVersion,
    deleteProductVersion,
    getProductDetail,
    getRoadmap,
    getUserOptions,
    updateProductVersionStatus,
  } from '@/api/modules/productManagement';

  const route = useRoute();
  const router = useRouter();
  const message = useMessage();

  const product = ref<any>({});
  const activeTab = ref('overview');
  const productRoadmap = ref<any[]>([]);
  const expandedModules = reactive<Record<string, boolean>>({});
  const expandedRoadmap = reactive<Record<string, boolean>>({});
  const versionCreateVisible = ref(false);
  const versionDetailVisible = ref(false);
  const versionSaving = ref(false);
  const selectedVersion = ref<any>(null);
  const userOptions = ref<Array<{ label: string; value: string }>>([]);
  const versionForm = reactive({
    version: '',
    status: '规划中',
    releaseDate: '',
    description: '[新功能]\n\n[修复]',
    productOwnerId: '',
    productOwner: '',
    devOwnerId: '',
    devOwner: '',
    attachmentFiles: [] as Array<{ id: string; name: string; size?: number }>,
  });

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

  const selectedVersionRequirements = computed(() => {
    if (!selectedVersion.value) {
      return [];
    }
    return requirementRows.value.filter((row) => row.version === selectedVersion.value.version);
  });

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

    const normalizedRows = rows.map((row) => {
      const status = row.status === '已上线' ? '已发布' : row.status;
      return {
        ...row,
        status,
        statusType: getRoadmapStatusType(status),
        description: row.description || '',
        attachments: Array.isArray(row.attachments) ? row.attachments : [],
        attachmentIds: Array.isArray(row.attachmentIds) ? row.attachmentIds : [],
      };
    });
    if (
      product.value.id &&
      product.value.version &&
      !normalizedRows.some((row) => row.version === product.value.version)
    ) {
      normalizedRows.push({
        id: `${product.value.id}-${product.value.version}`,
        productId: product.value.id,
        version: product.value.version,
        releaseDate: product.value.releaseDate || '--',
        status: product.value.status === '已上线' ? '已发布' : product.value.status || '规划中',
        statusType: getRoadmapStatusType(product.value.status || '规划中'),
        pendingCount: 0,
        productOwnerId: product.value.productOwnerId || '',
        productOwner: product.value.productOwner || '',
        productOwnerActive: Boolean(activeUserOption(product.value.productOwnerId)),
        devOwnerId: product.value.devOwnerId || '',
        devOwner: product.value.devOwner || '',
        devOwnerActive: Boolean(activeUserOption(product.value.devOwnerId)),
        description: '',
        attachments: [],
        attachmentIds: [],
      });
    }
    return normalizedRows;
  });

  async function loadProductDetail() {
    const id = route.params.id as string;
    if (id) {
      try {
        const [data, roadmap] = await Promise.all([getProductDetail(id), getRoadmap()]);
        product.value = data || {};
        const matchedRoadmap = (roadmap || []).filter((r: any) => r.productId === id);
        productRoadmap.value = matchedRoadmap;
        if (selectedVersion.value) {
          selectedVersion.value =
            roadmapRows.value.find((row) => row.id === selectedVersion.value.id) || selectedVersion.value;
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('获取产品详情失败', e);
        product.value = {};
      }
    }
  }

  onMounted(async () => {
    const tabParam = route.query.tab as string;
    if (tabParam && tabs.some((t) => t.key === tabParam)) {
      activeTab.value = tabParam;
    }
    await loadProductDetail();
    await loadUserOptions();
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

  function getNextVersionText() {
    const versions = roadmapRows.value
      .map((row) => String(row.version || '').match(/^v?(\d+)\.(\d+)/i))
      .filter(Boolean)
      .map((matched) => ({ major: Number(matched?.[1] || 0), minor: Number(matched?.[2] || 0) }))
      .sort((a, b) => b.major - a.major || b.minor - a.minor);
    const latest = versions[0];
    if (latest) {
      return `v${latest.major}.${latest.minor + 1}`;
    }
    const current = product.value.nextVersion || product.value.version || 'v1.0';
    const matched = String(current).match(/^v?(\d+)\.(\d+)/i);
    return matched ? `v${matched[1]}.${Number(matched[2]) + 1}` : current;
  }

  function openVersionCreate() {
    const fallbackOwner = userOptions.value[0] || null;
    const productOwnerOption = activeUserOption(product.value.productOwnerId) || fallbackOwner;
    const devOwnerOption = activeUserOption(product.value.devOwnerId) || fallbackOwner;
    versionForm.version = getNextVersionText();
    versionForm.status = '规划中';
    versionForm.releaseDate = '';
    versionForm.description = '<p>[新功能]</p><p><br></p><p>[修复]</p>';
    versionForm.productOwnerId = productOwnerOption?.value || '';
    versionForm.productOwner = productOwnerOption?.label || '';
    versionForm.devOwnerId = devOwnerOption?.value || '';
    versionForm.devOwner = devOwnerOption?.label || '';
    versionForm.attachmentFiles = [];
    versionCreateVisible.value = true;
  }

  function openVersionDetail(row: any) {
    selectedVersion.value = row;
    versionDetailVisible.value = true;
  }

  async function handleVersionSave() {
    if (!product.value.id || !versionForm.version.trim()) {
      message.warning('请填写版本号');
      return;
    }
    syncVersionOwner('productOwner');
    syncVersionOwner('devOwner');
    if (!versionForm.productOwnerId || !versionForm.devOwnerId) {
      message.warning('当前组织暂无可用负责人，请先在组织架构中添加成员');
      return;
    }
    try {
      versionSaving.value = true;
      await addProductVersion({
        productId: product.value.id,
        version: versionForm.version.trim(),
        status: versionForm.status,
        releaseDate: versionForm.releaseDate,
        description: versionForm.description,
        productOwnerId: versionForm.productOwnerId,
        productOwner: versionForm.productOwner,
        devOwnerId: versionForm.devOwnerId,
        devOwner: versionForm.devOwner,
        attachmentIds: versionForm.attachmentFiles.map((file) => file.id),
      });
      versionCreateVisible.value = false;
      activeTab.value = 'roadmap';
      await loadProductDetail();
      message.success('版本已保存');
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('保存版本失败', e);
      message.error('保存版本失败，请检查负责人、版本号和后端服务');
    } finally {
      versionSaving.value = false;
    }
  }

  async function handleVersionStatus(status: string) {
    if (!selectedVersion.value?.id) {
      return;
    }
    try {
      const updated = await updateProductVersionStatus(selectedVersion.value.id, status);
      selectedVersion.value = {
        ...selectedVersion.value,
        ...updated,
        status: updated.status === '已上线' ? '已发布' : updated.status,
        statusType: getRoadmapStatusType(updated.status),
      };
      await loadProductDetail();
      message.success(status === '已发布' ? '版本已发布' : '版本已转入开发');
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('更新版本状态失败', e);
      message.error('更新版本状态失败');
    }
  }

  async function handleVersionDelete() {
    if (!selectedVersion.value?.id) {
      return;
    }
    try {
      await deleteProductVersion(selectedVersion.value.id);
      versionDetailVisible.value = false;
      selectedVersion.value = null;
      await loadProductDetail();
      message.success('版本已删除');
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('删除版本失败', e);
      message.error('删除版本失败，请确认后端服务可用且产品至少保留一个版本');
    }
  }

  async function loadUserOptions() {
    try {
      userOptions.value = await getUserOptions();
    } catch (e) {
      // eslint-disable-next-line no-console
      console.warn('获取用户选项失败', e);
    }
  }

  function activeUserOption(value?: string) {
    if (!value) {
      return null;
    }
    return userOptions.value.find((option) => option.value === value) || null;
  }

  function ownerLabel(name?: string, active = true) {
    if (!name) {
      return '--';
    }
    return active ? name : `${name}（已移除）`;
  }

  function plainText(html?: string) {
    if (!html) {
      return '';
    }
    return html
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  }

  function syncVersionOwner(type: 'productOwner' | 'devOwner') {
    if (type === 'productOwner') {
      versionForm.productOwner =
        userOptions.value.find((option) => option.value === versionForm.productOwnerId)?.label || '';
      return;
    }
    versionForm.devOwner = userOptions.value.find((option) => option.value === versionForm.devOwnerId)?.label || '';
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
    cursor: pointer;
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
    transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
  }
  .sr-roadmap-item:hover .sr-roadmap-item__card {
    border-color: #c7d2fe;
    box-shadow: 0 8px 20px rgb(15 23 42 / 8%);
    transform: translateY(-1px);
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
  .sr-modal-mask {
    position: fixed;
    z-index: 2000;
    display: flex;
    justify-content: center;
    align-items: center;
    inset: 0;
    background: rgb(15 23 42 / 55%);
  }
  .sr-version-modal {
    display: flex;
    overflow: hidden;
    width: min(1180px, calc(100vw - 64px));
    max-height: calc(100vh - 72px);
    border-radius: 10px;
    background: #ffffff;
    box-shadow: 0 24px 80px rgb(15 23 42 / 24%);
    flex-direction: column;
  }
  .sr-version-modal--detail {
    width: min(1320px, calc(100vw - 64px));
  }
  .sr-version-modal__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 22px 24px;
    border-bottom: 1px solid #e2e8f0;
    gap: 24px;
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: #0f172a;
      line-height: 28px;
    }
  }
  .sr-version-modal__close {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    padding: 0;
    width: 28px;
    height: 28px;
    font-size: 28px;
    border: 0;
    color: #64748b;
    background: transparent;
    cursor: pointer;
  }
  .sr-version-modal__body {
    overflow: auto;
    padding: 24px;
    flex: 1 1 auto;
  }
  .sr-version-modal__footer {
    display: flex;
    justify-content: flex-end;
    padding: 16px 24px;
    border-top: 1px solid #e2e8f0;
    background: #f8fafc;
    gap: 10px;
  }
  .sr-version-form {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 18px;
    label {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    span {
      font-size: 13px;
      font-weight: 600;
      color: #334155;
      line-height: 18px;
    }
  }
  .sr-version-form__full {
    grid-column: 1 / -1;
  }
  .sr-input,
  .sr-version-textarea {
    padding: 0 12px;
    min-height: 38px;
    font-size: 14px;
    border: 1px solid #dbe3ef;
    border-radius: 6px;
    color: #0f172a;
    background: #ffffff;
    outline: none;
  }
  .sr-version-textarea {
    padding: 12px;
    min-height: 260px;
    line-height: 22px;
    resize: vertical;
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
  .sr-version-meta {
    display: flex;
    align-items: center;
    margin-top: 20px;
    font-size: 13px;
    color: #334155;
    gap: 28px;
    flex-wrap: wrap;
  }
  .sr-version-modal__actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  .sr-version-action {
    padding: 0;
    font-size: 13px;
    font-weight: 700;
    border: 0;
    background: transparent;
    cursor: pointer;
  }
  .sr-version-action--success {
    color: #16a34a;
  }
  .sr-version-action--danger {
    color: #dc2626;
  }
  .sr-version-detail-block + .sr-version-detail-block {
    margin-top: 24px;
  }
  .sr-version-detail-title {
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 700;
    color: #334155;
    line-height: 20px;
  }
  .sr-version-detail-desc {
    overflow: auto;
    padding: 18px;
    max-height: 330px;
    font-size: 13px;
    border-radius: 8px;
    color: #0f172a;
    background: #f8fafc;
    line-height: 22px;
  }
  .sr-version-detail-desc--empty {
    color: #94a3b8;
  }
  .sr-version-detail-desc :deep(h1),
  .sr-version-detail-desc :deep(h2),
  .sr-version-detail-desc :deep(p) {
    margin: 0 0 8px;
  }
  .sr-version-detail-desc :deep(pre) {
    padding: 8px;
    border-radius: 4px;
    white-space: pre-wrap;
    background: #eef2f7;
  }
  .sr-version-detail-desc :deep(table) {
    width: 100%;
    border-collapse: collapse;
  }
  .sr-version-detail-desc :deep(td) {
    padding: 6px;
    border: 1px solid #dbe3ef;
  }
  .sr-version-attachments {
    display: flex;
    margin-top: 10px;
    flex-wrap: wrap;
    gap: 8px;
  }
  .sr-version-attachments a {
    display: inline-flex;
    align-items: center;
    padding: 0 8px;
    height: 24px;
    font-size: 12px;
    border-radius: 4px;
    text-decoration: none;
    color: #4f46e5;
    background: #eef2ff;
  }
  .sr-version-empty {
    padding: 22px;
    font-size: 13px;
    text-align: center;
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

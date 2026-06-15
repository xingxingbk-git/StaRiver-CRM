<template>
  <StariverModulePage :title="pageTitle">
    <template #subtitle>
      <div class="sr-create-page__subtitle">
        {{ pageDescription }}
      </div>
    </template>

    <template #actions>
      <div class="sr-create-actions">
        <button class="sr-btn sr-btn--ghost" @click="handleCancel">取消</button>
        <button class="sr-btn sr-btn--primary" @click="handleSave">{{ primaryActionText }}</button>
      </div>
    </template>

    <div class="sr-create-page">
      <div class="sr-panel">
        <div class="sr-panel__title">基本信息</div>
        <div class="sr-form-grid">
          <div class="sr-field">
            <label class="sr-field__label">产品代号 <span class="sr-field__req">*</span></label>
            <input v-model="form.code" class="sr-input" placeholder="如 STARIVER / OPTIQA（唯一标识）" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">产品全称 <span class="sr-field__req">*</span></label>
            <input v-model="form.name" class="sr-input" placeholder="如：StaRiver AI 中台" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">版本号</label>
            <input v-model="form.version" class="sr-input" placeholder="v1.0" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">产品状态</label>
            <n-select
              v-model:value="form.status"
              class="sr-native-control"
              placeholder="请选择产品状态"
              :options="statusOptions"
            />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">计划发布日期</label>
            <n-date-picker
              v-model:formatted-value="form.releaseDate"
              class="sr-native-control"
              type="date"
              clearable
              value-format="yyyy-MM-dd"
              placeholder="请选择日期"
            />
          </div>
          <div class="sr-field sr-field--full">
            <label class="sr-field__label">产品简介（Slogan） <span class="sr-field__req">*</span></label>
            <textarea v-model="form.slogan" class="sr-textarea" placeholder="卡片展示简介内容" rows="4"></textarea>
          </div>
        </div>
      </div>

      <div class="sr-panel">
        <div class="sr-panel__title">模块与负责人</div>
        <div class="sr-form-grid">
          <div class="sr-field">
            <label class="sr-field__label">产品负责人 <span class="sr-field__req">*</span></label>
            <n-select
              v-model:value="form.productOwnerId"
              class="sr-native-control"
              filterable
              clearable
              placeholder="请选择产品负责人"
              :options="userOptions"
              @search="handleUserSearch"
              @update:value="(_value, option) => handleOwnerChange('productOwner', option)"
            />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">研发负责人 <span class="sr-field__req">*</span></label>
            <n-select
              v-model:value="form.devOwnerId"
              class="sr-native-control"
              filterable
              clearable
              placeholder="请选择研发负责人"
              :options="userOptions"
              @search="handleUserSearch"
              @update:value="(_value, option) => handleOwnerChange('devOwner', option)"
            />
          </div>
          <div class="sr-field sr-field--full">
            <div class="sr-field__label-row">
              <label class="sr-field__label">模块架构</label>
              <span class="sr-field__hint">支持录入一级模块与二级子模块，保存后会回填到详情页</span>
            </div>
            <div class="sr-module-list">
              <div v-for="(module, moduleIndex) in form.modules" :key="module.id" class="sr-module-card">
                <div class="sr-module-card__header">
                  <span class="sr-module-card__index">{{ moduleIndex + 1 }}</span>
                  <input
                    v-model="module.name"
                    class="sr-input sr-input--module"
                    :placeholder="`请输入一级模块名称，例如：模块 ${moduleIndex + 1}`"
                  />
                  <n-select
                    class="sr-module-owner"
                    placeholder="模块负责人"
                    :options="userOptions"
                    filterable
                    clearable
                  />
                  <button
                    v-if="form.modules.length > 1"
                    class="sr-link-btn sr-link-btn--danger"
                    @click="removeModule(moduleIndex)"
                  >
                    删除
                  </button>
                </div>
                <div class="sr-submodule-list">
                  <div v-for="(child, childIndex) in module.children" :key="child.id" class="sr-submodule-item">
                    <span class="sr-submodule-branch">└</span>
                    <input
                      v-model="child.name"
                      class="sr-input sr-input--module"
                      :placeholder="`请输入二级模块名称 ${childIndex + 1}`"
                    />
                    <n-select
                      class="sr-module-owner"
                      placeholder="负责人"
                      :options="userOptions"
                      filterable
                      clearable
                    />
                    <button
                      v-if="module.children.length > 1"
                      class="sr-link-btn sr-link-btn--danger"
                      @click="removeSubmodule(moduleIndex, childIndex)"
                    >
                      删除
                    </button>
                  </div>
                </div>
                <button class="sr-link-btn" @click="addSubmodule(moduleIndex)">+ 添加二级模块</button>
              </div>
              <button class="sr-add-module" @click="addModule">+ 添加模块</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </StariverModulePage>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';
  import { NDatePicker, NSelect } from 'naive-ui';

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  import { addProduct, getMockUserOptions, getProductDetail, updateProduct } from '@/api/modules/productMock';

  import type { SelectBaseOption } from 'naive-ui/es/select/src/interface';

  interface ProductModuleChild {
    id: string;
    name: string;
  }

  interface ProductModuleGroup {
    id: string;
    name: string;
    children: ProductModuleChild[];
  }

  interface ProductFormState {
    code: string;
    name: string;
    version: string;
    status: string;
    releaseDate: string | null;
    slogan: string;
    productOwner: string;
    productOwnerId: string | null;
    devOwner: string;
    devOwnerId: string | null;
    modules: ProductModuleGroup[];
  }

  const route = useRoute();
  const router = useRouter();

  const statusOptions = [
    { label: '规划中', value: '规划中' },
    { label: '开发中', value: '开发中' },
    { label: '已发布', value: '已发布' },
  ];

  const editProductId = computed(() => route.query.id?.toString() || '');
  const isEditMode = computed(() => route.query.mode === 'edit' && !!editProductId.value);
  const pageTitle = computed(() => (isEditMode.value ? '编辑产品' : '新建产品'));
  const pageDescription = computed(() =>
    isEditMode.value ? '调整产品资料、负责人和模块架构信息' : '创建一个新的产品条目，并维护负责人和模块结构'
  );
  const primaryActionText = computed(() => (isEditMode.value ? '保存修改' : '提交 ->'));
  const userOptions = ref<Array<{ label: string; value: string }>>([]);

  function createModuleChild(name = ''): ProductModuleChild {
    return {
      id: `module-child-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
      name,
    };
  }

  function createModuleGroup(name = '', children: Array<string | ProductModuleChild> = []): ProductModuleGroup {
    const normalizedChildren = children.length
      ? children.map((child) => createModuleChild(typeof child === 'string' ? child : child.name || ''))
      : [createModuleChild()];
    return {
      id: `module-group-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
      name,
      children: normalizedChildren,
    };
  }

  const form = reactive<ProductFormState>({
    code: '',
    name: '',
    version: '',
    status: '',
    releaseDate: null,
    slogan: '',
    productOwner: '',
    productOwnerId: null,
    devOwner: '',
    devOwnerId: null,
    modules: [createModuleGroup()],
  });

  function normalizeModules(modules: unknown): ProductModuleGroup[] {
    if (!Array.isArray(modules) || !modules.length) {
      return [createModuleGroup()];
    }

    const normalized = modules
      .map((module) => {
        if (typeof module === 'string') {
          return createModuleGroup(module);
        }
        if (module && typeof module === 'object') {
          const typedModule = module as { name?: string; children?: Array<string | ProductModuleChild> };
          return createModuleGroup(typedModule.name || '', typedModule.children || []);
        }
        return null;
      })
      .filter(Boolean) as ProductModuleGroup[];

    return normalized.length ? normalized : [createModuleGroup()];
  }

  function fillForm(data: Record<string, any>) {
    form.code = data.code || '';
    form.name = data.name || '';
    form.version = data.version || '';
    form.status = data.status || '';
    form.releaseDate = data.releaseDate || null;
    form.slogan = data.slogan || data.description || '';
    form.productOwner = data.productOwner || '';
    form.productOwnerId = data.productOwnerId || null;
    form.devOwner = data.devOwner || '';
    form.devOwnerId = data.devOwnerId || null;
    form.modules = normalizeModules(data.modules);
  }

  function addModule() {
    form.modules.push(createModuleGroup());
  }

  function removeModule(index: number) {
    form.modules.splice(index, 1);
  }

  function addSubmodule(moduleIndex: number) {
    form.modules[moduleIndex].children.push(createModuleChild());
  }

  function removeSubmodule(moduleIndex: number, childIndex: number) {
    form.modules[moduleIndex].children.splice(childIndex, 1);
  }

  function getOptionLabel(option: SelectBaseOption | null | SelectBaseOption[] | undefined) {
    if (!option || Array.isArray(option)) {
      return '';
    }
    const { label } = option;
    return typeof label === 'string' ? label : String(label || '');
  }

  function handleOwnerChange(type: 'productOwner' | 'devOwner', option: SelectBaseOption | null | SelectBaseOption[]) {
    const label = getOptionLabel(option);
    if (type === 'productOwner') {
      form.productOwner = label;
      return;
    }
    form.devOwner = label;
  }

  async function loadUserOptions(keyword = '') {
    userOptions.value = await getMockUserOptions({ keyword });
  }

  function handleUserSearch(keyword: string) {
    loadUserOptions(keyword);
  }

  function buildPayload() {
    const normalizedModules = form.modules
      .map((module) => ({
        name: module.name.trim(),
        children: module.children.map((child) => child.name.trim()).filter(Boolean),
      }))
      .filter((module) => module.name || module.children.length);

    return {
      code: form.code.trim(),
      name: form.name.trim(),
      version: form.version.trim(),
      status: form.status,
      releaseDate: form.releaseDate || '',
      slogan: form.slogan.trim(),
      productOwner: form.productOwner,
      productOwnerId: form.productOwnerId || '',
      devOwner: form.devOwner,
      devOwnerId: form.devOwnerId || '',
      modules: normalizedModules,
    };
  }

  function handleCancel() {
    router.back();
  }

  async function handleSave() {
    try {
      const payload = buildPayload();
      if (isEditMode.value) {
        await updateProduct({ id: editProductId.value, ...payload });
        router.push({ name: 'productDetail', params: { id: editProductId.value } });
        return;
      }

      const result = await addProduct(payload);
      const newId = result?.id;
      if (newId) {
        router.push({ name: 'productDetail', params: { id: newId } });
      } else {
        router.push({ name: 'productPro' });
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('保存产品失败', e);
    }
  }

  async function loadEditProduct() {
    if (!isEditMode.value || !editProductId.value) {
      return;
    }

    try {
      await loadUserOptions();
      const data = await getProductDetail(editProductId.value);
      if (data) {
        fillForm(data);
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('获取产品详情失败', e);
    }
  }

  watch(
    [isEditMode, editProductId],
    ([editing, id]) => {
      if (editing && id) {
        loadEditProduct();
      }
    },
    {
      immediate: true,
    }
  );

  onActivated(() => {
    loadEditProduct();
  });

  onMounted(() => {
    loadUserOptions();
  });
</script>

<style lang="less" scoped>
  .sr-create-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .sr-create-page__subtitle {
    color: #64748b;
    font-size: 12px;
    line-height: 18px;
  }

  .sr-create-actions {
    display: flex;
    gap: 8px;
  }

  .sr-btn {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 14px;
    background: #ffffff;
    color: #334155;
    font-size: 12px;
    font-weight: 500;
    line-height: 30px;
    cursor: pointer;

    &--primary {
      border-color: #0f172a;
      background: #0f172a;
      color: #ffffff;

      &:hover {
        border-color: #1e293b;
        background: #1e293b;
      }
    }

    &--ghost {
      background: #ffffff;

      &:hover {
        background: #f8fafc;
      }
    }
  }

  .sr-panel {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 16px;
  }

  .sr-panel__title {
    margin-bottom: 16px;
    color: #0f172a;
    font-size: 14px;
    font-weight: 600;
    line-height: 20px;
  }

  .sr-form-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px 16px;
  }

  .sr-field {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .sr-field--full {
    grid-column: 1 / -1;
  }

  .sr-field__label-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .sr-field__label {
    color: #334155;
    font-size: 12px;
    font-weight: 600;
    line-height: 17px;
  }

  .sr-field__hint {
    color: #94a3b8;
    font-size: 12px;
    line-height: 18px;
  }

  .sr-field__req {
    color: #ef4444;
  }

  .sr-input,
  .sr-textarea {
    width: 100%;
    border: 1px solid #dbe2ea;
    border-radius: 6px;
    padding: 0 10px;
    background: #ffffff;
    color: #0f172a;
    font-size: 12px;
    outline: none;
    transition: border-color 0.16s ease, box-shadow 0.16s ease;

    &:focus {
      border-color: #4f46e5;
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.12);
    }
  }

  .sr-input {
    height: 32px;
    line-height: 30px;
  }

  .sr-textarea {
    min-height: 88px;
    resize: vertical;
    padding-top: 10px;
    padding-bottom: 10px;
    line-height: 19px;
  }

  .sr-native-control {
    width: 100%;
  }

  .sr-module-list {
    display: flex;
    flex-direction: column;
    gap: 0;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    overflow: hidden;
    padding: 1px;
  }

  .sr-module-card {
    display: flex;
    flex-direction: column;
    gap: 0;
    overflow: hidden;
    background: #ffffff;

    & + & {
      border-top: 1px solid #eef2f7;
    }
  }

  .sr-module-card__header {
    display: flex;
    align-items: center;
    min-height: 48px;
    gap: 12px;
    background: #f8fafc;
    padding: 8px 12px;
  }

  .sr-module-card__index {
    display: inline-flex;
    width: 22px;
    height: 22px;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: #eef2ff;
    color: #4f46e5;
    font-size: 11px;
    font-weight: 600;
    line-height: 14px;
  }

  .sr-submodule-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px 0 54px;
  }

  .sr-submodule-item {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .sr-submodule-branch {
    width: 12px;
    flex-shrink: 0;
    color: #94a3b8;
    font-size: 10px;
    line-height: 12px;
  }

  .sr-input--module {
    flex: 1;
    min-width: 0;
  }

  .sr-module-owner {
    width: 160px;
    flex-shrink: 0;
  }

  .sr-link-btn,
  .sr-add-module {
    width: fit-content;
    border: 0;
    padding: 0;
    background: transparent;
    color: #4f46e5;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
  }

  .sr-link-btn--danger {
    color: #ef4444;
  }

  .sr-module-card > .sr-link-btn {
    margin: 8px 12px 10px 78px;
  }

  .sr-add-module {
    margin: 8px 12px 12px;
  }

  :deep(.sr-native-control .n-base-selection) {
    min-height: 32px;
    border-radius: 6px;
    box-shadow: none !important;
  }

  :deep(.sr-native-control .n-base-selection-label) {
    min-height: 30px;
  }

  :deep(.sr-module-owner .n-base-selection) {
    min-height: 32px;
    border-radius: 6px;
    background: #ffffff;
    box-shadow: none !important;
  }

  :deep(.sr-module-owner .n-base-selection-label) {
    min-height: 30px;
  }

  :deep(.sr-native-control .n-input__border),
  :deep(.sr-native-control .n-base-selection__border),
  :deep(.sr-module-owner .n-base-selection__border) {
    border-color: #dbe2ea !important;
  }

  :deep(.sr-native-control.n-base-selection--active .n-base-selection__border),
  :deep(.sr-native-control .n-input--focus .n-input__border),
  :deep(.sr-module-owner.n-base-selection--active .n-base-selection__border) {
    border-color: #4f46e5 !important;
  }

  @media (max-width: 1280px) {
    .sr-form-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
</style>

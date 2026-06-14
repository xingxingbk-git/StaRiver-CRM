<template>
  <StariverModulePage :title="pageTitle" eyebrow="产品需求">
    <template #actions>
      <div class="sr-create-actions">
        <button class="sr-btn sr-btn--ghost" @click="handleCancel">取消</button>
        <button class="sr-btn sr-btn--primary" @click="handleSave">{{ primaryActionText }}</button>
      </div>
    </template>

    <div class="sr-create-page">
      <!-- 基本信息 -->
      <div class="sr-panel">
        <div class="sr-panel__title">基本信息</div>
        <div class="sr-form-grid">
          <div class="sr-field">
            <label class="sr-field__label"> 产品代号 <span class="sr-field__req">*</span> </label>
            <input v-model="form.code" class="sr-input" placeholder="如 STARIVER / OPTIQA（唯一标识）" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label"> 产品全称 <span class="sr-field__req">*</span> </label>
            <input v-model="form.name" class="sr-input" placeholder="如：StaRiver AI 中台" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">版本号</label>
            <input v-model="form.version" class="sr-input" placeholder="v1.0" />
          </div>
          <div class="sr-field">
            <label class="sr-field__label">产品状态</label>
            <div class="sr-select" @click="toggleStatusDropdown">
              <span>{{ form.status || '请选择' }}</span>
              <svg class="sr-select__arrow" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M2.5 4L5 6.5L7.5 4"
                  stroke="currentColor"
                  stroke-width="1.2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <div v-if="statusDropdownVisible" class="sr-select__dropdown">
                <div v-for="opt in statusOptions" :key="opt" class="sr-select__option" @click.stop="selectStatus(opt)">
                  {{ opt }}
                </div>
              </div>
            </div>
          </div>
          <div class="sr-field">
            <label class="sr-field__label">计划发布日期</label>
            <div class="sr-select">
              <span :class="{ 'sr-select__placeholder': !form.releaseDate }">{{
                form.releaseDate || '请选择日期'
              }}</span>
              <svg class="sr-select__icon" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="1" y="2" width="12" height="11" rx="2" stroke="currentColor" stroke-width="1.2" />
                <path d="M1 5.5H13" stroke="currentColor" stroke-width="1.2" />
                <path d="M4.5 1V3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
                <path d="M9.5 1V3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
              </svg>
            </div>
          </div>
          <div class="sr-field sr-field--full">
            <label class="sr-field__label"> 产品简介（Slogan） <span class="sr-field__req">*</span> </label>
            <textarea v-model="form.slogan" class="sr-textarea" placeholder="卡片展示简介内容" rows="4"></textarea>
          </div>
        </div>
      </div>

      <!-- 模块与负责人 -->
      <div class="sr-panel">
        <div class="sr-panel__title">模块与负责人</div>
        <div class="sr-form-grid">
          <div class="sr-field">
            <label class="sr-field__label"> 产品负责人 <span class="sr-field__req">*</span> </label>
            <div class="sr-select">
              <span :class="{ 'sr-select__placeholder': !form.productOwner }">{{ form.productOwner || '请选择' }}</span>
              <svg class="sr-select__arrow" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M2.5 4L5 6.5L7.5 4"
                  stroke="currentColor"
                  stroke-width="1.2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
          </div>
          <div class="sr-field">
            <label class="sr-field__label"> 研发负责人 <span class="sr-field__req">*</span> </label>
            <div class="sr-select">
              <span :class="{ 'sr-select__placeholder': !form.devOwner }">{{ form.devOwner || '请选择' }}</span>
              <svg class="sr-select__arrow" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M2.5 4L5 6.5L7.5 4"
                  stroke="currentColor"
                  stroke-width="1.2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
          </div>
          <div class="sr-field sr-field--full">
            <label class="sr-field__label">模块架构</label>
            <div class="sr-module-list">
              <div v-for="(mod, index) in form.modules" :key="index" class="sr-module-item">
                <input
                  v-model="form.modules[index]"
                  class="sr-input sr-input--module"
                  :placeholder="`模块 ${index + 1}`"
                />
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

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  import { addProduct, getProductDetail, updateProduct } from '@/api/modules/productMock';

  const route = useRoute();
  const router = useRouter();

  const statusOptions = ['规划中', '开发中', '已发布'];
  const statusDropdownVisible = ref(false);
  const editProductId = computed(() => route.query.id?.toString() || '');
  const isEditMode = computed(() => route.query.mode === 'edit' && !!editProductId.value);
  const pageTitle = computed(() => (isEditMode.value ? '编辑产品' : '新建产品'));
  const primaryActionText = computed(() => (isEditMode.value ? '保存修改' : '保存'));

  const form = reactive({
    code: '',
    name: '',
    version: '',
    status: '',
    releaseDate: '',
    slogan: '',
    productOwner: '',
    devOwner: '',
    modules: [''],
  });

  function fillForm(data: Record<string, any>) {
    form.code = data.code || '';
    form.name = data.name || '';
    form.version = data.version || '';
    form.status = data.status || '';
    form.releaseDate = data.releaseDate || '';
    form.slogan = data.slogan || data.description || '';
    form.productOwner = data.productOwner || '';
    form.devOwner = data.devOwner || '';
    form.modules = Array.isArray(data.modules) && data.modules.length ? [...data.modules] : [''];
  }

  function toggleStatusDropdown() {
    statusDropdownVisible.value = !statusDropdownVisible.value;
  }

  function selectStatus(opt: string) {
    form.status = opt;
    statusDropdownVisible.value = false;
  }

  function addModule() {
    form.modules.push('');
  }

  function handleCancel() {
    router.back();
  }

  async function handleSave() {
    try {
      if (isEditMode.value) {
        await updateProduct({ id: editProductId.value, ...form });
        router.push({ name: 'productDetail', params: { id: editProductId.value } });
        return;
      }
      const result = await addProduct({ ...form });
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

  // 点击外部关闭下拉
  function closeDropdown() {
    statusDropdownVisible.value = false;
  }

  onMounted(() => {
    if (isEditMode.value) {
      getProductDetail(editProductId.value)
        .then((data) => {
          if (data) fillForm(data);
        })
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('获取产品详情失败', e);
        });
    }
    document.addEventListener('click', closeDropdown);
  });

  onUnmounted(() => {
    document.removeEventListener('click', closeDropdown);
  });
</script>

<style lang="less" scoped>
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

  .sr-create-page {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .sr-panel {
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #ffffff;
    padding: 16px;
  }

  .sr-panel__title {
    margin-bottom: 14px;
    color: #0f172a;
    font-size: 13px;
    font-weight: 500;
    line-height: 18px;
  }

  .sr-form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 22px 16px;
  }

  .sr-field {
    display: flex;
    flex-direction: column;
    gap: 5px;

    &--full {
      grid-column: 1 / -1;
    }
  }

  .sr-field__label {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #334155;
    font-size: 11px;
    font-weight: 500;
    line-height: 16px;
  }

  .sr-field__req {
    color: #dc2626;
    font-size: 11px;
  }

  .sr-input {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 10px;
    background: #ffffff;
    color: #0f172a;
    font-size: 12px;
    line-height: 17px;
    outline: none;
    transition: border-color 0.2s;

    &::placeholder {
      color: #94a3b8;
    }

    &:focus {
      border-color: #4f46e5;
    }

    &--module {
      width: 100%;
    }
  }

  .sr-textarea {
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 10px;
    background: #ffffff;
    color: #0f172a;
    font-size: 12px;
    line-height: 19px;
    outline: none;
    resize: vertical;
    transition: border-color 0.2s;

    &::placeholder {
      color: #94a3b8;
    }

    &:focus {
      border-color: #4f46e5;
    }
  }

  .sr-select {
    position: relative;
    display: flex;
    height: 32px;
    align-items: center;
    justify-content: space-between;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 10px;
    background: #ffffff;
    color: #0f172a;
    font-size: 12px;
    cursor: pointer;

    &__placeholder {
      color: #94a3b8;
    }

    &__arrow {
      width: 10px;
      height: 10px;
      color: #94a3b8;
    }

    &__icon {
      width: 14px;
      height: 14px;
      color: #94a3b8;
    }

    &__dropdown {
      position: absolute;
      top: 100%;
      left: 0;
      z-index: 10;
      width: 100%;
      margin-top: 4px;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      background: #ffffff;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
    }

    &__option {
      padding: 8px 10px;
      color: #334155;
      font-size: 12px;

      &:hover {
        background: #f1f5f9;
      }
    }
  }

  .sr-module-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .sr-module-item {
    display: flex;
    align-items: center;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 1px;
  }

  .sr-add-module {
    display: flex;
    height: 36px;
    align-items: center;
    border: none;
    border-radius: 0;
    padding: 0 12px;
    background: transparent;
    color: #4f46e5;
    font-size: 12px;
    font-weight: 500;
    line-height: 15px;
    cursor: pointer;

    &:hover {
      background: #f8fafc;
    }
  }
</style>

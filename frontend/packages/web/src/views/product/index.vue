<template>
  <StariverModulePage title="产品集" count-label="产品路线图" eyebrow="产品需求">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">全部产品</button>
        <button class="stariver-tab">规划中</button>
        <button class="stariver-tab">开发中</button>
        <button class="stariver-tab">已发布</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">负责人：全部</button>
        <button class="stariver-filter">版本：全部</button>
      </div>
    </template>

    <div class="stariver-product-stack">
      <StariverInsightStrip
        :metrics="productMetrics"
        :process="productProcess"
        :fields="productFields"
        :rules="productRules"
      >
      </StariverInsightStrip>
      <div :key="tableRefreshIdKey" class="stariver-product-table-panel">
        <CrmTable
          ref="crmTableRef"
          v-model:checked-row-keys="checkedRowKeys"
          v-bind="propsRes"
          class="crm-product-table"
          :action-config="actionConfig"
          :draggable="hasAnyPermission(['PRODUCT_MANAGEMENT:UPDATE'])"
          @page-change="propsEvent.pageChange"
          @page-size-change="propsEvent.pageSizeChange"
          @sorter-change="propsEvent.sorterChange"
          @filter-change="propsEvent.filterChange"
          @batch-action="handleBatchAction"
          @drag="dragHandler"
          @refresh="searchData"
        >
          <template #actionLeft>
            <div class="flex items-center gap-[12px]">
              <n-button
                v-permission="['PRODUCT_MANAGEMENT:ADD']"
                type="primary"
                @click="
                  {
                    activeProductId = '';
                    formCreateDrawerVisible = true;
                  }
                "
              >
                {{ t('product.createProduct') }}
              </n-button>
              <CrmImportButton
                v-if="hasAnyPermission(['PRODUCT_MANAGEMENT:IMPORT'])"
                :api-type="FormDesignKeyEnum.PRODUCT"
                :title="t('module.productManagement')"
                @import-success="() => searchData()"
              />
            </div>
          </template>
          <template #actionRight>
            <CrmSearchInput v-model:value="keyword" class="!w-[240px]" @search="searchData" />
          </template>
        </CrmTable>
      </div>
    </div>
  </StariverModulePage>
  <CrmFormCreateDrawer
    v-model:visible="formCreateDrawerVisible"
    :form-key="FormDesignKeyEnum.PRODUCT"
    :source-id="activeProductId"
    :need-init-detail="!!activeProductId"
    @saved="handleRefresh"
  />
  <CrmBatchEditModal
    v-model:visible="showEditModal"
    v-model:field-list="editFieldList"
    :ids="checkedRowKeys"
    :form-key="FormDesignKeyEnum.PRODUCT"
    @refresh="handleRefresh"
  />
  <detailDrawer
    v-model:visible="detailDrawerVisible"
    :source-id="activeProductId"
    :refresh-id="tableRefreshId"
    @edit="handleEdit"
  />
</template>

<script lang="ts" setup>
  import { DataTableRowKey, NButton, useMessage } from 'naive-ui';

  import { FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';
  import { useI18n } from '@lib/shared/hooks/useI18n';
  import { characterLimit } from '@lib/shared/method';
  import type { TableDraggedParams } from '@lib/shared/models/common';
  import type { ProductListItem } from '@lib/shared/models/product';

  import type { ActionsItem } from '@/components/pure/crm-more-action/type';
  import CrmSearchInput from '@/components/pure/crm-search-input/index.vue';
  import CrmTable from '@/components/pure/crm-table/index.vue';
  import { BatchActionConfig } from '@/components/pure/crm-table/type';
  import CrmTableButton from '@/components/pure/crm-table-button/index.vue';
  import CrmBatchEditModal from '@/components/business/crm-batch-edit-modal/index.vue';
  import CrmFormCreateDrawer from '@/components/business/crm-form-create-drawer/index.vue';
  import CrmImportButton from '@/components/business/crm-import-button/index.vue';
  import CrmOperationButton from '@/components/business/crm-operation-button/index.vue';
  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import detailDrawer from './components/detail.vue';

  import { batchDeleteProduct, deleteProduct, dragSortProduct } from '@/api/modules';
  import useFormCreateApi from '@/hooks/useFormCreateApi';
  import useFormCreateTable from '@/hooks/useFormCreateTable';
  import useModal from '@/hooks/useModal';
  import { hasAnyPermission } from '@/utils/permission';

  const { openModal } = useModal();

  const { t } = useI18n();

  const Message = useMessage();

  const productMetrics = [
    { label: '规划中产品', value: '4', hint: '等待需求拆解和版本排期', tone: 'amber' },
    { label: '开发中', value: '6', hint: '已绑定研发负责人', tone: 'blue' },
    { label: '已发布', value: '12', hint: '可被报价与合同引用', tone: 'emerald' },
    { label: '需求池', value: '28', hint: '来自销售反馈与客户场景', tone: 'indigo' },
  ];

  const productProcess = ['需求收集', '产品归类', '版本规划', '研发交付', '发布启用'];
  const productFields = ['产品编码', '产品全称', '版本号', '产品状态', '计划发布日期', '产品负责人'];
  const productRules = [
    '产品状态覆盖规划中、开发中、已发布',
    '产品信息需支持报价产品行引用',
    '需求管理用于沉淀销售场景和客户反馈',
  ];

  const checkedRowKeys = ref<DataTableRowKey[]>([]);
  const keyword = ref('');
  const formCreateDrawerVisible = ref(false);
  const activeProductId = ref('');
  const tableRefreshId = ref(0);
  const tableRefreshIdKey = ref(0);

  const actionConfig: BatchActionConfig = {
    baseAction: [
      {
        label: t('common.batchEdit'),
        key: 'batchEdit',
        permission: ['PRODUCT_MANAGEMENT:UPDATE'],
      },
      {
        label: t('common.batchDelete'),
        key: 'batchDelete',
        permission: ['PRODUCT_MANAGEMENT:DELETE'],
      },
    ],
  };

  // 批量删除
  function handleBatchDelete() {
    openModal({
      type: 'error',
      title: t('product.batchDeleteTitleTip', { number: checkedRowKeys.value.length }),
      content: t('product.batchDeleteContentTip'),
      positiveText: t('common.confirmDelete'),
      negativeText: t('common.cancel'),
      onPositiveClick: async () => {
        try {
          await batchDeleteProduct(checkedRowKeys.value);
          checkedRowKeys.value = [];
          tableRefreshId.value += 1;
          Message.success(t('common.deleteSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
    });
  }

  const showEditModal = ref(false);
  const { initFormConfig: initEditFormConfig, fieldList: editFieldList } = useFormCreateApi({
    formKey: ref(FormDesignKeyEnum.PRODUCT),
  });
  function handleBatchEdit() {
    initEditFormConfig();
    showEditModal.value = true;
  }

  function handleBatchAction(item: ActionsItem) {
    switch (item.key) {
      case 'batchEdit':
        handleBatchEdit();
        break;
      case 'batchDelete':
        handleBatchDelete();
        break;
      default:
        break;
    }
  }

  // 删除
  function handleDelete(row: ProductListItem) {
    openModal({
      type: 'error',
      title: t('common.deleteConfirmTitle', { name: characterLimit(row.name) }),
      content: t('product.batchDeleteContentTip'),
      positiveText: t('common.confirmDelete'),
      negativeText: t('common.cancel'),
      onPositiveClick: async () => {
        try {
          await deleteProduct(row.id);
          Message.success(t('common.deleteSuccess'));
          tableRefreshId.value += 1;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
    });
  }

  // 编辑
  function handleEdit(productId: string) {
    activeProductId.value = productId;
    formCreateDrawerVisible.value = true;
  }

  // 拖拽
  async function dragHandler(params: TableDraggedParams) {
    try {
      await dragSortProduct(params);
      Message.success(t('common.operationSuccess'));
      tableRefreshIdKey.value += 1;
      tableRefreshId.value += 1;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  function handleActionSelect(row: ProductListItem, actionKey: string) {
    switch (actionKey) {
      case 'edit':
        handleEdit(row.id);
        break;
      case 'delete':
        handleDelete(row);
        break;
      default:
        break;
    }
  }

  const operationGroupList: ActionsItem[] = [
    {
      label: t('common.edit'),
      key: 'edit',
      permission: ['PRODUCT_MANAGEMENT:UPDATE'],
    },
    {
      label: t('common.delete'),
      key: 'delete',
      permission: ['PRODUCT_MANAGEMENT:DELETE'],
    },
  ];
  const detailDrawerVisible = ref(false);

  const { useTableRes, fieldList } = await useFormCreateTable({
    formKey: FormDesignKeyEnum.PRODUCT,
    containerClass: '.crm-product-table',
    operationColumn: {
      key: 'operation',
      width: 100,
      fixed: 'right',
      render: (row: ProductListItem) =>
        h(CrmOperationButton, {
          groupList: operationGroupList,
          onSelect: (key: string) => handleActionSelect(row, key),
        }),
    },
    specialRender: {
      name: (row: ProductListItem) => {
        return h(
          CrmTableButton,
          {
            onClick: () => {
              activeProductId.value = row.id;
              detailDrawerVisible.value = true;
            },
          },
          { default: () => row.name, trigger: () => row.name }
        );
      },
    },
    permission: ['PRODUCT_MANAGEMENT:UPDATE', 'PRODUCT_MANAGEMENT:DELETE'],
  });
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTableRes;

  const crmTableRef = ref<InstanceType<typeof CrmTable>>();
  function searchData(val?: string) {
    setLoadListParams({ keyword: val ?? keyword.value });
    loadList();
    crmTableRef.value?.scrollTo({ top: 0 });
  }

  function handleRefresh() {
    checkedRowKeys.value = [];
    tableRefreshId.value += 1;
  }

  watch(
    () => tableRefreshId.value,
    () => {
      crmTableRef.value?.clearCheckedRowKeys();
      searchData();
    }
  );

  onMounted(() => {
    searchData();
  });
</script>

<style lang="less" scoped>
  .stariver-product-stack {
    display: flex;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    gap: 12px;
  }

  .stariver-product-table-panel {
    min-height: 320px;
    flex: 1;
    min-height: 0;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    padding: 16px;
  }

  .stariver-tabs,
  .stariver-filters {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stariver-tab,
  .stariver-filter {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 10px;
    background: #ffffff;
    color: #64748b;
    font-size: 13px;
    line-height: 30px;
    cursor: pointer;
  }

  .stariver-tab {
    border-color: transparent;
    background: transparent;
  }

  .stariver-tab--active {
    background: #eef2ff;
    color: #4f46e5;
    font-weight: 700;
  }

  .stariver-filter {
    background: #f8fafc;
    color: #475569;
  }
</style>

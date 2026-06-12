<template>
  <div class="stariver-customer-page">
    <div class="stariver-customer-page__header">
      <div class="stariver-customer-page__title-row">
        <h1>客户管理</h1>
        <span class="stariver-customer-page__count">{{ total }} 家</span>
      </div>
      <div class="stariver-customer-page__actions">
        <CrmImportButton
          :api-type="FormDesignKeyEnum.CUSTOMER"
          title="客户"
          button-text="批量导入"
          button-class="stariver-customer-page__btn-ghost"
          @import-success="() => loadCustomers(true)"
        />
        <n-button class="stariver-customer-page__btn-primary" @click="openCreate"> + 新建客户 </n-button>
      </div>
    </div>

    <StariverInsightStrip
      class="stariver-customer-page__insight"
      :metrics="customerMetrics"
      :process="customerProcess"
      :fields="customerFields"
      :rules="customerRules"
    />

    <div class="stariver-customer-page__table-shell">
      <div class="stariver-customer-page__toolbar">
        <div class="stariver-customer-page__tabs">
          <button
            v-for="tab in scopeTabs"
            :key="tab.value"
            :class="{ 'stariver-customer-page__tab--active': activeScope === tab.value }"
            class="stariver-customer-page__tab"
            @click="changeScope(tab.value)"
          >
            {{ tab.label }}
            <span v-if="activeScope === tab.value">{{ total }}</span>
          </button>
        </div>
        <div class="stariver-customer-page__filters">
          <button class="stariver-customer-page__filter-button">等级：全部</button>
          <n-input
            v-model:value="keyword"
            clearable
            size="small"
            class="stariver-customer-page__search"
            placeholder="搜索客户名称"
          >
            <template #prefix>
              <CrmIcon type="iconicon_search-outline_outlined" :size="14" />
            </template>
          </n-input>
        </div>
      </div>

      <n-spin :show="loading" class="stariver-customer-page__spin">
        <div v-if="displayRows.length" class="stariver-customer-table-wrap">
          <table class="stariver-customer-table">
            <thead>
              <tr>
                <th class="stariver-customer-table__cell-name">客户</th>
                <th class="stariver-customer-table__cell-industry">行业</th>
                <th class="stariver-customer-table__cell-level">等级</th>
                <th class="stariver-customer-table__cell-scale">规模</th>
                <th class="stariver-customer-table__cell-area">地区</th>
                <th class="stariver-customer-table__cell-owner">负责人</th>
                <th class="stariver-customer-table__cell-num">商机</th>
                <th class="stariver-customer-table__cell-num">合同</th>
                <th class="stariver-customer-table__cell-date">上次跟进</th>
                <th class="stariver-customer-table__cell-action"></th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="customer in displayRows"
                :key="customer.id"
                class="stariver-customer-table__row"
                @click="openDetail(customer)"
              >
                <td>
                  <div class="stariver-customer-table__customer">
                    <div class="stariver-customer-table__avatar">{{ getCustomerInitials(customer.name) }}</div>
                    <div class="min-w-0">
                      <div class="stariver-customer-table__name">{{ customer.name }}</div>
                      <div class="stariver-customer-table__meta">{{ getCustomerMeta(customer) }}</div>
                    </div>
                  </div>
                </td>
                <td>{{ getCustomerIndustryLabel(customer) }}</td>
                <td>
                  <span
                    class="stariver-customer-table__level-chip"
                    :class="getLevelClass(getCustomerLevelLabel(customer))"
                  >
                    {{ getCustomerLevelLabel(customer) }}
                  </span>
                </td>
                <td>{{ getCustomerScaleLabel(customer) }}</td>
                <td>{{ getFieldDisplay(customer, 'customerArea') }}</td>
                <td>{{ customer.ownerName || '-' }}</td>
                <td class="stariver-customer-table__cell-num">{{ customer.opportunityCount ?? 0 }}</td>
                <td class="stariver-customer-table__cell-num">{{ customer.contractCount ?? 0 }}</td>
                <td class="stariver-customer-table__cell-date">{{
                  formatDate(customer.followTime || customer.latestFollowUpTime)
                }}</td>
                <td class="stariver-customer-table__cell-action" @click.stop>
                  <button class="stariver-customer-table__arrow" title="查看详情">›</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <n-empty v-else class="stariver-customer-page__empty" description="暂无客户" />
      </n-spin>

      <div class="stariver-customer-page__pagination">
        <n-pagination
          v-model:page="currentPage"
          v-model:page-size="pageSize"
          :item-count="total"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>

    <CrmFormCreateDrawer
      v-model:visible="formDrawerVisible"
      :source-id="editingCustomerId"
      :form-key="FormDesignKeyEnum.CUSTOMER"
      :need-init-detail="!!editingCustomerId"
      @saved="handleFormSaved"
    />
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { NButton, NEmpty, NInput, NPagination, NSpin } from 'naive-ui';
  import dayjs from 'dayjs';

  import { CustomerSearchTypeEnum } from '@lib/shared/enums/customerEnum';
  import { FieldTypeEnum, FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';
  import { getCityPath } from '@lib/shared/method';
  import type { ModuleField } from '@lib/shared/models/common';
  import type { CustomerListItem } from '@lib/shared/models/customer';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import type { FormCreateField } from '@/components/business/crm-form-create/types';
  import CrmFormCreateDrawer from '@/components/business/crm-form-create-drawer/index.vue';
  import CrmImportButton from '@/components/business/crm-import-button/index.vue';
  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';

  import { getCustomerFormConfig, getCustomerList } from '@/api/modules';

  import { CustomerRouteEnum } from '@/enums/routeEnum';

  const router = useRouter();

  const loading = ref(false);
  const keyword = ref('');
  const activeScope = ref<CustomerSearchTypeEnum>(CustomerSearchTypeEnum.ALL);
  const currentPage = ref(1);
  const pageSize = ref(20);
  const total = ref(0);
  const customers = ref<CustomerListItem[]>([]);
  const optionMap = ref<Record<string, any[]>>({});
  const formFields = ref<FormCreateField[]>([]);
  const formDrawerVisible = ref(false);
  const editingCustomerId = ref<string | undefined>();

  const scopeTabs = [
    { label: '全部', value: CustomerSearchTypeEnum.ALL },
    { label: '我负责的', value: CustomerSearchTypeEnum.SELF },
  ];

  const customerMetrics = computed(() => [
    { label: '客户总量', value: `${total.value}`, hint: '按数据权限过滤', tone: 'blue' },
    { label: 'KA 客户', value: '8', hint: '战略客户优先维护', tone: 'indigo' },
    { label: '待跟进客户', value: '12', hint: '超过 7 天无跟进', tone: 'amber' },
    { label: '本月新增', value: '6', hint: '来自线索转化与手工录入', tone: 'emerald' },
  ]);

  const customerProcess = ['客户录入', '客户画像', '分级归属', '跟进计划', '商机转化'];
  const customerFields = ['客户画像', '客户等级', '行业', '客户规模', '地区', '负责人'];
  const customerRules = [
    '客户等级影响商机优先级和跟进频次',
    '客户画像由基本信息、联系人、商机和合同沉淀',
    '回收客户需填写原因并进入公海池',
  ];

  const fieldByInternalKey = computed(() => {
    return formFields.value.reduce<Record<string, FormCreateField>>((map, field) => {
      if (field.internalKey) {
        map[field.internalKey] = field;
      }
      return map;
    }, {});
  });

  function getField(internalKey: string) {
    return fieldByInternalKey.value[internalKey];
  }

  function getOptionLabel(options: any[], value: unknown) {
    const option = options.find((item) => String(item.id ?? item.value) === String(value));
    return option?.name ?? option?.label ?? '';
  }

  function getLocationLabel(value: string, scope?: string) {
    const addressArr = value.split('-').filter(Boolean);
    if (!addressArr.length) return '-';
    const city = getCityPath(addressArr[0], scope);
    const rest = addressArr.filter((_, index) => index > 0).join('-');
    return rest ? `${city}-${rest}` : city || '-';
  }

  function getRawFieldValue(customer: CustomerListItem, internalKey: string) {
    const field = getField(internalKey);
    if (!field) return '';
    const moduleField = customer.moduleFields?.find((item: ModuleField) => item.fieldId === field.id);
    return moduleField?.fieldValue ?? '';
  }

  function getFieldDisplay(customer: CustomerListItem, internalKey: string) {
    const rawValue = getRawFieldValue(customer, internalKey);
    const field = getField(internalKey);
    if (!field || rawValue === '' || rawValue === null || rawValue === undefined) return '-';
    if (field.type === FieldTypeEnum.LOCATION) {
      return getLocationLabel(String(rawValue), field.scope);
    }
    const options = optionMap.value[field.id] || field.options || [];
    if (Array.isArray(rawValue)) {
      const labels = rawValue.map((value) => getOptionLabel(options, value)).filter(Boolean);
      return labels.length ? labels.join('、') : '-';
    }
    return getOptionLabel(options, rawValue) || String(rawValue);
  }

  function getFieldText(customer: CustomerListItem, internalKey: string) {
    const value = getFieldDisplay(customer, internalKey);
    return value === '-' ? '' : value;
  }

  function getCustomerIndustryLabel(customer: CustomerListItem) {
    return getFieldText(customer, 'customerIndustry') || '-';
  }

  function getCustomerLevelLabel(customer: CustomerListItem) {
    return getFieldText(customer, 'customerLevel') || '-';
  }

  function getCustomerScaleLabel(customer: CustomerListItem) {
    return getFieldText(customer, 'customerScale') || '-';
  }

  function getCustomerMeta(customer: CustomerListItem) {
    const shortName = getFieldText(customer, 'customerShortName');
    const scale = getCustomerScaleLabel(customer);
    return [shortName, scale].filter((item) => item && item !== '-').join(' · ') || 'StaRiver 客户';
  }

  function formatDate(time?: number) {
    return time ? dayjs(time).format('YYYY-MM-DD') : '-';
  }

  const displayRows = computed(() => customers.value);

  let keywordTimer: ReturnType<typeof setTimeout> | null = null;

  async function initFormConfig() {
    const res = await getCustomerFormConfig();
    formFields.value = res.fields || [];
  }

  async function loadCustomers(resetPage = false) {
    if (resetPage) currentPage.value = 1;
    loading.value = true;
    try {
      const res = await getCustomerList({
        current: currentPage.value,
        pageSize: pageSize.value,
        keyword: keyword.value.trim(),
        viewId: activeScope.value,
      });
      customers.value = res.list || [];
      optionMap.value = res.optionMap || {};
      total.value = res.total || 0;
    } finally {
      loading.value = false;
    }
  }

  function changeScope(scope: CustomerSearchTypeEnum) {
    if (activeScope.value === scope) return;
    activeScope.value = scope;
    loadCustomers(true);
  }

  function handlePageChange() {
    loadCustomers();
  }
  function handlePageSizeChange() {
    loadCustomers(true);
  }

  function openCreate() {
    editingCustomerId.value = undefined;
    formDrawerVisible.value = true;
  }

  function openDetail(customer: CustomerListItem) {
    router.push({
      name: CustomerRouteEnum.CUSTOMER_DETAIL,
      params: { id: customer.id },
    });
  }

  function handleFormSaved() {
    loadCustomers(!editingCustomerId.value);
  }

  function getCustomerInitials(name: string) {
    return (name || 'SR').slice(0, 2).toUpperCase();
  }

  function getLevelClass(level: string) {
    if (level.includes('KA')) return 'stariver-level-tag--ka';
    if (level.startsWith('A')) return 'stariver-level-tag--a';
    if (level.startsWith('B')) return 'stariver-level-tag--b';
    return '';
  }

  watch(keyword, () => {
    if (keywordTimer) clearTimeout(keywordTimer);
    keywordTimer = setTimeout(() => loadCustomers(true), 300);
  });

  onMounted(async () => {
    await Promise.all([initFormConfig(), loadCustomers(true)]);
  });
</script>

<style lang="less" scoped>
  .stariver-customer-page {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    background: #f3f4f6;
    color: #0f172a;
  }

  .stariver-customer-page__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 64px;
    padding: 14px 24px;
    flex-shrink: 0;
  }

  .stariver-customer-page__title-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .stariver-customer-page__title-row h1 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
  }

  .stariver-customer-page__count {
    border-radius: 999px;
    padding: 2px 8px;
    background: rgba(100, 116, 139, 0.08);
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
    line-height: 18px;
  }

  .stariver-customer-page__actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .stariver-customer-page__insight {
    margin: 0 24px 12px;
  }

  .stariver-customer-page__btn-ghost {
    height: 34px;
    border-radius: 6px;
    font-size: 13px;
    background: #fff;
    border: 1px solid #e2e8f0;
    color: #334155;
    padding: 0 14px;
    font-weight: 500;
    cursor: pointer;
  }

  .stariver-customer-page__btn-primary {
    height: 32px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    background: #4f46e5;
    border: 1px solid #4f46e5;
    color: #fff;
    padding: 0 12px;
    cursor: pointer;
    --n-color: #4f46e5 !important;
    --n-color-hover: #4338ca !important;
    --n-color-pressed: #3730a3 !important;
    --n-border: 1px solid #4f46e5 !important;
    --n-border-hover: 1px solid #4338ca !important;
    --n-border-pressed: 1px solid #3730a3 !important;
  }

  .stariver-customer-page__table-shell {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    margin: 0 24px 24px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
    overflow: hidden;
  }

  .stariver-customer-page__toolbar {
    display: flex;
    min-height: 58px;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #e2e8f0;
    padding: 0 16px;
  }

  .stariver-customer-page__tabs {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .stariver-customer-page__tab {
    display: flex;
    height: 32px;
    align-items: center;
    gap: 6px;
    border: 0;
    border-radius: 6px;
    padding: 0 10px;
    background: transparent;
    color: #64748b;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
  }

  .stariver-customer-page__tab span {
    color: inherit;
    font-weight: 600;
  }

  .stariver-customer-page__tab--active {
    background: #eef2ff;
    color: #4338ca;
  }

  .stariver-customer-page__filters {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stariver-customer-page__search {
    width: 220px;
  }

  .stariver-customer-page__filter-button {
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 0 10px;
    background: #f8fafc;
    color: #475569;
    font-size: 13px;
    line-height: 30px;
    cursor: pointer;
  }

  .stariver-customer-page__spin {
    flex: 1;
    min-height: 0;
  }

  .stariver-customer-table-wrap {
    height: 100%;
    min-height: 0;
    overflow: auto;
  }

  .stariver-customer-table {
    width: 100%;
    min-width: 1060px;
    border-collapse: separate;
    border-spacing: 0;
  }

  .stariver-customer-table th {
    height: 42px;
    border-bottom: 1px solid #e2e8f0;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
    text-align: left;
    white-space: nowrap;
  }

  .stariver-customer-table th,
  .stariver-customer-table td {
    padding: 0 14px;
  }

  .stariver-customer-table td {
    height: 64px;
    border-bottom: 1px solid #edf2f7;
    color: #334155;
    font-size: 13px;
    vertical-align: middle;
  }

  .stariver-customer-table__row {
    cursor: pointer;
  }

  .stariver-customer-table__row:hover {
    background: #f8fafc;
  }

  .stariver-customer-table__cell-name {
    min-width: 240px;
  }

  .stariver-customer-table__cell-industry {
    width: 100px;
  }

  .stariver-customer-table__cell-level {
    width: 90px;
  }

  .stariver-customer-table__cell-scale {
    width: 110px;
  }

  .stariver-customer-table__cell-area {
    width: 120px;
  }

  .stariver-customer-table__cell-owner {
    width: 100px;
  }

  .stariver-customer-table__cell-num {
    width: 60px;
    text-align: center;
  }

  .stariver-customer-table__cell-date {
    width: 100px;
  }

  .stariver-customer-table__cell-action {
    width: 40px;
  }

  .stariver-customer-table__customer {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 10px;
  }

  .stariver-customer-table__avatar {
    display: flex;
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: #eef2ff;
    color: #4338ca;
    font-size: 12px;
    font-weight: 700;
  }

  .stariver-customer-table__name {
    max-width: 220px;
    overflow: hidden;
    color: #0f172a;
    font-size: 14px;
    font-weight: 600;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .stariver-customer-table__meta {
    max-width: 220px;
    overflow: hidden;
    color: #94a3b8;
    font-size: 12px;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .stariver-customer-table__level-chip {
    display: inline-flex;
    height: 22px;
    align-items: center;
    padding: 0 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    background: #f1f5f9;
    color: #475569;
  }

  .stariver-level-tag--ka {
    background: #ecfeff !important;
    color: #0e7490 !important;
  }

  .stariver-level-tag--a {
    background: #eef2ff !important;
    color: #4338ca !important;
  }

  .stariver-level-tag--b {
    background: #fef3c7 !important;
    color: #92400e !important;
  }

  .stariver-customer-table__arrow {
    display: flex;
    width: 28px;
    height: 28px;
    align-items: center;
    justify-content: center;
    border: 0;
    border-radius: 6px;
    background: transparent;
    color: #94a3b8;
    font-size: 18px;
    cursor: pointer;
  }

  .stariver-customer-table__arrow:hover {
    background: #eef2ff;
    color: #4338ca;
  }

  .stariver-customer-page__empty {
    padding-top: 96px;
  }

  .stariver-customer-page__pagination {
    display: flex;
    height: 52px;
    flex-shrink: 0;
    align-items: center;
    justify-content: flex-end;
    border-top: 1px solid #e2e8f0;
    padding: 0 16px;
  }

  @media (max-width: 960px) {
    .stariver-customer-page__header,
    .stariver-customer-page__toolbar {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      height: auto;
    }

    .stariver-customer-page__header {
      padding: 18px;
    }
    .stariver-customer-page__toolbar {
      padding: 12px;
    }
    .stariver-customer-page__table-shell {
      margin: 0 12px 12px;
    }
    .stariver-customer-page__insight {
      margin: 0 12px 12px;
    }
    .stariver-customer-page__filters {
      width: 100%;
      flex-wrap: wrap;
    }
    .stariver-customer-page__search {
      width: min(100%, 260px);
    }
  }
</style>

<template>
  <div class="stariver-customer-detail-page">
    <div class="stariver-customer-detail-page__header">
      <div class="stariver-customer-detail-page__header-top">
        <div class="stariver-customer-detail-page__chips">
          <span v-if="levelLabel" class="stariver-customer-detail-page__chip" :class="getLevelClass(levelLabel)">{{
            levelLabel
          }}</span>
          <span v-if="industryLabel" class="stariver-customer-detail-page__chip stariver-chip--industry">{{
            industryLabel
          }}</span>
          <span v-if="areaLabel" class="stariver-customer-detail-page__chip stariver-chip--area">{{ areaLabel }}</span>
          <span v-if="scaleLabel" class="stariver-customer-detail-page__chip stariver-chip--scale">{{
            scaleLabel
          }}</span>
        </div>
        <div class="stariver-customer-detail-page__header-actions">
          <n-button class="stariver-customer-detail-page__btn-follow" @click="addFollowRecord">
            <CrmIcon type="iconicon_phone" :size="14" />
            记录跟进
          </n-button>
          <n-button class="stariver-customer-detail-page__btn-opp" @click="addOpportunity"> + 新建商机 </n-button>
        </div>
      </div>
      <div class="stariver-customer-detail-page__header-bottom">
        <h1 class="stariver-customer-detail-page__title">{{ sourceName }}</h1>
        <div class="stariver-customer-detail-page__title-actions">
          <button class="stariver-customer-detail-page__icon-btn" title="编辑客户" @click="editCustomer">
            <CrmIcon type="iconicon_edit" :size="16" />
          </button>
          <button class="stariver-customer-detail-page__icon-btn" title="删除客户" @click="confirmDelete">
            <CrmIcon type="iconicon_delete" :size="16" />
          </button>
        </div>
      </div>
    </div>

    <div class="stariver-customer-detail-page__tabs">
      <button
        v-for="tab in tabList"
        :key="tab.key"
        :class="{ 'stariver-customer-detail-page__tab--active': activeTab === tab.key }"
        class="stariver-customer-detail-page__tab"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="stariver-customer-detail-page__content">
      <template v-if="activeTab === 'overview'">
        <div class="stariver-customer-detail-page__overview">
          <div class="stariver-customer-detail-page__stat-cards">
            <div class="stariver-customer-detail-page__stat-card">
              <div class="stariver-customer-detail-page__stat-label">健康度</div>
              <div class="stariver-customer-detail-page__stat-value">{{ healthScore }}</div>
            </div>
            <div class="stariver-customer-detail-page__stat-card">
              <div class="stariver-customer-detail-page__stat-label">本月新增商机</div>
              <div class="stariver-customer-detail-page__stat-value">{{ monthlyOpportunities }}</div>
            </div>
            <div class="stariver-customer-detail-page__stat-card">
              <div class="stariver-customer-detail-page__stat-label">本月新增合同</div>
              <div class="stariver-customer-detail-page__stat-value">{{ monthlyContracts }}</div>
            </div>
          </div>

          <div class="stariver-customer-detail-page__panel">
            <div class="stariver-customer-detail-page__panel-title">业务关系概览</div>
            <div class="stariver-customer-detail-page__panel-body">
              <span class="stariver-customer-detail-page__business-text">
                <strong>{{ sourceName }}</strong> 自 <strong>{{ firstContractDate || '—' }}</strong> 合作至今，共签订
                <strong>{{ contractCount }}</strong> 份合同；目前在途商机 <strong>{{ opportunityCount }}</strong> 个。
              </span>
            </div>
          </div>

          <div class="stariver-customer-detail-page__panel">
            <div class="stariver-customer-detail-page__panel-title">关联对象关系图</div>
            <div class="stariver-customer-detail-page__relation-graph">
              <div class="stariver-customer-detail-page__graph-row">
                <div class="stariver-customer-detail-page__graph-node stariver-graph--opportunity">
                  <div class="stariver-graph-label">商机</div>
                  <div class="stariver-graph-count">{{ opportunityCount }}</div>
                </div>
                <div class="stariver-customer-detail-page__graph-node stariver-graph--contract">
                  <div class="stariver-graph-label">合同</div>
                  <div class="stariver-graph-count">{{ contractCount }}</div>
                </div>
              </div>
              <div class="stariver-customer-detail-page__graph-center">
                <div class="stariver-customer-detail-page__graph-node stariver-graph--customer">
                  <div class="stariver-graph-label">{{ sourceShortName }}</div>
                  <div class="stariver-graph-sub">客户</div>
                </div>
              </div>
              <div class="stariver-customer-detail-page__graph-row">
                <div class="stariver-customer-detail-page__graph-node stariver-graph--order">
                  <div class="stariver-graph-label">工单</div>
                  <div class="stariver-graph-count">{{ orderCount }}</div>
                </div>
                <div class="stariver-customer-detail-page__graph-node stariver-graph--auth">
                  <div class="stariver-graph-label">授权</div>
                  <div class="stariver-graph-count">—</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else-if="activeTab === 'opportunityInfo'" class="stariver-customer-detail-page__tab-content">
        <opportunityTable
          :source-id="customerId"
          :customer-name="sourceName"
          is-customer-tab
          :form-key="FormDesignKeyEnum.CUSTOMER_OPPORTUNITY"
        />
      </div>

      <div v-else-if="activeTab === 'contact'" class="stariver-customer-detail-page__tab-content">
        <ContactTable
          :refresh-key="refreshKey"
          :source-id="customerId"
          :initial-source-name="sourceName"
          :form-key="FormDesignKeyEnum.CUSTOMER_CONTACT"
        />
      </div>

      <div v-else-if="activeTab === 'followRecord'" class="stariver-customer-detail-page__tab-content">
        <FollowDetail
          active-type="followRecord"
          :follow-api-key="FormDesignKeyEnum.CUSTOMER"
          :source-id="customerId"
          :refresh-key="refreshKey"
          :initial-source-name="sourceName"
          :show-add="true"
          :show-action="true"
          :parentFormKey="FormDesignKeyEnum.CUSTOMER"
        />
      </div>

      <div v-else-if="activeTab === 'contract'" class="stariver-customer-detail-page__tab-content">
        <ContractTimeline :form-key="FormDesignKeyEnum.CONTRACT" :source-id="customerId" />
      </div>

      <div v-else-if="activeTab === 'order'" class="stariver-customer-detail-page__tab-content">
        <OrderTable :formKey="FormDesignKeyEnum.CUSTOMER_ORDER" :sourceId="customerId" isCustomerTab />
      </div>

      <div v-else-if="activeTab === 'auth'" class="stariver-customer-detail-page__tab-content">
        <div class="stariver-customer-detail-page__empty-tab">
          <p>授权模块暂未开放</p>
        </div>
      </div>
    </div>

    <CrmFormCreateDrawer
      v-model:visible="formDrawerVisible"
      :form-key="activeFormKey"
      :source-id="activeSourceId"
      :need-init-detail="needInitDetail"
      :initial-source-name="sourceName"
      @saved="handleFormSaved"
    />

    <CrmMoveModal
      v-model:show="showMoveModal"
      :reason-key="ReasonTypeEnum.CUSTOMER_POOL_RS"
      :source-id="customerId"
      :name="sourceName"
      type="warning"
      @refresh="handleDeleted"
    />
  </div>
</template>

<script setup lang="ts">
  import { useRoute, useRouter } from 'vue-router';
  import { NButton, useDialog, useMessage } from 'naive-ui';
  import dayjs from 'dayjs';

  import { FieldTypeEnum, FormDesignKeyEnum } from '@lib/shared/enums/formDesignEnum';
  import { ReasonTypeEnum } from '@lib/shared/enums/moduleEnum';
  import { getCityPath } from '@lib/shared/method';
  import type { ModuleField } from '@lib/shared/models/common';

  import CrmIcon from '@/components/pure/crm-icon-font/index.vue';
  import type { FormCreateField } from '@/components/business/crm-form-create/types';
  import CrmFormCreateDrawer from '@/components/business/crm-form-create-drawer/index.vue';
  import ContactTable from '@/components/business/crm-form-create-table/contactTable.vue';
  import CrmMoveModal from '@/components/business/crm-move-modal/index.vue';
  import ContractTimeline from '@/views/contract/contract/components/contractTimeline.vue';
  import opportunityTable from '@/views/opportunity/components/opportunityTable.vue';
  import OrderTable from '@/views/order/order/components/orderTable.vue';

  import { deleteCustomer, getCustomer, getCustomerDetailStatistic, getCustomerFormConfig } from '@/api/modules';

  import { CustomerRouteEnum } from '@/enums/routeEnum';

  const FollowDetail = defineAsyncComponent(() => import('@/components/business/crm-follow-detail/index.vue'));

  const route = useRoute();
  const router = useRouter();
  const dialog = useDialog();
  const message = useMessage();

  const customerId = computed(() => String(route.params.id ?? ''));

  const loading = ref(true);
  const sourceName = ref('');
  const sourceShortName = ref('');
  const formFields = ref<FormCreateField[]>([]);
  const optionMap = ref<Record<string, any[]>>({});
  const customerDetail = ref<any>(null);
  const refreshKey = ref(0);
  const activeTab = ref('overview');

  const formDrawerVisible = ref(false);
  const activeSourceId = ref('');
  const activeFormKey = ref(FormDesignKeyEnum.CUSTOMER);
  const needInitDetail = ref(false);
  const showMoveModal = ref(false);

  const opportunityCount = ref(0);
  const contractCount = ref(0);
  const orderCount = ref(0);
  const monthlyOpportunities = ref(0);
  const monthlyContracts = ref(0);
  const firstContractDate = ref('');

  const tabList = computed(() => [
    { key: 'overview', label: '概览' },
    { key: 'opportunityInfo', label: `商机 ${opportunityCount.value}` },
    { key: 'contract', label: `合同 ${contractCount.value}` },
    { key: 'order', label: `工单 ${orderCount.value}` },
    { key: 'auth', label: '授权 —' },
    { key: 'contact', label: '联系人' },
    { key: 'followRecord', label: '跟进时间轴' },
  ]);

  const healthScore = computed(() => {
    if (opportunityCount.value > 0 && contractCount.value > 0) return 82;
    if (opportunityCount.value > 0 || contractCount.value > 0) return 55;
    return 30;
  });

  const fieldByInternalKey = computed(() => {
    return formFields.value.reduce<Record<string, FormCreateField>>((map, field) => {
      if (field.internalKey) map[field.internalKey] = field;
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

  function getRawFieldValue(internalKey: string) {
    const field = getField(internalKey);
    if (!field || !customerDetail.value?.moduleFields) return '';
    const moduleField = customerDetail.value.moduleFields.find((item: ModuleField) => item.fieldId === field.id);
    return moduleField?.fieldValue ?? '';
  }

  function getFieldDisplay(internalKey: string) {
    const rawValue = getRawFieldValue(internalKey);
    const field = getField(internalKey);
    if (!field || rawValue === '' || rawValue === null || rawValue === undefined) return '-';
    if (field.type === FieldTypeEnum.LOCATION) {
      return getLocationLabel(String(rawValue), field.scope);
    }
    const options = optionMap.value[field.id] || field.options || [];
    return getOptionLabel(options, rawValue) || String(rawValue);
  }

  const levelLabel = computed(() => getFieldDisplay('customerLevel'));
  const industryLabel = computed(() => getFieldDisplay('customerIndustry'));
  const areaLabel = computed(() => getFieldDisplay('customerArea'));
  const scaleLabel = computed(() => getFieldDisplay('customerScale'));

  function getLevelClass(level: string) {
    if (level.includes('KA')) return 'stariver-level-tag--ka';
    if (level.startsWith('A')) return 'stariver-level-tag--a';
    if (level.startsWith('B')) return 'stariver-level-tag--b';
    return '';
  }

  async function loadDetail() {
    loading.value = true;
    try {
      const configRes = (await getCustomerFormConfig()) as any;
      formFields.value = configRes.fields || [];
      optionMap.value = configRes.optionMap || {};

      const detailRes = await getCustomer(customerId.value);
      customerDetail.value = detailRes;
      sourceName.value = detailRes.name || '';

      const shortField = getField('customerShortName');
      if (shortField && detailRes.moduleFields) {
        const sf = detailRes.moduleFields.find((m: ModuleField) => m.fieldId === shortField.id);
        const rawVal = sf?.fieldValue;
        sourceShortName.value = Array.isArray(rawVal) ? rawVal.join(',') : rawVal || detailRes.name || '';
      } else {
        sourceShortName.value = detailRes.name || '';
      }

      const detailStat = await getCustomerDetailStatistic(customerId.value);
      opportunityCount.value = detailStat?.totalOpportunities ?? 0;
      contractCount.value = detailStat?.totalContracts ?? 0;
      monthlyOpportunities.value = detailStat?.monthlyOpportunities ?? 0;
      monthlyContracts.value = detailStat?.monthlyContracts ?? 0;
      firstContractDate.value = detailStat?.firstContractDate
        ? dayjs(detailStat.firstContractDate).format('YYYY-MM-DD')
        : '';
    } catch {
      message.error('加载客户详情失败');
    } finally {
      loading.value = false;
    }
  }

  function editCustomer() {
    activeFormKey.value = FormDesignKeyEnum.CUSTOMER;
    activeSourceId.value = customerId.value;
    needInitDetail.value = true;
    formDrawerVisible.value = true;
  }

  function addFollowRecord() {
    activeFormKey.value = FormDesignKeyEnum.FOLLOW_RECORD_CUSTOMER;
    activeSourceId.value = customerId.value;
    needInitDetail.value = false;
    formDrawerVisible.value = true;
  }

  function addOpportunity() {
    activeFormKey.value = FormDesignKeyEnum.CUSTOMER_OPPORTUNITY;
    activeSourceId.value = customerId.value;
    needInitDetail.value = false;
    formDrawerVisible.value = true;
  }

  function confirmDelete() {
    dialog.warning({
      title: '删除客户',
      content: `确认删除「${sourceName.value}」？`,
      positiveText: '删除',
      negativeText: '取消',
      positiveButtonProps: { type: 'error' },
      onPositiveClick: async () => {
        await deleteCustomer(customerId.value);
        message.success('客户已删除');
        router.push({ name: CustomerRouteEnum.CUSTOMER_INDEX });
      },
    });
  }

  function handleDeleted() {
    router.push({ name: CustomerRouteEnum.CUSTOMER_INDEX });
  }

  function handleFormSaved() {
    refreshKey.value += 1;
    loadDetail();
  }

  onMounted(() => loadDetail());
</script>

<style lang="less" scoped>
  .stariver-customer-detail-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    background: #f8fafc;
    color: #0f172a;
    overflow: hidden;
  }

  .stariver-customer-detail-page__header {
    padding: 16px 24px 12px;
    background: #fff;
    border-bottom: 1px solid #e2e8f0;
    flex-shrink: 0;
  }

  .stariver-customer-detail-page__header-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .stariver-customer-detail-page__chips {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .stariver-customer-detail-page__chip {
    display: inline-flex;
    height: 20px;
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

  .stariver-chip--industry {
    background: #f0fdf4 !important;
    color: #166534 !important;
  }

  .stariver-chip--area {
    background: #fef2f2 !important;
    color: #991b1b !important;
  }

  .stariver-chip--scale {
    background: #f5f3ff !important;
    color: #5b21b6 !important;
  }

  .stariver-customer-detail-page__header-actions {
    display: flex;
    gap: 8px;
  }

  .stariver-customer-detail-page__btn-follow {
    height: 30px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    background: #fff;
    border: 1px solid #e2e8f0;
    color: #334155;
    padding: 0 12px;
    cursor: pointer;
  }

  .stariver-customer-detail-page__btn-opp {
    height: 30px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    background: #0f172a;
    border: 1px solid #0f172a;
    color: #fff;
    padding: 0 12px;
    cursor: pointer;
  }

  .stariver-customer-detail-page__header-bottom {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .stariver-customer-detail-page__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    line-height: 28px;
    color: #0f172a;
  }

  .stariver-customer-detail-page__title-actions {
    display: flex;
    gap: 4px;
  }

  .stariver-customer-detail-page__icon-btn {
    display: flex;
    width: 28px;
    height: 28px;
    align-items: center;
    justify-content: center;
    border: 0;
    border-radius: 6px;
    background: transparent;
    color: #94a3b8;
    cursor: pointer;
  }

  .stariver-customer-detail-page__icon-btn:hover {
    background: #eef2ff;
    color: #4338ca;
  }

  .stariver-customer-detail-page__tabs {
    display: flex;
    align-items: center;
    padding: 0 24px;
    background: #fff;
    border-bottom: 1px solid #e2e8f0;
    gap: 0;
    flex-shrink: 0;
  }

  .stariver-customer-detail-page__tab {
    display: flex;
    height: 38px;
    align-items: center;
    padding: 0 14px;
    border: 0;
    border-bottom: 2px solid transparent;
    background: transparent;
    color: #64748b;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
  }

  .stariver-customer-detail-page__tab--active {
    color: #4338ca;
    border-bottom-color: #4338ca;
    font-weight: 600;
  }

  .stariver-customer-detail-page__content {
    flex: 1;
    min-height: 0;
    overflow: auto;
    padding: 20px 24px 24px;
  }

  .stariver-customer-detail-page__overview {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .stariver-customer-detail-page__stat-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .stariver-customer-detail-page__stat-card {
    background: #fff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 14px 18px;
  }

  .stariver-customer-detail-page__stat-label {
    font-size: 12px;
    color: #64748b;
    margin-bottom: 4px;
  }

  .stariver-customer-detail-page__stat-value {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
  }

  .stariver-customer-detail-page__panel {
    background: #fff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 18px;
  }

  .stariver-customer-detail-page__panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #0f172a;
    margin-bottom: 14px;
  }

  .stariver-customer-detail-page__panel-body {
    font-size: 13px;
    color: #475569;
    line-height: 22px;
  }

  .stariver-customer-detail-page__business-text strong {
    color: #0f172a;
    font-weight: 600;
  }

  .stariver-customer-detail-page__relation-graph {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 10px 0;
  }

  .stariver-customer-detail-page__graph-row {
    display: flex;
    gap: 120px;
  }

  .stariver-customer-detail-page__graph-center {
    display: flex;
    justify-content: center;
  }

  .stariver-customer-detail-page__graph-node {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    padding: 10px 20px;
    min-width: 100px;
  }

  .stariver-graph--opportunity {
    background: #eef2ff;
    border: 1px solid #c7d2fe;
  }

  .stariver-graph--contract {
    background: #ecfeff;
    border: 1px solid #a5f3fc;
  }

  .stariver-graph--order {
    background: #fef3c7;
    border: 1px solid #fde68a;
  }

  .stariver-graph--auth {
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
  }

  .stariver-graph--customer {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    padding: 14px 28px;
  }

  .stariver-graph-label {
    font-size: 12px;
    font-weight: 600;
    color: #475569;
  }

  .stariver-graph-count {
    font-size: 24px;
    font-weight: 700;
    color: #0f172a;
  }

  .stariver-graph-sub {
    font-size: 11px;
    color: #64748b;
    margin-top: 2px;
  }

  .stariver-customer-detail-page__tab-content {
    height: 100%;
    overflow: auto;
  }

  .stariver-customer-detail-page__empty-tab {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: #94a3b8;
    font-size: 14px;
  }
</style>

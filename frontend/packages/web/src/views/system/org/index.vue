<template>
  <StariverModulePage title="组织架构" count-label="用户与部门" eyebrow="系统">
    <template #toolbar>
      <div class="stariver-tabs">
        <button class="stariver-tab stariver-tab--active">组织树</button>
        <button class="stariver-tab">成员列表</button>
        <button class="stariver-tab">部门权限</button>
      </div>
      <div class="stariver-filters">
        <button class="stariver-filter">同步状态：正常</button>
        <button class="stariver-filter">数据范围：部门/下级</button>
      </div>
    </template>

    <div class="stariver-system-stack">
      <StariverInsightStrip :metrics="orgMetrics" :process="orgProcess" :fields="orgFields" :rules="orgRules" />
      <CrmCard
        no-content-padding
        hide-footer
        class="stariver-system-card"
        :special-height="licenseStore.expiredDuring ? 64 : 0"
      >
        <CrmSplitPanel class="h-full" :max="0.5" :min="0.25" :default-size="0.25">
          <template #1>
            <div class="org-tree-wrapper h-full">
              <OrgModuleTree
                ref="orgModuleTreeRef"
                @select-node="selectNode"
                @load-list="() => orgTableRef?.initOrgList()"
              />
            </div>
          </template>
          <template #2>
            <OrgTable
              ref="orgTableRef"
              :active-node="activeNodeId"
              :offspring-ids="offspringIds"
              @add-success="addSuccess"
            />
          </template>
        </CrmSplitPanel>
      </CrmCard>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import CrmCard from '@/components/pure/crm-card/index.vue';
  import CrmSplitPanel from '@/components/pure/crm-split-panel/index.vue';
  import StariverInsightStrip from '@/components/business/stariver-insight-strip/index.vue';
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import OrgModuleTree from './components/moduleTree.vue';
  import OrgTable from '@/views/system/org/components/orgTable.vue';

  import useLicenseStore from '@/store/modules/setting/license';

  const activeNodeId = ref<string | number>('');
  const offspringIds = ref<string[]>([]);

  const licenseStore = useLicenseStore();

  const orgMetrics = [
    { label: '部门层级', value: '6', hint: '销售、交付、产品协同维护', tone: 'blue' },
    { label: '启用成员', value: '42', hint: '默认沿用原系统用户能力', tone: 'emerald' },
    { label: '待补资料', value: '8', hint: '岗位、直属上级或手机号缺失', tone: 'amber' },
    { label: '权限角色', value: '9', hint: '按角色和数据范围授权', tone: 'indigo' },
  ];

  const orgProcess = ['创建部门', '添加成员', '绑定岗位', '分配角色', '设置数据范围'];
  const orgFields = ['姓名', '部门', '岗位', '直属上级', '手机号', '角色'];
  const orgRules = [
    '组织架构暂沿用原用户能力',
    '销售负责人用于客户、线索、商机归属',
    '部门数据范围会影响客户和合同可见性',
  ];

  function selectNode(_selectedKeys: Array<string | number>, _offspringIds: string[]) {
    [activeNodeId.value] = _selectedKeys;
    offspringIds.value = _offspringIds;
  }

  const orgModuleTreeRef = ref<InstanceType<typeof OrgModuleTree>>();
  function addSuccess() {
    orgModuleTreeRef.value?.initTree();
  }

  const orgTableRef = ref<InstanceType<typeof OrgTable>>();
</script>

<style lang="less" scoped>
  .stariver-system-stack {
    display: flex;
    height: 100%;
    min-height: 0;
    flex-direction: column;
    gap: 12px;
  }

  .stariver-system-card {
    min-height: 0;
    flex: 1;
    overflow: hidden;
  }

  .org-tree-wrapper {
    padding: 24px;
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

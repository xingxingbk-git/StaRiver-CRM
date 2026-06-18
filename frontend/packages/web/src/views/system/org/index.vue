<template>
  <StariverModulePage title="组织架构">
    <div class="org-management-page">
      <aside class="org-management-page__tree">
        <OrgModuleTree ref="orgModuleTreeRef" @select-node="selectNode" @load-list="() => orgTableRef?.initOrgList()" />
      </aside>

      <section class="org-management-page__table">
        <OrgTable
          ref="orgTableRef"
          :active-node="activeNodeId"
          :offspring-ids="offspringIds"
          @add-success="addSuccess"
        />
      </section>
    </div>
  </StariverModulePage>
</template>

<script setup lang="ts">
  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';
  import OrgModuleTree from './components/moduleTree.vue';
  import OrgTable from '@/views/system/org/components/orgTable.vue';

  const activeNodeId = ref<string | number>('');
  const offspringIds = ref<string[]>([]);

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
  .org-management-page {
    display: grid;
    height: 100%;
    min-height: 0;
    grid-template-columns: minmax(320px, 454px) minmax(0, 1fr);
    gap: 16px;
  }

  .org-management-page__tree,
  .org-management-page__table {
    min-height: 0;
    overflow: hidden;
    background: #ffffff;
    border: 1px solid #dbe4f0;
    border-radius: 8px;
  }
</style>

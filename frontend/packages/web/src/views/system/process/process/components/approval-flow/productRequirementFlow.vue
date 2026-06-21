<template>
  <section class="prm-flow-config">
    <div class="prm-flow-config__header">
      <div>
        <h3>产品需求交付流程</h3>
        <p>需求评审通过后，按以下节点和负责人依次推进。</p>
      </div>
    </div>
    <div class="prm-flow-config__chain">
      <template v-for="(stage, index) in stages" :key="stage.key">
        <button
          type="button"
          class="prm-flow-config__node"
          :class="{ 'is-active': activeIndex === index }"
          @click="activeIndex = index"
        >
          <span>{{ stage.name }}</span>
          <small>{{ stage.assigneeNames.length ? stage.assigneeNames.join('、') : '跟随产品负责人' }}</small>
        </button>
        <span v-if="index < stages.length - 1" class="prm-flow-config__line">→</span>
      </template>
    </div>
    <div v-if="activeStage" class="prm-flow-config__editor">
      <label>
        <span>节点名称</span>
        <n-input v-model:value="activeStage.name" :disabled="readonly" @update:value="emitConfig" />
      </label>
      <label>
        <span>节点负责人</span>
        <n-select
          v-model:value="activeStage.assigneeIds"
          multiple
          filterable
          clearable
          :disabled="readonly || activeStage.key === 'COMPLETED'"
          :options="userOptions"
          placeholder="未选择时跟随产品负责人"
          @update:value="handleAssigneeChange"
        />
      </label>
      <div class="prm-flow-config__flags">
        <n-checkbox v-model:checked="activeStage.returnable" :disabled="readonly" @update:checked="emitConfig">
          允许退回上一节点
        </n-checkbox>
        <n-checkbox v-if="activeStage.key === 'ACCEPTANCE'" v-model:checked="activeStage.requiresProductLink" disabled>
          提交时关联产品模块和预发布版本
        </n-checkbox>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
  /* eslint import/no-unresolved: off */
  import { computed, onMounted, ref, watch } from 'vue';
  import { NCheckbox, NInput, NSelect } from 'naive-ui';

  import type { ProductRequirementStageConfig } from '@lib/shared/models/system/process';

  import { getUserOptions } from '@/api/modules/productManagement';

  const props = defineProps<{ readonly?: boolean }>();
  const businessConfig = defineModel<string>('businessConfig', { default: '' });

  const defaults: ProductRequirementStageConfig[] = [
    {
      key: 'PRODUCT_DESIGN',
      name: '产品设计',
      assigneeIds: [],
      assigneeNames: [],
      returnable: false,
      requiresProductLink: false,
    },
    {
      key: 'TECH_REVIEW',
      name: '技术评审',
      assigneeIds: [],
      assigneeNames: [],
      returnable: true,
      requiresProductLink: false,
    },
    {
      key: 'DEVELOPMENT',
      name: '开发',
      assigneeIds: [],
      assigneeNames: [],
      returnable: true,
      requiresProductLink: false,
    },
    { key: 'TESTING', name: '测试', assigneeIds: [], assigneeNames: [], returnable: true, requiresProductLink: false },
    {
      key: 'ACCEPTANCE',
      name: '产品验收',
      assigneeIds: [],
      assigneeNames: [],
      returnable: true,
      requiresProductLink: true,
    },
    { key: 'RELEASE', name: '发布', assigneeIds: [], assigneeNames: [], returnable: true, requiresProductLink: false },
    {
      key: 'COMPLETED',
      name: '完成',
      assigneeIds: [],
      assigneeNames: [],
      returnable: false,
      requiresProductLink: false,
    },
  ];

  const stages = ref<ProductRequirementStageConfig[]>([]);
  const activeIndex = ref(0);
  const userOptions = ref<Array<{ label: string; value: string }>>([]);
  const activeStage = computed(() => stages.value[activeIndex.value]);

  function parseConfig(value: string) {
    try {
      const parsed = JSON.parse(value || '{}');
      stages.value = Array.isArray(parsed.stages) && parsed.stages.length ? parsed.stages : structuredClone(defaults);
    } catch {
      stages.value = structuredClone(defaults);
    }
  }

  function emitConfig() {
    businessConfig.value = JSON.stringify({ stages: stages.value });
  }

  function handleAssigneeChange(ids: string[]) {
    if (!activeStage.value) return;
    activeStage.value.assigneeNames = ids
      .map((id) => userOptions.value.find((option) => option.value === id)?.label)
      .filter((name): name is string => Boolean(name));
    emitConfig();
  }

  onMounted(async () => {
    parseConfig(businessConfig.value);
    userOptions.value = await getUserOptions();
    emitConfig();
  });

  watch(
    () => businessConfig.value,
    (value) => {
      if (value && value !== JSON.stringify({ stages: stages.value })) parseConfig(value);
    }
  );
</script>

<style scoped lang="less">
  .prm-flow-config {
    padding: 20px;
    border-bottom: 1px solid var(--text-n8);
    background: var(--text-n10);
  }
  .prm-flow-config__header h3 {
    margin: 0;
    font-size: 16px;
  }
  .prm-flow-config__header p {
    margin: 4px 0 16px;
    color: var(--text-n4);
  }
  .prm-flow-config__chain {
    display: flex;
    align-items: center;
    overflow-x: auto;
    padding: 16px;
    border-radius: 6px;
    background: var(--text-n9);
  }
  .prm-flow-config__node {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 126px;
    min-width: 126px;
    height: 68px;
    border: 1px solid var(--text-n7);
    border-radius: 6px;
    background: var(--text-n10);
    flex-direction: column;
    cursor: pointer;
  }
  .prm-flow-config__node.is-active {
    border: 2px solid var(--primary-8);
    color: var(--primary-8);
  }
  .prm-flow-config__node small {
    overflow: hidden;
    margin-top: 4px;
    max-width: 108px;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-n4);
  }
  .prm-flow-config__line {
    padding: 0 8px;
    color: var(--text-n5);
  }
  .prm-flow-config__editor {
    display: grid;
    grid-template-columns: 220px minmax(320px, 1fr);
    gap: 16px;
    margin-top: 16px;
    padding: 16px;
    border: 1px solid var(--text-n8);
    border-radius: 6px;
  }
  .prm-flow-config__editor label {
    display: flex;
    gap: 8px;
    flex-direction: column;
  }
  .prm-flow-config__editor label > span {
    font-weight: 600;
  }
  .prm-flow-config__flags {
    display: flex;
    grid-column: 1 / -1;
    gap: 24px;
  }
</style>

<template>
  <CrmModal
    v-model:show="showEditDrawer"
    size="small"
    :title="t('common.batchEdit')"
    :ok-loading="loading"
    @cancel="cancelHandler"
  >
    <n-form ref="formRef" :model="form" label-placement="left" label-width="auto">
      <n-form-item
        :rule="[
          {
            required: true,
            trigger: ['input', 'blur'],
            message: t('common.notNull', { value: `${t('org.attributes')}` }),
          },
        ]"
        require-mark-placement="left"
        label-placement="left"
        path="attributes"
        :label="t('org.attributes')"
      >
        <n-select
          v-model:value="form.attributes"
          :placeholder="t('common.pleaseSelect')"
          :options="attributesOptions"
        />
      </n-form-item>
      <n-form-item
        v-if="!form.attributes"
        :rule="[
          {
            required: true,
            trigger: ['input', 'blur'],
            message: t('common.value.notNull'),
          },
        ]"
        require-mark-placement="left"
        label-placement="left"
        path="value"
        :label="t('common.batchUpdate')"
      >
        <n-select v-model:value="form.value" :placeholder="t('common.pleaseSelect')" :options="valueOptions" />
      </n-form-item>
      <n-form-item
        v-else-if="form.attributes === 'departmentId'"
        :rule="[{ required: true, trigger: ['input', 'blur'], message: t('common.value.notNull') }]"
        require-mark-placement="left"
        label-placement="left"
        path="value"
        :label="t('common.batchUpdate')"
      >
        <n-tree-select
          v-model:value="form.value"
          :options="department"
          label-field="name"
          key-field="id"
          filterable
          clearable
          children-field="children"
        />
      </n-form-item>
      <n-form-item
        v-else-if="form.attributes === 'onboardingDate'"
        :rule="[{ required: true, trigger: ['input', 'blur'], type: 'number', message: t('common.value.notNull') }]"
        require-mark-placement="left"
        label-placement="left"
        path="onboardingDate"
        :label="t('common.batchUpdate')"
      >
        <n-date-picker v-model:value="form.onboardingDate" type="date" class="w-full"> </n-date-picker>
      </n-form-item>
    </n-form>
    <template #footer>
      <div class="flex w-full items-center justify-end gap-[12px]">
        <n-button :disabled="loading" secondary @click="cancelHandler">
          {{ t('common.cancel') }}
        </n-button>
        <n-button :loading="loading" type="primary" @click="confirmHandler">
          {{ t('common.update') }}
        </n-button>
      </div>
    </template>
  </CrmModal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import {
    DataTableRowKey,
    FormInst,
    NButton,
    NDatePicker,
    NForm,
    NFormItem,
    NSelect,
    NTreeSelect,
    SelectOption,
    useMessage,
  } from 'naive-ui';
  import { cloneDeep } from 'lodash-es';

  import { useI18n } from '@lib/shared/hooks/useI18n';

  import CrmModal from '@/components/pure/crm-modal/index.vue';
  import type { CrmTreeNodeData } from '@/components/pure/crm-tree/type';

  import { batchEditUser, getDepartmentTree } from '@/api/modules';

  const Message = useMessage();
  const { t } = useI18n();

  const props = defineProps<{
    userIds: DataTableRowKey[];
  }>();

  const emit = defineEmits<{
    (e: 'loadList'): void;
  }>();

  const showEditDrawer = defineModel<boolean>('show', {
    required: true,
    default: false,
  });

  const initForm = {
    attributes: null,
    value: null,
    onboardingDate: undefined,
  };

  const form = ref<{
    attributes: string | null;
    value: string | null;
    onboardingDate: number | undefined;
  }>(cloneDeep(initForm));

  const attributesOptions = computed<SelectOption[]>(() => {
    return [
      {
        value: 'departmentId',
        label: t('org.department'),
      },
      {
        value: 'onboardingDate',
        label: t('org.onboardingDate'),
      },
    ];
  });

  const valueOptions = ref([]);

  function cancelHandler() {
    form.value = cloneDeep(initForm);
    showEditDrawer.value = false;
  }

  const loading = ref<boolean>(false);
  const formRef = ref<FormInst | null>(null);
  function confirmHandler() {
    formRef.value?.validate(async (error) => {
      if (!error) {
        try {
          const { value, attributes, onboardingDate } = form.value;

          if (!attributes) {
            return;
          }
          loading.value = true;
          let payloadValue;
          if (attributes === 'onboardingDate') {
            payloadValue = onboardingDate;
          } else {
            payloadValue = value;
          }
          await batchEditUser({
            ids: props.userIds,
            [attributes]: payloadValue,
          });
          cancelHandler();
          emit('loadList');
          Message.success(t('common.updateSuccess'));
        } catch (e) {
          // eslint-disable-next-line no-console
          console.log(e);
        } finally {
          loading.value = false;
        }
      }
    });
  }

  const department = ref<CrmTreeNodeData[]>([]);
  async function initDepartList() {
    try {
      department.value = await getDepartmentTree();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => showEditDrawer.value,
    (val) => {
      if (val) {
        initDepartList();
      }
    }
  );
</script>

<style scoped></style>

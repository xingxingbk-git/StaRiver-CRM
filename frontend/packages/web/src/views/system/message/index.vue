<template>
  <StariverModulePage :title="pageTitle">
    <template v-if="mode === 'edit'" #subtitle>
      <div class="message-edit-title-block">
        <h1>新建通知模板</h1>
        <div>自定义多级审批流程</div>
      </div>
    </template>

    <template v-if="mode === 'edit'" #actions>
      <n-button :disabled="saving" @click="handleCancelEdit">取消</n-button>
      <n-button v-permission="['SYSTEM_NOTICE:UPDATE']" type="primary" :loading="saving" @click="handleSubmit">
        提交 →
      </n-button>
    </template>

    <div v-if="mode === 'list'" class="message-settings-page">
      <section class="message-template-card">
        <div class="message-template-toolbar">
          <n-input v-model:value="keyword" clearable placeholder="搜索" class="message-template-search">
            <template #prefix>
              <n-icon><Search /></n-icon>
            </template>
          </n-input>
          <n-select
            v-model:value="eventFilter"
            :options="eventFilterOptions"
            class="message-template-filter"
            :consistent-menu-width="false"
          />
          <n-button v-permission="['SYSTEM_NOTICE:ADD', 'SYSTEM_NOTICE:UPDATE']" type="primary" @click="handleCreate">
            + 新建模板
          </n-button>
        </div>

        <n-data-table
          :single-line="false"
          :columns="columns"
          :data="filteredTemplates"
          :scroll-x="760"
          :pagination="false"
          :loading="loading"
          class="message-template-table"
          :row-key="(row: NotificationTemplateRow) => row.key"
          :row-props="rowProps"
        />
      </section>

      <aside class="message-preview-card">
        <template v-if="activeTemplate">
          <div class="message-preview-header">
            <h2>{{ activeTemplate.code }} · {{ activeTemplate.name }}</h2>
            <div>{{ activeTemplate.event }}</div>
          </div>

          <div class="message-preview-section">
            <div class="message-preview-label">触发阈值</div>
            <div class="message-trigger-box">{{ activeTemplate.triggerRule }}</div>
          </div>

          <div v-if="activeTemplate.sysEnable" class="message-preview-section">
            <div class="message-preview-label">系统消息</div>
            <div class="message-system-card">
              <strong>{{ activeTemplate.systemTitle }}</strong>
              <p v-for="line in activeTemplate.systemLines" :key="line" v-html="highlightVariables(line)"></p>
              <div class="message-system-actions">
                <n-button size="small">查看详情</n-button>
                <n-button size="small">处理</n-button>
              </div>
            </div>
          </div>

          <div v-if="activeTemplate.emailEnable" class="message-preview-section">
            <div class="message-preview-label">邮件通知</div>
            <div class="message-email-preview">
              <strong>{{ activeTemplate.emailSubject || activeTemplate.systemTitle }}</strong>
              <p v-for="line in activeTemplate.emailBodyLines" :key="line" v-html="highlightVariables(line)"></p>
            </div>
          </div>

          <div class="message-preview-section">
            <div class="message-preview-label">变量</div>
            <div class="message-variable-list">
              <span v-for="item in activeTemplate.variables" :key="item">{{ item }}</span>
            </div>
          </div>
        </template>
      </aside>
    </div>

    <div v-else class="message-edit-page">
      <section class="message-edit-card">
        <h2>模板信息</h2>
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <div class="message-edit-grid">
            <n-form-item label="模板名称" path="name">
              <n-input v-model:value="form.name" placeholder="请输入模板名称" />
            </n-form-item>
            <n-form-item label="适用渠道" path="channels">
              <div class="message-channel-grid">
                <n-checkbox v-model:checked="form.sysEnable">系统消息</n-checkbox>
                <n-checkbox v-model:checked="form.emailEnable">邮件通知</n-checkbox>
              </div>
            </n-form-item>
          </div>

          <n-form-item label="触发事件" path="event">
            <n-select
              v-model:value="form.key"
              :options="templateOptions"
              :consistent-menu-width="false"
              @update:value="handleTemplateSelect"
            />
          </n-form-item>

          <n-form-item label="标题" path="title">
            <n-input v-model:value="form.title" placeholder="支持变量 {{ticketId}}" />
          </n-form-item>

          <n-form-item label="正文" path="body">
            <n-input
              v-model:value="form.body"
              type="textarea"
              :autosize="{ minRows: 6, maxRows: 10 }"
              placeholder="请输入正文"
            />
          </n-form-item>
        </n-form>
      </section>

      <section class="message-edit-card">
        <h2>可用变量</h2>
        <div class="message-variable-list">
          <button v-for="item in form.variables" :key="item" type="button" @click="insertVariable(item)">
            {{ item }}
          </button>
        </div>
      </section>
    </div>
  </StariverModulePage>
</template>

<script lang="ts" setup>
  import { useRoute, useRouter } from 'vue-router';
  import {
    DataTableColumn,
    FormInst,
    FormRules,
    NButton,
    NCheckbox,
    NDataTable,
    NForm,
    NFormItem,
    NIcon,
    NInput,
    NSelect,
    NSwitch,
    useMessage,
  } from 'naive-ui';
  import { Search } from '@vicons/ionicons5';

  import type {
    MessageConfigItem,
    MessageTaskDetailDTOItem,
    SaveMessageConfigParams,
  } from '@lib/shared/models/system/message';

  import StariverModulePage from '@/components/business/stariver-module-page/index.vue';

  import { getMessageTask, saveMessageTask } from '@/api/modules';

  type NotificationTemplateRow = MessageConfigItem &
    MessageTaskDetailDTOItem & {
      key: string;
      code: string;
      name: string;
      channelNames: string[];
      monthlyCount: number;
      triggerRule: string;
      systemTitle: string;
      systemBody: string;
      systemLines: string[];
      emailSubject: string;
      emailBody: string;
      emailBodyLines: string[];
      variables: string[];
    };

  const message = useMessage();
  const route = useRoute();
  const router = useRouter();
  const loading = ref(false);
  const saving = ref(false);
  const mode = ref<'list' | 'edit'>('list');
  const keyword = ref('');
  const eventFilter = ref('ALL');
  const templates = ref<NotificationTemplateRow[]>([]);
  const activeKey = ref('');
  const formRef = ref<FormInst | null>(null);

  const pageTitle = computed(() => (mode.value === 'list' ? '消息设置' : ''));
  const activeTemplate = computed(
    () => templates.value.find((item) => item.key === activeKey.value) || templates.value[0]
  );
  const eventFilterOptions = computed(() => [
    { label: '全部事件', value: 'ALL' },
    ...Array.from(new Set(templates.value.map((item) => item.module))).map((module) => ({
      label: templates.value.find((item) => item.module === module)?.moduleName || module,
      value: module,
    })),
  ]);
  const templateOptions = computed(() =>
    templates.value.map((item) => ({
      label: `${item.name}（${item.event}）`,
      value: item.key,
    }))
  );
  const filteredTemplates = computed(() => {
    const search = keyword.value.trim().toLowerCase();
    return templates.value.filter((item) => {
      const matchKeyword =
        !search ||
        [item.code, item.name, item.event, item.moduleName, item.channelNames.join(',')]
          .join(' ')
          .toLowerCase()
          .includes(search);
      const matchEvent = eventFilter.value === 'ALL' || item.module === eventFilter.value;
      return matchKeyword && matchEvent;
    });
  });

  const form = reactive({
    key: '',
    module: '',
    event: '',
    name: '',
    sysEnable: true,
    emailEnable: true,
    title: '',
    body: '',
    variables: [] as string[],
  });

  const rules: FormRules = {
    name: [{ required: true, message: '请输入模板名称', trigger: ['input', 'blur'] }],
    key: [{ required: true, message: '请选择触发事件', trigger: ['change'] }],
    title: [{ required: true, message: '请输入标题', trigger: ['input', 'blur'] }],
    body: [{ required: true, message: '请输入正文', trigger: ['input', 'blur'] }],
  };

  function getChannelNames(row: MessageTaskDetailDTOItem) {
    const channels: string[] = [];
    if (row.sysEnable) channels.push('系统');
    if (row.emailEnable) channels.push('邮件');
    if (row.weComEnable) channels.push('企微');
    if (row.dingTalkEnable) channels.push('钉钉');
    if (row.larkEnable) channels.push('飞书');
    return channels;
  }

  function getVariables(row: { event: string }) {
    if (row.event.includes('QUOTATION')) {
      return ['{{quote.id}}', '{{quote.title}}', '{{customer.name}}', '{{amount}}', '{{assignee.name}}', '{{url}}'];
    }
    if (row.event.includes('CONTRACT')) {
      return [
        '{{contract.id}}',
        '{{contract.title}}',
        '{{customer.name}}',
        '{{amount}}',
        '{{assignee.name}}',
        '{{url}}',
      ];
    }
    if (row.event.includes('CLUE')) {
      return ['{{clue.id}}', '{{clue.title}}', '{{customer.name}}', '{{owner.name}}', '{{url}}'];
    }
    if (row.event.includes('OPPORTUNITY')) {
      return ['{{opportunity.id}}', '{{opportunity.title}}', '{{customer.name}}', '{{stage.name}}', '{{url}}'];
    }
    if (row.event.includes('APPROVAL')) {
      return ['{{approval.id}}', '{{resource.name}}', '{{submitter.name}}', '{{assignee.name}}', '{{url}}'];
    }
    return [
      '{{ticket.id}}',
      '{{ticket.title}}',
      '{{ticket.priority}}',
      '{{customer.name}}',
      '{{sla.remaining}}',
      '{{assignee.name}}',
      '{{url}}',
    ];
  }

  function getTriggerRule(event: string, name: string) {
    if (event.includes('EXPIRING')) return `${name}时间进入提醒阈值`;
    if (event.includes('EXPIRED')) return `${name}已超过截止时间`;
    if (event.includes('APPROVAL')) return `${name}产生新的审批动作`;
    if (event.includes('TRANSFER')) return `${name}负责人发生变更`;
    return `${name}事件触发后立即发送`;
  }

  function buildTemplateRow(
    module: MessageConfigItem,
    detail: MessageTaskDetailDTOItem,
    index: number
  ): NotificationTemplateRow {
    const name = detail.eventName || detail.event;
    const variables = getVariables(detail);
    const title = `${name}提醒`;
    const systemBody =
      detail.template ||
      `${name}即将触发\n单据：${variables[0]} · ${variables[1]}\n客户：{{customer.name}}\n处理人：{{assignee.name}}`;
    return {
      ...module,
      ...detail,
      key: `${module.module}:${detail.event}`,
      code: `N-${String(index + 1).padStart(3, '0')}`,
      name,
      moduleName: module.moduleName || module.module,
      eventName: name,
      channelNames: getChannelNames(detail),
      monthlyCount: Math.max(22, 1840 - index * 156),
      triggerRule: getTriggerRule(detail.event, name),
      systemTitle: title,
      systemBody,
      systemLines: systemBody.split('\n'),
      emailSubject: title,
      emailBody: systemBody,
      emailBodyLines: systemBody.split('\n').filter(Boolean),
      variables,
    };
  }

  function fillForm(row: NotificationTemplateRow) {
    form.key = row.key;
    form.module = row.module;
    form.event = row.event;
    form.name = row.name;
    form.sysEnable = row.sysEnable;
    form.emailEnable = row.emailEnable;
    form.title = row.systemTitle;
    form.body = row.systemBody;
    form.variables = row.variables;
  }

  function startEdit(row: NotificationTemplateRow) {
    fillForm(row);
    mode.value = 'edit';
    if (route.query.mode !== 'create') {
      router.replace({ path: route.path, query: { ...route.query, mode: 'create' } });
    }
  }

  function rowProps(row: NotificationTemplateRow) {
    return {
      class: row.key === activeTemplate.value?.key ? 'message-template-row is-active' : 'message-template-row',
      onClick: () => {
        activeKey.value = row.key;
      },
      onDblclick: () => {
        startEdit(row);
      },
    };
  }

  async function initMessageList() {
    try {
      loading.value = true;
      const result = await getMessageTask();
      templates.value = result
        .flatMap((item) => item.messageTaskDetailDTOList.map((child) => ({ module: item, detail: child })))
        .map(({ module, detail }, index) => buildTemplateRow(module, detail, index));
      activeKey.value = templates.value[0]?.key || '';
      if (route.query.mode === 'create' && activeTemplate.value) {
        fillForm(activeTemplate.value);
        mode.value = 'edit';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      loading.value = false;
    }
  }

  function handleCreate() {
    if (activeTemplate.value) {
      startEdit(activeTemplate.value);
    }
  }

  function handleTemplateSelect(key: string) {
    const row = templates.value.find((item) => item.key === key);
    if (row) fillForm(row);
  }

  function handleCancelEdit() {
    mode.value = 'list';
    if (route.query.mode) {
      const { mode: _mode, ...query } = route.query;
      router.replace({ path: route.path, query });
    }
  }

  function buildSaveParams(): SaveMessageConfigParams {
    return {
      module: form.module,
      event: form.event,
      template: form.body,
      sysEnable: form.sysEnable,
      emailEnable: form.emailEnable,
      config: {
        timeList: [],
        userIds: ['OWNER'],
        roleIds: [],
        ownerEnable: true,
        ownerLevel: 1,
        roleEnable: false,
        userIdNames: [],
        roleIdNames: [],
      },
    };
  }

  async function handleSubmit() {
    await formRef.value?.validate();
    if (!form.sysEnable && !form.emailEnable) {
      message.warning('请至少选择一个适用渠道');
      return;
    }
    try {
      saving.value = true;
      await saveMessageTask(buildSaveParams());
      message.success('保存成功');
      handleCancelEdit();
      await initMessageList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      saving.value = false;
    }
  }

  async function toggleTemplate(row: NotificationTemplateRow) {
    try {
      saving.value = true;
      const nextEnable = !(row.sysEnable || row.emailEnable);
      await saveMessageTask({
        module: row.module,
        event: row.event,
        sysEnable: nextEnable,
        emailEnable: nextEnable && row.emailEnable,
        weComEnable: row.weComEnable,
        dingTalkEnable: row.dingTalkEnable,
        larkEnable: row.larkEnable,
      });
      await initMessageList();
      message.success('保存成功');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    } finally {
      saving.value = false;
    }
  }

  function highlightVariables(line: string) {
    return line.replace(/({{[^}]+}})/g, '<strong class="message-variable-highlight">$1</strong>');
  }

  function insertVariable(variable: string) {
    form.body = `${form.body}${form.body ? ' ' : ''}${variable}`;
  }

  const columns: DataTableColumn<NotificationTemplateRow>[] = [
    { title: '编码', key: 'code', width: 90 },
    { title: '名称', key: 'name', width: 160, ellipsis: { tooltip: true } },
    {
      title: '事件',
      key: 'event',
      width: 190,
      render: (row) => h('span', { class: 'message-event-text' }, row.event),
    },
    {
      title: '渠道',
      key: 'channels',
      width: 150,
      render: (row) =>
        h(
          'div',
          { class: 'message-channel-tags' },
          row.channelNames.map((name) =>
            h('span', { class: name === '邮件' ? 'message-channel-tag is-email' : 'message-channel-tag' }, name)
          )
        ),
    },
    { title: '月发', key: 'monthlyCount', width: 80 },
    {
      title: '状态',
      key: 'status',
      width: 90,
      render: (row) =>
        h(NSwitch, {
          value: row.sysEnable || row.emailEnable,
          disabled: saving.value,
          onUpdateValue: () => toggleTemplate(row),
        }),
    },
  ];

  onBeforeMount(() => {
    initMessageList();
  });

  watch(
    () => route.query.mode,
    (nextMode) => {
      if (nextMode !== 'create' && mode.value === 'edit') {
        mode.value = 'list';
      }
      if (nextMode === 'create' && mode.value === 'list' && activeTemplate.value) {
        fillForm(activeTemplate.value);
        mode.value = 'edit';
      }
    }
  );
</script>

<style lang="less" scoped>
  .message-settings-page {
    display: grid;
    min-height: 0;
    grid-template-columns: minmax(0, 1fr) minmax(320px, 400px);
    gap: 16px;
  }
  .message-edit-title-block {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 6px;
  }
  .message-edit-title-block h1 {
    margin: 0;
    font-size: 22px;
    font-weight: 800;
    color: #0f172a;
    line-height: 30px;
  }
  .message-edit-title-block div {
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
    line-height: 20px;
  }
  .message-template-card,
  .message-preview-card,
  .message-edit-card {
    border: 1px solid #dbe5f1;
    border-radius: 8px;
    background: #ffffff;
  }
  .message-template-card {
    overflow: hidden;
    padding: 20px;
    min-width: 0;
  }
  .message-template-toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    gap: 12px;
  }
  .message-template-search {
    width: 280px;
  }
  .message-template-filter {
    width: 140px;
  }
  .message-template-toolbar :deep(.n-button) {
    margin-left: auto;
  }
  .message-template-table {
    overflow: hidden;
    border: 1px solid #dbe5f1;
    border-radius: 6px;
  }
  :deep(.message-template-row) {
    cursor: pointer;
  }
  :deep(.message-template-row.is-active td) {
    background: #eef2ff !important;
  }
  :deep(.n-data-table-th) {
    background: #f8fafc;
  }
  :deep(.message-event-text) {
    font-weight: 600;
    color: #5b5cf6;
  }
  :deep(.message-channel-tags) {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  :deep(.message-channel-tag) {
    padding: 2px 8px;
    font-size: 12px;
    font-weight: 700;
    border-radius: 5px;
    color: #4f46e5;
    background: #eef2ff;
    line-height: 18px;
  }
  :deep(.message-channel-tag.is-email) {
    color: #d97706;
    background: #fff3df;
  }
  .message-preview-card {
    align-self: start;
    padding: 22px;
    min-width: 0;
  }
  .message-preview-header h2,
  .message-edit-card h2 {
    margin: 0 0 4px;
    font-size: 18px;
    font-weight: 800;
    color: #0f172a;
    line-height: 24px;
  }
  .message-preview-header div {
    font-size: 13px;
    color: #64748b;
  }
  .message-preview-section {
    margin-top: 22px;
  }
  .message-preview-label {
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 700;
    color: #64748b;
  }
  .message-trigger-box,
  .message-email-preview {
    padding: 13px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 5px;
    color: #1f2937;
    background: #ffffff;
  }
  .message-system-card {
    padding: 16px;
    border: 1px solid #86efac;
    border-radius: 7px;
    color: #12251d;
    background: #f0fdf4;
  }
  .message-system-card p,
  .message-email-preview p {
    margin: 8px 0 0;
    line-height: 22px;
  }
  .message-system-actions {
    display: flex;
    margin-top: 14px;
    gap: 8px;
  }
  .message-variable-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  .message-variable-list span,
  .message-variable-list button {
    padding: 3px 8px;
    font-size: 12px;
    font-weight: 700;
    border: 0;
    border-radius: 5px;
    color: #64748b;
    background: #f1f5f9;
  }
  .message-edit-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .message-edit-card {
    padding: 22px;
  }
  .message-edit-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
  .message-channel-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    width: 100%;
    gap: 10px;
  }
  .message-channel-grid :deep(.n-checkbox) {
    padding: 10px 12px;
    border: 1px solid #dbe5f1;
    border-radius: 6px;
  }
  :deep(.message-variable-highlight) {
    color: #dc2626;
  }
  @media (max-width: 1120px) {
    .message-settings-page {
      grid-template-columns: minmax(0, 1fr);
    }
  }
</style>

<template>
  <div class="sr-rich-editor" :class="{ 'sr-rich-editor--focused': focused }">
    <div class="sr-rich-editor__toolbar">
      <button
        v-for="tool in normalizedTools"
        :key="tool.key"
        type="button"
        :title="tool.title"
        @mousedown.prevent
        @click="handleTool(tool.key)"
      >
        {{ tool.label }}
      </button>
      <input
        ref="fileInputRef"
        type="file"
        multiple
        class="sr-rich-editor__file-input"
        @change="handleAttachmentChange"
      />
    </div>
    <div
      ref="editorRef"
      class="sr-rich-editor__content"
      :class="contentClass"
      contenteditable="true"
      :data-placeholder="placeholder"
      @blur="focused = false"
      @focus="focused = true"
      @input="handleInput"
    ></div>
    <div v-if="attachmentFiles.length" class="sr-rich-editor__attachments">
      <span v-for="file in attachmentFiles" :key="file.id" class="sr-rich-editor__attachment">
        {{ file.name }}
        <button type="button" title="移除附件" @click="removeAttachment(file.id)">×</button>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { uploadTempAttachment } from '@/api/modules';

  interface AttachmentFile {
    id: string;
    name: string;
    size?: number;
  }

  const TOOL_MAP: Record<string, { key: string; label: string; title: string }> = {
    'B': { key: 'bold', label: 'B', title: '加粗' },
    'I': { key: 'italic', label: 'I', title: '斜体' },
    'H1': { key: 'h1', label: 'H1', title: '一级标题' },
    'H2': { key: 'h2', label: 'H2', title: '二级标题' },
    '—': { key: 'hr', label: '—', title: '分割线' },
    '• 列表': { key: 'ul', label: '• 列表', title: '无序列表' },
    '1. 列表': { key: 'ol', label: '1. 列表', title: '有序列表' },
    '链接': { key: 'link', label: '链接', title: '插入链接' },
    '{ } 代码': { key: 'code', label: '{ } 代码', title: '代码块' },
    '图': { key: 'image', label: '图', title: '插入图片' },
    '附件': { key: 'attachment', label: '附件', title: '上传附件' },
    '表格': { key: 'table', label: '表格', title: '插入表格' },
  };

  const props = withDefaults(
    defineProps<{
      modelValue: string;
      attachmentFiles?: AttachmentFile[];
      placeholder?: string;
      minHeight?: 'small' | 'large';
      tools?: string[];
    }>(),
    {
      minHeight: 'small',
      placeholder: '',
      tools: () => ['B', 'I', 'H1', 'H2', '—', '• 列表', '1. 列表', '链接', '{ } 代码', '图', '附件', '表格'],
    }
  );

  const emit = defineEmits<{
    'update:modelValue': [value: string];
    'update:attachmentFiles': [value: AttachmentFile[]];
  }>();

  const editorRef = ref<HTMLElement | null>(null);
  const fileInputRef = ref<HTMLInputElement | null>(null);
  const focused = ref(false);
  const attachmentFiles = ref<AttachmentFile[]>([]);

  const contentClass = computed(() => `sr-rich-editor__content--${props.minHeight}`);
  const normalizedTools = computed(() =>
    props.tools.map((tool) => TOOL_MAP[tool] || { key: tool, label: tool, title: tool })
  );

  function syncEditorValue(value: string) {
    if (!editorRef.value || editorRef.value.innerHTML === value) return;
    editorRef.value.innerHTML = value || '';
  }

  function handleInput(event: Event) {
    emit('update:modelValue', (event.target as HTMLElement).innerHTML);
  }

  function focusEditor() {
    editorRef.value?.focus();
  }

  function runCommand(command: string, value?: string) {
    focusEditor();
    document.execCommand(command, false, value);
    if (editorRef.value) {
      emit('update:modelValue', editorRef.value.innerHTML);
    }
  }

  function insertHtml(html: string) {
    runCommand('insertHTML', html);
  }

  function handleTool(key: string) {
    if (key === 'bold') runCommand('bold');
    else if (key === 'italic') runCommand('italic');
    else if (key === 'h1') runCommand('formatBlock', '<h1>');
    else if (key === 'h2') runCommand('formatBlock', '<h2>');
    else if (key === 'hr') insertHtml('<hr>');
    else if (key === 'ul') runCommand('insertUnorderedList');
    else if (key === 'ol') runCommand('insertOrderedList');
    else if (key === 'code') insertHtml('<pre><code>请输入代码</code></pre>');
    else if (key === 'table')
      insertHtml('<table><tbody><tr><td>字段</td><td>说明</td></tr><tr><td></td><td></td></tr></tbody></table>');
    else if (key === 'link') {
      // eslint-disable-next-line no-alert
      const url = window.prompt('请输入链接地址');
      if (url) runCommand('createLink', url);
    } else if (key === 'image') {
      // eslint-disable-next-line no-alert
      const url = window.prompt('请输入图片地址');
      if (url) insertHtml(`<img src="${url}" alt="" />`);
    } else if (key === 'attachment') {
      fileInputRef.value?.click();
    }
  }

  async function handleAttachmentChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files || []);
    if (!files.length) return;
    try {
      const uploadedFiles = await Promise.all(
        files.map(async (file) => {
          const res = await uploadTempAttachment(file);
          const ids = Array.isArray(res.data) ? res.data : [];
          return ids.map((id: string) => ({ id, name: file.name, size: file.size }));
        })
      );
      const nextFiles = [...attachmentFiles.value, ...uploadedFiles.flat()];
      attachmentFiles.value = nextFiles;
      emit('update:attachmentFiles', nextFiles);
    } finally {
      input.value = '';
    }
  }

  function removeAttachment(id: string) {
    attachmentFiles.value = attachmentFiles.value.filter((file) => file.id !== id);
    emit('update:attachmentFiles', attachmentFiles.value);
  }

  onMounted(() => {
    syncEditorValue(props.modelValue);
    attachmentFiles.value = [...(props.attachmentFiles || [])];
  });

  watch(
    () => props.modelValue,
    (value) => {
      if (focused.value) return;
      syncEditorValue(value);
    }
  );

  watch(
    () => props.attachmentFiles,
    (value) => {
      attachmentFiles.value = [...(value || [])];
    },
    { deep: true }
  );
</script>

<style lang="less" scoped>
  .sr-rich-editor {
    position: relative;
    border: 1px solid #dbe2ea;
    border-radius: 6px;
    background: #ffffff;
    transition: border-color 0.16s ease, box-shadow 0.16s ease;
  }
  .sr-rich-editor--focused {
    border-color: #4f46e5;
    box-shadow: 0 0 0 3px rgb(79 70 229 / 12%);
  }
  .sr-rich-editor__toolbar {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 1;
    display: flex;
    justify-content: flex-end;
    max-width: calc(100% - 20px);
    flex-wrap: wrap;
    gap: 4px;
  }
  .sr-rich-editor__toolbar button {
    padding: 0 7px;
    height: 22px;
    font-size: 11px;
    font-weight: 700;
    border: 0;
    border-radius: 4px;
    color: #64748b;
    background: rgb(100 116 139 / 8%);
    cursor: pointer;
  }
  .sr-rich-editor__toolbar button:hover {
    color: #4f46e5;
    background: #eef2ff;
  }
  .sr-rich-editor__file-input {
    display: none;
  }
  .sr-rich-editor__content {
    overflow: auto;
    padding: 44px 10px 10px;
    min-height: 120px;
    font-size: 12px;
    white-space: pre-wrap;
    color: #334155;
    outline: none;
    line-height: 19px;
    word-break: break-word;
  }
  .sr-rich-editor__content :deep(h1),
  .sr-rich-editor__content :deep(h2),
  .sr-rich-editor__content :deep(p) {
    margin: 0 0 8px;
  }
  .sr-rich-editor__content :deep(h1) {
    font-size: 18px;
    line-height: 24px;
  }
  .sr-rich-editor__content :deep(h2) {
    font-size: 15px;
    line-height: 22px;
  }
  .sr-rich-editor__content :deep(pre) {
    margin: 8px 0;
    padding: 8px;
    border-radius: 4px;
    white-space: pre-wrap;
    background: #f8fafc;
  }
  .sr-rich-editor__content :deep(table) {
    margin: 8px 0;
    width: 100%;
    border-collapse: collapse;
  }
  .sr-rich-editor__content :deep(td) {
    padding: 6px;
    border: 1px solid #dbe2ea;
  }
  .sr-rich-editor__content :deep(img) {
    max-width: 100%;
    border-radius: 4px;
  }
  .sr-rich-editor__content:empty::before {
    color: #94a3b8;
    content: attr(data-placeholder);
  }
  .sr-rich-editor__content--large {
    min-height: 200px;
  }
  .sr-rich-editor__attachments {
    display: flex;
    padding: 8px 10px 10px;
    border-top: 1px solid #eef2f7;
    flex-wrap: wrap;
    gap: 6px;
  }
  .sr-rich-editor__attachment {
    display: inline-flex;
    align-items: center;
    padding: 0 6px;
    height: 22px;
    font-size: 11px;
    border-radius: 4px;
    color: #64748b;
    background: #f1f5f9;
    gap: 4px;
  }
  .sr-rich-editor__attachment button {
    padding: 0;
    border: 0;
    color: #94a3b8;
    background: transparent;
    cursor: pointer;
  }
</style>

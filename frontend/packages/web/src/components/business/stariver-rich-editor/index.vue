<template>
  <div class="sr-rich-editor" :class="{ 'sr-rich-editor--focused': focused }">
    <div class="sr-rich-editor__toolbar">
      <button v-for="tool in tools" :key="tool" type="button">{{ tool }}</button>
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
  </div>
</template>

<script setup lang="ts">
  const props = withDefaults(
    defineProps<{
      modelValue: string;
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
  }>();

  const editorRef = ref<HTMLElement | null>(null);
  const focused = ref(false);

  const contentClass = computed(() => `sr-rich-editor__content--${props.minHeight}`);

  function syncEditorValue(value: string) {
    if (!editorRef.value || editorRef.value.innerText === value) return;
    editorRef.value.innerText = value;
  }

  function handleInput(event: Event) {
    emit('update:modelValue', (event.target as HTMLElement).innerText);
  }

  onMounted(() => {
    syncEditorValue(props.modelValue);
  });

  watch(
    () => props.modelValue,
    (value) => {
      if (focused.value) return;
      syncEditorValue(value);
    }
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
  .sr-rich-editor__content {
    overflow: auto;
    padding: 44px 10px 10px;
    min-height: 120px;
    font-size: 12px;
    white-space: pre-wrap;
    color: #334155;
    outline: none;
    line-height: 19px;
  }
  .sr-rich-editor__content:empty::before {
    color: #94a3b8;
    content: attr(data-placeholder);
  }
  .sr-rich-editor__content--large {
    min-height: 200px;
  }
</style>

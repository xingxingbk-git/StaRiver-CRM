<template>
  <section class="stariver-module-page">
    <header class="stariver-module-page__header">
      <div class="stariver-module-page__title-wrap">
        <div v-if="eyebrow" class="stariver-module-page__eyebrow">{{ eyebrow }}</div>
        <div class="stariver-module-page__title-row">
          <h1>{{ title }}</h1>
          <span v-if="countLabel" class="stariver-module-page__count">{{ countLabel }}</span>
        </div>
        <div v-if="$slots.subtitle" class="stariver-module-page__subtitle-row">
          <slot name="subtitle"></slot>
        </div>
      </div>
      <div v-if="$slots.actions" class="stariver-module-page__actions">
        <slot name="actions"></slot>
      </div>
    </header>

    <div class="stariver-module-page__body">
      <div v-if="$slots.toolbar" class="stariver-module-page__toolbar">
        <slot name="toolbar"></slot>
      </div>
      <div class="stariver-module-page__content">
        <slot></slot>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
  defineProps<{
    title: string;
    countLabel?: string;
    eyebrow?: string;
  }>();
</script>

<style lang="less" scoped>
  .stariver-module-page {
    display: flex;
    overflow: hidden;
    height: 100%;
    min-height: 0;
    color: #0f172a;
    background: #f8fafc;
    flex-direction: column;
  }
  .stariver-module-page__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    min-height: 64px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
    flex-shrink: 0;
  }
  .stariver-module-page__title-wrap {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
  }
  .stariver-module-page__eyebrow {
    font-size: 11px;
    font-weight: 600;
    color: #94a3b8;
    line-height: 14px;
  }
  .stariver-module-page__title-row {
    display: flex;
    align-items: center;
    min-width: 0;
    gap: 8px;
  }
  .stariver-module-page__title-row h1 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
    line-height: 24px;
  }
  .stariver-module-page__count {
    display: inline-flex;
    align-items: center;
    padding: 0 7px;
    height: 18px;
    font-size: 11px;
    font-weight: 600;
    border-radius: 999px;
    color: #64748b;
    background: #eef2f7;
  }
  .stariver-module-page__actions {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    gap: 8px;
  }
  .stariver-module-page__subtitle-row {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #64748b;
    gap: 10px;
    line-height: 18px;
  }
  .stariver-module-page__body {
    display: flex;
    overflow: hidden;
    min-height: 0;
    flex: 1;
    flex-direction: column;
  }
  .stariver-module-page__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    min-height: 44px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
    flex-shrink: 0;
  }
  .stariver-module-page__content {
    overflow: auto;
    padding: 16px 24px 24px;
    min-height: 0;
    flex: 1;
  }
</style>

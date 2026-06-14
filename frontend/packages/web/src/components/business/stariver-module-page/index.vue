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
    height: 100%;
    min-height: 0;
    flex-direction: column;
    overflow: hidden;
    background: #f8fafc;
    color: #0f172a;
  }

  .stariver-module-page__header {
    display: flex;
    min-height: 64px;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
  }

  .stariver-module-page__title-wrap {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
  }

  .stariver-module-page__eyebrow {
    color: #94a3b8;
    font-size: 11px;
    font-weight: 600;
    line-height: 14px;
  }

  .stariver-module-page__title-row {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 8px;
  }

  .stariver-module-page__title-row h1 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
    font-weight: 700;
    line-height: 24px;
  }

  .stariver-module-page__count {
    display: inline-flex;
    height: 18px;
    align-items: center;
    border-radius: 999px;
    padding: 0 7px;
    background: #eef2f7;
    color: #64748b;
    font-size: 11px;
    font-weight: 600;
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
    gap: 10px;
  }

  .stariver-module-page__body {
    display: flex;
    min-height: 0;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
  }

  .stariver-module-page__toolbar {
    display: flex;
    min-height: 44px;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
  }

  .stariver-module-page__content {
    min-height: 0;
    flex: 1;
    overflow: auto;
    padding: 16px 24px 24px;
  }
</style>

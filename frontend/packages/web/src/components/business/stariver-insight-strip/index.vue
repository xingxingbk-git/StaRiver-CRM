<template>
  <div class="stariver-insight-strip">
    <section v-if="metrics?.length" class="stariver-insight-strip__metrics">
      <article
        v-for="item in metrics"
        :key="item.label"
        :class="['stariver-insight-strip__metric', `stariver-insight-strip__metric--${item.tone || 'blue'}`]"
      >
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small v-if="item.hint">{{ item.hint }}</small>
      </article>
    </section>

    <section v-if="process?.length || fields?.length || rules?.length" class="stariver-insight-strip__panels">
      <article v-if="process?.length" class="stariver-insight-strip__panel stariver-insight-strip__panel--flow">
        <header>业务流程</header>
        <div class="stariver-insight-strip__flow">
          <span v-for="item in process" :key="item">{{ item }}</span>
        </div>
      </article>

      <article v-if="fields?.length" class="stariver-insight-strip__panel">
        <header>关键字段</header>
        <div class="stariver-insight-strip__tags">
          <span v-for="item in fields" :key="item">{{ item }}</span>
        </div>
      </article>

      <article v-if="rules?.length" class="stariver-insight-strip__panel">
        <header>业务规则</header>
        <ul>
          <li v-for="item in rules" :key="item">{{ item }}</li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
  interface StariverMetric {
    label: string;
    value: string;
    hint?: string;
    tone?: string;
  }

  defineProps<{
    metrics?: StariverMetric[];
    process?: string[];
    fields?: string[];
    rules?: string[];
  }>();
</script>

<style lang="less" scoped>
  .stariver-insight-strip {
    display: flex;
    flex-shrink: 0;
    flex-direction: column;
    gap: 12px;
  }

  .stariver-insight-strip__metrics {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 12px;
  }

  .stariver-insight-strip__metric,
  .stariver-insight-strip__panel {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #ffffff;
  }

  .stariver-insight-strip__metric {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
    padding: 12px 14px;
    box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  }

  .stariver-insight-strip__metric span {
    color: #64748b;
    font-size: 12px;
    line-height: 16px;
  }

  .stariver-insight-strip__metric strong {
    color: #0f172a;
    font-size: 22px;
    font-weight: 700;
    line-height: 28px;
  }

  .stariver-insight-strip__metric small {
    color: #94a3b8;
    font-size: 11px;
    line-height: 15px;
  }

  .stariver-insight-strip__metric--blue {
    border-left: 3px solid #2563eb;
  }

  .stariver-insight-strip__metric--indigo {
    border-left: 3px solid #4f46e5;
  }

  .stariver-insight-strip__metric--emerald {
    border-left: 3px solid #059669;
  }

  .stariver-insight-strip__metric--amber {
    border-left: 3px solid #d97706;
  }

  .stariver-insight-strip__metric--rose {
    border-left: 3px solid #e11d48;
  }

  .stariver-insight-strip__metric--cyan {
    border-left: 3px solid #0891b2;
  }

  .stariver-insight-strip__metric--slate {
    border-left: 3px solid #64748b;
  }

  .stariver-insight-strip__panels {
    display: grid;
    grid-template-columns: minmax(260px, 1.2fr) minmax(240px, 1fr) minmax(280px, 1.2fr);
    gap: 12px;
  }

  .stariver-insight-strip__panel {
    min-width: 0;
    padding: 12px 14px;
  }

  .stariver-insight-strip__panel header {
    margin-bottom: 10px;
    color: #0f172a;
    font-size: 13px;
    font-weight: 700;
    line-height: 18px;
  }

  .stariver-insight-strip__flow {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .stariver-insight-strip__flow span {
    position: relative;
    display: inline-flex;
    height: 26px;
    align-items: center;
    border: 1px solid #dbe3ef;
    border-radius: 6px;
    padding: 0 10px;
    background: #f8fafc;
    color: #334155;
    font-size: 12px;
    line-height: 24px;
  }

  .stariver-insight-strip__flow span:not(:last-child)::after {
    position: absolute;
    right: -9px;
    color: #94a3b8;
    content: '>';
  }

  .stariver-insight-strip__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .stariver-insight-strip__tags span {
    display: inline-flex;
    height: 24px;
    align-items: center;
    border-radius: 4px;
    padding: 0 8px;
    background: #eef2ff;
    color: #4f46e5;
    font-size: 12px;
    font-weight: 600;
  }

  .stariver-insight-strip__panel ul {
    display: grid;
    gap: 7px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .stariver-insight-strip__panel li {
    position: relative;
    padding-left: 12px;
    color: #475569;
    font-size: 12px;
    line-height: 18px;
  }

  .stariver-insight-strip__panel li::before {
    position: absolute;
    top: 8px;
    left: 0;
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: #4f46e5;
    content: '';
  }

  @media (max-width: 1200px) {
    .stariver-insight-strip__metrics {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .stariver-insight-strip__panels {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 768px) {
    .stariver-insight-strip__metrics {
      grid-template-columns: 1fr;
    }
  }
</style>

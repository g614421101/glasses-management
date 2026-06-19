<template>
  <div class="mobile-card-list">
    <template v-if="data.length">
      <div v-for="(row, index) in data" :key="row.id ?? index" class="mobile-card">
        <div class="mobile-card-header">
          <span class="mobile-card-title">{{ row[titleField] }}</span>
          <span v-if="badgeField && row[badgeField] != null" class="mobile-card-badge">
            {{ row[badgeField] }}
          </span>
        </div>
        <div v-if="$slots.default" class="mobile-card-body">
          <slot :row="row" />
        </div>
        <div v-if="$slots.actions" class="mobile-card-actions">
          <slot name="actions" :row="row" />
        </div>
      </div>
    </template>
    <div v-else class="mobile-card-empty">
      <slot name="empty">
        <p>{{ emptyText }}</p>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  data: any[]
  titleField: string
  badgeField?: string
  emptyText?: string
}>(), {
  emptyText: '暂无数据'
})
</script>

<style scoped>
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 4px 0;
}

.mobile-card {
  background: var(--surface-overlay, #fff);
  border: 1px solid var(--border-color, #e2e8f0);
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  opacity: 0;
  animation: mobile-card-in var(--duration-slow, 0.45s) var(--ease-emphasized, cubic-bezier(0.22, 1, 0.36, 1)) forwards;
  transition: transform var(--duration-fast, 0.18s) var(--ease-standard, cubic-bezier(0.4, 0, 0.2, 1)),
    box-shadow var(--duration-fast, 0.18s) var(--ease-standard, cubic-bezier(0.4, 0, 0.2, 1));
}

.mobile-card:nth-child(1) { animation-delay: 40ms; }
.mobile-card:nth-child(2) { animation-delay: 90ms; }
.mobile-card:nth-child(3) { animation-delay: 140ms; }
.mobile-card:nth-child(4) { animation-delay: 190ms; }
.mobile-card:nth-child(5) { animation-delay: 240ms; }
.mobile-card:nth-child(6) { animation-delay: 290ms; }
.mobile-card:nth-child(n+7) { animation-delay: 340ms; }

.mobile-card:active {
  transform: scale(0.99);
}

@keyframes mobile-card-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.mobile-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.mobile-card-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary, #1e293b);
}

.mobile-card-badge {
  font-size: 12px;
  color: var(--text-muted, #64748b);
  background: var(--bg-color, #f1f5f9);
  padding: 2px 8px;
  border-radius: 999px;
}

.mobile-card-body {
  font-size: 13px;
  color: var(--text-muted, #64748b);
  margin-bottom: 10px;
  line-height: 1.6;
}

.mobile-card-actions {
  display: flex;
  gap: 8px;
}

.mobile-card-actions :deep(.el-button) {
  flex: 1;
}

.mobile-card-empty {
  padding: 32px 16px;
  text-align: center;
}

.mobile-card-empty p {
  color: var(--text-muted, #64748b);
  font-size: 14px;
  margin: 0;
}
</style>

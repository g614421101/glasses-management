<template>
  <div class="mobile-card-list">
    <div v-if="!data || data.length === 0" class="mobile-card-empty">
      <slot name="empty">
        <p>{{ emptyText }}</p>
      </slot>
    </div>

    <div v-for="(row, index) in data" :key="row.id ?? index" class="mobile-card">
      <div class="mobile-card-header">
        <span class="mobile-card-title">{{ row[titleField] }}</span>
        <el-tag v-if="badgeField && row[badgeField] != null" class="mobile-card-badge" size="small" round>
          {{ row[badgeField] }}
        </el-tag>
      </div>

      <div class="mobile-card-body">
        <slot :row="row" :index="index" />
      </div>

      <div v-if="$slots.actions" class="mobile-card-actions">
        <slot name="actions" :row="row" :index="index" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  data: any[]
  titleField: string
  badgeField?: string
  emptyText?: string
}>()

defineSlots<{
  default(props: { row: any; index: number }): any
  actions(props: { row: any; index: number }): any
  empty(): any
}>()
</script>

<style scoped>
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mobile-card {
  background: var(--surface-strong);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
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
  color: var(--text-primary);
}

.mobile-card-badge {
  border-radius: 999px;
}

.mobile-card-body {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 12px;
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
  text-align: center;
  padding: 40px 16px;
  color: var(--text-muted);
  font-size: 14px;
}
</style>

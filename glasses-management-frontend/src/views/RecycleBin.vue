<template>
  <div class="page-shell recycle-page">
    <section class="page-hero glass-card recycle-hero">
      <div>
        <h1 class="page-heading">回收站</h1>
        <p class="page-title-en">RECYCLE BIN</p>
      </div>
      <div class="recycle-actions">
        <el-select v-model="activeType" class="type-select" @change="loadData">
          <el-option label="全部" value="all" />
          <el-option label="顾客" value="customer" />
          <el-option label="验光记录" value="optometry" />
          <el-option label="配镜记录" value="sales" />
        </el-select>
        <el-button class="action-pill" @click="loadData">刷新</el-button>
        <el-button type="danger" plain @click="purgeExpired">清理超过 30 天</el-button>
      </div>
    </section>

    <section v-if="showCustomers" class="glass-card recycle-card">
      <div class="section-head">
        <h3>已删除顾客</h3>
        <span>{{ customers.length }} 条</span>
      </div>
      <el-table :data="customers" v-loading="loading" row-key="id" class="recycle-table">
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="deletedTime" label="删除时间" min-width="180" />
        <el-table-column label="操作" min-width="220" align="center" class-name="actions-column">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button class="action-pill" size="small" @click="restore('customer', row.id)">恢复</el-button>
              <el-button class="action-pill action-pill--danger" size="small" @click="purge('customer', row.id)">彻底删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="showOptometry" class="glass-card recycle-card">
      <div class="section-head">
        <h3>已删除验光记录</h3>
        <span>{{ optometryRecords.length }} 条</span>
      </div>
      <el-table :data="optometryRecords" v-loading="loading" row-key="id" class="recycle-table">
        <el-table-column prop="customerId" label="顾客ID" width="100" />
        <el-table-column prop="optometristName" label="验光师" min-width="140" />
        <el-table-column prop="examDate" label="验光时间" min-width="180" />
        <el-table-column prop="deletedTime" label="删除时间" min-width="180" />
        <el-table-column label="操作" min-width="220" align="center" class-name="actions-column">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button class="action-pill" size="small" @click="restore('optometry', row.id)">恢复</el-button>
              <el-button class="action-pill action-pill--danger" size="small" @click="purge('optometry', row.id)">彻底删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section v-if="showSales" class="glass-card recycle-card">
      <div class="section-head">
        <h3>已删除配镜记录</h3>
        <span>{{ salesRecords.length }} 条</span>
      </div>
      <el-table :data="salesRecords" v-loading="loading" row-key="id" class="recycle-table">
        <el-table-column prop="recordNo" label="单号" min-width="180" />
        <el-table-column prop="customerId" label="顾客ID" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="120" />
        <el-table-column prop="deletedTime" label="删除时间" min-width="180" />
        <el-table-column label="操作" min-width="220" align="center" class-name="actions-column">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button class="action-pill" size="small" @click="restore('sales', row.id)">恢复</el-button>
              <el-button class="action-pill action-pill--danger" size="small" @click="purge('sales', row.id)">彻底删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../utils/request';

const activeType = ref('all');
const loading = ref(false);
const customers = ref<any[]>([]);
const optometryRecords = ref<any[]>([]);
const salesRecords = ref<any[]>([]);

const showCustomers = computed(() => activeType.value === 'all' || activeType.value === 'customer');
const showOptometry = computed(() => activeType.value === 'all' || activeType.value === 'optometry');
const showSales = computed(() => activeType.value === 'all' || activeType.value === 'sales');

const loadData = async () => {
  loading.value = true;
  try {
    const data: any = await request.get('/recycle-bin', { params: { type: activeType.value } });
    customers.value = data.customers || [];
    optometryRecords.value = data.optometryRecords || [];
    salesRecords.value = data.salesRecords || [];
  } finally {
    loading.value = false;
  }
};

const restore = async (type: string, id: number) => {
  await request.post(`/recycle-bin/restore/${type}/${id}`);
  ElMessage.success('恢复成功');
  loadData();
};

const purge = async (type: string, id: number) => {
  await ElMessageBox.confirm('彻底删除后无法恢复，确认继续吗？', '危险操作', {
    type: 'warning',
    confirmButtonText: '彻底删除',
    cancelButtonText: '取消'
  });
  await request.delete(`/recycle-bin/purge/${type}/${id}`);
  ElMessage.success('已彻底删除');
  loadData();
};

const purgeExpired = async () => {
  await ElMessageBox.confirm('将彻底清理删除超过 30 天的数据，确认继续吗？', '清理回收站', {
    type: 'warning',
    confirmButtonText: '开始清理',
    cancelButtonText: '取消'
  });
  await request.delete('/recycle-bin/purge-expired');
  ElMessage.success('过期数据清理完成');
  loadData();
};

onMounted(loadData);
</script>

<style scoped>
.recycle-page {
  padding-top: 6px;
}

.recycle-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.recycle-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.type-select {
  width: 150px;
}

.recycle-card {
  padding: 24px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-head h3 {
  margin: 0;
}

.section-head span {
  color: var(--text-muted);
  font-weight: 800;
}

.table-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
  padding: 4px 0;
  white-space: nowrap;
}

.table-actions > * {
  flex: 0 0 auto;
  position: relative;
}

.table-actions :deep(.action-pill:hover) {
  position: relative;
  z-index: 2;
}

.recycle-table :deep(.actions-column) {
  overflow: visible;
}

.recycle-table :deep(.actions-column .cell) {
  overflow: visible;
  padding-top: 8px;
  padding-bottom: 8px;
}

@media (max-width: 760px) {
  .recycle-hero {
    flex-direction: column;
    align-items: stretch;
  }

  .recycle-actions,
  .type-select {
    width: 100%;
  }
}
</style>

<template>
  <div class="page-shell operation-log-page">
    <section class="page-hero glass-card operation-hero">
      <div>
        <h1 class="page-heading">操作日志</h1>
        <p class="page-title-en">Operation Log</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="authStore.role === 'admin'" class="action-pill action-pill--danger" @click="handleCleanup">清理近期日志</el-button>
        <el-button class="action-pill" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </section>

    <section class="surface-panel table-card">
      <div class="table-toolbar">
        <div class="toolbar-copy">
          <h3>日志列表</h3>
        </div>
        <div class="log-filters">
          <el-input v-model="filters.operatorName" placeholder="操作人" clearable class="filter-item" @keyup.enter="handleQuery" />
          <el-select v-model="filters.action" placeholder="操作类型" clearable class="filter-item">
            <el-option label="新增" value="ADD" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="filter-item filter-item--date"
          />
          <el-button type="primary" class="search-submit" @click="handleQuery">查询</el-button>
          <el-button class="search-submit" @click="resetFilters">重置</el-button>
        </div>
      </div>

      <el-table v-if="!isMobile" :data="tableData" v-loading="loading" row-key="id" class="main-table">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="log-detail">
              <p><span class="detail-label">请求：</span><span class="log-uri">{{ row.method }} {{ row.uri }}</span></p>
              <p><span class="detail-label">参数：</span>{{ row.params || '-' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作内容" min-width="240">
          <template #default="scope">{{ scope.row.description || '操作记录' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作人" width="110">
          <template #default="scope">{{ scope.row.operatorName || 'anonymous' }}</template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="scope">
            <el-tag :type="actionTagType(scope.row.action)" size="small">{{ actionText(scope.row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结果" min-width="160">
          <template #default="scope">
            <el-tag :type="scope.row.status === 200 ? 'success' : 'danger'" size="small">{{ scope.row.status }}</el-tag>
            <span v-if="scope.row.message && scope.row.message !== 'success'" class="log-msg">{{ scope.row.message }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="scope">{{ scope.row.costMs }}ms</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="120" />
      </el-table>

      <MobileCardList
        v-else
        :data="tableData"
        title-field="description"
        badge-field="action"
        empty-text="暂无操作日志"
      >
        <template #default="{ row }">
          🕐 {{ row.createTime || '-' }} · 👤 {{ row.operatorName || 'anonymous' }}<br>
          📦 {{ row.module || '-' }}<br>
          🔗 {{ row.method }} {{ row.uri }}<br>
          📄 {{ row.params || '-' }}<br>
          ✅ {{ row.status }} <template v-if="row.message && row.message !== 'success'">{{ row.message }}</template>
          · ⏱ {{ row.costMs }}ms · 🌐 {{ row.ip || '-' }}
        </template>
      </MobileCardList>

      <div class="pagination-shell">
        <el-pagination
          v-model:current-page="pageParams.current"
          v-model:page-size="pageParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          prev-text="上一页"
          next-text="下一页"
          @size-change="handleQuery"
          @current-change="loadData"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../utils/request';
import MobileCardList from '../components/MobileCardList.vue';
import { useAuthStore } from '../store/auth';

const authStore = useAuthStore();

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const pageParams = reactive({ current: 1, size: 10 });
const filters = reactive({ operatorName: '', action: '', dateRange: [] as string[] });

const isMobile = ref(window.matchMedia('(max-width: 640px)').matches);
let mediaQuery: MediaQueryList | null = null;
let mediaHandler: ((e: MediaQueryListEvent) => void) | null = null;

const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      current: pageParams.current,
      size: pageParams.size,
    };
    if (filters.operatorName) params.operatorName = filters.operatorName.trim();
    if (filters.action) params.action = filters.action;
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startTime = filters.dateRange[0] + ' 00:00:00';
      params.endTime = filters.dateRange[1] + ' 23:59:59';
    }
    const data: any = await request.get('/operation-log/page', { params });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  pageParams.current = 1;
  loadData();
};

const resetFilters = () => {
  filters.operatorName = '';
  filters.action = '';
  filters.dateRange = [];
  handleQuery();
};

const actionText = (action: string) => {
  const map: Record<string, string> = { ADD: '新增', UPDATE: '修改', DELETE: '删除', OTHER: '其他' };
  return map[action] || action || '-';
};

const actionTagType = (action: string) => {
  const map: Record<string, any> = { ADD: 'primary', UPDATE: 'warning', DELETE: 'danger', OTHER: 'info' };
  return map[action] || 'info';
};

/** 手动清理近期操作日志（仅 admin，天数由后端配置决定，更早的历史记录保留） */
const handleCleanup = async () => {
  try {
    await ElMessageBox.confirm('将永久删除最近保留期内的操作日志，更早的历史记录将保留，此操作不可恢复，确认继续吗？', '清理操作日志', {
      type: 'warning',
      confirmButtonText: '确认清理',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  try {
    const data: any = await request.post('/operation-log/cleanup');
    await loadData();
    ElMessage.success(`已清理最近 ${data.days} 天内的 ${data.count} 条日志`);
  } catch (e: any) {
    ElMessage.error(e?.message || '清理失败');
  }
};

onMounted(() => {
  loadData();
  mediaQuery = window.matchMedia('(max-width: 640px)');
  mediaHandler = (e: MediaQueryListEvent) => {
    isMobile.value = e.matches;
  };
  mediaQuery.addEventListener('change', mediaHandler);
});

onUnmounted(() => {
  mediaQuery?.removeEventListener('change', mediaHandler);
});
</script>

<style scoped>
.operation-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: auto;
  position: relative;
  z-index: 1;
}

.log-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-item {
  width: 140px;
}

.filter-item--date {
  width: 260px;
}

.log-detail {
  padding: 4px 8px;
  font-size: 13px;
  line-height: 1.8;
}

.detail-label {
  color: var(--text-muted, #64748b);
  font-weight: 600;
}

.log-uri {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: var(--text-secondary, #475569);
  word-break: break-all;
}

.log-msg {
  margin-left: 6px;
  font-size: 12px;
  color: var(--text-muted, #64748b);
}
</style>

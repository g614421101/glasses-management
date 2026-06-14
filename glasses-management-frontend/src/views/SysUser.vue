<template>
  <div class="sys-user-page">
    <div class="page-header">
      <div class="header-content">
        <h2>账号管理中心</h2>
        <p>管理商户账号状态，支持封禁、恢复、逻辑删除和彻底删除。</p>
      </div>
      <div class="header-actions">
        <el-switch v-model="includeDeleted" active-text="显示已删除" @change="fetchUsers" />
        <el-button type="primary" @click="fetchUsers" :icon="Refresh">刷新列表</el-button>
      </div>
    </div>

    <el-card class="glass-card table-card" shadow="never">
      <el-table v-if="!isMobile" :data="users" style="width: 100%" v-loading="loading" class="sys-user-table">
        <el-table-column prop="id" label="账号ID" width="90" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column label="状态" width="160">
          <template #default="{ row }">
            <el-tag v-if="row.deleted" type="info">已删除</el-tag>
            <el-tag v-else-if="row.disabled" type="warning">已封禁</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="420" align="center" class-name="actions-column">
          <template #default="{ row }">
            <div class="table-actions">
              <template v-if="row.deleted">
                <el-button class="action-pill" size="small" @click="restoreUser(row)">恢复</el-button>
                <el-button class="action-pill action-pill--danger" size="small" @click="purgeUser(row)">彻底删除</el-button>
              </template>
              <template v-else>
                <el-button class="action-pill" size="small" @click="resetPwd(row)">重置密码</el-button>
                <el-button class="action-pill" size="small" @click="toggleDisabled(row)">
                  {{ row.disabled ? '解除封禁' : '封禁' }}
                </el-button>
                <el-button class="action-pill action-pill--danger" size="small" @click="deleteUser(row)">删除</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <MobileCardList
        v-else
        :data="users"
        title-field="username"
      >
        <template #default="{ row }">
          📱 {{ row.phone || '-' }} · 🕐 {{ row.createTime || '-' }}
          <span v-if="row.deleted" style="color: #94a3b8;"> · 已删除</span>
          <span v-else-if="row.disabled" style="color: #eab308;"> · 已封禁</span>
          <span v-else style="color: #22c55e;"> · 正常</span>
        </template>
        <template #actions="{ row }">
          <template v-if="row.deleted">
            <el-button class="action-pill" size="small" @click="restoreUser(row)">恢复</el-button>
            <el-button class="action-pill action-pill--danger" size="small" @click="purgeUser(row)">彻底删除</el-button>
          </template>
          <template v-else>
            <el-button class="action-pill" size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-button class="action-pill" size="small" @click="toggleDisabled(row)">
              {{ row.disabled ? '解除封禁' : '封禁' }}
            </el-button>
            <el-button class="action-pill action-pill--danger" size="small" @click="deleteUser(row)">删除</el-button>
          </template>
        </template>
      </MobileCardList>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Refresh } from '@element-plus/icons-vue';
import request from '../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import MobileCardList from '../components/MobileCardList.vue';

const users = ref<any[]>([]);
const loading = ref(false);
const includeDeleted = ref(false);
const isMobile = ref(window.matchMedia('(max-width: 640px)').matches);
let mediaQuery: MediaQueryList | null = null;
let mediaHandler: ((e: MediaQueryListEvent) => void) | null = null;

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/sys-user/list', { params: { includeDeleted: includeDeleted.value } });
    users.value = (res as any[]) || [];
  } finally {
    loading.value = false;
  }
};

const resetPwd = async (row: any) => {
  await ElMessageBox.confirm(`确定为 ${row.username} 生成一次性临时密码吗？`, '重置密码', { type: 'warning' });
  const temporaryPassword = await request.post(`/sys-user/reset-password/${row.id}`);
  await ElMessageBox.alert(String(temporaryPassword), '临时密码', { confirmButtonText: '确定' });
  ElMessage.success('密码已重置，用户下次登录必须修改。');
  fetchUsers();
};

const toggleDisabled = async (row: any) => {
  const action = row.disabled ? '解除封禁' : '封禁';
  await ElMessageBox.confirm(`确定${action}账号 ${row.username} 吗？`, action, { type: 'warning' });
  await request.post(`/sys-user/${row.disabled ? 'enable' : 'disable'}/${row.id}`);
  ElMessage.success(`${action}成功`);
  fetchUsers();
};

const deleteUser = async (row: any) => {
  await ElMessageBox.confirm(`删除后账号 ${row.username} 将进入回收状态，可恢复。确认删除吗？`, '删除账号', { type: 'warning' });
  await request.delete(`/sys-user/${row.id}`);
  ElMessage.success('账号已删除');
  fetchUsers();
};

const restoreUser = async (row: any) => {
  await request.post(`/sys-user/restore/${row.id}`);
  ElMessage.success('账号已恢复');
  fetchUsers();
};

const purgeUser = async (row: any) => {
  await ElMessageBox.confirm(`彻底删除账号 ${row.username} 后无法恢复，确认继续吗？`, '危险操作', {
    type: 'warning',
    confirmButtonText: '彻底删除',
    cancelButtonText: '取消'
  });
  await request.delete(`/sys-user/purge/${row.id}`);
  ElMessage.success('账号已彻底删除');
  fetchUsers();
};

onMounted(() => {
  fetchUsers();
  mediaQuery = window.matchMedia('(max-width: 640px)');
  mediaHandler = (e: MediaQueryListEvent) => { isMobile.value = e.matches; };
  mediaQuery.addEventListener('change', mediaHandler);
});

onUnmounted(() => {
  if (mediaQuery && mediaHandler) {
    mediaQuery.removeEventListener('change', mediaHandler);
  }
});
</script>

<style scoped>
.sys-user-page {
  /* 入场动画统一由路由过渡负责 */
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: var(--text-primary);
  font-size: 24px;
  letter-spacing: 0.5px;
}

.page-header p {
  margin: 0;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.table-card {
  border-radius: 24px;
  padding: 12px;
  background: var(--surface-overlay);
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

.sys-user-table :deep(.actions-column) {
  overflow: visible;
}

.sys-user-table :deep(.actions-column .cell) {
  overflow: visible;
  padding-top: 8px;
  padding-bottom: 8px;
}

@media (max-width: 640px) {
  .page-header,
  .header-actions {
    align-items: stretch;
  }

  .page-header > :last-child,
  .header-actions > * {
    width: 100%;
  }
}
</style>

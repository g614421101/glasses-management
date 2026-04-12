<template>
  <div class="sys-user-page">
    <div class="page-header">
      <div class="header-content">
        <h2>权限账号管理中心</h2>
        <p class="subtitle">仅超级管理员 (Admin) 授权可见，可在此管理全站商户账号并重置其密码。</p>
      </div>
      <el-button type="primary" @click="fetchUsers" :icon="Refresh">刷新列表</el-button>
    </div>

    <el-card class="glass-card table-card" shadow="never">
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="账号ID" width="100" />
        <el-table-column prop="username" label="注册手机号 / 账号名" width="200" />
        <el-table-column prop="realName" label="系统内定名称" />
        <el-table-column prop="createTime" label="加入系统时间" width="220" />
        <el-table-column prop="role" label="权限组" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'success'">
              {{ row.role === 'admin' ? '超管' : '商户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="高级操作" width="240" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              plain 
              @click="resetPwd(row)"
              :disabled="row.role === 'admin'">
              重置密码
            </el-button>
            <el-popconfirm
              title="确定要彻底注销并且删除该用户的系统所有登录权吗？"
              confirm-button-type="danger"
              @confirm="deleteUser(row)"
            >
              <template #reference>
                <el-button 
                  size="small" 
                  type="danger" 
                  plain
                  :disabled="row.role === 'admin'"
                >注销删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Refresh } from '@element-plus/icons-vue';
import request from '../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';

const users = ref([]);
const loading = ref(false);

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await request.get('/sys-user/list');
    users.value = res || [];
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const resetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要将商户号 ${row.username} 的密码重置为初创密码 REMOVED_RESET_PASSWORD 吗？`, '强制重置', {
      type: 'warning'
    });
    await request.post(`/sys-user/reset-password/${row.id}`);
    ElMessage.success(`操作成功！密码已重设为 REMOVED_RESET_PASSWORD`);
  } catch (cancel) {}
};

const deleteUser = async (row) => {
  try {
    await request.delete(`/sys-user/${row.id}`);
    ElMessage.success('成功强制删除了该权限登录账号。');
    fetchUsers();
  } catch (error) {
    console.error(error);
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.sys-user-page {
  animation: fadeIn 0.4s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: var(--text-primary);
  font-size: 24px;
  letter-spacing: 0.5px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.table-card {
  border-radius: 12px;
  padding: 8px;
  background: white;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

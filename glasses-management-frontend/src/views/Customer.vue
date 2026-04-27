<template>
  <div class="page-shell customer-page">
    <section class="page-hero glass-card customer-hero">
      <div class="hero-copy">
        <h1 class="page-heading">顾客管理</h1>
        <p class="page-title-en">Customer Management</p>
      </div>

      <div class="hero-meta">
        <div class="meta-card">
          <span>当前顾客数</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="meta-card">
          <span>每页显示</span>
          <strong>{{ pageParams.size }}</strong>
        </div>
      </div>

      <div class="page-toolbar">
        <el-button type="primary" class="hero-btn" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新建顾客
        </el-button>
      </div>
    </section>

    <section class="surface-panel table-card">
      <div class="table-toolbar">
        <div class="toolbar-copy">
          <h3>顾客列表</h3>
        </div>

        <div class="search-shell compact-search-shell query-input">
          <div class="search-icon-badge compact-search-icon">
            <el-icon :size="18"><Search /></el-icon>
          </div>
          <el-input
            v-model="keyword"
            placeholder="搜索姓名 / 手机号"
            clearable
            class="search-control compact-search-control"
            @keyup.enter="handleQuery"
          />
          <el-button class="search-submit compact-search-submit" type="primary" @click="handleQuery">立即搜索</el-button>
        </div>
      </div>
      
      <el-table :data="tableData" v-loading="loading" row-key="id" class="main-table">
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="160" />
        <el-table-column label="性别" width="110">
          <template #default="scope">
            <el-tag class="gender-tag" :type="scope.row.gender === 1 ? 'primary' : (scope.row.gender === 2 ? 'success' : 'info')">
              {{ scope.row.gender === 1 ? '男' : (scope.row.gender === 2 ? '女' : '未知') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" min-width="130" />
        <el-table-column prop="createTime" label="注册时间" min-width="190" />
        <el-table-column label="操作" min-width="320" class-name="actions-column">
          <template #default="scope">
            <div class="table-actions">
              <el-button class="action-pill" @click="goArchive(scope.row.id)">
                <el-icon><View /></el-icon>档案
              </el-button>
              <el-button class="action-pill" @click="openEditDialog(scope.row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-popconfirm title="确定删除该顾客？将会级联清除其记录!" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button class="action-pill action-pill--danger">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-shell">
        <el-pagination
          v-model:current-page="pageParams.current"
          v-model:page-size="pageParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          prev-text="上一页"
          next-text="下一页"
          @size-change="handleQuery"
          @current-change="loadData"
        />
      </div>
    </section>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="min(92vw, 560px)">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="84px" class="customer-form">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入顾客姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender" class="gender-group">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择生日"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="其他需要补充的信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const keyword = ref('');
const pageParams = reactive({ current: 1, size: 10 });

const dialogVisible = ref(false);
const dialogTitle = ref('新建顾客');
const formRef = ref(null);
const form = reactive({
  id: null,
  name: '',
  phone: '',
  gender: 0,
  birthday: '',
  remark: ''
});

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
};

onMounted(() => {
  loadData();
});

const handleQuery = () => {
  pageParams.current = 1;
  loadData();
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await request.get('/customer/page', {
      params: { keyword: keyword.value, ...pageParams }
    });
    tableData.value = res.records;
    total.value = res.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const goArchive = (id) => {
  router.push('/archive/' + id);
};

const resetForm = () => {
  form.id = null;
  form.name = '';
  form.phone = '';
  form.gender = 0;
  form.birthday = '';
  form.remark = '';
};

const openAddDialog = () => {
  resetForm();
  dialogTitle.value = '新建顾客';
  dialogVisible.value = true;
};

const openEditDialog = (row) => {
  resetForm();
  Object.assign(form, row);
  dialogTitle.value = '编辑顾客';
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await request.put('/customer/update', form);
          ElMessage.success('更新成功');
        } else {
          await request.post('/customer/add', form);
          ElMessage.success('新建成功');
        }
        dialogVisible.value = false;
        loadData();
      } catch (e) {
        console.error(e);
      }
    }
  });
};

const handleDelete = async (id) => {
  try {
    await request.delete('/customer/' + id);
    ElMessage.success('删除成功');
    loadData();
  } catch (e) {
    console.error(e);
  }
};
</script>

<style scoped>
.customer-page {
  padding-top: 6px;
}

.customer-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) auto;
  gap: 24px;
  align-items: end;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
}

.meta-card {
  min-width: 120px;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.08);
}

.meta-card span {
  display: block;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.meta-card strong {
  font-size: 30px;
  line-height: 1;
  color: var(--text-primary);
}

.hero-btn {
  min-width: 138px;
}

.toolbar-copy h3 {
  margin: 0;
}

.query-input {
  width: min(100%, 390px);
}

.compact-search-shell {
  padding: 10px;
  border-radius: 24px;
  gap: 10px;
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.1);
}

.compact-search-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
}

.compact-search-submit {
  min-width: 118px;
  height: 44px !important;
  border-radius: 16px !important;
}

.gender-tag {
  border-radius: 999px;
  font-weight: 700;
  padding: 0 10px;
}

.table-actions {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  white-space: nowrap;
  min-width: max-content;
  align-items: center;
  padding: 4px 0;
}

.table-actions > * {
  flex: 0 0 auto;
  position: relative;
}

.table-actions :deep(.action-pill:hover) {
  position: relative;
  z-index: 2;
}

.main-table :deep(.actions-column) {
  overflow: visible;
}

.main-table :deep(.actions-column .cell) {
  overflow: visible;
  padding-top: 8px;
  padding-bottom: 8px;
}

.customer-form {
  padding-top: 10px;
}

.gender-group {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .customer-hero {
    grid-template-columns: 1fr;
  }

  .hero-meta {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .query-input {
    width: 100%;
  }

  .compact-search-submit {
    min-width: 100%;
  }

  .meta-card {
    flex: 1 1 140px;
  }

  .table-actions {
    gap: 8px;
  }
}
</style>

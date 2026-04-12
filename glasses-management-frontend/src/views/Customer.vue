<template>
  <div class="customer-container">
    <div class="page-header">
      <h2>顾客管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>新建顾客
      </el-button>
    </div>

    <div class="glass-card table-wrapper">
      <div class="filter-section">
        <el-input 
          v-model="keyword" 
          placeholder="搜索姓名 / 手机号" 
          clearable 
          class="w-64"
          @keyup.enter="handleQuery"
        >
          <template #append>
            <el-button @click="handleQuery"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe class="main-table">
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="150" />
        <el-table-column label="性别" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 1 ? '' : (scope.row.gender === 2 ? 'danger' : 'info')">
              {{ scope.row.gender === 1 ? '男' : (scope.row.gender === 2 ? '女' : '未知') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="120" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="goArchive(scope.row.id)">档案</el-button>
            <el-button type="primary" link @click="openEditDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该顾客？将会级联清除其记录!" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pageParams.current"
          v-model:page-size="pageParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入顾客姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" style="width: 100%" />
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

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Plus, Search } from '@element-plus/icons-vue';

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
}

const loadData = async () => {
  loading.value = true;
  try {
    const res = await request.get('/customer/page', {
      params: { keyword: keyword.value, ...pageParams }
    });
    tableData.value = res.records;
    total.value = res.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

const goArchive = (id) => {
  router.push('/archive/' + id);
}

const resetForm = () => {
  form.id = null;
  form.name = '';
  form.phone = '';
  form.gender = 0;
  form.birthday = '';
  form.remark = '';
}

const openAddDialog = () => {
  resetForm();
  dialogTitle.value = '新建顾客';
  dialogVisible.value = true;
}

const openEditDialog = (row) => {
  resetForm();
  Object.assign(form, row);
  dialogTitle.value = '编辑顾客';
  dialogVisible.value = true;
}

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
}

const handleDelete = async (id) => {
  try {
    await request.delete('/customer/' + id);
    ElMessage.success('删除成功');
    loadData();
  } catch (e) {
    console.error(e);
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: #111827;
}

.table-wrapper {
  padding: 24px;
}

.filter-section {
  margin-bottom: 20px;
}

.w-64 {
  width: 320px;
}

.main-table {
  border-radius: 8px;
  overflow: hidden;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

<template>
  <div class="page-shell data-page">
    <section class="page-hero glass-card">
      <div>
        <h1 class="page-heading">数据管理</h1>
        <p class="page-title-en">DATA MANAGEMENT</p>
      </div>
      <p class="hero-desc">导出全部业务数据为 JSON 文件，或从 JSON 文件导入数据。同名用户和手机号已有记录不会重复导入。</p>
    </section>

    <div class="data-grid">
      <!-- Export Card -->
      <section class="glass-card data-card">
        <div class="card-icon export-icon">
          <el-icon :size="28"><Download /></el-icon>
        </div>
        <h2>导出数据</h2>
        <p>将全部用户、顾客、验光记录和销售记录导出为一个 JSON 文件，可用于备份或迁移到其他设备。</p>
        <el-button type="primary" :loading="exportLoading" @click="handleExport" class="action-btn">
          <el-icon><Download /></el-icon>
          导出 JSON 文件
        </el-button>
      </section>

      <!-- Import Card -->
      <section class="glass-card data-card">
        <div class="card-icon import-icon">
          <el-icon :size="28"><Upload /></el-icon>
        </div>
        <h2>导入数据</h2>
        <p>从之前导出的 JSON 文件中导入数据。已存在的用户（同名）和顾客（同手机号）会跳过，验光记录和销售记录会追加导入。</p>

        <el-radio-group v-model="importMode" class="import-mode-group">
          <el-radio value="merge">合并追加（不覆盖已有数据）</el-radio>
          <el-radio value="replace">全量替换（清空后导入）</el-radio>
        </el-radio-group>

        <el-alert
          :title="importMode === 'replace' ? '⚠️ 全量替换将清空现有数据' : '导入不会覆盖现有数据'"
          :type="importMode === 'replace' ? 'warning' : 'info'"
          :closable="false"
          show-icon
          class="import-alert"
        >
          <template v-if="importMode === 'replace'">
            将清空全部顾客、验光记录、销售记录和非管理员账号，然后从文件导入。管理员账号会被保留。
          </template>
          <template v-else>
            同名用户和同手机号顾客已存在时会跳过；验光记录按顾客+日期+度数去重；销售记录按单号去重。
          </template>
        </el-alert>

        <div class="import-actions">
          <input
            ref="fileInputRef"
            type="file"
            accept=".json"
            hidden
            @change="handleFileChange"
          />
          <el-button class="action-btn action-btn--file" @click="triggerFileSelect">
            <el-icon><FolderOpened /></el-icon>
            <span class="file-btn-text">{{ selectedFile ? selectedFile.name : '选择 JSON 文件' }}</span>
          </el-button>

          <el-button
            type="primary"
            :loading="importLoading"
            :disabled="!selectedFile"
            @click="handleImport"
            class="action-btn action-btn--import"
          >
            <el-icon><Upload /></el-icon>
            确认导入
          </el-button>
        </div>

        <div v-if="importResult" class="import-result" :class="{ 'import-error': importResult.error }">
          <el-icon :size="18">
            <CircleCheckFilled v-if="!importResult.error" />
            <CircleCloseFilled v-else />
          </el-icon>
          <span>{{ importResult.message }}</span>
        </div>
      </section>
    </div>

    <!-- System Reset -->
    <section class="glass-card reset-card">
      <div class="card-icon reset-icon">
        <el-icon :size="28"><WarningFilled /></el-icon>
      </div>
      <h2>清空系统数据</h2>
      <p>彻底删除全部顾客、验光记录、销售记录和非管理员账号。管理员账号会被保留。此操作不可撤销，请谨慎使用。</p>
      <el-button type="danger" :loading="resetLoading" @click="handleReset" class="action-btn action-btn--reset">
        <el-icon><Delete /></el-icon>
        清空数据
      </el-button>
      <div v-if="resetResult" class="import-result" :class="{ 'import-error': resetResult.error }">
        <el-icon :size="18">
          <CircleCheckFilled v-if="!resetResult.error" />
          <CircleCloseFilled v-else />
        </el-icon>
        <span>{{ resetResult.message }}</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Download,
  Upload,
  FolderOpened,
  CircleCheckFilled,
  CircleCloseFilled,
  WarningFilled,
  Delete
} from '@element-plus/icons-vue';
import request, { downloadBlob } from '../utils/request';

const exportLoading = ref(false);
const importLoading = ref(false);
const selectedFile = ref<File | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const importMode = ref('merge');
const importResult = ref<{ message: string; error: boolean } | null>(null);
const resetLoading = ref(false);
const resetResult = ref<{ message: string; error: boolean } | null>(null);

const handleExport = async () => {
  exportLoading.value = true;
  try {
    await downloadBlob('/data/export', 'glasses_data.json');
    ElMessage.success('数据导出成功');
  } catch (e: any) {
    ElMessage.error('导出失败: ' + (e.message || '未知错误'));
  } finally {
    exportLoading.value = false;
  }
};

const triggerFileSelect = () => {
  fileInputRef.value?.click();
};

const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (file) {
    selectedFile.value = file;
    importResult.value = null;
  }
};

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件');
    return;
  }

  const isReplace = importMode.value === 'replace';
  try {
    await ElMessageBox.confirm(
      isReplace
        ? '将清空全部顾客、验光记录、销售记录和非管理员账号，然后从文件导入所有数据。此操作不可撤销，确定要继续吗？'
        : '导入将追加数据，已存在的用户和顾客不会重复导入。确定要继续吗？',
      '确认导入',
      {
        confirmButtonText: isReplace ? '确认清空并导入' : '确认导入',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
  } catch {
    return;
  }

  importLoading.value = true;
  importResult.value = null;
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('mode', importMode.value);
    const res = await request.post('/data/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    // request interceptor unwraps Result<T> and returns the data field
    importResult.value = { message: String(res || '导入成功'), error: false };
    ElMessage.success(String(res || '导入成功'));
    if (fileInputRef.value) {
      fileInputRef.value.value = '';
    }
    selectedFile.value = null;
  } catch (e: any) {
    const msg = e?.message || '导入失败';
    importResult.value = { message: msg, error: true };
    ElMessage.error(msg);
  } finally {
    importLoading.value = false;
  }
};

const handleReset = async () => {
  // Step 1: risk warning confirmation
  try {
    await ElMessageBox.confirm(
      '将彻底删除全部顾客、验光记录、销售记录和非管理员账号。管理员账号会被保留。此操作不可撤销！',
      '⚠️ 确认清空数据',
      {
        confirmButtonText: '继续',
        cancelButtonText: '取消',
        type: 'error'
      }
    );
  } catch {
    return;
  }

  // Step 2: type verification
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入「清空数据」以确认操作：',
      '二次确认',
      {
        confirmButtonText: '确认清空',
        cancelButtonText: '取消',
        type: 'error',
        inputValidator: (val: string) => {
          if (val !== '清空数据') {
            return '输入不匹配，请检查后重试';
          }
          return true;
        },
        inputErrorMessage: '输入不匹配，请检查后重试'
      }
    );
    if (value !== '清空数据') {
      ElMessage.warning('输入不匹配，操作已取消');
      return;
    }
  } catch {
    return;
  }

  // Execute reset
  resetLoading.value = true;
  resetResult.value = null;
  try {
    const res = await request.post('/data/reset');
    resetResult.value = { message: String(res || '数据已清空'), error: false };
    ElMessage.success(String(res || '数据已清空'));
  } catch (e: any) {
    const msg = e?.message || '清空失败';
    resetResult.value = { message: msg, error: true };
    ElMessage.error(msg);
  } finally {
    resetLoading.value = false;
  }
};
</script>

<style scoped>
.data-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-hero {
  margin-bottom: 28px;
}

.page-heading {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-title-en {
  margin: 4px 0 0;
  font-size: 12px;
  font-weight: 800;
  color: var(--text-muted);
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-desc {
  margin: 12px 0 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .data-grid {
    grid-template-columns: 1fr;
  }
}

.data-card {
  padding: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.export-icon {
  background: var(--gradient-soft);
  color: var(--primary-color);
}

.import-icon {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(16, 185, 129, 0.08));
  color: #16a34a;
}

.data-card h2 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.data-card p {
  margin: 0 0 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  flex: 1;
}

.action-btn {
  height: 42px;
  font-weight: 700;
  border-radius: 12px;
  flex-shrink: 0;
}

.action-btn--file {
  max-width: 200px;
}

.action-btn--import {
  min-width: 140px;
}

.file-btn-text {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.import-mode-group {
  width: 100%;
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
  gap: 24px;
}

.import-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
}

.import-alert {
  width: 100%;
  margin-bottom: 20px;
  text-align: left;
  border-radius: 12px;
}

.import-result {
  margin-top: 16px;
  padding: 12px 18px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
  width: 100%;
  justify-content: center;
}

.import-result.import-error {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.reset-card {
  margin-top: 28px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 1px solid rgba(239, 68, 68, 0.2);
  background: rgba(239, 68, 68, 0.03);
}

.reset-icon {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.18), rgba(220, 38, 38, 0.1));
  color: #dc2626;
}

.reset-card h2 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
}

.reset-card p {
  margin: 0 0 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 480px;
}

.action-btn--reset {
  min-width: 160px;
}
</style>

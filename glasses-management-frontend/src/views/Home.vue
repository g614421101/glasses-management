<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="greeting">工作台</h1>
      <p class="sub-greeting">输入顾客手机号即可快速调出其历史视光与配镜档案</p>
    </div>

    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="输入顾客手机号或姓名进行搜索..."
        class="main-search-input"
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon class="el-input__icon"><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="handleSearch">检索档案</el-button>
        </template>
      </el-input>
      
      <!-- 搜索结果弹层（如果有多个，可以展示列表；精简版如果搜到一个直接跳转） -->
      <transition name="el-zoom-in-top">
        <div v-if="searchResults.length > 0" class="search-results-popper glass-card">
          <div v-for="item in searchResults" :key="item.id" class="result-item" @click="goToArchive(item.id)">
            <div class="result-info">
              <span class="r-name">{{ item.name }}</span>
              <span class="r-phone">{{ item.phone }}</span>
            </div>
            <el-icon color="#9ca3af"><ArrowRight /></el-icon>
          </div>
        </div>
      </transition>
    </div>

    <div class="shortcuts-group">
      <div class="shortcut-card add-customer" @click="goCustomer">
        <el-icon :size="32"><UserFilled /></el-icon>
        <h3>新建顾客</h3>
        <p>录入新到店客户信息</p>
      </div>
      <!-- 目前精简版将验光和配镜挂在顾客详情页，这里快捷跳转顾客列表 -->
      <div class="shortcut-card view-list" @click="goCustomer">
        <el-icon :size="32"><Grid /></el-icon>
        <h3>所有顾客</h3>
        <p>浏览顾客列表并开单</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Search, ArrowRight, UserFilled, Grid } from '@element-plus/icons-vue';

const router = useRouter();
const searchQuery = ref('');
const searchResults = ref<any>([]);

const handleSearch = async () => {
  if (!searchQuery.value) {
    searchResults.value = [];
    return;
  }
  try {
    const res: any = await request.get('/customer/page', { params: { keyword: searchQuery.value, current: 1, size: 5 } });
    if (res.records && res.records.length > 0) {
      if (res.records.length === 1 && res.records[0].phone === searchQuery.value) {
        // 精确匹配手机号直接跳转
        goToArchive(res.records[0].id);
      } else {
        searchResults.value = res.records;
      }
    } else {
      ElMessage.warning('未找到对应记录');
      searchResults.value = [];
    }
  } catch (e) {
    console.error(e);
  }
}

const goToArchive = (id: any) => {
  router.push('/archive/' + id);
};

const goCustomer = () => {
  router.push('/customer');
}
</script>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 40px auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.greeting {
  font-size: 36px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.sub-greeting {
  font-size: 16px;
  color: #6b7280;
}

.search-section {
  width: 100%;
  max-width: 680px;
  position: relative;
  margin-bottom: 60px;
}

/* 覆盖Element Plus的输入框样式以实现现代胶囊极简设计 */
:deep(.main-search-input .el-input__wrapper) {
  padding: 8px 16px;
  font-size: 16px;
  border-radius: 999px 0 0 999px;
  box-shadow: 0 4px 15px -2px rgba(0, 0, 0, 0.05) !important;
}

:deep(.main-search-input .el-input-group__append) {
  border-radius: 0 999px 999px 0;
  padding: 4px 6px 4px 0; /* 内边距给胶囊按钮留出空间 */
  background-color: #ffffff;
  border: none;
  box-shadow: 0 4px 15px -2px rgba(0, 0, 0, 0.05) !important;
}

/* 内部的悬浮收缩检索胶囊按钮 */
:deep(.main-search-input .el-input-group__append button) {
  height: 100%;
  margin: 0;
  padding: 0 24px;
  background-color: var(--primary-color) !important;
  color: #ffffff !important;
  border-radius: 999px !important;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(255, 107, 107, 0.25) !important;
  transform: scale(0.9) !important; /* 不悬停时默认缩小在内部 */
  transition: all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important; /* 弹簧动画 */
}

/* 悬停时弹出来充满整个右侧胶囊边界 */
:deep(.main-search-input .el-input-group__append button:hover) {
  transform: scale(1) !important;
  background-color: var(--primary-hover) !important;
  box-shadow: 0 6px 14px rgba(255, 107, 107, 0.4) !important;
  opacity: 1;
}

/* 当搜索框或按钮激活时，将清除图标左移，避免被放大的按钮遮挡 */
:deep(.main-search-input .el-input__clear) {
  transition: all 0.3s;
}

:deep(.main-search-input:hover .el-input__clear) {
  transform: translateX(-12px);
}

.search-results-popper {
  position: absolute;
  top: 60px;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  z-index: 100;
  padding: 8px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.result-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.r-name {
  font-weight: 600;
  color: #374151;
}

.r-phone {
  color: #9ca3af;
}

.shortcuts-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.shortcut-card {
  width: 240px;
  padding: 30px 20px;
  background: white;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  border: 1px solid #f3f4f6;
  position: relative;
  overflow: hidden;
}

.shortcut-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; height: 4px;
  background: transparent;
  transition: all 0.3s;
}

.shortcut-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
}

.add-customer:hover::before {
  background: linear-gradient(90deg, #10b981, #34d399);
}

.view-list:hover::before {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

.shortcut-card h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: #1f2937;
}

.shortcut-card p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}
</style>

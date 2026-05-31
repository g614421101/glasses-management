<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="greeting">工作台</h1>
      <p class="sub-greeting">输入顾客手机号即可快速调出其历史视光与配镜档案</p>
    </div>

    <div class="search-section">
      <div class="search-shell home-search-shell">
        <div class="search-icon-badge">
          <el-icon :size="20"><Search /></el-icon>
        </div>
        <el-input
          v-model="searchQuery"
          placeholder="输入顾客手机号或姓名进行搜索..."
          class="search-control home-search-control"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" class="search-submit home-search-submit" @click="handleSearch">
          检索档案
        </el-button>
      </div>
      
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

    <div v-if="lanUrl" class="lan-card glass-card">
      <div class="lan-header">
        <el-icon :size="20"><Link /></el-icon>
        <span>局域网连接</span>
      </div>
      <p class="lan-tip">同一网络下的设备可通过以下地址访问本系统</p>
      <canvas ref="qrCanvas" class="lan-qr"></canvas>
      <div class="lan-url-row">
        <code class="lan-url">{{ lanUrl }}</code>
        <el-button size="small" class="lan-copy-btn" @click="copyLanUrl">复制链接</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Search, ArrowRight, UserFilled, Grid, Link } from '@element-plus/icons-vue';
import QRCode from 'qrcode';

const router = useRouter();
const searchQuery = ref('');
const searchResults = ref<any>([]);

const lanUrl = ref('');
const qrCanvas = ref<HTMLCanvasElement | null>(null);

onMounted(async () => {
  try {
    const res: any = await request.get('/system/lan-info');
    if (res.ip && res.port) {
      lanUrl.value = `http://${res.ip}:${res.port}`;
      await nextTick();
      if (qrCanvas.value) {
        await QRCode.toCanvas(qrCanvas.value, lanUrl.value, { width: 160, margin: 2 });
      }
    }
  } catch {
    // 未检测到局域网，静默隐藏
  }
});

const copyLanUrl = async () => {
  try {
    await navigator.clipboard.writeText(lanUrl.value);
    ElMessage.success('链接已复制');
  } catch {
    ElMessage.warning('复制失败，请手动复制');
  }
};

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
  padding: 0 12px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.greeting {
  font-size: clamp(32px, 5vw, 44px);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.sub-greeting {
  font-size: 16px;
  color: var(--text-secondary);
}

.search-section {
  width: 100%;
  max-width: 680px;
  position: relative;
  margin-bottom: 60px;
}

.home-search-shell {
  gap: 16px;
  padding: 14px;
  border-radius: 34px;
}

.home-search-submit {
  min-width: 144px;
}

.search-results-popper {
  position: absolute;
  top: calc(100% + 14px);
  left: 0;
  right: 0;
  background: var(--surface-overlay);
  border-radius: 20px;
  border: 1px solid var(--border-color);
  z-index: 100;
  padding: 8px;
  box-shadow: 0 22px 45px rgba(15, 23, 42, 0.12);
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
  background: var(--primary-soft);
  transform: translateX(4px);
}

.result-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.r-name {
  font-weight: 600;
  color: var(--text-primary);
}

.r-phone {
  color: var(--text-muted);
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
  background: var(--surface-overlay);
  border-radius: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 20px 40px rgba(37, 99, 235, 0.1);
  border: 1px solid var(--border-color);
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
  box-shadow: 0 28px 55px rgba(37, 99, 235, 0.16);
}

.add-customer:hover::before {
  background: linear-gradient(90deg, #2563eb, #38bdf8);
}

.view-list:hover::before {
  background: linear-gradient(90deg, #0f172a, #2563eb);
}

.shortcut-card h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  color: var(--text-primary);
}

.shortcut-card p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

@media (max-width: 640px) {
  .home-container {
    margin-top: 18px;
  }

  .search-section {
    margin-bottom: 36px;
  }

  .home-search-shell {
    padding: 10px;
  }

  .home-search-submit {
    min-width: 100%;
  }

  .result-info {
    gap: 10px;
    flex-direction: column;
    align-items: flex-start;
  }

  .shortcut-card {
    width: 100%;
  }
}

/* ── 局域网连接卡片 ── */
.lan-card {
  width: 100%;
  max-width: 400px;
  padding: 28px;
  margin-top: 8px;
  text-align: center;
}

.lan-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.lan-tip {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 18px;
}

.lan-qr {
  display: block;
  margin: 0 auto 16px;
}

.lan-url-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.lan-url {
  font-size: 13px;
  padding: 6px 12px;
  background: var(--bg-color);
  border-radius: 8px;
  color: var(--primary-color);
  word-break: break-all;
}

.lan-copy-btn {
  flex-shrink: 0;
}
</style>

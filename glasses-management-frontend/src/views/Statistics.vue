<template>
  <div class="stats-container">
    <div class="page-header">
      <h2 class="title">营收统计</h2>
      <div class="header-actions">
        <el-switch
          v-model="showAll"
          active-text="展示所有记录"
          class="mr-4"
          @change="handleShowAllChange"
        />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :clearable="false"
          :disabled="showAll"
          @change="handleDateChange"
        />
        <el-button type="warning" class="ml-4" @click="exportExcel">
          <el-icon><Download /></el-icon>导出流水
        </el-button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-grid">
      <div class="summary-card glass-card">
        <div class="card-icon revenue">
          <el-icon><Money /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">累计营收 (元)</p>
          <h3 class="value">￥{{ statsData.totalRevenue || '0.00' }}</h3>
        </div>
      </div>

      <div class="summary-card glass-card">
        <div class="card-icon orders">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">订单总量 (笔)</p>
          <h3 class="value">{{ statsData.orderCount || 0 }}</h3>
        </div>
      </div>

      <div class="summary-card glass-card">
        <div class="card-icon avg">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">平均客单价 (元)</p>
          <h3 class="value">￥{{ avgOrderValue }}</h3>
        </div>
      </div>
    </div>

    <!-- 流水详情列表 -->
    <div class="details-section mt-6">
      <div class="section-card glass-card">
        <h3 class="section-title">收支流水明细</h3>
        <el-table :data="statsData.records" style="width: 100%" v-loading="loading">
          <el-table-column prop="salesDate" label="日期" width="180" />
          <el-table-column prop="recordNo" label="单号" width="200" />
          <el-table-column label="商品项目">
            <template #default="scope">
              <span class="item-tag">{{ scope.row.frameBrand }} {{ scope.row.frameModel }}</span>
              <el-divider direction="vertical" />
              <span class="item-tag">{{ scope.row.lensBrand }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template #default="scope">
              <span class="price-text">￥{{ scope.row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="primary" @click="goToArchive(scope.row.customerId)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页控制 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pageParams.current"
            v-model:page-size="pageParams.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchStats"
            @current-change="fetchStats"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Money, Ticket, DataLine, Download } from '@element-plus/icons-vue';
import dayjs from 'dayjs';

const router = useRouter();
const loading = ref(false);
const showAll = ref(false);
const total = ref(0);
const pageParams = reactive({
  current: 1,
  size: 10
});

const dateRange = ref([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
]);

const statsData = ref({
  totalRevenue: 0,
  orderCount: 0,
  records: []
});

const avgOrderValue = computed(() => {
  if (!statsData.value.orderCount) return '0.00';
  return (statsData.value.totalRevenue / statsData.value.orderCount).toFixed(2);
});

onMounted(() => {
  fetchStats();
});

const handleDateChange = () => {
  pageParams.current = 1;
  fetchStats();
};

const handleShowAllChange = () => {
  pageParams.current = 1;
  fetchStats();
};

const fetchStats = async () => {
  loading.value = true;
  try {
    const params = {
      ...pageParams,
      showAll: showAll.value
    };
    
    if (!showAll.value && dateRange.value) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }

    const res = await request.get('/sales/stats', { params });
    
    if (res) {
      statsData.value.totalRevenue = res.totalRevenue;
      statsData.value.orderCount = res.orderCount;
      statsData.value.records = res.records.records; // MyBatis Plus Page records
      total.value = res.records.total;
    }
  } catch (e) {
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

const goToArchive = (customerId) => {
  router.push('/archive/' + customerId);
};

const exportExcel = () => {
  let url = `http://localhost:8080/api/print/export/revenue?showAll=${showAll.value}`;
  
  if (!showAll.value && dateRange.value) {
    url += `&startDate=${dateRange.value[0]}&endDate=${dateRange.value[1]}`;
  }
  
  window.open(url, '_blank');
};
</script>

<style scoped>
.stats-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.summary-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.card-icon.revenue { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.card-icon.orders { background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%); }
.card-icon.avg { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }

.label {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--text-secondary);
}

.value {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-card {
  padding: 24px;
}

.section-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.item-tag {
  color: var(--text-secondary);
  font-size: 13px;
}

.price-text {
  color: #ef4444;
  font-weight: 600;
}

.mt-6 { margin-top: 24px; }
.ml-4 { margin-left: 16px; }
.mr-4 { margin-right: 16px; }

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

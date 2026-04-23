<template>
  <div class="page-shell stats-page">
    <section class="page-hero glass-card stats-hero">
      <div class="hero-copy">
        <span class="hero-kicker">Revenue Overview</span>
        <h1 class="page-heading">营收统计</h1>
        <p class="page-subheading">
          把区间筛选、导出和分页放进同一套蓝白工作流里，让统计页在大屏和小屏上都更清晰、更顺手。
        </p>
      </div>

      <div class="filter-toolbar">
        <el-switch
          v-model="showAll"
          active-text="展示所有记录"
          class="stats-switch"
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
        <el-button type="primary" @click="exportExcel">
          <el-icon><Download /></el-icon>导出流水
        </el-button>
      </div>
    </section>

    <section class="summary-grid">
      <div class="summary-card glass-card summary-revenue">
        <div class="card-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">累计营收 (元)</p>
          <h3 class="value">￥{{ statsData.totalRevenue || '0.00' }}</h3>
        </div>
      </div>

      <div class="summary-card glass-card summary-orders">
        <div class="card-icon">
          <el-icon><Ticket /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">订单总量 (笔)</p>
          <h3 class="value">{{ statsData.orderCount || 0 }}</h3>
        </div>
      </div>

      <div class="summary-card glass-card summary-average">
        <div class="card-icon">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="card-content">
          <p class="label">平均客单价 (元)</p>
          <h3 class="value">￥{{ avgOrderValue }}</h3>
        </div>
      </div>
    </section>

    <section class="surface-panel table-card">
      <div class="table-toolbar">
        <div class="toolbar-copy">
          <h3>收支流水明细</h3>
        </div>
      </div>

      <el-table :data="statsData.records" style="width: 100%" v-loading="loading" class="stats-table">
        <el-table-column prop="salesDate" label="日期" min-width="180" />
        <el-table-column prop="recordNo" label="单号" min-width="200" />
        <el-table-column label="商品项目" min-width="220">
          <template #default="scope">
            <div class="item-tags">
              <span class="item-tag">{{ scope.row.frameBrand }} {{ scope.row.frameModel }}</span>
              <span class="item-tag item-tag--soft">{{ scope.row.lensBrand }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" min-width="120">
          <template #default="scope">
            <span class="price-text">￥{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="130">
          <template #default="scope">
            <el-button class="action-pill" @click="goToArchive(scope.row.customerId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-shell">
        <el-pagination
          v-model:current-page="pageParams.current"
          v-model:page-size="pageParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          prev-text="上一页"
          next-text="下一页"
          @size-change="fetchStats"
          @current-change="fetchStats"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
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
    const params: any = {
      ...pageParams,
      showAll: showAll.value
    };
    
    if (!showAll.value && dateRange.value) {
      params.startDate = dateRange.value[0];
      params.endDate = dateRange.value[1];
    }

    const res: any = await request.get('/sales/stats', { params });
    
    if (res) {
      statsData.value.totalRevenue = res.totalRevenue;
      statsData.value.orderCount = res.orderCount;
      statsData.value.records = res.records.records;
      total.value = res.records.total;
    }
  } catch (e) {
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

const goToArchive = (customerId: any) => {
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
.stats-page {
  padding-top: 6px;
}

.stats-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.9fr);
  gap: 24px;
  align-items: center;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  margin-bottom: 16px;
  border-radius: 999px;
  background: rgba(219, 234, 254, 0.9);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.filter-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.toolbar-copy h3 {
  margin: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.summary-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #ffffff;
  box-shadow: 0 18px 36px rgba(37, 99, 235, 0.18);
}

.summary-revenue .card-icon {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
}

.summary-orders .card-icon {
  background: linear-gradient(135deg, #2563eb 0%, #38bdf8 100%);
}

.summary-average .card-icon {
  background: linear-gradient(135deg, #0f172a 0%, #2563eb 100%);
}

.label {
  margin: 0 0 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.value {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
  color: var(--text-primary);
}

.item-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.item-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(219, 234, 254, 0.82);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 700;
}

.item-tag--soft {
  background: rgba(239, 246, 255, 0.92);
  color: var(--text-secondary);
}

.price-text {
  color: var(--primary-color);
  font-weight: 800;
}

@media (max-width: 1024px) {
  .stats-hero,
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .filter-toolbar {
    justify-content: stretch;
  }

  .filter-toolbar > * {
    width: 100%;
  }
}
</style>

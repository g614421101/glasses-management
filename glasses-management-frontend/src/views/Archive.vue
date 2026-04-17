<template>
  <div class="archive-container">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="router.back()">
          <el-icon><Back /></el-icon> 返回
        </el-button>
        <h2 class="ml-4">顾客档案</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openOptometryDialog">
          <el-icon><DocumentAdd /></el-icon>录入验光单
        </el-button>
        <el-button type="success" @click="openSalesDialog">
          <el-icon><ShoppingCart /></el-icon>我要开单
        </el-button>
        <el-button type="warning" @click="exportExcel">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
      </div>
    </div>

    <div class="archive-content">
      <!-- 顾客基础信息卡片 -->
      <div class="info-card glass-card">
        <div class="avatar-wrap">
          <div class="avatar">{{ customerInfo.name ? customerInfo.name.charAt(0) : '客' }}</div>
        </div>
        <h3 class="c-name">{{ customerInfo.name || '-' }}</h3>
        <p class="c-phone">{{ customerInfo.phone || '-' }}</p>
        <div class="base-info mt-4">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="性别">{{ customerInfo.gender === 1 ? '男' : (customerInfo.gender === 2 ? '女' : '未知') }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ customerInfo.birthday || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ customerInfo.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 时间轴主体 -->
      <div class="timeline-card glass-card">
        <h3 class="section-title">档案记录 (Timeline)</h3>
        <el-empty v-if="timelineData.length === 0" description="暂无历史记录" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(item, index) in timelineData"
            :key="index"
            :timestamp="item.date"
            placement="top"
            :type="item.type === 'OPTOMETRY' ? 'primary' : 'success'"
            :icon="item.type === 'OPTOMETRY' ? View : Goods"
          >
            <el-card shadow="hover" class="timeline-detail-card" @click="viewDetail(item)">
              <h4>{{ item.title }}</h4>
              <p class="subtitle">{{ item.subtitle }}</p>
              <div class="action-bar" @click.stop>
                <el-button v-if="item.type === 'SALES'" type="primary" link size="small" @click="printRecord(item.data.id)">预览/打印处方</el-button>
                <el-button type="primary" link size="small" @click="openEdit(item)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(item)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 侧边抽屉：展示详细信息 -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="400px">
      <div v-if="currentDetail && currentDetail.type === 'OPTOMETRY'" class="detail-box">
        <table class="opto-table">
          <thead>
            <tr><th>眼别</th><th>SPH<br/>(球镜)</th><th>CYL<br/>(柱镜)</th><th>AXIS<br/>(轴位)</th><th>VA<br/>(视力)</th></tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>右眼(OD)</strong></td>
              <td>{{ fmt(currentDetail.data.odSph) }}</td>
              <td>{{ fmt(currentDetail.data.odCyl) }}</td>
              <td>{{ currentDetail.data.odAxis || '-' }}</td>
              <td>{{ currentDetail.data.odVa || '-' }}</td>
            </tr>
            <tr>
              <td><strong>左眼(OS)</strong></td>
              <td>{{ fmt(currentDetail.data.osSph) }}</td>
              <td>{{ fmt(currentDetail.data.osCyl) }}</td>
              <td>{{ currentDetail.data.osAxis || '-' }}</td>
              <td>{{ currentDetail.data.osVa || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <div class="extra-opto mt-4">
          <p>右眼瞳距(PD): {{ currentDetail.data.odPd || '-' }}</p>
          <p>左眼瞳距(PD): {{ currentDetail.data.osPd || '-' }}</p>
          <p>远用瞳距: {{ currentDetail.data.pdFar || '-' }}</p>
          <p>近用瞳距: {{ currentDetail.data.pdNear || '-' }}</p>
          <p>下加光 (ADD): {{ currentDetail.data.addPower || '-' }}</p>
          <p>验光师: {{ currentDetail.data.optometristName || '-' }}</p>
        </div>
      </div>
      
      <div v-else-if="currentDetail && currentDetail.type === 'SALES'" class="detail-box">
        <p><strong>单号:</strong> {{ currentDetail.data.recordNo }}</p>
        <el-divider />
        <p><strong>镜架:</strong> {{ currentDetail.data.frameBrand || '-' }} {{ currentDetail.data.frameModel || '-' }} (￥{{ currentDetail.data.framePrice }})</p>
        <p><strong>镜片:</strong> {{ currentDetail.data.lensBrand || '-' }} {{ currentDetail.data.lensParams || '-' }} (￥{{ currentDetail.data.lensPrice }})</p>
        <el-divider />
        <p class="total-price"><strong>总实收:</strong> <span class="price-txt">￥{{ currentDetail.data.totalAmount }}</span></p>
      </div>
    </el-drawer>

    <!-- 弹窗：录入验光单 -->
    <el-dialog :title="optoForm.id ? '编辑验光单' : '录入验光单'" v-model="optoDialogVisible" width="600px">
      <el-form :model="optoForm" label-width="100px" label-position="left">
        <h4 style="margin-top:0">右眼 (OD)</h4>
        <el-row :gutter="10">
          <el-col :span="6"><el-form-item label="SPH" label-width="40px"><el-input v-model="optoForm.odSph"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="CYL" label-width="40px"><el-input v-model="optoForm.odCyl"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="AXIS" label-width="45px"><el-input v-model="optoForm.odAxis"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="VA" label-width="30px"><el-input v-model="optoForm.odVa"/></el-form-item></el-col>
        </el-row>
        <h4 style="margin-top:10px">左眼 (OS)</h4>
        <el-row :gutter="10">
          <el-col :span="6"><el-form-item label="SPH" label-width="40px"><el-input v-model="optoForm.osSph"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="CYL" label-width="40px"><el-input v-model="optoForm.osCyl"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="AXIS" label-width="45px"><el-input v-model="optoForm.osAxis"/></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="VA" label-width="30px"><el-input v-model="optoForm.osVa"/></el-form-item></el-col>
        </el-row>
        <el-divider />
        <el-row :gutter="10">
          <el-col :span="8"><el-form-item label="右眼PD" label-width="60px"><el-input v-model="optoForm.odPd" @input="calcPdTotal"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="左眼PD" label-width="60px"><el-input v-model="optoForm.osPd" @input="calcPdTotal"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="远用PD" label-width="60px"><el-input v-model="optoForm.pdFar"/></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="8"><el-form-item label="近用PD" label-width="60px"><el-input v-model="optoForm.pdNear"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="ADD" label-width="50px"><el-input v-model="optoForm.addPower"/></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="验光师" label-width="60px"><el-input v-model="optoForm.optometristName" placeholder="默认操作人"/></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="optoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOpto">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：录入开单配镜 -->
    <el-dialog :title="salesForm.id ? '编辑配镜单' : '新建配镜单'" v-model="salesDialogVisible" width="500px">
      <el-form :model="salesForm" label-width="80px">
        <el-form-item label="关联验光">
          <el-select v-model="salesForm.optometryId" placeholder="选择验光记录(可选)" clearable style="width:100%">
            <el-option 
              v-for="item in timelineData.filter(i => i.type==='OPTOMETRY')" 
              :key="item.data.id" 
              :label="item.date + ' 验光单'" 
              :value="item.data.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="镜架品牌"><el-input v-model="salesForm.frameBrand" /></el-form-item>
        <el-form-item label="镜架型号"><el-input v-model="salesForm.frameModel" /></el-form-item>
        <el-form-item label="镜架售价"><el-input-number v-model="salesForm.framePrice" :precision="2" :step="10" @change="calcTotal" style="width:100%"/></el-form-item>
        
        <el-divider />
        
        <el-form-item label="镜片品牌"><el-input v-model="salesForm.lensBrand" /></el-form-item>
        <el-form-item label="镜片参数"><el-input v-model="salesForm.lensParams" placeholder="例: 1.60 防蓝光" /></el-form-item>
        <el-form-item label="镜片售价"><el-input-number v-model="salesForm.lensPrice" :precision="2" :step="10" @change="calcTotal" style="width:100%"/></el-form-item>
        
        <el-divider />
        
        <el-form-item label="实收总价">
          <el-input-number v-model="salesForm.totalAmount" :precision="2" :step="10" style="width:100%"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="salesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSales">确认开单</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Back, DocumentAdd, ShoppingCart, View, Goods, Download, Edit, Delete } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const customerId = route.params.id;

const customerInfo = ref({});
const timelineData = ref([]);

// 抽屉
const drawerVisible = ref(false);
const drawerTitle = ref('');
const currentDetail = ref(null);

// 验光表单
const optoDialogVisible = ref(false);
const optoForm = reactive({ 
  id: null,
  customerId,
  odSph:'', odCyl:'', odAxis:'', odVa:'',
  osSph:'', osCyl:'', osAxis:'', osVa:'',
  odPd:'', osPd:'', pdFar:'', pdNear:'', addPower:'',
  optometristName: ''
});

// 配镜表单
const salesDialogVisible = ref(false);
const salesForm = reactive({ 
  id: null,
  customerId, 
  optometryId: null, 
  frameBrand: '',
  frameModel: '',
  framePrice: 0, 
  lensBrand: '',
  lensParams: '',
  lensPrice: 0, 
  totalAmount: 0 
});

onMounted(() => {
  loadCustomer();
  loadTimeline();
});

const loadCustomer = async () => {
  try {
    const res = await request.get('/customer/' + customerId);
    customerInfo.value = res || {};
  } catch(e) {}
}

const loadTimeline = async () => {
  try {
    const res = await request.get('/archive/' + customerId);
    timelineData.value = res || [];
  } catch (e) {}
}

const viewDetail = (item) => {
  currentDetail.value = item;
  drawerTitle.value = item.title;
  drawerVisible.value = true;
}

const resetOpto = () => {
  Object.assign(optoForm, {
    id: null,
    customerId,
    odSph:'', odCyl:'', odAxis:'', odVa:'',
    osSph:'', osCyl:'', osAxis:'', osVa:'',
    odPd:'', osPd:'', pdFar:'', pdNear:'', addPower:'',
    optometristName: ''
  });
};

const calcPdTotal = () => {
  const od = parseFloat(optoForm.odPd) || 0;
  const os = parseFloat(optoForm.osPd) || 0;
  if (od > 0 && os > 0) {
    optoForm.pdFar = (od + os).toFixed(1);
  }
}

const openOptometryDialog = () => {
  resetOpto();
  optoDialogVisible.value = true;
}

const submitOpto = async () => {
  try {
    if (optoForm.id) {
      await request.put('/optometry/update', optoForm);
      ElMessage.success('更新成功');
    } else {
      await request.post('/optometry/add', optoForm);
      ElMessage.success('验光单录入成功');
    }
    optoDialogVisible.value = false;
    loadTimeline();
  } catch (e) {}
}

const resetSales = () => {
  Object.assign(salesForm, {
    id: null,
    customerId, optometryId: null,
    frameBrand:'', frameModel:'', framePrice:0,
    lensBrand:'', lensParams:'', lensPrice:0,
    totalAmount:0
  });
}

const calcTotal = () => {
  salesForm.totalAmount = (salesForm.framePrice || 0) + (salesForm.lensPrice || 0);
}

const openSalesDialog = () => {
  resetSales();
  salesDialogVisible.value = true;
}

const submitSales = async () => {
  try {
    if (salesForm.id) {
      await request.put('/sales/update', salesForm);
      ElMessage.success('更新成功');
    } else {
      await request.post('/sales/add', salesForm);
      ElMessage.success('配镜开单成功');
    }
    salesDialogVisible.value = false;
    loadTimeline();
  } catch(e){}
}

const openEdit = (item) => {
  if (item.type === 'OPTOMETRY') {
    Object.assign(optoForm, item.data);
    optoDialogVisible.value = true;
  } else {
    Object.assign(salesForm, item.data);
    salesDialogVisible.value = true;
  }
}

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    const url = item.type === 'OPTOMETRY' ? '/optometry/' : '/sales/';
    await request.delete(url + item.data.id);
    ElMessage.success('删除成功');
    loadTimeline();
  })
}

const printRecord = (id) => {
  // 打印接口已在后端白名单中放行，直接打开即可
  const url = `http://localhost:8080/api/print/prescription/${id}`;
  window.open(url, '_blank');
}

const exportExcel = () => {
  // 导出Excel也已在后端白名单中，直接下载
  const url = `http://localhost:8080/api/print/export/customer/${customerId}`;
  window.open(url, '_blank');
}

// 格式化度数，保留正负号
const fmt = (val) => {
  if (val === undefined || val === null || val === '') return '-';
  const num = parseFloat(val);
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
}
</script>

<style scoped>
.archive-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.archive-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.info-card {
  width: 280px;
  padding: 24px;
  text-align: center;
  position: sticky;
  top: 24px;
}

.avatar-wrap {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 10px rgba(161, 140, 209, 0.4);
}

.avatar {
  font-size: 32px;
  color: white;
  font-weight: bold;
}

.c-name {
  margin: 0 0 8px;
  font-size: 20px;
}

.c-phone {
  margin: 0;
  color: #6b7280;
}

.timeline-card {
  flex: 1;
  padding: 24px;
  min-height: 500px;
}

.section-title {
  margin-top: 0;
  margin-bottom: 24px;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 12px;
}

.timeline-detail-card {
  cursor: pointer;
  border-radius: 8px;
  position: relative;
}

.timeline-detail-card h4 {
  margin: 0 0 8px;
}

.timeline-detail-card .subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.action-bar {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}

.detail-box {
  padding: 0 10px;
}

.opto-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
}

.opto-table th, .opto-table td {
  border: 1px solid #e5e7eb;
  padding: 8px 4px;
}

.opto-table th {
  background-color: #f9fafb;
}

.extra-opto p {
  margin: 6px 0;
  color: #374151;
}

.total-price {
  font-size: 16px;
}

.price-txt {
  color: #ef4444;
  font-weight: bold;
  font-size: 20px;
}
</style>

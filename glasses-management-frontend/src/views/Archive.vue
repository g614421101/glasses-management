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

    <!-- 居中弹窗：展示历史记录详细信息 -->
    <el-dialog 
      v-model="detailVisible" 
      width="560px" 
      class="elegant-dialog" 
      align-center 
      destroy-on-close
      :show-close="false"
    >
      <template #header="{ close }">
        <div class="dialog-custom-header">
          <span class="dialog-title">{{ drawerTitle }}</span>
          <el-button class="close-btn" circle link @click="close"><el-icon><Close /></el-icon></el-button>
        </div>
      </template>

      <div v-if="currentDetail && currentDetail.type === 'OPTOMETRY'" class="detail-box animate-fade-up">
        <div class="receipt-badge primary">验光记录</div>
        <div class="table-container">
          <table class="opto-table elegant-table">
            <thead>
              <tr><th>眼别</th><th>SPH (球镜)</th><th>CYL (柱镜)</th><th>AXIS (轴位)</th><th>VA (视力)</th></tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="eye-label">右眼 OD</span></td>
                <td class="numbold">{{ fmt(currentDetail.data.odSph) }}</td>
                <td class="numbold">{{ fmt(currentDetail.data.odCyl) }}</td>
                <td class="numbold">{{ currentDetail.data.odAxis || '-' }}</td>
                <td class="numbold">{{ currentDetail.data.odVa || '-' }}</td>
              </tr>
              <tr>
                <td><span class="eye-label">左眼 OS</span></td>
                <td class="numbold">{{ fmt(currentDetail.data.osSph) }}</td>
                <td class="numbold">{{ fmt(currentDetail.data.osCyl) }}</td>
                <td class="numbold">{{ currentDetail.data.osAxis || '-' }}</td>
                <td class="numbold">{{ currentDetail.data.osVa || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="extra-grid mt-4">
          <div class="info-tag"><span>右眼PD</span><strong>{{ currentDetail.data.odPd || '-' }}</strong></div>
          <div class="info-tag"><span>左眼PD</span><strong>{{ currentDetail.data.osPd || '-' }}</strong></div>
          <div class="info-tag"><span>远用PD</span><strong>{{ currentDetail.data.pdFar || '-' }}</strong></div>
          <div class="info-tag"><span>近用PD</span><strong>{{ currentDetail.data.pdNear || '-' }}</strong></div>
          <div class="info-tag"><span>ADD</span><strong>{{ currentDetail.data.addPower || '-' }}</strong></div>
          <div class="info-tag"><span>验光师</span><strong>{{ currentDetail.data.optometristName || '-' }}</strong></div>
        </div>
      </div>
      
      <div v-else-if="currentDetail && currentDetail.type === 'SALES'" class="detail-box animate-fade-up">
        <div class="receipt-badge success">销售票据</div>
        <div class="receipt-body">
          <div class="receipt-row">
            <span class="r-label">订单号</span>
            <span class="r-value code">{{ currentDetail.data.recordNo }}</span>
          </div>
          <div class="receipt-divider"></div>
          <div class="receipt-row">
            <span class="r-label">所配镜架</span>
            <span class="r-value highlight">{{ currentDetail.data.frameBrand || '-' }} {{ currentDetail.data.frameModel || '-' }}</span>
          </div>
          <div class="receipt-row">
            <span class="r-label">镜架金额</span>
            <span class="r-value">￥{{ currentDetail.data.framePrice }}</span>
          </div>
          <div class="receipt-divider"></div>
          <div class="receipt-row">
            <span class="r-label">所配镜片</span>
            <span class="r-value highlight">{{ currentDetail.data.lensBrand || '-' }} {{ currentDetail.data.lensParams || '-' }}</span>
          </div>
          <div class="receipt-row">
            <span class="r-label">镜片金额</span>
            <span class="r-value">￥{{ currentDetail.data.lensPrice }}</span>
          </div>
        </div>
        <div class="receipt-footer">
          <span class="total-label">总计收讫</span>
          <span class="total-amount">￥{{ currentDetail.data.totalAmount }}</span>
        </div>
      </div>
    </el-dialog>

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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';
import { Back, DocumentAdd, ShoppingCart, View, Goods, Download, Edit, Delete, Close } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const customerId = route.params.id;

const customerInfo = reactive<any>({});
const timelineData = ref<any>([]);

// 详细记录弹窗
const detailVisible = ref(false);
const drawerTitle = ref('');
const currentDetail = ref<any>(null);

// 验光表单
const optoDialogVisible = ref(false);
const optoForm = reactive<any>({ 
  id: null,
  customerId,
  odSph:'', odCyl:'', odAxis:'', odVa:'',
  osSph:'', osCyl:'', osAxis:'', osVa:'',
  odPd:'', osPd:'', pdFar:'', pdNear:'', addPower:'',
  optometristName: ''
});

// 配镜表单
const salesDialogVisible = ref(false);
const salesForm = reactive<any>({ 
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
    const res = await request.get('/customer/' + customerId) as any;
    Object.assign(customerInfo, res || {});
  } catch(e) {}
}

const loadTimeline = async () => {
  try {
    timelineData.value = await request.get('/archive/' + customerId) as any;
  } catch (e) {}
}

const viewDetail = (item: any) => {
  currentDetail.value = item;
  drawerTitle.value = item.title;
  detailVisible.value = true;
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
  padding: 10px 20px 20px;
  position: relative;
}

.animate-fade-up {
  animation: fadeUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}

.receipt-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 20px;
}
.receipt-badge.primary { background: #e0e7ff; color: #4338ca; }
.receipt-badge.success { background: #dcfce7; color: #166534; }

.table-container {
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.elegant-table {
  width: 100%;
  border-collapse: collapse;
  text-align: center;
  background: #fafafa;
}

.elegant-table th {
  background-color: var(--primary-color);
  color: white;
  font-weight: 500;
  padding: 12px 6px;
  font-size: 13px;
}

.elegant-table td {
  padding: 14px 6px;
  border-top: 1px solid var(--border-color);
  background: white;
}

.eye-label { font-weight: 600; color: #4b5563; font-size: 13px; }
.numbold { font-weight: 700; color: var(--primary-color); font-size: 15px; }

.extra-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.info-tag {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-tag span { color: #6b7280; font-size: 12px; }
.info-tag strong { color: #1f2937; font-size: 14px; }

/* 小票式的流线设计 */
.receipt-body {
  background: #f9fafb;
  border-radius: 12px;
  padding: 24px;
}

.receipt-row {
  display: flex;
  justify-content: space-between;
  line-height: 2;
  font-size: 15px;
}

.receipt-divider {
  border-top: 2px dashed #d1d5db;
  margin: 16px 0;
}

.r-label { color: #6b7280; }
.r-value { color: #111827; font-weight: 500; }
.r-value.code { font-family: monospace; letter-spacing: 1px; color: var(--primary-color); }
.r-value.highlight { font-weight: 600; }

.receipt-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}
.total-label { font-size: 16px; font-weight: 600; color: #374151; }
.total-amount { font-size: 28px; font-weight: 800; color: #ff6b6b; padding-right: 12px; }

:deep(.elegant-dialog) {
  border-radius: 20px !important;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;
}

:deep(.elegant-dialog .el-dialog__header) {
  display: none !important; /* hide default header to use custom slot */
}

.dialog-custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0;
}

.dialog-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.close-btn {
  font-size: 22px;
  color: #9ca3af;
  transition: transform 0.3s;
}

.close-btn:hover {
  transform: rotate(90deg);
  color: #ef4444;
}
</style>

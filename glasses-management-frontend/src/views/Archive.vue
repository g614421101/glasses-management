<template>
  <div class="page-shell archive-page">
    <section class="page-hero glass-card archive-hero">
      <div class="hero-copy">
        <div class="hero-breadcrumb">
          <el-button link class="back-link" @click="router.back()">
            <el-icon><Back /></el-icon>返回
          </el-button>
          <span class="breadcrumb-divider"></span>
          <span>顾客档案中心</span>
        </div>

        <h1 class="page-heading">顾客档案</h1>
        <p class="page-title-en">Customer Archive</p>
      </div>

      <div class="page-toolbar hero-actions">
        <el-button type="primary" @click="openOptometryDialog">
          <el-icon><DocumentAdd /></el-icon>录入验光单
        </el-button>
        <el-button class="action-pill" @click="openSalesDialog">
          <el-icon><ShoppingCart /></el-icon>我要开单
        </el-button>
        <el-button class="action-pill" @click="exportExcel">
          <el-icon><Download /></el-icon>导出 Excel
        </el-button>
        <el-button type="danger" plain @click="deleteCustomer">
          <el-icon><Delete /></el-icon>删除档案
        </el-button>
      </div>
    </section>

    <div class="archive-content">
      <aside class="info-card glass-card">
        <div class="info-hero">
          <div class="avatar-wrap">
            <div class="avatar">{{ customerInfo.name ? customerInfo.name.charAt(0) : '客' }}</div>
          </div>
          <h3 class="c-name">{{ customerInfo.name || '-' }}</h3>
          <p class="c-phone">{{ customerInfo.phone || '-' }}</p>
        </div>

        <div class="customer-summary">
          <div class="summary-chip">
            <span>档案条目</span>
            <strong>{{ timelineData.length }}</strong>
          </div>
          <div class="summary-chip">
            <span>最新记录</span>
            <strong>{{ latestRecordLabel }}</strong>
          </div>
        </div>

        <div class="base-info">
          <el-descriptions :column="1" border size="small" class="customer-meta">
            <el-descriptions-item label="性别">{{ customerInfo.gender === 1 ? '男' : (customerInfo.gender === 2 ? '女' : '未知') }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ customerInfo.birthday || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ customerInfo.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </aside>

      <section class="timeline-card glass-card">
        <div class="timeline-head">
          <div>
            <h3 class="section-title">档案记录</h3>
            <p class="timeline-tip">按时间查看顾客的验光和配镜流转，支持预览、编辑、打印和删除。</p>
          </div>
          <div class="timeline-count">{{ timelineData.length }} 条记录</div>
        </div>

        <el-empty v-if="timelineData.length === 0" description="暂无历史记录" />

        <el-timeline v-else class="custom-timeline">
          <el-timeline-item
            v-for="(item, index) in timelineData"
            :key="index"
            :timestamp="item.date"
            placement="top"
            :type="item.type === 'OPTOMETRY' ? 'primary' : 'success'"
            :icon="item.type === 'OPTOMETRY' ? View : Goods"
          >
            <div
              class="timeline-detail-card"
              :class="item.type === 'OPTOMETRY' ? 'is-optometry' : 'is-sales'"
              @click="viewDetail(item)"
            >
              <div class="card-glow"></div>

              <div class="record-top">
                <div class="record-title-wrap">
                  <span class="record-badge">{{ item.type === 'OPTOMETRY' ? '验光单' : '配镜单' }}</span>
                  <h4>{{ item.title }}</h4>
                  <p class="subtitle">{{ item.subtitle }}</p>
                </div>
                <div v-if="item.type === 'SALES'" class="record-amount">
                  ￥{{ item.data.totalAmount || '0.00' }}
                  <span v-if="(item.data.frameQuantity && item.data.frameQuantity > 1) || (item.data.lensQuantity && item.data.lensQuantity > 1)" class="qty-badge">
                    <template v-if="item.data.frameQuantity > 1">架×{{ item.data.frameQuantity }}</template>
                    <template v-if="item.data.frameQuantity > 1 && item.data.lensQuantity > 1"> </template>
                    <template v-if="item.data.lensQuantity > 1">片×{{ item.data.lensQuantity }}</template>
                  </span>
                </div>
              </div>

              <div class="record-body">
                <!-- Optometry Record Glance -->
                <template v-if="item.type === 'OPTOMETRY'">
                  <!-- Mobile View: compact table layout -->
                  <div v-if="isMobile">
                    <div class="mini-optometry-table">
                      <div class="table-header-row">
                        <div class="cell title-cell"></div>
                        <div class="cell">球镜(SPH)</div>
                        <div class="cell">柱镜(CYL)</div>
                        <div class="cell">轴位(AXIS)</div>
                        <div class="cell">视力(VA)</div>
                      </div>
                      <div class="table-body-row">
                        <div class="cell title-cell label-od">右眼</div>
                        <div class="cell spherical">{{ fmt(item.data.odSph) }}</div>
                        <div class="cell cylinder">{{ fmt(item.data.odCyl) }}</div>
                        <div class="cell axis">{{ item.data.odAxis || '-' }}</div>
                        <div class="cell va">{{ item.data.odVa || '-' }}</div>
                      </div>
                      <div class="table-body-row">
                        <div class="cell title-cell label-os">左眼</div>
                        <div class="cell spherical">{{ fmt(item.data.osSph) }}</div>
                        <div class="cell cylinder">{{ fmt(item.data.osCyl) }}</div>
                        <div class="cell axis">{{ item.data.osAxis || '-' }}</div>
                        <div class="cell va">{{ item.data.osVa || '-' }}</div>
                      </div>
                    </div>
                    
                    <div class="optometry-footer">
                      <span class="optometrist-tag" v-if="item.data.optometristName">
                        <el-icon><User /></el-icon>
                        验光师: <strong>{{ item.data.optometristName }}</strong>
                      </span>
                      <span class="pd-tag" v-if="item.data.pd">
                        瞳距: <strong>{{ item.data.pd }} mm</strong>
                      </span>
                      <span class="add-tag" v-if="item.data.addSph">
                        下加光: <strong>{{ fmt(item.data.addSph) }}</strong>
                      </span>
                    </div>
                  </div>
                  
                  <!-- Desktop View: original columns layout -->
                  <div class="record-glance" v-else>
                    <div class="glance-item">
                      <span>右眼</span>
                      <strong>{{ fmt(item.data.odSph) }} / {{ fmt(item.data.odCyl) }}</strong>
                    </div>
                    <div class="glance-item">
                      <span>左眼</span>
                      <strong>{{ fmt(item.data.osSph) }} / {{ fmt(item.data.osCyl) }}</strong>
                    </div>
                    <div class="glance-item">
                      <span>验光师</span>
                      <strong>{{ item.data.optometristName || '-' }}</strong>
                    </div>
                  </div>
                </template>

                <!-- Sales Record Glance -->
                <template v-else>
                  <!-- Mobile View: product items layout -->
                  <div v-if="isMobile">
                    <div class="sales-product-glance">
                      <div class="product-item" v-if="item.data.frameBrand || item.data.frameModel">
                        <div class="p-icon"><el-icon><Grid /></el-icon></div>
                        <div class="p-info">
                          <span class="p-label">镜架</span>
                          <strong class="p-val" :title="item.data.frameBrand + ' ' + (item.data.frameModel || '')">
                            {{ item.data.frameBrand || '-' }}
                            <small v-if="item.data.frameModel">{{ item.data.frameModel }}</small>
                          </strong>
                        </div>
                      </div>
                      
                      <div class="product-item" v-if="item.data.lensBrand || item.data.lensModel">
                        <div class="p-icon"><el-icon><Search /></el-icon></div>
                        <div class="p-info">
                          <span class="p-label">镜片</span>
                          <strong class="p-val" :title="item.data.lensBrand + ' ' + (item.data.lensModel || '')">
                            {{ item.data.lensBrand || '-' }}
                            <small v-if="item.data.lensModel">{{ item.data.lensModel }}</small>
                          </strong>
                        </div>
                      </div>
                    </div>

                    <div class="sales-footer">
                      <span class="sales-no-tag" v-if="item.data.recordNo">
                        单号: <code class="sales-no">{{ item.data.recordNo }}</code>
                      </span>
                      <span class="operator-tag" v-if="item.data.operatorName">
                        经手人: <strong>{{ item.data.operatorName }}</strong>
                      </span>
                    </div>
                  </div>

                  <!-- Desktop View: original columns layout -->
                  <div class="record-glance" v-else>
                    <div class="glance-item">
                      <span>镜架</span>
                      <strong>{{ item.data.frameBrand || '-' }}</strong>
                    </div>
                    <div class="glance-item">
                      <span>镜片</span>
                      <strong>{{ item.data.lensBrand || '-' }}</strong>
                    </div>
                    <div class="glance-item glance-item--record-no">
                      <span>单号</span>
                      <strong class="record-no-text" :title="item.data.recordNo || '-'">{{ item.data.recordNo || '-' }}</strong>
                    </div>
                  </div>
                </template>
              </div>

              <div class="action-bar" @click.stop>
                <el-button
                  v-if="item.type === 'SALES'"
                  class="action-pill"
                  size="small"
                  @click="printRecord(item.data.id)"
                >
                  预览处方
                </el-button>
                <el-button class="action-pill" size="small" @click="openEdit(item)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button class="action-pill action-pill--danger" size="small" @click="handleDelete(item)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </section>
    </div>

    <el-dialog
      v-model="detailVisible"
      width="min(92vw, 840px)"
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

      <div v-if="currentDetail && currentDetail.type === 'OPTOMETRY'" class="detail-box animate-fade-up optometry-sheet">
        <div class="sheet-hero">
          <div>
            <div class="receipt-badge primary">验光记录</div>
            <h3 class="sheet-title">双眼屈光参数总览</h3>
          </div>
          <div class="sheet-halo"></div>
        </div>

        <div class="eye-card-grid">
          <div class="eye-card">
            <div class="eye-card-head">
              <span>右眼</span>
              <strong>OD</strong>
            </div>
            <div class="eye-metric-grid">
              <div class="eye-metric"><span>球镜</span><strong>{{ fmt(currentDetail.data.odSph) }}</strong></div>
              <div class="eye-metric"><span>柱镜</span><strong>{{ fmt(currentDetail.data.odCyl) }}</strong></div>
              <div class="eye-metric"><span>轴位</span><strong>{{ currentDetail.data.odAxis || '-' }}</strong></div>
              <div class="eye-metric"><span>视力</span><strong>{{ currentDetail.data.odVa || '-' }}</strong></div>
            </div>
          </div>

          <div class="eye-card">
            <div class="eye-card-head">
              <span>左眼</span>
              <strong>OS</strong>
            </div>
            <div class="eye-metric-grid">
              <div class="eye-metric"><span>球镜</span><strong>{{ fmt(currentDetail.data.osSph) }}</strong></div>
              <div class="eye-metric"><span>柱镜</span><strong>{{ fmt(currentDetail.data.osCyl) }}</strong></div>
              <div class="eye-metric"><span>轴位</span><strong>{{ currentDetail.data.osAxis || '-' }}</strong></div>
              <div class="eye-metric"><span>视力</span><strong>{{ currentDetail.data.osVa || '-' }}</strong></div>
            </div>
          </div>
        </div>

        <div class="extra-grid">
          <div class="info-tag"><span>右眼瞳距</span><strong>{{ currentDetail.data.odPd || '-' }}</strong></div>
          <div class="info-tag"><span>左眼瞳距</span><strong>{{ currentDetail.data.osPd || '-' }}</strong></div>
          <div class="info-tag"><span>远用瞳距</span><strong>{{ currentDetail.data.pdFar || '-' }}</strong></div>
          <div class="info-tag"><span>近用瞳距</span><strong>{{ currentDetail.data.pdNear || '-' }}</strong></div>
          <div class="info-tag"><span>下加光</span><strong>{{ fmt(currentDetail.data.addPower) }}</strong></div>
          <div class="info-tag"><span>验光师</span><strong>{{ currentDetail.data.optometristName || '-' }}</strong></div>
        </div>

        <div v-if="currentDetail.data.remark" style="margin-top: 16px; padding: 12px; background: var(--surface-muted); border-radius: 12px; font-size: 13px; line-height: 1.5;">
          <span style="color: var(--text-secondary); margin-right: 6px;">备注:</span>
          <span style="color: var(--text-primary);">{{ currentDetail.data.remark }}</span>
        </div>
      </div>
      
      <div v-else-if="currentDetail && currentDetail.type === 'SALES'" class="detail-box animate-fade-up sales-sheet">
        <div class="sheet-hero">
          <div>
            <div class="receipt-badge success">配镜信息</div>
            <h3 class="sheet-title">订单 {{ currentDetail.data.recordNo }}</h3>
          </div>
          <div class="total-bubble">
            <small>
              <del v-if="currentDetail.data.totalRetailPrice > 0" style="margin-right:4px; opacity:0.7;">￥{{ currentDetail.data.totalRetailPrice }}</del>
              总计收讫
            </small>
            <strong>￥{{ currentDetail.data.totalAmount }}</strong>
            <span v-if="currentDetail.data.totalRetailPrice > 0" style="display:block; font-size:12px; margin-top:2px; text-align:right; font-weight:normal; opacity:0.9;">
              {{ ((currentDetail.data.totalAmount / currentDetail.data.totalRetailPrice) * 10).toFixed(1) }}折
            </span>
          </div>
        </div>

        <div class="sales-card-grid">
          <div class="product-card">
            <div class="product-card-head">
              <span class="product-badge">镜架</span>
              <div>
                <del v-if="currentDetail.data.frameRetailPrice > 0" style="color:#999; font-size:12px; margin-right:6px; font-weight:normal;">￥{{ currentDetail.data.frameRetailPrice }}</del>
                <strong>￥{{ currentDetail.data.framePrice || 0 }}</strong>
                <span v-if="currentDetail.data.frameQuantity && currentDetail.data.frameQuantity > 1" style="margin-left:6px; font-size:12px; color:var(--primary-color); font-weight:700;">×{{ currentDetail.data.frameQuantity }}</span>
              </div>
            </div>
            <h4>{{ currentDetail.data.frameBrand || '-' }}</h4>
            <p>{{ currentDetail.data.frameModel || '未填写型号' }}</p>
          </div>

          <div class="product-card">
            <div class="product-card-head">
              <span class="product-badge product-badge--soft">镜片</span>
              <div>
                <del v-if="currentDetail.data.lensRetailPrice > 0" style="color:#999; font-size:12px; margin-right:6px; font-weight:normal;">￥{{ currentDetail.data.lensRetailPrice }}</del>
                <strong>￥{{ currentDetail.data.lensPrice || 0 }}</strong>
                <span v-if="currentDetail.data.lensQuantity && currentDetail.data.lensQuantity > 1" style="margin-left:6px; font-size:12px; color:var(--primary-color); font-weight:700;">×{{ currentDetail.data.lensQuantity }}</span>
              </div>
            </div>
            <h4>{{ currentDetail.data.lensBrand || '-' }}</h4>
            <p>{{ currentDetail.data.lensParams || '未填写参数' }}</p>
          </div>
        </div>

        <div v-if="currentDetail.data.remark" style="margin-top: 16px; padding: 12px; background: var(--surface-muted); border-radius: 12px; font-size: 13px; line-height: 1.5;">
          <span style="color: var(--text-secondary); margin-right: 6px;">备注:</span>
          <span style="color: var(--text-primary);">{{ currentDetail.data.remark }}</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog :title="optoForm.id ? '编辑验光单' : '录入验光单'" v-model="optoDialogVisible" width="min(94vw, 720px)">
      <el-form :model="optoForm" label-width="100px" label-position="left" class="record-form">
        <h4 class="form-block-title">右眼 (OD)</h4>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="球镜" label-width="56px"><el-input v-model="optoForm.odSph" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="柱镜" label-width="56px"><el-input v-model="optoForm.odCyl" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="轴位" label-width="56px"><el-input v-model="optoForm.odAxis" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="视力" label-width="56px"><el-input v-model="optoForm.odVa" /></el-form-item></el-col>
        </el-row>

        <h4 class="form-block-title">左眼 (OS)</h4>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="球镜" label-width="56px"><el-input v-model="optoForm.osSph" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="柱镜" label-width="56px"><el-input v-model="optoForm.osCyl" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="轴位" label-width="56px"><el-input v-model="optoForm.osAxis" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="视力" label-width="56px"><el-input v-model="optoForm.osVa" /></el-form-item></el-col>
        </el-row>

        <el-divider />

        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="右眼瞳距" label-width="84px"><el-input v-model="optoForm.odPd" @input="calcPdTotal" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="左眼瞳距" label-width="84px"><el-input v-model="optoForm.osPd" @input="calcPdTotal" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="远用瞳距" label-width="84px"><el-input v-model="optoForm.pdFar" /></el-form-item></el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="近用瞳距" label-width="84px"><el-input v-model="optoForm.pdNear" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="下加光" label-width="70px"><el-input v-model="optoForm.addPower" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="验光师" label-width="68px"><el-input v-model="optoForm.optometristName" placeholder="默认操作人" /></el-form-item></el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" label-width="60px">
              <el-input type="textarea" v-model="optoForm.remark" placeholder="选填，记录眼部情况或其他补充信息..." />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="optoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="optoSubmitting" @click="submitOpto">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="salesForm.id ? '编辑配镜单' : '新建配镜单'" v-model="salesDialogVisible" width="min(94vw, 800px)">
      <el-form :model="salesForm" label-width="80px" label-position="left" class="record-form">
        <el-row>
          <el-col :span="24">
            <el-form-item label="关联验光" label-width="80px">
              <el-select v-model="salesForm.optometryId" placeholder="选择验光记录(可选)" clearable style="width: 100%">
                <el-option
                  v-for="item in timelineData.filter(i => i.type === 'OPTOMETRY')"
                  :key="item.data.id"
                  :label="item.date + ' 验光单'"
                  :value="item.data.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <h4 class="form-block-title" style="margin-top: 8px;">镜架信息</h4>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="镜架品牌">
              <el-input v-model="salesForm.frameBrand" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="镜架型号">
              <el-input v-model="salesForm.frameModel" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="8">
            <el-form-item label="数量">
              <el-input-number v-model="salesForm.frameQuantity" :min="1" :step="1" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="零售单价">
              <el-input-number v-model="salesForm.frameRetailPrice" :precision="2" :step="10" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="实际售价">
              <el-input-number v-model="salesForm.framePrice" :precision="2" :step="10" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider style="margin: 16px 0;" />

        <h4 class="form-block-title">镜片信息</h4>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12">
            <el-form-item label="镜片品牌">
              <el-input v-model="salesForm.lensBrand" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="镜片参数">
              <el-input v-model="salesForm.lensParams" placeholder="例: 1.60 防蓝光" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="8">
            <el-form-item label="数量">
              <el-input-number v-model="salesForm.lensQuantity" :min="1" :step="1" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="零售单价">
              <el-input-number v-model="salesForm.lensRetailPrice" :precision="2" :step="10" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="实际售价">
              <el-input-number v-model="salesForm.lensPrice" :precision="2" :step="10" @change="calcTotal" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider style="margin: 16px 0;" />

        <h4 class="form-block-title">结算与备注</h4>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="8">
            <el-form-item label="零售总价">
              <el-input-number v-model="salesForm.totalRetailPrice" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="实收总价">
              <el-input-number v-model="salesForm.totalAmount" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="折扣">
              <el-input :value="computedDiscount" readonly placeholder="自动计算" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input type="textarea" v-model="salesForm.remark" :rows="2" placeholder="选填，如：送隐形眼镜盒..." />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="salesDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="salesSubmitting" @click="submitSales">确认开单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request, { openBlob, downloadBlob } from '../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Back,
  DocumentAdd,
  ShoppingCart,
  View,
  Goods,
  Download,
  Edit,
  Delete,
  Close,
  User,
  Grid
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const customerId = route.params.id;

const customerInfo = reactive<any>({});
const timelineData = ref<any>([]);

const isMobile = ref(window.matchMedia('(max-width: 900px)').matches);
let mediaQuery: MediaQueryList | null = null;
let mediaHandler: ((e: MediaQueryListEvent) => void) | null = null;

const detailVisible = ref(false);
const drawerTitle = ref('');
const currentDetail = ref<any>(null);

const optoDialogVisible = ref(false);
const optoSubmitting = ref(false);
const optoForm = reactive<any>({
  id: null,
  customerId,
  odSph: '', odCyl: '', odAxis: '', odVa: '',
  osSph: '', osCyl: '', osAxis: '', osVa: '',
  odPd: '', osPd: '', pdFar: '', pdNear: '', addPower: '',
  optometristName: '',
  remark: ''
});

const salesDialogVisible = ref(false);
const salesSubmitting = ref(false);
const salesForm = reactive<any>({
  id: null,
  customerId,
  optometryId: null,
  frameBrand: '',
  frameModel: '',
  frameQuantity: 1,
  frameRetailPrice: 0,
  framePrice: 0,
  lensBrand: '',
  lensParams: '',
  lensQuantity: 1,
  lensRetailPrice: 0,
  lensPrice: 0,
  totalRetailPrice: 0,
  totalAmount: 0,
  remark: ''
});

const computedDiscount = computed(() => {
  const retail = salesForm.totalRetailPrice || 0;
  const actual = salesForm.totalAmount || 0;
  if (retail <= 0) return '-';
  const discount = (actual / retail) * 10;
  return discount.toFixed(1) + '折';
});

const latestRecordLabel = computed(() => {
  if (!timelineData.value.length) return '暂无';
  return timelineData.value[0].type === 'OPTOMETRY' ? '验光' : '配镜';
});

onMounted(() => {
  loadCustomer();
  loadTimeline();
  mediaQuery = window.matchMedia('(max-width: 900px)');
  mediaHandler = (e: MediaQueryListEvent) => { isMobile.value = e.matches; };
  mediaQuery.addEventListener('change', mediaHandler);
});

onUnmounted(() => {
  if (mediaQuery && mediaHandler) {
    mediaQuery.removeEventListener('change', mediaHandler);
  }
});

const loadCustomer = async () => {
  try {
    const res = await request.get('/customer/' + customerId) as any;
    Object.assign(customerInfo, res || {});
  } catch (e) {}
};

const loadTimeline = async () => {
  try {
    timelineData.value = await request.get('/archive/' + customerId) as any;
  } catch (e) {}
};

const viewDetail = (item: any) => {
  currentDetail.value = item;
  drawerTitle.value = item.title;
  detailVisible.value = true;
};

const resetOpto = () => {
  Object.assign(optoForm, {
    id: null,
    customerId,
    odSph: '', odCyl: '', odAxis: '', odVa: '',
    osSph: '', osCyl: '', osAxis: '', osVa: '',
    odPd: '', osPd: '', pdFar: '', pdNear: '', addPower: '',
    optometristName: '', remark: ''
  });
};

const calcPdTotal = () => {
  const od = parseFloat(optoForm.odPd) || 0;
  const os = parseFloat(optoForm.osPd) || 0;
  if (od > 0 && os > 0) {
    optoForm.pdFar = (od + os).toFixed(1);
  }
};

const openOptometryDialog = () => {
  resetOpto();
  optoDialogVisible.value = true;
};

const submitOpto = async () => {
  if (optoSubmitting.value) return;
  optoSubmitting.value = true;
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
  } catch (e) {} finally {
    optoSubmitting.value = false;
  }
};

const resetSales = () => {
  Object.assign(salesForm, {
    id: null,
    customerId,
    optometryId: null,
    frameBrand: '',
    frameModel: '',
    frameQuantity: 1,
    frameRetailPrice: 0,
    framePrice: 0,
    lensBrand: '',
    lensParams: '',
    lensQuantity: 1,
    lensRetailPrice: 0,
    lensPrice: 0,
    totalRetailPrice: 0,
    totalAmount: 0,
    remark: ''
  });
};

const calcTotal = () => {
  const fq = salesForm.frameQuantity || 1;
  const lq = salesForm.lensQuantity || 1;
  salesForm.totalRetailPrice = (salesForm.frameRetailPrice || 0) * fq + (salesForm.lensRetailPrice || 0) * lq;
  salesForm.totalAmount = (salesForm.framePrice || 0) * fq + (salesForm.lensPrice || 0) * lq;
};

const openSalesDialog = () => {
  resetSales();
  salesDialogVisible.value = true;
};

const submitSales = async () => {
  if (salesSubmitting.value) return;
  salesSubmitting.value = true;
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
  } catch (e) {} finally {
    salesSubmitting.value = false;
  }
};

const openEdit = (item) => {
  if (item.type === 'OPTOMETRY') {
    Object.assign(optoForm, item.data);
    optoForm.odSph = fmtInput(optoForm.odSph);
    optoForm.odCyl = fmtInput(optoForm.odCyl);
    optoForm.osSph = fmtInput(optoForm.osSph);
    optoForm.osCyl = fmtInput(optoForm.osCyl);
    optoForm.addPower = fmtInput(optoForm.addPower);
    optoDialogVisible.value = true;
  } else {
    Object.assign(salesForm, item.data);
    salesDialogVisible.value = true;
  }
};

const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    const url = item.type === 'OPTOMETRY' ? '/optometry/' : '/sales/';
    await request.delete(url + item.data.id);
    ElMessage.success('删除成功');
    loadTimeline();
  });
};

const printRecord = async (id) => {
  try {
    await openBlob(`/print/prescription/${id}`);
  } catch {
    // request helper already shows the user-facing error.
  }
};

const exportExcel = async () => {
  try {
    await downloadBlob(`/print/export/customer/${customerId}`, 'customer-records.xlsx');
  } catch {
    // request helper already shows the user-facing error.
  }
};

const deleteCustomer = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该顾客及其所有验光单、配镜单吗？\n删除后数据将移入回收站。', '危险操作', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    });
    await request.delete(`/customer/${customerId}`);
    ElMessage.success('已删除并移入回收站');
    router.push('/customer');
  } catch {
    // cancel or request error
  }
};

const fmt = (val) => {
  if (val === undefined || val === null || val === '') return '-';
  const num = parseFloat(val);
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
};

const fmtInput = (val) => {
  if (val === undefined || val === null || val === '') return '';
  const num = parseFloat(val);
  if (isNaN(num)) return '';
  return num > 0 ? '+' + num.toFixed(2) : num.toFixed(2);
};
</script>

<style scoped>
.archive-page {
  padding-top: 6px;
}

.archive-hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
}

.hero-breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 700;
}

.back-link {
  padding: 0 !important;
  color: var(--primary-color) !important;
  font-weight: 700 !important;
}

.breadcrumb-divider {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.35);
}

.hero-actions {
  position: relative;
  z-index: 1;
}

.archive-content {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.info-card {
  padding: 24px;
  position: sticky;
  top: 102px;
}

:root[data-theme='dark'] .info-card::before {
  opacity: 0;
}

.info-hero {
  text-align: center;
}

.avatar-wrap {
  width: 88px;
  height: 88px;
  margin: 0 auto 18px;
  padding: 4px;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.18) 0%, rgba(56, 189, 248, 0.24) 100%);
  box-shadow: 0 20px 36px rgba(37, 99, 235, 0.14);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d4ed8 0%, #38bdf8 100%);
  font-size: 34px;
  color: #ffffff;
  font-weight: 800;
}

.c-name {
  margin: 0 0 8px;
  font-size: 24px;
}

.c-phone {
  margin: 0;
  color: var(--text-secondary);
}

.customer-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
  margin-bottom: 26px;
}

.summary-chip {
  padding: 14px;
  border-radius: 18px;
  background: var(--primary-soft);
  border: 1px solid var(--border-strong);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.summary-chip span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.summary-chip strong {
  color: var(--text-primary);
  font-size: 20px;
}

:root[data-theme='dark'] .summary-chip {
  background: linear-gradient(145deg, rgba(30, 64, 115, 0.82) 0%, rgba(15, 23, 42, 0.9) 100%);
  border-color: rgba(96, 165, 250, 0.38);
  box-shadow: inset 0 1px 0 rgba(125, 211, 252, 0.12), 0 14px 30px rgba(2, 6, 23, 0.24);
}

:root[data-theme='dark'] .summary-chip span {
  color: #bfdbfe;
}

:root[data-theme='dark'] .summary-chip strong {
  color: #f8fbff;
  text-shadow: 0 0 18px rgba(96, 165, 250, 0.22);
}

.base-info {
  margin-top: 0;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

:root[data-theme='dark'] .customer-meta :deep(.el-descriptions__cell),
:root[data-theme='dark'] .customer-meta :deep(.el-descriptions__label),
:root[data-theme='dark'] .customer-meta :deep(.el-descriptions__content) {
  background: transparent !important;
  border-color: rgba(148, 163, 184, 0.36) !important;
  color: var(--text-primary) !important;
}

:root[data-theme='dark'] .customer-meta :deep(.el-descriptions__label) {
  color: #c7d7ec !important;
}

.timeline-card {
  padding: 26px;
  min-height: 520px;
}

.timeline-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 22px;
}

.timeline-tip {
  margin: 10px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.timeline-count {
  padding: 12px 16px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.custom-timeline :deep(.el-timeline-item__node--primary) {
  box-shadow: 0 0 0 6px rgba(219, 234, 254, 0.7);
}

.timeline-detail-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 24px;
  border: 1px solid var(--border-color);
  background: var(--surface-overlay);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  cursor: pointer;
}

.timeline-detail-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 48px rgba(37, 99, 235, 0.14);
}

.card-glow {
  position: absolute;
  inset: auto -40px -60px auto;
  width: 180px;
  height: 180px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.14), rgba(37, 99, 235, 0));
  pointer-events: none;
}

.timeline-detail-card.is-sales .card-glow {
  background: radial-gradient(circle, rgba(56, 189, 248, 0.14), rgba(56, 189, 248, 0));
}

.record-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.record-title-wrap {
  min-width: 0;
  flex: 1;
}

.record-title-wrap h4 {
  margin: 10px 0 6px;
  font-size: 20px;
}

.record-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 800;
}

.timeline-detail-card.is-sales .record-badge {
  background: var(--surface-muted);
  color: var(--primary-color);
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.record-amount {
  padding: 10px 14px;
  border-radius: 18px;
  background: var(--gradient-soft);
  color: var(--primary-color);
  font-size: 18px;
  font-weight: 800;
  white-space: nowrap;
  flex-shrink: 0;
}

.qty-badge {
  display: inline-block;
  margin-left: 6px;
  font-size: 11px;
  font-weight: 700;
  opacity: 0.75;
}

.record-glance {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.glance-item {
  padding: 12px 14px;
  border-radius: 18px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
  min-width: 0;
}

.glance-item span {
  display: block;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

.glance-item strong {
  display: block;
  color: var(--text-primary);
  font-size: 14px;
  min-width: 0;
}

.glance-item--record-no {
  align-self: stretch;
}

.record-no-text {
  font-size: 13px !important;
  line-height: 1.4;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.record-body {
  margin-top: 14px;
}

.mini-optometry-table {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow-x: auto;
  margin-top: 8px;
  box-shadow: inset 0 2px 8px rgba(15, 23, 42, 0.03);
}

.table-header-row, .table-body-row {
  display: grid;
  grid-template-columns: 52px repeat(4, 1fr);
  width: 100%;
  border-bottom: 1px solid var(--border-color);
}

.table-body-row:last-child {
  border-bottom: none;
}

.mini-optometry-table .cell {
  width: 100%;
  height: 100%;
  padding: 8px 2px;
  font-size: 12px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  min-width: 0;
  font-weight: 700;
  border-right: 1px solid var(--border-color);
  box-sizing: border-box;
}

.mini-optometry-table .cell:last-child {
  border-right: none;
}

.mini-optometry-table .table-header-row .cell {
  color: var(--text-muted);
  font-size: 11px;
  font-weight: 800;
  background: rgba(148, 163, 184, 0.05);
}

.mini-optometry-table .title-cell {
  font-weight: 800;
  font-size: 11px;
  background: rgba(148, 163, 184, 0.08);
  color: var(--text-secondary);
}

.mini-optometry-table .label-od {
  color: var(--primary-color) !important;
}

.mini-optometry-table .label-os {
  color: var(--el-color-success) !important;
}

.sales-product-glance {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  min-width: 0;
}

.product-item .p-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 15px;
  flex-shrink: 0;
}

.timeline-detail-card.is-sales .product-item .p-icon {
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
}

.product-item .p-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.product-item .p-label {
  font-size: 11px;
  color: var(--text-muted);
}

.product-item .p-val {
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-item .p-val small {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: normal;
  margin-left: 4px;
}

.sales-footer, .optometry-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
  font-size: 12px;
  color: var(--text-secondary);
}

.optometrist-tag, .pd-tag, .add-tag, .sales-no-tag, .operator-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.sales-no {
  font-family: Consolas, Monaco, monospace;
  background: var(--surface-muted);
  padding: 2px 6px;
  border-radius: 6px;
  color: var(--primary-color);
  font-size: 11px;
  border: 1px solid var(--border-color);
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 18px;
}

.detail-box {
  padding: 10px 10px 18px;
}

.animate-fade-up {
  animation: fadeUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}

.sheet-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  padding: 20px;
  border-radius: 24px;
  background: var(--gradient-soft);
  border: 1px solid var(--border-strong);
  margin-bottom: 18px;
}

.sheet-title {
  margin: 10px 0 6px;
  font-size: 26px;
  line-height: 1.15;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.sheet-halo {
  width: 82px;
  height: 82px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.26), rgba(37, 99, 235, 0));
}

.total-bubble {
  min-width: 180px;
  padding: 14px 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, #1d4ed8 0%, #38bdf8 100%);
  color: #ffffff;
  box-shadow: 0 18px 36px rgba(37, 99, 235, 0.24);
  white-space: nowrap;
}

.total-bubble small {
  display: block;
  opacity: 0.8;
  margin-bottom: 6px;
}

.total-bubble strong {
  font-size: 24px;
}

.eye-card-grid,
.sales-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.eye-card,
.product-card {
  padding: 18px;
  border-radius: 22px;
  background: var(--surface-overlay);
  border: 1px solid var(--border-color);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.06);
}

.eye-card-head,
.product-card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.eye-card-head span,
.product-badge {
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 800;
}

.eye-card-head strong {
  font-size: 18px;
  color: var(--text-primary);
}

.eye-metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.eye-metric {
  padding: 12px;
  border-radius: 16px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
}

.eye-metric span,
.info-tag span,
.product-card p {
  color: var(--text-muted);
  font-size: 12px;
}

.eye-metric strong {
  display: block;
  margin-top: 8px;
  font-size: 18px;
  color: var(--text-primary);
}

.extra-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.info-tag {
  padding: 14px;
  border-radius: 18px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
}

.info-tag strong {
  display: block;
  margin-top: 8px;
  color: var(--text-primary);
}

.product-card h4 {
  margin: 0 0 8px;
  font-size: 22px;
  color: var(--text-primary);
}

.product-badge--soft {
  background: var(--surface-muted);
  color: var(--primary-color);
}

.receipt-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.receipt-badge.primary {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.receipt-badge.success {
  background: var(--surface-muted);
  color: var(--primary-color);
}

:deep(.elegant-dialog) {
  border-radius: 26px !important;
  overflow: hidden;
  box-shadow: 0 32px 70px rgba(15, 23, 42, 0.18) !important;
}

:deep(.elegant-dialog .el-dialog__header) {
  display: none !important;
}

.dialog-custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26px 26px 0;
}

.dialog-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
}

.close-btn {
  font-size: 22px;
  color: var(--text-muted);
}

.close-btn:hover {
  transform: rotate(90deg);
  color: var(--primary-color);
}

.record-form {
  padding-top: 8px;
}

.form-block-title {
  margin: 0 0 12px;
  color: var(--text-primary);
}

@media (max-width: 1100px) {
  .archive-content {
    grid-template-columns: 1fr;
  }

  .info-card {
    position: static;
  }
}

@media (max-width: 900px) {
  .archive-hero,
  .sheet-hero,
  .record-top,
  .timeline-head {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-actions {
    margin-top: 16px;
  }

  .eye-card-grid,
  .sales-card-grid {
    grid-template-columns: 1fr;
  }

  .extra-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-amount,
  .timeline-count {
    white-space: normal;
    width: fit-content;
  }
}

@media (max-width: 640px) {
  .info-card,
  .timeline-card {
    padding: 14px;
  }

  .timeline-detail-card {
    padding: 14px 12px;
    max-width: 100%;
    box-sizing: border-box;
  }

  .customer-summary {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    display: grid !important;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)) !important;
    gap: 10px !important;
    width: 100% !important;
    margin-top: 16px !important;
  }

  .hero-actions .el-button {
    margin: 0 !important;
    width: 100% !important;
    padding: 10px 8px !important;
    font-size: 13px !important;
    justify-content: center;
  }

  .custom-timeline {
    padding-left: 2px !important;
  }

  .custom-timeline :deep(.el-timeline-item__wrapper) {
    padding-left: 14px !important;
  }

  .action-bar {
    justify-content: flex-start;
    flex-wrap: nowrap !important;
    gap: 8px !important;
    overflow-x: auto;
  }

  .action-bar .el-button {
    padding: 6px 10px !important;
    font-size: 11px !important;
    margin: 0 !important;
    height: 28px !important;
  }
}
</style>

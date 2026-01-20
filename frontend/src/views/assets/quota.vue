<template>
  <div class="quota-container" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-info">
        <h1>碳配额管理</h1>
        <p>管理您的碳排放配额，追踪履约进度</p>
      </div>
      <div class="year-selector">
        <span class="selector-label">履约年度</span>
        <el-select v-model="currentYear" class="year-select">
          <el-option label="2025年" value="2025" />
          <el-option label="2024年" value="2024" />
          <el-option label="2023年" value="2023" />
        </el-select>
      </div>
    </div>

    <!-- 履约概览卡片 -->
    <div class="overview-card">
      <div class="overview-header">
        <div class="status-badge" :class="quotaData.status === 1 ? 'completed' : 'pending'">
          <el-icon v-if="quotaData.status === 1"><CircleCheck /></el-icon>
          <el-icon v-else><Warning /></el-icon>
          <span>{{ quotaData.status === 1 ? '履约完成' : '待履约' }}</span>
        </div>
        <el-button type="primary" plain size="small" icon="Download" @click="handleExportReport">导出报表</el-button>
      </div>
      
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-icon green">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ quotaData.totalQuota?.toLocaleString() || 0 }}</span>
            <span class="stat-label">发放总配额 <small>tCO2e</small></span>
          </div>
        </div>
        
        <div class="stat-divider"></div>
        
        <div class="stat-item">
          <div class="stat-icon orange">
            <el-icon><Smoking /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ quotaData.verifiedEmission?.toLocaleString() || 0 }}</span>
            <span class="stat-label">核查排放量 <small>tCO2e</small></span>
          </div>
        </div>
        
        <div class="stat-divider"></div>
        
        <div class="stat-item">
          <div class="stat-icon" :class="surplus >= 0 ? 'blue' : 'red'">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value" :class="{ 'positive': surplus >= 0, 'negative': surplus < 0 }">
              {{ surplus > 0 ? '+' : '' }}{{ surplus.toLocaleString() }}
            </span>
            <span class="stat-label">盈余配额 <small>tCO2e</small></span>
          </div>
        </div>
      </div>

      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">配额使用率</span>
          <span class="progress-value" :class="{ warning: usageRate > 80, danger: usageRate > 95 }">
            {{ usageRate }}%
          </span>
        </div>
        <div class="progress-bar-wrapper">
          <div class="progress-bar" :style="{ width: `${usageRate}%` }" :class="getProgressClass()"></div>
        </div>
        <div class="progress-tips">
          <span v-if="usageRate < 80">配额充足，可考虑出售多余配额</span>
          <span v-else-if="usageRate < 95" class="warning">配额使用率较高，请关注履约进度</span>
          <span v-else class="danger">配额紧张，建议尽快购入配额</span>
        </div>
      </div>
    </div>

    <!-- 数据可视化图表 -->
    <el-row :gutter="20" style="margin-bottom: 24px;">
      <el-col :span="12">
        <el-card shadow="hover" style="height: 300px;">
          <template #header>
            <div class="header-title">
              <el-icon><TrendCharts /></el-icon>
              <span>历年配额与排放趋势</span>
            </div>
          </template>
          <div id="quotaTrendChart" style="width: 100%; height: 250px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" style="height: 300px;">
          <template #header>
            <div class="header-title">
              <el-icon><PieChart /></el-icon>
              <span>配额使用分布</span>
            </div>
          </template>
          <div id="quotaUsageChart" style="width: 100%; height: 250px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 明细记录 -->
    <div class="detail-card">
      <div class="card-header">
        <div class="header-title">
          <el-icon><Document /></el-icon>
          <span>配额明细记录</span>
        </div>
        <el-input v-model="searchQuery" placeholder="搜索记录..." class="search-input" clearable @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append>
            <el-button @click="handleSearch" icon="Search" size="small" />
          </template>
        </el-input>
      </div>
      
      <el-table :data="tableData" class="custom-table">
        <el-table-column prop="date" label="变动日期" width="160">
          <template #default="scope">
            <div class="date-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ scope.row.date }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <span class="type-badge" :class="scope.row.type === '发放' ? 'issue' : 'change'">
              {{ scope.row.type }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="变动数量" width="150" align="right">
          <template #default="scope">
            <span class="amount-value" :class="scope.row.amount > 0 ? 'plus' : 'minus'">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount?.toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="结余" width="150" align="right">
          <template #default="scope">
            <span class="balance-value">{{ scope.row.balance?.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
      
      <div class="empty-state" v-if="tableData.length === 0">
        <el-empty description="暂无配额记录">
          <el-button type="primary" plain @click="handleApplyQuota">申请配额发放</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 申请配额发放对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请配额发放" width="600px">
      <el-form :model="applyForm" label-width="120px" :rules="applyRules">
        <el-form-item label="申请年份" prop="year">
          <el-input v-model="applyForm.year" disabled />
        </el-form-item>
        <el-form-item label="申请数量(t)" prop="applyAmount">
          <el-input-number v-model="applyForm.applyAmount" :min="0" :step="100" placeholder="请输入申请数量" />
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input v-model="applyForm.reason" type="textarea" placeholder="请详细说明申请原因" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApply">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { getQuota, getQuotaDetails } from '@/api/assets'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { CircleCheck, Warning, Coin, Smoking, TrendCharts, Document, Search, Calendar, Download, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const currentYear = ref('2025')
const quotaData = ref({
  totalQuota: 0,
  verifiedEmission: 0,
  status: 0
})

const loading = ref(false)
const tableData = ref([])
const userStore = useUserStore()
const searchQuery = ref('')
const applyDialogVisible = ref(false)

// 申请配额发放表单
const applyForm = ref({
  applyAmount: 0,
  reason: '',
  year: ''
})

const applyRules = ref({
  applyAmount: [
    { required: true, message: '请输入申请配额数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '申请数量必须大于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' }
  ]
})

const surplus = computed(() => {
  if (!quotaData.value.totalQuota) return 0
  return quotaData.value.totalQuota - quotaData.value.verifiedEmission
})

const usageRate = computed(() => {
  if (!quotaData.value.totalQuota || quotaData.value.totalQuota === 0) return 0
  let rate = Math.round((quotaData.value.verifiedEmission / quotaData.value.totalQuota) * 100)
  return rate > 100 ? 100 : rate
})

const getProgressClass = () => {
  if (usageRate.value > 95) return 'danger'
  if (usageRate.value > 80) return 'warning'
  return 'normal'
}

const fetchData = async () => {
  loading.value = true
  try {
    // 优先从 store，其次 localStorage，最后默认 1
    const userId = userStore.userId || localStorage.getItem('userId') || 1
    console.log('Loading quota for userId:', userId, 'year:', currentYear.value)
    
    const res = await getQuota(userId, currentYear.value)
    console.log('Quota API response:', res)
    
    if (res && res.id) {
      quotaData.value = res
      const details = await getQuotaDetails(res.id)
      if (details) {
        tableData.value = details.map(item => ({
          ...item,
          date: item.changeDate
        }))
      }
    } else {
      quotaData.value = { totalQuota: 0, verifiedEmission: 0, status: 0 }
      tableData.value = []
      ElMessage.info(`${currentYear.value}年暂无配额数据`)
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 执行搜索逻辑
  fetchData()
}

const handleApplyQuota = () => {
  // 打开申请配额发放对话框
  applyForm.value.year = currentYear.value
  applyDialogVisible.value = true
}

const submitApply = () => {
  // 执行申请配额发放逻辑
  ElMessage.success('配额申请已提交')
  applyDialogVisible.value = false
  // 重置表单
  applyForm.value = {
    applyAmount: 0,
    reason: '',
    year: currentYear.value
  }
  // 重新加载数据
  fetchData()
}

const handleExportReport = () => {
  // 执行导出报表逻辑
  ElMessage.success('报表导出功能已触发')
}

const initCharts = () => {
  nextTick(() => {
    // 初始化历年配额与排放趋势图表
    const trendChart = echarts.init(document.getElementById('quotaTrendChart'))
    const trendData = getTrendData()
    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}年<br/>{a}: {c} tCO2e'
      },
      legend: {
        data: ['发放配额', '核查排放'],
        top: 0
      },
      xAxis: {
        type: 'category',
        data: trendData.years
      },
      yAxis: {
        type: 'value',
        name: 'tCO2e'
      },
      series: [
        {
          name: '发放配额',
          type: 'bar',
          data: trendData.quotas,
          itemStyle: {
            color: '#10b981'
          }
        },
        {
          name: '核查排放',
          type: 'line',
          data: trendData.emissions,
          itemStyle: {
            color: '#f59e0b'
          },
          lineStyle: {
            width: 3
          },
          symbol: 'circle',
          symbolSize: 8
        }
      ]
    })

    // 初始化配额使用分布图表
    const usageChart = echarts.init(document.getElementById('quotaUsageChart'))
    const usageData = getUsageData()
    usageChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} tCO2e ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: usageData.map(item => item.name)
      },
      series: [
        {
          name: '配额使用',
          type: 'pie',
          radius: '60%',
          data: usageData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })

    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      trendChart.resize()
      usageChart.resize()
    })
  })
}

const getTrendData = () => {
  // 模拟历年数据
  return {
    years: ['2023', '2024', '2025'],
    quotas: [50000, 60000, 65000],
    emissions: [45000, 52000, 58000]
  }
}

const getUsageData = () => {
  // 模拟配额使用分布数据
  const used = quotaData.value.verifiedEmission || 0
  const remaining = Math.max(0, (quotaData.value.totalQuota || 0) - used)
  return [
    { name: '已使用', value: used },
    { name: '剩余配额', value: remaining }
  ]
}

watch([currentYear, quotaData], () => {
  initCharts()
}, { deep: true })

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.quota-container {
  padding: 24px;
  background: var(--bg-secondary, #f8fafc);
  min-height: calc(100vh - 60px);
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-info h1 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}
.header-info p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}
.year-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}
.selector-label {
  font-size: 14px;
  color: #64748b;
}
.year-select {
  width: 120px;
}
.year-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #fff;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}

/* 概览卡片 */
.overview-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  margin-bottom: 24px;
}
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e2e8f0;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}
.status-badge.completed {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669;
}
.status-badge.pending {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}
.status-badge .el-icon {
  font-size: 18px;
}

/* 统计行 */
.stats-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
}
.stat-icon.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-icon.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-icon.red { background: linear-gradient(135deg, #ef4444, #dc2626); }

.stat-content {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}
.stat-value.positive { color: #10b981; }
.stat-value.negative { color: #ef4444; }
.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}
.stat-label small {
  font-size: 12px;
  color: #94a3b8;
}
.stat-divider {
  width: 1px;
  height: 60px;
  background: #e2e8f0;
}

/* 进度条 */
.progress-section {
  margin-top: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.progress-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}
.progress-value {
  font-size: 18px;
  font-weight: 700;
  color: #10b981;
}
.progress-value.warning { color: #f59e0b; }
.progress-value.danger { color: #ef4444; }

.progress-bar-wrapper {
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease;
}
.progress-bar.normal { background: linear-gradient(90deg, #10b981, #34d399); }
.progress-bar.warning { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.progress-bar.danger { background: linear-gradient(90deg, #ef4444, #f87171); }

.progress-tips {
  margin-top: 12px;
  font-size: 13px;
  color: #64748b;
}
.progress-tips .warning { color: #f59e0b; }
.progress-tips .danger { color: #ef4444; }

/* 明细卡片 */
.detail-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  overflow: hidden;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}
.header-title .el-icon {
  color: #10b981;
  font-size: 20px;
}
.search-input {
  width: 220px;
}

/* 自定义表格 */
.custom-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #f8fafc;
}
.custom-table :deep(.el-table__header th) {
  font-weight: 600;
  color: #475569;
  padding: 16px 12px;
}
.custom-table :deep(.el-table__row td) {
  padding: 16px 12px;
}
.custom-table :deep(.el-table__row:hover > td) {
  background: #f0fdf4 !important;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
}
.type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
.type-badge.issue {
  background: #d1fae5;
  color: #059669;
}
.type-badge.change {
  background: #fef3c7;
  color: #d97706;
}
.amount-value {
  font-weight: 600;
}
.amount-value.plus { color: #10b981; }
.amount-value.minus { color: #ef4444; }
.balance-value {
  font-weight: 600;
  color: #1e293b;
}

.empty-state {
  padding: 60px 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-row {
    flex-direction: column;
    gap: 24px;
  }
  .stat-divider {
    width: 80%;
    height: 1px;
  }
}
@media (max-width: 768px) {
  .quota-container { padding: 16px; }
  .page-header { flex-direction: column; gap: 16px; align-items: flex-start; }
}
</style>

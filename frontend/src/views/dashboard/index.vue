<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎回来，{{ username }} 👋</h1>
          <p class="date-info">{{ currentDate }}</p>
        </div>
        <div class="welcome-illustration">
          <img src="https://img.icons8.com/clouds/200/combo-chart.png" alt="chart" />
        </div>
      </div>
    </div>

    <!-- 数据概览卡片 - 带骨架屏 -->
    <div class="stats-grid">
      <template v-if="loading">
        <div class="stat-card skeleton-card" v-for="i in 4" :key="i">
          <div class="skeleton-icon"></div>
          <div class="skeleton-content">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line long"></div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="stat-card stat-card-quota hover-float" @click="goToPage('/assets/quota')">
          <div class="stat-icon">
            <el-icon><Cloudy /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">碳配额总量</span>
            <span class="stat-value">{{ formatNumber(summaryData.totalQuota) }}</span>
            <span class="stat-unit">tCO2e</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon> +12.5%
          </div>
        </div>

        <div class="stat-card stat-card-credit hover-float" @click="goToPage('/assets/credit')">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">CCER持有量</span>
            <span class="stat-value">{{ formatNumber(summaryData.totalCredit) }}</span>
            <span class="stat-unit">tCO2e</span>
          </div>
          <div class="stat-trend up">
            <el-icon><Top /></el-icon> +8.3%
          </div>
        </div>

        <div class="stat-card stat-card-trade hover-float" @click="goToPage('/market/account')">
          <div class="stat-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">今日交易额</span>
            <span class="stat-value">¥{{ formatNumber(summaryData.todayTradeAmount) }}</span>
            <span class="stat-unit">元</span>
          </div>
          <div class="stat-trend down">
            <el-icon><Bottom /></el-icon> -3.2%
          </div>
        </div>

        <div class="stat-card stat-card-task hover-float" @click="goToPage('/system/approval')">
          <div class="stat-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-label">待办事项</span>
            <span class="stat-value">{{ summaryData.pendingTasks }}</span>
            <span class="stat-unit">项待处理</span>
          </div>
          <el-badge :is-dot="summaryData.pendingTasks > 0" class="task-dot" />
        </div>
      </template>
    </div>

    <!-- 图表和快捷区域 -->
    <el-row :gutter="24" class="content-row">
      <!-- 左侧：趋势图表 -->
      <el-col :span="16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><TrendCharts /></el-icon>
                <span>碳资产趋势分析</span>
              </div>
              <el-radio-group v-model="chartPeriod" size="small">
                <el-radio-button value="week">本周</el-radio-button>
                <el-radio-button value="month">本月</el-radio-button>
                <el-radio-button value="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" class="chart-area"></div>
        </el-card>
      </el-col>
      
      <!-- 右侧：快捷入口和动态 -->
      <el-col :span="8">
        <el-card class="shortcut-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Grid /></el-icon>
                <span>快捷入口</span>
              </div>
            </div>
          </template>
          <div class="shortcut-grid">
            <div class="shortcut-item" @click="goToPage('/development/project')">
              <div class="shortcut-icon" style="background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);">
                <el-icon><Plus /></el-icon>
              </div>
              <span>项目立项</span>
            </div>
            <div class="shortcut-item" @click="goToPage('/market/supply-demand')">
              <div class="shortcut-icon" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%);">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <span>发布交易</span>
            </div>
            <div class="shortcut-item" @click="goToPage('/market/trade-records')">
              <div class="shortcut-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">
                <el-icon><Document /></el-icon>
              </div>
              <span>交易记录</span>
            </div>
            <div class="shortcut-item" @click="goToPage('/system/config')">
              <div class="shortcut-icon" style="background: linear-gradient(135deg, #64748b 0%, #475569 100%);">
                <el-icon><Setting /></el-icon>
              </div>
              <span>系统设置</span>
            </div>
          </div>
        </el-card>

        <el-card class="timeline-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Clock /></el-icon>
                <span>最新动态</span>
              </div>
              <el-button link type="primary" size="small">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="10:00" placement="top" type="primary" :hollow="true">
              <div class="timeline-content">
                <span class="timeline-title">系统完成每日数据盘点</span>
                <span class="timeline-desc">所有数据核对完成，系统运行正常</span>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="09:30" placement="top" type="success" :hollow="true">
              <div class="timeline-content">
                <span class="timeline-title">CCER项目审核通过</span>
                <span class="timeline-desc">项目 #202401 已完成审核</span>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="昨天" placement="top" type="warning" :hollow="true">
              <div class="timeline-content">
                <span class="timeline-title">新增碳交易订单</span>
                <span class="timeline-desc">今日新增 5 笔交易订单</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardSummary, getUserStats } from '@/api/dashboard'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Top, Bottom, TrendCharts, Grid, Clock, Plus, ShoppingCart, Document, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const username = computed(() => userStore.username || 'Guest')
const loading = ref(false)
const chartRef = ref(null)
const chartPeriod = ref('month')
let chartInstance = null

// 仪表盘汇总数据
const summaryData = ref({
  totalUsers: 0,
  certifiedCompanies: 0,
  totalQuota: 0,
  totalCredit: 0,
  totalAssets: 0,
  todayTradeAmount: 0,
  totalTradeAmount: 0,
  pendingTasks: 0,
  systemStatus: '正常运行'
})

// 用户个人统计数据
const userStatsData = ref({
  userQuota: 0,
  userCredit: 0,
  tradeCount: 0
})

onMounted(() => {
  loadDashboardData()
  // 使用 nextTick 确保 DOM 渲染完成后再初始化图表
  nextTick(() => {
    initChart()
  })
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

// 加载仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    const userId = userStore.userId || 1
    const summaryRes = await getDashboardSummary(userId)
    if (summaryRes) {
      summaryData.value = summaryRes
    }

    const userStatsRes = await getUserStats(userId)
    if (userStatsRes) {
      userStatsData.value = userStatsRes
    }
  } catch (error) {
    console.error('加载仪表盘数据失败', error)
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return Number(num).toLocaleString('zh-CN')
}

// 页面跳转
const goToPage = (path) => {
  router.push(path)
}

// 窗口resize处理
const handleResize = () => {
  chartInstance?.resize()
}

// 初始化ECharts图表
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#1e293b' },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#94a3b8' }
      }
    },
    legend: {
      data: ['碳配额', 'CCER', '交易量'],
      bottom: 0,
      textStyle: { color: '#64748b' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: [{
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b' },
      axisPointer: { type: 'shadow' }
    }],
    yAxis: [{
      type: 'value',
      name: '资产量 (吨)',
      nameTextStyle: { color: '#64748b' },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: { color: '#64748b', formatter: '{value}' }
    }, {
      type: 'value',
      name: '交易量 (笔)',
      nameTextStyle: { color: '#64748b' },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { color: '#64748b', formatter: '{value}' }
    }],
    series: [{
      name: '碳配额',
      type: 'bar',
      barWidth: '20%',
      data: [820000, 932000, 901000, 934000, 1290000, 1330000, 1320000],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#6ee7b7' },
          { offset: 1, color: '#10b981' }
        ])
      }
    }, {
      name: 'CCER',
      type: 'bar',
      barWidth: '20%',
      data: [45000, 52000, 48000, 50000, 60000, 55000, 58000],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#93c5fd' },
          { offset: 1, color: '#3b82f6' }
        ])
      }
    }, {
      name: '交易量',
      type: 'line',
      yAxisIndex: 1,
      data: [150, 230, 224, 218, 350, 380, 420],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#f59e0b' },
      itemStyle: { color: '#f59e0b', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
          { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
        ])
      }
    }]
  }
  
  chartInstance.setOption(option)
  window.addEventListener('resize', handleResize)
}
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background: var(--bg-secondary, #f8fafc);
  min-height: calc(100vh - 60px);
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 40px rgba(16, 185, 129, 0.3);
  position: relative;
  overflow: hidden;
}
.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 60%;
  height: 200%;
  background: radial-gradient(ellipse, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}
.welcome-content {
  position: relative;
  z-index: 1;
}
.welcome-content h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.welcome-content p {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
}
.welcome-illustration {
  width: 200px;
  height: 120px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 120"><path d="M30 100 L50 60 L70 80 L100 30 L130 70 L160 40 L180 60 L180 100 Z" fill="rgba(255,255,255,0.2)" /><circle cx="100" cy="50" r="8" fill="rgba(255,255,255,0.8)"/><circle cx="50" cy="60" r="6" fill="rgba(255,255,255,0.6)"/><circle cx="160" cy="40" r="6" fill="rgba(255,255,255,0.6)"/></svg>') no-repeat center;
  opacity: 0.9;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: none;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  border-radius: 16px 16px 0 0;
}
.stat-card.primary::before { background: linear-gradient(90deg, #10b981, #34d399); }
.stat-card.success::before { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.stat-card.warning::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.stat-card.danger::before { background: linear-gradient(90deg, #ef4444, #f87171); }

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg, 0 20px 40px rgba(0,0,0,0.12));
}
.stat-card:active { transform: translateY(-2px); }

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}
.stat-card.primary .stat-icon { background: linear-gradient(135deg, #10b981, #059669); }
.stat-card.success .stat-icon { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-card.warning .stat-icon { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-card.danger .stat-icon { background: linear-gradient(135deg, #ef4444, #dc2626); }

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 20px;
}
.stat-trend.up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}
.stat-trend.down {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}
.stat-body .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}
.stat-body .stat-unit {
  font-size: 14px;
  color: #64748b;
  margin-left: 4px;
}
.stat-body .stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

/* 图表卡片 */
.chart-card {
  background: #fff;
  border-radius: 16px;
  border: none;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  overflow: hidden;
}
.chart-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}
.chart-card :deep(.el-card__body) {
  padding: 24px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
.chart-area {
  height: 360px;
  width: 100%;
}
.period-selector :deep(.el-radio-button__inner) {
  padding: 8px 16px;
  border-radius: 8px !important;
  border: 1px solid #e2e8f0 !important;
  margin: 0 4px;
}
.period-selector :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981 !important;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* 快捷入口 */
.shortcut-card {
  background: #fff;
  border-radius: 16px;
  border: none;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}
.shortcut-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.shortcut-card :deep(.el-card__body) {
  padding: 24px;
}
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8fafc;
}
.shortcut-item:hover {
  background: #f1f5f9;
  transform: translateY(-4px);
}
.shortcut-item:active { transform: translateY(-2px); }
.shortcut-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  margin-bottom: 12px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}
.shortcut-icon.green { background: linear-gradient(135deg, #10b981, #059669); }
.shortcut-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.shortcut-icon.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.shortcut-icon.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.shortcut-item span {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}

/* 时间线卡片 */
.timeline-card {
  background: #fff;
  border-radius: 16px;
  border: none;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}
.timeline-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.timeline-card :deep(.el-card__body) {
  padding: 24px;
}
.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.timeline-desc {
  font-size: 13px;
  color: #64748b;
}
.timeline-card :deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #94a3b8;
}
.timeline-card :deep(.el-timeline-item__node--hollow) {
  border-width: 3px;
}

/* 骨架屏样式 */
.skeleton-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: default;
}
.skeleton-card:hover {
  transform: none;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}
.skeleton-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}
.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.skeleton-line {
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}
.skeleton-line.short {
  width: 60%;
  height: 12px;
}
.skeleton-line.long {
  width: 80%;
  height: 24px;
}
@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .dashboard-container { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .shortcut-grid { grid-template-columns: repeat(2, 1fr); }
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  .welcome-illustration { margin-top: 20px; }
}
</style>


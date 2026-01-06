<template>
  <div class="app-container">
    <div class="welcome-section">
      <h2>欢迎回到工作台，{{ username }}</h2>
      <p class="date-info">{{ currentDate }}</p>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="data-row">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-blue">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><Cloudy /></el-icon>
            </div>
            <div class="info">
              <div class="label">碳配额总量</div>
              <div class="value">1,000,000 <span class="unit">tCO2e</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-green">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="info">
              <div class="label">CCER持有量</div>
              <div class="value">50,000 <span class="unit">tCO2e</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-orange">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><Money /></el-icon>
            </div>
            <div class="info">
              <div class="label">今日交易额</div>
              <div class="value">¥ 2,300,000</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card card-red">
          <div class="card-content">
            <div class="icon-wrapper">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="info">
              <div class="label">待办事项</div>
              <div class="value">3 <span class="unit">个</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 左侧：趋势图表 -->
      <el-col :span="16">
        <el-card class="chart-card" header="碳排放与资产趋势">
          <div class="chart-placeholder">
             <!-- 模拟图表 -->
             <div class="mock-chart">
                <div class="bar" style="height: 40%"></div>
                <div class="bar" style="height: 60%"></div>
                <div class="bar" style="height: 55%"></div>
                <div class="bar" style="height: 70%"></div>
                <div class="bar" style="height: 85%"></div>
                <div class="bar" style="height: 65%"></div>
                <div class="bar" style="height: 90%"></div>
             </div>
             <p style="text-align: center; color: #909399;">(此处应集成 ECharts 展示动态数据)</p>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：快捷入口和通知 -->
      <el-col :span="8">
        <el-card header="快捷入口" class="shortcut-card">
          <div class="shortcut-grid">
             <div class="shortcut-item">
               <el-button type="primary" circle size="large" icon="Plus" />
               <span>项目立项</span>
             </div>
             <div class="shortcut-item">
               <el-button type="success" circle size="large" icon="ShoppingCart" />
               <span>发布交易</span>
             </div>
             <div class="shortcut-item">
               <el-button type="warning" circle size="large" icon="Document" />
               <span>报表下载</span>
             </div>
             <div class="shortcut-item">
               <el-button type="info" circle size="large" icon="Setting" />
               <span>系统设置</span>
             </div>
          </div>
        </el-card>

        <el-card header="最新动态" style="margin-top: 20px;">
          <el-timeline>
             <el-timeline-item timestamp="10:00" type="primary">
               系统完成每日数据盘点
             </el-timeline-item>
             <el-timeline-item timestamp="09:30" type="success">
               CCER 项目 #202401 审核通过
             </el-timeline-item>
             <el-timeline-item timestamp="Yesterday" type="warning">
               新增碳交易订单 5 笔
             </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'

const username = ref('Admin')

onMounted(() => {
  const storedUser = localStorage.getItem('username')
  if (storedUser) {
    username.value = storedUser
  }
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})
</script>

<style scoped>
.app-container { padding: 20px; }
.welcome-section {
  margin-bottom: 20px;
}
.welcome-section h2 { margin: 0; color: #303133; }
.date-info { margin: 5px 0 0; color: #909399; font-size: 14px; }

.data-card {
  border: none;
  border-radius: 8px;
  color: #fff;
  transition: transform 0.3s;
}
.data-card:hover { transform: translateY(-5px); }
.card-blue { background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%); }
.card-green { background: linear-gradient(135deg, #42e695 0%, #3bb2b8 100%); }
.card-orange { background: linear-gradient(135deg, #f09819 0%, #edde5d 100%); }
.card-red { background: linear-gradient(135deg, #ff512f 0%, #dd2476 100%); }

.card-content { display: flex; align-items: center; }
.icon-wrapper { 
  font-size: 40px; 
  padding: 10px; 
  background: rgba(255,255,255,0.2); 
  border-radius: 50%; 
  margin-right: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px; 
}
.info .label { font-size: 14px; opacity: 0.9; }
.info .value { font-size: 24px; font-weight: bold; margin-top: 5px; }
.info .unit { font-size: 12px; font-weight: normal; opacity: 0.8; }

.chart-placeholder {
  height: 350px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
}
.mock-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 80%;
  border-bottom: 1px solid #dcdfe6;
  padding-bottom: 10px;
}
.bar {
  width: 40px;
  background-color: #409EFF;
  border-radius: 4px 4px 0 0;
  opacity: 0.8;
  transition: opacity 0.3s;
}
.bar:hover { opacity: 1; }

.shortcut-grid { display: flex; justify-content: space-around; padding: 10px 0; }
.shortcut-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; }
.shortcut-item span { margin-top: 8px; font-size: 14px; color: #606266; }
</style>

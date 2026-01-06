<template>
  <div class="app-container">
    <el-alert
      title="行情数据实时连接中，数据延迟 < 100ms"
      type="success"
      show-icon
      style="margin-bottom: 20px;"
    />

    <el-row :gutter="20">
      <el-col :span="8" v-for="ex in exchanges" :key="ex.name">
        <el-card class="exchange-card" :class="{ 'is-connected': ex.connected }" shadow="hover">
          <div class="status-dot" :class="{ 'online': ex.connected }"></div>
          <div class="ex-logo">{{ ex.code }}</div>
          <h3 class="ex-name">{{ ex.name }}</h3>
          <div class="ex-info">
             <p>交易品种: <span>{{ ex.products }}</span></p>
             <p>当前状态: 
               <el-tag size="small" :type="ex.connected ? 'success' : 'info'">{{ ex.connected ? '已连接' : '未连接' }}</el-tag>
             </p>
          </div>
          <div class="ex-action">
            <el-button v-if="ex.connected" type="danger" plain size="small">断开连接</el-button>
            <el-button v-else type="primary" plain size="small">建立连接</el-button>
            <el-button link type="primary" size="small">查看行情</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模拟实时行情滚动 -->
    <el-card header="市场即时消息" style="margin-top: 20px">
      <el-table :data="marketFlow" height="250" style="width: 100%" :show-header="false">
         <el-table-column prop="time" width="100" />
         <el-table-column prop="market" width="150" />
         <el-table-column prop="message" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const exchanges = ref([
  { name: '上海环境能源交易所', code: 'SEEE', products: 'SHEA, CCER', connected: true },
  { name: '全国碳排放权交易系统', code: 'NCETS', products: 'CEA', connected: true },
  { name: '北京绿色交易所', code: 'CBEEX', products: 'BCEA, CCER', connected: false },
])

const marketFlow = [
  { time: '10:30:05', market: 'SEEE', message: 'SHEA 2023 最新成交价 65.50 元/吨，成交量 500 吨' },
  { time: '10:30:02', market: 'NCETS', message: 'CEA 开盘价 72.00 元/吨' },
  { time: '10:29:55', market: 'SEEE', message: '大宗协议交易申报成功：卖出 10000 吨 CCER' },
  { time: '10:29:10', market: 'CBEEX', message: '收盘通知：昨日交易结算完成' },
]
</script>

<style scoped>
.app-container { padding: 20px; }
.exchange-card {
  position: relative;
  text-align: center;
  transition: all 0.3s;
  border-top: 3px solid transparent;
}
.exchange-card.is-connected {
  border-top-color: #67c23a;
}
.status-dot {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #909399;
}
.status-dot.online { background-color: #67c23a; box-shadow: 0 0 5px #67c23a; }

.ex-logo {
  width: 60px;
  height: 60px;
  line-height: 60px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 50%;
  margin: 0 auto 15px;
  font-weight: bold;
  font-size: 14px;
}
.ex-name { margin: 0 0 10px; font-size: 16px; color: #303133; }
.ex-info p { margin: 5px 0; font-size: 13px; color: #606266; }
.ex-action { margin-top: 15px; border-top: 1px solid #EBEEF5; padding-top: 15px; }
</style>

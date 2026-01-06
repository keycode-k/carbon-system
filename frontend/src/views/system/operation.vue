<template>
  <div class="app-container">
    <el-card shadow="never">
       <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
             <span>系统操作日志</span>
             <div>
                <el-date-picker type="daterange" range-separator="To" start-placeholder="Start date" end-placeholder="End date" size="small" style="margin-right: 10px;" />
                <el-button type="primary" size="small">查询</el-button>
                <el-button size="small">导出</el-button>
             </div>
          </div>
       </template>
       
       <div class="log-timeline">
          <el-timeline>
             <el-timeline-item
               v-for="(activity, index) in activities"
               :key="index"
               :icon="activity.icon"
               :type="activity.type"
               :color="activity.color"
               :size="activity.size"
               :timestamp="activity.timestamp"
             >
               <el-card class="log-card">
                  <div class="log-header">
                     <span class="log-user">{{ activity.user }}</span>
                     <span class="log-ip">IP: {{ activity.ip }}</span>
                  </div>
                  <p class="log-content">{{ activity.content }}</p>
                  <div v-if="activity.detail" class="log-detail">
                     {{ activity.detail }}
                  </div>
               </el-card>
             </el-timeline-item>
          </el-timeline>
       </div>
    </el-card>
  </div>
</template>

<script setup>
const activities = [
  {
    content: '修改了企业认证资料',
    timestamp: '2026-01-06 14:26',
    size: 'large',
    type: 'primary',
    icon: 'Edit',
    user: 'admin',
    ip: '192.168.1.101',
    detail: '更新了营业执照副本附件'
  },
  {
    content: '用户登录成功',
    timestamp: '2026-01-06 09:00',
    color: '#0bbd87',
    user: 'admin',
    ip: '192.168.1.101'
  },
  {
    content: '提交了配额卖出挂单',
    timestamp: '2026-01-05 16:30',
    type: 'warning',
    user: 'trader001',
    ip: '10.0.0.5',
    detail: '方向：卖出，数量：2000，价格：66.00'
  },
  {
    content: '审批通过了项目立项申请',
    timestamp: '2026-01-04 11:20',
    color: '#0bbd87',
    user: 'manager_zhang',
    ip: '192.168.1.105',
    detail: '项目编号：P2025001'
  }
]
</script>

<style scoped>
.app-container { padding: 20px; }
.log-card { margin-bottom: 10px; }
.log-header { display: flex; justify-content: space-between; margin-bottom: 5px; border-bottom: 1px solid #ebeef5; padding-bottom: 5px; }
.log-user { font-weight: bold; color: #303133; }
.log-ip { font-size: 12px; color: #909399; }
.log-content { font-size: 14px; color: #606266; margin: 10px 0; }
.log-detail { background: #f5f7fa; padding: 8px; border-radius: 4px; font-size: 12px; color: #909399; font-family: monospace; }
</style>

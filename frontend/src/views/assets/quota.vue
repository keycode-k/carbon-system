<template>
  <div class="app-container" v-loading="loading">
    <el-card shadow="never" class="compliance-card">
      <div class="compliance-header">
         <div>
            <span class="label">当前履约年度：</span>
            <el-select v-model="currentYear" placeholder="Select" style="width: 100px; font-weight: bold;">
              <el-option label="2025" value="2025" />
              <el-option label="2024" value="2024" />
              <el-option label="2023" value="2023" />
            </el-select>
         </div>
         <div class="compliance-status">
            履约状态：
            <el-tag v-if="quotaData.status === 1" type="success" effect="dark">履约完成</el-tag>
            <el-tag v-else type="danger" effect="dark">未履约</el-tag>
         </div>
      </div>
      
      <el-row :gutter="40" style="margin-top: 30px;">
        <el-col :span="8" class="stat-col">
          <div class="stat-value">{{ quotaData.totalQuota?.toLocaleString() || 0 }}</div>
          <div class="stat-label">发放总配额 (tCO2e)</div>
        </el-col>
        <el-col :span="8" class="stat-col">
          <div class="stat-value">{{ quotaData.verifiedEmission?.toLocaleString() || 0 }}</div>
          <div class="stat-label">核查排放量 (tCO2e)</div>
        </el-col>
        <el-col :span="8" class="stat-col">
          <div class="stat-value" :class="{ 'positive': surplus >= 0, 'negative': surplus < 0 }">
            {{ surplus > 0 ? '+' : '' }} {{ surplus.toLocaleString() }}
          </div>
          <div class="stat-label">盈余配额 (tCO2e)</div>
        </el-col>
      </el-row>

      <div class="progress-section">
         <div class="progress-info">
           <span>配额使用率</span>
           <span>{{ usageRate }}%</span>
         </div>
         <el-progress :text-inside="true" :stroke-width="20" :percentage="usageRate" status="warning" />
      </div>
    </el-card>

    <el-card shadow="hover" header="配额明细记录" style="margin-top: 20px;">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="date" label="发放/变动日期" width="180" />
        <el-table-column prop="type" label="类型" width="150">
           <template #default="scope">
              <el-tag :type="scope.row.type === '发放' ? 'success' : 'warning'">{{ scope.row.type }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="amount" label="变动数量" align="right">
          <template #default="scope">
             <span :style="{ color: scope.row.amount > 0 ? 'green' : 'red' }">
               {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
             </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="结余" align="right" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { getQuota, getQuotaDetails } from '@/api/assets'
import { ElMessage } from 'element-plus'

const currentYear = ref('2025')
const quotaData = ref({
  totalQuota: 0,
  verifiedEmission: 0,
  status: 0
})

const loading = ref(false)
const tableData = ref([])

// 计算盈余配额
const surplus = computed(() => {
  if (!quotaData.value.totalQuota) return 0
  return quotaData.value.totalQuota - quotaData.value.verifiedEmission
})

// 计算使用率
const usageRate = computed(() => {
  if (!quotaData.value.totalQuota || quotaData.value.totalQuota === 0) return 0
  let rate = Math.round((quotaData.value.verifiedEmission / quotaData.value.totalQuota) * 100)
  return rate > 100 ? 100 : rate
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 同时传userId和year
    const userId = 1 // TODO: 从登录信息中获取
    const res = await getQuota(userId, currentYear.value)
    if (res && res.id) {
      quotaData.value = res
      // 获取明细
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

watch(currentYear, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.compliance-card { background: #fdfdfd; }
.compliance-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ebeef5; padding-bottom: 15px; }
.stat-col { text-align: center; border-right: 1px solid #ebeef5; }
.stat-col:last-child { border-right: none; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-value.positive { color: #67C23A; }
.stat-value.negative { color: #F56C6C; }
.stat-label { font-size: 14px; color: #909399; margin-top: 5px; }
.progress-section { margin-top: 30px; padding: 0 20px; }
.progress-info { display: flex; justify-content: space-between; margin-bottom: 5px; color: #606266; font-size: 14px; }
</style>

<template>
  <div class="trade-records-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的交易记录</span>
          <el-button type="primary" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table 
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%">
        <el-table-column prop="id" label="交易ID" width="80" />
        <el-table-column label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.buyerId === userId ? 'success' : 'warning'">
              {{ row.buyerId === userId ? '买入' : '卖出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="资产类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.assetType === 'QUOTA' ? 'primary' : 'success'">
              {{ row.assetType === 'QUOTA' ? '碳配额' : 'CCER' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tradePrice" label="成交单价" width="120">
          <template #default="{ row }">
            ¥{{ row.tradePrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="tradeQuantity" label="成交数量" width="120">
          <template #default="{ row }">
            {{ row.tradeQuantity.toFixed(2) }} 吨
          </template>
        </el-table-column>
        <el-table-column prop="tradeAmount" label="成交金额" width="150">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: bold;">
              ¥{{ row.tradeAmount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="tradeTime" label="成交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.tradeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="buyOrderId" label="买单ID" width="100" />
        <el-table-column prop="sellOrderId" label="卖单ID" width="100" />
        <el-table-column label="对手方ID" width="100">
          <template #default="{ row }">
            {{ row.buyerId === userId ? row.sellerId : row.buyerId }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center;"
      />

      <!-- 统计信息 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <el-statistic title="总交易次数" :value="pagination.total" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="总买入金额" :value="totalBuyAmount" prefix="¥" :precision="2" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="总卖出金额" :value="totalSellAmount" prefix="¥" :precision="2" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getMyTradeRecords } from '@/api/trade'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userId = computed(() => userStore.userId)

const loading = ref(false)
const records = ref([])

const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 计算总买入金额
const totalBuyAmount = computed(() => {
  return records.value
    .filter(r => r.buyerId === userId.value)
    .reduce((sum, r) => sum + Number(r.tradeAmount), 0)
})

// 计算总卖出金额
const totalSellAmount = computed(() => {
  return records.value
    .filter(r => r.sellerId === userId.value)
    .reduce((sum, r) => sum + Number(r.tradeAmount), 0)
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const res = await getMyTradeRecords({
      userId: userId.value,
      current: pagination.value.current,
      size: pagination.value.size
    })
    
    console.log('交易记录API响应:', res)
    
    // axios拦截器已经处理过，res直接就是分页数据对象
    if (res && res.records !== undefined) {
      records.value = res.records || []
      pagination.value.total = res.total || 0
      console.log('交易记录数据:', records.value)
    } else {
      console.error('API返回数据格式错误:', res)
      ElMessage.error('获取交易记录失败：数据格式错误')
    }
  } catch (error) {
    console.error('加载交易记录失败，详细错误:', error)
    console.error('错误响应:', error.response)
    ElMessage.error('加载交易记录失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  loadData()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.value.current = page
  loadData()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.trade-records-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
}

:deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
</style>

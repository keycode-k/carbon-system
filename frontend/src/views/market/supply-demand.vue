<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="searchKeyword" placeholder="搜索关键字" style="width: 200px; margin-right: 10px;" />
      <el-select v-model="searchDirection" placeholder="交易方向" style="width: 120px; margin-right: 10px;" clearable>
        <el-option label="出售" value="SELL" />
        <el-option label="求购" value="BUY" />
      </el-select>
      <el-select v-model="searchType" placeholder="资产类型" style="width: 120px; margin-right: 10px;" clearable>
        <el-option label="配额 (QUOTA)" value="QUOTA" />
        <el-option label="信用 (CREDIT)" value="CREDIT" />
      </el-select>
      <el-button type="primary" icon="Search" @click="handleFilter">筛选</el-button>
      <el-button type="success" icon="Plus" style="float: right;" @click="showPublishDialog">发布供需</el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="item in items" :key="item.id" style="margin-bottom: 20px;">
        <el-card shadow="hover" class="box-card">
          <div class="card-header">
            <el-tag :type="item.type === '出售' ? 'success' : 'warning'" effect="dark">{{ item.type }}</el-tag>
            <span class="asset-name">{{ item.asset }}</span>
          </div>
          <div class="card-body">
             <div class="info-row">
               <span class="label">数量:</span>
               <span class="value">{{ item.amount }} 吨</span>
             </div>
             <div class="info-row">
               <span class="label">价格:</span>
               <span class="value price">{{ item.price }} 元/吨</span>
             </div>
             <div class="info-row">
               <span class="label">状态:</span>
               <el-tag size="small" :type="item.status === 'OPEN' ? 'success' : 'info'">
                 {{ item.status === 'OPEN' ? '等待撮合' : '已成交' }}
               </el-tag>
             </div>
             <div class="info-row">
               <span class="label">发布时间:</span>
               <span class="value">{{ item.deadline }}</span>
             </div>
          </div>
          <div class="card-footer">
             <div class="user-info">
                <el-avatar :size="24" style="margin-right: 5px;">{{ item.user.charAt(2) }}</el-avatar>
                <span>{{ item.user }}</span>
             </div>
             <el-button link type="primary" size="small">详情</el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 空状态 -->
      <el-col :span="24" v-if="!loading && items.length === 0">
        <el-empty description="暂无订单数据，快来发布第一个订单吧！" />
      </el-col>
    </el-row>
    
    <el-pagination 
      v-if="total > 0"
      layout="prev, pager, next, total" 
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
      style="justify-content: center; margin-top: 20px;" 
    />

    <!-- 发布订单对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布交易订单" width="500px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="交易方向">
          <el-radio-group v-model="publishForm.direction">
            <el-radio label="BUY">求购</el-radio>
            <el-radio label="SELL">出售</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-radio-group v-model="publishForm.itemType">
            <el-radio label="QUOTA">碳配额</el-radio>
            <el-radio label="CREDIT">碳信用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格(元/吨)">
          <el-input-number v-model="publishForm.price" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="数量(吨)">
          <el-input-number v-model="publishForm.quantity" :min="1" :step="10" />
        </el-form-item>
        <el-alert 
          title="提示：订单发布后将进入撮合队列，系统每5秒自动匹配一次" 
          type="info" 
          :closable="false"
          style="margin-bottom: 20px;"
        />
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMarketOrders, publishTradeOrder } from '@/api/trade'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 搜索条件
const searchType = ref('')
const searchDirection = ref('')
const searchKeyword = ref('')

// 发布订单对话框
const publishDialogVisible = ref(false)
const publishForm = ref({
  direction: 'SELL',
  itemType: 'QUOTA',
  price: null,
  quantity: null
})

// 转换为卡片显示格式
const items = computed(() => {
  return orders.value.map(order => ({
    id: order.id,
    type: order.direction === 'BUY' ? '求购' : '出售',
    asset: order.itemType === 'QUOTA' ? '碳配额' : '碳信用',
    amount: order.quantity?.toLocaleString() || '0',
    price: order.price?.toFixed(2) || '面议',
    year: new Date(order.createTime).getFullYear(),
    deadline: order.createTime ? new Date(order.createTime).toLocaleDateString() : '-',
    user: `用户${order.userId}`,
    status: order.status,
    rawData: order
  }))
})

// 加载市场订单
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchType.value) params.type = searchType.value
    if (searchDirection.value) params.direction = searchDirection.value
    
    const res = await getMarketOrders(params)
    if (res && res.records) {
      orders.value = res.records
      total.value = res.total || 0
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
  loadOrders()
}

// 显示发布对话框
const showPublishDialog = () => {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  publishDialogVisible.value = true
}

// 发布订单
const handlePublish = async () => {
  if (!publishForm.value.price || !publishForm.value.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const data = {
      userId: userStore.userId,
      direction: publishForm.value.direction,
      itemType: publishForm.value.itemType,
      price: publishForm.value.price,
      quantity: publishForm.value.quantity,
      status: 'OPEN'
    }
    
    await publishTradeOrder(data)
    ElMessage.success('订单发布成功，等待撮合')
    publishDialogVisible.value = false
    // 重置表单
    publishForm.value = {
      direction: 'SELL',
      itemType: 'QUOTA',
      price: null,
      quantity: null
    }
    loadOrders()
  } catch (error) {
    console.error('发布订单失败:', error)
  }
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

onMounted(() => {
  loadOrders()
  // 每10秒刷新一次（查看撮合结果）
  setInterval(() => {
    loadOrders()
  }, 10000)
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 4px; }
.card-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; margin-bottom: 10px; }
.asset-name { font-weight: bold; font-size: 16px; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.label { color: #909399; }
.value { color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
.card-footer { border-top: 1px solid #f0f0f0; padding-top: 10px; margin-top: 15px; display: flex; justify-content: space-between; align-items: center; }
.user-info { display: flex; align-items: center; font-size: 12px; color: #606266; }
</style>

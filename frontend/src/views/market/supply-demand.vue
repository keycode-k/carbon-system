<template>
  <div class="market-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>供需大厅</h1>
        <p>发布您的碳交易需求，系统自动撮合最优价格</p>
      </div>
      <el-button type="primary" size="large" icon="Plus" class="publish-btn" @click="showPublishDialog">
        发布订单
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <span class="filter-label">关键字</span>
          <el-input v-model="searchKeyword" placeholder="搜索关键字" clearable class="filter-input">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item">
          <span class="filter-label">交易方向</span>
          <el-select v-model="searchDirection" placeholder="全部" clearable class="filter-select">
            <el-option label="出售" value="SELL" />
            <el-option label="求购" value="BUY" />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="filter-label">资产类型</span>
          <el-select v-model="searchType" placeholder="全部" clearable class="filter-select">
            <el-option label="碳配额 (QUOTA)" value="QUOTA" />
            <el-option label="碳信用 (CREDIT)" value="CREDIT" />
          </el-select>
        </div>
        <el-button type="primary" icon="Search" @click="handleFilter">筛选</el-button>
        <el-button icon="Refresh" @click="resetFilter">重置</el-button>
      </div>
    </div>

    <!-- 订单卡片列表 -->
    <div class="order-grid" v-loading="loading">
      <div class="order-card" v-for="item in items" :key="item.id">
        <div class="order-badge" :class="item.type === '出售' ? 'sell' : 'buy'">
          {{ item.type }}
        </div>
        <div class="order-header">
          <div class="asset-tag" :class="item.asset === '碳配额' ? 'quota' : 'credit'">
            {{ item.asset }}
          </div>
          <el-tag size="small" :type="item.status === 'OPEN' ? 'success' : 'info'" effect="light">
            {{ item.status === 'OPEN' ? '待撮合' : '已成交' }}
          </el-tag>
        </div>
        <div class="order-body">
          <div class="order-price">
            <span class="price-value">¥{{ item.price }}</span>
            <span class="price-unit">/吨</span>
          </div>
          <div class="order-amount">
            <span class="amount-label">交易数量</span>
            <span class="amount-value">{{ item.amount }} <small>吨</small></span>
          </div>
          <div class="order-total">
            <span class="total-label">订单总额</span>
            <span class="total-value">¥{{ calculateTotal(item) }}</span>
          </div>
        </div>
        <div class="order-footer">
          <div class="user-info">
            <el-avatar :size="28" class="user-avatar">{{ item.user.slice(-1) }}</el-avatar>
            <span class="user-name">{{ item.user }}</span>
          </div>
          <div class="order-time">
            <el-icon><Clock /></el-icon>
            <span>{{ item.deadline }}</span>
          </div>
        </div>
        <div class="order-actions">
          <el-button text type="primary" @click="viewDetail(item)">查看详情</el-button>
          <el-button text type="success" v-if="item.status === 'OPEN'" @click="quickMatch(item)">快速交易</el-button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && items.length === 0">
        <el-empty description="暂无订单数据">
          <template #image>
            <div class="empty-icon">📋</div>
          </template>
          <el-button type="primary" @click="showPublishDialog">立即发布订单</el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination 
        background
        layout="total, prev, pager, next, jumper" 
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 发布订单对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布交易订单" width="520px" class="publish-dialog">
      <el-form :model="publishForm" label-width="100px" class="publish-form">
        <el-form-item label="交易方向">
          <el-radio-group v-model="publishForm.direction" class="direction-group">
            <el-radio-button value="SELL">
              <el-icon><SoldOut /></el-icon> 出售
            </el-radio-button>
            <el-radio-button value="BUY">
              <el-icon><ShoppingCart /></el-icon> 求购
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-radio-group v-model="publishForm.itemType" class="type-group">
            <el-radio value="QUOTA">碳配额</el-radio>
            <el-radio value="CREDIT">碳信用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="publishForm.price" :min="0" :precision="2" :step="0.5" placeholder="请输入价格">
            <template #suffix>元/吨</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="publishForm.quantity" :min="1" :step="10" placeholder="请输入数量">
            <template #suffix>吨</template>
          </el-input-number>
        </el-form-item>
        <el-alert 
          title="订单发布后将进入撮合队列，系统每5秒自动匹配一次最优价格" 
          type="info" 
          :closable="false"
          show-icon
          class="tip-alert"
        />
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { getMarketOrders, publishTradeOrder } from '@/api/trade'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Clock, SoldOut, ShoppingCart, Plus, Refresh } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const publishing = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
let refreshTimer = null

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

// 计算订单总额
const calculateTotal = (item) => {
  const price = parseFloat(item.price) || 0
  const amount = parseFloat(item.amount?.replace(/,/g, '')) || 0
  return (price * amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

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

// 重置筛选
const resetFilter = () => {
  searchType.value = ''
  searchDirection.value = ''
  searchKeyword.value = ''
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

// 查看详情
const viewDetail = (item) => {
  ElMessageBox.alert(`订单编号: ${item.id}<br>类型: ${item.asset}<br>方向: ${item.type}<br>数量: ${item.amount} 吨<br>价格: ¥${item.price}/吨`, '订单详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

// 快速交易
const quickMatch = (item) => {
  ElMessageBox.confirm(`确认与该订单进行交易？<br>交易金额: ¥${calculateTotal(item)}`, '确认交易', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success('交易请求已提交，等待处理')
  }).catch(() => {})
}

// 发布订单
const handlePublish = async () => {
  if (!publishForm.value.price || !publishForm.value.quantity) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  publishing.value = true
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
    publishForm.value = {
      direction: 'SELL',
      itemType: 'QUOTA',
      price: null,
      quantity: null
    }
    loadOrders()
  } catch (error) {
    console.error('发布订单失败:', error)
  } finally {
    publishing.value = false
  }
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

onMounted(() => {
  loadOrders()
  refreshTimer = setInterval(() => {
    loadOrders()
  }, 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.market-container {
  padding: 24px;
  background: var(--bg-secondary, #f8fafc);
  min-height: calc(100vh - 60px);
}

/* 页面标题区域 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(59, 130, 246, 0.3);
}
.header-content h1 {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
}
.header-content p {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}
.publish-btn {
  padding: 14px 28px;
  font-size: 15px;
  border-radius: 10px;
  background: #fff;
  color: #3b82f6;
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
}
.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.2);
  background: #f8fafc;
  color: #2563eb;
}

/* 筛选卡片 */
.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-size: 14px;
  color: #64748b;
  white-space: nowrap;
}
.filter-input { width: 200px; }
.filter-select { width: 140px; }

/* 订单卡片网格 */
.order-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.order-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  position: relative;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}
.order-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: #e2e8f0;
}

/* 订单角标 */
.order-badge {
  position: absolute;
  top: 0;
  right: 20px;
  padding: 6px 16px;
  border-radius: 0 0 8px 8px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
}
.order-badge.sell { background: linear-gradient(135deg, #10b981, #059669); }
.order-badge.buy { background: linear-gradient(135deg, #f59e0b, #d97706); }

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-top: 8px;
}
.asset-tag {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}
.asset-tag.quota {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}
.asset-tag.credit {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
}

/* 订单主体 */
.order-body {
  padding: 16px 0;
  border-top: 1px dashed #e2e8f0;
  border-bottom: 1px dashed #e2e8f0;
}
.order-price {
  margin-bottom: 12px;
}
.price-value {
  font-size: 28px;
  font-weight: 700;
  color: #ef4444;
}
.price-unit {
  font-size: 14px;
  color: #64748b;
  margin-left: 4px;
}
.order-amount,
.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.amount-label, .total-label {
  font-size: 13px;
  color: #64748b;
}
.amount-value, .total-value {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}
.amount-value small { font-weight: 400; color: #64748b; }

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
}
.user-name {
  font-size: 13px;
  color: #64748b;
}
.order-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
}

/* 订单操作 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}

/* 发布对话框样式 */
.publish-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.publish-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
.publish-form {
  padding: 0 12px;
}
.direction-group :deep(.el-radio-button__inner) {
  padding: 12px 24px;
}
.tip-alert {
  margin-top: 16px;
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .market-container { padding: 16px; }
  .page-header { 
    flex-direction: column; 
    text-align: center; 
    padding: 20px;
  }
  .publish-btn { margin-top: 16px; width: 100%; }
  .filter-row { flex-direction: column; align-items: stretch; }
  .filter-item { flex-direction: column; align-items: flex-start; }
  .filter-input, .filter-select { width: 100%; }
  .order-grid { grid-template-columns: 1fr; }
}
</style>

<template>
  <div class="quotes-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1>询报价中心</h1>
        <p>发布询价单，获取市场最优报价</p>
      </div>
      <el-button type="primary" size="large" icon="Plus" class="publish-btn" @click="showPublishDialog">
        发布询价
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="订单类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable class="filter-select">
            <el-option label="买入" value="BUY" />
            <el-option label="卖出" value="SELL" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-select v-model="queryForm.assetType" placeholder="全部" clearable class="filter-select">
            <el-option label="碳配额" value="QUOTA" />
            <el-option label="碳信用" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable class="filter-select">
            <el-option label="待报价" value="PENDING" />
            <el-option label="已报价" value="QUOTED" />
            <el-option label="已接受" value="ACCEPTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadQuotes">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 市场询报价列表 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <el-icon class="title-icon"><Document /></el-icon>
          <span>市场询报价单</span>
        </div>
        <div class="table-stats">
          <span class="stat-item">共 <strong>{{ total }}</strong> 条记录</span>
        </div>
      </div>
      
      <el-table :data="quoteList" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="询价单号" width="100">
          <template #default="scope">
            <span class="quote-id">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="scope">
            <div class="type-badge" :class="scope.row.type === 'BUY' ? 'buy' : 'sell'">
              {{ scope.row.type === 'BUY' ? '买入' : '卖出' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="assetType" label="资产类型" width="100">
          <template #default="scope">
            <span class="asset-type" :class="scope.row.assetType === 'QUOTA' ? 'quota' : 'credit'">
              {{ scope.row.assetType === 'QUOTA' ? '碳配额' : '碳信用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="120">
          <template #default="scope">
            <span class="quantity-value">{{ scope.row.quantity?.toLocaleString() }}</span>
            <span class="quantity-unit">吨</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectPrice" label="期望价格" width="120">
          <template #default="scope">
            <span class="price expect">¥{{ scope.row.expectPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="finalPrice" label="最终报价" width="120">
          <template #default="scope">
            <span v-if="scope.row.finalPrice" class="price final">¥{{ scope.row.finalPrice }}</span>
            <span v-else class="no-price">待报价</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag class="status-tag" :class="getStatusClass(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validUntil" label="有效期至" width="160">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ scope.row.validUntil ? formatDate(scope.row.validUntil) : '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="primary"
                size="small"
                round
                @click="showQuoteDialog(scope.row)"
              >
                报价
              </el-button>
              <el-button
                v-if="scope.row.status === 'QUOTED' && scope.row.userId === currentUserId"
                type="success"
                size="small"
                round
                @click="handleAccept(scope.row.id)"
              >
                接受
              </el-button>
              <el-button
                v-if="scope.row.status !== 'PENDING'"
                type="info"
                size="small"
                text
                @click="viewDetail(scope.row)"
              >
                详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadQuotes"
          @size-change="loadQuotes"
        />
      </div>
    </div>

    <!-- 发布询价对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布询价单" width="520px" class="custom-dialog">
      <el-form :model="publishForm" label-width="100px" class="publish-form">
        <el-form-item label="订单类型">
          <el-radio-group v-model="publishForm.type" class="type-radio">
            <el-radio-button value="BUY">
              <el-icon><ShoppingCart /></el-icon> 买入
            </el-radio-button>
            <el-radio-button value="SELL">
              <el-icon><SoldOut /></el-icon> 卖出
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-radio-group v-model="publishForm.assetType">
            <el-radio value="QUOTA">碳配额</el-radio>
            <el-radio value="CREDIT">碳信用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="publishForm.quantity" :min="1" :max="100000" class="full-width" />
          <span class="unit-label">吨</span>
        </el-form-item>
        <el-form-item label="期望价格">
          <el-input-number v-model="publishForm.expectPrice" :min="0.01" :precision="2" class="full-width" />
          <span class="unit-label">元/吨</span>
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="publishForm.validUntil"
            type="datetime"
            placeholder="选择有效期"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="publishForm.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注信息（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishLoading">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- 报价对话框 -->
    <el-dialog v-model="quoteDialogVisible" title="提交报价" width="450px" class="custom-dialog">
      <div class="quote-info">
        <div class="info-item">
          <span class="info-label">询价单号</span>
          <span class="info-value">#{{ currentQuote.id }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">期望价格</span>
          <span class="info-value price">¥{{ currentQuote.expectPrice }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">数量</span>
          <span class="info-value">{{ currentQuote.quantity?.toLocaleString() }} 吨</span>
        </div>
      </div>
      <el-divider />
      <el-form :model="quoteForm" label-width="100px">
        <el-form-item label="我的报价">
          <el-input-number 
            v-model="quoteForm.finalPrice" 
            :min="0.01" 
            :precision="2" 
            class="full-width"
            size="large"
          />
          <span class="unit-label">元/吨</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quoteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMakeQuote" :loading="quoteLoading">确认报价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMarketQuotes, publishQuote, makeQuote, acceptQuote } from '@/api/trade'
import { useUserStore } from '@/store/user'
import { Search, Refresh, Document, Clock, ShoppingCart, SoldOut, Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const currentUserId = ref(userStore.userId || 1)

// 查询条件
const queryForm = ref({
  type: '',
  assetType: '',
  status: '',
  page: 1,
  size: 10
})

// 询价单列表
const quoteList = ref([])
const total = ref(0)
const loading = ref(false)

// 发布询价
const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishForm = ref({
  userId: userStore.userId || 1,
  type: 'BUY',
  assetType: 'QUOTA',
  quantity: 100,
  expectPrice: 50,
  validUntil: null,
  remark: ''
})

// 报价
const quoteDialogVisible = ref(false)
const quoteLoading = ref(false)
const currentQuote = ref({})
const quoteForm = ref({
  finalPrice: 0
})

// 状态映射
const getStatusClass = (status) => {
  const map = {
    'PENDING': 'warning',
    'QUOTED': 'primary',
    'ACCEPTED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待报价',
    'QUOTED': '已报价',
    'ACCEPTED': '已接受',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

// 加载询报价列表
const loadQuotes = async () => {
  loading.value = true
  try {
    const data = await getMarketQuotes(queryForm.value)
    quoteList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('加载询报价列表失败:', error)
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    type: '',
    assetType: '',
    status: '',
    page: 1,
    size: 10
  }
  loadQuotes()
}

// 显示发布对话框
const showPublishDialog = () => {
  publishForm.value = {
    userId: currentUserId.value,
    type: 'BUY',
    assetType: 'QUOTA',
    quantity: 100,
    expectPrice: 50,
    validUntil: null,
    remark: ''
  }
  publishDialogVisible.value = true
}

// 发布询价
const handlePublish = async () => {
  publishLoading.value = true
  try {
    await publishQuote(publishForm.value)
    ElMessage.success('发布成功')
    publishDialogVisible.value = false
    loadQuotes()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  } finally {
    publishLoading.value = false
  }
}

// 显示报价对话框
const showQuoteDialog = (quote) => {
  currentQuote.value = quote
  quoteForm.value.finalPrice = quote.expectPrice
  quoteDialogVisible.value = true
}

// 查看详情
const viewDetail = (quote) => {
  ElMessageBox.alert(
    `<div style="line-height: 1.8">
      <p><strong>询价单号:</strong> #${quote.id}</p>
      <p><strong>类型:</strong> ${quote.type === 'BUY' ? '买入' : '卖出'}</p>
      <p><strong>资产:</strong> ${quote.assetType === 'QUOTA' ? '碳配额' : '碳信用'}</p>
      <p><strong>数量:</strong> ${quote.quantity?.toLocaleString()} 吨</p>
      <p><strong>期望价格:</strong> ¥${quote.expectPrice}</p>
      <p><strong>最终报价:</strong> ¥${quote.finalPrice || '-'}</p>
      <p><strong>状态:</strong> ${getStatusText(quote.status)}</p>
    </div>`,
    '询价详情',
    { dangerouslyUseHTMLString: true, confirmButtonText: '关闭' }
  )
}

// 提交报价
const handleMakeQuote = async () => {
  if (!quoteForm.value.finalPrice || quoteForm.value.finalPrice <= 0) {
    ElMessage.warning('请输入有效的报价')
    return
  }
  
  quoteLoading.value = true
  try {
    await makeQuote(currentQuote.value.id, quoteForm.value.finalPrice)
    ElMessage.success('报价成功')
    quoteDialogVisible.value = false
    loadQuotes()
  } catch (error) {
    console.error('报价失败:', error)
    ElMessage.error('报价失败')
  } finally {
    quoteLoading.value = false
  }
}

// 接受报价
const handleAccept = async (quoteId) => {
  try {
    await ElMessageBox.confirm('确认接受此报价吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await acceptQuote(quoteId, currentUserId.value)
    ElMessage.success('接受成功')
    loadQuotes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接受失败:', error)
      ElMessage.error('接受失败')
    }
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadQuotes()
})
</script>

<style scoped>
.quotes-container {
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
  padding: 24px 32px;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 50%, #4f46e5 100%);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(99, 102, 241, 0.3);
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
  color: #6366f1;
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
}
.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.2);
  background: #f8fafc;
  color: #4f46e5;
}

/* 筛选卡片 */
.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
}
.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}
.filter-select {
  width: 130px;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.1));
  overflow: hidden;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}
.title-icon {
  color: #8b5cf6;
  font-size: 20px;
}
.table-stats {
  font-size: 14px;
  color: #64748b;
}
.stat-item strong {
  color: #8b5cf6;
}

/* 自定义表格 */
.custom-table {
  --el-table-border-color: #f1f5f9;
  --el-table-header-bg-color: #f8fafc;
}
.custom-table :deep(.el-table__header th) {
  font-weight: 600;
  color: #475569;
  background: #f8fafc;
}
.custom-table :deep(.el-table__row) {
  transition: background 0.2s;
}
.custom-table :deep(.el-table__row:hover > td) {
  background: #faf5ff !important;
}

.quote-id {
  font-family: 'Monaco', monospace;
  color: #64748b;
  font-size: 13px;
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.type-badge.buy {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  color: #dc2626;
}
.type-badge.sell {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  color: #059669;
}

.asset-type {
  font-size: 13px;
  font-weight: 500;
}
.asset-type.quota { color: #059669; }
.asset-type.credit { color: #2563eb; }

.quantity-value {
  font-weight: 600;
  color: #1e293b;
}
.quantity-unit {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 4px;
}

.price {
  font-weight: 600;
}
.price.expect { color: #64748b; }
.price.final { color: #ef4444; }
.no-price {
  color: #94a3b8;
  font-style: italic;
}

.status-tag {
  border: none;
  font-weight: 500;
}
.status-tag.warning { background: #fef3c7; color: #d97706; }
.status-tag.primary { background: #dbeafe; color: #2563eb; }
.status-tag.success { background: #d1fae5; color: #059669; }
.status-tag.danger { background: #fee2e2; color: #dc2626; }

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #f1f5f9;
}

/* 对话框 */
.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  font-weight: 600;
}
.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}
.publish-form {
  padding: 0 12px;
}
.type-radio :deep(.el-radio-button__inner) {
  padding: 12px 24px;
}
.full-width {
  width: 100%;
}
.unit-label {
  margin-left: 8px;
  color: #64748b;
  font-size: 13px;
}

/* 报价信息 */
.quote-info {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px 20px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.info-item:not(:last-child) {
  border-bottom: 1px dashed #e2e8f0;
}
.info-label {
  color: #64748b;
  font-size: 14px;
}
.info-value {
  font-weight: 600;
  color: #1e293b;
}
.info-value.price {
  color: #ef4444;
  font-size: 18px;
}

/* 响应式 */
@media (max-width: 768px) {
  .quotes-container { padding: 16px; }
  .page-header { 
    flex-direction: column; 
    text-align: center;
    padding: 20px;
  }
  .publish-btn { margin-top: 16px; width: 100%; }
  .filter-form { 
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .filter-select { width: 100%; }
}
</style>


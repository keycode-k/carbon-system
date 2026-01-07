<template>
  <div class="app-container">
    <!-- 筛选栏 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="订单类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="买入" value="BUY" />
            <el-option label="卖出" value="SELL" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-select v-model="queryForm.assetType" placeholder="全部" clearable style="width: 120px">
            <el-option label="配额" value="QUOTA" />
            <el-option label="信用" value="CREDIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待报价" value="PENDING" />
            <el-option label="已报价" value="QUOTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuotes">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showPublishDialog">发布询价</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 市场询报价列表 -->
    <el-card>
      <template #header>
        <span>市场询报价单</span>
      </template>
      <el-table :data="quoteList" border stripe v-loading="loading">
        <el-table-column prop="id" label="询价单号" width="100" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'BUY' ? 'danger' : 'success'">
              {{ scope.row.type === 'BUY' ? '买入' : '卖出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetType" label="资产类型" width="100">
          <template #default="scope">
            {{ scope.row.assetType === 'QUOTA' ? '配额' : '信用' }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量（吨）" width="120" />
        <el-table-column prop="expectPrice" label="期望价格" width="120">
          <template #default="scope">¥{{ scope.row.expectPrice }}</template>
        </el-table-column>
        <el-table-column prop="finalPrice" label="报价" width="120">
          <template #default="scope">
            <span v-if="scope.row.finalPrice">¥{{ scope.row.finalPrice }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待报价</el-tag>
            <el-tag v-else-if="scope.row.status === 'QUOTED'" type="primary">已报价</el-tag>
            <el-tag v-else-if="scope.row.status === 'ACCEPTED'" type="success">已接受</el-tag>
            <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validUntil" label="有效期至" width="160">
          <template #default="scope">
            {{ scope.row.validUntil ? formatDate(scope.row.validUntil) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="showQuoteDialog(scope.row)"
            >
              报价
            </el-button>
            <el-button
              v-if="scope.row.status === 'QUOTED' && scope.row.userId === currentUserId"
              type="success"
              size="small"
              @click="handleAccept(scope.row.id)"
            >
              接受
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center"
        @current-change="loadQuotes"
        @size-change="loadQuotes"
      />
    </el-card>

    <!-- 发布询价对话框 -->
    <el-dialog v-model="publishDialogVisible" title="发布询价单" width="500px">
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="订单类型">
          <el-radio-group v-model="publishForm.type">
            <el-radio value="BUY">买入</el-radio>
            <el-radio value="SELL">卖出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资产类型">
          <el-radio-group v-model="publishForm.assetType">
            <el-radio value="QUOTA">配额</el-radio>
            <el-radio value="CREDIT">信用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量（吨）">
          <el-input-number v-model="publishForm.quantity" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="期望价格">
          <el-input-number v-model="publishForm.expectPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="publishForm.validUntil"
            type="datetime"
            placeholder="选择有效期"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="publishForm.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishLoading">发布</el-button>
      </template>
    </el-dialog>

    <!-- 报价对话框 -->
    <el-dialog v-model="quoteDialogVisible" title="报价" width="400px">
      <el-form :model="quoteForm" label-width="100px">
        <el-form-item label="询价单号">
          <el-input v-model="currentQuote.id" disabled />
        </el-form-item>
        <el-form-item label="期望价格">
          <el-input v-model="currentQuote.expectPrice" disabled>
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="报价">
          <el-input-number v-model="quoteForm.finalPrice" :min="0.01" :precision="2" style="width: 100%" />
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

// 使用userStore获取当前用户ID
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

// 初始化
onMounted(() => {
  loadQuotes()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>


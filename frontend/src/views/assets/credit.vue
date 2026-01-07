<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <div class="stat-box bg-blue">
          <div class="title">总持有量 (tCO2e)</div>
          <div class="num">{{ totalAmount.toLocaleString() }}</div>
          <div class="trend">较上月 +0% <el-icon><Top /></el-icon></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-box bg-green">
          <div class="title">今日新增</div>
          <div class="num">2,000</div>
          <div class="trend">-</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-box bg-purple">
          <div class="title">冻结/交易中</div>
          <div class="num">15,000</div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-input v-model="searchQuery" placeholder="项目名称/ID" style="width: 200px; margin-right: 10px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="状态" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value="" />
          <el-option label="持有中" value="active" />
          <el-option label="交易中" value="trading" />
          <el-option label="已注销" value="retired" />
        </el-select>
        <el-button type="primary" icon="Search">搜索</el-button>
        <el-button icon="Refresh">重置</el-button>
        <div style="float: right;">
           <el-button type="success" icon="Plus">登记资产</el-button>
           <el-button type="warning" icon="Download">导出</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectType" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.projectType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="持有量(t)" width="120" sortable />
        <el-table-column prop="issueDate" label="签发日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
              <el-tag v-if="scope.row.status === 0" type="success">持有中</el-tag>
              <el-tag v-else-if="scope.row.status === 1" type="warning">交易中</el-tag>
              <el-tag v-else type="info">已注销</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDetails(scope.row)">详情</el-button>
            <el-button link type="success" size="small" v-if="scope.row.status === 0">挂牌</el-button>
            <el-button link type="danger" size="small" v-if="scope.row.status === 0">注销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, jumper" :total="100" />
      </div>

      <el-dialog v-model="detailsVisible" title="资产详情" width="500px">
        <el-descriptions :column="1" border>
            <el-descriptions-item label="项目名称">{{ currentCredit.projectName }}</el-descriptions-item>
            <el-descriptions-item label="项目类型">{{ currentCredit.projectType }}</el-descriptions-item>
            <el-descriptions-item label="持有量">{{ currentCredit.amount }} tCO2e</el-descriptions-item>
            <el-descriptions-item label="签发日期">{{ currentCredit.issueDate }}</el-descriptions-item>
            <el-descriptions-item label="有效期至">{{ currentCredit.expiryDate || '长期有效' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
                <el-tag v-if="currentCredit.status === 0" type="success">持有中</el-tag>
                <el-tag v-else-if="currentCredit.status === 1" type="warning">交易中</el-tag>
                <el-tag v-else type="info">已注销</el-tag>
            </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getCreditList } from '@/api/assets'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const searchQuery = ref('')
const statusFilter = ref('')

const detailsVisible = ref(false)
const currentCredit = ref({})

const handleDetails = (row) => {
  currentCredit.value = row
  detailsVisible.value = true
}

const totalAmount = computed(() => {
    return tableData.value.reduce((sum, item) => sum + (item.amount || 0), 0)
})

const userStore = useUserStore()

const fetchData = async () => {
    loading.value = true
    const token = localStorage.getItem('token')
    if (!token) {
       // ElMessage.warning('请先登录')
       loading.value = false
       return
    }
    
    try {
        // 从store获取userId
        const userId = userStore.userId || 1
        const res = await getCreditList(userId)
        if (res && res.length !== undefined) {
             // If response is the list directly (due to request interceptor processing)
             tableData.value = res
        } else if (res && res.code === 200) {
            tableData.value = res.data || []
        } else if (Array.isArray(res)) {
             tableData.value = res
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.stat-box {
  padding: 20px;
  border-radius: 8px;
  color: white;
  margin-bottom: 0;
}
.bg-blue { background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%); }
.bg-green { background: linear-gradient(135deg, #52c41a 0%, #95de64 100%); }
.bg-purple { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }

.stat-box .title { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.stat-box .num { font-size: 28px; font-weight: bold; }
.stat-box .trend { margin-top: 10px; font-size: 12px; display: flex; align-items: center; }

.filter-container { margin-bottom: 10px; }
.pagination-container { margin-top: 20px; text-align: right; }
</style>

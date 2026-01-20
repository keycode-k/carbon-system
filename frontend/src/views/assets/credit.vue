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

    <!-- 数据可视化图表 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" style="height: 300px;">
          <template #header>
            <div class="card-header">
              <span>碳信用类型分布</span>
            </div>
          </template>
          <div id="creditTypeChart" style="width: 100%; height: 250px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" style="height: 300px;">
          <template #header>
            <div class="card-header">
              <span>碳信用状态分布</span>
            </div>
          </template>
          <div id="creditStatusChart" style="width: 100%; height: 250px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-input v-model="searchQuery" placeholder="项目名称/ID" style="width: 200px; margin-right: 10px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="状态" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value="" />
          <el-option label="持有中" value="0" />
          <el-option label="交易中" value="1" />
          <el-option label="已注销" value="2" />
        </el-select>
        <el-button type="success" icon="Search" @click="handleSearch" style="background-color: #10b981; border-color: #10b981;">搜索</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
        <div style="float: right;">
           <el-button type="success" icon="Plus" @click="handleRegisterAsset" style="background-color: #10b981; border-color: #10b981;">登记资产</el-button>
           <el-button type="warning" icon="Download" @click="handleExport" style="background-color: #f59e0b; border-color: #f59e0b;">导出</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectType" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small" type="success">{{ scope.row.projectType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="持有量(t)" width="120" sortable />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
              <el-tag v-if="scope.row.status === 0" type="success">持有中</el-tag>
              <el-tag v-else-if="scope.row.status === 1" type="warning">交易中</el-tag>
              <el-tag v-else type="info">已注销</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="issueDate" label="签发日期" width="140" />
        <el-table-column prop="expiryDate" label="有效期至" width="140" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDetails(scope.row)">详情</el-button>
            <el-button link type="success" size="small" v-if="scope.row.status === 0" @click="handleList(scope.row)">挂牌</el-button>
            <el-button link type="danger" size="small" v-if="scope.row.status === 0" @click="handleRetire(scope.row)">注销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination 
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailsVisible" title="资产详情" width="600px">
      <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentCredit.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentCredit.userId }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ currentCredit.projectName }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">{{ currentCredit.projectType }}</el-descriptions-item>
          <el-descriptions-item label="持有量">{{ currentCredit.amount }} tCO2e</el-descriptions-item>
          <el-descriptions-item label="状态">
              <el-tag v-if="currentCredit.status === 0" type="success">持有中</el-tag>
              <el-tag v-else-if="currentCredit.status === 1" type="warning">交易中</el-tag>
              <el-tag v-else type="info">已注销</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="签发日期">{{ currentCredit.issueDate }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ currentCredit.expiryDate || '长期有效' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentCredit.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentCredit.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 登记资产对话框 -->
    <el-dialog v-model="registerDialogVisible" title="登记资产" width="600px">
      <el-form :model="registerForm" label-width="120px" :rules="registerRules">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="registerForm.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目类型" prop="projectType">
          <el-select v-model="registerForm.projectType" placeholder="请选择项目类型">
            <el-option label="风电" value="风电" />
            <el-option label="林业" value="林业" />
            <el-option label="光伏" value="光伏" />
          </el-select>
        </el-form-item>
        <el-form-item label="持有量(t)" prop="amount">
          <el-input-number v-model="registerForm.amount" :min="0" :step="100" placeholder="请输入持有量" />
        </el-form-item>
        <el-form-item label="签发日期" prop="issueDate">
          <el-date-picker v-model="registerForm.issueDate" type="date" placeholder="选择签发日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="有效期至" prop="expiryDate">
          <el-date-picker v-model="registerForm.expiryDate" type="date" placeholder="选择有效期至" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="registerForm.remark" type="textarea" rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="registerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRegister">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { getCreditList, createCredit, listCredit, retireCredit } from '@/api/assets'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Top } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref([])
const searchQuery = ref('')
const statusFilter = ref('')

const detailsVisible = ref(false)
const currentCredit = ref({})
const registerDialogVisible = ref(false)

// 分页相关
const total = ref(100)
const currentPage = ref(1)
const pageSize = ref(10)

// 登记资产表单
const registerForm = ref({
  projectName: '',
  projectType: '',
  amount: 0,
  issueDate: '',
  expiryDate: '',
  remark: ''
})

const registerRules = ref({
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  projectType: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入持有量', trigger: 'blur' },
    { type: 'number', min: 1, message: '持有量必须大于0', trigger: 'blur' }
  ],
  issueDate: [
    { required: true, message: '请选择签发日期', trigger: 'change' }
  ]
})

const handleDetails = (row) => {
  currentCredit.value = row
  detailsVisible.value = true
}

const handleSearch = () => {
  // 执行搜索逻辑
  currentPage.value = 1 // 搜索时重置到第一页
  fetchData()
}

const handleReset = () => {
  searchQuery.value = ''
  statusFilter.value = ''
  currentPage.value = 1 // 重置时也重置到第一页
  fetchData()
}

const handleRegisterAsset = () => {
  // 打开登记资产对话框
  registerDialogVisible.value = true
}

const handleExport = () => {
  // 执行导出逻辑
  exportToExcel()
}

const exportToExcel = () => {
  // 模拟导出为Excel文件
  const data = tableData.value.map(item => ({
    'ID': item.id,
    '项目名称': item.projectName,
    '类型': item.projectType,
    '持有量(t)': item.amount,
    '签发日期': item.issueDate,
    '状态': item.status === 0 ? '持有中' : item.status === 1 ? '交易中' : '已注销'
  }))
  
  // 创建CSV内容
  const headers = Object.keys(data[0] || {})
  const csvContent = [
    headers.join(','),
    ...data.map(row => headers.map(header => row[header]).join(','))
  ].join('\n')
  
  // 创建并下载文件
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `碳信用资产_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('导出成功')
}

const handleList = async (row) => {
  try {
    // 调用后端API进行挂牌操作
    const response = await listCredit(row.id)
    console.log('挂牌操作响应:', response)
    
    // 操作成功后刷新数据
    await fetchData()
    ElMessage.success('资产已成功挂牌')
  } catch (error) {
    console.error('挂牌操作失败:', error)
    ElMessage.error('挂牌失败：' + (error.message || '未知错误'))
  }
}

const handleRetire = async (row) => {
  try {
    // 调用后端API进行注销操作
    const response = await retireCredit(row.id)
    console.log('注销操作响应:', response)
    
    // 操作成功后刷新数据
    await fetchData()
    ElMessage.success('资产已成功注销')
  } catch (error) {
    console.error('注销操作失败:', error)
    ElMessage.error('注销失败：' + (error.message || '未知错误'))
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchData()
}

const submitRegister = async () => {
  try {
    // 获取当前用户ID
    const userId = userStore.userId || localStorage.getItem('userId') || 1
    
    // 格式化日期为YYYY-MM-DD格式
    const formatDate = (date) => {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    
    // 准备要发送到后端的数据
    const creditData = {
      userId: userId,
      projectName: registerForm.value.projectName,
      projectType: registerForm.value.projectType,
      amount: Number(registerForm.value.amount), // 确保转换为数字类型
      status: 0, // 默认为持有中
      issueDate: formatDate(registerForm.value.issueDate),
      expiryDate: formatDate(registerForm.value.expiryDate),
      remark: registerForm.value.remark
    }
    
    // 调用后端API创建碳信用资产
    console.log('发送登记数据:', creditData)
    const response = await createCredit(creditData)
    console.log('后端响应:', response)
    
    // 由于response拦截器已经处理了code字段，直接检查response是否存在即可
    if (response) {
      // 创建成功后，重新加载数据以显示最新的数据
      await fetchData()
      
      ElMessage.success('资产登记成功，数据已保存到数据库')
      registerDialogVisible.value = false
      
      // 重置表单
      registerForm.value = {
        projectName: '',
        projectType: '',
        amount: 0,
        issueDate: '',
        expiryDate: '',
        remark: ''
      }
    } else {
      ElMessage.error('资产登记失败：未知错误')
    }
  } catch (error) {
    console.error('登记资产时发生错误:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('登记资产失败：' + (error.response?.data?.message || error.message || '请检查网络连接或联系管理员'))
  }
}

const totalAmount = computed(() => {
    return tableData.value.reduce((sum, item) => sum + (item.amount || 0), 0)
})

const userStore = useUserStore()

const fetchData = async () => {
    loading.value = true
    
    try {
        // 优先从 store，其次 localStorage，最后默认 1
        const userId = userStore.userId || localStorage.getItem('userId') || 1
        console.log('Loading credits for userId:', userId)
        console.log('Search parameters:', {
            searchQuery: searchQuery.value,
            statusFilter: statusFilter.value,
            currentPage: currentPage.value,
            pageSize: pageSize.value
        })
        
        // 模拟API调用，实际项目中应传递搜索参数
        const res = await getCreditList(userId)
        console.log('Credit API response:', res)
        
        // 处理各种返回格式
        if (Array.isArray(res)) {
            tableData.value = res
        } else if (res && res.data && Array.isArray(res.data)) {
            tableData.value = res.data
        } else if (res && res.length !== undefined) {
            tableData.value = res
        } else {
            tableData.value = []
            console.warn('Unexpected response format:', res)
        }
        
        // 清理异常数据（确保所有字段都有正确的值）
        tableData.value = tableData.value.filter(item => {
            return item.id && item.projectName && item.projectType && item.amount
        })
        
        // 模拟搜索过滤（实际项目中应由后端处理）
        if (searchQuery.value || statusFilter.value) {
            tableData.value = tableData.value.filter(item => {
                const matchesSearch = !searchQuery.value || 
                    (item.projectName && item.projectName.includes(searchQuery.value)) ||
                    (item.id && item.id.toString().includes(searchQuery.value))
                const matchesStatus = !statusFilter.value || 
                    item.status === parseInt(statusFilter.value)
                return matchesSearch && matchesStatus
            })
            total.value = tableData.value.length
        } else {
            total.value = tableData.value.length // 使用实际数据条数
        }
        
        // 按ID排序（与截图一致）
        tableData.value.sort((a, b) => a.id - b.id)
    } catch (e) {
        console.error('加载碳信用数据失败:', e)
        ElMessage.error('加载数据失败，请刷新重试')
        tableData.value = []
    } finally {
        loading.value = false
        // 数据加载完成后重新初始化图表
        initCharts()
    }
}

const initCharts = () => {
  nextTick(() => {
    // 初始化碳信用类型分布图表
    const typeChart = echarts.init(document.getElementById('creditTypeChart'))
    const typeData = processTypeData()
    typeChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} tCO2e ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: typeData.map(item => item.name)
      },
      series: [
        {
          name: '碳信用类型',
          type: 'pie',
          radius: '60%',
          data: typeData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
    
    // 初始化碳信用状态分布图表
    const statusChart = echarts.init(document.getElementById('creditStatusChart'))
    const statusData = processStatusData()
    statusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 个 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: statusData.map(item => item.name)
      },
      series: [
        {
          name: '碳信用状态',
          type: 'pie',
          radius: '60%',
          data: statusData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      typeChart.resize()
      statusChart.resize()
    })
  })
}

const processTypeData = () => {
  const typeMap = {}
  tableData.value.forEach(item => {
    const type = item.projectType || '其他'
    typeMap[type] = (typeMap[type] || 0) + (item.amount || 0)
  })
  
  const result = Object.entries(typeMap).map(([name, value]) => ({
    name,
    value
  }))
  
  // 确保至少返回一个数据项，避免图表显示异常
  return result.length > 0 ? result : [{ name: '无数据', value: 1 }]
}

const processStatusData = () => {
  const statusMap = {
    '持有中': 0,
    '交易中': 0,
    '已注销': 0
  }
  
  tableData.value.forEach(item => {
    if (item.status === 0) {
      statusMap['持有中']++
    } else if (item.status === 1) {
      statusMap['交易中']++
    } else {
      statusMap['已注销']++
    }
  })
  
  const result = Object.entries(statusMap).map(([name, value]) => ({
    name,
    value
  }))
  
  // 确保至少返回一个数据项，避免图表显示异常
  return result.length > 0 ? result : [{ name: '无数据', value: 1 }]
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.stat-box {
  padding: 20px;
  border-radius: 8px;
  color: white;
  height: 120px;
  display: flex;
  flex-direction: column;
}

.bg-blue {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.bg-green {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.bg-purple {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.stat-box .title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-box .num {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
  flex: 1;
  display: flex;
  align-items: center;
}

.stat-box .trend {
  font-size: 12px;
  opacity: 0.8;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 自定义表格样式 */
:deep(.el-table__header-wrapper th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.el-table__body-wrapper tr:hover > td) {
  background-color: #f0f9eb !important;
}
</style>
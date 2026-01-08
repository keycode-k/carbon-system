<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="操作用户">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input v-model="queryParams.module" placeholder="请输入模块" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.operationType" placeholder="请选择" clearable style="width: 130px">
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 工具栏 -->
      <el-row :gutter="10" class="mb10">
        <el-col :span="1.5">
          <el-button type="danger" plain :icon="Delete" @click="handleClean">清理日志</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain :icon="Download" @click="handleExport">导出</el-button>
        </el-col>
      </el-row>

      <!-- 日志表格 -->
      <el-table v-loading="loading" :data="logList" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="操作用户" width="100" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.operationType)">
              {{ getTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" width="150" show-overflow-tooltip />
        <el-table-column prop="requestUrl" label="请求地址" width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="操作IP" width="130" />
        <el-table-column prop="browser" label="浏览器" width="100" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="90" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getTypeColor(currentLog.operationType)">
            {{ getTypeName(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ currentLog.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ currentLog.os }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ currentLog.duration }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider v-if="currentLog.requestParams" content-position="left">请求参数</el-divider>
      <el-input
        v-if="currentLog.requestParams"
        v-model="currentLog.requestParams"
        type="textarea"
        :rows="4"
        readonly
      />
      
      <el-divider v-if="currentLog.responseResult" content-position="left">响应结果</el-divider>
      <el-input
        v-if="currentLog.responseResult"
        v-model="currentLog.responseResult"
        type="textarea"
        :rows="4"
        readonly
      />
      
      <el-divider v-if="currentLog.errorMsg" content-position="left">错误信息</el-divider>
      <el-alert
        v-if="currentLog.errorMsg"
        :title="currentLog.errorMsg"
        type="error"
        :closable="false"
      />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog v-model="cleanVisible" title="清理操作日志" width="400px">
      <el-form :model="cleanForm" label-width="100px">
        <el-form-item label="保留天数">
          <el-input-number v-model="cleanForm.days" :min="1" :max="365" />
          <span style="margin-left: 10px; color: #909399;">天前的日志将被删除</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleanVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmClean">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, Download, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOperationLogList, getOperationLogDetail, cleanOperationLog } from '@/api/log'

// 查询参数
const queryParams = reactive({
  username: '',
  module: '',
  operationType: '',
  status: null,
  startTime: '',
  endTime: '',
  page: 1,
  size: 10
})

// 日期范围
const dateRange = ref([])

// 数据列表
const logList = ref([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailVisible = ref(false)
const currentLog = ref({})

// 清理对话框
const cleanVisible = ref(false)
const cleanForm = reactive({
  days: 30
})

// 操作类型映射
const typeMap = {
  CREATE: { name: '新增', color: 'success' },
  UPDATE: { name: '修改', color: 'primary' },
  DELETE: { name: '删除', color: 'danger' },
  QUERY: { name: '查询', color: 'info' },
  LOGIN: { name: '登录', color: 'warning' },
  LOGOUT: { name: '登出', color: 'info' },
  EXPORT: { name: '导出', color: '' },
  IMPORT: { name: '导入', color: '' },
  OTHER: { name: '其他', color: 'info' }
}

const getTypeName = (type) => {
  return typeMap[type]?.name || type
}

const getTypeColor = (type) => {
  return typeMap[type]?.color || 'info'
}

// 获取日志列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围，后端需要完整的日期时间格式
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0] + ' 00:00:00'
      queryParams.endTime = dateRange.value[1] + ' 23:59:59'
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    
    const res = await getOperationLogList(queryParams)
    // request.js 已经解包，返回的直接是 Page 对象
    logList.value = res.records || res.data?.records || []
    total.value = res.total || res.data?.total || 0
  } catch (error) {
    console.error('获取日志列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.username = ''
  queryParams.module = ''
  queryParams.operationType = ''
  queryParams.status = null
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  queryParams.page = 1
  getList()
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const res = await getOperationLogDetail(row.id)
    // request.js 已经解包
    currentLog.value = res.data || res
    detailVisible.value = true
  } catch (error) {
    // 如果详情接口失败，直接使用列表数据
    currentLog.value = row
    detailVisible.value = true
  }
}

// 清理日志
const handleClean = () => {
  cleanVisible.value = true
}

// 确认清理
const confirmClean = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${cleanForm.days} 天前的操作日志吗？此操作不可恢复！`,
      '警告',
      { type: 'warning' }
    )
    
    await cleanOperationLog(cleanForm.days)
    ElMessage.success('清理成功')
    cleanVisible.value = false
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理日志失败:', error)
    }
  }
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 15px;
}

.mb10 {
  margin-bottom: 10px;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style>

<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>方法学管理</span>
          <el-button type="primary" @click="handleAdd">新增方法学</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="方法学名称">
          <el-input v-model="searchForm.name" placeholder="请输入方法学名称" clearable />
        </el-form-item>
        <el-form-item label="适用领域">
          <el-select v-model="searchForm.category" placeholder="请选择领域" clearable>
            <el-option label="林业碳汇" value="林业碳汇" />
            <el-option label="可再生能源" value="可再生能源" />
            <el-option label="节能减排" value="节能减排" />
            <el-option label="废弃物处理" value="废弃物处理" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="有效" value="ACTIVE" />
            <el-option label="草案" value="DRAFT" />
            <el-option label="已废弃" value="DEPRECATED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="code" label="方法学编号" width="150" />
        <el-table-column prop="name" label="方法学名称" min-width="200" />
        <el-table-column prop="category" label="适用领域" width="120" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="publisher" label="发布机构" width="120" />
        <el-table-column prop="publishDate" label="发布日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="方法学编号" prop="code">
          <el-input v-model="form.code" placeholder="如: CM-001-V01" />
        </el-form-item>
        <el-form-item label="方法学名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入方法学名称" />
        </el-form-item>
        <el-form-item label="适用领域" prop="category">
          <el-select v-model="form.category" placeholder="请选择适用领域" style="width: 100%">
            <el-option label="林业碳汇" value="林业碳汇" />
            <el-option label="可再生能源" value="可再生能源" />
            <el-option label="节能减排" value="节能减排" />
            <el-option label="废弃物处理" value="废弃物处理" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="如: V1.0" />
        </el-form-item>
        <el-form-item label="发布机构" prop="publisher">
          <el-input v-model="form.publisher" placeholder="如: 国家发改委" />
        </el-form-item>
        <el-form-item label="发布日期" prop="publishDate">
          <el-date-picker
            v-model="form.publishDate"
            type="datetime"
            placeholder="选择发布日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">有效</el-radio>
            <el-radio label="DRAFT">草案</el-radio>
            <el-radio label="DEPRECATED">已废弃</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="方法学描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入方法学描述"
          />
        </el-form-item>
        <el-form-item label="文件URL">
          <el-input v-model="form.fileUrl" placeholder="方法学文件下载地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="方法学详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="方法学编号">{{ detailData.code }}</el-descriptions-item>
        <el-descriptions-item label="方法学名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="适用领域">{{ detailData.category }}</el-descriptions-item>
        <el-descriptions-item label="版本号">{{ detailData.version }}</el-descriptions-item>
        <el-descriptions-item label="发布机构">{{ detailData.publisher }}</el-descriptions-item>
        <el-descriptions-item label="发布日期">{{ formatDate(detailData.publishDate) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件URL" :span="2">
          <el-link v-if="detailData.fileUrl" :href="detailData.fileUrl" type="primary" target="_blank">
            下载方法学文件
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="方法学描述" :span="2">
          {{ detailData.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailVisible = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  id: null,
  code: '',
  name: '',
  category: '',
  version: '',
  publisher: '',
  publishDate: '',
  status: 'ACTIVE',
  description: '',
  fileUrl: ''
})

const detailData = ref({})

// 表单验证规则
const rules = {
  code: [{ required: true, message: '请输入方法学编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入方法学名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择适用领域', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request({
      url: '/api/development/methodology/list',
      method: 'get',
      params
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.status = ''
  pagination.page = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增方法学'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑方法学'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  detailData.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该方法学吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/api/development/methodology/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const url = form.id
      ? `/api/development/methodology/${form.id}`
      : '/api/development/methodology'
    const method = form.id ? 'put' : 'post'
    
    await request({
      url,
      method,
      data: form
    })
    
    ElMessage.success(form.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.code = ''
  form.name = ''
  form.category = ''
  form.version = ''
  form.publisher = ''
  form.publishDate = ''
  form.status = 'ACTIVE'
  form.description = ''
  form.fileUrl = ''
}

// 分页变化
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 状态类型
const getStatusType = (status) => {
  const typeMap = {
    ACTIVE: 'success',
    DRAFT: 'warning',
    DEPRECATED: 'info'
  }
  return typeMap[status] || 'info'
}

// 状态文本
const getStatusText = (status) => {
  const textMap = {
    ACTIVE: '有效',
    DRAFT: '草案',
    DEPRECATED: '已废弃'
  }
  return textMap[status] || status
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>


<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>开发项目管理</span>
          <el-button type="primary" @click="handleAdd">新建项目</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.name" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="适用方法学">
          <el-select v-model="searchForm.methodologyId" placeholder="请选择方法学" clearable style="width: 200px">
            <el-option
              v-for="item in methodologyList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="已立项" value="PLANNED" />
            <el-option label="备案中" value="REGISTERED" />
            <el-option label="已核证" value="VERIFIED" />
            <el-option label="已签发" value="ISSUED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="projectType" label="项目类型" width="120" />
        <el-table-column prop="methodologyName" label="适用方法学" width="180" />
        <el-table-column prop="location" label="项目地点" width="150" />
        <el-table-column prop="estimatedEmissionReduction" label="预估减排量(tCO2e)" width="150">
          <template #default="{ row }">
            {{ row.estimatedEmissionReduction ? row.estimatedEmissionReduction.toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currentStep" label="当前阶段" width="120">
          <template #default="{ row }">
            <el-tag>{{ getStepText(row.currentStep) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="goToWorkbench(row)">工作台</el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目类型" prop="projectType">
          <el-select v-model="form.projectType" placeholder="请选择项目类型" style="width: 100%">
            <el-option label="风电" value="风电" />
            <el-option label="光伏" value="光伏" />
            <el-option label="林业碳汇" value="林业碳汇" />
            <el-option label="节能提效" value="节能提效" />
            <el-option label="废弃物处理" value="废弃物处理" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用方法学" prop="methodologyId">
          <el-select v-model="form.methodologyId" placeholder="请选择适用方法学" style="width: 100%">
            <el-option
              v-for="item in methodologyList"
              :key="item.id"
              :label="`${item.code} - ${item.name}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目地点" prop="location">
          <el-input v-model="form.location" placeholder="如：内蒙古阿拉善左旗" />
        </el-form-item>
        <el-form-item label="预估减排量" prop="estimatedEmissionReduction">
          <el-input-number
            v-model="form.estimatedEmissionReduction"
            :min="0"
            :precision="2"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399">tCO2e/年</span>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="PLANNED">已立项</el-radio>
            <el-radio value="REGISTERED">备案中</el-radio>
            <el-radio value="VERIFIED">已核证</el-radio>
            <el-radio value="ISSUED">已签发</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="当前阶段" prop="currentStep">
          <el-select v-model="form.currentStep" placeholder="请选择当前阶段" style="width: 100%">
            <el-option :value="0" label="立项" />
            <el-option :value="1" label="PDD设计" />
            <el-option :value="2" label="第三方审定" />
            <el-option :value="3" label="主管部门备案" />
            <el-option :value="4" label="减排量核证" />
            <el-option :value="5" label="资产签发" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="项目详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目名称" :span="2">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">{{ detailData.projectType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适用方法学">{{ detailData.methodologyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目地点">{{ detailData.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预估减排量">
          {{ detailData.estimatedEmissionReduction ? detailData.estimatedEmissionReduction.toLocaleString() + ' tCO2e' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="项目状态">
          <el-tag :type="getStatusType(detailData.status)">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前阶段">
          {{ getStepText(detailData.currentStep) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(detailData.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">
          {{ detailData.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 项目阶段流程 -->
      <div class="step-container">
        <h4>开发进度</h4>
        <el-steps :active="detailData.currentStep || 0" finish-status="success" align-center>
          <el-step title="立项" description="提交项目建议书" />
          <el-step title="PDD设计" description="编写项目设计文件" />
          <el-step title="第三方审定" description="DOE现场审定" />
          <el-step title="主管部门备案" description="发改委/生态部备案" />
          <el-step title="减排量核证" description="监测与核证" />
          <el-step title="资产签发" description="CCER上市" />
        </el-steps>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getProjectList,
  getProjectById,
  createProject,
  updateProject,
  deleteProject
} from '@/api/development'
import request from '@/utils/request'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  methodologyId: '',
  status: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 方法学列表（用于下拉选择）
const methodologyList = ref([])

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
  name: '',
  projectType: '',
  methodologyId: null,
  location: '',
  estimatedEmissionReduction: null,
  status: 'PLANNED',
  currentStep: 0,
  description: ''
})

const detailData = ref({})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  methodologyId: [{ required: true, message: '请选择适用方法学', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
}

// 加载方法学列表
const loadMethodologies = async () => {
  try {
    const res = await request({
      url: '/api/development/methodology/active',
      method: 'get'
    })
    methodologyList.value = res || []
  } catch (error) {
    console.error('加载方法学列表失败', error)
  }
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
    const res = await getProjectList(params)
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
  searchForm.methodologyId = ''
  searchForm.status = ''
  pagination.page = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新建开发项目'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑开发项目'
  try {
    const data = await getProjectById(row.id)
    Object.assign(form, data)
  } catch (error) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

// 查看
const handleView = async (row) => {
  try {
    const data = await getProjectById(row.id)
    detailData.value = data
  } catch (error) {
    detailData.value = row
  }
  detailVisible.value = true
}

// 跳转到工作台
const goToWorkbench = (row) => {
  router.push({
    path: '/development/workbench',
    query: { projectId: row.id }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该项目吗？删除后不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProject(row.id)
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
    if (form.id) {
      await updateProject(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createProject(form)
      ElMessage.success('创建成功')
    }
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
  form.name = ''
  form.projectType = ''
  form.methodologyId = null
  form.location = ''
  form.estimatedEmissionReduction = null
  form.status = 'PLANNED'
  form.currentStep = 0
  form.description = ''
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
  return new Date(date).toLocaleString('zh-CN')
}

// 状态类型
const getStatusType = (status) => {
  const typeMap = {
    PLANNED: 'info',
    REGISTERED: 'warning',
    VERIFIED: 'success',
    ISSUED: ''
  }
  return typeMap[status] || 'info'
}

// 状态文本
const getStatusText = (status) => {
  const textMap = {
    PLANNED: '已立项',
    REGISTERED: '备案中',
    VERIFIED: '已核证',
    ISSUED: '已签发'
  }
  return textMap[status] || status
}

// 阶段文本
const getStepText = (step) => {
  const steps = ['立项', 'PDD设计', '第三方审定', '主管部门备案', '减排量核证', '资产签发']
  return steps[step] || '未知'
}

onMounted(() => {
  loadMethodologies()
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

.step-container {
  margin-top: 30px;
  padding: 20px;
  background: #fafafa;
  border-radius: 4px;
}

.step-container h4 {
  margin-bottom: 20px;
  color: #303133;
}
</style>

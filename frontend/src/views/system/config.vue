<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="配置名称">
          <el-input v-model="queryParams.configName" placeholder="请输入配置名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="配置键名">
          <el-input v-model="queryParams.configKey" placeholder="请输入配置键名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="配置分组">
          <el-select v-model="queryParams.configGroup" placeholder="请选择" clearable style="width: 130px">
            <el-option v-for="group in groupOptions" :key="group" :label="group" :value="group" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置类型">
          <el-select v-model="queryParams.configType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="系统内置" :value="1" />
            <el-option label="自定义" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 工具栏 -->
      <el-row :gutter="10" class="mb10">
        <el-col :span="1.5">
          <el-button type="primary" plain :icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain :icon="RefreshRight" @click="handleRefreshCache">刷新缓存</el-button>
        </el-col>
      </el-row>

      <!-- 配置表格 -->
      <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="55" :selectable="row => row.configType !== 1" />
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="configName" label="配置名称" width="180" show-overflow-tooltip />
        <el-table-column prop="configKey" label="配置键名" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.configKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configGroup" label="分组" width="100">
          <template #default="{ row }">
            <el-tag :type="getGroupType(row.configGroup)" size="small">{{ row.configGroup }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'warning' : 'success'" size="small">
              {{ row.configType === 1 ? '内置' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" :disabled="row.configType === 1" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键名" :disabled="form.id && form.configType === 1" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="配置分组" prop="configGroup">
          <el-select v-model="form.configGroup" placeholder="请选择或输入分组" filterable allow-create style="width: 100%">
            <el-option v-for="group in groupOptions" :key="group" :label="group" :value="group" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-radio-group v-model="form.configType" :disabled="!!form.id">
            <el-radio :value="1">系统内置</el-radio>
            <el-radio :value="2">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Edit, Delete, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getConfigList, 
  getConfigById,
  getConfigGroups,
  createConfig, 
  updateConfig, 
  deleteConfig,
  batchDeleteConfig,
  refreshConfigCache 
} from '@/api/system'

// 查询参数
const queryParams = reactive({
  configName: '',
  configKey: '',
  configGroup: '',
  configType: null,
  page: 1,
  size: 10
})

// 数据列表
const configList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedIds = ref([])
const groupOptions = ref(['system', 'account', 'trade', 'upload', 'notification', 'log'])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

// 表单
const form = reactive({
  id: null,
  configName: '',
  configKey: '',
  configValue: '',
  configGroup: 'default',
  configType: 2,
  status: 1,
  remark: ''
})

// 表单校验规则
const rules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configKey: [
    { required: true, message: '请输入配置键名', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9._-]*$/, message: '键名以字母开头，只能包含字母、数字、点、下划线和横线', trigger: 'blur' }
  ],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configGroup: [{ required: true, message: '请选择配置分组', trigger: 'change' }]
}

// 分组类型映射
const groupTypeMap = {
  system: 'primary',
  account: 'success',
  trade: 'warning',
  upload: 'info',
  notification: 'danger',
  log: ''
}

const getGroupType = (group) => {
  return groupTypeMap[group] || 'info'
}

// 获取配置列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getConfigList(queryParams)
    // request.js 已经解包，返回的直接是 Page 对象
    configList.value = res.records || res.data?.records || []
    total.value = res.total || res.data?.total || 0
  } catch (error) {
    console.error('获取配置列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取配置分组
const loadGroups = async () => {
  try {
    const res = await getConfigGroups()
    // request.js 已经解包，返回的直接是数组
    if (Array.isArray(res)) {
      groupOptions.value = res
    } else if (res.data?.length) {
      groupOptions.value = res.data
    }
  } catch (error) {
    console.error('获取分组失败:', error)
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.configName = ''
  queryParams.configKey = ''
  queryParams.configGroup = ''
  queryParams.configType = null
  queryParams.page = 1
  getList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增配置'
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑配置'
  try {
    const res = await getConfigById(row.id)
    // request.js 已经解包
    Object.assign(form, res.data || res)
  } catch (error) {
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除配置"${row.configName}"吗？`, '提示', { type: 'warning' })
    await deleteConfig(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedIds.value.length}条配置吗？`, '提示', { type: 'warning' })
    const res = await batchDeleteConfig(selectedIds.value)
    ElMessage.success(res || '删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await refreshConfigCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    console.error('刷新缓存失败:', error)
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value?.validate()
    
    const isEdit = !!form.id
    isEdit ? await updateConfig(form) : await createConfig(form)
    
    ElMessage.success(isEdit ? '更新成功' : '创建成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.configName = ''
  form.configKey = ''
  form.configValue = ''
  form.configGroup = 'default'
  form.configType = 2
  form.status = 1
  form.remark = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
  loadGroups()
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

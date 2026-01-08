<template>
  <div class="app-container">
    <!-- 左侧字典类型列表 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="handleAddType">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
            </div>
          </template>
          
          <!-- 搜索框 -->
          <el-input
            v-model="typeSearchKey"
            placeholder="搜索字典类型"
            clearable
            style="margin-bottom: 15px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <!-- 字典类型列表 -->
          <div class="type-list" v-loading="typeLoading">
            <div
              v-for="item in filteredTypeList"
              :key="item.id"
              class="type-item"
              :class="{ active: currentType && currentType.id === item.id }"
              @click="selectType(item)"
            >
              <div class="type-info">
                <div class="type-name">{{ item.dictName }}</div>
                <div class="type-code">{{ item.dictType }}</div>
              </div>
              <div class="type-actions">
                <el-button link type="primary" size="small" @click.stop="handleEditType(item)">编辑</el-button>
                <el-button link type="danger" size="small" @click.stop="handleDeleteType(item)">删除</el-button>
              </div>
            </div>
            <el-empty v-if="filteredTypeList.length === 0" description="暂无字典类型" />
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧字典数据列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>
                字典数据
                <el-tag v-if="currentType" type="info" style="margin-left: 10px">{{ currentType.dictName }}</el-tag>
              </span>
              <el-button type="primary" size="small" :disabled="!currentType" @click="handleAddData">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
            </div>
          </template>
          
          <el-table :data="dataList" border stripe v-loading="dataLoading">
            <el-table-column prop="dictLabel" label="字典标签" min-width="120" />
            <el-table-column prop="dictValue" label="字典键值" width="120" />
            <el-table-column prop="dictSort" label="排序" width="80" />
            <el-table-column prop="listClass" label="样式" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.listClass" :type="scope.row.listClass">{{ scope.row.dictLabel }}</el-tag>
                <span v-else>{{ scope.row.dictLabel }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button link type="primary" @click="handleEditData(scope.row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteData(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型对话框 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="500px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType" :loading="typeSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="500px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签（显示名称）" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典键值（实际值）" />
        </el-form-item>
        <el-form-item label="显示样式" prop="listClass">
          <el-select v-model="dataForm.listClass" placeholder="选择样式" clearable>
            <el-option label="默认" value="" />
            <el-option label="主要(primary)" value="primary" />
            <el-option label="成功(success)" value="success" />
            <el-option label="信息(info)" value="info" />
            <el-option label="警告(warning)" value="warning" />
            <el-option label="危险(danger)" value="danger" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitData" :loading="dataSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { 
  getDictTypeList, createDictType, updateDictType, deleteDictType,
  getDictDataByType, createDictData, updateDictData, deleteDictData 
} from '@/api/system'

// 字典类型相关
const typeList = ref([])
const typeLoading = ref(false)
const typeSearchKey = ref('')
const currentType = ref(null)

// 字典数据相关
const dataList = ref([])
const dataLoading = ref(false)

// 字典类型表单
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const typeFormRef = ref(null)
const typeSubmitLoading = ref(false)
const typeForm = reactive({
  id: null,
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
})
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

// 字典数据表单
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增字典数据')
const dataFormRef = ref(null)
const dataSubmitLoading = ref(false)
const dataForm = reactive({
  id: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  listClass: '',
  dictSort: 0,
  status: 1,
  remark: ''
})
const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

// 过滤后的字典类型列表
const filteredTypeList = computed(() => {
  if (!typeSearchKey.value) return typeList.value
  const key = typeSearchKey.value.toLowerCase()
  return typeList.value.filter(item => 
    item.dictName.toLowerCase().includes(key) || 
    item.dictType.toLowerCase().includes(key)
  )
})

// 加载字典类型
const loadTypeList = async () => {
  try {
    typeLoading.value = true
    const res = await getDictTypeList({ page: 1, size: 100 })
    if (res && res.records) {
      typeList.value = res.records
    }
  } catch (error) {
    console.error('加载字典类型失败:', error)
  } finally {
    typeLoading.value = false
  }
}

// 选择字典类型
const selectType = async (item) => {
  currentType.value = item
  try {
    dataLoading.value = true
    const res = await getDictDataByType(item.dictType)
    if (res) {
      dataList.value = res
    }
  } catch (error) {
    console.error('加载字典数据失败:', error)
  } finally {
    dataLoading.value = false
  }
}

// 新增字典类型
const handleAddType = () => {
  typeDialogTitle.value = '新增字典类型'
  typeForm.id = null
  typeForm.dictName = ''
  typeForm.dictType = ''
  typeForm.status = 1
  typeForm.remark = ''
  typeDialogVisible.value = true
}

// 编辑字典类型
const handleEditType = (item) => {
  typeDialogTitle.value = '编辑字典类型'
  typeForm.id = item.id
  typeForm.dictName = item.dictName
  typeForm.dictType = item.dictType
  typeForm.status = item.status
  typeForm.remark = item.remark
  typeDialogVisible.value = true
}

// 提交字典类型
const submitType = async () => {
  try {
    await typeFormRef.value.validate()
    typeSubmitLoading.value = true
    
    if (typeForm.id) {
      await updateDictType(typeForm.id, typeForm)
      ElMessage.success('更新成功')
    } else {
      await createDictType(typeForm)
      ElMessage.success('创建成功')
    }
    
    typeDialogVisible.value = false
    loadTypeList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    typeSubmitLoading.value = false
  }
}

// 删除字典类型
const handleDeleteType = (item) => {
  ElMessageBox.confirm(`确定要删除字典类型"${item.dictName}"吗？\n将同时删除该类型下的所有字典数据！`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictType(item.id)
      ElMessage.success('删除成功')
      if (currentType.value && currentType.value.id === item.id) {
        currentType.value = null
        dataList.value = []
      }
      loadTypeList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 新增字典数据
const handleAddData = () => {
  dataDialogTitle.value = '新增字典数据'
  dataForm.id = null
  dataForm.dictType = currentType.value.dictType
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.listClass = ''
  dataForm.dictSort = 0
  dataForm.status = 1
  dataForm.remark = ''
  dataDialogVisible.value = true
}

// 编辑字典数据
const handleEditData = (row) => {
  dataDialogTitle.value = '编辑字典数据'
  dataForm.id = row.id
  dataForm.dictType = row.dictType
  dataForm.dictLabel = row.dictLabel
  dataForm.dictValue = row.dictValue
  dataForm.listClass = row.listClass
  dataForm.dictSort = row.dictSort
  dataForm.status = row.status
  dataForm.remark = row.remark
  dataDialogVisible.value = true
}

// 提交字典数据
const submitData = async () => {
  try {
    await dataFormRef.value.validate()
    dataSubmitLoading.value = true
    
    if (dataForm.id) {
      await updateDictData(dataForm.id, dataForm)
      ElMessage.success('更新成功')
    } else {
      await createDictData(dataForm)
      ElMessage.success('创建成功')
    }
    
    dataDialogVisible.value = false
    selectType(currentType.value)
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    dataSubmitLoading.value = false
  }
}

// 删除字典数据
const handleDeleteData = (row) => {
  ElMessageBox.confirm(`确定要删除字典数据"${row.dictLabel}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDictData(row.id)
      ElMessage.success('删除成功')
      selectType(currentType.value)
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadTypeList()
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

.type-list {
  max-height: 500px;
  overflow-y: auto;
}

.type-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.type-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.type-info {
  flex: 1;
}

.type-name {
  font-weight: 500;
  color: #303133;
}

.type-code {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.type-actions {
  flex-shrink: 0;
}
</style>

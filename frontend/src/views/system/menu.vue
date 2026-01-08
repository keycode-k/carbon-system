<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="success" @click="handleAdd(0)">
            <el-icon><Plus /></el-icon> 新增菜单
          </el-button>
          <el-button @click="toggleExpand">
            <el-icon><Sort /></el-icon> {{ isExpand ? '折叠' : '展开' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 菜单树形表格 -->
    <el-card>
      <el-table 
        :data="menuList" 
        row-key="id" 
        border
        :tree-props="{ children: 'children' }"
        :default-expand-all="isExpand"
        v-loading="loading">
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="scope">
            <el-icon v-if="scope.row.icon">
              <component :is="scope.row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.menuType === 'M'" type="primary">目录</el-tag>
            <el-tag v-else-if="scope.row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else-if="scope.row.menuType === 'F'" type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuPath" label="路由路径" width="180" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" width="180" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" width="180" show-overflow-tooltip />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.menuType !== 'F'" link type="primary" @click="handleAdd(scope.row.id)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', children: 'children', value: 'id' }"
            check-strictly
            :render-after-expand="false"
            placeholder="选择上级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址" prop="menuPath">
          <el-input v-model="form.menuPath" placeholder="请输入路由地址，如：/system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径，如：system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'M'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="请输入权限标识，如：system:user:list" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="菜单图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.menuType !== 'F'" label="显示状态">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sort } from '@element-plus/icons-vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/system'

// 数据列表
const menuList = ref([])
const loading = ref(false)
const isExpand = ref(true)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuPath: '',
  component: '',
  permission: '',
  menuType: 'C',
  icon: '',
  orderNum: 0,
  status: 1,
  visible: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 菜单树下拉选项
const menuTreeOptions = computed(() => {
  return [{ id: 0, menuName: '顶级菜单', children: menuList.value }]
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const res = await getMenuTree()
    if (res) {
      menuList.value = res
    }
  } catch (error) {
    console.error('加载菜单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换展开/折叠
const toggleExpand = () => {
  isExpand.value = !isExpand.value
  loadData()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.parentId = 0
  form.menuName = ''
  form.menuPath = ''
  form.component = ''
  form.permission = ''
  form.menuType = 'C'
  form.icon = ''
  form.orderNum = 0
  form.status = 1
  form.visible = 1
}

// 新增菜单
const handleAdd = (parentId) => {
  resetForm()
  form.parentId = parentId
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

// 编辑菜单
const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  form.id = row.id
  form.parentId = row.parentId
  form.menuName = row.menuName
  form.menuPath = row.menuPath
  form.component = row.component
  form.permission = row.permission
  form.menuType = row.menuType
  form.icon = row.icon
  form.orderNum = row.orderNum
  form.status = row.status
  form.visible = row.visible
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (form.id) {
      await updateMenu(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createMenu(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除菜单
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除菜单"${row.menuName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败，可能存在子菜单')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}
</style>

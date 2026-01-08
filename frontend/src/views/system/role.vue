<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.roleName" placeholder="请输入角色名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增角色
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card>
      <el-table :data="roleList" border stripe v-loading="loading">
        <el-table-column prop="id" label="角色ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="handleAssignMenu(scope.row)">分配权限</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="menuDialogVisible" title="分配权限" width="500px">
      <div style="max-height: 400px; overflow-y: auto;">
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          show-checkbox
          default-expand-all
          :default-checked-keys="checkedMenuIds"
        />
      </div>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignMenus" :loading="menuLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/system'
import { getMenuTree } from '@/api/system'

// 查询参数
const queryForm = reactive({
  roleName: '',
  status: null,
  page: 1,
  size: 10
})

// 数据列表
const roleList = ref([])
const total = ref(0)
const loading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 权限分配相关
const menuDialogVisible = ref(false)
const menuTree = ref([])
const menuTreeRef = ref(null)
const checkedMenuIds = ref([])
const currentRoleId = ref(null)
const menuLoading = ref(false)

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const res = await getRoleList(queryForm)
    if (res && res.records) {
      roleList.value = res.records
      total.value = res.total
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.roleName = ''
  queryForm.status = null
  queryForm.page = 1
  loadData()
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.id = row.id
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.status = row.status
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (form.id) {
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
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

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 分配权限
const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  try {
    // 加载菜单树
    const menuRes = await getMenuTree()
    if (menuRes) {
      menuTree.value = menuRes
    }
    
    // 加载角色已有权限
    const menuIds = await getRoleMenus(row.id)
    checkedMenuIds.value = menuIds || []
    
    menuDialogVisible.value = true
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

// 提交权限分配
const submitAssignMenus = async () => {
  try {
    menuLoading.value = true
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    
    await assignRoleMenus(currentRoleId.value, allKeys)
    ElMessage.success('分配权限成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
  } finally {
    menuLoading.value = false
  }
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

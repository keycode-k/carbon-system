<template>
  <div class="user-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">用户列表</span>
          <div class="actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="userList" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="nickname" label="昵称" min-width="100" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="角色" min-width="150">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role.id" class="role-tag" size="small" type="info">
              {{ role.roleName }}
            </el-tag>
            <span v-if="!scope.row.roles || scope.row.roles.length === 0" class="no-role">未分配角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" :icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="primary" :icon="Setting" @click="handleAssignRole(scope.row)">分配角色</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" append-to-body @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="form.id !== undefined" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
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
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog title="重置密码" v-model="resetPwdVisible" width="400px" append-to-body>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" v-model="roleVisible" width="500px" append-to-body>
      <div class="role-assign-container">
        <p class="user-info">当前用户：<strong>{{ currentUser.username }}</strong></p>
        <el-checkbox-group v-model="selectedRoleIds">
          <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id" :disabled="role.status === 0">
            {{ role.roleName }}
            <span v-if="role.description" class="role-desc">({{ role.description }})</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Key, Setting } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUserAdmin, deleteUserAdmin, resetUserPassword, assignUserRoles, getUserRoleIds } from '@/api/system'
import { getRoleList } from '@/api/system'

// 查询参数
const queryParams = reactive({
  username: '',
  phone: '',
  status: null,
  page: 1,
  size: 10
})

// 表格数据
const loading = ref(false)
const userList = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({
  id: undefined,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在4到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 重置密码
const resetPwdVisible = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

// 分配角色
const roleVisible = ref(false)
const currentUser = ref({})
const allRoles = ref([])
const selectedRoleIds = ref([])

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    userList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
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
  queryParams.phone = ''
  queryParams.status = null
  handleQuery()
}

// 分页
const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

// 添加用户
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '编辑用户'
  form.id = row.id
  form.username = row.username
  form.nickname = row.nickname
  form.email = row.email
  form.phone = row.phone
  form.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateUserAdmin(form)
          ElMessage.success('修改成功')
        } else {
          await createUser(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        getList()
      } catch (error) {
        console.error('操作失败:', error)
      }
    }
  })
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateUserAdmin({ id: row.id, status: row.status })
    ElMessage.success(row.status === 1 ? '用户已启用' : '用户已禁用')
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态修改失败:', error)
  }
}

// 删除用户
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteUserAdmin(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 重置密码
const handleResetPwd = (row) => {
  pwdForm.userId = row.id
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  resetPwdVisible.value = true
}

const submitResetPwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await resetUserPassword({
          userId: pwdForm.userId,
          newPassword: pwdForm.newPassword
        })
        ElMessage.success('密码重置成功')
        resetPwdVisible.value = false
      } catch (error) {
        console.error('密码重置失败:', error)
      }
    }
  })
}

// 分配角色
const handleAssignRole = async (row) => {
  currentUser.value = row
  try {
    // 获取所有角色
    const roleRes = await getRoleList({ page: 1, size: 1000 })
    allRoles.value = roleRes.records || []
    
    // 获取用户当前角色
    const userRoleIds = await getUserRoleIds(row.id)
    selectedRoleIds.value = userRoleIds || []
    
    roleVisible.value = true
  } catch (error) {
    console.error('获取角色信息失败:', error)
  }
}

const submitAssignRole = async () => {
  try {
    await assignUserRoles({
      userId: currentUser.value.id,
      roleIds: selectedRoleIds.value
    })
    ElMessage.success('角色分配成功')
    roleVisible.value = false
    getList()
  } catch (error) {
    console.error('角色分配失败:', error)
  }
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card .title {
  font-size: 16px;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.role-tag {
  margin-right: 5px;
  margin-bottom: 3px;
}

.no-role {
  color: #909399;
  font-size: 12px;
}

.role-assign-container {
  max-height: 400px;
  overflow-y: auto;
}

.role-assign-container .user-info {
  margin-bottom: 15px;
  color: #606266;
}

.role-assign-container .el-checkbox {
  display: block;
  margin-bottom: 10px;
}

.role-desc {
  color: #909399;
  font-size: 12px;
}
</style>

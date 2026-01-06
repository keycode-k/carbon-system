<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card" shadow="hover">
          <div class="user-header">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <h2 class="user-name">{{ username }}</h2>
            <p class="user-role">注册用户</p>
            <div class="user-tags">
              <el-tag size="small">普通权限</el-tag>
              <el-tag size="small" type="success" style="margin-left: 5px">状态正常</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="user-progress">
             <div class="progress-item">
               <span>账户安全评分</span>
               <el-progress :percentage="securityScore" status="warning" />
             </div>
             <div class="progress-item">
               <span>资料完善度</span>
               <el-progress :percentage="completionScore" />
             </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人资料编辑</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form label-width="100px" style="max-width: 500px; margin-top: 20px">
                <el-form-item label="用户名">
                  <el-input v-model="username" disabled />
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="暂无信息" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" placeholder="暂无信息" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="form.email" placeholder="暂无信息" />
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="暂无信息" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="安全设置" name="security">
              <el-collapse accordion>
                <el-collapse-item title="修改密码" name="1">
                   <el-form label-width="100px" style="max-width: 500px">
                     <el-form-item label="旧密码"><el-input type="password" v-model="passwordForm.oldPassword"/></el-form-item>
                     <el-form-item label="新密码"><el-input type="password" v-model="passwordForm.newPassword"/></el-form-item>
                     <el-form-item label="确认密码"><el-input type="password" v-model="passwordForm.confirmPassword"/></el-form-item>
                     <el-form-item><el-button type="warning" @click="handleUpdatePassword">修改密码</el-button></el-form-item>
                   </el-form>
                </el-collapse-item>
                <el-collapse-item title="绑定手机" name="2">
                   <div>当前未绑定手机 <el-button type="primary" link>立即绑定</el-button></div>
                </el-collapse-item>
                <el-collapse-item title="注销账户" name="3">
                   <div class="danger-zone">
                     <p>注销账户将永久删除您的所有个人信息，且无法恢复。</p>
                     <el-button type="danger" @click="handleDeleteAccount">确认注销账户</el-button>
                   </div>
                </el-collapse-item>
              </el-collapse>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getUserInfo, updateUser, updatePassword, deleteAccount } from '@/api/user'

const router = useRouter()
const activeTab = ref('basic')
const username = ref('')

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算资料完善度
const completionScore = computed(() => {
  let score = 0
  // 总共4项，每项25分
  if (form.nickname && form.nickname.trim() !== '') score += 25
  if (form.phone && form.phone.trim() !== '') score += 25
  if (form.email && form.email.trim() !== '') score += 25
  if (form.bio && form.bio.trim() !== '') score += 25
  return score
})

// 计算账户安全评分
const securityScore = computed(() => {
  let score = 40 // 基础分
  if (form.phone && form.phone.trim() !== '') score += 30
  if (form.email && form.email.trim() !== '') score += 30
  // 可以根据密码强度等增加逻辑
  return score > 100 ? 100 : score
})

onMounted(async () => {
  const storedUser = localStorage.getItem('username')
  if (storedUser) {
    username.value = storedUser
    try {
      const res = await getUserInfo(storedUser)
      if (res.success && res.data) {
        form.nickname = res.data.nickname || ''
        form.phone = res.data.phone || ''
        form.email = res.data.email || ''
        form.bio = res.data.bio || ''
      }
    } catch (e) {
      console.error(e)
    }
  } else {
    username.value = 'Guest'
  }
})

const saveInfo = async () => {
  try {
    const res = await updateUser({
      username: username.value,
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      bio: form.bio
    })
    
    if (res.success) {
       ElMessage.success('保存成功')
    } else {
       ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleUpdatePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  
  try {
    const res = await updatePassword({
      username: username.value,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res.success) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
       ElMessage.error(res.message || '密码修改失败')
    }
  } catch (e) {
    ElMessage.error('系统异常，请稍后重试')
  }
}

const handleDeleteAccount = () => {
  ElMessageBox.confirm(
    '此操作将永久删除您的账户信息，是否继续?',
    '警告',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await deleteAccount(username.value)
        if (res.success) {
           ElMessage.success('账户已注销')
           localStorage.removeItem('username')
           localStorage.removeItem('token') // 假设有token
           router.push('/login')
        } else {
           ElMessage.error(res.message || '注销失败')
        }
      } catch (e) {
        ElMessage.error('系统异常，注销失败')
      }
    })
    .catch(() => {
      // 取消操作
    })
}
</script>

<style scoped>
.app-container { padding: 20px; }
.user-header { text-align: center; padding: 20px 0; }
.user-name { margin: 10px 0 5px; font-size: 24px; color: #303133; }
.user-role { color: #909399; margin-bottom: 15px; }
.user-tags { margin-top: 10px; }
.progress-item { margin-bottom: 15px; }
.progress-item span { display: block; margin-bottom: 5px; color: #606266; font-size: 14px; }
.danger-zone { padding: 10px; color: #F56C6C; }
</style>

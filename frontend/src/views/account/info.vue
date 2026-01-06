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
import { getUserInfo, updateUser, updatePassword, deleteAccount } from '@/api/user'

const username = ref('')

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  bio: ''
})

const securityScore = ref(60)
const completionScore = ref(30)
const activeTab = ref('basic')
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saveInfo = async () => {
    try {
        await updateUser(form)
        ElMessage.success('更新成功')
        fetchUserInfo()
    } catch (e) {
        // request.js handles message
    }
}

const handleUpdatePassword = async () => {
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        ElMessage.error('两次密码不一致')
        return
    }
    try {
        await updatePassword({
            username: username.value, // Pass username if backend needs it to find user
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
    } catch (e) {
        // error handled
    }
}

const handleDeleteAccount = async () => {
    try {
        await ElMessageBox.confirm('确定要注销账号吗？此操作不可逆！', '警告', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        // Need user ID. Assuming we have it in a hidden field or similar. 
        // For now, let's use the API that accepts userId as per FeignClient update
        // But wait, the frontend API wrapper `deleteAccount` passes `userId`.
        // We need to store userId when fetching info.
        
        if (!form.id) {
             ElMessage.error('无法获取用户ID')
             return
        }

        await deleteAccount(form.id)
        ElMessage.success('账户已注销')
        localStorage.clear()
        window.location.reload()
    } catch (e) {
        // cancel or error
    }
}

const fetchUserInfo = async () => {
    // 假设token存储在 localStorage 或 cookie 中，这里简化处理
    // 实际项目中应该从 Pinia userStore 中获取
    const token = localStorage.getItem('token') 
    if (!token) return

    try {
        const res = await getUserInfo(token)
        if (res) {
            username.value = res.username
            form.id = res.id
            form.nickname = res.nickname // Use nickname as per DB schema
            form.phone = res.phone
            form.email = res.email
            form.bio = res.bio || '这家伙很懒，什么都没留下' 
            
            // 简单计算完成度
            let score = 20
            if (res.nickname) score += 20
            if (res.phone) score += 20
            if (res.email) score += 20
            if (res.bio) score += 20
            completionScore.value = score
        }
    } catch (err) {
        console.error(err)
    }
}

onMounted(() => {
    fetchUserInfo()
})
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

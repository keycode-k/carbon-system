<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card" shadow="hover" v-loading="loadingInfo">
          <div class="user-header">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <h2 class="user-name">{{ username || '加载中...' }}</h2>
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
          <!-- 加载失败时显示重试按钮 -->
          <div v-if="loadError" class="error-retry">
            <el-alert title="数据加载失败" type="error" :closable="false" show-icon>
              <template #default>
                <p>{{ loadError }}</p>
                <el-button type="primary" size="small" @click="retryLoad">重新加载</el-button>
              </template>
            </el-alert>
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
              <el-form label-width="100px" style="max-width: 500px; margin-top: 20px" v-loading="loadingInfo">
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
            <el-tab-pane label="资产概览" name="assets">
               <el-card shadow="never">
                 <el-row :gutter="40">
                   <el-col :span="12">
                     <div class="asset-summary-box">
                        <h3>持有配额 (Quota)</h3>
                        <div class="num">{{ totalQuota.toLocaleString() }} <small>tCO2e</small></div>
                     </div>
                   </el-col>
                   <el-col :span="12">
                     <div class="asset-summary-box">
                        <h3>持有信用 (CCER)</h3>
                        <div class="num">{{ totalCredit.toLocaleString() }} <small>tCO2e</small></div>
                     </div>
                   </el-col>
                 </el-row>
               </el-card>
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
import { listQuotas, getCreditList } from '@/api/assets'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const username = ref('')

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  bio: ''
})

const totalQuota = ref(0)
const totalCredit = ref(0)

const activeTab = ref('basic')
const securityScore = ref(65)
const completionScore = ref(30)
const loadingInfo = ref(false)
const loadingAssets = ref(false)
const loadError = ref('')

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
    // 优先从store获取，再从localStorage获取
    const token = userStore.token || localStorage.getItem('token') 
    if (!token) {
        loadError.value = '未登录，请先登录'
        return
    }

    loadingInfo.value = true
    loadError.value = ''
    
    try {
        const res = await getUserInfo()
        if (res) {
            username.value = res.username
            form.id = res.id || res.userId
            form.nickname = res.nickname
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
        console.error('获取用户信息失败:', err)
        loadError.value = err.message || '网络请求失败，请稍后重试'
        
        // 如果 store 中有缓存的用户信息，使用缓存
        if (userStore.username) {
            username.value = userStore.username
            form.nickname = userStore.nickname || ''
        }
    } finally {
        loadingInfo.value = false
    }
}

const fetchAssets = async () => {
    loadingAssets.value = true
    try {
        // 获取 userId - 确保是数字
        let userId = userStore.userId || form.id || localStorage.getItem('userId')
        if (userId) {
            userId = parseInt(userId)
        } else {
            userId = 1 // 默认值
        }
        console.log('Fetching assets for userId:', userId, typeof userId)
        
        // 并行获取配额和信用数据
        const [quotaRes, creditRes] = await Promise.allSettled([
            listQuotas(userId),
            getCreditList(userId)
        ])
        
        // 处理配额数据
        let qTotal = 0
        if (quotaRes.status === 'fulfilled' && quotaRes.value) {
            const quotas = quotaRes.value
            console.log('Quota response:', quotas)
            const quotaArray = Array.isArray(quotas) ? quotas : (quotas?.data || [])
            qTotal = quotaArray.reduce((sum, q) => {
                const val = parseFloat(q.totalQuota) || 0
                return sum + val
            }, 0)
        } else {
            console.error('获取配额失败:', quotaRes.reason)
        }
        totalQuota.value = qTotal

        // 处理信用数据
        let cTotal = 0
        if (creditRes.status === 'fulfilled' && creditRes.value) {
            const credits = creditRes.value
            console.log('Credit response:', credits)
            const creditArray = Array.isArray(credits) ? credits : (credits?.data || [])
            cTotal = creditArray.reduce((sum, c) => {
                const val = parseFloat(c.amount) || 0
                return sum + val
            }, 0)
        } else {
            console.error('获取信用失败:', creditRes.reason)
        }
        totalCredit.value = cTotal
        
        console.log('Final - Total Quota:', totalQuota.value, 'Total Credit:', totalCredit.value)
    } catch (e) {
        console.error("获取资产信息失败:", e)
    } finally {
        loadingAssets.value = false
    }
}

// 重试加载
const retryLoad = async () => {
    await fetchUserInfo()
    await fetchAssets()
}

onMounted(async () => {
    // 先从缓存显示基本信息
    if (userStore.username) {
        username.value = userStore.username
        form.nickname = userStore.nickname || ''
    }
    
    // 再从API获取最新信息
    await fetchUserInfo()
    await fetchAssets()
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
.error-retry { 
  margin-top: 20px; 
  padding: 10px; 
}
.error-retry p { 
  margin: 0 0 10px 0; 
  font-size: 13px; 
}
</style>

<style scoped>
.user-header { text-align: center; padding: 20px 0; }
.user-name { margin: 10px 0 5px; font-size: 20px; }
.user-role { color: #909399; margin-bottom: 10px; }
.user-progress { padding: 0 20px; }
.progress-item { margin-bottom: 15px; }
.progress-item span { font-size: 14px; color: #606266; display: block; margin-bottom: 5px; }
.card-header { font-weight: bold; }
.asset-summary-box { text-align: center; padding: 30px; background: #f8f9fa; border-radius: 8px; }
.asset-summary-box h3 { margin: 0 0 10px; color: #606266; }
.asset-summary-box .num { font-size: 32px; font-weight: bold; color: #409EFF; }
.asset-summary-box .num small { font-size: 14px; color: #909399; font-weight: normal; }
.danger-zone { padding: 20px; background: #fef0f0; border-radius: 4px; color: #f56c6c; }
</style>


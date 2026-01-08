<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-title">
        <h1 class="system-name">碳中和</h1>
        <h2 class="sub-title">数字碳资产管理系统</h2>
        <div class="feature-list">
          <div class="feature-item"><el-icon><DataLine /></el-icon> 全流程碳资产开发管理</div>
          <div class="feature-item"><el-icon><TrendCharts /></el-icon> 实时碳交易行情监控</div>
          <div class="feature-item"><el-icon><SetUp /></el-icon> 智能配额履约分析</div>
        </div>
      </div>
      
      <el-card class="login-card" shadow="always">
        <template #header>
          <div class="card-header-tabs">
            <span :class="{ active: isLogin }" @click="isLogin = true">用户登录</span>
            <span class="divider">|</span>
            <span :class="{ active: !isLogin }" @click="isLogin = false">注册账号</span>
          </div>
        </template>

        <!-- 登录表单 -->
        <el-form v-if="isLogin" ref="loginRef" :model="loginForm" :rules="loginRules" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" prefix-icon="User" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <el-button link type="primary">忘记密码？</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="action-btn" @click="handleLogin" :loading="loading">立即登录</el-button>
          </el-form-item>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-else ref="registerRef" :model="registerForm" :rules="registerRules" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" prefix-icon="User" placeholder="设置用户名 (4-16位)" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" prefix-icon="Lock" type="password" placeholder="设置密码 (6位以上)" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" prefix-icon="Key" type="password" placeholder="确认密码" show-password />
          </el-form-item>
          <el-form-item>
             <el-button type="success" class="action-btn" @click="handleRegister" :loading="loading">注册并登录</el-button> 
          </el-form-item>
          <div class="register-footer">
             已有账号？<el-link type="primary" @click="isLogin = true">直接登录</el-link>
          </div>
        </el-form>

      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/user'
import { useUserStore } from '@/store/user'
import { DataLine, TrendCharts, SetUp } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const isLogin = ref(true) // 切换登录/注册
const rememberMe = ref(false)

const loginRef = ref(null)
const registerRef = ref(null)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 16, message: '长度在 4 到 16 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPass, trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginRef.value) return
  await loginRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res && res.token) {
          ElMessage.success('登录成功')
          
          // 设置用户信息到store（包含JWT Token和权限信息）
          userStore.setUserInfo({
            id: res.userId || res.id || 1,
            username: res.username || loginForm.username,
            nickname: res.nickname || '',
            token: res.token,
            refreshToken: res.refreshToken || '',
            roles: res.roles || [],
            permissions: res.permissions || []
          })
          
          router.push('/')
        } else {
          ElMessage.error('登录失败，请检查用户名或密码')
        }
      } catch (error) {
        // 请求错误已经在 request.js 处理了部分，这里可以兜底
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 处理注册
const handleRegister = async () => {
  if (!registerRef.value) return
  await registerRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register({
          username: registerForm.username,
          password: registerForm.password
        })
        
        if (res && (res.success || res.code === 200 || res === null)) { // Result<Object> from register might be null data if void
          ElMessage.success('注册成功，正在为您自动登录...')
          
          // 直接调用登录接口，不依懒 DOM 校验
          try {
            const loginRes = await login({
              username: registerForm.username,
              password: registerForm.password
            })
            
            if (loginRes && loginRes.token) {
              // 设置用户信息到store
              userStore.setUserInfo({
                id: loginRes.userId || loginRes.id || 1,
                username: loginRes.username || registerForm.username,
                token: loginRes.token,
                roles: loginRes.roles || []
              })
              
              router.push('/')
            } else {
              ElMessage.warning('自动登录失败，请手动登录')
              isLogin.value = true // 切换回登录页
              loginForm.username = registerForm.username
            }
          } catch (e) {
             console.error(e)
             isLogin.value = true
          }
        } else {
           ElMessage.error(res.message || '注册失败')
        }
      } catch (error) {
         console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #064e3b 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  position: relative;
  overflow: hidden;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 动态背景粒子效果 */
.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(16, 185, 129, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(139, 92, 246, 0.1) 0%, transparent 40%);
  pointer-events: none;
}

.login-container::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
  pointer-events: none;
}

.login-content {
  display: flex;
  width: 1100px;
  justify-content: space-between;
  align-items: center;
  gap: 80px;
  z-index: 1;
  padding: 0 40px;
}

.login-title {
  color: #fff;
  flex: 1;
  animation: fadeInLeft 0.8s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.system-name {
  font-size: 64px;
  margin: 0;
  font-weight: 800;
  letter-spacing: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #6ee7b7 50%, #10b981 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 4px 30px rgba(16, 185, 129, 0.3);
}

.sub-title {
  font-size: 26px;
  margin-top: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
  letter-spacing: 2px;
}

.feature-list {
  margin-top: 50px;
}

.feature-item {
  color: rgba(255, 255, 255, 0.85);
  font-size: 16px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(10px);
  border-color: rgba(16, 185, 129, 0.3);
}

.feature-item .el-icon {
  font-size: 24px;
  color: #10b981;
}

.login-card {
  width: 440px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.98);
  border: none;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  animation: fadeInRight 0.8s ease-out;
  overflow: hidden;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.login-card :deep(.el-card__header) {
  padding: 28px 32px 20px;
  border-bottom: none;
  background: linear-gradient(180deg, #fafafa 0%, #fff 100%);
}

.login-card :deep(.el-card__body) {
  padding: 24px 32px 32px;
}

.card-header-tabs {
  text-align: center;
  font-size: 16px;
  color: #94a3b8;
}

.card-header-tabs span {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 8px;
}

.card-header-tabs span:hover:not(.divider) {
  background: #f1f5f9;
}

.card-header-tabs span.active {
  color: #10b981;
  font-weight: 600;
  font-size: 18px;
  background: rgba(16, 185, 129, 0.1);
}

.card-header-tabs .divider {
  margin: 0 12px;
  color: #e2e8f0;
  cursor: default;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #e2e8f0;
  transition: all 0.3s ease;
}

.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #10b981;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2), 0 0 0 1px #10b981;
}

.action-btn {
  width: 100%;
  padding: 20px 0;
  font-size: 16px;
  letter-spacing: 3px;
  margin-top: 12px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.35);
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(16, 185, 129, 0.45);
}

.action-btn:active {
  transform: translateY(0);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-options :deep(.el-checkbox__label) {
  color: #64748b;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-content {
    flex-direction: column;
    gap: 40px;
    padding: 20px;
  }
  
  .login-title {
    text-align: center;
  }
  
  .system-name {
    font-size: 42px;
  }
  
  .sub-title {
    font-size: 18px;
  }
  
  .feature-list {
    display: none;
  }
  
  .login-card {
    width: 100%;
    max-width: 420px;
  }
}
</style>
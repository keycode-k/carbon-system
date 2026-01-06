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

const router = useRouter()
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
        if (res.success) {
          ElMessage.success('登录成功')
          localStorage.setItem('token', res.token) // 存储 token
          localStorage.setItem('username', loginForm.username)
          router.push('/')
        } else {
          ElMessage.error(res.message || '登录失败，请检查用户名或密码')
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
        
        if (res.success) {
          ElMessage.success('注册成功，正在为您自动登录...')
          
          // 直接调用登录接口，不依懒 DOM 校验
          try {
            const loginRes = await login({
              username: registerForm.username,
              password: registerForm.password
            })
            
            if (loginRes.success) {
              localStorage.setItem('token', loginRes.token)
              localStorage.setItem('username', registerForm.username)
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
  background: linear-gradient(135deg, #1f2a3d 0%, #2e4c6b 100%);
  background-size: cover;
  position: relative;
  overflow: hidden;
}

/* 增加背景动态效果 */
.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url('https://img.js.design/assets/img/677b8c8d8c8d8c8d8c8d8c8d.png') no-repeat center/cover;
  opacity: 0.1;
  pointer-events: none;
}

.login-content {
  display: flex;
  width: 1000px;
  justify-content: center;
  align-items: center;
  gap: 100px;
  z-index: 1;
}

.login-title {
  color: #fff;
  flex: 1;
}

.system-name {
  font-size: 56px;
  margin: 0;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(to right, #ffffff, #81c784);
  -webkit-background-clip: text;
  color: transparent;
}

.sub-title {
  font-size: 24px;
  margin-top: 15px;
  color: #a8abb2;
  font-weight: 300;
}

.feature-list {
  margin-top: 40px;
}

.feature-item {
  color: #e0e0e0;
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.login-card {
  width: 420px;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.98);
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.card-header-tabs {
  text-align: center;
  font-size: 18px;
  color: #909399;
}

.card-header-tabs span {
  cursor: pointer;
  transition: all 0.3s;
}

.card-header-tabs span.active {
  color: #409eff;
  font-weight: bold;
  font-size: 20px;
}

.card-header-tabs .divider {
  margin: 0 15px;
  color: #dcdfe6;
  cursor: default;
}

.action-btn {
  width: 100%;
  padding: 22px 0;
  font-size: 16px;
  letter-spacing: 2px;
  margin-top: 10px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
}
</style>
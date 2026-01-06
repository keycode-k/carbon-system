<template>
  <div class="app-container">
    <h1>微服务登录系统</h1>
    
    <!-- 功能切换按钮 -->
    <div class="tab-buttons">
      <button @click="activeTab = 'register'" :class="{ active: activeTab === 'register' }">注册</button>
      <button @click="activeTab = 'login'" :class="{ active: activeTab === 'login' }">登录</button>
      <button @click="activeTab = 'logout'" :class="{ active: activeTab === 'logout' }">登出</button>
      <button @click="activeTab = 'redisTest'" :class="{ active: activeTab === 'redisTest' }">Redis测试</button>
    </div>

    <!-- 注册表单 -->
    <div v-if="activeTab === 'register'" class="form-container">
      <h2>用户注册</h2>
      <form @submit.prevent="register">
        <div class="form-item">
          <label for="register-username">用户名</label>
          <input type="text" id="register-username" v-model="registerForm.username" placeholder="请输入用户名" required>
        </div>
        <div class="form-item">
          <label for="register-password">密码</label>
          <input type="password" id="register-password" v-model="registerForm.password" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="submit-btn">注册</button>
      </form>
    </div>

    <!-- 登录表单 -->
    <div v-if="activeTab === 'login'" class="form-container">
      <h2>用户登录</h2>
      <form @submit.prevent="login">
        <div class="form-item">
          <label for="login-username">用户名</label>
          <input type="text" id="login-username" v-model="loginForm.username" placeholder="请输入用户名" required>
        </div>
        <div class="form-item">
          <label for="login-password">密码</label>
          <input type="password" id="login-password" v-model="loginForm.password" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="submit-btn">登录</button>
      </form>
    </div>

    <!-- 登出表单 -->
    <div v-if="activeTab === 'logout'" class="form-container">
      <h2>用户登出</h2>
      <form @submit.prevent="logout">
        <div class="form-item">
          <label for="logout-token">Token</label>
          <input type="text" id="logout-token" v-model="logoutForm.token" placeholder="请输入登录获得的token" required>
        </div>
        <button type="submit" class="submit-btn">登出</button>
      </form>
    </div>
    
    <!-- Redis测试表单 -->
    <div v-if="activeTab === 'redisTest'" class="form-container">
      <h2>Redis Token验证</h2>
      <form @submit.prevent="verifyToken">
        <div class="form-item">
          <label for="redis-token">Token</label>
          <input type="text" id="redis-token" v-model="redisTestForm.token" placeholder="请输入登录获取的token" required>
        </div>
        <button type="submit" class="submit-btn">验证Token</button>
      </form>
    </div>

    <!-- 结果显示 -->
    <div v-if="result" class="result-container" :class="{ success: result.success, error: !result.success }">
      <h3>{{ result.success ? '操作成功' : '操作失败' }}</h3>
      <p>{{ result.message }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      activeTab: 'register',
      registerForm: {
        username: '',
        password: ''
      },
      loginForm: {
        username: '',
        password: ''
      },
      logoutForm: {
        token: ''
      },
      redisTestForm: {
        token: ''
      },
      result: null
    }
  },
  methods: {
    // 注册功能
    async register() {
      try {
        const response = await axios.post('/api/user/register', this.registerForm)
        this.result = response.data
      } catch (error) {
        console.error('注册错误详情:', error)
        this.result = {
          success: false,
          message: error.response?.data?.message || error.message || `注册失败: ${error.response?.status || '网络错误'}`
        }
      }
    },
    
    // 登录功能
    async login() {
      try {
        console.log('发送登录请求:', this.loginForm)
        const response = await axios.post('/api/user/login', this.loginForm)
        console.log('登录响应:', response.data)
        this.result = response.data
        // 登录成功后自动填充token到登出和Redis测试表单
        if (response.data.success && response.data.token) {
          this.logoutForm.token = response.data.token
          this.redisTestForm.token = response.data.token
        }
      } catch (error) {
        console.error('登录错误详情:', error)
        console.error('错误响应:', error.response)
        this.result = {
          success: false,
          message: error.response?.data?.message || error.message || `登录失败: ${error.response?.status || '网络错误'}`
        }
      }
    },
    
    // 登出功能
    async logout() {
      try {
        const response = await axios.post('/api/user/logout', null, {
          params: { token: this.logoutForm.token }
        })
        this.result = response.data
      } catch (error) {
        console.error('登出错误详情:', error)
        console.error('错误响应数据:', error.response?.data)
        console.error('错误状态码:', error.response?.status)
        this.result = {
          success: false,
          message: error.response?.data?.message || error.response?.data || error.message || '登出失败，请稍后重试'
        }
      }
    },
    
    // Redis Token验证功能
    async verifyToken() {
      try {
        const response = await axios.get('/api/user/verifyToken', {
          params: this.redisTestForm
        })
        this.result = response.data
      } catch (error) {
        this.result = {
          success: false,
          message: error.response?.data?.message || 'Token验证失败，请稍后重试'
        }
      }
    }
  }
}
</script>

<style scoped>
.app-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  text-align: center;
  color: #333;
}

.tab-buttons {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.tab-buttons button {
  padding: 10px 20px;
  margin: 0 5px;
  border: 1px solid #ddd;
  background-color: #f0f0f0;
  cursor: pointer;
  border-radius: 4px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.tab-buttons button.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.form-container {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
  color: #666;
  font-weight: bold;
}

.form-item input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.result-container {
  margin-top: 20px;
  padding: 15px;
  border-radius: 4px;
  text-align: center;
}

.result-container.success {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.result-container.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4ab;
}
</style>

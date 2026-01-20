import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: '', // Using proxy in vite.config.js
  timeout: 30000 // 30秒超时
})

// 请求重试配置
const retryConfig = {
  retries: 2,        // 最大重试次数
  retryDelay: 1000,  // 重试间隔（毫秒）
  retryCondition: (error) => {
    // 仅在网络错误或超时时重试
    return !error.response || error.code === 'ECONNABORTED'
  }
}

// Request interceptor - 自动添加JWT Token
service.interceptors.request.use(
  config => {
    // 从store获取token
    const userStore = useUserStore()
    if (userStore.token) {
      // 在请求头中添加Authorization
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    // 确保 Content-Type 为 application/json
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    // 初始化重试计数
    config.__retryCount = config.__retryCount || 0
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // 兼容 { code, message, data } 格式和旧格式
    if (res.code && res.code !== 200) {
        // 处理401未授权错误
        if (res.code === 401) {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          ElMessage({
            message: '登录已过期，请重新登录',
            type: 'warning',
            duration: 3 * 1000
          })
          return Promise.reject(new Error('登录已过期'))
        }
        
        // 处理403权限不足错误
        if (res.code === 403) {
          ElMessage({
            message: res.message || '权限不足',
            type: 'error',
            duration: 3 * 1000
          })
          return Promise.reject(new Error(res.message || '权限不足'))
        }
        
        ElMessage({
            message: res.message || 'Error',
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(new Error(res.message || 'Error'))
    } else {
        // 如果是 Result 结构，返回 data，否则(如 map)直接返回
        return res.data !== undefined ? res.data : res
    }
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理HTTP 401错误
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage({
        message: '登录已过期，请重新登录',
        type: 'warning',
        duration: 3 * 1000
      })
      return Promise.reject(error)
    }
    
    // 处理HTTP 403错误
    if (error.response && error.response.status === 403) {
      ElMessage({
        message: '权限不足，无法访问该资源',
        type: 'error',
        duration: 3 * 1000
      })
      return Promise.reject(error)
    }
    
    ElMessage({
      message: error.message || '网络错误',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

// 添加重试拦截器
service.interceptors.response.use(undefined, async (error) => {
  const config = error.config
  
  // 如果 config 不存在或不满足重试条件或已达最大重试次数，直接抛出错误
  if (!config || !retryConfig.retryCondition(error)) {
    return Promise.reject(error)
  }
  
  // 初始化重试计数
  config.__retryCount = config.__retryCount || 0
  
  if (config.__retryCount >= retryConfig.retries) {
    return Promise.reject(error)
  }
  
  // 增加重试计数
  config.__retryCount += 1
  console.log(`请求重试 (${config.__retryCount}/${retryConfig.retries}): ${config.url}`)
  
  // 等待指定时间后重试
  await new Promise(resolve => setTimeout(resolve, retryConfig.retryDelay))
  
  return service(config)
})

export default service

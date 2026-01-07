import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '', // Using proxy in vite.config.js
  timeout: 60000
})

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // 兼容 { code, message, data } 格式和旧格式
    if (res.code && res.code !== 200) {
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
    console.log('err' + error)
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service

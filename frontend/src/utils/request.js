import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '', // Using proxy in vite.config.js
  timeout: 5000
})

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // If the backend returns a success flag, we can check it here
    // But looking at the controller, it returns a Map. We will handle logic in component or here.
    return res
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

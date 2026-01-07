import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    token: '',
    roles: [],
    userInfo: null
  }),
  
  actions: {
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userId = userInfo.id || userInfo.userId
      this.username = userInfo.username
      this.token = userInfo.token || localStorage.getItem('token')
      this.roles = userInfo.roles || []
      this.userInfo = userInfo
      
      // 持久化到 localStorage
      if (this.token) {
        localStorage.setItem('token', this.token)
      }
      if (this.userId) {
        localStorage.setItem('userId', String(this.userId))
      }
      if (this.username) {
        localStorage.setItem('username', this.username)
      }
    },
    
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    // 退出登录
    logout() {
      this.userId = null
      this.username = ''
      this.token = ''
      this.roles = []
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
    },
    
    // 从localStorage恢复登录状态
    async restoreLoginState() {
      const token = localStorage.getItem('token')
      const userId = localStorage.getItem('userId')
      const username = localStorage.getItem('username')
      
      if (token) {
        this.token = token
      }
      if (userId) {
        this.userId = parseInt(userId)
      }
      if (username) {
        this.username = username
      }
      
      // 如果有 token 但没有 username，调用接口获取用户信息
      if (token && !username) {
        try {
          const userInfo = await getUserInfo(token)
          if (userInfo) {
            this.setUserInfo({
              ...userInfo,
              token: token
            })
          }
        } catch (error) {
          console.error('恢复用户信息失败:', error)
          // 如果获取失败，清理 token
          this.logout()
        }
      }
    }
  },
  
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    
    // 获取用户ID
    getUserId: (state) => state.userId,
    
    // 是否有某个角色
    hasRole: (state) => (role) => {
      return state.roles.includes(role)
    }
  }
})

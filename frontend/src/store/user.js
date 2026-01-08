import { defineStore } from 'pinia'
import { getUserInfo, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: null,
    username: '',
    nickname: '',
    token: '',
    refreshToken: '',
    roles: [],
    permissions: [],
    userInfo: null
  }),
  
  actions: {
    // 设置用户信息（登录成功后调用）
    setUserInfo(userInfo) {
      this.userId = userInfo.id || userInfo.userId
      this.username = userInfo.username
      this.nickname = userInfo.nickname || ''
      this.token = userInfo.token || localStorage.getItem('token')
      this.refreshToken = userInfo.refreshToken || localStorage.getItem('refreshToken') || ''
      this.roles = userInfo.roles || []
      this.permissions = userInfo.permissions || []
      this.userInfo = userInfo
      
      // 持久化到 localStorage
      if (this.token) {
        localStorage.setItem('token', this.token)
      }
      if (this.refreshToken) {
        localStorage.setItem('refreshToken', this.refreshToken)
      }
      if (this.userId) {
        localStorage.setItem('userId', String(this.userId))
      }
      if (this.username) {
        localStorage.setItem('username', this.username)
      }
      if (this.nickname) {
        localStorage.setItem('nickname', this.nickname)
      }
      if (this.roles && this.roles.length > 0) {
        localStorage.setItem('roles', JSON.stringify(this.roles))
      }
      if (this.permissions && this.permissions.length > 0) {
        localStorage.setItem('permissions', JSON.stringify(this.permissions))
      }
    },
    
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    // 退出登录
    async logout() {
      try {
        // 调用后端登出接口
        if (this.token) {
          await logoutApi().catch(() => {})
        }
      } finally {
        // 清除本地状态
        this.userId = null
        this.username = ''
        this.nickname = ''
        this.token = ''
        this.refreshToken = ''
        this.roles = []
        this.permissions = []
        this.userInfo = null
        
        // 清除 localStorage
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('roles')
        localStorage.removeItem('permissions')
      }
    },
    
    // 从localStorage恢复登录状态
    async restoreLoginState() {
      const token = localStorage.getItem('token')
      const refreshToken = localStorage.getItem('refreshToken')
      const userId = localStorage.getItem('userId')
      const username = localStorage.getItem('username')
      const nickname = localStorage.getItem('nickname')
      const rolesStr = localStorage.getItem('roles')
      const permissionsStr = localStorage.getItem('permissions')
      
      if (token) {
        this.token = token
      }
      if (refreshToken) {
        this.refreshToken = refreshToken
      }
      if (userId) {
        this.userId = parseInt(userId)
      }
      if (username) {
        this.username = username
      }
      if (nickname) {
        this.nickname = nickname
      }
      if (rolesStr) {
        try {
          this.roles = JSON.parse(rolesStr)
        } catch (e) {
          this.roles = []
        }
      }
      if (permissionsStr) {
        try {
          this.permissions = JSON.parse(permissionsStr)
        } catch (e) {
          this.permissions = []
        }
      }
      
      // 如果有 token，尝试刷新用户信息（但失败不清除状态）
      if (token && this.userId) {
        try {
          const userInfo = await getUserInfo()
          if (userInfo) {
            this.setUserInfo({
              ...userInfo,
              token: token,
              refreshToken: refreshToken
            })
          }
        } catch (error) {
          console.warn('恢复用户信息失败，使用缓存数据:', error.message)
          // 不再自动登出，保持缓存的登录状态
          // 只有当收到 401 响应时才在 request.js 中登出
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
      if (state.roles.includes('admin')) return true
      return state.roles.includes(role)
    },
    
    // 是否有某个权限
    hasPermission: (state) => (permission) => {
      if (state.roles.includes('admin')) return true
      if (state.permissions.includes('*:*:*')) return true
      return state.permissions.includes(permission)
    },
    
    // 获取显示名称
    displayName: (state) => state.nickname || state.username || '用户'
  }
})

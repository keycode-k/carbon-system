<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '260px'" class="aside">
      <div class="logo">
        <div class="logo-icon-wrapper">
          <el-icon class="logo-icon"><Promotion /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!isCollapse" class="logo-text">碳资产管理平台</span>
        </transition>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activePath"
          class="el-menu-vertical"
          router
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.7)"
          active-text-color="#ffffff"
        >
          <template v-for="route in accessRoutes" :key="route.path">
            <!-- Single menu item (no children) -->
            <el-menu-item 
                v-if="!route.children || (route.children.length === 1 && !route.children[0].children)" 
                :index="resolvePath(route)"
                class="menu-item"
              >
                <el-icon v-if="route.meta && route.meta.icon || (route.children && route.children[0].meta && route.children[0].meta.icon)">
                  <component :is="route.meta?.icon || route.children[0].meta.icon" />
                </el-icon>
                <template #title>
                  <span>{{ route.children ? route.children[0].meta.title : route.meta.title }}</span>
                </template>
            </el-menu-item>

            <!-- Submenu -->
            <el-sub-menu v-else :index="route.path" class="sub-menu">
              <template #title>
                  <el-icon v-if="route.meta && route.meta.icon"><component :is="route.meta.icon" /></el-icon>
                  <span>{{ route.meta.title }}</span>
              </template>
              <el-menu-item 
                  v-for="child in route.children" 
                  :key="child.path" 
                  :index="route.path + '/' + child.path"
                  class="menu-item-child"
                >
                  {{ child.meta.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
      
      <!-- 底部用户信息 -->
      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="system-info">
          <el-icon><Monitor /></el-icon>
          <span>系统运行正常</span>
        </div>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">
              <el-icon><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              placeholder="搜索功能..."
              :prefix-icon="Search"
              size="default"
              style="width: 200px"
            />
          </div>
          
          <!-- 通知铃铛 -->
          <el-badge :value="3" :max="99" class="notification-badge">
            <el-tooltip content="通知消息" placement="bottom">
              <el-icon class="header-icon"><Bell /></el-icon>
            </el-tooltip>
          </el-badge>
          
          <el-tooltip content="使用文档" placement="bottom">
            <el-icon class="header-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
          
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen"><FullScreen /></el-icon>
          </el-tooltip>
          
          <el-divider direction="vertical" />
          
          <el-dropdown class="avatar-dropdown" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ username.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-detail">
                <span class="username">{{ username }}</span>
                <span class="user-role">管理员</span>
              </div>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/account/info')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/system/config')">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <keep-alive :max="10">
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { HomeFilled, Monitor, User, Setting, SwitchButton, ArrowDown, Bell, Promotion, Fold, Expand, QuestionFilled, FullScreen, Search } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapse = ref(false)

// 从 userStore 获取用户名，如果没有则使用默认值
const username = computed(() => userStore.username || 'Guest')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// Filter routes that should be displayed in menu (exclude hidden ones)
const accessRoutes = computed(() => {
  return router.options.routes.filter(r => !r.hidden)
})

const activePath = computed(() => route.path)
const currentRouteTitle = computed(() => route.meta.title || '当前页面')

const resolvePath = (route) => {
  if (route.redirect) return route.redirect
  if (route.children && route.children.length === 1) {
    return route.path === '/' ? route.children[0].path : route.path + '/' + route.children[0].path
  }
  return route.path
}

const logout = async () => {
  // 清理用户信息
  await userStore.logout()
  // 跳转到登录页
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: #fff;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 22px;
  color: #fff;
}

.logo-text {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  background: linear-gradient(90deg, #fff 0%, #a7f3d0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.el-menu-vertical {
  border-right: none !important;
  padding: 12px 8px;
}

.el-menu-vertical .el-menu-item {
  margin: 4px 0;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

.el-menu-vertical .el-menu-item:hover {
  background: rgba(16, 185, 129, 0.15) !important;
}

.el-menu-vertical .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.3) 0%, rgba(16, 185, 129, 0.1) 100%) !important;
  border-left: 3px solid #10b981;
}

.el-menu-vertical .el-sub-menu__title {
  margin: 4px 0;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

.el-menu-vertical .el-sub-menu__title:hover {
  background: rgba(16, 185, 129, 0.15) !important;
}

.menu-item-child {
  padding-left: 54px !important;
  font-size: 13px;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.system-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.system-info .el-icon {
  color: #10b981;
}

.main-container {
  flex-direction: column;
  background: #f1f5f9;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 28px;
  cursor: pointer;
  margin-right: 20px;
  color: #64748b;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: #f1f5f9;
  color: #10b981;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-box {
  margin-right: 12px;
}

.search-box .el-input__wrapper {
  border-radius: 20px;
  background: #f1f5f9;
}

.notification-badge {
  margin-right: 8px;
}

.header-icon {
  font-size: 28px;
  cursor: pointer;
  color: #64748b;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.header-icon:hover {
  background: #f1f5f9;
  color: #10b981;
}

.avatar-dropdown {
  cursor: pointer;
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 24px;
  transition: all 0.2s;
}

.user-info:hover {
  background: #f1f5f9;
}

.user-avatar {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  margin-right: 4px;
}

.username {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: #94a3b8;
}

.main {
  background-color: #f1f5f9;
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active {
  transition: all 0.3s ease-out;
}

.fade-slide-leave-active {
  transition: all 0.2s ease-in;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Element Plus 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 20px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}
</style>


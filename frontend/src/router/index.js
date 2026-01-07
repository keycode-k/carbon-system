import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      }
    ]
  },
  {
    path: '/account',
    component: Layout,
    meta: { title: '账户管理', icon: 'User' },
    children: [
      {
        path: 'info',
        name: 'AccountInfo',
        component: () => import('@/views/account/info.vue'),
        meta: { title: '基本信息' }
      },
      {
        path: 'company',
        name: 'AccountCompany',
        component: () => import('@/views/account/company.vue'),
        meta: { title: '企业信息' }
      }
    ]
  },
  {
    path: '/assets',
    component: Layout,
    meta: { title: '碳资产管理', icon: 'Wallet' },
    children: [
      {
        path: 'credit',
        name: 'AssetsCredit',
        component: () => import('@/views/assets/credit.vue'),
        meta: { title: '碳信用' }
      },
      {
        path: 'quota',
        name: 'AssetsQuota',
        component: () => import('@/views/assets/quota.vue'),
        meta: { title: '碳配额' }
      }
    ]
  },
  {
    path: '/development',
    component: Layout,
    meta: { title: '碳资产开发', icon: 'Edit' },
    children: [
      {
        path: 'methodology',
        name: 'DevMethodology',
        component: () => import('@/views/development/methodology.vue'),
        meta: { title: '方法学管理' }
      },
      {
        path: 'project',
        name: 'DevProject',
        component: () => import('@/views/development/project.vue'),
        meta: { title: '开发项目管理' }
      },
      {
        path: 'workbench',
        name: 'DevWorkbench',
        component: () => import('@/views/development/workbench.vue'),
        meta: { title: '开发工作台' }
      }
    ]
  },
  {
    path: '/market',
    component: Layout,
    meta: { title: '碳交易行情', icon: 'TrendCharts' },
    children: [
      {
        path: 'supply-demand',
        name: 'MarketSupplyDemand',
        component: () => import('@/views/market/supply-demand.vue'),
        meta: { title: '供需行情' }
      },
      {
        path: 'quotes',
        name: 'MarketQuotes',
        component: () => import('@/views/market/quotes.vue'),
        meta: { title: '询报价' }
      },
      {
        path: 'exchange',
        name: 'MarketExchange',
        component: () => import('@/views/market/exchange.vue'),
        meta: { title: '交易所' }
      },
      {
        path: 'account',
        name: 'MarketAccount',
        component: () => import('@/views/market/account.vue'),
        meta: { title: '交易账户' }
      },
      {
        path: 'trade-records',
        name: 'TradeRecords',
        component: () => import('@/views/market/trade-records.vue'),
        meta: { title: '交易记录' }
      }
    ]
  },
  {
    path: '/news',
    component: Layout,
    redirect: '/news/index',
    meta: { title: '碳资讯', icon: 'Bell' },
    children: [
      {
        path: 'index',
        name: 'News',
        component: () => import('@/views/news/index.vue'),
        meta: { title: '碳资讯' }
      }
    ]
  },
  {
    path: '/news/detail/:id',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        name: 'NewsDetail',
        component: () => import('@/views/news/detail.vue'),
        meta: { title: '新闻详情' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'operation',
        name: 'SysOperation',
        component: () => import('@/views/system/operation.vue'),
        meta: { title: '平台运营管理' }
      },
      {
        path: 'config',
        name: 'SysConfig',
        component: () => import('@/views/system/config.vue'),
        meta: { title: '基础配置管理' }
      },
      {
        path: 'approval',
        name: 'SysApproval',
        component: () => import('@/views/system/approval.vue'),
        meta: { title: '平台审批管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由导航守卫
router.beforeEach((to, from, next) => {
  // 获取 token
  const token = localStorage.getItem('token')
  
  // 如果访问登录页
  if (to.path === '/login') {
    // 如果已经登录，跳转到首页
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    // 访问其他页面，检查是否已登录
    if (token) {
      next()
    } else {
      // 未登录，跳转到登录页
      next('/login')
    }
  }
})

export default router

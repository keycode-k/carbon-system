import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import store from './store'
import { useUserStore } from './store/user'
import './style.css'

const app = createApp(App)

app.use(router)
app.use(store)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 异步初始化：先恢复登录状态，再挂载应用
;(async () => {
  const userStore = useUserStore()
  await userStore.restoreLoginState()
  app.mount('#app')
})()

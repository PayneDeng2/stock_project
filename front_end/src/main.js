import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入初始化函数
import { initializeDefaultRecords } from '@/utils/tradeManager'
import { initializeDefaultWatchlist } from '@/utils/stockManager'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化默认数据
initializeDefaultRecords()
initializeDefaultWatchlist()

app.use(router)
app.use(ElementPlus)
app.mount('#app')

// 全局错误处理，忽略 ResizeObserver 相关错误
const resizeObserverErrorHandler = (e) => {
  if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
    const resizeObserverErrDiv = document.getElementById('webpack-dev-server-client-overlay-div')
    const resizeObserverErr = document.getElementById('webpack-dev-server-client-overlay')
    if (resizeObserverErr) {
      resizeObserverErr.setAttribute('style', 'display: none');
    }
    if (resizeObserverErrDiv) {
      resizeObserverErrDiv.setAttribute('style', 'display: none');
    }
    return true;
  }
  return false;
}

window.addEventListener('error', resizeObserverErrorHandler);
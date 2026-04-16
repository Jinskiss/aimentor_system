/**
 * 应用入口文件
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 全局样式（借鉴参考项目设计风格）
import './styles/index.css'

const app = createApp(App)

// 使用插件（注意顺序：pinia -> router）
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化主题（在应用挂载前）
import { useThemeStore } from './stores/theme'
useThemeStore()

app.mount('#app')

console.log('应用已挂载')
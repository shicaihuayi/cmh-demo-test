/**
 * 测盟汇前端 - 应用入口文件
 * 
 * 功能说明：
 * - Vue3应用初始化
 * - Element Plus UI框架集成
 * - Vue Router路由配置
 * - Pinia状态管理
 * - Axios HTTP客户端配置
 * - 全局组件注册
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 */

//引入createApp用于创建应用
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'
//引入ElementPlus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 引入App根组件
import App from './App.vue'
//引入路由器
import router from './router'
import {createPinia} from "pinia";

// 创建Vue应用实例
const app = createApp(App)
const pinia=createPinia()

// 注册所有Element Plus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置axios跨域请求携带凭证（暂时注释，避免TypeScript错误）
// axios.defaults.withCredentials = true;

// 使用插件
app.use(router)      // 路由
app.use(ElementPlus) // UI框架
app.use(pinia)       // 状态管理

// 挂载应用到DOM
app.mount('#app')

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
//引入store
const app = createApp(App)
const pinia=createPinia()
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
axios.defaults.withCredentials = true;
//使用路由器
app.use(router)
app.use(ElementPlus)
app.use(pinia)
app.mount('#app')



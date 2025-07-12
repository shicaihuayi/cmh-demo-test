/**
 * 测盟汇前端 - 路由配置
 * 
 * 功能说明：
 * - Vue Router路由管理
 * - 页面组件路由映射
 * - 嵌套路由配置
 * - 路由守卫和权限控制
 * 
 * 路由结构：
 * - /: 登录页面
 * - /register: 注册页面
 * - /home: 主页面（包含子路由）
 *   - /home/main: 首页
 *   - /home/course: 课程管理
 *   - /home/conference: 会议管理
 *   - /home/analytics: 数据分析
 *   - /home/myself: 个人中心
 *   - /home/user: 用户管理
 *   - /home/tenant: 租户管理
 *   - /home/department: 部门管理
 *   - /home/trend: 行业动态
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 */

//引入createRouter
import { createRouter, createWebHistory } from "vue-router"
//引入组件
import Login from "../view/login.vue";
import Register from "../view/register.vue";
import Home from "../view/home.vue"
import Myself from "../view/myself.vue"
import Main from "../view/main.vue"
import TenantManager from "../view/TenantManager.vue";
import UserManager from "../view/UserManager.vue";
import DepartmentManage from "../view/DepartmentManage.vue";
import ChangePwd from "@/components/changePwd.vue";
import Course from "../view/course.vue";
import Conference from "../view/conference.vue";
import Analytics from "../view/analytics.vue";
import Admin from "@/view/trends/Admin.vue";
import Preview from "@/view/trends/Preview.vue";
import Add from "@/view/trends/Add.vue";
import Edit from "@/view/trends/Edit.vue";

/**
 * 创建Vue Router实例
 * 
 * 配置说明：
 * - history: 使用HTML5 History模式
 * - routes: 路由配置数组
 */
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            name: 'login',
            path: '/',
            component: Login
        },

        {
            name: 'register',
            path: '/register',
            component: Register
        },
        {
            name: 'home',
            path: '/home',
            component: Home,
            children: [{
                name: 'main',
                path: 'main',
                component: Main
            },
            {
                name: 'course',
                path: 'course',
                component: Course
            },
            {
                name: 'conference',
                path: 'conference',
                component: Conference
            },
            {
                name: 'analytics',
                path: 'analytics',
                component: Analytics
            },
            {
                name: 'myself',
                path: 'myself',
                component: Myself,
                children: [{
                    name: 'change',
                    path: 'change',
                    component: ChangePwd
                },
                ]
            },{name: 'user',path: 'user',  component: UserManager },
            { name: 'tenant',path: 'tenant',  component: TenantManager },
            { name: 'department',path: 'department',  component: DepartmentManage },
            { name: 'trend',path: 'trend',  component: Admin},
                {
                    name: 'preview',
                    path: 'preview/:id',
                    component: Preview
                },
                {
                    name: 'add',
                    path: 'add',
                    component: Add
                },
                {
                    name: 'edit',
                    path: 'edit/:id',
                    component: Edit
                },

            ]


        }

    ],
})
export default router;

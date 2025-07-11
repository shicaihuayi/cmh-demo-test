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
//创建路由器
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

# 测盟汇前端项目介绍文档

## 1. 项目概述

本项目是 **测盟汇** Web 应用的第一版前端工程。它是一个基于 Vue 3 和 Element Plus 构建的现代化单页应用（SPA）。项目的主要目标是为测盟汇提供一个功能丰富、易于使用的在线平台，涵盖了用户管理、课程、会议、新闻动态等多个核心业务模块。

项目采用 TypeScript 进行开发，保证了代码的类型安全和可维护性。通过 Vite 进行项目构建和开发，提供了极速的开发体验。

## 2. 技术栈

项目选用业界主流且成熟的技术栈，确保了开发效率和应用的稳定性。

- **核心框架:** [Vue.js 3](https://vuejs.org/)
- **应用构建:** [Vite](https://vitejs.dev/)
- **编程语言:** [TypeScript](https://www.typescriptlang.org/)
- **路由管理:** [Vue Router 4](https://router.vuejs.org/)
- **状态管理:** [Pinia](https://pinia.vuejs.org/)
- **UI 组件库:** [Element Plus](https://element-plus.org/)
- **HTTP 请求:** [Axios](https://axios-http.com/)
- **日期处理:** [Moment.js](https://momentjs.com/)
- **样式预处理器:** [Less](https://lesscss.org/) / [Sass](https://sass-lang.com/)

## 3. 项目结构

项目遵循了 Vue 社区推荐的目录结构，清晰地分离了不同功能的代码。

```
├── public/                # 静态资源，不会被 Webpack 处理
├── src/                   # 核心源代码
│   ├── api/               # API 请求模块
│   │   ├── news.ts
│   │   └── user.ts
│   ├── components/        # 可复用的 UI 组件
│   │   └── changePwd.vue
│   ├── hooks/             # Vue Composition API 的自定义 Hooks
│   │   ├── useCourse.ts
│   │   └── ...
│   ├── img/               # 图片资源
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # Pinia 状态管理
│   │   ├── home.ts
│   │   ├── task.ts
│   │   └── user.ts
│   ├── utils/             # 工具函数
│   │   ├── errorCode.ts
│   │   ├── request.ts
│   │   └── utils.ts
│   ├── view/              # 页面视图组件
│   │   ├── login.vue
│   │   ├── home.vue
│   │   └── ...
│   ├── App.vue            # 根组件
│   └── main.ts            # 应用入口文件
├── index.html             # 主 HTML 文件
├── package.json           # 项目依赖和脚本
└── vite.config.ts         # Vite 配置文件
```

- **`src/api`**: 存放所有与后端交互的 API 请求函数。按模块（如 `user`, `news`）组织文件，方便管理。
- **`src/components`**: 存放全局或可复用的 UI 组件，如密码修改弹窗 `changePwd.vue`。
- **`src/hooks`**: 存放自定义的 Composition API 函数 (Composables)。这些函数用于封装和复用有状态的逻辑，例如表单逻辑 (`useUserForm`) 或特定页面的逻辑 (`useHome`)。
- **`src/router`**: 定义了整个应用的路由规则，将 URL 路径映射到对应的视图组件。
- **`src/stores`**: 使用 Pinia 来管理应用的全局状态。每个文件代表一个独立的 store，如 `user.ts` 负责管理用户登录状态和信息。
- **`src/utils`**: 包含项目的工具函数，例如 `request.ts` 中封装的 Axios 实例，用于统一处理请求拦截、响应拦截和错误码。
- **`src/view`**: 存放页面级别的组件，每个文件通常对应一个路由。

## 4. 核心功能模块

根据路由和代码结构，项目主要包含以下几个核心功能模块：

### 4.1. 用户与权限
- **登录/注册**: 应用的入口，提供用户身份验证。
- **用户管理**: (仅管理员) 对平台用户进行增删改查。
- **租户管理**: (仅管理员) 管理不同的租户单位。
- **部门管理**: (仅管理员) 管理组织架构内的部门信息。
- **个人中心**: 用户查看个人信息和修改密码的地方。

### 4.2. 业务模块
- **主页 (`main`)**: 用户登录后看到的仪表盘或欢迎页面。
- **课程 (`course`)**: 展示和管理课程信息。
- **会议 (`conference`)**: 展示和管理会议信息。
- **新闻动态 (`trend`)**: 提供了新闻/动态的发布、预览、编辑和管理功能 (CRUD)。

## 5. 状态管理 (Pinia)

项目使用 Pinia 进行状态管理，主要定义了以下几个 `Store`:
- **`userStore`**: 存储用户信息、Token、角色权限等。
- **`homeStore`**: 可能用于管理主页相关的数据状态。
- **`taskStore`**: 可能用于管理任务或待办事项相关的状态。

Pinia 的使用使得跨组件状态共享变得简单和类型安全。

## 6. 数据请求 (Axios)

所有与后端的 HTTP 通信都通过 Axios 完成。在 `src/utils/request.ts` 中，通常会进行以下封装：
- **创建 Axios 实例**: 配置基础 URL (`baseURL`) 和超时时间 (`timeout`)。
- **请求拦截器**: 在请求发送前统一添加 `Authorization` 请求头（携带 Token）。
- **响应拦截器**: 对后端返回的数据进行预处理，例如只返回 `data` 部分，或者统一处理业务错误码和网络异常。
- **携带 `cookie`**: 项目在 `main.ts` 中设置了 `axios.defaults.withCredentials = true;`，允许跨域请求时携带凭证。

## 7. 如何运行

1.  **安装依赖**
    ```bash
    npm install
    ```

2.  **启动开发服务器**
    ```bash
    npm run dev
    ```
    项目将在 Vite 开发服务器上运行，通常地址为 `http://localhost:5173`。

3.  **打包构建**
    ```bash
    npm run build
    ```
    构建后的生产文件将输出到 `dist` 目录。 
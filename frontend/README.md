# 测盟汇Web端管理系统

## 项目简介

测盟汇Web端管理系统是我们参与开发的一个综合性测试技术交流平台管理后台，为"电子质量管理协会计算机软硬件和信息系统质量测评分会"提供全方位的会议管理、课程培训、行业动态发布等功能。该系统采用现代化的前后端分离架构，提供了直观易用的管理界面，有效解决了传统会议组织过程中的效率低下、信息散乱等问题。

## 项目背景

在开发过程中，我们发现传统的会议组织方式存在以下痛点：
- 基于电子邮件的会议注册工作量大、及时性差，参会者信息更新困难
- 微信群的通知和新闻发布方式不够有序，重要信息容易被淹没
- 优秀会议报告无法在线回放，影响知识传播和共享

基于这些实际需求，我们设计并开发了这套完整的Web端管理系统。

## 技术架构

### 前端技术栈
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **样式预处理**: SCSS

### 后端技术栈
- **框架**: Spring Boot 2.x
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis Plus
- **认证方式**: Session + JWT
- **文件存储**: 本地文件系统

## 核心功能模块

### 1. 用户管理与认证模块

我们设计了完整的用户权限管理体系，支持多级权限控制：

- **超级管理员(role=3)**: 拥有系统所有权限，可以管理所有用户、租户、部门
- **企业管理员(role=2)**: 管理本企业用户和资源
- **普通管理员(role=1)**: 管理本部门资源
- **普通用户(role=0)**: 仅可查看和参与

**主要功能**:
- 用户注册、登录、密码修改
- 个人信息管理（头像、昵称、联系方式等）
- 用户列表查询、搜索、分页
- 权限分配和角色管理
- 登录状态维护和会话管理

### 2. 租户管理模块

为了解决多企业协作的需求，我们开发了租户管理系统：

- **租户信息管理**: 企业基本信息、联系方式、状态管理
- **部门管理**: 支持多级部门结构，便于组织架构管理
- **管理员分配**: 为每个租户分配专属管理员
- **数据隔离**: 确保不同租户间的数据安全隔离

### 3. 部门管理模块

- **部门层级管理**: 支持树形结构的部门组织
- **部门信息维护**: 部门名称、描述、负责人等信息
- **用户部门分配**: 将用户分配到对应部门
- **权限继承**: 部门级别的权限控制

### 4. 行业动态管理模块

我们开发了完整的资讯发布和管理系统：

- **动态发布**: 支持富文本编辑、图片上传
- **审核机制**: 普通用户发布需要管理员审核
- **分类管理**: 按类型、作者、时间等维度分类
- **搜索功能**: 支持标题、内容、作者的模糊搜索
- **状态管理**: 草稿、待审核、已发布、已下架等状态

### 5. 课程管理模块

针对技术培训需求，我们设计了专业的课程管理系统：

- **课程信息管理**: 课程名称、封面、简介、视频等
- **视频上传**: 支持大文件上传，带进度显示
- **审核流程**: 企业用户创建的课程需要超级管理员审核
- **课程排序**: 支持自定义排序，优化展示效果
- **批量操作**: 支持批量删除、状态修改等

### 6. 会议管理模块

这是系统的核心模块，我们重点解决了会议组织的痛点：

- **会议创建**: 支持详细的会议信息录入
- **时间管理**: 自动计算会议状态（未开始、进行中、已结束）
- **参会回执**: 移动端用户可填写参会回执
- **审核机制**: 企业用户创建的会议需要审核
- **分类管理**: 会议研讨、标准定制、技术培训、工具研发等

### 7. 数据分析与反馈模块

为了提供数据驱动的决策支持，我们开发了数据分析模块：

- **会议统计**: 总会议数、参与人次、月度趋势
- **课程统计**: 总课程数、报名人次、分类分布
- **用户行为分析**: 用户活跃度、参与度分析
- **可视化图表**: 使用ECharts展示数据趋势
- **报表导出**: 支持数据导出功能

### 8. 个人中心模块

- **个人信息管理**: 头像、昵称、联系方式等
- **密码修改**: 安全的密码更新机制
- **登录历史**: 查看登录记录
- **系统设置**: 个性化配置选项

## 项目特色

### 1. 响应式设计
我们采用了现代化的UI设计，确保在不同设备上都有良好的用户体验。使用Element Plus组件库，提供了统一的设计语言和交互体验。

### 2. 权限精细化控制
设计了基于角色的访问控制(RBAC)系统，确保不同用户只能访问和操作被授权的资源，提高了系统的安全性。

### 3. 数据实时更新
使用Vue 3的响应式系统，结合Pinia状态管理，实现了数据的实时更新和状态同步。

### 4. 用户体验优化
- 加载状态提示
- 操作确认对话框
- 错误信息友好展示
- 表单验证和提示
- 批量操作支持

## 开发环境搭建

### 环境要求
- Node.js 16+
- npm 或 yarn
- Vue CLI 或 Vite

### 安装步骤

```bash
# 克隆项目
git clone [项目地址]

# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 环境配置

在项目根目录创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=测盟汇管理系统
```

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口封装
│   ├── components/        # 公共组件
│   ├── hooks/            # 组合式函数
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── types/            # TypeScript类型定义
│   ├── utils/            # 工具函数
│   ├── view/             # 页面组件
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── package.json          # 项目配置
├── vite.config.ts        # Vite配置
└── tsconfig.json         # TypeScript配置
```

## 部署说明

### 开发环境
```bash
npm run dev
```

### 生产环境
```bash
npm run build
npm run preview
```

### Docker部署
```dockerfile
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "run", "preview"]
```

## 开发规范

### 代码规范
- 使用ESLint + Prettier进行代码格式化
- 遵循Vue 3 Composition API最佳实践
- TypeScript类型定义完整
- 组件命名采用PascalCase
- 文件命名采用kebab-case

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

## 性能优化

### 前端优化
- 路由懒加载
- 组件按需引入
- 图片懒加载
- 代码分割
- 缓存策略

### 用户体验优化
- 骨架屏加载
- 防抖节流
- 错误边界处理
- 离线提示

## 测试策略

### 单元测试
- 使用Vitest进行单元测试
- 组件测试覆盖率>80%
- 工具函数100%测试覆盖

### 集成测试
- API接口测试
- 用户流程测试
- 权限控制测试

## 版本历史

### v1.0.0 (2024-12-19)
- 初始版本发布
- 完成核心功能模块开发
- 实现基础权限管理
- 支持会议、课程、动态管理

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 联系方式

- 项目维护者: [汪申俊]
- 邮箱: [3181993730@qq.com]
- 项目地址: [https://github.com/shicaihuayi/cmh-demo-test]

---

*本项目是软件工程实训课程的一部分，旨在通过实际项目开发提升团队协作能力和技术实践水平。*

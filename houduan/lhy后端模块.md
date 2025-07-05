# 测盟汇后端项目说明文档

## 1. 项目概述

本项目是"测盟汇"平台的后端服务，基于 Java Spring Boot 框架开发。它为前端应用提供了一整套 RESTful API，用于处理用户管理、文章发布、企业信息、会议安排、课程管理、部门组织以及文件上传等核心业务功能。

## 2. 技术栈

*   **核心框架**: Spring Boot 3.3.1
*   **编程语言**: Java 17
*   **数据库**: MySQL
*   **数据持久层**: MyBatis, MyBatis-Plus
*   **缓存**: Spring Data Redis
*   **Web服务**: Spring Web
*   **工具库**:
    *   `Lombok`: 简化实体类开发。
    *   `FastJSON`: 高性能的JSON处理库。
    *   `aliyun-java-sdk-dysmsapi`: 阿里云短信服务SDK，用于发送验证码。

## 3. 项目结构

```
hou/
├── pom.xml               # Maven项目配置，定义项目依赖和构建方式
└── src/
    └── main/
        ├── java/
        │   └── com/lfx/demo/
        │       ├── controller/   # 控制器层：处理HTTP请求，调用服务层
        │       ├── entity/       # 实体层：定义数据库表对应的Java对象
        │       ├── mapper/       # 数据访问层：定义与数据库交互的接口
        │       ├── service/      # 服务层：实现核心业务逻辑
        │       └── util/         # 工具类：通用工具和配置
        └── resources/
            └── application.properties # Spring Boot配置文件
```

## 4. 核心模块说明

系统主要包含以下几个核心功能模块：

### 4.1. 用户模块 (`UserController`, `LoginController`)

管理平台的所有用户，包括注册、登录、信息管理等功能。

*   **注册与登录**:
    *   支持通过手机验证码进行注册。
    *   提供独立的 `/login` 和 `/user/login` 接口进行登录，登录成功后用户信息存入Session。
*   **用户信息管理**:
    *   提供用户的增、删、改、查（CRUD）功能。
    *   支持多条件模糊搜索用户信息。
    *   支持修改用户状态（如启用/禁用）。
    *   支持用户修改个人信息和密码。
*   **API列表**:
    *   `POST /login`: 用户登录 (根路径)
    *   `POST /user/login`: 用户登录
    *   `POST /user/register`: 用户注册
    *   `POST /user/send`: 发送手机验证码
    *   `GET /user/list`: 获取所有用户列表
    *   `POST /user/add`: 添加新用户
    *   `POST /user/del`: 批量删除用户
    *   `POST /user/update`: 更新用户信息
    *   `POST /user/search`: 多条件搜索用户
    *   `GET /user/loadMyself`: 获取当前登录用户信息
    *   `POST /user/changePwd`: 修改当前用户密码
    *   `POST /user/exit`: 用户退出登录

### 4.2. 文章模块 (`ArticleController`)

负责平台内的文章内容管理。

*   **功能**: 提供文章的增、删、改、查（CRUD）以及分页查询功能。
*   **API列表**:
    *   `POST /article/add`: 添加文章
    *   `POST /article/update`: 修改文章
    *   `POST /article/delete`: 删除文章
    *   `GET /article/detail`: 获取文章详情
    *   `POST /article/pageList`: 分页查询文章列表

### 4.3. 公司模块 (`CompanyController`)

管理与平台合作或注册的公司信息。

*   **功能**: 提供公司的增、删、改、查功能，并支持多条件搜索。
*   **API列表**:
    *   `GET /company/list`: 获取所有公司列表
    *   `POST /company/add`: 添加新公司
    *   `POST /company/update`: 修改公司信息
    *   `POST /company/del`: 批量删除公司
    *   `GET /company/find`: 根据名称查找公司
    *   `POST /company/search`: 多条件搜索公司

### 4.4. 会议模块 (`ConferController`)

用于发布和管理会议信息。

*   **功能**: 提供会议的增、删、改、查和搜索功能。
*   **API列表**:
    *   `GET /confer/list`: 获取会议列表
    *   `POST /confer/add`: 添加会议
    *   `POST /confer/update`: 修改会议
    *   `POST /confer/del`: 批量删除会议
    *   `POST /confer/search`: 多条件搜索会议

### 4.5. 课程模块 (`CourseController`)

负责在线课程的管理，包含发布和审核流程。

*   **功能**:
    *   提供课程的增、删、改、查功能。
    *   支持课程发布，发布后课程进入"审核中"状态。
    *   管理员可以对"审核中"的课程进行审核（通过/不通过）。
*   **API列表**:
    *   `GET /course/list`: 获取课程列表（区分管理员视图）
    *   `POST /course/add`: 添加课程
    *   `POST /course/update`: 修改课程
    *   `POST /course/del`: 批量删除课程
    *   `POST /course/search`: 搜索课程
    *   `POST /course/publish`: 发布课程（提交审核）
    *   `POST /course/pass`: 审核通过课程

### 4.6. 部门模块 (`DepartmentController`)

用于管理公司内部的组织架构。

*   **功能**:
    *   提供部门的增、删、改、查功能。
    *   支持按公司获取其下所有部门。
    *   添加和修改部门时，会校验同一父部门下名称和排序号的唯一性。
*   **API列表**:
    *   `POST /department/add`: 添加部门
    *   `POST /department/modify`: 修改部门
    *   `GET /department/delete`: 根据ID删除部门
    *   `GET /department/companydata`: 获取某公司的所有部门数据
    *   `POST /department/search`: 搜索部门

### 4.7. 文件上传模块 (`FileController`, `UploadController`)

提供文件上传服务。

*   **`FileController`**:
    *   提供一个通用的文件上传接口。
    *   `/file/upload`: 文件上传后保存至项目根目录下的 `public/` 文件夹，并返回可访问的URL。
*   **`UploadController`**:
    *   提供针对特定类型文件的上传接口。
    *   `/upload/video`: 上传视频，保存到服务器的硬编码路径 `D:/backProject/upload`。
    *   `/upload/image`: 上传图片，保存到服务器的硬编码路径 `D:/video/upload`。
    *   **注意**: `UploadController` 中使用了硬编码的本地磁盘路径，这在生产环境中可能存在问题，建议后续修改为相对路径或可配置的路径。

## 5. 如何运行

1.  **配置数据库**:
    *   打开 `src/main/resources/application.properties` 文件。
    *   修改 `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` 以匹配你的MySQL数据库配置。
2.  **配置Redis**:
    *   同样在 `application.properties` 文件中，配置 `spring.redis.host` 和 `spring.redis.port`。
3.  **运行项目**:
    *   在项目根目录下，执行Maven命令 `mvn spring-boot:run`。
    *   或者直接运行 `DemoApplication.java` 类的主方法。
4.  **服务访问**:
    *   服务启动后，默认监听 `8080` 端口。API的基础路径为 `http://localhost:8080`。

---
*文档生成时间: {CURRENT_DATETIME}* 
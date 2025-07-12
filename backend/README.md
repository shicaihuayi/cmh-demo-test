# 测盟汇后端服务系统

## 项目简介

测盟汇后端服务系统是我独立开发的一套完整的RESTful API服务，为"电子质量管理协会计算机软硬件和信息系统质量测评分会"提供强大的后端支撑。该系统采用Spring Boot框架构建，实现了用户管理、会议管理、课程培训、行业动态、数据分析等核心功能，为前端Web管理系统和微信小程序提供稳定可靠的数据服务。

## 项目背景

在开发过程中，我发现传统的会议组织方式存在诸多痛点：基于电子邮件的会议注册效率低下、微信群的信息发布散乱无序、优秀会议报告无法在线回放等。为了解决这些问题，我设计并开发了这套完整的后端服务系统，通过现代化的技术架构和合理的业务设计，有效提升了会议组织的效率和用户体验。

## 技术架构

### 核心技术栈
- **框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis Plus
- **认证方式**: Session + JWT Token
- **文件存储**: 本地文件系统
- **构建工具**: Maven
- **开发语言**: Java 8+

### 技术选型说明
我选择Spring Boot作为主要框架，主要考虑以下因素：
- **快速开发**: Spring Boot的自动配置特性大大减少了配置工作
- **生态丰富**: Spring生态提供了完整的解决方案
- **性能稳定**: 经过大量生产环境验证，性能表现优异
- **社区活跃**: 文档完善，问题解决资源丰富

## 系统架构设计

### 分层架构
```
┌─────────────────────────────────────┐
│            Controller层              │  # 接口控制层
├─────────────────────────────────────┤
│             Service层                │  # 业务逻辑层
├─────────────────────────────────────┤
│             Mapper层                 │  # 数据访问层
├─────────────────────────────────────┤
│             Entity层                 │  # 实体对象层
└─────────────────────────────────────┘
```

### 模块划分
- **用户管理模块**: 用户认证、权限控制、个人信息管理
- **会议管理模块**: 会议CRUD、参会回执、状态管理
- **课程管理模块**: 课程信息、视频管理、报名系统
- **动态管理模块**: 资讯发布、审核流程、内容管理
- **数据分析模块**: 统计报表、趋势分析、数据可视化
- **文件管理模块**: 图片上传、视频处理、文件存储

## 核心功能模块

### 1. 用户管理与认证模块

我设计了完整的用户权限管理体系，支持多级权限控制：

#### 权限体系设计
- **超级管理员(role=3)**: 拥有系统所有权限，可以管理所有用户、租户、部门
- **企业管理员(role=2)**: 管理本企业用户和资源，可以创建和管理本企业内容
- **普通管理员(role=1)**: 管理本部门资源，可以创建和管理本部门内容
- **普通用户(role=0)**: 仅可查看和参与，主要用于小程序端用户

#### 主要功能
- **用户认证**: 
  - 支持用户名密码登录
  - 微信小程序登录集成
  - Session管理和JWT Token支持
  - 登录失败次数限制和防暴力破解

- **用户管理**:
  - 用户注册、信息修改、状态管理
  - 用户列表查询、搜索、分页
  - 权限分配和角色管理
  - 用户头像上传和管理

- **个人信息管理**:
  - 基本信息修改（昵称、联系方式、邮箱等）
  - 密码修改和验证
  - 头像上传和更新
  - 登录历史记录

#### 技术实现
```java
@RestController
@RequestMapping("/user")
public class UserController {
    // 用户登录接口
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> loginForAdmin(User user, HttpSession session)
    
    // 用户列表查询
    @RequestMapping("/list")
    public Map<String, Object> list(HttpServletRequest request)
    
    // 个人信息更新
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public Map<String, Object> updateUserInfo(@RequestParam("id") Integer id, ...)
}
```

### 2. 会议管理模块

这是系统的核心模块，我重点解决了会议组织的痛点问题：

#### 功能特性
- **会议信息管理**:
  - 会议创建、编辑、删除
  - 会议状态自动管理（未开始、进行中、已结束）
  - 会议分类管理（会议研讨、标准定制、技术培训、工具研发、公益行动）
  - 会议封面图片上传

- **参会回执系统**:
  - 参会人员信息收集
  - 到达方式、车次、时间等详细信息
  - 回执数据统计和分析
  - 参会人员管理

- **审核机制**:
  - 企业用户创建的会议需要超级管理员审核
  - 审核状态跟踪和管理
  - 审核历史记录

#### 技术实现
```java
@Service
public class ConferService {
    public List<Conference> getConferList(String currentUserName, String userRole) {
        // 根据用户角色返回不同的会议列表
        if ("3".equals(userRole)) {
            return mapper.selectConfers(); // 超级管理员查看所有会议
        } else if ("2".equals(userRole) || "1".equals(userRole)) {
            return mapper.selectConfersForNormalAdmin(currentUserName); // 普通管理员查看部分会议
        } else {
            return mapper.selectConfers().stream()
                .filter(conf -> "审核通过".equals(conf.getAuditStatus()))
                .collect(Collectors.toList()); // 普通用户只查看已审核通过的会议
        }
    }
}
```

### 3. 课程管理模块

针对技术培训需求，我设计了专业的课程管理系统：

#### 功能特性
- **课程信息管理**:
  - 课程基本信息（名称、封面、简介、作者等）
  - 课程视频上传和管理
  - 课程排序和分类
  - 课程状态管理

- **视频处理**:
  - 大文件上传支持
  - 上传进度显示
  - 视频格式验证
  - 视频播放地址生成

- **审核流程**:
  - 企业用户创建的课程需要超级管理员审核
  - 审核状态跟踪
  - 审核意见反馈

- **报名系统**:
  - 课程报名功能
  - 报名状态管理
  - 学习进度跟踪

#### 技术实现
```java
@RestController
@RequestMapping("/course")
public class CourseController {
    @RequestMapping("/list")
    public AjaxResult findCourses(HttpSession session) {
        User user = (User) session.getAttribute("login_user");
        // 根据用户权限返回不同的课程列表
        if ("3".equals(user.getRole())) {
            allCourses = courseService.getCourseList(); // 超级管理员查看所有课程
        } else {
            allCourses = courseService.getCourseListForNonSuperAdmin(user.getName()); // 普通管理员查看部分课程
        }
    }
}
```

### 4. 行业动态管理模块

我开发了完整的资讯发布和管理系统：

#### 功能特性
- **动态发布**:
  - 富文本内容编辑
  - 图片上传和管理
  - 动态分类和标签
  - 发布时间管理

- **审核机制**:
  - 普通用户发布需要管理员审核
  - 审核状态跟踪
  - 审核意见反馈

- **内容管理**:
  - 动态列表查询和分页
  - 搜索功能（标题、内容、作者）
  - 状态管理（草稿、待审核、已发布、已下架）
  - 内容统计和分析

#### 技术实现
```java
@RestController
@RequestMapping("/article")
public class ArticleController {
    @RequestMapping("/page")
    public Map<String, Object> getArticlePage(@RequestParam Map<String, Object> params) {
        // 分页查询文章列表
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Integer.parseInt(params.get("pageNum").toString()));
        pageDomain.setPageSize(Integer.parseInt(params.get("pageSize").toString()));
        
        List<Article> articles = articleService.getArticlePage(pageDomain, params);
        return AjaxResult.success(articles);
    }
}
```

### 5. 数据分析与反馈模块

为了提供数据驱动的决策支持，我开发了数据分析模块：

#### 功能特性
- **会议统计**:
  - 总会议数统计
  - 会议参与人次统计
  - 月度参与趋势分析
  - 会议类型分布统计

- **课程统计**:
  - 总课程数统计
  - 课程报名人次统计
  - 月度报名趋势分析
  - 课程分类分布统计

- **用户行为分析**:
  - 用户活跃度分析
  - 用户参与度分析
  - 用户偏好分析

#### 技术实现
```java
@Mapper
public interface AnalyticsMapper {
    // 总会议数
    @Select("SELECT COUNT(*) FROM t_conference")
    long getTotalConferences();
    
    // 会议参与月度趋势
    @Select("SELECT DATE_FORMAT(submit_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM conference_receipt " +
            "GROUP BY DATE_FORMAT(submit_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlyConferenceParticipation();
}
```

### 6. 文件管理模块

我设计了完整的文件上传和管理系统：

#### 功能特性
- **图片上传**:
  - 支持多种图片格式
  - 图片大小限制和压缩
  - 图片存储路径管理
  - 图片访问URL生成

- **视频上传**:
  - 大文件分片上传
  - 上传进度跟踪
  - 视频格式验证
  - 视频存储管理

- **文件安全**:
  - 文件类型验证
  - 文件大小限制
  - 文件存储路径安全
  - 文件访问权限控制

## 数据库设计

### 核心数据表
- **t_user**: 用户信息表
- **t_conference**: 会议信息表
- **t_course**: 课程信息表
- **t_article**: 文章信息表
- **conference_receipt**: 会议回执表
- **t_course_enrollment**: 课程报名表
- **t_company**: 企业信息表

### 数据库优化
- 合理的索引设计
- 查询性能优化
- 数据分页处理
- 连接池配置优化

## 安全设计

### 认证安全
- Session管理和超时控制
- JWT Token验证
- 密码加密存储
- 登录失败次数限制

### 数据安全
- SQL注入防护
- XSS攻击防护
- CSRF攻击防护
- 文件上传安全控制

### 接口安全
- 接口权限验证
- 请求频率限制
- 参数验证和过滤
- 错误信息脱敏

## 性能优化

### 数据库优化
- 索引优化
- 查询语句优化
- 连接池配置
- 分页查询优化

### 应用优化
- 缓存策略
- 异步处理
- 文件上传优化
- 内存使用优化

### 接口优化
- 响应时间优化
- 并发处理优化
- 数据传输优化
- 错误处理优化

## 开发环境搭建

### 环境要求
- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- IDE (推荐IntelliJ IDEA)

### 安装步骤

1. **克隆项目**
```bash
git clone [项目地址]
cd backend
```

2. **数据库配置**
```sql
-- 创建数据库
CREATE DATABASE cemenghui DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入数据库脚本
source db/t_course_enrollment.sql;
```

3. **配置文件修改**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/cemenghui?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **启动项目**
```bash
mvn spring-boot:run
```

### 配置说明

**application.properties**:
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/cemenghui
spring.datasource.username=root
spring.datasource.password=password

# 文件上传配置
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# 服务器配置
server.port=8080
server.servlet.context-path=/

# MyBatis配置
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.lfx.demo.entity
```

## 部署说明

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
```bash
# 打包
mvn clean package

# 运行
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker部署
```dockerfile
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## API文档

### 基础配置
- 基础URL: http://localhost:8080
- 认证方式: Session + JWT Token
- 请求格式: JSON
- 响应格式: JSON

### 主要接口

#### 用户相关
- `POST /user/login` - 用户登录
- `GET /user/list` - 获取用户列表
- `POST /user/updateUser` - 更新用户信息
- `GET /user/loadMyself` - 获取个人信息

#### 会议相关
- `GET /confer/list` - 获取会议列表
- `POST /confer/add` - 创建会议
- `PUT /confer/update` - 更新会议
- `DELETE /confer/delete` - 删除会议
- `POST /confer/receipt` - 提交参会回执

#### 课程相关
- `GET /course/list` - 获取课程列表
- `POST /course/add` - 创建课程
- `PUT /course/update` - 更新课程
- `DELETE /course/delete` - 删除课程
- `POST /course/enroll` - 课程报名

#### 动态相关
- `GET /article/page` - 获取动态列表
- `POST /article/add` - 发布动态
- `PUT /article/update` - 更新动态
- `DELETE /article/delete` - 删除动态

#### 数据分析相关
- `GET /analytics/comprehensive-report` - 获取综合报告
- `GET /analytics/conference-stats` - 获取会议统计
- `GET /analytics/course-stats` - 获取课程统计

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化工具
- 完整的注释和文档
- 合理的异常处理

### 命名规范
- 类名: PascalCase
- 方法名: camelCase
- 常量: UPPER_SNAKE_CASE
- 包名: 全小写

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

## 测试策略

### 单元测试
- 使用JUnit进行单元测试
- 业务逻辑测试覆盖率>80%
- 工具类100%测试覆盖

### 集成测试
- API接口测试
- 数据库操作测试
- 文件上传测试

### 性能测试
- 接口响应时间测试
- 并发处理能力测试
- 数据库性能测试

## 版本历史

### v1.0.0 (2024-12-19)
- 初始版本发布
- 完成核心功能模块开发
- 实现用户认证和权限管理
- 支持会议、课程、动态管理
- 完成数据分析功能

## 常见问题

### 1. 数据库连接失败
- 检查数据库服务是否启动
- 确认数据库连接配置是否正确
- 验证数据库用户权限

### 2. 文件上传失败
- 检查文件上传目录权限
- 确认文件大小是否超限
- 验证文件格式是否支持

### 3. 接口调用失败
- 检查服务是否正常启动
- 确认接口地址和参数是否正确
- 验证用户认证状态

## 联系方式

- 项目维护者: [汪申俊]
- 邮箱: [3181993730@qq.com]
- 项目地址: [https://github.com/shicaihuayi/cmh-demo-test]
---

*本项目是软件工程实训课程的一部分，旨在通过实际项目开发提升后端开发能力和系统架构设计水平。* 
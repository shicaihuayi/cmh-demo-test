# 后端课程审核功能修改指南 (v2)

## 概述
根据前端课程管理功能的更新需求，后端需要进行以下修改以支持新的课程审核流程。**V2版本针对授权和认证问题提供了更明确的解决方案。**

## 1. 数据库修改

### 1.1 课程表字段更新
确保课程表中的`pass`字段支持以下状态：
- `待审核` - 新创建的课程默认状态
- `审核中` - 普通/企业管理员发布后的状态
- `通过` - 超级管理员审核通过
- `不通过` - 超级管理员审核不通过

```sql
-- 确保pass字段支持这些状态值
ALTER TABLE course MODIFY COLUMN pass VARCHAR(20) DEFAULT '待审核';
```

## 2. API接口修改

### 2.1 课程创建接口 `/course/add`
- **修改点**: 新创建的课程状态必须设置为`待审核`
- **修改内容**: 
  ```java
  // 在保存课程时，设置默认状态
  course.setPass("待审核");
  ```

### 2.2 课程修改接口 `/course/update`
- **修改点**: 根据操作者角色决定是否重置审核状态
- **修改内容**:
  ```java
  // 如果是普通管理员修改课程，重置状态为待审核
  if (!isAdmin) {
      course.setPass("待审核");
  }
  // 超级管理员修改课程时保持原状态
  ```

### 2.3 课程发布接口 `/course/publish`
- **修改点**: 将选中课程的状态从`待审核`改为`审核中`
- **修改内容**:
  ```java
  public Result publishCourse(@RequestBody List<Course> courses) {
      for (Course course : courses) {
          // 检查课程状态，只有待审核的课程才能发布
          if ("待审核".equals(course.getPass())) {
              course.setPass("审核中");
              courseService.updateCourse(course);
          }
      }
      return Result.success("课程发布成功，等待审核");
  }
  ```

### 2.4 新增审核列表接口 `/course/audit-list`
- **功能**: 获取状态为`审核中`的课程列表
- **实现内容**:
  ```java
  @GetMapping("/audit-list")
  public Result getAuditList(HttpServletRequest request) {
      // 从请求头获取角色信息 (假设您的拦截器将角色信息放入了header)
      String userRole = request.getHeader("role"); 

      // 权限判断
      if (!"3".equals(userRole)) {
          return Result.error("权限不足，只有超级管理员可以查看审核列表");
      }

      // 执行业务逻辑
      List<Course> auditCourses = courseService.findByStatus("审核中");
      return Result.success(auditCourses).setMsg("加载审核列表成功");
  }
  ```

### 2.5 新增课程审核接口 `/course/audit`
- **功能**: 超级管理员审核课程
- **实现内容**:
  ```java
  @PostMapping("/audit")
  public Result auditCourse(@RequestBody AuditRequest request) {
      Course course = courseService.findById(request.getCourseId());
      if (course == null) {
          return Result.error("课程不存在");
      }
      
      // 只有状态为"审核中"的课程才能被审核
      if (!"审核中".equals(course.getPass())) {
          return Result.error("课程状态不正确，无法审核");
      }
      
      // 更新审核状态
      course.setPass(request.getStatus()); // "通过" 或 "不通过"
      courseService.updateCourse(course);
      
      return Result.success("审核完成");
  }
  
  // 审核请求数据类
  public class AuditRequest {
      private Integer courseId;
      private String courseName;
      private String status;
      private String remark;
      // getters and setters
  }
  ```

## 3. 课程列表接口 `/course/list`
- **修改点**: 根据用户角色返回不同的课程列表
- **修改内容**:
  ```java
  @GetMapping("/list")
  public Result getCourseList(HttpServletRequest request) {
      String role = getUserRole(request); // 获取用户角色
      List<Course> courses;
      
      if ("超级管理员".equals(role)) {
          // 超级管理员可以看到所有课程
          courses = courseService.findAll();
      } else {
          // 普通管理员只能看到自己创建的课程
          String userId = getUserId(request);
          courses = courseService.findByCreator(userId);
      }
      
      return Result.success()
          .put("courses", courses)
          .put("isAdmin", "超级管理员".equals(role));
  }
  ```

## 4. 权限控制
确保以下权限控制正确实现：
- 超级管理员：可以查看所有课程，可以审核课程，不能发布课程
- 普通管理员/企业管理员：只能查看自己创建的课程，可以发布课程，不能审核课程

## 5. 数据库服务层修改

### 5.1 CourseService 新增方法
```java
// 根据状态查询课程
List<Course> findByStatus(String status);

// 根据创建者查询课程
List<Course> findByCreator(String creatorId);

// 批量更新课程状态
void batchUpdateStatus(List<Integer> courseIds, String status);
```

## 6. 状态流转验证
确保后端对课程状态流转进行严格验证：
- 新建课程 → 待审核
- 待审核 → 审核中 (通过发布操作)
- 审核中 → 通过/不通过 (通过审核操作)
- 不允许其他状态转换

## 7. 测试建议
1. 测试不同角色用户的权限控制
2. 测试课程状态流转的正确性
3. 测试审核接口的数据验证
4. 测试并发审核的处理

## 8. 注意事项
1. 所有状态更新操作都需要添加事务处理
2. 审核操作需要记录操作日志
3. 需要处理并发审核的场景（防止重复审核）
4. 确保前后端状态值完全一致

## 9. 前端已完成的修改
- 超级管理员页面：发布按钮改为查看审核列表按钮
- 审核状态显示：所有管理员都能看到课程的审核状态
- 发布逻辑：只有普通管理员和企业管理员能看到发布按钮
- 审核列表：超级管理员可以查看和处理审核中的课程

## 10. "权限不足"问题修复 (`/course/audit-list`)

**问题定位**：前端请求 `/course/audit-list` 时，后端返回 "权限不足"。原因是该接口的后端实现没有对用户角色进行校验。

**解决方案**：为 `/course/audit-list` 接口添加权限校验，确保只有角色ID为'3'的超级管理员可以访问。

**代码示例 (以Spring Boot为例)**：
```java
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController {

    // ... 其他依赖注入

    @GetMapping("/audit-list")
    public Result getAuditList(HttpServletRequest request) {
        // 从请求头获取角色信息 (假设您的拦截器将角色信息放入了header)
        String userRole = request.getHeader("role"); 

        // 权限判断
        if (!"3".equals(userRole)) {
            return Result.error("权限不足，只有超级管理员可以查看审核列表");
        }

        // 执行业务逻辑
        List<Course> auditCourses = courseService.findByStatus("审核中");
        return Result.success(auditCourses).setMsg("加载审核列表成功");
    }

    // ... 其他接口
}
```

## 11. "请先登录"问题修复 (`/course/update`)

**问题定位**：前端使用 `FormData` 提交课程修改数据到 `/course/update` 时，后端返回 "请先登录"。这是因为后端未能正确处理 `multipart/form-data` 类型请求的认证信息。

**解决方案**：确保 `/course/update` 接口能正确处理 `multipart` 请求并完成认证。通常这意味着需要正确配置Spring Security或您的认证拦截器。

**代码示例 (以Spring Boot为例)**：
```java
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController {

    // ... 其他依赖注入

    // 使用 @PostMapping 或 @PutMapping
    @PostMapping("/update") 
    public Result updateCourse(HttpServletRequest request, Course course) { 
        // Course course 参数会自动从form-data中封装
        // 您的认证拦截器应该已经在此之前运行，并验证了用户身份

        // (可选) 再次检查权限
        String userRole = request.getHeader("role");
        if (!"3".equals(userRole)) { // 假设只有管理员可以修改
             // return Result.error("权限不足");
        }

        // 根据前端逻辑，如果非管理员修改，状态应重置
        if (!"3".equals(userRole)) {
            course.setPass("待审核");
        }

        // 执行更新服务
        courseService.updateCourse(course);

        return Result.success().setMsg("修改成功");
    }

    // ... 其他接口
}

```
**注意**：
- 在 `updateCourse` 方法中，参数 `Course course` 可以让Spring MVC自动将表单中的字段映射到`Course`对象的属性上，非常方便。
- 请确保您的**认证拦截器**或**Spring Security配置**对 `/course/update` 路径生效，并且能处理 `multipart/form-data` 请求。

## 12. 其他接口回顾

请确保以下接口的权限和逻辑也已正确实现：

- **`/course/add`**: 需认证。
- **`/course/publish`**: 需认证，且角色不能是超级管理员。
- **`/course/audit`**: 需认证，且角色必须是超级管理员。
- **`/course/list`**: 根据角色返回不同数据。

将此指南交给您的后端开发人员，应该可以快速定位并解决问题。

## 行业动态相关接口

### 1. 新增动态接口 (`/news/add`)
- **修复**: 统一使用 `status` 字段，值为 `PENDING`
- **建议**: 保持现有 `pass` 字段兼容性

### 2. 发布动态接口 (`/news/publish`)
- **修复**: 支持批量发布，参数为 ID 数组
- **建议**: 添加权限检查，确保只有作者可以发布自己的动态

### 3. 审核动态接口 (`/news/approve`, `/news/reject`)
- **修复**: 实现审核通过和驳回功能
- **建议**: 添加操作日志记录

## 课程管理相关接口

### 1. 课程列表接口 (`/course/list`)
- **修复**: 支持 `authorId` 参数用于权限过滤
  - 当 `authorId` 存在时，只返回该用户创建的课程
  - 当 `isAdminView=true` 时，返回所有课程
- **建议**: 后端实现权限过滤逻辑

### 2. 新增课程接口 (`/course/add`)
- **修复**: 自动设置 `authorId` 为当前用户ID
- **修复**: 统一使用 `status` 字段，值为 `PENDING`
- **建议**: 保持现有 `pass` 字段兼容性

### 3. 更新课程接口 (`/course/update`)
- **修复**: 支持权限参数
  - `adminEdit`: 是否为管理员编辑
  - `operatorRole`: 操作者角色
  - `operatorId`: 操作者ID
- **建议**: 添加权限检查，确保只有作者或超级管理员可以编辑

### 4. 删除课程接口 (`/course/delete`)
- **修复**: 支持多种权限参数
  - `operatorId`: 操作者ID
  - `operatorRole`: 操作者角色
  - `isSuperAdmin`: 是否为超级管理员
  - `adminForceDelete`: 管理员强制删除
- **建议**: 添加权限检查，确保只有作者或超级管理员可以删除

### 5. 课程审核接口 (`/course/approve`, `/course/reject`)
- **修复**: 实现审核通过和驳回功能
- **建议**: 只允许超级管理员进行审核操作

### 6. 课程发布接口 (`/course/publish` 或 `/course/submitForReview`)
- **修复**: 支持批量发布，参数为 ID 数组
- **建议**: 添加权限检查，确保只有作者可以发布自己的课程

## 权限体系

### 角色定义
- **超级管理员 (role=3)**: 
  - 可以查看、编辑、删除所有课程
  - 拥有审核权限
  - 不能发布课程（只能审核）
  
- **企业管理员 (role=2)**: 
  - 只能查看、编辑、删除自己创建的课程
  - 拥有发布权限
  - 不能审核课程

- **普通管理员 (role=1)**: 
  - 只能查看、编辑、删除自己创建的课程
  - 拥有发布权限
  - 不能审核课程

### 状态流转
- **新建**: `PENDING`（待审核）
- **发布**: `REVIEWING`（审核中）
- **通过**: `PUBLISHED`（已发布）
- **驳回**: `REJECTED`（已驳回）→ 可重新发布

## 数据兼容性

### 状态字段映射
```javascript
// 前端处理新旧状态映射
const statusMapping = {
  '待审核': 'PENDING',
  '审核中': 'REVIEWING',
  '通过': 'PUBLISHED',
  '不通过': 'REJECTED'
};
```

### 建议后端返回格式
```json
{
  "id": 1,
  "name": "课程名称",
  "author": "作者名",
  "authorId": 123,
  "status": "PENDING",
  "pass": "待审核",
  "coverUrl": "/path/to/cover.jpg",
  "videoUrl": "/path/to/video.mp4",
  "introduction": "课程简介",
  "courseOrder": 1,
  "createTime": "2024-01-01 10:00:00"
}
```

## 🚨 紧急修复：课程添加500错误

### 错误现象
- 前端发送课程添加请求后，后端返回：`{"msg":"系统繁忙，请稍后再试","code":500}`
- 请求数据格式正确，包含所有必需字段

### 可能原因及解决方案

#### 1. 数据库字段不匹配
**检查项**：
- 确认数据库表是否有 `authorId` 字段
- 确认数据库表是否有 `status` 字段
- 检查字段类型是否匹配（如 `authorId` 应为数字类型）

**解决方案**：
```sql
-- 如果缺少字段，添加相应字段
ALTER TABLE course ADD COLUMN authorId INT;
ALTER TABLE course ADD COLUMN status VARCHAR(20);
```

#### 2. 字段验证失败
**检查项**：
- 后端是否对 `status` 字段进行了枚举验证
- 是否对 `authorId` 进行了外键约束检查

**解决方案**：
```java
// 暂时放宽验证或添加对应的枚举值
public enum CourseStatus {
    PENDING("待审核"),
    REVIEWING("审核中"), 
    PUBLISHED("已发布"),
    REJECTED("已驳回")
}
```

#### 3. 权限验证失败
**检查项**：
- 检查用户ID是否存在
- 检查用户权限是否允许创建课程

#### 4. 响应格式统一
**当前问题**：后端返回格式不统一
- 成功时：`{isOk: true, msg: "...", data: ...}`
- 失败时：`{code: 500, msg: "..."}`

**建议统一格式**：
```json
{
  "isOk": false,
  "code": 500,
  "msg": "系统繁忙，请稍后再试",
  "data": null
}
```

### 调试步骤
1. 检查后端日志，查看具体异常信息
2. 确认数据库连接和表结构
3. 验证请求数据格式是否符合后端期望
4. 测试用户权限和数据库约束

## 权限过滤问题

### 问题现象
企业管理员看不到自己创建的课程（过滤后数量为0）

### 可能原因
1. 后端返回的课程数据中缺少 `authorId` 字段
2. `authorId` 字段值与用户ID不匹配
3. 数据类型不一致（字符串vs数字）

### 解决方案
后端在返回课程列表时，确保包含 `authorId` 字段：
```json
{
  "id": 1,
  "name": "课程名称",
  "author": "作者名",
  "authorId": 98,  // 重要：必须包含此字段
  "status": "PENDING",
  "pass": "待审核"
}
```

## 实施优先级

1. **🔥 紧急**: 修复课程添加500错误
2. **高优先级**: 课程列表权限过滤
3. **中优先级**: 新增/更新课程权限控制
4. **低优先级**: 审核接口实现 
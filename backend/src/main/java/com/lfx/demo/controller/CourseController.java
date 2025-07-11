package com.lfx.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfx.demo.entity.Course;
import com.lfx.demo.entity.User;
import com.lfx.demo.entity.dto.CourseAuditDTO;
import com.lfx.demo.entity.dto.CourseResponseDTO;
import com.lfx.demo.service.CourseService;
import com.lfx.demo.util.MyException;
import com.lfx.demo.util.RequestLimitUtil;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course")
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseService courseService;
    @Autowired
    private RequestLimitUtil requestLimitUtil;
    @Autowired
    private ObjectMapper objectMapper;

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }
    
    @RequestMapping("/list")
    public AjaxResult findCourses(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 用户未登录检查
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }
            
            logger.info(">>> /course/list: 收到来自用户 ID '{}' 的请求", user.getId());
            
            // 对管理员用户不限制，对普通用户进行防抖检查
            if (!user.getName().equals("admin")) {
                String requestKey = "course_list_" + session.getId();
                if (!requestLimitUtil.isAllowed(requestKey)) {
                    return AjaxResult.error("请求过于频繁，请稍后再试");
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            boolean isAdmin = "admin".equals(user.getName()) || "3".equals(user.getRole());
            data.put("isAdmin", isAdmin);
            
            List<Course> allCourses;
            // 根据新的权限体系：role="3"是超级管理员，role="2"是企业管理员，role="1"是普通管理员
            if ("3".equals(user.getRole())) {
                // 超级管理员查看所有课程
                allCourses = courseService.getCourseList();
                logger.info(">>> /course/list: 超级管理员用户, 共找到 {} 门课程", allCourses.size());
            } else {
                // 企业管理员和普通管理员只能查看超级管理员创建的课程和自己创建的课程
                allCourses = courseService.getCourseListForNonSuperAdmin(user.getName());
                logger.info(">>> /course/list: 用户 '{}' (角色: {}) 找到 {} 门课程", user.getName(), user.getRole(), allCourses.size());
            }

            // 将 Course 列表转换为 CourseResponseDTO 列表
            List<CourseResponseDTO> courseDTOs = allCourses.stream().map(course -> {
                CourseResponseDTO dto = new CourseResponseDTO();
                dto.setId(course.getId());
                dto.setName(course.getName());
                dto.setCoverUrl(course.getCoverUrl());
                dto.setIntroduction(course.getIntroduction());
                dto.setAuthor(course.getAuthor());
                if (course.getAuthorId() != null) {
                    dto.setAuthorId(String.valueOf(course.getAuthorId())); // 转换为字符串
                }
                dto.setVideoUrl(course.getVideoUrl());
                dto.setCourseOrder(course.getCourseOrder());
                dto.setPass(course.getPass());
                dto.setStatus(course.getStatus());
                return dto;
            }).collect(Collectors.toList());
            
            // 待审核课程列表（只有管理员需要）
            List<Course> publishCourses = null;
            if (isAdmin) {
                publishCourses = courseService.selectPublish();
            }
            
            data.put("courses", courseDTOs); // 使用转换后的 DTO 列表
            data.put("courses2", publishCourses);
            
            AjaxResult result = AjaxResult.success("加载成功", data);
            
            try {
                // 手动将最终结果序列化为JSON字符串并打印
                String jsonResponse = objectMapper.writeValueAsString(result);
                logger.info(">>> /course/list: 最终序列化的JSON响应: {}", jsonResponse);
            } catch (Exception e) {
                logger.error(">>> /course/list: 序列化JSON响应时发生错误", e);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error(">>> /course/list: 接口发生错误", e);
            return AjaxResult.error("系统繁忙，请稍后再试");
        }
    }

    @RequestMapping("/add")
    public AjaxResult add(@RequestBody Course course, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }
            
            // 参数验证
            if (course.getName() == null || course.getName().trim().isEmpty()) {
                return AjaxResult.error("课程名称不能为空");
            }
            
            // 自动填充创建者信息
            course.setCreator(user.getName());              // 创建者用户名
            course.setCreator_id(user.getId());             // 创建者ID
            course.setDepartment(user.getSection());        // 创建者部门
            course.setCreator_role(user.getRole().toString()); // 创建者角色
            
            // 设置默认状态为待审核
            course.setStatus("PENDING");
            course.setPass("待审核");
            
            logger.info(">>> /course/add: 创建课程，创建者信息 - 用户名: {}, ID: {}, 部门: {}, 角色: {}", 
                user.getName(), user.getId(), user.getSection(), user.getRole());
            
            if (courseService.addCourse(course)) {
                return AjaxResult.success("添加成功");
            } else {
                return AjaxResult.error("添加失败");
            }
            
        } catch (Exception e) {
            logger.error(">>> /course/add: 添加课程发生异常", e);
            return AjaxResult.error("系统繁忙，请稍后再试");
        }
    }
    
    @RequestMapping("/update")
    public AjaxResult update(@RequestBody Course course, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");

            // 1. 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }

            // 2. 参数验证
            if (course.getId() == null) {
                return AjaxResult.error("课程ID不能为空");
            }

            logger.info(">>> /course/update: 用户ID: {} 正在尝试修改课程ID: {}", user.getId(), course.getId());

            // 3. 从数据库加载原始课程数据
            Course existingCourse = courseService.selectById(course.getId());

            if (existingCourse == null) {
                return AjaxResult.error("课程不存在，无法修改");
            }

            logger.info(">>> /course/update: 找到的课程信息 - authorId: {}", existingCourse.getAuthorId());

            // 4. 权限验证 (核心修复)
            // 使用数据库中的 authorId 进行验证，而不是前端传来的值
            boolean isAdmin = "admin".equals(user.getName()) || "3".equals(user.getRole());
            boolean isOwner = existingCourse.getAuthorId() != null && existingCourse.getAuthorId().equals(user.getId());

            logger.info(">>> /course/update: 权限检查 - isAdmin: {}, isOwner: {}", isAdmin, isOwner);
            logger.info(">>> /course/update: 比较 User ID (类型: {}, 值: {}) 与 Course Author ID (类型: {}, 值: {})",
                user.getId().getClass().getName(), user.getId(),
                existingCourse.getAuthorId() != null ? existingCourse.getAuthorId().getClass().getName() : "null", existingCourse.getAuthorId());

            if (!isAdmin && !isOwner) {
                return AjaxResult.error("权限不足，只能修改自己创建的课程");
            }

            // 5. 更新数据
            // 只更新允许修改的字段，并保留原始的创建者信息
            existingCourse.setName(course.getName());
            existingCourse.setCoverUrl(course.getCoverUrl());
            existingCourse.setIntroduction(course.getIntroduction());
            existingCourse.setAuthor(course.getAuthor());
            existingCourse.setCourseOrder(course.getCourseOrder());
            existingCourse.setVideoUrl(course.getVideoUrl());
            
            // 如果是超级管理员，允许更新审核状态
            if ("3".equals(user.getRole())) {
                logger.info(">>> /course/update: 超级管理员正在更新状态. pass: {}, status: {}", course.getPass(), course.getStatus());
                if (course.getPass() != null) {
                    existingCourse.setPass(course.getPass());
                }
                if (course.getStatus() != null) {
                    existingCourse.setStatus(course.getStatus());
                }
            } else {
                // 企业管理员和普通管理员修改课程时，如果原状态是"已驳回"，则修改后状态变为"待审核"
                if ("REJECTED".equals(existingCourse.getStatus()) || "已驳回".equals(existingCourse.getPass())) {
                    existingCourse.setStatus("PENDING");
                    existingCourse.setPass("待审核");
                    logger.info(">>> /course/update: 修改已驳回的课程，状态重置为待审核");
                } else {
                    logger.info(">>> /course/update: 非超级管理员修改，保持原有状态. pass: {}, status: {}", existingCourse.getPass(), existingCourse.getStatus());
                }
            }

            if (courseService.updateCourse(existingCourse)) {
                logger.info(">>> /course/update: 课程ID: {} 修改成功", existingCourse.getId());
                return AjaxResult.success("修改成功");
            } else {
                logger.warn(">>> /course/update: 课程ID: {} 修改失败", existingCourse.getId());
                return AjaxResult.error("修改失败");
            }

        } catch (Exception e) {
            logger.error(">>> /course/update: 修改课程发生异常", e);
            return AjaxResult.error("系统繁忙，请稍后再试: " + e.getMessage());
        }
    }
    
    //模糊搜索
    @RequestMapping("/search")
    public AjaxResult search(@RequestBody Map<String,String> map) {
        try {
            String name = map.get("name");
            String courseOrder = map.get("courseOrder");
            List<Course> courses = courseService.serachCourse(name, courseOrder);
            return AjaxResult.success("查询成功", courses);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("查询失败");
        }
    }
    
    //删除
    @RequestMapping("/del")
    public AjaxResult deleteCourses(@RequestBody List<Integer> ids, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            logger.info(">>> /course/del: 收到批量删除请求，用户ID: {}, 要删除的课程IDs: {}", 
                user != null ? user.getId() : "null", ids);
            
            // 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }
            
            // 权限验证：只有管理员可以批量删除（或者可以根据需要调整为只能删除自己的课程）
            if (!user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有管理员可以删除课程");
            }
            
            boolean isOk = courseService.delCourses(ids);
            if (isOk) {
                logger.info(">>> /course/del: 批量删除成功，删除了 {} 门课程", ids.size());
                return AjaxResult.success("删除成功");
            } else {
                logger.warn(">>> /course/del: 批量删除失败，课程IDs: {}", ids);
                return AjaxResult.error("删除失败");
            }
            
        } catch (Exception e) {
            logger.error(">>> /course/del: 批量删除发生异常", e);
            return AjaxResult.error("删除操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除单个课程接口
     */
    @RequestMapping("/del/single")
    public AjaxResult deleteSingleCourse(@RequestBody Map<String, Object> requestData, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            logger.info(">>> /course/del/single: 收到单个删除请求，用户ID: {}, 请求数据: {}", 
                user != null ? user.getId() : "null", requestData);
            
            // 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }
            
            // 从请求数据中提取课程ID
            Object idObj = requestData.get("id");
            if (idObj == null) {
                return AjaxResult.error("课程ID不能为空");
            }
            
            Integer courseId;
            try {
                courseId = Integer.valueOf(idObj.toString());
            } catch (NumberFormatException e) {
                return AjaxResult.error("课程ID格式错误");
            }
            
            // 检查课程是否存在以及权限
            List<Course> allCourses = courseService.getCourseList();
            Course targetCourse = allCourses.stream()
                .filter(course -> course.getId().equals(courseId))
                .findFirst()
                .orElse(null);
            
            if (targetCourse == null) {
                return AjaxResult.error("课程不存在");
            }
            
            // 权限验证：管理员可以删除任何课程，普通用户只能删除自己的课程
            boolean canDelete = user.getName().equals("admin") || 
                               (targetCourse.getAuthorId() != null && targetCourse.getAuthorId().equals(user.getId()));
            
            if (!canDelete) {
                return AjaxResult.error("权限不足，只能删除自己创建的课程");
            }
            
            // 执行删除操作
            List<Integer> idsToDelete = List.of(courseId);
            boolean isOk = courseService.delCourses(idsToDelete);
            
            if (isOk) {
                logger.info(">>> /course/del/single: 删除成功，课程ID: {}, 课程名称: {}", 
                    courseId, targetCourse.getName());
                return AjaxResult.success("删除成功");
            } else {
                logger.warn(">>> /course/del/single: 删除失败，课程ID: {}", courseId);
                return AjaxResult.error("删除失败，请稍后重试");
            }
            
        } catch (Exception e) {
            logger.error(">>> /course/del/single: 单个删除发生异常", e);
            return AjaxResult.error("删除操作失败: " + e.getMessage());
        }
    }
    
    @RequestMapping("/publish")
    public AjaxResult publishCourse(@RequestBody List<Course> courses, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }

            int successCount = 0;
            for (Course c : courses) {
                c.setStatus("REVIEWING"); // 设置为审核中状态
                c.setPass("审核中"); // 设置pass字段为审核中
                if (courseService.updateCourse(c)) { // 使用现有的updateCourse方法
                    successCount++;
                }
            }

            if (successCount == courses.size()) {
                return AjaxResult.success("发布成功，请等待审核");
            } else {
                return AjaxResult.error("部分课程发布失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("系统繁忙，请稍后再试");
        }
    }
    
    @RequestMapping("/pass")
    public AjaxResult passCourse(@RequestBody Course course, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 权限验证
            if (user == null || !user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有管理员可以进行审核操作");
            }
            
            logger.info(">>> /course/pass: 接收到审核请求，课程ID: {}, pass: {}, status: {}", 
                course.getId(), course.getPass(), course.getStatus());
            
            // 确保审核状态的正确映射
            if ("通过".equals(course.getPass()) || "PUBLISHED".equals(course.getStatus())) {
                course.setPass("通过");
                course.setStatus("PUBLISHED");
            } else if ("不通过".equals(course.getPass()) || "REJECTED".equals(course.getStatus())) {
                course.setPass("不通过");
                course.setStatus("REJECTED");
            }
            
            logger.info(">>> /course/pass: 修正后的状态，课程ID: {}, pass: {}, status: {}", 
                course.getId(), course.getPass(), course.getStatus());
            
            if (courseService.updateStatus(course)) {
                logger.info(">>> /course/pass: 审核成功，课程ID: {}", course.getId());
                return AjaxResult.success("审核成功");
            } else {
                logger.warn(">>> /course/pass: 审核失败，课程ID: {}", course.getId());
                return AjaxResult.error("该课程已被删除");
            }
            
        } catch (MyException e) {
            logger.error(">>> /course/pass: MyException异常", e);
            return AjaxResult.error("审核失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error(">>> /course/pass: 系统异常", e);
            e.printStackTrace();
            return AjaxResult.error("系统繁忙，请稍后再试");
        }
    }

    /**
     * 获取待审核课程列表接口
     * @return 待审核的课程列表
     */
    @RequestMapping("/audit-list")
    public AjaxResult getPublishList(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 权限验证
            if (user == null || !user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有管理员可以查看待审核列表");
            }
            
            List<Course> list = courseService.selectPublish();
            return AjaxResult.success("获取待审核列表成功", list);
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("获取待审核列表失败");
        }
    }

    /**
     * 获取待发布课程列表接口 - 兼容前端路径
     * @return 待发布的课程列表
     */
    @RequestMapping("/publishList")
    public AjaxResult getPublishListCompat(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            // 权限验证
            if (user == null || !user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有管理员可以查看待审核列表");
            }
            
            logger.info(">>> /course/publishList: 管理员 {} 请求待审核课程列表", user.getName());
            
            List<Course> list = courseService.selectPublish();
            
            logger.info(">>> /course/publishList: 找到 {} 门待审核课程", list.size());
            
            return AjaxResult.success("获取待审核列表成功", list);
            
        } catch (Exception e) {
            logger.error(">>> /course/publishList: 获取待审核列表发生异常", e);
            return AjaxResult.error("获取待审核列表失败");
        }
    }

    /**
     * 获取待审核课程列表接口 - 新的标准接口名称
     * @return 待审核的课程列表
     */
    @RequestMapping("/pendingList")
    public AjaxResult getPendingList(HttpSession session) {
        // 复用publishList的逻辑
        return getPublishListCompat(session);
    }

    /**
     * 测试端点 - 验证 /course/publishList 路径是否正常工作
     */
    @RequestMapping("/publishList/test")
    public AjaxResult testPublishListEndpoint() {
        logger.info(">>> /course/publishList/test: 测试端点被调用");
        return AjaxResult.success("publishList 端点工作正常", "测试成功，API路径配置正确");
    }

    /**
     * 调试端点 - 查看特定课程的详细信息
     */
    @RequestMapping("/debug/course/{id}")
    public AjaxResult debugCourseById(@PathVariable Integer id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }
            
            // 从数据库查询课程详情
            List<Course> allCourses = courseService.getCourseList();
            Course targetCourse = allCourses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElse(null);
            
            if (targetCourse == null) {
                return AjaxResult.error("课程不存在，ID: " + id);
            }
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("course", targetCourse);
            debugInfo.put("queryTime", java.time.LocalDateTime.now());
            
            logger.info(">>> /course/debug/course/{}: 课程详情 - ID: {}, name: {}, pass: {}, status: {}", 
                id, targetCourse.getId(), targetCourse.getName(), targetCourse.getPass(), targetCourse.getStatus());
            
            return AjaxResult.success("获取课程详情成功", debugInfo);
            
        } catch (Exception e) {
            logger.error(">>> /course/debug/course/{}: 获取课程详情异常", id, e);
            return AjaxResult.error("获取课程详情失败: " + e.getMessage());
        }
    }

    /**
     * 课程审核接口
     * @param auditDTO 审核请求数据
     * @param session HTTP会话
     * @return 审核结果
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public AjaxResult auditCourse(@RequestBody CourseAuditDTO auditDTO, HttpSession session) {
        try {
            // 打印接收到的参数用于调试
            System.out.println("接收到的审核请求: " + auditDTO.toString());
            
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有管理员可以进行审核操作");
            }

            // 参数验证
            if (auditDTO.getCourseId() == null) {
                return AjaxResult.error("课程ID不能为空");
            }

            if (auditDTO.getStatus() == null || auditDTO.getStatus().trim().isEmpty()) {
                return AjaxResult.error("审核状态不能为空");
            }

            // 创建课程对象进行审核
            Course course = new Course();
            course.setId(auditDTO.getCourseId());
            course.setStatus(auditDTO.getStatus());

            // 执行审核逻辑
            if ("通过".equals(auditDTO.getStatus())) {
                // 审核通过：更新课程状态
                if (courseService.updateStatus(course)) {
                    return AjaxResult.success("课程审核通过");
                } else {
                    return AjaxResult.error("该课程已被删除或不存在");
                }
            } else if ("不通过".equals(auditDTO.getStatus())) {
                // 审核不通过：更新课程状态
                if (courseService.updateStatus(course)) {
                    String message = "课程审核不通过";
                    if (auditDTO.getRemark() != null && !auditDTO.getRemark().trim().isEmpty()) {
                        message += "，原因：" + auditDTO.getRemark();
                    }
                    return AjaxResult.success(message);
                } else {
                    return AjaxResult.error("该课程已被删除或不存在");
                }
            } else {
                return AjaxResult.error("无效的审核状态，只支持'通过'或'不通过'");
            }

        } catch (MyException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("审核操作失败");
        }
    }

    /**
     * 数据管理接口：检查和修复课程数据
     */
    @RequestMapping("/debug/data")
    public AjaxResult debugCourseData(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 获取所有课程数据
            List<Course> allCourses = courseService.getCourseList();
            debugInfo.put("totalCourses", allCourses.size());
            
            // 统计authorId情况
            long coursesWithAuthorId = allCourses.stream()
                .filter(course -> course.getAuthorId() != null)
                .count();
            long coursesWithoutAuthorId = allCourses.size() - coursesWithAuthorId;
            
            debugInfo.put("coursesWithAuthorId", coursesWithAuthorId);
            debugInfo.put("coursesWithoutAuthorId", coursesWithoutAuthorId);
            
            // 获取当前用户的课程
            List<Course> userCourses = courseService.getCourseListByAuthorId(user.getId());
            debugInfo.put("currentUserCourses", userCourses.size());
            
            // 详细课程信息（最多显示10条）
            List<Map<String, Object>> courseDetails = allCourses.stream()
                .limit(10)
                .map(course -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", course.getId());
                    detail.put("name", course.getName());
                    detail.put("author", course.getAuthor());
                    detail.put("authorId", course.getAuthorId());
                    detail.put("status", course.getStatus());
                    detail.put("pass", course.getPass());
                    return detail;
                })
                .collect(java.util.stream.Collectors.toList());
            
            debugInfo.put("courseDetails", courseDetails);
            debugInfo.put("currentUser", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "nickname", user.getNickname(),
                "role", user.getRole()
            ));
            
            return AjaxResult.success("课程数据检查完成", debugInfo);
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("数据检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 数据修复接口：为现有课程设置authorId
     */
    @RequestMapping("/debug/fix-data")
    public AjaxResult fixCourseData(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }
            
            // 只有管理员可以执行数据修复
            if (!user.getName().equals("admin") && !"3".equals(user.getRole())) {
                return AjaxResult.error("权限不足，只有管理员可以执行数据修复");
            }
            
            // 获取所有没有authorId的课程
            List<Course> allCourses = courseService.getCourseList();
            List<Course> coursesToFix = allCourses.stream()
                .filter(course -> course.getAuthorId() == null)
                .collect(java.util.stream.Collectors.toList());
            
            int fixedCount = 0;
            
            // 为没有authorId的课程设置默认的authorId
            for (Course course : coursesToFix) {
                try {
                    // 尝试将author字段转换为用户ID
                    if (course.getAuthor() != null) {
                        try {
                            Integer authorIdFromString = Integer.parseInt(course.getAuthor().trim());
                            course.setAuthorId(authorIdFromString);
                        } catch (NumberFormatException e) {
                            // 如果author不是数字，设置为当前用户ID
                            course.setAuthorId(user.getId());
                        }
                    } else {
                        // 如果author为空，设置为当前用户ID
                        course.setAuthorId(user.getId());
                    }
                    
                    if (courseService.updateCourse(course)) {
                        fixedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("修复课程 " + course.getId() + " 失败: " + e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCourses", allCourses.size());
            result.put("coursesToFix", coursesToFix.size());
            result.put("fixedCourses", fixedCount);
            
            return AjaxResult.success("数据修复完成", result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("数据修复失败: " + e.getMessage());
        }
    }

    /**
     * 创建测试课程接口
     */
    @RequestMapping("/debug/create-test")
    public AjaxResult createTestCourse(HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }
            
            // 创建一个测试课程
            Course testCourse = new Course();
            testCourse.setName("测试课程 - " + user.getName());
            testCourse.setCoverUrl("/public/default-course.jpg");
            testCourse.setIntroduction("这是一个由用户 " + user.getName() + " 创建的测试课程");
            testCourse.setVideoUrl("");
            testCourse.setCourseOrder(1);
            testCourse.setStatus("草稿");
            
            if (courseService.addCourse(testCourse)) {
                Map<String, Object> result = new HashMap<>();
                result.put("courseCreated", true);
                result.put("courseName", testCourse.getName());
                
                return AjaxResult.success("测试课程创建成功", result);
            } else {
                return AjaxResult.error("测试课程创建失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("创建测试课程时出错: " + e.getMessage());
        }
    }

    /**
     * 将现有课程分配给当前用户（仅用于测试）
     */
    @RequestMapping("/debug/assign-course")
    public AjaxResult assignCourseToCurrentUser(@RequestParam Integer courseId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }
            
            if (courseId == null) {
                return AjaxResult.error("课程ID不能为空");
            }
            
            // 获取所有课程，找到指定的课程
            List<Course> allCourses = courseService.getCourseList();
            Course targetCourse = allCourses.stream()
                .filter(course -> course.getId().equals(courseId))
                .findFirst()
                .orElse(null);
            
            if (targetCourse == null) {
                return AjaxResult.error("课程不存在");
            }
            
            // 更新课程的作者信息
            targetCourse.setAuthor(user.getNickname() != null ? user.getNickname() : user.getName());
            targetCourse.setAuthorId(user.getId());
            
            if (courseService.updateCourse(targetCourse)) {
                Map<String, Object> result = new HashMap<>();
                result.put("courseId", targetCourse.getId());
                result.put("courseName", targetCourse.getName());
                result.put("newAuthor", targetCourse.getAuthor());
                result.put("newAuthorId", targetCourse.getAuthorId());
                
                return AjaxResult.success("课程分配成功", result);
            } else {
                return AjaxResult.error("课程分配失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("分配课程失败: " + e.getMessage());
        }
    }

    /**
     * 静态数据测试接口（最终验证）
     * 直接返回一个写死的、100%正确的JSON字符串，用于最终验证前端逻辑。
     */
    @GetMapping(value = "/debug/static-test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStaticCourses() {
        String staticJsonResponse = """
        {
            "msg": "加载成功 [来自静态数据]",
            "code": 200,
            "data": {
                "courses": [
                    {
                        "id": 50,
                        "name": "静态测试课程 - A",
                        "coverUrl": "/public/default-course.jpg",
                        "introduction": "这是一个用于最终验证前端逻辑的静态课程。",
                        "author": "ceshi1",
                        "authorId": "98",
                        "videoUrl": "",
                        "courseOrder": 1,
                        "pass": "未发布",
                        "status": "草稿"
                    },
                    {
                        "id": 51,
                        "name": "静态测试课程 - B",
                        "coverUrl": "/public/default-course.jpg",
                        "introduction": "这个课程的作者ID也是98。",
                        "author": "ceshi1",
                        "authorId": "98",
                        "videoUrl": "",
                        "courseOrder": 2,
                        "pass": "通过",
                        "status": "发布"
                    }
                ],
                "isAdmin": false
            }
        }
        """;
        return ResponseEntity.ok(staticJsonResponse);
    }

    /**
     * 详细测试删除功能的每个步骤
     */
    @RequestMapping("/debug/test-delete-detailed")
    public AjaxResult testDeleteDetailed(@RequestParam Integer courseId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 步骤1：用户验证
            User user = (User) session.getAttribute("login_user");
            result.put("step1_user_check", user != null ? 
                Map.of("id", user.getId(), "name", user.getName(), "role", user.getRole()) : 
                "用户未登录");
            
            if (user == null) {
                return AjaxResult.error("用户未登录", result);
            }
            
            // 步骤2：查找课程
            logger.info(">>> 测试删除 - 步骤2：查找课程ID {}", courseId);
            List<Course> allCourses = courseService.getCourseList();
            result.put("step2_all_courses_count", allCourses.size());
            
            Course targetCourse = allCourses.stream()
                .filter(course -> course.getId().equals(courseId))
                .findFirst()
                .orElse(null);
            
            if (targetCourse != null) {
                result.put("step2_target_course", Map.of(
                    "id", targetCourse.getId(),
                    "name", targetCourse.getName(),
                    "author", targetCourse.getAuthor(),
                    "authorId", targetCourse.getAuthorId()
                ));
            } else {
                result.put("step2_target_course", "课程不存在");
                return AjaxResult.error("课程不存在", result);
            }
            
            // 步骤3：权限检查
            boolean isAdmin = user.getName().equals("admin");
            boolean isOwner = targetCourse.getAuthorId() != null && targetCourse.getAuthorId().equals(user.getId());
            boolean canDelete = isAdmin || isOwner;
            
            result.put("step3_permissions", Map.of(
                "isAdmin", isAdmin,
                "isOwner", isOwner,
                "canDelete", canDelete,
                "userIdType", user.getId().getClass().getSimpleName(),
                "authorIdType", targetCourse.getAuthorId() != null ? targetCourse.getAuthorId().getClass().getSimpleName() : "null",
                "idsEqual", targetCourse.getAuthorId() != null && targetCourse.getAuthorId().equals(user.getId())
            ));
            
            if (!canDelete) {
                return AjaxResult.error("权限不足", result);
            }
            
            // 步骤4：模拟删除（不实际删除）
            List<Integer> idsToDelete = List.of(courseId);
            result.put("step4_delete_params", Map.of(
                "idsToDelete", idsToDelete,
                "deleteMethod", "courseService.delCourses(idsToDelete)"
            ));
            
            // 步骤5：实际执行删除
            logger.info(">>> 测试删除 - 步骤5：执行删除操作");
            boolean deleteResult = courseService.delCourses(idsToDelete);
            result.put("step5_delete_result", deleteResult);
            
            // 步骤6：验证删除结果
            List<Course> coursesAfterDelete = courseService.getCourseList();
            boolean courseStillExists = coursesAfterDelete.stream()
                .anyMatch(course -> course.getId().equals(courseId));
            
            result.put("step6_verification", Map.of(
                "coursesCountAfterDelete", coursesAfterDelete.size(),
                "courseStillExists", courseStillExists,
                "deleteSuccessful", !courseStillExists
            ));
            
            if (deleteResult && !courseStillExists) {
                return AjaxResult.success("删除测试成功", result);
            } else {
                return AjaxResult.error("删除测试失败", result);
            }
            
        } catch (Exception e) {
            logger.error(">>> 测试删除发生异常", e);
            result.put("exception", e.getMessage());
            result.put("stackTrace", e.getStackTrace()[0].toString());
            return AjaxResult.error("测试删除异常: " + e.getMessage(), result);
        }
    }

    /**
     * 删除课程接口 - 匹配前端请求路径
     */
    @PostMapping("/delete")
    public AjaxResult deleteCourse(@RequestBody Map<String, Object> requestData, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            
            logger.info(">>> /course/delete: 收到删除请求，用户ID: {}, 请求数据: {}", 
                user != null ? user.getId() : "null", requestData);
            
            // 用户登录验证
            if (user == null) {
                return AjaxResult.error("用户未登录，请先登录");
            }
            
            // 从请求数据中提取课程ID
            Object idObj = requestData.get("id");
            if (idObj == null) {
                return AjaxResult.error("课程ID不能为空");
            }
            
            Integer courseId;
            try {
                courseId = Integer.valueOf(idObj.toString());
            } catch (NumberFormatException e) {
                logger.error(">>> /course/delete: 课程ID格式错误: {}", idObj);
                return AjaxResult.error("课程ID格式错误");
            }
            
            // 检查课程是否存在
            List<Course> allCourses = courseService.getCourseList();
            Course targetCourse = allCourses.stream()
                .filter(course -> course.getId().equals(courseId))
                .findFirst()
                .orElse(null);
            
            if (targetCourse == null) {
                logger.warn(">>> /course/delete: 课程不存在，ID: {}", courseId);
                return AjaxResult.error("课程不存在");
            }
            
            logger.info(">>> /course/delete: 找到目标课程 - ID: {}, 名称: {}, 作者ID: {}", 
                targetCourse.getId(), targetCourse.getName(), targetCourse.getAuthorId());
            
            // 权限验证：管理员可以删除任何课程，普通用户只能删除自己的课程
            boolean isAdmin = user.getName().equals("admin");
            boolean isOwner = targetCourse.getAuthorId() != null && targetCourse.getAuthorId().equals(user.getId());
            boolean canDelete = isAdmin || isOwner;
            
            logger.info(">>> /course/delete: 权限检查 - 用户: {}, 是管理员: {}, 是课程所有者: {}, 可以删除: {}", 
                user.getName(), isAdmin, isOwner, canDelete);
            
            if (!canDelete) {
                return AjaxResult.error("权限不足，只能删除自己创建的课程");
            }
            
            // 执行删除操作
            List<Integer> idsToDelete = List.of(courseId);
            logger.info(">>> /course/delete: 开始执行删除操作，课程ID: {}", courseId);
            
            boolean isOk = courseService.delCourses(idsToDelete);
            
            if (isOk) {
                logger.info(">>> /course/delete: 删除成功，课程ID: {}, 课程名称: {}", 
                    courseId, targetCourse.getName());
                return AjaxResult.success("删除成功");
            } else {
                logger.warn(">>> /course/delete: 删除失败，课程ID: {}", courseId);
                return AjaxResult.error("删除失败，请稍后重试");
            }
            
        } catch (Exception e) {
            logger.error(">>> /course/delete: 删除操作发生异常", e);
            return AjaxResult.error("删除操作失败: " + e.getMessage());
        }
    }

    @RequestMapping("/submitForReview")
    public AjaxResult submitForReview(@RequestParam Integer id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("login_user");
            if (user == null) {
                return AjaxResult.error("用户未登录");
            }

            Course courseToSubmit = courseService.selectById(id);

            if (courseToSubmit == null) {
                return AjaxResult.error("课程不存在");
            }
            
            // 权限验证: 只有课程所有者或超级管理员可以提交审核
            boolean isOwner = courseToSubmit.getAuthorId() != null && courseToSubmit.getAuthorId().equals(user.getId());
            boolean isAdmin = "admin".equals(user.getName());

            if (!isOwner && !isAdmin) {
                return AjaxResult.error("权限不足，只能发布自己创建的课程");
            }

            // 检查课程状态，只有"草稿"、"PENDING"（待审核）、"REJECTED"（已驳回）状态的课程才能提交审核
            String currentStatus = courseToSubmit.getStatus();
            logger.info(">>> /course/submitForReview: 课程ID: {}, 当前状态: {}, pass字段: {}", 
                id, currentStatus, courseToSubmit.getPass());
            
            if (!"草稿".equals(currentStatus) && !"PENDING".equals(currentStatus) && !"REJECTED".equals(currentStatus)) {
                logger.warn(">>> /course/submitForReview: 课程状态不允许提交审核，当前状态: {}", currentStatus);
                return AjaxResult.error("该课程已提交审核或已发布，请勿重复操作");
            }

            // 更新课程状态为审核中
            courseToSubmit.setStatus("REVIEWING");
            courseToSubmit.setPass("审核中");
            
            logger.info(">>> /course/submitForReview: 准备更新课程状态 - ID: {}, 新状态: REVIEWING, 新pass: 审核中", id);
            
            if (courseService.updateCourse(courseToSubmit)) {
                logger.info(">>> /course/submitForReview: 提交审核成功，课程ID: {}", id);
                return AjaxResult.success("提交审核成功");
            } else {
                logger.error(">>> /course/submitForReview: 提交审核失败，课程ID: {}", id);
                return AjaxResult.error("提交审核失败");
            }
        } catch (Exception e) {
            logger.error("提交审核时出错", e);
            return AjaxResult.error("系统繁忙，请稍后再试");
        }
    }
}

package com.lfx.demo.controller;

import com.lfx.demo.entity.Course;
import com.lfx.demo.entity.CourseEnrollment;
import com.lfx.demo.entity.User;
import com.lfx.demo.service.CourseEnrollmentService;
import com.lfx.demo.service.CourseService;
import com.lfx.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 小程序课程控制器
 */
@RestController
@RequestMapping("/app/course")
public class AppCourseController {
    private static final Logger logger = LoggerFactory.getLogger(AppCourseController.class);
    
    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseEnrollmentService enrollmentService;

    /**
     * 获取已审核通过的课程列表（小程序版本）
     */
    @RequestMapping("/list")
    public Map<String, Object> getApprovedCourses(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) Integer courseOrder) {
        logger.info("(/app/course/list) [小程序] 获取课程列表 -> 页码: {}, 每页数量: {}, 热门等级: {}", page, size, courseOrder);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 获取所有课程并过滤pass字段为"通过"的课程
            List<Course> allCourses = courseService.getCourseList();
            Stream<Course> courseStream = allCourses.stream()
                    .filter(course -> "通过".equals(course.getPass()));
            
            // 如果提供了热门等级，则添加过滤条件
            if (courseOrder != null) {
                courseStream = courseStream.filter(course -> course.getCourseOrder() != null && course.getCourseOrder().equals(courseOrder));
            }

            List<Course> approvedCourses = courseStream.collect(Collectors.toList());
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, approvedCourses.size());
            List<Course> pagedCourses;
            if (start < approvedCourses.size()) {
                pagedCourses = approvedCourses.subList(start, end);
            } else {
                pagedCourses = List.of();
            }
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> courseList = pagedCourses.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("courses", courseList);
            map.put("total", approvedCourses.size());
            map.put("currentPage", page);
            map.put("pageSize", size);
            
        } catch (Exception e) {
            logger.error("获取课程列表失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 根据ID获取课程详情（小程序版本）
     * 新增：同时检查用户是否已报名该课程
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("/detail")
    public Map<String, Object> getCourseDetail(@RequestParam Integer id, Authentication authentication) {
        logger.info("(/app/course/detail) [小程序] 获取课程详情 -> ID: {}", id);
        
        Map<String, Object> map = new HashMap<>();
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：课程ID不能为空");
            return map;
        }
        
        try {
            Course course = courseService.selectById(id);

            if (course != null && "通过".equals(course.getPass())) {
                Map<String, Object> courseDetail = convertToAppDetailFormat(course);
                
                // 检查用户是否已报名
                boolean isEnrolled = false;
                if (authentication != null && authentication.getPrincipal() instanceof User) {
                    User user = (User) authentication.getPrincipal();
                    CourseEnrollment enrollment = enrollmentService.findByUserIdAndCourseId(user.getId(), id);
                    if (enrollment != null) {
                        isEnrolled = true;
                    }
                    logger.info("用户(ID: {}) 对课程(ID: {}) 的报名状态检查结果: {}", user.getId(), id, isEnrolled);
                } else {
                    logger.warn("无法获取用户信息，无法检查课程报名状态。");
                }
                courseDetail.put("isEnrolled", isEnrolled);
                
                map.put("isOk", true);
                map.put("msg", "获取成功");
                map.put("course", courseDetail);
            } else {
                map.put("isOk", false);
                map.put("msg", "课程不存在或未审核通过");
            }
        } catch (Exception e) {
            logger.error("获取课程详情失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 搜索课程（小程序版本）
     */
    @RequestMapping("/search")
    public Map<String, Object> searchCourses(@RequestParam String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        logger.info("(/app/course/search) [小程序] 搜索课程 -> 关键词: {}", keyword);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 在pass字段为"通过"的课程中搜索
            List<Course> allCourses = courseService.getCourseList();
            List<Course> searchResults = allCourses.stream()
                    .filter(course -> "通过".equals(course.getPass()))
                    .filter(course -> 
                        (course.getName() != null && course.getName().contains(keyword)) ||
                        (course.getAuthor() != null && course.getAuthor().contains(keyword))
                    )
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, searchResults.size());
            List<Course> pagedResults;
            if (start < searchResults.size()) {
                pagedResults = searchResults.subList(start, end);
            } else {
                pagedResults = List.of();
            }
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> courseList = pagedResults.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "搜索成功");
            map.put("courses", courseList);
            map.put("total", searchResults.size());
            map.put("keyword", keyword);
            
        } catch (Exception e) {
            logger.error("搜索课程失败", e);
            map.put("isOk", false);
            map.put("msg", "搜索失败");
        }
        return map;
    }

    /**
     * 课程报名（小程序版本）
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/enroll")
    public Map<String, Object> enrollCourse(@RequestBody Map<String, Object> enrollData) {
        logger.info("(/app/course/enroll) [小程序] 课程报名 -> 数据: {}", enrollData);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // Fix: Handle potential String to Integer conversion issue
            Integer courseId = Integer.parseInt(String.valueOf(enrollData.get("courseId")));
            Integer userId = Integer.parseInt(String.valueOf(enrollData.get("userId")));

            if (courseId == null || userId == null) {
                map.put("isOk", false);
                map.put("msg", "必填信息不完整 (courseId, userId)");
                return map;
            }

            Course course = courseService.selectById(courseId);
            if (course == null) {
                map.put("isOk", false);
                map.put("msg", "课程不存在");
                return map;
            }
            
            User user = userService.findUserById(userId);
            if (user == null) {
                map.put("isOk", false);
                map.put("msg", "用户不存在");
                return map;
            }
            
            CourseEnrollment enrollment = enrollmentService.createEnrollment(course, user);
            
            if (enrollment != null) {
                map.put("isOk", true);
                map.put("msg", "报名成功");
                map.put("enrollment", enrollment);
            } else {
                map.put("isOk", false);
                map.put("msg", "您已报名该课程");
            }
            
        } catch (Exception e) {
            // 关键调试步骤：记录下真正的根本原因
            logger.error("<<<<< 课程报名接口内部发生严重错误 >>>>>", e);
            map.put("isOk", false);
            map.put("msg", "报名失败，服务器内部错误");
        }
        return map;
    }

    /**
     * 获取用户的课程报名记录（小程序版本）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myEnrollments")
    public Map<String, Object> getMyEnrollments(@RequestParam Integer userId) {
        logger.info("(/app/course/myEnrollments) [小程序] 获取用户课程报名记录 -> 用户ID: {}", userId);
        
        Map<String, Object> map = new HashMap<>();
        if (userId == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        
        try {
            List<CourseEnrollment> enrollments = enrollmentService.getEnrollmentsByUserId(userId);
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("enrollments", enrollments);
            map.put("total", enrollments.size());
            
        } catch (Exception e) {
            logger.error("获取用户课程报名记录失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 更新学习进度（小程序版本）
     */
    @RequestMapping("/updateProgress")
    public Map<String, Object> updateProgress(@RequestBody Map<String, Object> progressData) {
        logger.info("(/app/course/updateProgress) [小程序] 更新学习进度 -> 数据: {}", progressData);
        Map<String, Object> map = new HashMap<>();
        try {
            Integer enrollmentId = (Integer) progressData.get("enrollmentId");
            Integer progress = (Integer) progressData.get("progress");

            if (enrollmentId == null || progress == null) {
                map.put("isOk", false);
                map.put("msg", "参数错误");
                return map;
            }

            CourseEnrollment enrollment = enrollmentService.getEnrollmentById(enrollmentId);
            if (enrollment == null) {
                map.put("isOk", false);
                map.put("msg", "报名记录不存在");
                return map;
            }

            enrollment.setProgress(progress);
            if (enrollmentService.updateProgress(enrollment)) {
                map.put("isOk", true);
                map.put("msg", "进度更新成功");
            } else {
                map.put("isOk", false);
                map.put("msg", "进度更新失败");
            }
        } catch (Exception e) {
            logger.error("更新学习进度失败", e);
            map.put("isOk", false);
            map.put("msg", "更新失败");
        }
        return map;
    }

    /**
     * 获取热门课程（小程序版本）
     */
    @RequestMapping("/hot")
    public Map<String, Object> getHotCourses(@RequestParam(defaultValue = "5") int limit) {
        logger.info("(/app/course/hot) [小程序] 获取热门课程 -> 数量限制: {}", limit);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 获取pass字段为"通过"的课程，按某种热度排序（这里简单按ID倒序）
            List<Course> allCourses = courseService.getCourseList();
            List<Course> hotCourses = allCourses.stream()
                    .filter(course -> "通过".equals(course.getPass()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> courseList = hotCourses.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("courses", courseList);
            
        } catch (Exception e) {
            logger.error("获取热门课程失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 将Course对象转换为小程序列表展示格式
     */
    private Map<String, Object> convertToAppFormat(Course course) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("name", course.getName());
        result.put("author", course.getAuthor());
        result.put("coverUrl", course.getCoverUrl());
        result.put("courseOrder", course.getCourseOrder());
        // 简介摘要（截取前100个字符）
        String introduction = course.getIntroduction();
        if (introduction != null && introduction.length() > 100) {
            result.put("summary", introduction.substring(0, 100) + "...");
        } else {
            result.put("summary", introduction);
        }
        return result;
    }

    /**
     * 将Course对象转换为小程序详情展示格式
     */
    private Map<String, Object> convertToAppDetailFormat(Course course) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("name", course.getName());
        result.put("introduction", course.getIntroduction());
        result.put("author", course.getAuthor());
        result.put("coverUrl", course.getCoverUrl());
        result.put("videoUrl", course.getVideoUrl());
        result.put("courseOrder", course.getCourseOrder());
        result.put("status", course.getStatus());
        result.put("pass", course.getPass());
        return result;
    }
} 
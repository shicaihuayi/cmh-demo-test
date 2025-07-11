package com.lfx.demo.controller;

import com.lfx.demo.entity.Course;
import com.lfx.demo.entity.User;
import com.lfx.demo.entity.dto.CourseAuditDTO;
import com.lfx.demo.service.CourseService;
import com.lfx.demo.util.MyException;
import com.lfx.demo.util.RequestLimitUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private RequestLimitUtil requestLimitUtil;

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }
    @RequestMapping("/list")
    public Map findCourses(HttpSession session){
        User user = (User) session.getAttribute("login_user");
        
        // 对管理员用户不限制，对普通用户进行防抖检查
        if (user == null || !user.getName().equals("admin")) {
            String requestKey = "course_list_" + session.getId();
            if (!requestLimitUtil.isAllowed(requestKey)) {
                Map result = new HashMap<>();
                result.put("isOk", false);
                result.put("msg", "请求过于频繁，请稍后再试");
                return result;
            }
        }
        
        Map result = new HashMap<>();

        if (user != null && user.getName().equals("admin")){
            result.put("isAdmin",true);
        }else{
            result.put("isAdmin",false);
        }
        
        // 优化：避免重复查询同一个数据
        List<Course> list = courseService.getCourseList();
        // 移除重复的查询和打印
        List<Course> list2 = courseService.selectPublish();
        
        result.put("isOk", true);
        result.put("msg", "加载成功");
        result.put("courses", list);
        result.put("courses2", list2);
        return result;
    }

    @RequestMapping("/add")
    public Map add( Course course){
        Map result = new HashMap<>();
        if(courseService.addCourse(course)){

            result.put("isOk",true);
            result.put("msg","添加成功");
        }else{
            result.put("isOk",false);
            result.put("msg","添加失败");
        }
        return result;
    }
    @RequestMapping("/update")
    public Map update( Course course){
        Map result = new HashMap<>();
        if(courseService.updateCourse(course)){
            result.put("isOk",true);
            result.put("msg","修改成功");
        }else{
            result.put("isOk",false);
            result.put("msg","修改失败");
        }
        return result;
    }
    //模糊搜索
    @RequestMapping("/search")
    public Map search(@RequestBody Map<String,String> map){

        String name = map.get("name");

       String courseOrder =  map.get("courseOrder");
        Map result = new HashMap<>();
        List<Course> l=courseService.serachCourse(name,courseOrder);
        result.put("isOk",true);
        result.put("msg","查询成功");
        result.put("courses",l);
        return result;
    }
    //删除
    @RequestMapping("/del")
    public Map deleteCourses(@RequestBody List<Integer> ids) {
        boolean isOk = courseService.delCourses(ids);
        Map map = new HashMap();
        if (isOk) {
            map.put("isOk", true);
            map.put("msg", "删除成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "删除失败");
        }
        return map;
    }
    @RequestMapping("/publish")
    public Map publishCourse( @RequestBody List<Course> courses){
        Map map = new HashMap();
        if(courseService.insertPublishes(courses)) {
            for(Course c:courses){
                c.setPass("审核中");
                courseService.updatePassStatus(c);
            }
            map.put("isOk", true);
            map.put("msg", "发布成功，请等待审核");
        }else{
            map.put("isOk", false);
            map.put("msg", "发布失败");
        }
        return map;
    }
    @RequestMapping("/pass")
    public Map passCourse( Course course){
        Map map = new HashMap();
        if(courseService.deletePublish(course.getId())){
            if(courseService.updatePassStatus(course)){
                map.put("isOk", true);
                map.put("msg", "审核成功");
            }else{
                map.put("isOk", false);
                map.put("msg", "该课程已被删除");
            }
        }else{
            throw new MyException("该审核项已不存在");
        }
        return map;
    }

    /**
     * 获取待审核课程列表接口
     * @return 待审核的课程列表
     */
    @RequestMapping("/publishList")
    public Map getPublishList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Course> list = courseService.selectPublish();
            result.put("isOk", true);
            result.put("msg", "获取待审核列表成功");
            result.put("data", list);
        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "获取待审核列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 课程审核接口
     * @param auditDTO 审核请求数据
     * @param session HTTP会话
     * @return 审核结果
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public Map auditCourse(@RequestBody CourseAuditDTO auditDTO, HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 打印接收到的参数用于调试
            System.out.println("接收到的审核请求: " + auditDTO.toString());
            
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有管理员可以进行审核操作");
                return result;
            }

            // 参数验证
            if (auditDTO.getCourseId() == null) {
                result.put("isOk", false);
                result.put("msg", "课程ID不能为空");
                return result;
            }

            if (auditDTO.getStatus() == null || auditDTO.getStatus().trim().isEmpty()) {
                result.put("isOk", false);
                result.put("msg", "审核状态不能为空");
                return result;
            }

            // 创建课程对象进行审核
            Course course = new Course();
            course.setId(auditDTO.getCourseId());
            course.setPass(auditDTO.getStatus());

            // 执行审核逻辑
            if ("通过".equals(auditDTO.getStatus())) {
                // 审核通过：先更新课程状态，然后删除发布表中的记录
                if (courseService.updatePassStatus(course)) {
                    // 可选：删除发布表中的记录（如果存在）
                    courseService.deletePublishByCourseId(course.getId());
                    result.put("isOk", true);
                    result.put("msg", "课程审核通过");
                } else {
                    result.put("isOk", false);
                    result.put("msg", "该课程已被删除或不存在");
                }
            } else if ("不通过".equals(auditDTO.getStatus())) {
                // 审核不通过：先更新课程状态，然后删除发布表中的记录
                if (courseService.updatePassStatus(course)) {
                    // 可选：删除发布表中的记录（如果存在）
                    courseService.deletePublishByCourseId(course.getId());
                    String message = "课程审核不通过";
                    if (auditDTO.getRemark() != null && !auditDTO.getRemark().trim().isEmpty()) {
                        message += "，原因：" + auditDTO.getRemark();
                    }
                    result.put("isOk", true);
                    result.put("msg", message);
                } else {
                    result.put("isOk", false);
                    result.put("msg", "该课程已被删除或不存在");
                }
            } else {
                result.put("isOk", false);
                result.put("msg", "无效的审核状态，只支持'通过'或'不通过'");
            }

        } catch (MyException e) {
            result.put("isOk", false);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "审核操作失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}

package com.lfx.demo.controller;
import com.lfx.demo.entity.Course;
import com.lfx.demo.entity.User;
import com.lfx.demo.service.CourseService;
import com.lfx.demo.util.MyException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }
    @RequestMapping("/list")
    public Map findCourses(HttpSession session){
        User user = (User) session.getAttribute("login_user");
        Map result = new HashMap<>();

        if (user.getName().equals("admin")){
            result.put("isAdmin",true);
        }else{
            result.put("isAdmin",false);

        }
        List<Course> list = courseService.getCourseList();
        for (Course c : courseService.getCourseList()) {
            System.out.println(c);
        }
        List<Course> list2=courseService.selectPublish();
        for (Course c : courseService.selectPublish()) {
            System.out.println(c);
        }
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
}

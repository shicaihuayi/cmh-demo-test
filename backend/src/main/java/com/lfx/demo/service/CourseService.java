package com.lfx.demo.service;


import com.lfx.demo.entity.Course;
import com.lfx.demo.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseMapper mapper;

    public void setMapper(CourseMapper mapper) {
        this.mapper = mapper;
    }
    public List<Course> getCourseList() {

        return mapper.selectAllCourse();
    }
    
    //新增：根据作者ID获取课程列表（用于权限过滤）
    public List<Course> getCourseListByAuthorId(Integer authorId) {
        return mapper.selectCoursesByAuthorId(authorId);
    }
    
    //新增：为非超级管理员获取课程列表（自己创建的课程 + 超级管理员创建的课程）
    public List<Course> getCourseListForNonSuperAdmin(String currentUserName) {
        List<Course> allCourses = mapper.selectAllCourse();
        return allCourses.stream()
            .filter(course -> {
                // 显示自己创建的课程
                if (currentUserName.equals(course.getCreator())) {
                    return true;
                }
                // 显示超级管理员创建的课程（creator_role为"3"）
                if ("3".equals(course.getCreator_role())) {
                    return true;
                }
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    //模糊搜索
    public List<Course> serachCourse(String name,String courseOrder) {
        return mapper.searchCourses(name,courseOrder);
    }
    public boolean addCourse(Course course) {
        System.out.println(course);
        return mapper.insertCourse(course) > 0;
    }
    //批量删除
    public boolean delCourses(List<Integer> ids){
        return mapper.deleteCoursesById(ids)>0;
    }
    public boolean updateCourse(Course course) {
        System.out.println(course);
        return mapper.updateCourse(course)>0;
    }

    public Course selectById(Integer id) {
        return mapper.selectById(id);
    }

    public List<Course> selectPublish(){
        return mapper.selectCoursesByStatus("REVIEWING");
    }

    public boolean updateStatus(Course course){
        return mapper.updateStatus(course)>0;
    }

}

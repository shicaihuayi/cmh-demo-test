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

    public List<Course> selectPublish(){
        return mapper.selectAllPublishCourse();
    }
    public boolean insertPublishes(List<Course> courses){
        return mapper.insertPublish(courses)>0;
    }

    public boolean deletePublishByCourseId(Integer courseId){
        return mapper.deletePublishByCourseId(courseId)>0;
    }
    public boolean deletePublish(Integer id){
        return mapper.deletePublishById(id)>0;
    }
    public boolean updatePassStatus(Course course){
        return mapper.updatePass(course)>0;
    }

}

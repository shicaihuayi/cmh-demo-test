package com.lfx.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyticsMapper {
    
    // 总会议数
    @Select("SELECT COUNT(*) FROM t_conference")
    long getTotalConferences();
    
    // 总会议参与人次
    @Select("SELECT COUNT(*) FROM conference_receipt")
    long getTotalConferenceParticipants();
    
    // 会议参与月度趋势
    @Select("SELECT DATE_FORMAT(submit_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM conference_receipt " +
            "GROUP BY DATE_FORMAT(submit_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlyConferenceParticipation();
    
    // 会议类型分布
    @Select("SELECT COALESCE(category, '未分类') AS name, COUNT(*) AS value " +
            "FROM t_conference " +
            "GROUP BY category")
    List<Map<String, Object>> getConferenceTypeDistribution();

    // 总课程数
    @Select("SELECT COUNT(*) FROM t_course")
    long getTotalCourses();
    
    // 总课程报名人次
    @Select("SELECT COUNT(*) FROM t_course_enrollment")
    long getTotalCourseEnrollments();
    
    // 课程报名月度趋势
    @Select("SELECT DATE_FORMAT(enroll_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM t_course_enrollment " +
            "GROUP BY DATE_FORMAT(enroll_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlyCourseEnrollment();
    
    // 课程分类分布（按部门）
    @Select("SELECT COALESCE(c.department, '未分类') AS name, COUNT(*) AS value " +
            "FROM t_course c " +
            "JOIN t_course_enrollment ce ON c.id = ce.course_id " +
            "GROUP BY c.department")
    List<Map<String, Object>> getCourseCategoryDistribution();
} 
package com.lfx.demo.service;

import com.lfx.demo.entity.Course;
import com.lfx.demo.entity.CourseEnrollment;
import com.lfx.demo.entity.User;
import com.lfx.demo.mapper.CourseEnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseEnrollmentService {

    @Autowired
    private CourseEnrollmentMapper enrollmentMapper;

    /**
     * 创建一个新的课程报名记录
     *
     * @param course 课程对象
     * @param user   用户对象
     * @return 如果报名成功，返回创建的报名记录；如果已报名，返回null。
     */
    @Transactional
    public CourseEnrollment createEnrollment(Course course, User user) {
        // 检查用户是否已报名
        CourseEnrollment existingEnrollment = enrollmentMapper.findByUserIdAndCourseId(user.getId(), course.getId());
        if (existingEnrollment != null) {
            // 用户已报名
            return null;
        }

        // 创建新的报名记录
        CourseEnrollment newEnrollment = new CourseEnrollment();
        newEnrollment.setCourseId(course.getId());
        newEnrollment.setUserId(user.getId());
        newEnrollment.setUserName(user.getName());
        newEnrollment.setPhone(user.getTel());
        newEnrollment.setEmail(user.getEmail());
        newEnrollment.setCourseName(course.getName());
        newEnrollment.setCourseCoverUrl(course.getCoverUrl());
        newEnrollment.setCourseVideoUrl(course.getVideoUrl());
        newEnrollment.setAuthor(course.getAuthor());

        enrollmentMapper.insert(newEnrollment);
        return newEnrollment;
    }

    /**
     * 根据用户ID和课程ID查找报名记录
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 报名记录
     */
    public CourseEnrollment findByUserIdAndCourseId(Integer userId, Integer courseId) {
        return enrollmentMapper.findByUserIdAndCourseId(userId, courseId);
    }

    /**
     * 根据用户ID获取其所有报名课程
     *
     * @param userId 用户ID
     * @return 课程报名记录列表
     */
    public List<CourseEnrollment> getEnrollmentsByUserId(Integer userId) {
        return enrollmentMapper.findByUserId(userId);
    }

    /**
     * 根据ID获取报名记录
     *
     * @param enrollmentId 报名记录ID
     * @return 报名记录
     */
    public CourseEnrollment getEnrollmentById(Integer enrollmentId) {
        return enrollmentMapper.findById(enrollmentId);
    }

    /**
     * 更新学习进度
     *
     * @param enrollment 报名记录
     * @return 是否更新成功
     */
    public boolean updateProgress(CourseEnrollment enrollment) {
        return enrollmentMapper.updateProgress(enrollment) > 0;
    }
} 
package com.lfx.demo.mapper;

import com.lfx.demo.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseEnrollmentMapper {

    /**
     * 插入一条新的课程报名记录
     * @param enrollment 报名记录对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO t_course_enrollment(course_id, user_id, user_name, phone, email, course_name, course_cover_url, course_video_url, author) " +
            "VALUES(#{courseId}, #{userId}, #{userName}, #{phone}, #{email}, #{courseName}, #{courseCoverUrl}, #{courseVideoUrl}, #{author})")
    void insert(CourseEnrollment enrollment);

    /**
     * 根据用户ID查询该用户是否已报名某课程
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 报名记录，如果未报名则返回null
     */
    @Select("SELECT * FROM t_course_enrollment WHERE user_id = #{userId} AND course_id = #{courseId}")
    CourseEnrollment findByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    /**
     * 根据用户ID查询其所有报名记录
     * @param userId 用户ID
     * @return 报名记录列表
     */
    @Select("SELECT * FROM t_course_enrollment WHERE user_id = #{userId}")
    List<CourseEnrollment> findByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM t_course_enrollment WHERE id = #{id}")
    CourseEnrollment findById(@Param("id") Integer id);

    @Update("UPDATE t_course_enrollment SET progress = #{progress}, last_viewed_at = NOW() WHERE id = #{id}")
    int updateProgress(CourseEnrollment enrollment);
} 
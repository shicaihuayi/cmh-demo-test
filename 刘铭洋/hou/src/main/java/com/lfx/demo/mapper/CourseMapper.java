package com.lfx.demo.mapper;


import com.lfx.demo.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {
    //模糊搜索
    @Select({
            "<script>" +
                    "SELECT * FROM t_course WHERE 1=1" +
                    "<if test='name != null and name != \"\"'>" +
                    " AND name LIKE CONCAT('%', #{name}, '%')" +
                    "</if>" +
                    "<if test='courseOrder != null and courseOrder != \"\"'>" +
                    " AND courseOrder LIKE CONCAT('%', #{courseOrder}, '%')" +
                    "</if>" +

                    "</script>"
    })
    List<Course> searchCourses(@Param("name") String name, @Param("courseOrder") String courseOrder);
    //批量删除
    @Delete("<script>" +
            "DELETE FROM t_course WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteCoursesById(@Param("ids") List<Integer> ids);
    @Insert("INSERT INTO t_course (name, coverUrl, introduction, courseOrder, author, videoUrl, `pass`) " +
            "VALUES (#{name}, #{coverUrl}, #{introduction}, #{courseOrder}, #{author}, #{videoUrl}, '未发布')")
    public int insertCourse(Course course);

    @Select("select * from t_course")
    public List<Course> selectAllCourse();
    
    // 修改：查询待审核课程（从主表查询状态为"审核中"的课程）
    @Select("select * from t_course where `pass` = '审核中'")
    public List<Course> selectAllPublishCourse();

    @Update("update t_course set name=#{name},coverUrl=#{coverUrl},introduction=#{introduction},courseOrder=#{courseOrder},author=#{author},videoUrl=#{videoUrl} where id=#{id}")
    int updateCourse(Course course);
    
    // 修改：发布课程时包含课程ID，确保数据同步
    @Insert("<script>" +
            "INSERT INTO t_publishcourses (course_id, name, introduction, author) VALUES " +
            "<foreach collection='list' item='course' separator=','>" +
            "(#{course.id}, #{course.name}, #{course.introduction}, #{course.author})" +
            "</foreach>" +
            "</script>")
    int insertPublish(@Param("list") List<Course> courses);

    // 修改：通过课程ID删除发布记录
    @Delete("delete from t_publishcourses where course_id=#{courseId}")
    int deletePublishByCourseId(int courseId);
    
    // 保留原方法用于兼容
    @Delete("delete from t_publishcourses where id=#{id}")
    int deletePublishById(int id);

    // 修改：审核时通过ID更新状态，而不是通过名称
    @Update("update t_course set `pass`=#{pass} where id=#{id}")
    int updatePass(Course course);
}

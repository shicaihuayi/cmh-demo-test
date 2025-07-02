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
    @Select("select * from t_publishcourses")
    public List<Course> selectAllPublishCourse();

    @Update("update t_course set name=#{name},coverUrl=#{coverUrl},introduction=#{introduction},courseOrder=#{courseOrder},author=#{author},videoUrl=#{videoUrl} where id=#{id}")
    int updateCourse(Course course);
    @Insert("<script>" +
            "INSERT INTO t_publishcourses (name, introduction, author) VALUES " +
            "<foreach collection='list' item='course' separator=','>" +
            "(#{course.name}, #{course.introduction}, #{course.author})" +
            "</foreach>" +
            "</script>")
    int insertPublish(@Param("list") List<Course> courses);

    @Delete("delete from t_publishcourses where id=#{id}")
    int deletePublishById(int id);

    //审核
    @Update("update t_course set `pass`=#{pass} where name=#{name}")
    int updatePass(Course course);
}

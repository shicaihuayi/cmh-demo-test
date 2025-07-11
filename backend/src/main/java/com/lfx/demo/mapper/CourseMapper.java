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
    @Insert("INSERT INTO t_course (name, coverUrl, introduction, courseOrder, author, authorId, videoUrl, `pass`, status, creator, creator_id, department, creator_role) " +
            "VALUES (#{name}, #{coverUrl}, #{introduction}, #{courseOrder}, #{author}, #{authorId}, #{videoUrl}, '待审核', 'PENDING', #{creator}, #{creator_id}, #{department}, #{creator_role})")
    public int insertCourse(Course course);

    @Select("select * from t_course")
    public List<Course> selectAllCourse();
    
    // 新增：根据用户ID查询课程（用于权限过滤）
    @Select("select * from t_course where authorId = #{authorId}")
    public List<Course> selectCoursesByAuthorId(@Param("authorId") Integer authorId);
    
    // 新增：根据创建者用户名查询课程（用于权限过滤）
    @Select("select * from t_course where creator = #{creator}")
    public List<Course> selectCoursesByCreator(@Param("creator") String creator);
    
    @Select("SELECT * FROM t_course WHERE status = #{status}")
    List<Course> selectCoursesByStatus(@Param("status") String status);

    @Select("SELECT * FROM t_course WHERE id = #{id}")
    Course selectById(@Param("id") Integer id);

    @Update("update t_course set name=#{name},coverUrl=#{coverUrl},introduction=#{introduction},courseOrder=#{courseOrder},author=#{author},authorId=#{authorId},videoUrl=#{videoUrl},`pass`=#{pass},status=#{status},creator=#{creator},creator_id=#{creator_id},department=#{department},creator_role=#{creator_role} where id=#{id}")
    int updateCourse(Course course);
    
    @Update("update t_course set `pass`=#{pass}, status=#{status} where id=#{id}")
    int updateStatus(Course course);
}

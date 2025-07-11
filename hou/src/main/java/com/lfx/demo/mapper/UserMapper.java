package com.lfx.demo.mapper;

import com.lfx.demo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from t_user where role != '3' and role != '2'")
    List<User> selectAll();
    @Select("select * from t_user where name=#{name}")
    List<User> selectUserByName(String name);
    @Select("select * from t_user where secId=#{secId}")
    List<User> selectUserById(Integer secId);
    @Select("select * from t_user where companyName=#{companyName} and secId=0")
    List<User> selectUserByCompany(String companyName);
    @Select("select * from t_user where companyName=#{companyName} and role!='3' and role!='2'")
    List<User> selectUserByCompanyName(String companyName);
    //查询
    @Select("select * from t_user where id=#{id}")
    public User selectUserById1(Integer id);
    @Select("select * from t_user where name=#{name}")
    public User selectUserByName1(@Param("name") String name);
    @Insert("insert into t_user (name,pwd,tel,companyName,section,email,time,position,nickname,secId,sex,role,state) values (#{name},#{pwd},#{tel},#{companyName},#{section},#{email},#{time},#{position},#{nickname},#{secId},#{sex},#{role},#{state})")
    int insert(User user);
    @Update("update t_user SET name=#{name},nickname = #{nickname},pwd=#{pwd}, tel=#{tel},section=#{section},sex=#{sex},role=#{role},position=#{position},email=#{email},companyName=#{companyName},secId=#{secId} WHERE id = #{id}")
    int updateUserInfById(User user);
    @Update("update t_user SET state=#{state} WHERE id = #{id}")
    int updateStateInfById(@Param("state") Integer state,@Param("id") Integer id);
    @Update("update t_user SET companyName=#{newName} WHERE companyName = #{oldName}")
    int updateUserByName(String newName,String oldName);
    @Update("update t_user SET name=#{name} WHERE name = #{oldadmin}")
    int updateAdminNameByName(String oldadmin,String name);
    @Update("update t_user set nickname=#{nickname},tel=#{tel},email=#{email},sex=#{sex} where id=#{id}")
    int updateUserInfo(User user);
    @Update("update t_user set section=#{section},companyName=#{companyName} where secId=#{secId}")
    int updateSectionBySection(Integer secId,String section,String companyName);
    @Update("update t_user set companyName=#{companyName} where secId=#{secId}")
    int updateSection(String companyName,Integer secId);
    @Update("update t_user set imageUrl=#{imageUrl} where id=#{id}")
    int updateUserImage(User user);
    //修改密码
    @Update("update t_user set pwd=#{password} where id=#{id}")
    int updatePwd(String password,Integer id);
    @Delete("delete from t_user where companyName=#{companyName}")
    int deleteUserByName(String companyName);
    @Delete("delete from t_user where secId=#{secId}")
    int deleteUserBySecId(Integer secId);
    @Select("select role from t_user where id=#{id}")
    String getRoleById(Integer id);
    @Delete("<script>" +
            "DELETE FROM t_user WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteUserById(@Param("ids") List<Integer> ids);
    @Select("<script>" +
            "SELECT * FROM t_user WHERE 1=1" +
            "<if test='name != null and name != \"\"'>" +
            " AND name LIKE CONCAT('%', #{name}, '%')" +
            "</if>" +
            "<if test='nickname != null and nickname != \"\"'>" +
            " AND nickname LIKE CONCAT('%', #{nickname}, '%')" +
            "</if>" +
            "<if test='tel != null and tel != \"\"'>" +
            " AND tel LIKE CONCAT('%', #{tel}, '%')" +
            "</if>" +
            "<if test='startDate != null and startDate != \"\"'>" +
            " AND time &gt;= #{startDate}" +
            "</if>" +
            "<if test='endDate != null and endDate != \"\"'>" +
            " AND time &lt;= #{endDate}" +
            "</if>" +
            "</script>")
    List<User> searchUsers(@Param("name") String name,
                           @Param("nickname") String nickname,
                           @Param("tel") String tel,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate);

    User findUserById(@Param("id") Integer id);
}

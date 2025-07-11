package com.lfx.demo.mapper;


import com.lfx.demo.entity.Conference;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConferMapper {

    @Select("CALL UpdateConferenceStatus2()")
    void callUpdateConferenceStatus();

    @Select("SELECT * FROM t_conference")
    List<Conference> selectConfers();

    @Select("<script>" +
            "SELECT conferName, creater, stime, etime, picture, category, location, content, conferState, organizer, agenda, guests " +
            "FROM t_conference WHERE auditStatus = '通过'" +
            "<if test='category != null and category != \"\"'>" +
            " AND category = #{category}" +
            "</if>" +
            " ORDER BY stime DESC" +
            " LIMIT #{offset}, #{size}" +
            "</script>")
    List<Conference> selectApprovedForAppPaged(@Param("category") String category, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT count(*) FROM t_conference WHERE auditStatus = '通过'" +
            "<if test='category != null and category != \"\"'>" +
            " AND category = #{category}" +
            "</if>" +
            "</script>")
    int countApprovedForApp(@Param("category") String category);

    @Select("SELECT * FROM t_conference WHERE conferName = #{conferName} AND auditStatus = '通过'")
    Conference selectApprovedByIdForApp(@Param("conferName") String conferName);

    @Select("SELECT c.* FROM t_conference c " +
           "WHERE c.publisher IN (SELECT name FROM t_user WHERE role = '3') " +  // 超级管理员发布的
           "OR c.publisher = #{currentUserName}")  // 当前用户发布的
    List<Conference> selectConfersForNormalAdmin(@Param("currentUserName") String currentUserName);

    @Select("select * from t_conference where auditStatus = '审核中'")
    List<Conference> selectAuditConferences();

    @Select("<script>" +
            "SELECT * FROM t_conference WHERE 1=1" +
            "<if test='conferName != null and conferName != \"\"'>" +
            " AND conferName LIKE CONCAT('%', #{conferName}, '%')" +
            "</if>" +
            "<if test='creater != null and creater != \"\"'>" +
            " AND creater LIKE CONCAT('%', #{creater}, '%')" +
            "</if>" +
            "<if test='content != null and content != \"\"'>" +
            " AND content LIKE CONCAT('%', #{content}, '%')" +
            "</if>" +
            "<if test='stime != null and stime != \"\"'>" +
            " AND stime &gt;= #{stime}" +
            "</if>"  +
            "</script>")
    List<Conference> selectConferForAdmin(@Param("conferName") String conferName,
                           @Param("creater") String creater,
                           @Param("stime") String stime,
                           @Param("content") String content);

    @Select("<script>" +
            "SELECT * FROM t_conference WHERE" +
            " (publisher IN (SELECT name FROM t_user WHERE role = '3') OR publisher = #{currentUserName})" +  // 权限控制
            "<if test='conferName != null and conferName != \"\"'>" +
            " AND conferName LIKE CONCAT('%', #{conferName}, '%')" +
            "</if>" +
            "<if test='creater != null and creater != \"\"'>" +
            " AND creater LIKE CONCAT('%', #{creater}, '%')" +
            "</if>" +
            "<if test='content != null and content != \"\"'>" +
            " AND content LIKE CONCAT('%', #{content}, '%')" +
            "</if>" +
            "<if test='stime != null and stime != \"\"'>" +
            " AND stime &gt;= #{stime}" +
            "</if>"  +
            "</script>")
    List<Conference> selectConfer(@Param("conferName") String conferName,
                           @Param("creater") String creater,
                           @Param("stime") String stime,
                           @Param("content") String content,
                           @Param("currentUserName") String currentUserName);

    @Insert("insert into t_conference (conferName, creater, conferState, content, stime, etime, picture, auditStatus, publisher, category, location, organizer, agenda, guests) " +
            "values (#{conferName}, #{creater}, #{conferState}, #{content, jdbcType=LONGVARCHAR}, #{stime}, #{etime}, #{picture}, '待审核', #{publisher}, #{category}, #{location}, #{organizer}, #{agenda, jdbcType=LONGVARCHAR}, #{guests, jdbcType=LONGVARCHAR})")
    public int insertConfer(Conference confer);

    @Update("<script>" +
            "UPDATE t_conference SET auditStatus = '审核中' WHERE conferName IN " +
            "<foreach item='conferName' collection='conferNames' open='(' separator=',' close=')'>" +
            "#{conferName}" +
            "</foreach>" +
            "</script>")
    int publishConferences(@Param("conferNames") List<String> conferNames);

    @Update("update t_conference set auditStatus = #{auditStatus} where conferName = #{conferName}")
    int updateAuditStatus(@Param("conferName") String conferName, @Param("auditStatus") String auditStatus);

    @Update("update t_conference set auditStatus = #{auditStatus} where id = #{id}")
    int updateAuditStatusById(@Param("id") Integer id, @Param("auditStatus") String auditStatus);

    @Delete("<script>" +
            "DELETE FROM t_conference WHERE conferName IN " +
            "<foreach item='conferName' collection='conferNames' open='(' separator=',' close=')'>" +
            "#{conferName}" +
            "</foreach>" +
            "</script>")
    int deleteConferByName(@Param("conferNames") List<String> conferNames);

    @Select("SELECT * FROM t_conference WHERE conferName = #{conferName}")
    Conference selectByName(@Param("conferName") String conferName);

    @Update("update t_conference set conferName=#{conferName}, creater=#{creater}, publisher=#{publisher}, conferstate=#{conferState}, " +
            "content=#{content, jdbcType=LONGVARCHAR}, stime=#{stime}, etime=#{etime}, picture=#{picture}, auditStatus=#{auditStatus}, " +
            "category=#{category}, location=#{location}, organizer=#{organizer}, agenda=#{agenda, jdbcType=LONGVARCHAR}, guests=#{guests, jdbcType=LONGVARCHAR} " +
            "where conferName=#{oldName}")
    int updateConfer(Conference confer);
}

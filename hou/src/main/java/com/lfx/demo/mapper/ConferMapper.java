package com.lfx.demo.mapper;


import com.lfx.demo.entity.Conference;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConferMapper {

    @Select("CALL UpdateConferenceStatus2()")
    void callUpdateConferenceStatus();

    @Select("select * from t_conference")
    List<Conference> selectConfers();

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
    List<Conference> selectConfer(@Param("conferName") String conferName,
                           @Param("creater") String creater,
                           @Param("stime") String stime,
                           @Param("content") String content);

    @Insert("insert into t_conference (conferName, creater, conferState, content, stime, etime, picture, auditStatus) " +
            "values (#{conferName}, #{creater}, #{conferState}, #{content}, #{stime}, #{etime}, #{picture}, '待审核')")
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

    @Delete("<script>" +
            "DELETE FROM t_conference WHERE conferName IN " +
            "<foreach item='conferName' collection='conferNames' open='(' separator=',' close=')'>" +
            "#{conferName}" +
            "</foreach>" +
            "</script>")
    int deleteConferByName(@Param("conferNames") List<String> conferNames);

    @Update("update t_conference set conferName=#{conferName}, creater=#{creater}, conferstate=#{conferState}, " +
            "content=#{content}, stime=#{stime}, etime=#{etime}, picture=#{picture}, auditStatus=#{auditStatus} " +
            "where conferName=#{oldName}")
    int updateConfer(Conference confer);
}

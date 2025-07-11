package com.lfx.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lfx.demo.entity.ConferenceReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会议回执Mapper接口
 */
@Mapper
public interface ConferenceReceiptMapper extends BaseMapper<ConferenceReceipt> {
    
    /**
     * 根据会议名称查询回执列表
     * @param conferenceName 会议名称
     * @return 回执列表
     */
    @Select("SELECT * FROM conference_receipt WHERE conference_name = #{conferenceName} ORDER BY submit_time DESC")
    List<ConferenceReceipt> selectByConferenceName(@Param("conferenceName") String conferenceName);
    
    /**
     * 根据用户ID和会议名称查询回执
     * @param userId 用户ID
     * @param conferenceName 会议名称
     * @return 回执信息
     */
    @Select("SELECT * FROM conference_receipt WHERE user_id = #{userId} AND conference_name = #{conferenceName} LIMIT 1")
    ConferenceReceipt selectByUserIdAndConferenceName(@Param("userId") Long userId, @Param("conferenceName") String conferenceName);
    
    /**
     * 统计会议报名人数
     * @param conferenceName 会议名称
     * @return 报名人数
     */
    @Select("SELECT COUNT(*) FROM conference_receipt WHERE conference_name = #{conferenceName} AND status = 'submitted'")
    int countByConferenceName(@Param("conferenceName") String conferenceName);
    
    /**
     * 获取所有会议的回执统计信息
     * @return 统计信息列表
     */
    @Select("SELECT conference_name, COUNT(*) as count FROM conference_receipt WHERE status = 'submitted' GROUP BY conference_name ORDER BY count DESC")
    List<Object> getConferenceReceiptStats();
} 
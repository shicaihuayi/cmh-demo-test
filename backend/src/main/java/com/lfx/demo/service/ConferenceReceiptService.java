package com.lfx.demo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfx.demo.entity.ConferenceReceipt;
import com.lfx.demo.mapper.ConferenceReceiptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 会议回执服务类
 */
@Slf4j
@Service
public class ConferenceReceiptService extends ServiceImpl<ConferenceReceiptMapper, ConferenceReceipt> {
    
    @Autowired
    private ConferenceReceiptMapper conferenceReceiptMapper;
    
    /**
     * 提交会议回执
     * @param receipt 回执信息
     * @return 是否成功
     */
    public boolean submitReceipt(ConferenceReceipt receipt) {
        try {
            // 检查是否已经提交过回执
            if (receipt.getUserId() != null) {
                ConferenceReceipt existingReceipt = conferenceReceiptMapper.selectByUserIdAndConferenceName(
                    receipt.getUserId(), receipt.getConferenceName());
                if (existingReceipt != null) {
                    log.warn("用户 {} 已经为会议 {} 提交过回执", receipt.getUserId(), receipt.getConferenceName());
                    return false;
                }
            }
            
            // 设置提交时间和状态
            receipt.setSubmitTime(LocalDateTime.now());
            receipt.setStatus("submitted");
            receipt.setCreatedAt(LocalDateTime.now());
            receipt.setUpdatedAt(LocalDateTime.now());
            
            // 保存到数据库
            int result = conferenceReceiptMapper.insert(receipt);
            
            log.info("会议回执提交成功，会议：{}，参会者：{}", receipt.getConferenceName(), receipt.getName());
            return result > 0;
            
        } catch (Exception e) {
            log.error("提交会议回执失败", e);
            return false;
        }
    }
    
    /**
     * 根据会议名称获取回执列表
     * @param conferenceName 会议名称
     * @return 回执列表
     */
    public List<ConferenceReceipt> getReceiptsByConferenceName(String conferenceName) {
        return conferenceReceiptMapper.selectByConferenceName(conferenceName);
    }
    
    /**
     * 检查用户是否已经提交过回执
     * @param userId 用户ID
     * @param conferenceName 会议名称
     * @return 是否已提交
     */
    public boolean hasSubmittedReceipt(Long userId, String conferenceName) {
        if (userId == null) {
            return false;
        }
        ConferenceReceipt receipt = conferenceReceiptMapper.selectByUserIdAndConferenceName(userId, conferenceName);
        return receipt != null;
    }
    
    /**
     * 统计会议报名人数
     * @param conferenceName 会议名称
     * @return 报名人数
     */
    public int countReceiptsByConferenceName(String conferenceName) {
        return conferenceReceiptMapper.countByConferenceName(conferenceName);
    }
    
    /**
     * 获取所有会议的回执统计信息
     * @return 统计信息列表
     */
    public List<Object> getConferenceReceiptStats() {
        return conferenceReceiptMapper.getConferenceReceiptStats();
    }
    
    /**
     * 处理时间字符串转换
     * @param timeStr 时间字符串
     * @return LocalTime对象
     */
    public LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            log.warn("时间格式解析失败：{}", timeStr);
            return null;
        }
    }
} 
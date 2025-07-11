package com.lfx.demo.service;

import com.lfx.demo.entity.Conference;
import com.lfx.demo.mapper.ConferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ConferService {
    @Autowired
    private ConferMapper mapper;

    public List<Conference> getConferList(String currentUserName, String userRole){
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        
        // 超级管理员可以看到所有会议
        if ("3".equals(userRole)) {
            return mapper.selectConfers();
        }
        // 企业管理员和普通管理员只能看到超级管理员和自己创建的会议
        else if ("2".equals(userRole) || "1".equals(userRole)) {
            return mapper.selectConfersForNormalAdmin(currentUserName);
        }
        // 其他角色（如普通用户）只能看到已审核通过的会议
        else {
            return mapper.selectConfers().stream()
                .filter(conf -> "审核通过".equals(conf.getAuditStatus()))
                .collect(Collectors.toList());
        }
    }

    /**
     * 获取待审核的会议列表
     * @return 待审核会议列表
     */
    public List<Conference> getAuditConferences(){
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        return mapper.selectAuditConferences();
    }

    public List<Conference> getConfer(String conferName, String creater, String stime, String content, String currentUserName, String userRole) {
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        
        // 超级管理员可以搜索所有会议
        if ("3".equals(userRole)) {
            return mapper.selectConferForAdmin(conferName, creater, stime, content);
        }
        // 企业管理员和普通管理员只能搜索自己创建的和超级管理员创建的会议
        else if ("2".equals(userRole) || "1".equals(userRole)) {
            return mapper.selectConfer(conferName, creater, stime, content, currentUserName);
        }
        // 其他角色返回空列表
        else {
            return List.of();
        }
    }

    public boolean addConfer(Conference confer){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.insertConfer(confer) > 0;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 发布会议到审核队列
     * @param conferNames 会议名称列表
     * @return 发布是否成功
     */
    public boolean publishConferences(List<String> conferNames){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.publishConferences(conferNames) > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 审核会议
     * @param conferName 会议名称
     * @param auditStatus 审核状态
     * @return 审核是否成功
     */
    public boolean auditConference(String conferName, String auditStatus){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.updateAuditStatus(conferName, auditStatus) > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过ID审核通过会议
     * @param id 会议ID
     * @return 审核是否成功
     */
    public boolean approveConferenceById(Integer id){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.updateAuditStatusById(id, "通过") > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过ID审核驳回会议
     * @param id 会议ID
     * @return 审核是否成功
     */
    public boolean rejectConferenceById(Integer id){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.updateAuditStatusById(id, "不通过") > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeConfer(List<String> conferName){
        return mapper.deleteConferByName(conferName)>0;
    }

    public boolean modifyConfer(Conference conferFromRequest){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态

            // 1. 从数据库获取原始会议记录
            Conference originalConfer = mapper.selectByName(conferFromRequest.getOldName());
            if (originalConfer == null) {
                // 如果找不到原始记录，则直接返回失败
                return false;
            }

            // 2. 将前端传入的需要更新的字段，更新到原始对象上
            //    保留不需要更新（或前端没传）的字段，比如 publisher
            originalConfer.setConferName(conferFromRequest.getConferName());
            originalConfer.setCreater(conferFromRequest.getCreater());
            originalConfer.setConferState(conferFromRequest.getConferState());
            originalConfer.setContent(conferFromRequest.getContent());
            originalConfer.setStime(conferFromRequest.getStime());
            originalConfer.setEtime(conferFromRequest.getEtime());
            originalConfer.setPicture(conferFromRequest.getPicture());
            originalConfer.setCategory(conferFromRequest.getCategory());
            originalConfer.setLocation(conferFromRequest.getLocation());
            originalConfer.setOrganizer(conferFromRequest.getOrganizer());
            originalConfer.setAgenda(conferFromRequest.getAgenda());
            originalConfer.setGuests(conferFromRequest.getGuests());

            // 传入旧名称以供WHERE子句使用
            originalConfer.setOldName(conferFromRequest.getOldName());

            // 3. 重置审核状态
            originalConfer.setAuditStatus("待审核");

            // 4. 执行更新
            return mapper.updateConfer(originalConfer) > 0;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    // ================ 以下是为小程序端添加的方法 ================

    /**
     * 获取已审核通过的会议列表（小程序用）
     */
    public List<Map<String, Object>> getApprovedConferencesForApp(int page, int size, String category) {
        mapper.callUpdateConferenceStatus();
        int offset = (page - 1) * size;
        List<Conference> pagedConferences = mapper.selectApprovedForAppPaged(category, offset, size);
        return pagedConferences.stream()
                .map(this::convertToAppFormat)
                .collect(Collectors.toList());
    }

    /**
     * 获取已审核通过的会议总数（小程序用）
     */
    public int getApprovedConferencesCount(String category) {
        mapper.callUpdateConferenceStatus();
        return mapper.countApprovedForApp(category);
    }

    /**
     * 获取已审核通过的会议详情（小程序用）
     */
    public Map<String, Object> getApprovedConferenceDetailForApp(String conferName) {
        mapper.callUpdateConferenceStatus();
        Conference conference = mapper.selectApprovedByIdForApp(conferName);
        if (conference != null) {
            return convertToAppDetailFormat(conference);
        }
        return null;
    }

    /**
     * 搜索已审核通过的会议（小程序用）
     */
    public List<Map<String, Object>> searchApprovedConferencesForApp(String keyword, int page, int size) {
        mapper.callUpdateConferenceStatus();
        
        // 在内存中进行关键词过滤
        List<Conference> searchResults = mapper.selectConfers().stream() // 注意：这里为了简化，仍在内存操作，可后续优化为SQL
                .filter(conf -> "通过".equals(conf.getAuditStatus()))
                .filter(conf -> 
                    (conf.getConferName() != null && conf.getConferName().contains(keyword)) ||
                    (conf.getContent() != null && conf.getContent().contains(keyword)) ||
                    (conf.getCreater() != null && conf.getCreater().contains(keyword))
                )
                .collect(Collectors.toList());
        
        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, searchResults.size());
        List<Conference> pagedResults;
        if (start < searchResults.size()) {
            pagedResults = searchResults.subList(start, end);
        } else {
            pagedResults = List.of();
        }
        
        return pagedResults.stream()
                .map(this::convertToAppFormat)
                .collect(Collectors.toList());
    }

    /**
     * 获取搜索已审核通过会议的总数（小程序用）
     */
    public int getSearchApprovedConferencesCount(String keyword) {
        mapper.callUpdateConferenceStatus();
        return (int) mapper.selectConfers().stream() // 注意：这里为了简化，仍在内存操作，可后续优化为SQL
                .filter(conf -> "通过".equals(conf.getAuditStatus()))
                .filter(conf -> 
                    (conf.getConferName() != null && conf.getConferName().contains(keyword)) ||
                    (conf.getContent() != null && conf.getContent().contains(keyword)) ||
                    (conf.getCreater() != null && conf.getCreater().contains(keyword))
                )
                .count();
    }

    /**
     * 提交参会回执（小程序用）
     */
    public boolean submitReceiptForApp(Map<String, Object> receiptData) {
        // 这里需要根据实际的数据库表结构来实现
        // 假设有一个receipt表来存储参会回执信息
        try {
            // 调用mapper方法保存回执信息
            // return mapper.insertReceipt(receiptData) > 0;
            
            // 临时实现：简单返回true，实际需要根据数据库表结构实现
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取用户的参会回执记录（小程序用）
     */
    public List<Map<String, Object>> getUserReceiptsForApp(Integer userId, int page, int size) {
        // 这里需要根据实际的数据库表结构来实现
        // 假设有一个receipt表来存储参会回执信息
        try {
            // 调用mapper方法获取用户的回执记录
            // List<Receipt> receipts = mapper.selectReceiptsByUserId(userId);
            
            // 临时实现：返回空列表，实际需要根据数据库表结构实现
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 获取用户的参会回执记录总数（小程序用）
     */
    public int getUserReceiptsCount(Integer userId) {
        // 这里需要根据实际的数据库表结构来实现
        // 临时实现：返回0，实际需要根据数据库表结构实现
        return 0;
    }

    /**
     * 将Conference对象转换为小程序列表展示格式
     */
    private Map<String, Object> convertToAppFormat(Conference conference) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", conference.getConferName());  // 使用会议名称作为唯一标识
        result.put("name", conference.getConferName());
        result.put("creator", conference.getCreater());
        result.put("startTime", conference.getStime());
        result.put("endTime", conference.getEtime());
        result.put("status", conference.getConferState());
        result.put("picture", conference.getPicture());
        // 内容摘要
        String content = conference.getContent();
        if (content != null && content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        result.put("summary", content);
        result.put("location", conference.getLocation());
        result.put("category", conference.getCategory());
        return result;
    }

    /**
     * 将Conference对象转换为小程序详情展示格式
     */
    private Map<String, Object> convertToAppDetailFormat(Conference conference) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", conference.getConferName());
        result.put("startTime", conference.getStime());
        result.put("endTime", conference.getEtime());
        result.put("location", conference.getLocation());
        result.put("organizer", conference.getOrganizer());
        result.put("agenda", conference.getAgenda());
        result.put("guests", conference.getGuests());
        result.put("picture", conference.getPicture());
        result.put("creator", conference.getCreater());
        result.put("auditStatus", conference.getAuditStatus());
        result.put("status", conference.getConferState());
        // 保留 content 字段以备后用，但小程序应优先使用独立字段
        result.put("content", conference.getContent());
        return result;
    }
}

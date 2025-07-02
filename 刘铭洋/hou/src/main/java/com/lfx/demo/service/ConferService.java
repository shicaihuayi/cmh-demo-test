package com.lfx.demo.service;

import com.lfx.demo.entity.Conference;
import com.lfx.demo.mapper.ConferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConferService {
    @Autowired
    private ConferMapper mapper;

    public List<Conference> getConferList(){
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        return mapper.selectConfers();
    }

    /**
     * 获取待审核的会议列表
     * @return 待审核会议列表
     */
    public List<Conference> getAuditConferences(){
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        return mapper.selectAuditConferences();
    }

    public List<Conference>  getConfer(String conferName,String creater,String stime,String content){
        mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
        return mapper.selectConfer(conferName,creater,stime,content);
    }

    public boolean addConfer(Conference confer){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.insertConfer(confer)>0;
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

    public boolean removeConfer(List<String> conferName){
        return mapper.deleteConferByName(conferName)>0;
    }

    public boolean modifyConfer(Conference confer){
        try {
            mapper.callUpdateConferenceStatus();//调用触发器自动修改状态
            return mapper.updateConfer(confer)>0;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

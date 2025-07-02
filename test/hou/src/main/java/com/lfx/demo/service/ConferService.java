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

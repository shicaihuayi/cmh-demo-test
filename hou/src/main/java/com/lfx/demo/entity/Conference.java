package com.lfx.demo.entity;

import lombok.Getter;
import java.io.Serializable;

@Getter
public class Conference implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String oldName;
    private String conferName;
    private String creater;
    private String conferState;
    private String content;
    private String stime;
    private String etime;
    private String picture;
    private String auditStatus;
    
    Conference(){

    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public void setConferState(String conferState) {
        this.conferState = conferState;
    }

    public void setConferName(String conferName) {
        this.conferName = conferName;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    @Override
    public String toString() {
        return "Conference{" +
                "id=" + id +
                ", oldName='" + oldName + '\'' +
                ", conferName='" + conferName + '\'' +
                ", creater='" + creater + '\'' +
                ", conferState='" + conferState + '\'' +
                ", content='" + content + '\'' +
                ", stime='" + stime + '\'' +
                ", etime='" + etime + '\'' +
                ", picture='" + picture + '\'' +
                ", auditStatus='" + auditStatus + '\'' +
                '}';
    }
}

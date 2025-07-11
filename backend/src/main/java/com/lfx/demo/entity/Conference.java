package com.lfx.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;

@TableName("t_conference")
public class Conference implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String oldName;
    private String conferName;
    private String creater;
    private String publisher;
    private String conferState;
    @TableField(value = "content", jdbcType = JdbcType.LONGVARCHAR)
    private String content;
    private String stime;
    private String etime;
    private String picture;
    private String auditStatus;
    private String category;
    private String location;
    private String organizer;
    @TableField(value = "agenda", jdbcType = JdbcType.LONGVARCHAR)
    private String agenda;
    @TableField(value = "guests", jdbcType = JdbcType.LONGVARCHAR)
    private String guests;
    
    public Conference(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getConferName() {
        return conferName;
    }

    public void setConferName(String conferName) {
        this.conferName = conferName;
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getConferState() {
        return conferState;
    }

    public void setConferState(String conferState) {
        this.conferState = conferState;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public String getGuests() {
        return guests;
    }

    public void setGuests(String guests) {
        this.guests = guests;
    }

    @Override
    public String toString() {
        return "Conference{" +
                "id=" + id +
                ", oldName='" + oldName + '\'' +
                ", conferName='" + conferName + '\'' +
                ", creater='" + creater + '\'' +
                ", publisher='" + publisher + '\'' +
                ", conferState='" + conferState + '\'' +
                ", content='" + content + '\'' +
                ", stime='" + stime + '\'' +
                ", etime='" + etime + '\'' +
                ", picture='" + picture + '\'' +
                ", auditStatus='" + auditStatus + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", organizer='" + organizer + '\'' +
                ", agenda='" + agenda + '\'' +
                ", guests='" + guests + '\'' +
                '}';
    }
}

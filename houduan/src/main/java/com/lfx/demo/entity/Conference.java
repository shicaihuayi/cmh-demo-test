package com.lfx.demo.entity;
import lombok.Getter;

@Getter
public class Conference {
    private String oldName;
    private String conferName;
    private String creater;
    private String conferState;
    private String content;
    private String stime;
    private String etime;
    private String picture;
    Conference(){

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
}

package com.lfx.demo.entity;

import java.io.Serializable;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String name;
    private String coverUrl;
    private String introduction;
    private String author;
    private Integer authorId;
    private String videoUrl;
    private Integer courseOrder;
    private String pass;
    private String status;
    
    // 新增：创建者相关字段
    private String creator;        // 创建者用户名
    private Integer creator_id;    // 创建者ID
    private String department;     // 创建者部门
    private String creator_role;   // 创建者角色

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", introduction='" + introduction + '\'' +
                ", author='" + author + '\'' +
                ", authorId=" + authorId +
                ", videoUrl='" + videoUrl + '\'' +
                ", courseOrder=" + courseOrder +
                ", pass='" + pass + '\'' +
                ", status='" + status + '\'' +
                ", creator='" + creator + '\'' +
                ", creator_id=" + creator_id +
                ", department='" + department + '\'' +
                ", creator_role='" + creator_role + '\'' +
                '}';
    }

    public Course() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getCourseOrder() {
        return courseOrder;
    }

    public void setCourseOrder(Integer courseOrder) {
        this.courseOrder = courseOrder;
    }

    // 新增：创建者相关字段的getter和setter方法
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Integer creator_id) {
        this.creator_id = creator_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCreator_role() {
        return creator_role;
    }

    public void setCreator_role(String creator_role) {
        this.creator_role = creator_role;
    }
}

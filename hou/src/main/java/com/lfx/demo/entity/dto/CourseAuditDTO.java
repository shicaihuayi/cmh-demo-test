package com.lfx.demo.entity.dto;

/**
 * 课程审核请求DTO
 */
public class CourseAuditDTO {
    private Integer courseId;
    private String courseName; // 课程名称
    private String status; // "通过" 或 "不通过" (对应前端的status字段)
    private String remark; // 审核备注/原因 (对应前端的remark字段)

    public CourseAuditDTO() {
    }

    public CourseAuditDTO(Integer courseId, String status) {
        this.courseId = courseId;
        this.status = status;
    }

    public CourseAuditDTO(Integer courseId, String status, String remark) {
        this.courseId = courseId;
        this.status = status;
        this.remark = remark;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "CourseAuditDTO{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
} 
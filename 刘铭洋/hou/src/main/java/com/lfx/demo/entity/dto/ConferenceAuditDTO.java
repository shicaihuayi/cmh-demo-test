package com.lfx.demo.entity.dto;

/**
 * 会议审核请求DTO
 */
public class ConferenceAuditDTO {
    private String conferName; // 会议名称
    private String status; // 审核状态："通过" 或 "不通过"
    private String remark; // 审核备注/原因（可选）

    public ConferenceAuditDTO() {
    }

    public ConferenceAuditDTO(String conferName, String status) {
        this.conferName = conferName;
        this.status = status;
    }

    public ConferenceAuditDTO(String conferName, String status, String remark) {
        this.conferName = conferName;
        this.status = status;
        this.remark = remark;
    }

    public String getConferName() {
        return conferName;
    }

    public void setConferName(String conferName) {
        this.conferName = conferName;
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
        return "ConferenceAuditDTO{" +
                "conferName='" + conferName + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
} 
package com.lfx.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 会议回执实体类
 */
@Data
@TableName("conference_receipt")
public class ConferenceReceipt {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 会议名称
     */
    @TableField("conference_name")
    private String conferenceName;
    
    /**
     * 用户ID（如果用户已登录）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 单位名称
     */
    @TableField("company")
    private String company;
    
    /**
     * 参会者姓名
     */
    @TableField("name")
    private String name;
    
    /**
     * 性别（男/女）
     */
    @TableField("gender")
    private String gender;
    
    /**
     * 手机号码
     */
    @TableField("phone")
    private String phone;
    
    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 到达方式（飞机/火车/汽车/其他）
     */
    @TableField("arrival_method")
    private String arrivalMethod;
    
    /**
     * 到达车次/航班号
     */
    @TableField("arrival_flight_or_train")
    private String arrivalFlightOrTrain;
    
    /**
     * 到达时间
     */
    @TableField("arrival_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime arrivalTime;
    
    /**
     * 提交时间
     */
    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    /**
     * 状态（submitted-已提交，confirmed-已确认，cancelled-已取消）
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 
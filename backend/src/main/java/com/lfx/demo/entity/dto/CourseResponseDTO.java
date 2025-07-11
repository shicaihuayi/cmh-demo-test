package com.lfx.demo.entity.dto;

import lombok.Data;

/**
 * 课程响应数据传输对象
 * 用于将课程数据安全地发送到前端
 */
@Data
public class CourseResponseDTO {
    private Integer id;
    private String name;
    private String coverUrl;
    private String introduction;
    private String author;
    private String authorId; // 关键变更：将 authorId 定义为字符串类型，以兼容前端
    private String videoUrl;
    private Integer courseOrder;
    private String pass;
    private String status;
} 
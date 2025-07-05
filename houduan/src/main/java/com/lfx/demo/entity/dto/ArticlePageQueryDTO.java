package com.lfx.demo.entity.dto;
import lombok.Data;

@Data
public class ArticlePageQueryDTO {
    private String title;
    private String description;
    private String authorName;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

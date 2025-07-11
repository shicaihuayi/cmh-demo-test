package com.lfx.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

@Getter
@TableName("article")
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String content;
    private String keywords;
    private String authorName;
    private Long authorId;
    private String fileUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private ArticleStatus status;

    public Article() {}

    // 普通字段的 setter 方法
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }

    // 作者信息的 setter 方法，仅供内部使用
    protected void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    protected void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    // 提供一个公共方法来设置作者信息
    public void setAuthorInfo(String authorName, Long authorId) {
        this.authorName = authorName;
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", keywords='" + keywords + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorId=" + authorId +
                ", fileUrl='" + fileUrl + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", status=" + status +
                '}';
    }
}

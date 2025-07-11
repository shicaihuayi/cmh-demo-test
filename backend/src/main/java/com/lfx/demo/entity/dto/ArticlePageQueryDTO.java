package com.lfx.demo.entity.dto;

import com.lfx.demo.web.page.PageDomain;
import lombok.Data;

@Data
public class ArticlePageQueryDTO extends PageDomain {
    private String title;
    private String description;
    private String status;
    
    // 为了兼容不同的分页参数名称，添加对应的setter方法
    
    public void setPage(Integer page) {
        super.page = page;
        if (super.pageNum == null) {
            super.pageNum = page;
        }
    }
    
    public void setCurrent(Integer current) {
        super.current = current;
        if (super.pageNum == null && super.page == null) {
            super.pageNum = current;
        }
    }
    
    public void setSize(Integer size) {
        super.size = size;
        if (super.pageSize == null) {
            super.pageSize = size;
        }
    }
    
    public void setLimit(Integer limit) {
        super.limit = limit;
        if (super.pageSize == null && super.size == null) {
            super.pageSize = limit;
        }
    }
}

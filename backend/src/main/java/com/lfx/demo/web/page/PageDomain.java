package com.lfx.demo.web.page;

import lombok.Setter;

/**
 * 分页数据
 * 支持多种分页参数名称以兼容不同前端实现
 *
 * @author dream
 */
@Setter
public class PageDomain {
    /** 当前记录起始索引 - 支持多种参数名 */
    protected Integer pageNum;
    protected Integer page;
    protected Integer current;
    
    /** 每页显示记录数 - 支持多种参数名 */
    protected Integer pageSize;
    protected Integer size;
    protected Integer limit;

    /**
     * 获取页码，支持多种参数名称
     * 优先级：pageNum > page > current
     */
    public Integer getPageNum() {
        Integer finalPageNum = null;
        
        if (pageNum != null) {
            finalPageNum = pageNum;
        } else if (page != null) {
            finalPageNum = page;
        } else if (current != null) {
            finalPageNum = current;
        }
        
        // 默认值为1，且确保不小于1
        return finalPageNum == null ? 1 : Math.max(finalPageNum, 1);
    }

    /**
     * 获取每页大小，支持多种参数名称
     * 优先级：pageSize > size > limit
     */
    public Integer getPageSize() {
        Integer finalPageSize = null;
        
        if (pageSize != null) {
            finalPageSize = pageSize;
        } else if (size != null) {
            finalPageSize = size;
        } else if (limit != null) {
            finalPageSize = limit;
        }
        
        // 默认值为10，且确保在合理范围内
        if (finalPageSize == null) {
            return 10;
        }
        
        // 限制每页最小1条，最大100条
        return Math.max(1, Math.min(finalPageSize, 100));
    }
    
    /**
     * 设置页码的便捷方法，同时设置所有可能的参数
     */
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        this.page = pageNum;
        this.current = pageNum;
    }
    
    /**
     * 设置每页大小的便捷方法，同时设置所有可能的参数
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        this.size = pageSize;
        this.limit = pageSize;
    }
    
    /**
     * 获取分页调试信息
     */
    public String getDebugInfo() {
        return String.format("分页参数 - pageNum:%d, page:%d, current:%d, pageSize:%d, size:%d, limit:%d, 最终页码:%d, 最终大小:%d",
                pageNum, page, current, pageSize, size, limit, getPageNum(), getPageSize());
    }
}

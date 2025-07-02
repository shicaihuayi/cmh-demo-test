package com.lfx.demo.controller;

import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import com.lfx.demo.web.domain.AjaxResult;
import com.lfx.demo.web.page.PageDomain;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页测试控制器
 * 用于测试分页参数的各种组合是否正常工作
 */
@RestController
@RequestMapping("/pagination-test")
public class PaginationTestController {

    /**
     * 测试基础分页参数解析
     */
    @GetMapping("/basic")
    public AjaxResult testBasicPagination(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(pageNum);
        pageDomain.setPageSize(pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("输入参数", Map.of("pageNum", pageNum, "pageSize", pageSize));
        result.put("解析结果", Map.of("页码", pageDomain.getPageNum(), "每页大小", pageDomain.getPageSize()));
        result.put("调试信息", pageDomain.getDebugInfo());
        
        return AjaxResult.success(result);
    }

    /**
     * 测试多种分页参数名称
     */
    @GetMapping("/multi-names")
    public AjaxResult testMultipleParameterNames(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "limit", required = false) Integer limit) {
        
        ArticlePageQueryDTO queryDTO = new ArticlePageQueryDTO();
        
        // 设置各种参数名称
        if (pageNum != null) queryDTO.setPageNum(pageNum);
        if (page != null) queryDTO.setPage(page);
        if (current != null) queryDTO.setCurrent(current);
        if (pageSize != null) queryDTO.setPageSize(pageSize);
        if (size != null) queryDTO.setSize(size);
        if (limit != null) queryDTO.setLimit(limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("输入参数", Map.of(
                "pageNum", pageNum, "page", page, "current", current,
                "pageSize", pageSize, "size", size, "limit", limit));
        result.put("解析结果", Map.of("最终页码", queryDTO.getPageNum(), "最终每页大小", queryDTO.getPageSize()));
        result.put("调试信息", queryDTO.getDebugInfo());
        
        return AjaxResult.success(result);
    }

    /**
     * 测试POST请求的分页参数
     */
    @PostMapping("/post-test")
    public AjaxResult testPostPagination(@RequestBody Map<String, Object> requestData) {
        ArticlePageQueryDTO queryDTO = new ArticlePageQueryDTO();
        
        // 从请求体中提取分页参数
        if (requestData.containsKey("pageNum")) {
            queryDTO.setPageNum(((Number) requestData.get("pageNum")).intValue());
        }
        if (requestData.containsKey("page")) {
            queryDTO.setPage(((Number) requestData.get("page")).intValue());
        }
        if (requestData.containsKey("current")) {
            queryDTO.setCurrent(((Number) requestData.get("current")).intValue());
        }
        if (requestData.containsKey("pageSize")) {
            queryDTO.setPageSize(((Number) requestData.get("pageSize")).intValue());
        }
        if (requestData.containsKey("size")) {
            queryDTO.setSize(((Number) requestData.get("size")).intValue());
        }
        if (requestData.containsKey("limit")) {
            queryDTO.setLimit(((Number) requestData.get("limit")).intValue());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("输入请求体", requestData);
        result.put("解析结果", Map.of("最终页码", queryDTO.getPageNum(), "最终每页大小", queryDTO.getPageSize()));
        result.put("调试信息", queryDTO.getDebugInfo());
        
        return AjaxResult.success(result);
    }

    /**
     * 测试边界情况
     */
    @GetMapping("/boundary")
    public AjaxResult testBoundaryValues(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(pageNum);
        pageDomain.setPageSize(pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("输入参数", Map.of("pageNum", pageNum, "pageSize", pageSize));
        result.put("解析结果", Map.of("页码", pageDomain.getPageNum(), "每页大小", pageDomain.getPageSize()));
        result.put("边界处理说明", "页码最小为1，每页大小在1-100之间");
        result.put("调试信息", pageDomain.getDebugInfo());
        
        return AjaxResult.success(result);
    }
} 
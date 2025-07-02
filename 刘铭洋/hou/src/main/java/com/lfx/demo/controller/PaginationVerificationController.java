package com.lfx.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import com.lfx.demo.service.IArticleService;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页功能验证控制器
 * 专门用于验证分页插件是否正确工作
 */
@RestController
@RequestMapping("/pagination-verify")
public class PaginationVerificationController {

    @Autowired
    private IArticleService articleService;

    /**
     * 验证第一页分页功能
     */
    @GetMapping("/page1")
    public AjaxResult verifyPage1(@RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize,
                                 HttpServletRequest request) {
        System.out.println("\n🔍 验证第一页分页功能 (pageSize=" + pageSize + ")");
        
        ArticlePageQueryDTO queryDTO = new ArticlePageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(pageSize);
        
        IPage<Article> result = articleService.pageList(queryDTO, request);
        
        Map<String, Object> verification = new HashMap<>();
        verification.put("请求页码", 1);
        verification.put("请求每页大小", pageSize);
        verification.put("返回记录数", result.getRecords().size());
        verification.put("总记录数", result.getTotal());
        verification.put("总页数", result.getPages());
        
        boolean isPageWorking = result.getRecords().size() <= pageSize;
        verification.put("分页是否生效", isPageWorking);
        verification.put("验证说明", isPageWorking ? 
            "✅ 分页正常：返回记录数 <= 每页大小" : 
            "❌ 分页异常：返回了超过每页大小的记录数");
        
        return AjaxResult.success(verification);
    }

    /**
     * 验证第二页分页功能
     */
    @GetMapping("/page2")
    public AjaxResult verifyPage2(@RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize,
                                 HttpServletRequest request) {
        System.out.println("\n🔍 验证第二页分页功能 (pageSize=" + pageSize + ")");
        
        ArticlePageQueryDTO queryDTO = new ArticlePageQueryDTO();
        queryDTO.setPageNum(2);
        queryDTO.setPageSize(pageSize);
        
        IPage<Article> result = articleService.pageList(queryDTO, request);
        
        Map<String, Object> verification = new HashMap<>();
        verification.put("请求页码", 2);
        verification.put("请求每页大小", pageSize);
        verification.put("返回记录数", result.getRecords().size());
        verification.put("总记录数", result.getTotal());
        verification.put("总页数", result.getPages());
        
        boolean hasEnoughRecords = result.getTotal() > pageSize;
        boolean isPageWorking = hasEnoughRecords ? 
            (result.getRecords().size() <= pageSize && result.getRecords().size() < result.getTotal()) :
            result.getRecords().size() == 0;
            
        verification.put("分页是否生效", isPageWorking);
        verification.put("验证说明", 
            !hasEnoughRecords ? "⚠️ 总记录数不足以验证第二页" :
            isPageWorking ? "✅ 分页正常：第二页返回了不同的记录" : 
            "❌ 分页异常：第二页返回了相同的记录");
        
        return AjaxResult.success(verification);
    }

    /**
     * 对比第一页和第二页的数据
     */
    @GetMapping("/compare-pages")
    public AjaxResult comparePages(@RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize,
                                 HttpServletRequest request) {
        System.out.println("\n🔍 对比第一页和第二页数据");
        
        // 获取第一页
        ArticlePageQueryDTO queryDTO1 = new ArticlePageQueryDTO();
        queryDTO1.setPageNum(1);
        queryDTO1.setPageSize(pageSize);
        IPage<Article> page1Result = articleService.pageList(queryDTO1, request);
        
        // 获取第二页
        ArticlePageQueryDTO queryDTO2 = new ArticlePageQueryDTO();
        queryDTO2.setPageNum(2);
        queryDTO2.setPageSize(pageSize);
        IPage<Article> page2Result = articleService.pageList(queryDTO2, request);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("第一页记录数", page1Result.getRecords().size());
        comparison.put("第二页记录数", page2Result.getRecords().size());
        comparison.put("总记录数", page1Result.getTotal());
        
        // 检查是否有重复的记录ID
        boolean hasDuplicates = false;
        if (!page1Result.getRecords().isEmpty() && !page2Result.getRecords().isEmpty()) {
            for (Article article1 : page1Result.getRecords()) {
                for (Article article2 : page2Result.getRecords()) {
                    if (article1.getId().equals(article2.getId())) {
                        hasDuplicates = true;
                        break;
                    }
                }
                if (hasDuplicates) break;
            }
        }
        
        comparison.put("是否有重复记录", hasDuplicates);
        comparison.put("分页状态", !hasDuplicates ? "✅ 正常：两页数据不重复" : "❌ 异常：两页数据有重复");
        
        // 添加记录ID用于调试
        if (!page1Result.getRecords().isEmpty()) {
            comparison.put("第一页ID列表", page1Result.getRecords().stream()
                .map(Article::getId).toArray());
        }
        if (!page2Result.getRecords().isEmpty()) {
            comparison.put("第二页ID列表", page2Result.getRecords().stream()
                .map(Article::getId).toArray());
        }
        
        return AjaxResult.success(comparison);
    }

    /**
     * 分页功能完整测试
     */
    @GetMapping("/full-test")
    public AjaxResult fullPaginationTest(HttpServletRequest request) {
        System.out.println("\n🧪 执行完整分页功能测试");
        
        Map<String, Object> testResults = new HashMap<>();
        
        // 测试1：小分页大小
        ArticlePageQueryDTO queryDTO1 = new ArticlePageQueryDTO();
        queryDTO1.setPageNum(1);
        queryDTO1.setPageSize(2);
        IPage<Article> result1 = articleService.pageList(queryDTO1, request);
        
        testResults.put("测试1_每页2条_返回记录数", result1.getRecords().size());
        testResults.put("测试1_分页是否生效", result1.getRecords().size() <= 2);
        
        // 测试2：检查总记录数是否一致
        ArticlePageQueryDTO queryDTO2 = new ArticlePageQueryDTO();
        queryDTO2.setPageNum(1);
        queryDTO2.setPageSize(10);
        IPage<Article> result2 = articleService.pageList(queryDTO2, request);
        
        testResults.put("测试2_每页10条_返回记录数", result2.getRecords().size());
        testResults.put("测试2_总记录数", result2.getTotal());
        testResults.put("测试2_总记录数是否一致", Long.valueOf(result1.getTotal()).equals(Long.valueOf(result2.getTotal())));
        
        // 综合评估
        boolean isWorking = (result1.getRecords().size() <= 2) && 
                           (Long.valueOf(result1.getTotal()).equals(Long.valueOf(result2.getTotal()))) &&
                           (result1.getTotal() > 0);
        
        testResults.put("分页插件状态", isWorking ? "✅ 正常工作" : "❌ 工作异常");
        testResults.put("建议", isWorking ? 
            "分页功能正常，可以正常使用" : 
            "请检查MyBatis-Plus分页插件配置是否正确");
        
        return AjaxResult.success(testResults);
    }
} 
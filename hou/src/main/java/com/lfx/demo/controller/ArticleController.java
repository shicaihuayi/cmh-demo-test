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

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private IArticleService articleService;

    @PostMapping("/add")
    public AjaxResult add(@RequestBody Article article, HttpServletRequest request) {
        Article newArticle = articleService.addArticle(article, request);
        return AjaxResult.success("添加成功", newArticle);
    }

    @PutMapping("/update")
    public AjaxResult update(@RequestBody Article article, HttpServletRequest request) {
        articleService.updateArticle(article, request);
        return AjaxResult.success("编辑成功");
    }

    @PostMapping("/update")
    public AjaxResult updatePost(@RequestBody Article article, HttpServletRequest request) {
        articleService.updateArticle(article, request);
        return AjaxResult.success("编辑成功");
    }

    @DeleteMapping("/delete")
    public AjaxResult delete(@RequestBody Article article, HttpServletRequest request) {
        articleService.deleteArticle(article, request);
        return AjaxResult.success("删除成功");
    }

    @PostMapping("/delete")
    public AjaxResult deletePost(@RequestBody Article article, HttpServletRequest request) {
        articleService.deleteArticle(article, request);
        return AjaxResult.success("删除成功");
    }

    @GetMapping("/delete")
    public AjaxResult deleteGet(@RequestParam("id") Long id, HttpServletRequest request) {
        Article article = new Article();
        article.setId(id);
        articleService.deleteArticle(article, request);
        return AjaxResult.success("删除成功");
    }

    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam("id") Long id) {
        Article article = articleService.getArticleById(id);
        return AjaxResult.success(article);
    }

    @GetMapping("/list")
    public AjaxResult list(ArticlePageQueryDTO queryDTO, HttpServletRequest request) {
        // 输出分页调试信息
        System.out.println("=== 文章列表分页请求 ===");
        System.out.println(queryDTO.getDebugInfo());
        System.out.println("查询条件 - 标题: " + queryDTO.getTitle() + ", 描述: " + queryDTO.getDescription() + ", 状态: " + queryDTO.getStatus());
        
        IPage<Article> pageResult = articleService.pageList(queryDTO, request);
        
        // 输出分页结果信息
        System.out.println("=== 分页查询结果 ===");
        System.out.println("当前页: " + pageResult.getCurrent());
        System.out.println("每页大小: " + pageResult.getSize());
        System.out.println("总记录数: " + pageResult.getTotal());
        System.out.println("总页数: " + pageResult.getPages());
        System.out.println("当前页记录数: " + pageResult.getRecords().size());
        
        return AjaxResult.success(pageResult);
    }
    
    @GetMapping("/pageList")
    public AjaxResult pageList(ArticlePageQueryDTO queryDTO, HttpServletRequest request) {
        return list(queryDTO, request);
    }
    
    @PostMapping("/list")
    public AjaxResult listPost(@RequestBody ArticlePageQueryDTO queryDTO, HttpServletRequest request) {
        return list(queryDTO, request);
    }

    @RequestMapping(value = "/pageList", method = {RequestMethod.GET, RequestMethod.POST})
    public AjaxResult pageListGeneric(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestBody(required = false) ArticlePageQueryDTO bodyDTO,
            HttpServletRequest request) {
        
        ArticlePageQueryDTO queryDTO = new ArticlePageQueryDTO();
        
        // 设置页码，优先级：bodyDTO > pageNum > page > current
        if (bodyDTO != null && bodyDTO.getPageNum() != null) {
            queryDTO.setPageNum(bodyDTO.getPageNum());
        } else if (pageNum != null) {
            queryDTO.setPageNum(pageNum);
        } else if (page != null) {
            queryDTO.setPage(page);
        } else if (current != null) {
            queryDTO.setCurrent(current);
        }
        
        // 设置每页大小，优先级：bodyDTO > pageSize > size > limit
        if (bodyDTO != null && bodyDTO.getPageSize() != null) {
            queryDTO.setPageSize(bodyDTO.getPageSize());
        } else if (pageSize != null) {
            queryDTO.setPageSize(pageSize);
        } else if (size != null) {
            queryDTO.setSize(size);
        } else if (limit != null) {
            queryDTO.setLimit(limit);
        }
        
        // 设置查询条件，优先级：bodyDTO > 请求参数
        if (bodyDTO != null) {
            queryDTO.setTitle(bodyDTO.getTitle());
            queryDTO.setDescription(bodyDTO.getDescription());
            queryDTO.setStatus(bodyDTO.getStatus());
        } else {
            queryDTO.setTitle(title);
            queryDTO.setDescription(description);
            queryDTO.setStatus(status);
        }
        
        IPage<Article> pageResult = articleService.pageList(queryDTO, request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        result.put("pages", pageResult.getPages());
        
        return AjaxResult.success(result);
    }
    
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id, HttpServletRequest request) {
        articleService.approveArticle(id, request);
        return AjaxResult.success("审核通过");
    }

    @PostMapping("/reject/{id}")
    public AjaxResult reject(@PathVariable Long id, HttpServletRequest request) {
        articleService.rejectArticle(id, request);
        return AjaxResult.success("审核不通过");
    }

    @PostMapping("/submit/{id}")
    public AjaxResult submitForReview(@PathVariable Long id, HttpServletRequest request) {
        articleService.submitForReview(id, request);
        return AjaxResult.success("提交审核成功");
    }
    
    // 添加兼容路径
    @PostMapping("/submitForReview")
    public AjaxResult submitForReviewCompat(@RequestParam("id") Long id, HttpServletRequest request) {
        articleService.submitForReview(id, request);
        return AjaxResult.success("提交审核成功");
    }
    
    @RequestMapping(value = "/submitForReview", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult submitForReviewGeneric(@RequestParam("id") Long id, HttpServletRequest request) {
        articleService.submitForReview(id, request);
        return AjaxResult.success("提交审核成功");
    }

    /**
     * 为前端 adminUpdateNews 请求添加的兼容接口
     */
    @PostMapping("/adminUpdate")
    public AjaxResult adminUpdate(@RequestBody Article article, HttpServletRequest request) {
        // 复用现有的更新逻辑
        articleService.updateArticle(article, request);
        return AjaxResult.success("编辑成功");
    }
}


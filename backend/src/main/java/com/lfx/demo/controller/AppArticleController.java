package com.lfx.demo.controller;

import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.ArticleStatus;
import com.lfx.demo.service.ArticleServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app/article")
public class AppArticleController {
    private static final Logger logger = LoggerFactory.getLogger(AppArticleController.class);
    
    @Autowired
    private ArticleServiceImpl articleService;

    /**
     * 获取已审核通过的行业动态列表（小程序版本）
     */
    @RequestMapping("/list")
    public Map<String, Object> getApprovedArticles(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        logger.info("(/app/article/list) [小程序] 获取行业动态列表 -> 页码: {}, 每页数量: {}", page, size);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 只获取已审核通过的文章（status=PUBLISHED表示已审核通过）
            List<Article> allApprovedArticles = articleService.list().stream()
                    .filter(article -> article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED)
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allApprovedArticles.size());
            List<Article> pagedArticles;
            if (start < allApprovedArticles.size()) {
                pagedArticles = allApprovedArticles.subList(start, end);
            } else {
                pagedArticles = List.of();
            }
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> articleList = pagedArticles.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("articles", articleList);
            map.put("total", allApprovedArticles.size());
            map.put("currentPage", page);
            map.put("pageSize", size);
            
        } catch (Exception e) {
            logger.error("获取行业动态列表失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 根据ID获取行业动态详情（小程序版本）
     */
    @RequestMapping("/detail")
    public Map<String, Object> getArticleDetail(@RequestParam Long id) {
        logger.info("(/app/article/detail) [小程序] 获取行业动态详情 -> ID: {}", id);
        
        Map<String, Object> map = new HashMap<>();
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：文章ID不能为空");
            return map;
        }
        
        try {
            Article article = articleService.getById(id);
            if (article != null && article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED) {
                // 只返回已审核通过的文章
                Map<String, Object> articleDetail = convertToAppDetailFormat(article);
                map.put("isOk", true);
                map.put("msg", "获取成功");
                map.put("article", articleDetail);
            } else {
                map.put("isOk", false);
                map.put("msg", "文章不存在或未审核通过");
            }
        } catch (Exception e) {
            logger.error("获取行业动态详情失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 搜索行业动态（小程序版本）
     */
    @RequestMapping("/search")
    public Map<String, Object> searchArticles(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        logger.info("(/app/article/search) [小程序] 搜索行业动态 -> 关键词: {}", keyword);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 只在已审核通过的文章中搜索
            List<Article> allApprovedArticles = articleService.list().stream()
                    .filter(article -> article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED)
                    .filter(article -> 
                        (article.getTitle() != null && article.getTitle().contains(keyword)) ||
                        (article.getDescription() != null && article.getDescription().contains(keyword))
                    )
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allApprovedArticles.size());
            List<Article> pagedArticles;
            if (start < allApprovedArticles.size()) {
                pagedArticles = allApprovedArticles.subList(start, end);
            } else {
                pagedArticles = List.of();
            }
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> articleList = pagedArticles.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "搜索成功");
            map.put("articles", articleList);
            map.put("total", allApprovedArticles.size());
            map.put("keyword", keyword);
            
        } catch (Exception e) {
            logger.error("搜索行业动态失败", e);
            map.put("isOk", false);
            map.put("msg", "搜索失败");
        }
        return map;
    }

    /**
     * 获取热门行业动态（小程序版本）
     */
    @RequestMapping("/hot")
    public Map<String, Object> getHotArticles(@RequestParam(defaultValue = "5") int limit) {
        logger.info("(/app/article/hot) [小程序] 获取热门行业动态 -> 数量限制: {}", limit);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 获取已审核通过的文章，按某种热度排序（这里简单按ID倒序）
            List<Article> hotArticles = articleService.list().stream()
                    .filter(article -> article.getStatus() != null && article.getStatus() == ArticleStatus.PUBLISHED)
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // 转换为小程序需要的格式
            List<Map<String, Object>> articleList = hotArticles.stream()
                    .map(this::convertToAppFormat)
                    .collect(Collectors.toList());
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("articles", articleList);
            
        } catch (Exception e) {
            logger.error("获取热门行业动态失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 将Article对象转换为小程序列表展示格式
     */
    private Map<String, Object> convertToAppFormat(Article article) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getTitle());
        result.put("author", article.getAuthorName());
        result.put("publishTime", article.getCreateTime());
        result.put("imageUrl", article.getFileUrl());
        result.put("description", article.getDescription());
        // 内容摘要（截取前100个字符）
        String content = article.getContent();
        if (content != null && content.length() > 100) {
            result.put("summary", content.substring(0, 100) + "...");
        } else {
            result.put("summary", content);
        }
        return result;
    }

    /**
     * 将Article对象转换为小程序详情展示格式
     */
    private Map<String, Object> convertToAppDetailFormat(Article article) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getTitle());
        result.put("content", article.getContent());
        result.put("author", article.getAuthorName());
        result.put("publishTime", article.getCreateTime());
        result.put("imageUrl", article.getFileUrl());
        result.put("description", article.getDescription());
        result.put("keywords", article.getKeywords());
        return result;
    }
} 
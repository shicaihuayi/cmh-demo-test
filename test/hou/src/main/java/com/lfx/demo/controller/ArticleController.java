package com.lfx.demo.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import com.lfx.demo.service.IArticleService;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private IArticleService articleService;

    @PostMapping("/add")
    public AjaxResult add(@RequestBody Article article, HttpServletRequest request) {
        articleService.addArticle(article, request);
        return AjaxResult.success("添加成功");
    }


    @PostMapping("/update")
    public AjaxResult update(@RequestBody Article article, HttpServletRequest request) {
        articleService.updateArticle(article, request);
        return AjaxResult.success("编辑成功");
    }

    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody Article article, HttpServletRequest request) {
        articleService.deleteArticle(article, request);
        return AjaxResult.success("删除成功");
    }

    @GetMapping("/detail")
    public AjaxResult delete(@RequestParam("id") Long id) {
        Article article = articleService.getArticleById(id);

        return AjaxResult.success(article);
    }


    @PostMapping("/pageList")
    // 分页查询
    public AjaxResult pageList(@RequestBody ArticlePageQueryDTO queryDTO){
        IPage<Article> pageResult = articleService.pageList(queryDTO);
        return AjaxResult.success(pageResult);
    }


}

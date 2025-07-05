package com.lfx.demo.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import jakarta.servlet.http.HttpServletRequest;


/**
 * 用户 业务层
 *
 * @author dream
 */
public interface IArticleService extends IService<Article> {

    Article getArticleById(Long id);
    void addArticle(Article article, HttpServletRequest request);

    void updateArticle(Article article, HttpServletRequest request);

    void deleteArticle(Article article, HttpServletRequest request);

    IPage<Article> pageList(ArticlePageQueryDTO queryDTO);

}

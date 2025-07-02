package com.lfx.demo.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import com.lfx.demo.mapper.ArticleMapper;
import com.lfx.demo.util.BaseException;
import com.lfx.demo.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 * @author dream
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {


    // 查看文章详情
    @Override
    public Article getArticleById(Long id) {
        return this.baseMapper.selectById(id);
    }


    // 新增文章
    @Override
    public void addArticle(Article article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String)request.getAttribute("username");
        article.setAuthorId(userId);
        article.setAuthorName(username);
        this.baseMapper.insert(article);
    }

    // 编辑文章
    @Override
    public void updateArticle(Article article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String)request.getAttribute("username");
        String role = (String)request.getAttribute("role");
        // 先查出来
        Article existArticle = this.getArticleById(article.getId());
        if(existArticle == null){
            throw new BaseException("文章不存在");
        }

        this.checkEditPermission(role, userId, existArticle.getAuthorId());
        existArticle.setTitle(article.getTitle());
        existArticle.setDescription(article.getDescription());
        existArticle.setContent(article.getContent());
        existArticle.setKeywords(article.getKeywords());
        existArticle.setFileUrl(article.getFileUrl());

        this.baseMapper.updateById(article);
    }


    // 删除文章
    @Override
    public void deleteArticle(Article article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String)request.getAttribute("username");
        String role = (String)request.getAttribute("role");
        // 先查出来
        Article existArticle = this.getArticleById(article.getId());
        if(existArticle == null){
            throw new BaseException("文章不存在");
        }
        this.checkEditPermission(role, userId, existArticle.getAuthorId());
        this.baseMapper.deleteById(article);
    }


    // 分页获取文章
    @Override
    public IPage<Article> pageList(ArticlePageQueryDTO queryDTO) {

        // 创建分页参数
        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询Wrapper，你可以根据需要设置查询条件
         QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
         queryWrapper.like(StringUtils.isNotEmpty(queryDTO.getTitle()),"title", queryDTO.getTitle());
         queryWrapper.like(StringUtils.isNotEmpty(queryDTO.getDescription()),"description", queryDTO.getDescription());
         queryWrapper.like(StringUtils.isNotEmpty(queryDTO.getAuthorName()),"author_name", queryDTO.getAuthorName());

        // 调用selectPage方法执行分页查询
        // 如果不需要额外的查询条件，直接传null
        IPage<Article> result = this.baseMapper.selectPage(page, queryWrapper);
        return result;
    }




    private void checkEditPermission(String loginRole, Long loginUserId, Long authorId){
        if("normal".equals(loginRole)){
            throw new BaseException("普通用户无权操作文章");
        }

        if("enterprise".equals(loginRole) && !Objects.equals(authorId, loginUserId)){
            throw new BaseException("企业租户无权操作他人发布的文章");
        }
    }

}

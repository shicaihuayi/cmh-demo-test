package com.lfx.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lfx.demo.entity.Article;
import com.lfx.demo.entity.ArticleStatus;
import com.lfx.demo.entity.User;
import com.lfx.demo.entity.dto.ArticlePageQueryDTO;
import com.lfx.demo.mapper.ArticleMapper;
import com.lfx.demo.util.AuthUtils;
import com.lfx.demo.util.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * 文章服务实现类
 * @author dream
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    // 查看文章详情
    @Override
    public Article getArticleById(Long id) {
        return this.getById(id);
    }

    // 新增文章
    @Override
    public Article addArticle(Article article, HttpServletRequest request) {
        log.info("=== 开始保存文章 ===");
        log.info("接收到的原始文章数据: {}", article);
        
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        log.info("当前登录用户: id={}, name={}, role={}", 
            user.getId(), user.getName(), user.getRole());
        
        // 设置时间
        Date now = new Date();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        
        // 强制根据用户角色设置状态
        if (AuthUtils.isSuperAdmin(user)) {
            article.setStatus(ArticleStatus.PUBLISHED); // 超级管理员直接发布
            log.info("超级管理员发布文章，状态设置为: {}", ArticleStatus.PUBLISHED);
        } else {
            article.setStatus(ArticleStatus.PENDING); // 其他用户创建后为待审核状态
            log.info("普通用户发布文章，状态设置为: {}", ArticleStatus.PENDING);
        }
        
        // 设置作者ID为当前用户ID（用于权限控制），发布者为当前用户名
        article.setAuthorId(Long.valueOf(user.getId()));
        article.setPublisher(user.getName());
        
        log.info("最终保存的文章数据: {}", article);
        
        boolean success = this.save(article);
        if (!success) {
            log.error("文章保存失败");
            throw new MyException("文章保存失败");
        }
        
        log.info("=== 文章保存完成，返回新创建的文章，ID: {} ===", article.getId());
        return article;
    }

    // 编辑文章
    @Override
    public void updateArticle(Article article, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        checkPermissionForArticle(article.getId(), request);
        
        // 获取原文章状态
        Article originalArticle = this.getById(article.getId());
        if (originalArticle == null) {
            throw new MyException("文章不存在");
        }
        
        // 如果原文章状态是已驳回，修改后重置为待审核
        if (originalArticle.getStatus() == ArticleStatus.REJECTED) {
            article.setStatus(ArticleStatus.PENDING);
            log.info("被驳回的文章修改后状态重置为待审核: articleId={}", article.getId());
        }
        
        article.setUpdateTime(new Date());
        this.updateById(article);
    }

    // 删除文章
    @Override
    public void deleteArticle(Article article, HttpServletRequest request) {
        checkPermissionForArticle(article.getId(), request);
        this.removeById(article.getId());
    }

    // 超级管理员删除文章（无需检查作者权限）
    @Override
    public void adminDeleteArticle(Article article, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException(AuthUtils.getAuthErrorMessage(request));
        }
        
        // 只有超级管理员才能使用此接口
        if (!AuthUtils.isSuperAdmin(user)) {
            throw new MyException("无权执行此操作，只有超级管理员可以删除任何动态");
        }
        
        Article existingArticle = this.getById(article.getId());
        if (existingArticle == null) {
            throw new MyException("文章不存在");
        }
        
        log.info("超级管理员 {} (id={}) 删除文章: id={}, title={}, author={}", 
            user.getName(), user.getId(), existingArticle.getId(), 
            existingArticle.getTitle(), existingArticle.getAuthorName());
            
        this.removeById(article.getId());
    }

    // 分页获取文章
    @Override
    public IPage<Article> pageList(ArticlePageQueryDTO queryDTO, HttpServletRequest request) {
        log.info("=== 开始分页查询文章 ===");
        log.info("查询参数: pageNum={}, pageSize={}, title={}, description={}, status={}", 
            queryDTO.getPageNum(), queryDTO.getPageSize(), queryDTO.getTitle(), 
            queryDTO.getDescription(), queryDTO.getStatus());
        
        // 创建分页对象
        Page<Article> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        // 获取当前用户
        User currentUser = AuthUtils.getCurrentUser(request);
        if (currentUser == null) {
            throw new MyException("用户未登录");
        }
        log.info("当前用户: id={}, name={}, role={}", 
            currentUser.getId(), currentUser.getName(), currentUser.getRole());

        // 检查前端是否明确要求管理员视图
        boolean isAdminView = "true".equalsIgnoreCase(request.getParameter("isAdminView"));

        // 根据用户角色设置查询条件
        if (AuthUtils.isSuperAdmin(currentUser)) {
            // 超级管理员可以看到所有动态
            log.info("超级管理员：返回所有文章");
            
            // 如果是审核视图，只显示审核中的动态
            if ("REVIEWING".equals(queryDTO.getStatus())) {
                queryWrapper.eq("status", ArticleStatus.REVIEWING);
                log.info("超级管理员审核视图：只显示审核中的文章");
            }
        } else {
            // 企业管理员和普通管理员只能看到：
            // 1. 自己发布的动态
            // 2. 超级管理员发布的动态
            queryWrapper.and(wrapper -> wrapper
                .eq("publisher", currentUser.getName())  // 自己发布的动态
                .or()
                .eq("publisher", "admin") // 超级管理员发布的动态
            );
            log.info("普通/企业管理员：只查看自己或超管发布的文章, publisher={}", currentUser.getName());
        }

        // 动态查询条件
        if (StringUtils.hasText(queryDTO.getTitle())) {
            queryWrapper.like("title", queryDTO.getTitle());
            log.info("添加标题查询条件: LIKE '%{}%'", queryDTO.getTitle());
        }
        if (StringUtils.hasText(queryDTO.getDescription())) {
            queryWrapper.like("description", queryDTO.getDescription());
            log.info("添加描述查询条件: LIKE '%{}%'", queryDTO.getDescription());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            try {
                ArticleStatus statusEnum = ArticleStatus.valueOf(queryDTO.getStatus().toUpperCase());
                queryWrapper.eq("status", statusEnum);
                log.info("添加状态查询条件: status={}", statusEnum);
            } catch (IllegalArgumentException e) {
                log.warn("无效的状态值: {}", queryDTO.getStatus());
            }
        }

        // 按更新时间倒序排序
        queryWrapper.orderByDesc("update_time");
        
        // 执行查询
        IPage<Article> result = this.page(page, queryWrapper);
        
        log.info("查询结果: 总记录数={}, 当前页记录数={}", 
            result.getTotal(), result.getRecords().size());
        log.info("=== 分页查询文章完成 ===");
        
        return result;
    }

    @Override
    public void approveArticle(Long id, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        if (!AuthUtils.isAdmin(user)) {
            throw new MyException("无权执行此操作");
        }
        
        Article article = this.getById(id);
        if (article == null) {
            throw new MyException("文章不存在");
        }
        
        // 只有审核中的文章才能被审核通过
        if (article.getStatus() != ArticleStatus.REVIEWING) {
            throw new MyException("只有审核中的文章才能被审核通过");
        }
        
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setUpdateTime(new Date());
        this.updateById(article);
    }

    @Override
    public void rejectArticle(Long id, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        if (!AuthUtils.isAdmin(user)) {
            throw new MyException("无权执行此操作");
        }
        
        Article article = this.getById(id);
        if (article == null) {
            throw new MyException("文章不存在");
        }
        
        // 只有审核中的文章才能被驳回
        if (article.getStatus() != ArticleStatus.REVIEWING) {
            throw new MyException("只有审核中的文章才能被驳回");
        }
        
        article.setStatus(ArticleStatus.REJECTED);
        article.setUpdateTime(new Date());
        this.updateById(article);
    }

    @Override
    public void submitForReview(Long id, HttpServletRequest request) {
        log.info("=== 开始提交文章审核, articleId: {} ===", id);
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        log.info("当前操作用户: id={}, name={}", user.getId(), user.getName());

        Article article = this.getById(id);
        if (article == null) {
            log.error("提交审核失败: 文章不存在, articleId: {}", id);
            throw new MyException("文章不存在");
        }
        log.info("获取到的文章: {}", article);

        // 检查是否是文章作者 - 使用 Objects.equals 避免空指针异常
        if (!java.util.Objects.equals(article.getAuthorId(), Long.valueOf(user.getId()))) {
            log.warn("提交审核失败: 用户无权操作他人文章. articleAuthorId={}, currentUserId={}",
                    article.getAuthorId(), user.getId());
            throw new MyException("只能提交自己的文章进行审核");
        }
        log.info("文章作者权限验证通过");

        // 只有未审核或已驳回的文章才能提交审核
        if (article.getStatus() != ArticleStatus.PENDING && article.getStatus() != ArticleStatus.REJECTED) {
            log.warn("提交审核失败: 文章状态不正确. currentStatus={}", article.getStatus());
            throw new MyException("只有未审核或已驳回的文章才能提交审核");
        }
        log.info("文章状态验证通过, 当前状态: {}", article.getStatus());

        article.setStatus(ArticleStatus.REVIEWING);
        article.setUpdateTime(new Date());
        log.info("准备更新文章状态为审核中 (REVIEWING)");
        
        boolean success = this.updateById(article);
        if (success) {
            log.info("文章 {} 提交审核成功", id);
        } else {
            log.error("文章 {} 提交审核失败: 数据库更新返回false", id);
            throw new MyException("提交审核失败，请稍后重试");
        }
    }

    @Override
    public void publishArticles(java.util.List<Long> articleIds, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        
        // 只有普通管理员和企业管理员可以发布
        if (AuthUtils.isSuperAdmin(user)) {
            throw new MyException("超级管理员不需要发布，创建的动态直接发布");
        }
        
        log.info("用户 {} 开始发布动态，ID列表: {}", user.getName(), articleIds);
        
        for (Long articleId : articleIds) {
            Article article = this.getById(articleId);
            if (article == null) {
                log.warn("文章不存在: {}", articleId);
                continue;
            }
            
            // 检查是否是文章作者
            if (!java.util.Objects.equals(article.getAuthorId(), Long.valueOf(user.getId()))) {
                log.warn("用户 {} 试图发布他人文章: {}", user.getName(), articleId);
                throw new MyException("只能发布自己的动态");
            }
            
            // 只有待审核或已驳回的动态可以发布
            if (article.getStatus() != ArticleStatus.PENDING && article.getStatus() != ArticleStatus.REJECTED) {
                log.warn("文章状态不正确，无法发布: articleId={}, status={}", articleId, article.getStatus());
                throw new MyException("只有待审核或已驳回的动态可以发布");
            }
            
            article.setStatus(ArticleStatus.REVIEWING);
            article.setUpdateTime(new Date());
            this.updateById(article);
            log.info("文章 {} 发布成功，状态变更为审核中", articleId);
        }
    }

    /**
     * 检查文章操作权限（更健壮的版本）
     */
    private void checkPermissionForArticle(Long articleId, HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException(AuthUtils.getAuthErrorMessage(request));
        }
        
        Article article = this.getById(articleId);
        if (article == null) {
            throw new MyException("文章不存在");
        }

        // 超级管理员可以操作所有动态
        if (AuthUtils.isSuperAdmin(user)) {
            return;
        }
        
        // 其他用户只能操作自己的动态
        if (!AuthUtils.hasPermission(user, article.getAuthorId())) {
            throw new MyException("无权操作他人文章");
        }
    }
    
    /**
     * 检查用户登录状态和操作权限（保留用于兼容性）
     */
    private User checkLoginAndPermission(Long articleId, HttpServletRequest request) {
        checkPermissionForArticle(articleId, request);
        return AuthUtils.getCurrentUser(request);
    }
    
    /**
     * 检查是否为超级管理员（保留用于兼容性）
     */
    private void checkAdminRole(HttpServletRequest request) {
        User user = AuthUtils.getCurrentUser(request);
        if (user == null) {
            throw new MyException("用户未登录");
        }
        if (!AuthUtils.isAdmin(user)) {
            throw new MyException("无权执行此操作");
        }
    }
}

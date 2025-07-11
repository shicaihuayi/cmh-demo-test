package com.lfx.demo.controller;

import com.lfx.demo.entity.User;
import com.lfx.demo.service.ConferService;
import com.lfx.demo.service.CourseService;
import com.lfx.demo.service.IArticleService;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ConferService conferService;

    @Autowired
    private IArticleService articleService;

    /**
     * 获取所有类型的待审核任务统计
     * @return 待审核任务统计信息
     */
    @RequestMapping("/pending-summary")
    public AjaxResult getPendingSummary(HttpSession session, HttpServletRequest request) {
        try {
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                return AjaxResult.error("权限不足，只有超级管理员可以查看待审核统计");
            }

            Map<String, Object> summary = new HashMap<>();
            
            try {
                // 获取待审核课程数量
                int coursesCount = courseService.selectPublish().size();
                summary.put("courses", coursesCount);
            } catch (Exception e) {
                summary.put("courses", 0);
                System.err.println("获取待审核课程数量失败: " + e.getMessage());
            }

            try {
                // 获取待审核会议数量
                int conferencesCount = conferService.getAuditConferences().size();
                summary.put("conferences", conferencesCount);
            } catch (Exception e) {
                summary.put("conferences", 0);
                System.err.println("获取待审核会议数量失败: " + e.getMessage());
            }

            try {
                // 获取待审核动态数量
                com.lfx.demo.entity.dto.ArticlePageQueryDTO queryDTO = new com.lfx.demo.entity.dto.ArticlePageQueryDTO();
                queryDTO.setStatus("REVIEWING");
                queryDTO.setPageNum(1);
                queryDTO.setPageSize(1000);
                
                com.baomidou.mybatisplus.core.metadata.IPage<com.lfx.demo.entity.Article> pageResult = 
                    articleService.pageList(queryDTO, request);
                int newsCount = (int) pageResult.getTotal();
                summary.put("news", newsCount);
            } catch (Exception e) {
                summary.put("news", 0);
                System.err.println("获取待审核动态数量失败: " + e.getMessage());
            }

            // 计算总数
            int total = (Integer) summary.get("courses") + 
                       (Integer) summary.get("conferences") + 
                       (Integer) summary.get("news");
            summary.put("total", total);

            return AjaxResult.success("获取待审核任务统计成功", summary);

        } catch (Exception e) {
            return AjaxResult.error("获取待审核任务统计失败：" + e.getMessage());
        }
    }
} 
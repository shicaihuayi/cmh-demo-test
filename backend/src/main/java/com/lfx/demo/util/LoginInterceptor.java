package com.lfx.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfx.demo.entity.User;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        logger.debug("拦截请求: {} {}", requestMethod, requestPath);

        // 首先检查Session中是否已有用户信息（备用验证机制）
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("login_user") != null) {
            User existingUser = (User) session.getAttribute("login_user");
            logger.debug("从Session中找到已登录用户: ID={}, Name={}, Role={}", 
                existingUser.getId(), existingUser.getName(), existingUser.getRole());
            return true;
        }

        // 从请求头中获取用户信息
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");

        // 记录认证信息状态
        logger.debug("认证信息检查 - UserId: {}, Username: {}, UserRole: {}", 
            StringUtils.hasText(userId) ? userId : "缺失",
            StringUtils.hasText(username) ? username : "缺失", 
            StringUtils.hasText(userRole) ? userRole : "缺失");

        // 检查用户信息是否完整
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(username) || !StringUtils.hasText(userRole)) {
            logger.warn("认证失败 - 请求路径: {} {}, 缺失认证头信息", requestMethod, requestPath);
            
            // 优雅地返回JSON错误响应，而不是抛异常
            writeErrorResponse(response, "用户未登录或认证信息不完整");
            return false;
        }

        try {
            // 解码用户名（处理中文字符）
            String decodedUsername;
            try {
                decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
                logger.debug("用户名解码: {} -> {}", username, decodedUsername);
            } catch (Exception e) {
                logger.warn("用户名解码失败，使用原始值: {}", username, e);
                decodedUsername = username;
            }
            
            // 创建User对象并设置到session中
            User user = new User();
            user.setId(Integer.parseInt(userId));
            user.setName(decodedUsername);
            user.setRole(userRole);

            request.getSession().setAttribute("login_user", user);
            
            logger.debug("用户认证成功 - ID: {}, Name: {}, Role: {}", userId, decodedUsername, userRole);
            return true;
            
        } catch (NumberFormatException e) {
            logger.error("用户ID格式错误: {}", userId, e);
            writeErrorResponse(response, "用户ID格式错误");
            return false;
        } catch (Exception e) {
            logger.error("认证过程中发生未知错误", e);
            writeErrorResponse(response, "认证失败，请重新登录");
            return false;
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        AjaxResult errorResult = AjaxResult.error(message);
        String jsonResponse = objectMapper.writeValueAsString(errorResult);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
} 
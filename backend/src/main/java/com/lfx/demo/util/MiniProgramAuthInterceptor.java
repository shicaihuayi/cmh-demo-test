package com.lfx.demo.util;

import com.lfx.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 小程序认证拦截器
 * 处理小程序端通过自定义请求头发送的认证信息
 */
@Component
public class MiniProgramAuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(MiniProgramAuthInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");
        
        logger.debug("小程序认证拦截器 - URI: {}, 请求头: userId={}, username={}, userRole={}", 
            request.getRequestURI(), userId, username, userRole);
        
        // 如果请求头中包含用户信息，设置到Session中
        if (StringUtils.hasText(userId) && StringUtils.hasText(username) && StringUtils.hasText(userRole)) {
            try {
                User user = new User();
                user.setId(Integer.parseInt(userId));
                user.setName(username);
                user.setRole(userRole);
                
                // 将用户信息存入Session，便于后续的Controller使用
                request.getSession().setAttribute("login_user", user);
                
                logger.debug("小程序用户认证成功: id={}, name={}, role={}", user.getId(), user.getName(), user.getRole());
                
            } catch (NumberFormatException e) {
                logger.error("用户ID格式错误: {}", userId, e);
            }
        }
        
        return true; // 继续执行后续处理
    }
} 
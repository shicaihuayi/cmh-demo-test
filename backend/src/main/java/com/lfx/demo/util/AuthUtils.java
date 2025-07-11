package com.lfx.demo.util;

import com.lfx.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 认证工具类
 * 提供健壮的用户认证和权限验证功能
 */
public class AuthUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
    
    // 角色常量
    public static final String ROLE_SUPER_ADMIN = "3";    // 超级管理员
    public static final String ROLE_NORMAL_ADMIN = "2";   // 普通管理员
    public static final String ROLE_COMPANY_ADMIN = "1";  // 企业管理员
    
    /**
     * 从请求中获取当前用户信息
     * 支持多种获取方式，提高成功率
     */
    public static User getCurrentUser(HttpServletRequest request) {
        logger.debug("开始获取当前用户信息");
        
        // 方式1：从Session中获取（优先级最高）
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("login_user") != null) {
            User user = (User) session.getAttribute("login_user");
            logger.debug("从Session获取用户信息成功: id={}, name={}, role={}", 
                user.getId(), user.getName(), user.getRole());
            return user;
        }
        
        // 方式2：从请求头中获取并创建用户对象
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String userRole = request.getHeader("X-User-Role");
        
        if (StringUtils.hasText(userId) && StringUtils.hasText(username) && StringUtils.hasText(userRole)) {
            try {
                User user = new User();
                user.setId(Integer.parseInt(userId));
                user.setName(username);
                user.setRole(userRole);
                
                // 将用户信息存入Session，便于后续使用
                request.getSession().setAttribute("login_user", user);
                
                logger.debug("从请求头获取用户信息成功: id={}, name={}, role={}", 
                    user.getId(), user.getName(), user.getRole());
                return user;
            } catch (NumberFormatException e) {
                logger.error("用户ID格式错误: {}", userId, e);
            }
        }
        
        logger.warn("无法获取用户信息 - UserId: {}, Username: {}, UserRole: {}", 
            userId, username, userRole);
        return null;
    }
    
    /**
     * 检查用户是否有权限执行操作
     */
    public static boolean hasPermission(User user, Long resourceOwnerId) {
        if (user == null) {
            logger.warn("用户未登录，无权限");
            return false;
        }
        
        // 超级管理员有权操作任何资源
        if (isSuperAdmin(user)) {
            logger.debug("超级管理员有权限操作所有资源: userId={}", user.getId());
            return true;
        }
        
        // 普通用户只能操作自己的资源
        boolean hasPermission = user.getId().longValue() == resourceOwnerId;
        logger.debug("用户权限检查: userId={}, resourceOwnerId={}, hasPermission={}", 
            user.getId(), resourceOwnerId, hasPermission);
        return hasPermission;
    }
    
    /**
     * 判断用户是否为超级管理员
     */
    public static boolean isSuperAdmin(User user) {
        if (user == null) {
            return false;
        }
        // 支持两种判断方式：用户名为"admin"或角色为"3"
        return "admin".equals(user.getName()) || ROLE_SUPER_ADMIN.equals(user.getRole());
    }
    
    /**
     * 判断用户是否为普通管理员
     */
    public static boolean isNormalAdmin(User user) {
        if (user == null) {
            return false;
        }
        return ROLE_NORMAL_ADMIN.equals(user.getRole());
    }
    
    /**
     * 判断用户是否为企业管理员
     */
    public static boolean isCompanyAdmin(User user) {
        if (user == null) {
            return false;
        }
        return ROLE_COMPANY_ADMIN.equals(user.getRole());
    }
    
    /**
     * 判断用户是否为任意类型的管理员
     */
    public static boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        boolean isAdmin = ROLE_SUPER_ADMIN.equals(role) || 
                         ROLE_NORMAL_ADMIN.equals(role) || 
                         ROLE_COMPANY_ADMIN.equals(role);
        
        logger.debug("检查用户是否为管理员: userId={}, role={}, isAdmin={}", 
            user.getId(), role, isAdmin);
        return isAdmin;
    }
    
    /**
     * 获取用户友好的错误消息
     */
    public static String getAuthErrorMessage(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().contains("mobile")) {
            return "登录已过期，请重新登录";
        }
        return "认证失败，请刷新页面后重试";
    }
} 
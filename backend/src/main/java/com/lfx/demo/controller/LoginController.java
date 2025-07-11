package com.lfx.demo.controller;

import com.lfx.demo.entity.User;
import com.lfx.demo.service.UserService;
import com.lfx.demo.util.MyException;
import com.lfx.demo.web.domain.AjaxResult;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 独立的登录控制器，提供根路径 "/login" 接口，
 * 方便前端直接调用，而无需携带 "/user" 前缀。
 */
@RestController
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    /**
     * 登录接口：POST /login
     */
    @RequestMapping("/login")
    public Map<String, Object> login(User user, HttpSession session, HttpServletRequest request) {
        logger.info("登录请求 -> 用户名: '{}'", user.getName());
        Map<String, Object> map = new HashMap<>();
        try {
            User dbUser = userService.checkLogin(user);
            if (dbUser != null) {
                // Invalidate session only if it's not a mock session (for testing purposes)
                if (!session.getClass().getName().contains("Mock")) {
                    // 清除旧的session信息，防止数据混乱
                    session.invalidate();
                    // 获取新的session并存储当前用户信息
                    session = request.getSession(true);
                }
                session.setAttribute("login_user", dbUser);
                
                map.put("isOk", true);
                map.put("msg", "登录成功");
                map.put("user", dbUser);
                logger.info("用户 '{}' 登录成功, Session已更新.", dbUser.getName());
                return map;
            } else {
                logger.warn("用户 '{}' 登录失败: 用户名或密码错误", user.getName());
                map.put("isOk", false);
                map.put("msg", "用户名或密码错误");
                map.put("user", null);
                return map;
            }
        } catch (MyException e) {
            logger.warn("用户 '{}' 登录失败: {}", user.getName(), e.getMessage());
            map.put("isOk", false);
            map.put("msg", e.getMessage()); // 返回 "该账号已被禁用"
            map.put("user", null);
            return map;
        }
    }

    /**
     * 调试接口：检查当前session状态
     */
    @RequestMapping("/debug/session")
    public AjaxResult debugSession(HttpSession session, HttpServletRequest request) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        // 基本session信息
        debugInfo.put("sessionId", session.getId());
        debugInfo.put("isNew", session.isNew());
        debugInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        debugInfo.put("creationTime", session.getCreationTime());
        debugInfo.put("lastAccessedTime", session.getLastAccessedTime());
        
        // 用户信息检查
        Object loginUser = session.getAttribute("login_user");
        if (loginUser != null) {
            User user = (User) loginUser;
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("companyName", user.getCompanyName());
            debugInfo.put("loginUser", userInfo);
        } else {
            debugInfo.put("loginUser", null);
        }
        
        // 请求头信息
        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", request.getHeader("X-User-Id"));
        headers.put("X-Username", request.getHeader("X-Username"));
        headers.put("X-User-Role", request.getHeader("X-User-Role"));
        headers.put("User-Agent", request.getHeader("User-Agent"));
        headers.put("Cookie", request.getHeader("Cookie"));
        debugInfo.put("requestHeaders", headers);
        
        // 所有session属性
        Map<String, Object> sessionAttributes = new HashMap<>();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            sessionAttributes.put(attributeName, attributeValue != null ? attributeValue.toString() : "null");
        }
        debugInfo.put("sessionAttributes", sessionAttributes);
        
        logger.info("Session调试信息: {}", debugInfo);
        return AjaxResult.success("Session调试信息", debugInfo);
    }
} 
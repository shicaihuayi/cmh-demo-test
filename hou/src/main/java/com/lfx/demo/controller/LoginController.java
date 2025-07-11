package com.lfx.demo.controller;

import com.lfx.demo.entity.User;
import com.lfx.demo.service.UserService;
import com.lfx.demo.util.MyException;
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
        User dbUser = userService.checkLogin(user);
        Map<String, Object> map = new HashMap<>();
        if (dbUser != null) {
            // 清除旧的session信息，防止数据混乱
            session.invalidate();
            // 获取新的session并存储当前用户信息
            session = request.getSession(true);
            session.setAttribute("user", dbUser);
            
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
    }
} 
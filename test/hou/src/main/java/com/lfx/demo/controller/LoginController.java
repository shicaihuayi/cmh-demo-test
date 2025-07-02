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
    public Map<String, Object> login(User user, HttpSession session) {
        logger.info("(Root) 登录请求 -> 用户名: '{}', 密码: '{}'", user.getName(), user.getPwd());
        User dbUser = userService.checkLogin(user);
        Map<String, Object> map = new HashMap<>();
        if (dbUser != null) {
            session.setAttribute("login_user", dbUser);
            map.put("isOk", true);
            map.put("msg", "登录成功");
            map.put("user", dbUser);
            logger.info("用户 '{}' 登录成功 (Root)", dbUser.getName());
            return map;
        } else {
            logger.warn("用户 '{}' 登录失败: 用户名或密码错误 (Root)", user.getName());
            throw new MyException("用户名或密码错误");
        }
    }
} 
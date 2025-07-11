package com.lfx.demo.controller;

import com.lfx.demo.entity.User;
import com.lfx.demo.service.UserService;
import com.lfx.demo.util.LoginAttemptService;
import com.lfx.demo.util.MyException;
import com.lfx.demo.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/user")
public class AppUserController {
    private static final Logger logger = LoggerFactory.getLogger(AppUserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * 微信小程序登录接口
     * 根据前端传入的微信用户信息，创建一个临时的游客用户对象并返回。
     * 这个过程不涉及数据库写入，仅用于快速模拟登录。
     */
    @RequestMapping(value = "/wxLogin", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> wxLogin(@RequestBody Map<String, Object> loginData) {
        String nickName = (String) loginData.get("nickname");
        String avatarUrl = (String) loginData.get("avatarUrl");
        // gender 的值可能是数字，也可能是字符串，这里做简单处理
        String gender = String.valueOf(loginData.get("gender"));

        logger.info("(/app/user/wxLogin) [小程序] 微信快速登录请求 -> nickName: '{}'", nickName);
        
        Map<String, Object> map = new HashMap<>();
        
        try {
            // 创建一个临时的游客用户，不写入数据库
            User guestUser = new User();
            guestUser.setId(-1); // 使用-1表示这是一个临时的、未保存的游客
            guestUser.setName(nickName != null ? nickName : "微信用户");
            guestUser.setNickname(nickName != null ? nickName : "微信用户");
            guestUser.setImageUrl(avatarUrl);
            guestUser.setRole("guest"); // 赋予一个特殊的 "guest" 角色
            guestUser.setState(1);

            if ("0".equals(gender)) {
                guestUser.setSex("未知");
            } else if ("1".equals(gender)) {
                guestUser.setSex("man");
            } else if ("2".equals(gender)) {
                guestUser.setSex("woman");
            }

            // 使用JwtUtil生成Token
            String token = JwtUtil.generateToken(guestUser);

            map.put("isOk", true);
            map.put("msg", "游客登录成功");
            map.put("user", guestUser);
            map.put("token", token);
            
            logger.info("成功创建临时游客: {}, 并为其签发了Token", nickName);
            
        } catch (Exception e) {
            logger.error("创建临时游客失败", e);
            map.put("isOk", false);
            map.put("msg", "微信登录失败：" + e.getMessage());
        }
        return map;
    }
    
    /**
     * 小程序登录接口
     * 使用手机号和密码登录，只允许role=0的普通用户登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> login(@RequestBody User user, HttpSession session, HttpServletRequest request) {
        String clientKey = getClientIpAddress(session);
        
        if (loginAttemptService.isBlocked(clientKey)) {
            logger.warn("客户端 '{}' 登录尝试次数过多，暂时被阻止", clientKey);
            throw new MyException("登录尝试次数过多，请稍后再试");
        }
        
        logger.info("(/app/user/login) [小程序] 登录请求 -> 手机号: '{}'", user.getTel());
                   
        // 使用手机号和密码进行登录验证
        User dbUser = userService.checkLoginByPhone(user.getTel(), user.getPwd());
        Map<String, Object> map = new HashMap<>();
        
        if(dbUser != null){
            // 检查是否为普通用户（role=0）
            if(!"0".equals(dbUser.getRole())){
                loginAttemptService.loginFailed(clientKey);
                logger.warn("用户 '{}' 不是普通用户，无法通过小程序登录", user.getTel());
                map.put("isOk", false);
                map.put("msg", "该账号无法使用小程序登录");
                return map;
            }
            
            loginAttemptService.loginSucceeded(clientKey);

            // 使用JwtUtil生成Token
            String token = JwtUtil.generateToken(dbUser);
            
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("login_user", dbUser);
            
            map.put("isOk", true);
            map.put("msg", "登录成功");
            // 只返回小程序需要的用户信息字段
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", dbUser.getId());
            userInfo.put("name", dbUser.getName());
            userInfo.put("nickname", dbUser.getNickname());
            userInfo.put("tel", dbUser.getTel());
            userInfo.put("imageUrl", dbUser.getImageUrl());
            userInfo.put("sex", dbUser.getSex());
            userInfo.put("email", dbUser.getEmail());
            userInfo.put("role", dbUser.getRole());
            map.put("user", userInfo);
            map.put("token", token);
            
            logger.info("普通用户 '{}' 通过小程序登录成功, Token已签发", dbUser.getName());
            return map;
        } else {
            loginAttemptService.loginFailed(clientKey);
            logger.warn("用户 '{}' 登录失败: 手机号或密码错误", user.getTel());
            map.put("isOk", false);
            map.put("msg", "手机号或密码错误");
            return map;
        }
    }

    /**
     * 小程序用户注册接口
     */
    @RequestMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        logger.info("(/app/user/register) [小程序] 注册请求 -> 手机号: '{}'", user.getTel());
        
        // 检查用户名是否已存在
        User existUser = userService.getUserByName(user.getName());
        Map<String, Object> map = new HashMap<>();
        
        if(existUser != null) {
            map.put("isOk", false);
            map.put("msg", "用户名已存在");
            return map;
        }
        
        // 验证验证码
        if(!(userService.checkCode(user.getTel(), user.getCode()))) {
            map.put("isOk", false);
            map.put("msg", "验证码错误");
            return map;
        }
        
        // 设置为普通用户
        user.setRole("0");
        
        boolean flag = userService.insert(user);
        if (flag) {
            map.put("isOk", true);
            map.put("msg", "注册成功");
            logger.info("新用户 '{}' 注册成功", user.getName());
        } else {
            map.put("isOk", false);
            map.put("msg", "注册失败");
        }
        return map;
    }

    /**
     * 获取当前登录用户信息（小程序版本）
     */
    @RequestMapping("/profile")
    public Map<String, Object> getProfile(@RequestParam Integer id) {
        logger.info("(/app/user/profile) [小程序] 获取用户信息 -> ID: {}", id);
        
        Map<String, Object> map = new HashMap<>();
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        
        User user = userService.getUserById(id);
        if (user != null && "0".equals(user.getRole().toString())) {
            // 只返回小程序需要的字段
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("tel", user.getTel());
            userInfo.put("imageUrl", user.getImageUrl());
            userInfo.put("sex", user.getSex());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("user", userInfo);
        } else {
            map.put("isOk", false);
            map.put("msg", "用户不存在或无权限");
        }
        return map;
    }

    /**
     * 更新用户信息（小程序版本）
     */
    @RequestMapping("/updateProfile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> updateData) {
        logger.info("(/app/user/updateProfile) [小程序] 更新用户信息 -> 数据: {}", updateData);
        
        Map<String, Object> map = new HashMap<>();
        
        // 从请求数据中提取字段
        Integer id = (Integer) updateData.get("id");
        String nickname = (String) updateData.get("nickname");
        String tel = (String) updateData.get("tel");
        String email = (String) updateData.get("email");
        String sex = (String) updateData.get("sex");
        String imageUrl = (String) updateData.get("imageUrl");
        String code = (String) updateData.get("code"); // 验证码
        
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        
        // 验证是否为普通用户
        User existUser = userService.getUserById(id);
        if (existUser == null || !"0".equals(existUser.getRole())) {
            map.put("isOk", false);
            map.put("msg", "用户不存在或无权限");
            return map;
        }

        // 如果手机号被修改，则需要验证码
        if (tel != null && !tel.equals(existUser.getTel())) {
            if (!userService.checkCode(tel, code)) {
                map.put("isOk", false);
                map.put("msg", "验证码错误");
                return map;
            }
        }
        
        // 更新用户信息
        User toUpdate = new User();
        toUpdate.setId(id);
        toUpdate.setNickname(nickname);
        toUpdate.setTel(tel);
        toUpdate.setEmail(email);
        toUpdate.setSex(sex);
        toUpdate.setImageUrl(imageUrl);

        if (userService.update(toUpdate)) {
            map.put("isOk", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    /**
     * DTO for password change requests
     */
    public static class PasswordChangeDTO {
        private Integer id;
        private String oldPassword;
        private String newPassword;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * 修改密码（小程序版本）
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public Map<String, Object> changePassword(@RequestBody PasswordChangeDTO dto) {
        logger.info("(/app/user/changePassword) [小程序] 修改密码 -> ID: {}", dto.getId());
        
        Map<String, Object> map = new HashMap<>();
        if (dto.getId() == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        
        // 验证是否为普通用户并检查旧密码
        User user = userService.getUserById(dto.getId());
        if (user == null || !"0".equals(user.getRole().toString())) {
            map.put("isOk", false);
            map.put("msg", "用户不存在或无权限");
            return map;
        }
        
        if (!dto.getOldPassword().equals(user.getPwd())) {
            map.put("isOk", false);
            map.put("msg", "旧密码错误");
            return map;
        }
        
        boolean success = userService.updatePwd(dto.getNewPassword(), dto.getId());
        if (success) {
            map.put("isOk", true);
            map.put("msg", "密码修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "密码修改失败");
        }
        return map;
    }

    /**
     * 更新头像（小程序版本）
     */
    @RequestMapping("/updateAvatar")
    public Map<String, Object> updateAvatar(@RequestBody User user) {
        logger.info("(/app/user/updateAvatar) [小程序] 更新头像 -> ID: {}", user.getId());
        
        Map<String, Object> map = new HashMap<>();
        // 只更新头像字段
        User toUpdate = new User();
        toUpdate.setId(user.getId());
        toUpdate.setImageUrl(user.getImageUrl());

        if (userService.update(toUpdate)) {
            map.put("isOk", true);
            map.put("msg", "头像更新成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "头像更新失败");
        }
        return map;
    }

    /**
     * 发送验证码（小程序版本）
     */
    @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
    public Map<String, Object> sendCode(@RequestBody Map<String, String> payload) {
        String tel = payload.get("tel");
        logger.info("(/app/user/sendCode) [小程序] 发送验证码 -> 手机号: {}", tel);
        return userService.toSendMessage(tel);
    }

    private String getClientIpAddress(HttpSession session) {
        try {
            return session.getId();
        } catch (Exception e) {
            return "unknown";
        }
    }
} 
/**
 * 用户管理控制器
 * 
 * 功能说明：
 * - 用户登录认证（Web端和小程序端）
 * - 用户注册和验证码发送
 * - 用户信息管理（增删改查）
 * - 用户权限控制（基于角色和公司）
 * - 微信小程序登录集成
 * - 登录安全防护（防暴力破解）
 * 
 * 接口路径：/user
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 */
package com.lfx.demo.controller;

import com.lfx.demo.entity.User;
import com.lfx.demo.service.UserService;
import com.lfx.demo.util.LoginAttemptService;
import com.lfx.demo.util.MyException;
import com.lfx.demo.entity.dto.UserLoadDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private LoginAttemptService loginAttemptService;

    public void setCompanyService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Web后台登录接口
     * 支持 application/x-www-form-urlencoded 和 multipart/form-data
     * 使用用户名和密码登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String, Object> loginForAdmin(User user, HttpSession session, HttpServletRequest request) {
        String clientKey = getClientIpAddress(session);
        
        if (loginAttemptService.isBlocked(clientKey)) {
            logger.warn("客户端 '{}' 登录尝试次数过多，暂时被阻止", clientKey);
            throw new MyException("登录尝试次数过多，请稍后再试");
        }
        
        logger.info("(/user/login) [Web] 登录请求 -> 用户名: '{}'", user.getName());
                   
        User dbUser = userService.checkLogin(user); // This uses username
        Map<String, Object> map = new HashMap<>();
        if(dbUser != null){
            loginAttemptService.loginSucceeded(clientKey);
            
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("login_user", dbUser);
            
            map.put("isOk", true);
            map.put("msg", "登录成功");
            map.put("user", dbUser);
            logger.info("用户 '{}' 登录成功 (via Web /user/login), Session 已更新.", dbUser.getName());
            return map;
        } else {
            loginAttemptService.loginFailed(clientKey);
            logger.warn("用户 '{}' 登录失败: 用户名或密码错误 (via Web /user/login)", user.getName());
            map.put("isOk", false);
            map.put("msg", "用户名或密码错误");
            map.put("user", null);
            return map;
        }
    }

    /**
     * 小程序登录接口
     * 支持 application/json
     * 使用手机号和密码登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> loginForApp(@RequestBody User user, HttpSession session, HttpServletRequest request) {
        String clientKey = getClientIpAddress(session);
        
        if (loginAttemptService.isBlocked(clientKey)) {
            logger.warn("客户端 '{}' 登录尝试次数过多，暂时被阻止", clientKey);
            throw new MyException("登录尝试次数过多，请稍后再试");
        }
        
        logger.info("(/user/login) [App] 登录请求 -> 手机号: '{}'", user.getTel());
                   
        // 使用手机号和密码进行登录验证
        User dbUser = userService.checkLoginByPhone(user.getTel(), user.getPwd());
        Map<String, Object> map = new HashMap<>();
        if(dbUser != null){
            loginAttemptService.loginSucceeded(clientKey);
            
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("login_user", dbUser);
            
            map.put("isOk", true);
            map.put("msg", "登录成功");
            map.put("user", dbUser);
            logger.info("用户 '{}' 登录成功 (via App /user/login), Session 已更新.", dbUser.getName());
            return map;
        } else {
            loginAttemptService.loginFailed(clientKey);
            logger.warn("用户 '{}' 登录失败: 手机号或密码错误 (via App /user/login)", user.getTel());
            map.put("isOk", false);
            map.put("msg", "用户名或密码错误");
            map.put("user", null);
            return map;
        }
    }
    
    private String getClientIpAddress(HttpSession session) {
        try {
            return session.getId(); // 简化版本，返回session ID作为标识
        } catch (Exception e) {
            return "unknown";
        }
    }
    //发送验证码 (POST方式，给小程序和其他JSON客户端使用)
    @RequestMapping (value = "/send", method = RequestMethod.POST)
    public Map<String, Object> SendMessage(@RequestBody Map<String, String> payload){
        String tel = payload.get("tel");
        logger.info("(/user/send) POST 发送验证码 -> 手机号: '{}'", tel);
        return userService.toSendMessage(tel);
    }

    //发送验证码 (GET方式，给Web前端使用)
    @RequestMapping (value = "/send", method = RequestMethod.GET)
    public Map<String, Object> SendMessageByGet(@RequestParam("tel") String tel){
        logger.info("(/user/send) GET 发送验证码 -> 手机号: '{}'", tel);
        return userService.toSendMessage(tel);
    }

    //注册
    @RequestMapping ("/register")
    public Map<String, Object> register(User user){
        User user1=userService.getUserByName(user.getName());
        Map<String, Object> map = new HashMap<>();
        if(user1!=null){

            map.put("isOk",false);
            map.put("msg","用户名已存在");
            return map;
        }else{
            if(!(userService.checkCode(user.getTel(),user.getCode()))){
                map.put("isOk",false);
                map.put("msg","验证码错误");
                return map;
            }else {
                boolean flag = userService.insert(user);
                if (flag) {
                    map.put("isOk", true);
                    map.put("msg", "注册成功");
                    return map;
                } else {
                    map.put("isOk", false);
                    map.put("msg", "注册失败");
                    return map;
                }
            }
        }

    }

    @RequestMapping("/list")
    public Map<String, Object> list(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute("login_user");
        Map<String, Object> map = new HashMap<>();
        
        if (currentUser == null) {
            map.put("isOK", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        String userRole = currentUser.getRole();
        String userCompany = currentUser.getCompanyName();
        
        System.out.println("用户列表请求 - 用户: " + currentUser.getName() + 
                          ", 角色: " + userRole + 
                          ", 所属公司: " + userCompany);
        
        List<User> list;
        
        // 超级管理员可以查看所有用户
        if ("3".equals(userRole)) {
            list = userService.findAll();
        }
        // 企业管理员和普通管理员只能查看同公司的用户
        else if ("2".equals(userRole) || "1".equals(userRole)) {
            list = userService.searchUserByCompany(userCompany);
        }
        // 未知角色，拒绝访问
        else {
            map.put("isOK", false);
            map.put("msg", "权限不足");
            return map;
        }
        
        map.put("isOK", true);
        map.put("users", list);
        map.put("msg", "加载成功");
        return map;
    }

    @RequestMapping("/adminList")
    public Map<String, Object> adminList() {
        List<User> list = userService.findAdminUsers();
        Map<String, Object> map = new HashMap<>();
        map.put("isOK", true);
        map.put("users", list);
        map.put("msg", "管理员用户列表加载成功");
        return map;
    }

    @RequestMapping("/add")
    public Map<String, Object> add(User user) {

        boolean isOk = userService.insert(user);
        HashMap<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "添加成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "添加失败");
        }
        return map;
    }

    @RequestMapping("/del")
    public Map<String, Object> delete(@RequestBody List<Integer> ids) {
        logger.info("接收到删除用户请求 (JSON格式)，IDs: {}", ids);
        boolean isOk = userService.delete(ids);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
            logger.info("用户删除成功，IDs: {}", ids);
        } else {
            map.put("isOK", false);
            map.put("msg", "删除失败，可能包含超级管理员用户或其他限制");
            logger.warn("用户删除失败，IDs: {}", ids);
        }
        return map;
    }

    @RequestMapping(value = "/del", consumes = "multipart/form-data")
    public Map<String, Object> deleteFormData(@RequestParam("ids") List<Integer> ids) {
        logger.info("接收到删除用户请求 (FormData格式)，IDs: {}", ids);
        boolean isOk = userService.delete(ids);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
            logger.info("用户删除成功 (FormData)，IDs: {}", ids);
        } else {
            map.put("isOK", false);
            map.put("msg", "删除失败，可能包含超级管理员用户或其他限制");
            logger.warn("用户删除失败 (FormData)，IDs: {}", ids);
        }
        return map;
    }

    @RequestMapping("/delUser")
    public Map<String, Object> deleteUser(String companyName) {
        boolean isOk = userService.deleteUser(companyName);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        }
        return map;
    }

    @RequestMapping("/delUserBySecId")
    public Map<String, Object> deleteUserBySecId(Integer secId) {
        boolean isOk = userService.deleteSectionUser(secId);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        }
        return map;
    }

    @RequestMapping("/update")
    public Map<String, Object> update(User user) {
        boolean isOk = userService.update(user);
        HashMap<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping("/updateName")
    public Map<String, Object> update(String newName, String oldName) {
        boolean isOk = userService.updateCompanyName(newName,oldName);
        HashMap<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOk", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping("/updateAdmin")
    public Map<String, Object> updateAdmin(String oldadmin, String name) {
        boolean isOk = userService.updateAdminName(oldadmin,name);
        HashMap<String, Object> map = new HashMap<>();
        if(isOk){
            map.put("isOk",true);
            map.put("msg","修改成功");
        }else{
            map.put("isOk",false);
            map.put("msg","修改失败");
        }
        return map;
    }

    @RequestMapping("/updateSection")
    public Map<String, Object> updateAdmin(Integer secId, String section,String companyName) {
        boolean isOk = userService.updateSection(secId,section,companyName);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping("/updateState")
    public Map<String, Object> updateState(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        Integer state = params.get("state");
        boolean isOk = userService.updateState(state, id);
        Map<String, Object> map = new HashMap<>();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping ("/search")
        public Map<String, Object> search(@RequestBody Map<String, String> searchParams, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute("login_user");
        Map<String, Object> map = new HashMap<>();
        
        if (currentUser == null) {
            map.put("isOk", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        String name = searchParams.get("name");
        String nickname = searchParams.get("nickname");
        String tel = searchParams.get("tel");
        String startDate = searchParams.get("startDate");
        String endDate = searchParams.get("endDate");
        String companyName = searchParams.get("companyName");
        
        String userRole = currentUser.getRole();
        String userCompany = currentUser.getCompanyName();
        
        System.out.println("用户搜索请求 - 用户: " + currentUser.getName() + 
                          ", 角色: " + userRole + 
                          ", 所属公司: " + userCompany +
                          ", 搜索范围: " + companyName);
        
        List<User> list;
        
        // 超级管理员可以搜索所有用户
        if ("3".equals(userRole)) {
            list = userService.searchUsers(name, nickname, tel, startDate, endDate);
        }
        // 企业管理员和普通管理员只能搜索同公司的用户
        else if ("2".equals(userRole) || "1".equals(userRole)) {
            // 强制限制在用户所属公司范围内搜索
            list = userService.searchUsersByCompany(name, nickname, tel, startDate, endDate, userCompany);
        }
        // 未知角色，拒绝访问
        else {
            map.put("isOk", false);
            map.put("msg", "权限不足");
            return map;
        }
        
        map.put("isOk", true);
        map.put("users", list);
        map.put("msg", "搜索成功");
        return map;
    }

    @RequestMapping ("/search1")
    public Map<String, Object> search1(Integer secId) {
        List<User> list = userService.find(secId);
        Map<String, Object> map = new HashMap<>();
        map.put("isOk", true);
        map.put("users", list);
        return map;
    }

    @RequestMapping ("/searchuserbycompany")
    public Map<String, Object> searchUserByCompany(String companyName) {
        List<User> list = userService.searchUserByCompany(companyName);
        Map<String, Object> map = new HashMap<>();
        map.put("isOk", true);
        map.put("users", list);
        return map;
    }

    @RequestMapping ("/searchuserbycompanyName")
    public Map<String, Object> searchUserByCompanyName(String companyName) {
        List<User> list = userService.searchUserByCompanyName(companyName);
        HashMap<String, Object> map = new HashMap<>();
        map.put("isOk", true);
        map.put("users", list);
        map.put("msg","重置成功");
        return map;
    }
    @RequestMapping("/loadMyself")
    public Map<String, Object> loadMyself(Integer id, HttpServletRequest request) {
        logger.info("=== 开始加载个人信息 ===");
        
        // 优先从认证头获取用户ID
        String userIdFromHeader = request.getHeader("X-User-Id");
        Integer userId = null;
        
        if (userIdFromHeader != null && !userIdFromHeader.isEmpty()) {
            try {
                userId = Integer.parseInt(userIdFromHeader);
                logger.info("从认证头获取到用户ID: {}", userId);
            } catch (NumberFormatException e) {
                logger.warn("认证头中的用户ID格式错误: {}", userIdFromHeader);
            }
        }
        
        // 如果认证头中没有用户ID，则使用参数中的ID
        if (userId == null) {
            userId = id;
            logger.info("从参数获取到用户ID: {}", userId);
        }
        
        logger.info("最终使用的用户ID: {}", userId);
        
        if (userId == null) {
            logger.warn("用户ID为 null");
            Map<String, Object> map = new HashMap<>();
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        
        logger.info("调用 userService.getUserById，参数: {}", userId);
        
        User user = userService.getUserById(userId);
        logger.info("从数据库查询到的用户信息: {}", user);
        
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("isOk", true);
            map.put("msg", "加载成功");
            map.put("user", user);
            logger.info("成功返回用户信息: name={}, id={}", user.getName(), user.getId());
        } else {
            map.put("isOk", false);
            map.put("msg", "用户不存在");
            logger.warn("未找到 ID 为 {} 的用户", userId);
        }
        
        logger.info("=== 个人信息加载完成 ===");
        return map;
    }

    /**
     * 为前端 findUserById 请求添加的兼容接口
     */
    @RequestMapping("/findUserById")
    public Map<String, Object> findUserById(Integer id, HttpServletRequest request) {
        // 直接复用 loadMyself 的逻辑
        return loadMyself(id, request);
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    public Map<String, Object> updateUserInfo(@RequestParam("id") Integer id,
                              @RequestParam(value = "nickname", required = false) String nickname,
                              @RequestParam(value = "tel", required = false) String tel,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "sex", required = false) String sex,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              @RequestParam(value = "code", required = false) String code) {
        logger.info("(/user/updateUser) 更新用户信息 -> ID: {}, nickname: {}, tel: {}, email: {}, sex: {}, imageUrl: {}",
                id, nickname, tel, email, sex, imageUrl);

        Map<String, Object> map = new HashMap<>();
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }

        try {
            // 创建一个只包含需要更新字段的User对象
            User userToUpdate = new User();
            userToUpdate.setId(id);
            if (nickname != null) userToUpdate.setNickname(nickname);
            if (tel != null) userToUpdate.setTel(tel);
            if (email != null) userToUpdate.setEmail(email);
            if (sex != null) userToUpdate.setSex(sex);
            if (imageUrl != null && !imageUrl.isEmpty()) userToUpdate.setImageUrl(imageUrl);

            logger.info("Web端调用 userService.updateInfoForWeb，用户对象: {}", userToUpdate);
            // Web端使用专门的方法，不需要验证码验证
            boolean flag = userService.updateInfoForWeb(userToUpdate);

            if(flag){
                map.put("isOk", true);
                map.put("msg", "保存成功");
                logger.info("用户信息更新成功，ID: {}", id);
            }else{
                map.put("isOk", false);
                map.put("msg","保存失败");
                logger.warn("用户信息更新失败，ID: {}", id);
            }
            return map;
        } catch (MyException e) {
            logger.error("更新用户信息时发生业务异常，ID: {}", id, e);
            map.put("isOk", false);
            map.put("msg", e.getMessage());
            return map;
        } catch (Exception e) {
            logger.error("更新用户信息时发生系统异常，ID: {}", id, e);
            map.put("isOk", false);
            map.put("msg", "系统异常：" + e.getMessage());
            return map;
        }
    }
    @RequestMapping("/changePwd")
    public Map<String, Object> changePwd(String password, Integer id){
        Map<String, Object> map = new HashMap<>();
        if (id == null) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID不能为空");
            return map;
        }
        boolean isOk = userService.updatePwd(password, id);

        if(isOk){
            map.put("isOk",true);
            map.put("msg","修改成功");

        }else {
            map.put("isOk",false);
            map.put("msg","修改失败");
        }
        return map;
    }
    @RequestMapping(value = "/updateImage", method = RequestMethod.POST)
    public Map<String, Object> updateImage(@RequestParam("id") Integer id, @RequestParam("imageUrl") String imageUrl){
        logger.info("(/user/updateImage) 更新用户头像 -> ID: {}, imageUrl: {}", id, imageUrl);

        Map<String, Object> map = new HashMap<>();
        if (id == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            map.put("isOk", false);
            map.put("msg", "参数错误：用户ID和头像URL不能为空");
            return map;
        }

        User user = new User();
        user.setId(id);
        user.setImageUrl(imageUrl);

        // 直接调用updateInfo，而不是专用的updateImg
        boolean flag = userService.updateInfo(user, null); // 更新头像不需要验证码
        if(flag){
            map.put("isOk", true);
            map.put("msg", "头像修改成功");
            return map;
        }else{
            map.put("isOk", false);
            map.put("msg","头像修改失败");
            return map;
        }
    }
    @RequestMapping("/exit")
    public Map<String, Object> exit(HttpSession session){
        session.removeAttribute("login_user");
        session.invalidate();
        Map<String, Object> map = new HashMap<>();
        map.put("isOk",true);
        map.put("msg","退出成功");
        return map;
    }

    /**
     * 微信小程序登录接口
     * 为小程序用户提供专门的登录认证
     */
    @RequestMapping("/wxLogin")
    public Map<String, Object> wxLogin(@RequestBody Map<String, Object> wxLoginData, HttpSession session, HttpServletRequest request) {
        logger.info("(/user/wxLogin) 微信小程序登录请求 -> 数据: {}", wxLoginData);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从微信登录数据中获取用户信息
            String nickname = (String) wxLoginData.get("nickname");
            String avatarUrl = (String) wxLoginData.get("avatarUrl");
            String phone = (String) wxLoginData.get("phone");
            String password = (String) wxLoginData.get("password");
            
            // 如果提供了手机号和密码，尝试普通登录
            if (phone != null && password != null) {
                User dbUser = userService.checkLoginByPhone(phone, password);
                if (dbUser != null) {
                    // 登录成功，返回用户信息
                    result.put("isOk", true);
                    result.put("msg", "登录成功");
                    result.put("user", dbUser);
                    logger.info("微信小程序用户 '{}' 登录成功", dbUser.getName());
                    return result;
                }
            }
            
            // 如果没有提供手机号密码，或登录失败，创建游客用户
            User guestUser = new User();
            guestUser.setId(0); // 使用特殊ID 0 表示游客用户
            guestUser.setName(nickname != null ? nickname : "微信用户");
            guestUser.setRole("0"); // 角色0表示普通用户（小程序端用户）
            guestUser.setTel(phone);
            guestUser.setImageUrl(avatarUrl);
            
            result.put("isOk", true);
            result.put("msg", "微信登录成功");
            result.put("user", guestUser);
            logger.info("微信小程序游客用户登录成功: {}", guestUser.getName());
            
        } catch (Exception e) {
            logger.error("微信小程序登录失败", e);
            result.put("isOk", false);
            result.put("msg", "登录失败: " + e.getMessage());
        }
        
        return result;
    }
}





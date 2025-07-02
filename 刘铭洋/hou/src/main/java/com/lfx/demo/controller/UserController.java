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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/login")
    public Map<String, Object> login(User user, HttpSession session, HttpServletRequest request) {
        String clientKey = getClientIpAddress(session);
        
        if (loginAttemptService.isBlocked(clientKey)) {
            logger.warn("客户端 '{}' 登录尝试次数过多，暂时被阻止", clientKey);
            throw new MyException("登录尝试次数过多，请稍后再试");
        }
        
        logger.info("(/user/login) 登录请求 -> 用户名: '{}', 密码: '{}'", user.getName(), user.getPwd());
                   
        User dbUser = userService.checkLogin(user);
        Map<String, Object> map = new HashMap<>();
        if(dbUser != null){
            loginAttemptService.loginSucceeded(clientKey);
            
            // 销毁旧会话并创建新会话
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("user", dbUser); // 使用正确的 "user" 键
            
            map.put("isOk", true);
            map.put("msg", "登录成功");
            map.put("user", dbUser);
            logger.info("用户 '{}' 登录成功 (via /user/login), Session 已更新.", dbUser.getName());
            return map;
        } else {
            loginAttemptService.loginFailed(clientKey);
            logger.warn("用户 '{}' 登录失败: 用户名或密码错误 (via /user/login)", user.getName());
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
    //发送验证码
    @RequestMapping ("/send")
    public Map SendMessage(String tel){
        System.out.println("发送验证码");
        return userService.toSendMessage(tel);
    }

    //注册
    @RequestMapping ("/register")
    public Map register(User user){
        User user1=userService.getUserByName(user.getName());
        Map map = new HashMap();
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
    public Map list() {
        List<User> list = userService.findAll();
        Map map = new HashMap();
        map.put("isOK", true);
        map.put("users", list);
        map.put("msg", "加载成功");
        return map;
    }

    @RequestMapping("/add")
    public Map add(User user) {

        boolean isOk = userService.insert(user);
        HashMap map = new HashMap();
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
    public Map delete(@RequestBody List<Integer> ids) {
        boolean isOk = userService.delete(ids);
        Map map = new HashMap();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "删除失败");
        }
        return map;
    }

    @RequestMapping("/delUser")
    public Map deleteUser(String companyName) {
        boolean isOk = userService.deleteUser(companyName);
        Map map = new HashMap();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        }
        return map;
    }

    @RequestMapping("/delUserBySecId")
    public Map deleteUserBySecId(Integer secId) {
        boolean isOk = userService.deleteSectionUser(secId);
        Map map = new HashMap();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        }
        return map;
    }

    @RequestMapping("/update")
    public Map update(User user) {
        boolean isOk = userService.update(user);
        HashMap map = new HashMap();
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
    public Map update(String newName, String oldName) {
        boolean isOk = userService.updateCompanyName(newName,oldName);
        HashMap map = new HashMap();
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
    public Map updateAdmin(String oldadmin, String name) {
        boolean isOk = userService.updateAdminName(oldadmin,name);
        HashMap map = new HashMap();
        if (isOk) {
            map.put("isOk", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping("/updateSection")
    public Map updateAdmin(Integer secId, String section,String companyName) {
        boolean isOk = userService.updateSection(secId,section,companyName);
        HashMap map = new HashMap();
        if (isOk) {
            map.put("isOk", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "修改失败");
        }
        return map;
    }

    @RequestMapping("/updateState")
    public Map updateState(@RequestBody Map<String, Integer> params) {
        Integer state = params.get("state");
        Integer id = params.get("id");
        boolean isOk = userService.updateState(state,id);
        HashMap<String, Object> map = new HashMap();
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
        public Map search(@RequestBody Map<String, String> searchParams) {
            String name = searchParams.get("name");
            String nickname = searchParams.get("nickname");
            String tel = searchParams.get("tel");
            String startDate = searchParams.get("startDate");
            String endDate = searchParams.get("endDate");
            List<User> list = userService.searchUsers(name, nickname, tel, startDate, endDate);
            Map<String, Object> map = new HashMap<>();
            map.put("isOK", true);
            map.put("users", list);
            map.put("msg", "搜索成功");
            return map;
        }

    @RequestMapping ("/search1")
    public Map<String, Object> search1(Integer secId) {
        List<User> list = userService.find(secId);
        HashMap map = new HashMap();
        map.put("isOk", true);
        map.put("users", list);
        map.put("msg", "搜索成功");
        return map;
    }

    @RequestMapping ("/searchuserbycompany")
    public Map searchUserByCompany(String companyName) {
        List<User> list = userService.searchUserByCompany(companyName);
        HashMap map = new HashMap();
        map.put("isOk", true);
        map.put("users", list);
        return map;
    }

    @RequestMapping ("/searchuserbycompanyName")
    public Map searchUserByCompanyName(String companyName) {
        List<User> list = userService.searchUserByCompanyName(companyName);
        HashMap map = new HashMap();
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

    @RequestMapping("/updateUser")
    public Map updateUserInfo(User user) {

        boolean flag = userService.updateInfo(user);
        Map map = new HashMap();
        if(flag){
            map.put("isOk", true);
            map.put("msg", "保存成功");
            return map;
        }else{
            map.put("isOk", false);
            map.put("msg","保存失败");
            return map;
        }


    }
    @RequestMapping("/changePwd")
    public Map changePwd(String password ,HttpSession session){
        User user1= (User) session.getAttribute("login_user");

        if(user1!=null) {
            Integer uid=user1.getId();
            boolean flag = userService.updatePwd(password,uid);
            Map map = new HashMap();
            if (flag) {
                map.put("isOk", true);
                map.put("msg", "修改成功,请重新登录");

            } else {
                map.put("isOk", false);
                map.put("msg", "修改失败");
            }
            return map;
        }else{
            throw new MyException("用户未登录");
        }
    }
    @RequestMapping("/updateImage")
    public Map updateImage(User user){
        System.out.println(user);
        boolean flag = userService.updateImg(user);
        Map map = new HashMap();
        if(flag){
            map.put("isOk", true);
            map.put("msg", "修改成功");
            return map;
        }else{
            map.put("isOk", false);
            map.put("msg","修改失败");
            return map;
        }
    }
    @RequestMapping("/exit")
    public Map exit(HttpSession session){
        session.removeAttribute("login_user");
        session.invalidate();
        Map map = new HashMap();
        map.put("isOk",true);
        map.put("msg","退出成功");
        return map;
    }
}





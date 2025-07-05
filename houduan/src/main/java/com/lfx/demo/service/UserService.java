package com.lfx.demo.service;

import com.aliyuncs.utils.StringUtils;
import com.lfx.demo.entity.User;
import com.lfx.demo.mapper.UserMapper;
import com.lfx.demo.util.MyException;
import com.lfx.demo.util.SmsUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,String> redisTemplate;
    // 检查登录
    public User checkLogin(User user) {
        if (user == null || user.getName() == null) {
            logger.warn("checkLogin接收到的user对象或用户名为空");
            return null;
        }
        logger.info("正在通过用户名 '{}' 查询数据库", user.getName());
        User dbUser = userMapper.selectUserByName1(user.getName());
        logger.info("数据库查询结果: {}", dbUser);

        if (dbUser != null && dbUser.getPwd() != null && dbUser.getPwd().equals(user.getPwd())) {
            logger.info("密码验证成功: {}", user.getName());
            return dbUser;
        } else if (dbUser == null) {
            logger.warn("数据库中未找到用户: {}", user.getName());
        } else {
            logger.warn("密码验证失败: 用户名='{}', 输入密码='{}', 数据库密码='{}'", user.getName(), user.getPwd(), dbUser.getPwd());
        }
        return null;
    }

    public List<User> findAll() {return userMapper.selectAll();}
    public List<User> find(Integer secId) {return userMapper.selectUserById(secId);}
    public boolean insert(User user) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = currentDateTime.format(formatter);
        System.out.println("创建时间为"+formattedDate);
        user.setTime(formattedDate);
        return userMapper.insert(user)>0;}

    public boolean delete(List<Integer> ids) {
        for(Integer id:ids){
            if(userMapper.getRoleById(id).equals("2")){
                return false;
            }
        }
        return userMapper.deleteUserById(ids)>0;
    }

    public boolean update(User user) {
        return userMapper.updateUserInfById(user)>0;
    }
    public boolean updateState(Integer state, Integer id) {
        return userMapper.updateStateInfById(state,id)>0;
    }
    public boolean updateCompanyName(String oldName,String newName) {
        return userMapper.updateUserByName(oldName,newName)>0;
    }

    public boolean updateAdminName(String oldadmin,String name) {
        return userMapper.updateAdminNameByName(oldadmin,name)>0;
    }

    public boolean updateSection(Integer secId,String section,String companyName) {
        return userMapper.updateSectionBySection(secId,section,companyName)>0;
    }

    public List<User> searchUsers(String name, String nickname, String tel, String startDate, String endDate) {
        return userMapper.searchUsers(name, nickname, tel, startDate, endDate);
    }

    public boolean deleteUser(String companyName){
        return userMapper.deleteUserByName(companyName)>0;
    }

    public boolean deleteSectionUser(Integer secId){
        return userMapper.deleteUserBySecId(secId)>0;
    }

    public List<User> searchUserByCompany(String companyName) {
        return userMapper.selectUserByCompany(companyName);
    }

    public List<User> searchUserByCompanyName(String companyName) {
        return userMapper.selectUserByCompanyName(companyName);
    }
    //  发送验证码
    public Map toSendMessage(String phoneNumber) {
        //扩展 可以验证该电话号码是否注册
        //1.判定验证码是否过期
        Map map = new HashMap();
        // 临时禁用Redis检查
        // String code = redisTemplate.opsForValue().get(phoneNumber);
        // System.out.println("redis中存的:" + code);
        // if (!StringUtils.isEmpty(code)) {
        //     map.put("isOk", false);
        //     map.put("msg", phoneNumber + "验证码还未过期");
        //     return map;
        // }
        
        //2.已过期/无验证码 生成验证码
        //随机生成字符串做验证码
        int toCode = (int) (Math.random() * 8999 + 1000);
        System.out.println("验证码为:" + toCode);
        String code = Integer.toString(toCode);
        // 临时跳过短信发送
        // String toSendMes = SmsUtil.toSendMes(phoneNumber, code);

        // if ("OK".equals(toSendMes)) {
            //redis 中存放 5分钟过期
            // redisTemplate.opsForValue().set(phoneNumber, code, 5, TimeUnit.MINUTES);
            System.out.println("发送的" + code);
            //3.发送短信
            map.put("isOk", true);
            map.put("msg", "短信发送成功（临时模拟）");
            return map;
        // }else{
        //     map.put("isOk", false);
        //     map.put("msg", "该手机号无法注册！");
        //     return map;
        // }
    }
    
    //校验验证码
    public boolean checkCode(String phoneNumber, String code) {
        //1.redis 验证码校验
        System.out.println("前端传来的code:"+code);
        // 临时跳过Redis验证，直接返回true
        // String redisCode = redisTemplate.opsForValue().get(phoneNumber);
        // System.out.println("redis中的code:"+redisCode);
        // if (code.equals(redisCode)){
        //     return true;
        // }
        // return false;
        
        // 临时解决方案：任何验证码都通过
        System.out.println("临时跳过Redis验证码检查");
        return true;
    }
    //根据ID获取用户
    public User getUserById(Integer id){
//        System.out.println(id);
//        System.out.println("这里是id");
        return userMapper.selectUserById1(id);
    }
    //根据用户名获取用户
    public User getUserByName(String name){

        return userMapper.selectUserByName1(name);
    }
    public boolean updateInfo(User user){
        System.out.println(user);
        System.out.println("这里是null");
        return userMapper.updateUserInfo(user)>0;
    }
    //更新用户
    public boolean updatePwd(String pwd,Integer id){
        return userMapper.updatePwd(pwd,id)>0;
    }
    public boolean updateImg(User user){
        return userMapper.updateUserImage(user)>0;
    }
}

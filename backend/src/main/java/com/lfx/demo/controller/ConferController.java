package com.lfx.demo.controller;

import com.lfx.demo.entity.Conference;
import com.lfx.demo.entity.User;
import com.lfx.demo.entity.dto.ConferenceAuditDTO;
import com.lfx.demo.mapper.ConferMapper;
import com.lfx.demo.service.ConferService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/confer")
public class ConferController {
    @Autowired
    private ConferService biz;
    private ConferMapper mapper;
    
    @RequestMapping("/list")
    public Map findAll(HttpSession session){
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("login_user");
        Map map = new HashMap();
        
        if (currentUser == null) {
            map.put("isOk", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        List<Conference> confers = biz.getConferList(currentUser.getName(), currentUser.getRole());
        map.put("isOk", true);
        map.put("confers", confers);
        map.put("msg", "会议列表数据加载成功！");
        return map;
    }

    /**
     * 获取审核列表接口
     * 只有超级管理员（role=3）可以访问
     */
    @RequestMapping("/auditList")
    public Map getAuditList(HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有超级管理员可以查看审核列表");
                return result;
            }

            List<Conference> auditConfers = biz.getAuditConferences();
            result.put("isOk", true);
            result.put("auditConfers", auditConfers);
            result.put("msg", "加载成功");
            
        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "获取审核列表失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 会议审核接口
     * 只有超级管理员（role=3）可以进行审核
     */
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    public Map auditConference(@RequestBody ConferenceAuditDTO auditDTO, HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 打印接收到的参数用于调试
            System.out.println("接收到的会议审核请求: " + auditDTO.toString());
            
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有超级管理员可以进行审核操作");
                return result;
            }

            // 参数验证
            if (auditDTO.getConferName() == null || auditDTO.getConferName().trim().isEmpty()) {
                result.put("isOk", false);
                result.put("msg", "会议名称不能为空");
                return result;
            }

            if (auditDTO.getStatus() == null || auditDTO.getStatus().trim().isEmpty()) {
                result.put("isOk", false);
                result.put("msg", "审核状态不能为空");
                return result;
            }

            // 执行审核逻辑
            boolean success = false;
            String message = "";
            
            if ("通过".equals(auditDTO.getStatus())) {
                success = biz.auditConference(auditDTO.getConferName(), "通过");
                message = success ? "会议审核通过" : "会议审核失败，该会议可能已被删除";
            } else if ("不通过".equals(auditDTO.getStatus())) {
                success = biz.auditConference(auditDTO.getConferName(), "不通过");
                if (success) {
                    message = "会议审核不通过";
                    if (auditDTO.getRemark() != null && !auditDTO.getRemark().trim().isEmpty()) {
                        message += "，原因：" + auditDTO.getRemark();
                    }
                } else {
                    message = "会议审核失败，该会议可能已被删除";
                }
            } else {
                result.put("isOk", false);
                result.put("msg", "无效的审核状态，只支持'通过'或'不通过'");
                return result;
            }

            result.put("isOk", success);
            result.put("msg", message);

        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "审核操作失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 发布会议到审核队列
     * 普通管理员（role=1）和企业管理员（role=2）可以发布
     */
    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public Map publishConferences(@RequestBody List<String> conferNames, HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 打印接收到的参数用于调试
            System.out.println("接收到的会议发布请求: " + conferNames);
            
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null) {
                result.put("isOk", false);
                result.put("msg", "用户未登录");
                return result;
            }

            // 检查用户角色（假设普通管理员和企业管理员都可以发布）
            // 如果有具体的角色字段，可以进一步验证
            
            // 参数验证
            if (conferNames == null || conferNames.isEmpty()) {
                result.put("isOk", false);
                result.put("msg", "请选择要发布的会议");
                return result;
            }

            // 执行发布操作
            boolean success = biz.publishConferences(conferNames);
            
            if (success) {
                result.put("isOk", true);
                result.put("msg", "发布成功，请等待超级管理员审核");
            } else {
                result.put("isOk", false);
                result.put("msg", "发布失败");
            }

        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "发布操作失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取待审核会议列表接口
     * @return 待审核的会议列表
     */
    @RequestMapping("/pendingList")
    public Map getPendingList(HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有超级管理员可以查看待审核列表");
                return result;
            }

            List<Conference> pendingConfers = biz.getAuditConferences();
            result.put("isOk", true);
            result.put("data", pendingConfers);
            result.put("msg", "获取待审核会议列表成功");
            
        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "获取待审核会议列表失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 审核通过会议
     */
    @RequestMapping(value = "/approve/{id}", method = RequestMethod.POST)
    public Map approveConference(@PathVariable Integer id, HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有超级管理员可以进行审核操作");
                return result;
            }

            // 通过ID查找会议并审核通过
            boolean success = biz.approveConferenceById(id);
            
            if (success) {
                result.put("isOk", true);
                result.put("msg", "会议审核通过");
            } else {
                result.put("isOk", false);
                result.put("msg", "会议审核失败，该会议可能已被删除");
            }

        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "会议审核失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 审核驳回会议
     */
    @RequestMapping(value = "/reject/{id}", method = RequestMethod.POST)
    public Map rejectConference(@PathVariable Integer id, HttpSession session) {
        Map result = new HashMap<>();
        
        try {
            // 验证用户权限
            User user = (User) session.getAttribute("login_user");
            if (user == null || !user.getName().equals("admin")) {
                result.put("isOk", false);
                result.put("msg", "权限不足，只有超级管理员可以进行审核操作");
                return result;
            }

            // 通过ID查找会议并审核驳回
            boolean success = biz.rejectConferenceById(id);
            
            if (success) {
                result.put("isOk", true);
                result.put("msg", "会议审核驳回");
            } else {
                result.put("isOk", false);
                result.put("msg", "会议审核失败，该会议可能已被删除");
            }

        } catch (Exception e) {
            result.put("isOk", false);
            result.put("msg", "会议审核失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @RequestMapping("/search")
    public Map search(@RequestBody Map<String, String> searchParams, HttpSession session) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("login_user");
        Map<String, Object> map = new HashMap<>();
        
        if (currentUser == null) {
            map.put("isOk", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        String conferName = searchParams.get("conferName");
        String creater = searchParams.get("creater");
        String stime = searchParams.get("stime");
        String content = searchParams.get("content");
        
        // 使用当前用户信息进行搜索
        List<Conference> list = biz.getConfer(
            conferName, 
            creater, 
            stime, 
            content, 
            currentUser.getName(),  // 当前用户名
            currentUser.getRole()   // 当前用户角色
        );
        
        if (list != null) {
            map.put("isOk", true);
            map.put("confers", list);
            map.put("msg", "查询成功！");
        } else {
            map.put("isOk", false);
            map.put("msg", "查询失败，未找到相关会议！");
        }
        return map;
    }
    
    @PostMapping("/del")
    public Map del(@RequestBody List<String> conferName){
        System.out.println("del!!!!");
        System.out.println(conferName);
        boolean isOk = biz.removeConfer(conferName);
        Map map = new HashMap();
        if(isOk){
            System.out.println("delyes!!!!");
            map.put("isOk",true);
            map.put("msg","删除成功");
        }else{
            System.out.println("delno!!!!");
            map.put("isOk",false);
            map.put("msg","删除失败");
        }
        return map;
    }
    
    @PostMapping("/add")
    public Map add(@RequestBody Conference confer, HttpSession session){
        User user = (User) session.getAttribute("login_user");
        Map<String, Object> map = new HashMap<>();
        if(user == null){
            map.put("isOk", false);
            map.put("msg", "用户未登录");
            return map;
        }

        // 自动设置发布者为当前登录用户
        confer.setPublisher(user.getName());

        boolean isOk = biz.addConfer(confer);
        if(isOk){
            map.put("isOk",true);
            map.put("msg","添加成功");
        }else{
            map.put("isOk",false);
            map.put("msg","添加失败");
        }
        return map;
    }
    
    @PostMapping("/update")
    public Map update(@RequestBody Conference confer){
        if(confer==null) System.out.println("confer为空!");
        boolean isOk = biz.modifyConfer(confer);
        Map map = new HashMap();
        if(isOk){
            map.put("isOk",true);
            map.put("msg","修改成功");
        }else{
            map.put("isOk",false);
            map.put("msg","修改失败");
        }
        return map;
    }
    
    public void setBiz(ConferService biz) {
        this.biz = biz;
    }
}
package com.lfx.demo.controller;

import com.lfx.demo.entity.Company;
import com.lfx.demo.entity.User;
import com.lfx.demo.service.CompanyService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @RequestMapping("/list")
    public Map list(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute("login_user");
        Map map = new HashMap();
        
        if (currentUser == null) {
            map.put("isOK", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        String userRole = currentUser.getRole();
        String userCompany = currentUser.getCompanyName();
        
        System.out.println("公司列表请求 - 用户: " + currentUser.getName() + 
                          ", 角色: " + userRole + 
                          ", 所属公司: " + userCompany);
        
        List<Company> list;
        
        // 超级管理员可以查看所有公司
        if ("3".equals(userRole)) {
            list = companyService.findAll();
        }
        // 企业管理员和普通管理员只能查看自己的公司
        else if ("2".equals(userRole) || "1".equals(userRole)) {
            if (userCompany != null) {
                Company company = companyService.findCompanyByName(userCompany);
                list = company != null ? List.of(company) : List.of();
            } else {
                list = List.of();
            }
        }
        // 未知角色，拒绝访问
        else {
            map.put("isOK", false);
            map.put("msg", "权限不足");
            return map;
        }
        
        map.put("isOK", true);
        map.put("comps", list);
        map.put("msg", "加载成功");
        return map;
    }

    @RequestMapping("add")
    public Map add( Company company) {
        System.out.println("Inserting company: " + company);
        boolean isOk = companyService.insert(company);
        HashMap map = new HashMap();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "添加成功");
        } else {
            map.put("isOK", false);
            map.put("msg", "公司名称已被注册！！！");
        }
        return map;
    }

    @RequestMapping("/update")
    public Map update( Company company, String oldname) {
        System.out.println("Updating company: " + company+"oldname:"+oldname);
        boolean isOk = companyService.update(company,oldname);
        HashMap map = new HashMap();
        if (isOk) {
            map.put("isOk", true);
            map.put("msg", "修改成功");
        } else {
            map.put("isOk", false);
            map.put("msg", "修改的公司名已被注册！");
        }
        return map;
    }

    @RequestMapping("/del")
    public Map delete(@RequestBody List<Integer> ids) {
        boolean isOk = companyService.delete(ids);
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

    @RequestMapping("/find")
    public Map find(String name, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = (User) request.getSession().getAttribute("login_user");
        Map map = new HashMap();
        
        if (currentUser == null) {
            map.put("isOk", false);
            map.put("msg", "用户未登录");
            return map;
        }
        
        // 根据用户角色进行权限控制
        String userRole = currentUser.getRole();
        String userCompany = currentUser.getCompanyName();
        
        System.out.println("公司查找请求 - 用户: " + currentUser.getName() + 
                          ", 角色: " + userRole + 
                          ", 所属公司: " + userCompany + 
                          ", 查找公司: " + name);
        
        // 超级管理员可以查找任何公司
        if ("3".equals(userRole)) {
            Company company = companyService.findCompanyByName(name);
            map.put("isOk", true);
            map.put("company", company);
            return map;
        }
        
        // 企业管理员和普通管理员只能查找自己所属的公司
        if ("2".equals(userRole) || "1".equals(userRole)) {
            if (userCompany != null && userCompany.equals(name)) {
                Company company = companyService.findCompanyByName(name);
                map.put("isOk", true);
                map.put("company", company);
                return map;
            } else {
                map.put("isOk", false);
                map.put("msg", "权限不足，您只能查看自己所属的公司信息");
                return map;
            }
        }
        
        // 未知角色，拒绝访问
        map.put("isOk", false);
        map.put("msg", "权限不足");
        return map;
    }

    @RequestMapping("/search")
    public Map search(@RequestBody Map<String, String> searchParams) {
        String name = searchParams.get("name");
        String linkman = searchParams.get("linkman");
        String tel = searchParams.get("tel");
        String sign = searchParams.get("sign");
        List<Company> list = companyService.searchCompanies(name, linkman, tel, sign);
        Map<String, Object> map = new HashMap<>();
        map.put("isOK", true);
        map.put("comps", list);
        map.put("msg", "搜索成功");
        return map;
    }
}

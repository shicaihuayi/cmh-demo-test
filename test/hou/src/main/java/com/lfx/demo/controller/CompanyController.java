package com.lfx.demo.controller;
import com.lfx.demo.entity.Company;
import com.lfx.demo.service.CompanyService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map list() {
        List<Company> list = companyService.findAll();
        Map map = new HashMap();
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
    public Map find( String name) {
        Company company=companyService.findCompanyByName(name);
        Map map = new HashMap();
            map.put("isOk", true);
            map.put("company", company);
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

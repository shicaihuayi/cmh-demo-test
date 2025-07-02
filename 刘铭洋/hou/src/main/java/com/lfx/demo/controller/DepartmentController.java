package com.lfx.demo.controller;
import com.lfx.demo.entity.Department;
import com.lfx.demo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @RequestMapping("/add")
    public Map addDartment(  Department department){
        Map result = new HashMap<>();
        int error = departmentService.addDepartment(department);
        switch (error) {
            case 0:
                result.put("isOk",true);
                result.put("msg","添加成功");
                break;
            case 1:
                result.put("isOk",false);
                result.put("msg","添加失败,与同一个父部门的部门排序重复");
                break;
            case 2:
                result.put("isOk",false);
                result.put("msg","添加失败,与同一个父部门的部门名称重复");
                break;
            case 3:
                result.put("isOk",false);
                result.put("msg","数据库添加失败");
                break;
        }
        return result;
    }

    @RequestMapping("/delete")
    public Map deleteDartment(  Integer departmentId){
        Map result = new HashMap<>();
        if(departmentService.deleteDepartment(departmentId)){
            result.put("isOk",true);
            result.put("msg","删除成功");
        }else{
            result.put("isOk",false);
            result.put("msg","删除失败");
        }
        return result;
    }

    @RequestMapping("/del")
    public Map delete(  String companyName) {
        boolean isOk = departmentService.deleteDepartmentByName(companyName);
        Map map = new HashMap();
        if (isOk) {
            map.put("isOK", true);
            map.put("msg", "删除成功");
        }
        return map;
    }

    @RequestMapping("/search")
    public Map searchDartment(  String departmentName, Integer state){
        Map result = new HashMap<>();
        System.out.println(departmentName);
        System.out.println(state);
        List<Department> list = departmentService.searchDepartment(departmentName, state);
        if(list!=null){
            result.put("isOk",true);
            result.put("msg","查找成功");
            result.put("list",list);
        }else{
            result.put("isOk",false);
            result.put("msg","查找失败，未找到符合条件的部门");
        }
        return result;
    }

    @RequestMapping("/companydata")
    public Map getCompanyData(  String name){
        Map result = new HashMap<>();
        List<Department> list = departmentService.selectDepartmentByCompanyName(name);
        result.put("isOk",true);
        result.put("msg","读取成功");
        result.put("list",list);
        return result;
    }

    @RequestMapping("/updateName")
    public Map update(  String newName, String oldName) {
        boolean isOk = departmentService.updateCompanyName(newName,oldName);
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

    @RequestMapping("/departmentdata")
    public Map getDepartmentById(  Integer departmentId){
        Map result = new HashMap<>();
        Department dbDepartment = departmentService.getDepartmentById(departmentId);
        if(dbDepartment != null){
            result.put("isOk",true);
            result.put("msg","读取成功");
            result.put("departmentId",dbDepartment.getDepartmentId());
            result.put("departmentName",dbDepartment.getDepartmentName());
            result.put("order",dbDepartment.getOrder());
            result.put("director",dbDepartment.getDirector());
            result.put("tel",dbDepartment.getTel());
            result.put("mail",dbDepartment.getMail());
            result.put("state",dbDepartment.getState());
        }else{
            result.put("isOk",false);
            result.put("msg","读取失败");
        }
        return result;
    }

    @RequestMapping("/modify")
    public Map modifyDartment(  Department department){
        Map result = new HashMap<>();
        int error = departmentService.modifyDepartment(department);
        switch (error) {
            case 0:
                result.put("isOk",true);
                result.put("msg","修改成功");
                break;
            case 1:
                result.put("isOk",false);
                result.put("msg","修改失败,与同一个父部门的部门排序重复");
                break;
            case 2:
                result.put("isOk",false);
                result.put("msg","修改失败,与同一个父部门的部门名称重复");
                break;
            case 3:
                result.put("isOk",false);
                result.put("msg","数据库修改失败");
                break;
            case 4:
                result.put("isOk",false);
                result.put("msg","该部门有子部门，无法修改上级部门");
                break;
        }
        return result;
    }
}

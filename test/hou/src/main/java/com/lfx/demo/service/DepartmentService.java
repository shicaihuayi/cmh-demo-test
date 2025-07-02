package com.lfx.demo.service;
import com.lfx.demo.entity.Department;
import com.lfx.demo.mapper.DepartmentMapper;
import com.lfx.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private UserMapper  userMapper;
    public void setUserMapper(UserMapper userMapper) {this.userMapper = userMapper;}

    public void setDepartmentMapper(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public int addDepartment(Department department) {
        Department dbDepartment = new Department();
        int id = 0;
        if (department.getDepartmentLevel() != 1) {
            if (departmentMapper.selectDepartmentByOrderAndSuperior(department.getOrder()
                    , department.getSuperior(), id) != null) {
                return 1; // 排序重复
            }
            dbDepartment = departmentMapper.selectDepartmentBySuperiorAndName(department.getSuperior()
                    , department.getDepartmentName(), id);
        } else {
            if (departmentMapper.selectDepartmentByOrderAndCompany(department.getOrder()
                    , department.getCompanyName(), id) != null) {
                return 1; // 排序重复
            }
            dbDepartment = departmentMapper.selectDepartmentByCompanyAndName(department.getCompanyName()
                    , department.getDepartmentName(), id);
        }
        if (dbDepartment != null) {
            return 2; // 名称重复
        }
        if (departmentMapper.addDepartment(department) <= 0) {
            return 3; // 数据库添加失败
        }
        return 0; // 正常执行
    }

    public boolean deleteDepartment(Integer departmentId) {
        return departmentMapper.deleteDepartmentById(departmentId) > 0;
    }

    public boolean deleteDepartmentByName(String companyName) {
        System.out.println(companyName);
        return departmentMapper.deleteDepartmentByName(companyName) > 0;
    }

    public List<Department> searchDepartment(String departmentName, Integer state) {
        if (!departmentName.isEmpty() && (state == 1 || state == 0)) {
            System.out.println("有名"+state);
            return departmentMapper.searchDepartmentByNameAndState(departmentName, state);
        } else if (departmentName.isEmpty() && (state == 1 || state == 0)) {
            System.out.println("无名"+state);
            return departmentMapper.searchDepartmentByState(state);
        } else if (!departmentName.isEmpty()) {
            System.out.println("有名");
            return departmentMapper.searchDepartmentByName(departmentName);
        } else {
            return null;
        }
    }

    public Department getDepartmentById(Integer departmentId) {
        return departmentMapper.getDepartmentById(departmentId);
    }

    public List<Department> selectDepartmentByCompanyName(String name) {
        System.out.println(name);
        return departmentMapper.selectDepartmentByCompanyName(name);
    }

    public boolean updateCompanyName(String oldName, String newName) {
        return departmentMapper.updateDepByName(oldName, newName) > 0;
    }

    public int modifyDepartment(Department department) {
        Department dbDepartment = departmentMapper.getDepartmentById(department.getDepartmentId());
        if (department.getDepartmentLevel() != 1) { // 非1级部门
            if (dbDepartment.getSuperior().equals(department.getSuperior())) { // 上级不变
                if (!(dbDepartment.getOrder().equals(department.getOrder()))) { // 排序变化
                    if (departmentMapper.selectDepartmentByOrderAndSuperior(department.getOrder()
                            , department.getSuperior(), department.getDepartmentId()) != null) {
                        return 1; // 排序重复
                    }
                }
                if (!(dbDepartment.getDepartmentName().equals(department.getDepartmentName()))) { // 名称变化
                    dbDepartment = departmentMapper.selectDepartmentBySuperiorAndName(department.getSuperior()
                            , department.getDepartmentName(), department.getDepartmentId());
                    if (dbDepartment != null) {
                        return 2; // 名称重复
                    }
                }
            } else { // 上级变化
                if (departmentMapper.selectDepartmentByOrderAndSuperior(department.getOrder()
                        , department.getSuperior(), department.getDepartmentId()) != null) {
                    return 1; // 排序重复
                }
                dbDepartment = departmentMapper.selectDepartmentBySuperiorAndName(department.getSuperior()
                        , department.getDepartmentName(), department.getDepartmentId());
                if (dbDepartment != null) {
                    return 2; // 名称重复
                }
            }
        } else { // 1级部门
            if (dbDepartment.getCompanyName().equals(department.getCompanyName()) && dbDepartment.getSuperior() == 0) { // 上级不变
                if (!(dbDepartment.getOrder().equals(department.getOrder()))) { // 排序变化
                    if (departmentMapper.selectDepartmentByOrderAndCompany(department.getOrder()
                            , department.getCompanyName(), department.getDepartmentId()) != null) {
                        return 1; // 排序重复
                    }
                }
                if (!(dbDepartment.getDepartmentName().equals(department.getDepartmentName()))) { // 名称变化
                    dbDepartment = departmentMapper.selectDepartmentByCompanyAndName(department.getCompanyName()
                            , department.getDepartmentName(), department.getDepartmentId());
                    if (dbDepartment != null) {
                        return 2; // 名称重复
                    }
                }
            } else { // 上级变化
                if (departmentMapper.selectDepartmentByOrderAndCompany(department.getOrder()
                        , department.getCompanyName(), department.getDepartmentId()) != null) {
                    return 1; // 排序重复
                }
                dbDepartment = departmentMapper.selectDepartmentByCompanyAndName(department.getCompanyName()
                        , department.getDepartmentName(), department.getDepartmentId());
                if (dbDepartment != null) {
                    return 2; // 名称重复
                }
            }
        }
        if (departmentMapper.updateDepartment(department) <= 0) {
            return 3; // 数据库添加失败
        }
        System.out.println(department.getCompanyName());
        UpdateSuperiorDFS(department.getDepartmentLevel(), department.getDepartmentId(), department.getCompanyName());
        return 0; // 正常执行
    }

    private void UpdateSuperiorDFS(Integer superiorLevel, Integer superior, String companyName) {
        List<Department> childs = departmentMapper.selectDepartmentBySuperior(superior);
        if (!childs.isEmpty()) {
            Integer departmentLevel = superiorLevel+1;
            for (Department child : childs) {
                Integer departmentId = child.getDepartmentId();
                userMapper.updateSection(companyName,child.getDepartmentId());
                departmentMapper.updateSuperiorById(superior, departmentLevel, departmentId, companyName);
                UpdateSuperiorDFS(departmentLevel+1, child.getDepartmentId(), companyName);
            }
        }

    }

}

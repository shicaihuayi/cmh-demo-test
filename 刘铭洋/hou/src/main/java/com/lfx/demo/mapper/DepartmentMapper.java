package com.lfx.demo.mapper;
import com.lfx.demo.entity.Department;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    @Insert("insert into t_department values(null,#{superior},#{departmentName}" +
            ",#{order},#{director},#{tel},#{mail},1,#{departmentLevel},#{companyName})")
    int addDepartment(Department department);


    @Delete("delete from t_department where departmentId=#{departmentId}")
    int deleteDepartmentById(Integer departmentId);


    @Select("select * from t_department where departmentId=#{departmentId}")
    Department getDepartmentById(Integer departmentId);

    @Select("select * from t_department where superior=#{superior} and departmentName=#{departmentName}" +
            " and not departmentid=#{departmentId}")
    Department selectDepartmentBySuperiorAndName(Integer superior, String departmentName, Integer departmentId);

    @Select("select * from t_department where `order`=#{order} and superior=#{superior}" +
            " and not departmentid=#{departmentId}")
    Department selectDepartmentByOrderAndSuperior(Integer order, Integer superior, Integer departmentId);

    @Select("select * from t_department where companyName=#{companyName} and departmentName=#{departmentName}" +
            " and not departmentid=#{departmentId} and departmentlevel=1")
    Department selectDepartmentByCompanyAndName(String companyName, String departmentName, Integer departmentId);

    @Select("select * from t_department where `order`=#{order} and companyName=#{companyName}" +
            " and not departmentid=#{departmentId} and departmentlevel=1")
    Department selectDepartmentByOrderAndCompany(Integer order, String companyName, Integer departmentId);

    @Select("select * from t_department where departmentName like concat('%',#{departmentName},'%') and state=#{state}")
    List<Department> searchDepartmentByNameAndState(String departmentName, Integer state);

    @Select("select * from t_department where departmentName like concat('%',#{departmentName},'%')")
    List<Department> searchDepartmentByName(String departmentName);

    @Select("select * from t_department where state=#{state}")
    List<Department> searchDepartmentByState(Integer state);

    @Select("select * from t_department where companyName=#{companyName} order by departmentlevel, `order`")
    List<Department> selectDepartmentByCompanyName(String companyName);

    @Select("select * from t_department where superior=#{superior} ")
    List<Department> selectDepartmentBySuperior(Integer superior);


    @Delete("delete from t_department where companyName=#{companyName}")
    int deleteDepartmentByName(String companyName);

    @Update("update t_department SET companyName=#{newName} WHERE companyName = #{oldName}")
    int updateDepByName(String newName,String oldName);

    @Update("update t_department SET superior=#{superior}, departmentlevel=#{departmentLevel}" +
            ", companyname=#{companyName} WHERE departmentid = #{departmentId}")
    void updateSuperiorById(@Param("superior")Integer superior, @Param("departmentLevel")Integer departmentLevel
            , @Param("departmentId")Integer departmentId, @Param("companyName")String companyName);

    @Update("update t_department set superior=#{superior},departmentName=#{departmentName},`order`=#{order}" +
            ",director=#{director},tel=#{tel},mail=#{mail},state=#{state},departmentLevel=#{departmentLevel}" +
            ",companyName=#{companyName} where departmentId=#{departmentId}")
    int updateDepartment(Department department);

}

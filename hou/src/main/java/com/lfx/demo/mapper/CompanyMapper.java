package com.lfx.demo.mapper;
import com.lfx.demo.entity.Company;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;


@Mapper
public interface CompanyMapper {
    @Insert("insert into t_company values (#{name},#{tel},#{admin},#{linkman},#{sign},#{desc},null,#{state},#{mail},#{coverUrl})")
    int insert(Company company);
    @Select("select * from t_company")
    List<Company> selectAll();
    @Select("select * from t_company where name=#{name}")
    Company selectCompanyByName(String name);
    @Select("select * from t_company where name=#{oldname}")
    Company selectCompanyByOldName(String oldname);
    @Update("update t_company SET name=#{name},linkman = #{linkman},`desc`=#{desc}, admin=#{admin},sign=#{sign},tel=#{tel},mail=#{mail},coverUrl=#{coverUrl} WHERE id = #{id}")
    int updateComInfById(Company company);

    @Delete("<script>" +
            "DELETE FROM t_company WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteComById(@Param("ids") List<Integer> ids);

    @SelectProvider(type = CompanySqlBuilder.class, method = "buildSearchQuery")
    List<Company> searchCompanies(@Param("name") String name, @Param("linkman") String linkman, @Param("tel") String tel, @Param("sign") String sign);

    class CompanySqlBuilder {
        public static String buildSearchQuery(@Param("name") String name, @Param("linkman") String linkman, @Param("tel") String tel, @Param("sign") String sign) {
            StringBuilder sql = new StringBuilder("SELECT * FROM t_company WHERE 1=1");
            if (name != null && !name.isEmpty()) {
                sql.append(" AND name LIKE CONCAT('%', #{name}, '%')");
            }
            if (linkman != null && !linkman.isEmpty()) {
                sql.append(" AND linkman LIKE CONCAT('%', #{linkman}, '%')");
            }
            if (tel != null && !tel.isEmpty()) {
                sql.append(" AND tel LIKE CONCAT('%', #{tel}, '%')");
            }
            if (sign != null && !sign.isEmpty()) {
                sql.append(" AND sign LIKE CONCAT('%', #{sign}, '%')");
            }
            return sql.toString();
        }
    }

}

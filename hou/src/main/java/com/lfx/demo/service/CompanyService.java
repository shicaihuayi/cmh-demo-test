package com.lfx.demo.service;
import com.lfx.demo.entity.Company;
import com.lfx.demo.mapper.CompanyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CompanyService {
    @Autowired
    private CompanyMapper companyMapper;
    public List<Company> findAll() {return companyMapper.selectAll();}
    public boolean insert(Company company) {
        System.out.println("Inserting company: " + company);
        Company dbCompany = companyMapper.selectCompanyByName(company.getName());
        return dbCompany==null&&companyMapper.insert(company)>0;
    }
    public boolean update(Company company,String oldname) {
        Company dbCompany = companyMapper.selectCompanyByName(company.getName());
        Company com=companyMapper.selectCompanyByOldName(oldname);

        if(dbCompany==null){
            boolean res = companyMapper.updateComInfById(company)>0;
            return res;
        }else if(dbCompany.getName().equals(com.getName())){
            boolean res = companyMapper.updateComInfById(company)>0;
            return res;
        }
        return false;
    }
    public List<Company> searchCompanies(String name, String linkman, String tel, String sign) {
        return companyMapper.searchCompanies(name, linkman, tel, sign);
    }
    public boolean delete(List<Integer> ids) {return companyMapper.deleteComById(ids)>0;}

    public Company findCompanyByName(String name) {return companyMapper.selectCompanyByName(name);}
}

package com.lfx.demo.entity;

import java.io.Serializable;

public class Department implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer departmentId;
    private Integer superior;
    private String departmentName;
    private Integer order;
    private String director;
    private String tel;
    private String mail;
    private Integer state;
    private Integer departmentLevel;
    private String companyName;

    public Department(){};

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getSuperior() {
        return superior;
    }

    public void setSuperior(Integer superior) {
        this.superior = superior;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getDepartmentLevel() {
        return departmentLevel;
    }

    public void setDepartmentLevel(Integer departmentLevel) {
        this.departmentLevel = departmentLevel;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

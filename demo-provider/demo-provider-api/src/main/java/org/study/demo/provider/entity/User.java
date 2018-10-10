package org.study.demo.provider.entity;

import org.study.common.statics.annotations.PK;
import org.study.common.statics.pojos.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class User extends BaseEntity {
    /**
     * 主键
     */
    @PK
    private Long id;

    /**
     * 创建时间，默认为当前时间
     */
    private Date createTime = new Date();

    /**
     * 版本号，默认为0
     */
    private long version = 0;
    /**
     * 用户编号
     */
    private String userNo;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 年收入
     */
    private BigDecimal annualIncome;
    /**
     * 是否程序员
     */
    private Integer isProgrammer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public Integer getIsProgrammer() {
        return isProgrammer;
    }

    public void setIsProgrammer(Integer isProgrammer) {
        this.isProgrammer = isProgrammer;
    }
}

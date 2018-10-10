package org.study.demo.provider.entity;

import org.study.common.statics.annotations.PK;
import org.study.common.statics.pojos.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

public class UserExt extends BaseEntity {
    /**
     * 用户编号
     */
    @PK
    private String userNo;
    /**
     * 创建时间，默认为当前时间
     */
    private Date createTime = new Date();

    /**
     * 版本号，默认为0
     */
    private long version = 0;
    /**
     * 地址
     */
    private String address;
    /**
     * 身高(cm)
     */
    private Integer height;
    /**
     * 体重(kg)
     */
    private BigDecimal weight;

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}

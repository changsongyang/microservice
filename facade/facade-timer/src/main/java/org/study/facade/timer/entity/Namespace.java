package org.study.facade.timer.entity;

import org.study.common.statics.annotations.PK;

import java.io.Serializable;
import java.util.Date;

public class Namespace implements Serializable {
    private static final long serialVersionUID = 43493636351475483L;
    @PK
    private String namespace;
    private Integer status;
    private Date createTime = new Date();
    private Date updateTime = new Date();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

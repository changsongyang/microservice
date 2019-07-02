package org.study.timer.api.entity;

import org.study.common.statics.annotations.PK;

import java.io.Serializable;
import java.util.Date;

public class InstanceStage implements Serializable {
    private static final long serialVersionUID = 43493635241245483L;

    public final static int RUNNING_STAGE = 1;
    public final static int STAND_BY_STAGE = 2;

    @PK
    private Long id;
    private Date createTime = new Date();

    private String instanceId;
    private Integer status;
    private String remark = "";
    private Date updateTime;

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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}

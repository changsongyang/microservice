package org.study.facade.timer.entity;

import org.study.common.statics.annotations.PK;

import java.io.Serializable;
import java.util.Date;

public class Instance implements Serializable {
    private static final long serialVersionUID = 43493635241245483L;
    @PK
    private String instanceId;
    private Date createTime = new Date();
    private String namespace;
    private Integer status;
    private String remark = "";
    private Date updateTime;

    /**
     * 命名空间的状态，属于业务冗余字段
     */
    private Integer namespaceStatus;

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

    public Integer getNamespaceStatus() {
        return namespaceStatus;
    }

    public void setNamespaceStatus(Integer namespaceStatus) {
        this.namespaceStatus = namespaceStatus;
    }
}

package org.study.demo.shutdown.hook.provider.dao;

import org.study.common.statics.pojos.BaseEntity;

public class User extends BaseEntity {
    private Long id;
    private Long version;
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

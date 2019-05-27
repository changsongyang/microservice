package org.study.demo.nacos.api.vo;

import java.io.Serializable;

public class HelloVo implements Serializable {
    private Integer count;
    private String description;
    private String content;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

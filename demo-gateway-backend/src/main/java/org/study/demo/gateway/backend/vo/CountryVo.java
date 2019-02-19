package org.study.demo.gateway.backend.vo;

import java.io.Serializable;

public class CountryVo implements Serializable {
    private String cnName;
    private String enName;
    private Integer num;
    private String reqText;
    private String repText;

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getReqText() {
        return reqText;
    }

    public void setReqText(String reqText) {
        this.reqText = reqText;
    }

    public String getRepText() {
        return repText;
    }

    public void setRepText(String repText) {
        this.repText = repText;
    }
}

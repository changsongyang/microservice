package org.study.demo.gateway.backend.vo;

import java.util.List;

public class BatchVo {
    private String batchNo;
    private Integer totalCount;
    private String requestTime;
    private String totalAmount;
    private List<DetailVo> details;

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<DetailVo> getDetails() {
        return details;
    }

    public void setDetails(List<DetailVo> details) {
        this.details = details;
    }
}

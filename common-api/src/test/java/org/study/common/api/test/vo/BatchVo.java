package org.study.common.api.test.vo;

import java.util.List;

public class BatchVo {
    private String batch_no;
    private Integer total_count;
    private String request_time;
    private String total_amount;
    private List<DetailVo> details;

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public Integer getTotal_count() {
        return total_count;
    }

    public void setTotal_count(Integer total_count) {
        this.total_count = total_count;
    }

    public String getRequest_time() {
        return request_time;
    }

    public void setRequest_time(String request_time) {
        this.request_time = request_time;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public List<DetailVo> getDetails() {
        return details;
    }

    public void setDetails(List<DetailVo> details) {
        this.details = details;
    }
}

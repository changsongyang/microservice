package org.study.demo.gateway.backend.vo;

import java.io.Serializable;
import java.util.List;

public class ProductVo implements Serializable {
    private String price;
    private String productCode;
    private String productName;
    private String reqText;
    private String repText;
    private List<DetailVo> details;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public List<DetailVo> getDetails() {
        return details;
    }

    public void setDetails(List<DetailVo> details) {
        this.details = details;
    }
}

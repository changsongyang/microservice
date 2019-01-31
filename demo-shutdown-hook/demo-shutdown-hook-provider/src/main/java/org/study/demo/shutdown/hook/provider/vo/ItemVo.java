package org.study.demo.shutdown.hook.provider.vo;

import java.math.BigDecimal;

public class ItemVo {
    private String itemNo;
    private BigDecimal itemAmount;

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public BigDecimal getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount;
    }
}

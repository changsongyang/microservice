package org.study.demo.shutdown.hook.api.vo;

import org.study.common.statics.vo.MessageVo;

import java.math.BigDecimal;
import java.util.List;

public class OrderVo extends MessageVo {
    private BigDecimal amount;
    private boolean isFinish;
    List<ItemVo> itemVoList;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(boolean finish) {
        isFinish = finish;
    }

    public List<ItemVo> getItemVoList() {
        return itemVoList;
    }

    public void setItemVoList(List<ItemVo> itemVoList) {
        this.itemVoList = itemVoList;
    }
}

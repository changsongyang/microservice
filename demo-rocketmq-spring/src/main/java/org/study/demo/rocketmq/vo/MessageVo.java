package org.study.demo.rocketmq.vo;

public class MessageVo {
    /**
     * 交易流水号
     */
    private String trxNo;
    /**
     * 消息类型(如：支付完成、结算完成等)
     */
    private long msgType;

    public String getTrxNo() {
        return trxNo;
    }

    public void setTrxNo(String trxNo) {
        this.trxNo = trxNo;
    }

    public long getMsgType() {
        return msgType;
    }

    public void setMsgType(long msgType) {
        this.msgType = msgType;
    }
}

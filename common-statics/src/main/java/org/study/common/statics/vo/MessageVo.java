package org.study.common.statics.vo;

public class MessageVo {
    /**
     * 主题
     */
    private String topic;

    /**
     * tags
     */
    private String tags;

    /**
     * 消息类型(如：支付完成、结算完成等)
     */
    private long msgType;

    /**
     * 交易流水号/业务流水号
     */
    private String trxNo;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

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
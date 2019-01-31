package org.study.demo.shutdown.hook.provider.rmq;

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
     * 交易流水号
     */
    private String trxNo;

    /**
     * 消息的业务编码
     */
    private String msgKey;


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

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }
}

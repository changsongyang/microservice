package org.study.common.mq.conts;

public class LoggerConst {
    public static final String ROCKETMQ_NOT_OK_LOGGER_NAME = "rmqNotOkLogger";
    /**
     * 可能失败的消息在Redis存储时的KEY
     */
    public static final String REDIS_STORE_SEND_NOT_OK_KEY = "ROCKET_MQ_MAY_SEND_FAIL_MESSAGE_KEY";
}

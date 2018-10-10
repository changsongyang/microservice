package org.study.common.mq;

import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.StringUtil;

import javax.annotation.PostConstruct;

/**
 * @description RocketMQ的NameServer配置地址
 * @author chenyf on 2018-03-18
 */
public class NameServerAddress {
    private String addresses;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    @PostConstruct
    public void init(){
        if(StringUtil.isEmpty(addresses)){
            throw new BizException("addresses is empty");
        }
    }
}

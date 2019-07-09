package org.study.common.api.service;

import org.study.common.api.params.RequestParam;

/**
 * 验证失败之后的处理器(包括验签、验IP等)，如：发送邮件通知或短信预警，需要用户自行实现，如果不需要此功能也可以不实现，
 * @author chenyf
 * @date 2018-12-15
 */
public interface ValidFailService {

    /**
     * 验签失败之后的处理
     * @param uid           唯一id标识
     * @param requestIp     用户请求的IP
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     * @param cause         验签失败的异常，可能为null
     */
    public void afterSignValidFail(String uid, String requestIp, RequestParam requestParam, Throwable cause);

    /**
     * IP校验失败之后的处理
     * @param uid           唯一id标识
     * @param requestIp     用户请求的IP
     * @param expectIp      实际要求的IP，可能为null
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     */
    public void afterIpValidFail(String uid, String requestIp, String expectIp, RequestParam requestParam);
}

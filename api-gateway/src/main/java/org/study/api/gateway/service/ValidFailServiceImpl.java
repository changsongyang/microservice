package org.study.api.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.api.params.RequestParam;
import org.study.common.api.service.ValidFailService;
import org.study.common.util.utils.JsonUtil;

/**
 * @description 网关校验失败之后的处理器：如：IP校验、签名校验 等
 * @author: chenyf
 * @Date: 2019-02-20
 */
public class ValidFailServiceImpl implements ValidFailService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 验签失败之后的处理，如果是发送通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次
     * @param requestIp     用户请求的IP
     * @param routeId       定义路由的id
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     * @param cause         验签失败的异常，可能为null
     */
    public void afterSignValidFail(String routeId, String requestIp, RequestParam requestParam, Throwable cause){
        logger.info("routeId={} requestIp={} RequestParam={} 验签失败", routeId, requestIp, JsonUtil.toString(requestParam), cause);
    }

    /**
     * IP校验失败之后的处理，如果是发送通知，需注意限制发送通知的频率，如：同一个商户每分钟不超过5次
     * @param routeId       定义路由的id
     * @param requestIp     用户请求的IP
     * @param expectIp      实际要求的IP，可能为null
     * @param requestParam  用户的请求数据，当用户没有传入数据时为null
     */
    public void afterIpValidFail(String routeId, String requestIp, String expectIp, RequestParam requestParam){
        logger.info("routeId={} requestIp={} expectIp={} RequestParam={} IP校验失败", routeId, requestIp, expectIp, JsonUtil.toString(requestParam));
    }
}

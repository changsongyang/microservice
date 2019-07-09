package org.study.common.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.study.common.util.utils.StringUtil;

import java.util.List;

public class RequestUtil {
    private static Logger logger = LoggerFactory.getLogger(RequestUtil.class);
    private static final String HEADER_X_FORWARD = "x-forwarded-for";
    private static final String HEADER_PROXY_CLIENT = "Proxy-Client-IP";
    private static final String HEADER_WL_PROXY_CLIENT = "WL-Proxy-Client-IP";
    private static final String HEADER_UNKNOWN_VALUE = "unknown";
    private static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCAL_IPV4 = "127.0.0.1";

    public static String getRequestPath(ServerHttpRequest request){
        return request.getURI().getPath();
    }

    /**
     * 获取客户端的IP地址
     *
     * @param request 请求参数
     * @return
     */
    public static String getIpAddr(ServerHttpRequest request) {
        String ipAddress;

        HttpHeaders httpHeaders = request.getHeaders();
        List<String> values = httpHeaders.get(HEADER_X_FORWARD);
        ipAddress = (values==null || values.size() <= 0) ? "" : values.get(0);
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            values = httpHeaders.get(HEADER_PROXY_CLIENT);
            ipAddress = (values==null || values.size() <= 0) ? "" : values.get(0);
        }
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            values = httpHeaders.get(HEADER_WL_PROXY_CLIENT);
            ipAddress = (values==null || values.size() <= 0) ? "" : values.get(0);
        }
        if (StringUtil.isEmpty(ipAddress) || HEADER_UNKNOWN_VALUE.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddress().getAddress().getHostAddress();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (StringUtil.isNotEmpty(ipAddress) && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        if (LOCAL_IPV6.equals(ipAddress) || StringUtil.isEmpty(ipAddress)) {
            ipAddress = LOCAL_IPV4;
        }
        return ipAddress;
    }
}

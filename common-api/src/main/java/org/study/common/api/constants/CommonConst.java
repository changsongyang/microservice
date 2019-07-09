package org.study.common.api.constants;

/**
 * 公共常量类
 * @author: chenyf
 * @Date: 2018-12-15
 */
public class CommonConst {
    public static final String ENCODING_UTF_8 = "UTF-8";

    public static final String SYS_ERROR_MSG = "SYS_ERROR";

    public static final String ERROR_PATH = "/error";

    /**
     * SpringMVC在HttpServletRequest中存放请求体的key
     */
    public static final String REQUEST_STORE_OBJECT_KEY_PREFIX = "REQUEST_STORE_OBJECT_KEY_PREFIX.";
    /**
     * 网关转发请求到后端服务之前，往http header中存放mch_no的key
     */
    public static final String REQUEST_HEADER_STORE_MCHNO_KEY = "REQUEST-MCHNO-KEY";
    /**
     * 网关转发请求到后端服务之前，往http header中存放sign_type的key
     */
    public static final String REQUEST_HEADER_STORE_SIGNTYPE_KEY = "REQUEST-SIGNTYPE-KEY";
}

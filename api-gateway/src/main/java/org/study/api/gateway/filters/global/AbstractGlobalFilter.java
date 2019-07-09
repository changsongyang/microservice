package com.gw.api.gateway.filters.global;

import com.gw.api.gateway.config.conts.ReqCacheKey;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

/**
 * @description 全局过滤器抽象类，负责处理子类全局过滤器的一些公共逻辑
 * @author chenyf
 * @date 2019-02-23
 */
public abstract class AbstractGlobalFilter implements GlobalFilter, Ordered {
    protected static final String CACHE_REQUEST_BODY_OBJECT_KEY = ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY;
}

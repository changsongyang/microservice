package com.gw.api.gateway.filters.global;

import com.gw.api.base.enums.BizCodeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.utils.StringUtil;
import com.gw.api.gateway.config.ApiConfig;
import com.gw.api.gateway.config.conts.FilterOrder;
import com.gw.api.gateway.config.conts.InnerErrorCode;
import com.gw.api.gateway.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * @description IP黑名单过滤器
 * @author chenyf
 * @date 2019-02-23
 */
@Component
public class IPBlackListFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(IPBlackListFilter.class);
    @Autowired
    ApiConfig apiConfig;

    @Override
    public int getOrder() {
        return FilterOrder.PRE_NONE_BIZ_FIRST;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = RequestUtil.getIpAddr(exchange.getRequest());
        if(StringUtil.isEmpty(apiConfig.getIpBlackListPattern()) || isPass(apiConfig.getIpBlackListPattern(), ip)){
            return chain.filter(exchange);
        }

        logger.warn("ip = {} 被列为黑名单，禁止访问！ pattern = {}", ip, apiConfig.getIpBlackListPattern());
        throw ApiException.acceptFail(BizCodeEnum.PARAM_VALID_FAIL.getCode(), BizCodeEnum.PARAM_VALID_FAIL.getMsg())
                .innerCode(InnerErrorCode.IP_BLACK_LIST);
    }

    private boolean isPass(String pattern, String ip){
        if(Pattern.matches(pattern, ip)){
            return false;
        }else{
            return true;
        }
    }
}

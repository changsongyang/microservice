package com.gw.api.gateway.filters.global;

import com.gw.api.base.enums.BizCodeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.helpers.RequestHelper;
import com.gw.api.base.params.APIParam;
import com.gw.api.base.params.RequestParam;
import com.gw.api.base.service.ValidFailService;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.gateway.config.conts.FilterOrder;
import com.gw.api.gateway.config.conts.InnerErrorCode;
import com.gw.api.gateway.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @description 请求体鉴权校验，包括：签名校验 等等
 * @author chenyf
 * @date 2019-02-23
 */
@Component
public class RequestAuthFilter extends AbstractGlobalFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RequestHelper requestHelper;
    @Autowired
    ValidFailService validFailService;

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序建议为第3个，因为，如果鉴权不通过，就没有必要进行后续的过滤器处理了
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.PRE_THIRD;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        boolean isVerifyOk = false;//默认为false，勿改此默认值
        String ip = RequestUtil.getIpAddr(request);
        RequestParam requestParam = (RequestParam) exchange.getAttributes().get(CACHE_REQUEST_BODY_OBJECT_KEY);
        Throwable cause = null;

        try{
            RequestHelper.Result<Throwable> result = requestHelper.signVerify(requestParam, new APIParam(requestParam.getVersion()));
            isVerifyOk = result.isVerifyOk();
            cause = result.getOtherInfo();
        }catch (Throwable e){
            logger.error("签名校验失败 RequestParam = {}", JsonUtil.toString(requestParam), e);
        }

        if(! isVerifyOk){
            try{
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                validFailService.afterSignValidFail(route.getId(), ip, requestParam, cause);
            }catch(Throwable e){
                logger.error("验签失败，验签失败后处理器有异常 RequestParam = {}", JsonUtil.toString(requestParam), e);
            }
        }

        if(isVerifyOk){
            return chain.filter(exchange);
        }else{
            //抛出异常，由全局异常处理器来处理响应信息
            throw ApiException.acceptFail(BizCodeEnum.SIGN_VALID_FAIL.getCode(), BizCodeEnum.SIGN_VALID_FAIL.getMsg())
                    .innerCode(InnerErrorCode.SIGN_VALID_ERROR);
        }
    }
}

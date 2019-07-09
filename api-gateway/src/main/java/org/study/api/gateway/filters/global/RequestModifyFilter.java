package org.study.api.gateway.filters.global;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import org.study.api.gateway.config.conts.FilterOrder;
import org.study.common.api.constants.CommonConst;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.exceptions.ApiException;
import org.study.common.api.helpers.RequestHelper;
import org.study.common.api.params.APIParam;
import org.study.common.api.params.RequestParam;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @description 修改请求体，包括：aes_key解密 等
 * @author chenyf
 * @date 2019-02-23
 */
@Component
public class RequestModifyFilter extends AbstractGlobalFilter {
    @Autowired
    RequestHelper requestHelper;

    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序建议为第4个
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.PRE_FOURTH;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestParam requestParam = (RequestParam) exchange.getAttributes().get(CACHE_REQUEST_BODY_OBJECT_KEY);

        secKeyDecrypt(requestParam);
        modifyData(requestParam);

        //1.重新封装请求体
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.set(CommonConst.REQUEST_HEADER_STORE_MCHNO_KEY, requestParam.getMch_no());
        headers.set(CommonConst.REQUEST_HEADER_STORE_SIGNTYPE_KEY, requestParam.getSign_type());
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);//强制设置为application/json;charset=UTF-8
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        String bodyStr = JsonUtil.toString(requestParam);
        Mono<String> modifiedBody = Mono.just(bodyStr);

        //2.更新缓存的内容
        exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_KEY, requestParam);
        //3.更新body
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> {
                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public HttpHeaders getHeaders() {
                            long contentLength = headers.getContentLength();
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.putAll(headers);
                            if (contentLength > 0) {
                                httpHeaders.setContentLength(contentLength);
                            } else {
                                httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                            }
                            return httpHeaders;
                        }
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return outputMessage.getBody();
                        }
                    };
                    return chain.filter(exchange.mutate().request(decorator).build());
                }));
    }

    private boolean secKeyDecrypt(RequestParam requestParam){
        if(StringUtil.isEmpty(requestParam.getSec_key())){
            return false;
        }

        try{
            //如果sec_key不为空，则对sec_key进行解密，解密完成之后，再把RequestParam重新设置回缓存
            requestHelper.secKeyDecrypt(requestParam, new APIParam(requestParam.getVersion()));
            return true;
        }catch(Throwable ex){
            throw ApiException.acceptFail(BizCodeEnum.PARAM_VALID_FAIL.getCode(), "sec_key解密失败");
        }
    }

    private void modifyData(RequestParam requestParam){
        try{
            requestParam.setData(JSON.parseObject(requestParam.getData().toString(), Feature.OrderedField));
        }catch(Throwable ex){
            throw ApiException.acceptFail(BizCodeEnum.PARAM_VALID_FAIL.getCode(), "data序列化失败，清确保为JSON格式，且没有特殊字符");
        }
    }
}

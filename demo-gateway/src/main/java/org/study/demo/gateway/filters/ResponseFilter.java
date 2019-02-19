package org.study.demo.gateway.filters;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.MD5Util;
import org.study.common.util.utils.RandomUtil;
import org.study.demo.gateway.param.ResponseParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Component
public class ResponseFilter implements GlobalFilter, Ordered {
    @Override
    public int getOrder() {
        return -2;  // -1 is response write filter, must be called before that
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        //获取响应体内容
                        byte[] content = getResponseBody(dataBuffer);

                        //对响应体进行签名
                        ResponseParam response = JsonUtil.toBean(content, ResponseParam.class);
                        response.setSign(RandomUtil.get32LenStr());
                        byte[] newRs = JsonUtil.toString(response, SerializerFeature.WriteMapNullValue).getBytes(Charset.forName("UTF-8"));

                        originalResponse.getHeaders().setContentLength(newRs.length);//如果不重新设置长度则收不到消息。
                        return bufferFactory.wrap(newRs);
                    }));
                }
                // if body is not a flux. never got there.
                return super.writeWith(body);
            }
        };
        // replace response with decorator
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private byte[] getResponseBody(DataBuffer dataBuffer){
        byte[] content;
        try{
            content = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(content);
        } finally {
            // 释放掉内存
            DataBufferUtils.release(dataBuffer);
        }
        return content;
    }
}

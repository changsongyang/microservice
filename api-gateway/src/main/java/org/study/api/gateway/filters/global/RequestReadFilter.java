package org.study.api.gateway.filters.global;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.study.api.gateway.config.conts.FilterOrder;
import org.study.common.api.params.RequestParam;
import org.study.common.util.utils.JsonUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @description 读请求体，并转换成RequestParam，之后再放到缓存中存起来，这个过滤器必须是第1个执行，不然，后续的过滤器无法获得请求参数
 * @author chenyf
 * @date 2019-02-23
 */
@Component
public class RequestReadFilter extends AbstractGlobalFilter{
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    /**
     * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序必须为第1个，不然，后续的过滤器无法获得请求参数
     * @return
     */
    @Override
    public int getOrder() {
        return FilterOrder.PRE_FIRST;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getHeaders().getContentType() == null) {
            return chain.filter(exchange);
        } else {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        //Update the retain counts so we can read the body twice, once to parse into an object
                        //that we can test the predicate against and a second time when the HTTP client sends
                        //the request downstream
                        //Note: if we end up reading the body twice we will run into a problem, but as of right
                        //now there is no good use case for doing this
                        DataBufferUtils.retain(dataBuffer);
                        //Make a slice for each read so each read has its own read/write indexes
                        Flux<DataBuffer> cachedFlux = Flux
                                .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));

                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };

                        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                        return ServerRequest.create(mutatedExchange, messageReaders)
                                .bodyToMono(String.class)
                                .doOnNext(objectValue -> {
                                    RequestParam requestParam = JsonUtil.toBeanOrderly(objectValue, RequestParam.class);
                                    exchange.getAttributes().put(CACHE_REQUEST_BODY_OBJECT_KEY, requestParam);
                                }).then(chain.filter(mutatedExchange));
                    });
        }
    }
}

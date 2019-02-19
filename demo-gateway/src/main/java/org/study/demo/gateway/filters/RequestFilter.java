package org.study.demo.gateway.filters;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import org.study.demo.gateway.param.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RequestFilter implements GlobalFilter, Ordered {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();

        //取得请求体数据
        String bodyStr = getRequestBody(serverHttpRequest);
        RequestParam requestParam = JsonUtil.toBeanOrderly(bodyStr, RequestParam.class);//一定要维持顺序，否则会导致验签失败

        //TODO 得到Post请求的请求参数后，做你想做的事
        requestParam.setAes_key("888888888888888888888");

        //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
        URI newUri = getRewriteUri(serverHttpRequest.getURI(), requestParam.getMethod());
        ServerHttpRequest request = serverHttpRequest.mutate().uri(newUri).build();
        DataBuffer bodyDataBuffer = stringBuffer(JsonUtil.toString(requestParam));
        Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

        request = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return bodyFlux;
            }
        };

        //封装request，传给下一级
        return chain.filter(exchange.mutate().request(request).build());
    }

    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * @return 请求体
     */
    private String getRequestBody(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();

        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        return bodyRef.get();
    }

    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    private URI getRewriteUri(URI uri, String method){
        String path = "/";
        if(StringUtil.isNotEmpty(method)){
            path = "/" + method.replace(".", "/");
        }

        String uriStr = uri.toString();
        if(uriStr.endsWith("/")){
            uriStr = uriStr.substring(0, uriStr.length()-1);
        }

        try{
            return new URI(uriStr + path);
        }catch(Exception e){
            throw new BizException(e);
        }
    }
}

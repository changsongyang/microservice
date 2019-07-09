package com.gw.api.gateway.ratelimit;

import com.gw.api.base.params.RequestParam;
import com.gw.api.base.utils.MD5Util;
import com.gw.api.gateway.config.conts.ReqCacheKey;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @description 以 path + mchNo 作为限流的key
 * @author chenyf
 * @date 2019-02-23
 */
public class PathKeyResolver implements KeyResolver {
    private static final String PATH_KEY_RESOLVER_FLAG = ":resolvePath";
    /**
     * 以 path + mchNo 作为限流的key
     *
     *  假如有下面的请求入参：
     *      1、请求uri为： 127.0.0.1:8080/test
     *      2、JSON请求体中method为：demo.country
     *      3、商户号为：88880000088123
     *  则返回的key值为：/test/demo/country:88880000088123
     *
     * @param exchange
     * @return
     */
    public Mono<String> resolve(ServerWebExchange exchange){
        RequestParam requestParam = (RequestParam) exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
        String mchNo = requestParam.getMch_no();

        StringBuilder pathBuilder = new StringBuilder();
        String originalPath = (String) exchange.getAttributes().get(ReqCacheKey.GATEWAY_ORIGINAL_REQUEST_PATH_ATTR);
        String path = exchange.getRequest().getURI().getPath();
        if(originalPath != null){//有经过RewritePathFilter全局过滤器的时候originalPath就不会为null
            pathBuilder.append(originalPath);
            if(! path.startsWith("/") && ! originalPath.endsWith("/")){
                pathBuilder.append("/");
            }
        }
        pathBuilder.append(path).append(mchNo);
        String key = MD5Util.getMD5Hex(pathBuilder.toString());//用md5压缩长度
        return Mono.just(key + PATH_KEY_RESOLVER_FLAG + path);
    }

    public static String getPathFromKey(String key){
        if(key == null || key.indexOf(PATH_KEY_RESOLVER_FLAG) < 0){
            return null;
        }
        int index = key.indexOf(PATH_KEY_RESOLVER_FLAG) + PATH_KEY_RESOLVER_FLAG.length();
        return key.substring(index);
    }
}

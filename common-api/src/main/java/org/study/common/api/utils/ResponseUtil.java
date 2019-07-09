package org.study.common.api.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.api.params.ResponseParam;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 响应处理工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseUtil {

    public static void fillAcceptUnknownIfEmpty(ResponseParam response){
        if(StringUtil.isEmpty(response.getResp_code())){
            response.setResp_code(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
        }
        if(StringUtil.isEmpty(response.getBiz_code())){
            response.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getMsg());
        }
    }

    public static Mono<Void> writeResponse(ServerHttpResponse response, ResponseParam responseParam){
        byte[] data = JsonUtil.toString(responseParam).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = response.bufferFactory().wrap(data);
        return response.writeWith(Mono.just(dataBuffer));
    }
}

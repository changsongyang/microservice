package com.gw.api.gateway.utils;

import com.gw.api.base.enums.RespCodeEnum;
import com.gw.api.base.params.ResponseParam;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.base.utils.StringUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 响应处理工具类，可通过HttpServletResponse对响应信息进行修改等操作
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseUtil {

    public static void fillAcceptUnknownIfEmpty(ResponseParam response){
        if(StringUtil.isEmpty(response.getResp_code())){
            response.setResp_code(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
        }
        if(StringUtil.isEmpty(response.getResp_msg())){
            response.setResp_msg(RespCodeEnum.ACCEPT_UNKNOWN.getMsg());
        }
    }

    public static Mono<Void> writeResponse(ServerHttpResponse response, ResponseParam responseParam){
        byte[] data = JsonUtil.toString(responseParam).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = response.bufferFactory().wrap(data);
        return response.writeWith(Mono.just(dataBuffer));
    }
}

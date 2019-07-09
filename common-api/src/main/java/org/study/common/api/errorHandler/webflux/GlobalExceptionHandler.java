package com.gw.api.base.errorHandler.webflux;

import com.gw.api.base.constants.CommonConst;
import com.gw.api.base.enums.BizCodeEnum;
import com.gw.api.base.enums.RespCodeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.params.RequestParam;
import com.gw.api.base.params.ResponseParam;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.base.utils.RequestUtil;
import com.gw.api.base.vo.BizCodeVo;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用webflux时的全局异常处理器
 */
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {
    protected static final String HTTP_STATUS_KEY = "httpStatus";

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                  ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    protected Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
        boolean includeStackTrace = isIncludeStackTrace(request, MediaType.ALL);
        Map<String, Object> error = getErrorAttributes(request, includeStackTrace);
        HttpStatus errorStatus = getHttpStatus(error);
        if(error.containsKey(HTTP_STATUS_KEY)){
            error.remove(HTTP_STATUS_KEY);
        }

        return ServerResponse.status(HttpStatus.OK)//统一返回200响应码
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(error))
                .doOnNext((resp) -> logError(request, errorStatus));
    }

    /**
     * 获取异常属性
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Throwable error = super.getError(request);
        return buildResponse(request, error);
    }

    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        HttpStatus httpStatus;
        if(errorAttributes.containsKey(HTTP_STATUS_KEY) && errorAttributes.get(HTTP_STATUS_KEY) != null){
            httpStatus = HttpStatus.valueOf((int) errorAttributes.get(HTTP_STATUS_KEY));
        }else{
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    private Map<String, Object> buildResponse(ServerRequest request, Throwable ex) {
        HttpStatus httpStatus = getHttpStatus(ex);
        RequestParam requestParam = getRequestParam(request);

        ResponseParam response = getResponseParam(requestParam, ex);

        Map<String, Object> map = new HashMap<>();
        map.put(HTTP_STATUS_KEY, httpStatus.value());
        Field[] fields = ResponseParam.class.getDeclaredFields();
        for(int i=0; i<fields.length; i++){
            Field filed = fields[i];
            String name = filed.getName();

            filed.setAccessible(true);
            try{
                map.put(name, filed.get(response));
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private ResponseParam getResponseParam(RequestParam requestParam, Throwable ex){
        String mchNo = requestParam.getMch_no();
        String signType = requestParam.getSign_type();
        ResponseParam response;
        BizCodeVo bizVo = new BizCodeVo();

        if (ex instanceof ApiException) {
            ApiException e = (ApiException) ex;
            response = new ResponseParam();
            response.setResp_code(e.getRespCode());
            response.setResp_msg(e.getRespMsg());

            bizVo.setBiz_code(e.getBizCode());
            bizVo.setBiz_msg(e.getBizMsg());
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            response = new ResponseParam();
            response.setResp_code(RespCodeEnum.ACCEPT_FAIL.getCode());
            response.setResp_msg(RespCodeEnum.ACCEPT_FAIL.getMsg());

            bizVo.setBiz_code(BizCodeEnum.PARAM_VALID_FAIL.getCode());
            bizVo.setBiz_msg(responseStatusException.getMessage());
        } else {
            bizVo.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
            bizVo.setBiz_msg(BizCodeEnum.ACCEPT_UNKNOWN.getMsg() + ", Internal Server Error");
            response = ResponseParam.acceptUnknown(mchNo);
        }

        response.setMch_no(mchNo);
        response.setSign_type(signType);
        response.setData(bizVo);
        return response;
    }

    private HttpStatus getHttpStatus(Throwable ex){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if(ex instanceof ApiException){
            httpStatus = HttpStatus.OK;
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            httpStatus = responseStatusException.getStatus();
        }
        return httpStatus;
    }

    private RequestParam getRequestParam(ServerRequest request){
        List<String> mchs = request.headers().header(CommonConst.REQUEST_HEADER_STORE_MCHNO_KEY);
        List<String> signTypes = request.headers().header(CommonConst.REQUEST_HEADER_STORE_SIGNTYPE_KEY);

        RequestParam requestParam = new RequestParam();
        requestParam.setMch_no((mchs==null || mchs.isEmpty()) ? "" : mchs.get(0));
        requestParam.setSign_type((signTypes==null || signTypes.isEmpty()) ? "" : signTypes.get(0));
        return requestParam;
    }
}
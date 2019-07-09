package org.study.api.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import org.study.api.gateway.config.conts.InnerErrorCode;
import org.study.api.gateway.config.conts.ReqCacheKey;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.api.exceptions.ApiException;
import org.study.common.api.helpers.RequestHelper;
import org.study.common.api.params.APIParam;
import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.utils.ResponseUtil;
import org.study.common.util.utils.JsonUtil;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @description 全局异常处理器
 * @author chenyf
 * @date 2019-02-23
 */
public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(JsonExceptionHandler.class);
    protected static final String HTTP_STATUS_KEY = "httpStatus";

    @Autowired
    RequestHelper requestHelper;

    public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        boolean includeStackTrace = isIncludeStackTrace(request, MediaType.ALL);
        Map<String, Object> error = getErrorAttributes(request, includeStackTrace);
        HttpStatus errorStatus = getHttpStatus(error);
        if(error.containsKey(HTTP_STATUS_KEY)){
            error.remove(HTTP_STATUS_KEY);
        }

        return ServerResponse.status(errorStatus)
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

    /**
     * 根据code获取对应的HttpStatus
     * @param errorAttributes
     */
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

    protected void logError(ServerRequest request, HttpStatus errorStatus) {
        Throwable ex = super.getError(request);
        RequestParam requestParam = (RequestParam) request.attributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);
        if(ex instanceof ApiException){
            logger.error("网关处理过程中出现业务判断异常 Exception = {} RequestParam = {}", ex.getMessage(), JsonUtil.toString(requestParam));
        }else{
            logger.error("网关处理过程中出现未预期异常 RequestParam = {} ", JsonUtil.toString(requestParam), ex);
        }
    }

    /**
     * 构建返回的JSON数据格式，此处是处理gateway处理过程中本身的异常，后端服务的异常不会进入到这里
     *
     * @param request          请求体
     * @param ex               异常信息
     * @return
     */
    private Map<String, Object> buildResponse(ServerRequest request, Throwable ex) {
        HttpStatus httpStatus = getHttpStatus(ex);
        RequestParam requestParam = (RequestParam) request.attributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);

        ResponseParam response = getResponseParam(requestParam, ex);
        try{
            requestHelper.signAndEncrypt(response, new APIParam(requestParam.getVersion()));
        }catch(Throwable e){
            if(response.getSign() == null){
                response.setSign("");
            }
            logger.error("异常处理器中，添加签名时出现异常 RequestParam = {} ResponseParam = {}", JsonUtil.toString(requestParam), JsonUtil.toString(response), e);
        }

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
        String mchNo = requestParam == null ? "" : requestParam.getMch_no();
        String signType = requestParam == null ? "" : requestParam.getSign_type();
        ResponseParam response;

        if (ex instanceof ApiException) {
            ApiException e = (ApiException) ex;
            response = new ResponseParam();
            response.setSign("");//默认为空串做签名内容
            response.setResp_code(e.getRespCode());
            response.setBiz_code(e.getBizCode());
            if(e.getInnerCode() == InnerErrorCode.IP_BLACK_LIST){
                response.setBiz_msg("Not Allow!");
            }else{
                response.setBiz_msg(e.getBizMsg());
            }
        } else if(ex instanceof TimeoutException){
            response = ResponseParam.acceptUnknown(mchNo);
            response.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
            response.setBiz_msg("Time Out");
        }else if(ex instanceof NotFoundException){//后端服务无法从注册中心被发现时
            response = new ResponseParam();
            response.setSign("");//默认为空串做签名内容
            response.setResp_code(RespCodeEnum.ACCEPT_FAIL.getCode());
            response.setBiz_code(BizCodeEnum.PARAM_VALID_FAIL.getCode());
            response.setBiz_msg("Service Not Found");
        } else if (ex instanceof ResponseStatusException) {//访问没有配置的route path时
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            response = new ResponseParam();
            response.setSign("");//默认为空串做签名内容
            response.setResp_code(RespCodeEnum.ACCEPT_FAIL.getCode());
            response.setBiz_code(BizCodeEnum.PARAM_VALID_FAIL.getCode());
            response.setBiz_msg(responseStatusException.getMessage());
        } else {
            response = ResponseParam.acceptUnknown(mchNo);
            response.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
            response.setBiz_msg(BizCodeEnum.ACCEPT_UNKNOWN.getMsg() + ", Internal Server Error");
        }

        ResponseUtil.fillAcceptUnknownIfEmpty(response);

        response.setMch_no(mchNo);
        response.setSign_type(signType);
        return response;
    }

    private HttpStatus getHttpStatus(Throwable ex){
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if(ex instanceof ApiException){
            ApiException e = (ApiException) ex;
            if(InnerErrorCode.PARAM_CHECK_ERROR == e.getInnerCode()){
                httpStatus = HttpStatus.BAD_REQUEST;
            }else if(InnerErrorCode.SIGN_VALID_ERROR == e.getInnerCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(InnerErrorCode.IP_VALID_ERROR == e.getInnerCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }else if(InnerErrorCode.RATE_LIMIT_ERROR == e.getInnerCode()){
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
            }else if(InnerErrorCode.IP_BLACK_LIST == e.getInnerCode()){
                httpStatus = HttpStatus.FORBIDDEN;
            }
        }else if (ex instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            httpStatus = responseStatusException.getStatus();
        }
        return httpStatus;
    }
}

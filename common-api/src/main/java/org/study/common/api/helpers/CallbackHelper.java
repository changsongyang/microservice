package com.gw.api.base.helpers;

import com.gw.api.base.enums.RespCodeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.params.APIParam;
import com.gw.api.base.params.ResponseParam;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.base.utils.StringUtil;
import com.gw.api.base.vo.CallBackRespVo;
import com.gw.api.base.vo.CallBackResult;
import com.gw.api.base.vo.ResponseVo;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CallbackHelper {
    private WebClient webClient;
    private WebClient webClientSsl;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RequestHelper requestHelper;

    public CallbackHelper(RequestHelper requestHelper){
        this.requestHelper = requestHelper;
    }

    /**
     * 发起回调，异步处理响应结果
     * @param url               回调地址
     * @param response          回调内容
     * @param param             是否需要对商户响应信息进行验签
     * @param onComplete        响应完成之后的回调处理器
     * @return
     */
    public void callBackAsync(String url, ResponseVo response, APIParam param, Consumer<CallBackResult> onComplete){
        this.doCallback(url, response, param).subscribe(onComplete);
    }

    /**
     * 发起回调，同步等待响应结果
     * @param url           回调地址
     * @param response      回调内容
     * @param timeoutMills  等待超时时间
     * @param param         是否需要对商户响应信息进行验签
     * @return
     */
    public CallBackResult callBackSync(String url, ResponseVo response, long timeoutMills, APIParam param){
        Mono<CallBackResult> monoResult = doCallback(url, response, param);

        try{
            return monoResult.block(Duration.ofMillis(timeoutMills));
        }catch (RuntimeException e){//超时异常
            CallBackRespVo respVo = new CallBackRespVo();
            respVo.setHttpStatus(HttpStatus.REQUEST_TIMEOUT.value());
            respVo.setResp_code(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
            respVo.setResp_msg(e.getMessage());
            CallBackResult result = CallBackResult.from(respVo);
            return result;
        }
    }

    private Mono<CallBackResult> doCallback(String uri, ResponseVo response, APIParam param){
        ResponseParam responseParam = JsonUtil.toBean(JsonUtil.toStringUnderline(response), ResponseParam.class);
        if(responseParam == null){
            throw new ApiException("ResponseParam为null，请确定ResponseVo不为null");
        }

        try{
            requestHelper.signAndEncrypt(responseParam, param);
        }catch(Throwable e){
            throw new ApiException("签名异常,请检查传入的参数是否正确！", e);
        }
        if(StringUtil.isEmpty(responseParam.getSign())){
            throw new ApiException("签名失败,请检查传入的参数是否正确！");
        }

        boolean signVerify = param != null && param.getRespSignVerify();
        Mono<String> mono = this.doPost(uri, responseParam);
        Mono<CallBackResult> monoResult = mono.map(respStr -> {
            CallBackRespVo respVo = JsonUtil.toBean(respStr, CallBackRespVo.class);
            if(respVo == null){
                //出现这种情况时，只能是商户无内容返回或返回内容的格式不正确导致的，因为在this.doPost()中有针对异常的处理
                respVo = CallBackRespVo.defaultResp();
            }
            if(respVo.getHttpStatus() == 0){
                respVo.setHttpStatus(HttpStatus.OK.value());
            }
            CallBackResult result = CallBackResult.from(respVo);
            result.setSignPass(false);//默认为false

            //验签
            if(signVerify && StringUtil.isNotEmpty(respVo.getSign())){
                try{
                    RequestHelper.Result<Throwable> verifyResult = requestHelper.signVerify(respVo, param);
                    result.setSignPass(verifyResult.isVerifyOk());
                    if(verifyResult.getOtherInfo() != null){
                        result.setMsg(result.getMsg() + "," + verifyResult.getOtherInfo().getMessage());
                    }
                }catch (Throwable e){
                    logger.error("响应信息的签名校验失败 ResponseParam = {} CallBackRespVo = {}", JsonUtil.toString(responseParam), JsonUtil.toString(respVo), e);
                    result.setSignPass(false);
                }
            }
            return result;
        }).doOnNext(result -> result.setSignVerify(signVerify));

        return monoResult;
    }

    private Mono<String> doPost(String uri, Object param){
        if(! uri.startsWith("http://") && ! uri.startsWith("https://")){
            uri = "http://" + uri;
        }

        boolean isSsl = uri.startsWith("https://");
        return getWebClient(isSsl)
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(param)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(throwable -> {
                    //如果网络请求过程中出现异常，则返回下面的内容
                    int httpStatus;
                    String msg;

                    if(throwable instanceof WebClientResponseException){
                        httpStatus = ((WebClientResponseException) throwable).getStatusCode().value();
                        msg = ((WebClientResponseException) throwable).getMessage();
                    }else if(throwable instanceof TimeoutException){
                        httpStatus = HttpStatus.REQUEST_TIMEOUT.value();
                        msg = throwable.getClass().getSimpleName();
                    }else{
                        httpStatus = HttpStatus.BAD_REQUEST.value();
                        msg = "回调异常：" + throwable.getClass().getName();
                    }
                    CallBackRespVo respVo = new CallBackRespVo();
                    respVo.setHttpStatus(httpStatus);
                    respVo.setResp_code(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
                    respVo.setResp_msg(msg);
                    return Mono.just(JsonUtil.toString(respVo));
                });
    }

    private WebClient getWebClient(boolean isSsl){
        if(isSsl){
            if(webClientSsl == null){
                synchronized (this){
                    if(webClientSsl == null){
                        webClientSsl = buildWebClient(isSsl);
                    }
                }
            }
            return webClientSsl;
        }

        if(webClient == null){
            synchronized (this){
                if(webClient == null){
                    webClient = buildWebClient(isSsl);
                }
            }
        }
        return webClient;
    }

    private WebClient buildWebClient(boolean isSsl) {
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> {
            if(isSsl){
                try{
                    SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                    options.sslContext(sslContext);
                }catch (Exception e){
                    throw new ApiException("SslContext获取异常", e);
                }
            }

            options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .compression(true)
                    .afterNettyContextInit(ctx -> {
                        ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                    });
        });
        return WebClient.builder()
                .clientConnector(connector)
                .build();
    }
}

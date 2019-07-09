package com.joinpay.sdk.utils;

import com.joinpay.sdk.entity.Request;
import com.joinpay.sdk.entity.Response;
import com.joinpay.sdk.entity.SecretKey;
import com.joinpay.sdk.exceptions.JPException;

/**
 * 请求处理工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class RequestUtil {

    /**
     * 发起请求，对于返回的Response内容
     *   说明：
     *      1、如果request中的aes_key不为空，且secretKey中的aes_key_decrypt_key不为空，会自动使用aes_key_decrypt_key为aes_key进行加密
     *      1、验签时不用理会http状态码，而只管对返回的内容进行验签，如果没有内容返回，也当做验签失败处理
     *      2、如果验签不通过，一切结果都不可信，不用理会http状态码是什么，也不用例会返回的内容是什么
     *
     * @param url
     * @param request
     * @param secretKey
     * @return
     * @throws JPException
     */
    public static Response doRequest(String url, Request request, SecretKey secretKey) throws JPException {
        requestParamValid(request, secretKey);

        try{
            SignUtil.sign(request, secretKey.getMchSignPrivateKey());
        }catch(Exception e){
            throw new JPException("签名失败: "+e.getMessage(), e);
        }

        if(StringUtil.isNotEmpty(request.getAes_key()) && StringUtil.isNotEmpty(secretKey.getJpEncryptPublicKey())){
            request.setAes_key(RSAUtil.encrypt(request.getAes_key(), secretKey.getJpEncryptPublicKey()));
        }

        String respJson = null;
        try{
            respJson = OkHttpUtil.postJsonSync(url, JsonUtil.toString(request));
        }catch(Exception e){
            throw new JPException("发送http请求时发生异常: "+e.getMessage(), e);
        }
        if(StringUtil.isEmpty(respJson)){
            throw new JPException("请求完成，但响应信息为空");
        }

        Response response = null;
        try{
            response = JsonUtil.toBeanOrderly(respJson, Response.class);
        }catch (Exception e){
            throw new JPException("请求完成，但响应信息转换失败: "+e.getMessage() + "，respJson="+respJson, e);
        }

        try{
            boolean isOk = SignUtil.verify(response, secretKey.getJpVerifyPublicKey());
            if(!isOk){
                throw new JPException("验签不通过！");
            }
        }catch (Exception e){
            throw new JPException("请求完成，但响应信息验签失败: "+e.getMessage() + "，respJson="+respJson, e);
        }

        if(StringUtil.isNotEmpty(response.getAes_key()) && StringUtil.isNotEmpty(secretKey.getMchDecryptPrivateKey())){
            String aesKey = RSAUtil.decrypt(response.getAes_key(), secretKey.getMchDecryptPrivateKey());
            response.setAes_key(aesKey);
        }

        return response;
    }

    private static <T> void requestParamValid(Request request, SecretKey secretKey){
        if(StringUtil.isEmpty(request)){
            throw new JPException("request不能为空");
        }else if(StringUtil.isEmpty(request.getMethod())){
            throw new JPException("Request.method不能为空");
        }else if(StringUtil.isEmpty(request.getVersion())){
            throw new JPException("Request.version不能为空");
        }else if(StringUtil.isEmpty(request.getData())){
            throw new JPException("Request.data不能为空");
        }else if(StringUtil.isEmpty(request.getSign_type())){
            throw new JPException("Request.sign_type不能为空");
        }else if(StringUtil.isEmpty(request.getMch_no())){
            throw new JPException("Request.mch_no不能为空");
        }

        if(StringUtil.isEmpty(secretKey)){
            throw new JPException("secretKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getMchSignPrivateKey())){
            throw new JPException("SecretKey.mchSignPrivateKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getJpVerifyPublicKey())){
            throw new JPException("SecretKey.jpVerifyPublicKey不能为空");
        }
    }

}

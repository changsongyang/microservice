package org.study.common.api.test;

import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.utils.SignUtil;
import org.study.common.statics.exceptions.BizException;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.OkHttpUtil;
import org.study.common.util.utils.RSAUtil;
import org.study.common.util.utils.StringUtil;

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
     * @throws BizException
     */
    public static ResponseParam doRequest(String url, RequestParam request, SecretKey secretKey) throws BizException {
        requestParamValid(request, secretKey);

        try{
            String signStr = SignUtil.getSortedString(request);
            signStr = SignUtil.sign(signStr, request.getSign_type(), secretKey.getReqSignPriKey());
            request.setSign(signStr);
        }catch(Exception e){
            throw new BizException("签名失败: "+e.getMessage(), e);
        }

        if(StringUtil.isNotEmpty(request.getSec_key()) && StringUtil.isNotEmpty(secretKey.getSecKeyEncryptPubKey())){
            request.setSec_key(RSAUtil.encrypt(request.getSec_key(), secretKey.getSecKeyEncryptPubKey(), false));
        }

        String respJson = null;
        try{
            respJson = OkHttpUtil.postJsonSync(url, JsonUtil.toString(request));
        }catch(Exception e){
            throw new BizException("发送http请求时发生异常: "+e.getMessage(), e);
        }
        if(StringUtil.isEmpty(respJson)){
            throw new BizException("请求完成，但响应信息为空");
        }

        ResponseParam response = null;
        try{
            response = JsonUtil.toBean(respJson, ResponseParam.class);
        }catch (Exception e){
            throw new BizException("请求完成，但响应信息转换失败: "+e.getMessage() + "，respJson="+respJson, e);
        }

        try{
            String signStr = SignUtil.getSortedString(response);
            boolean isOk = SignUtil.verify(signStr, response.getSign(), response.getSign_type(), secretKey.getRespVerifyPubKey());
            if(!isOk){
                throw new BizException("响应信息验签不通过！");
            }
        }catch (Exception e){
            throw new BizException("请求完成，但响应信息验签异常: "+e.getMessage() + "，respJson="+respJson, e);
        }

        if(StringUtil.isNotEmpty(response.getSec_key()) && StringUtil.isNotEmpty(secretKey.getSecKeyDecryptPriKey())){
            String secKey = RSAUtil.decrypt(response.getSec_key(), secretKey.getSecKeyDecryptPriKey());
            response.setSec_key(secKey);
        }

        return response;
    }

    private static <T> void requestParamValid(RequestParam request, SecretKey secretKey){
        if(StringUtil.isEmpty(request)){
            throw new BizException("request不能为空");
        }else if(StringUtil.isEmpty(request.getMethod())){
            throw new BizException("Request.method不能为空");
        }else if(StringUtil.isEmpty(request.getVersion())){
            throw new BizException("Request.version不能为空");
        }else if(StringUtil.isEmpty(request.getData())){
            throw new BizException("Request.data不能为空");
        }else if(StringUtil.isEmpty(request.getSign_type())){
            throw new BizException("Request.sign_type不能为空");
        }else if(StringUtil.isEmpty(request.getMch_no())){
            throw new BizException("Request.mch_no不能为空");
        }

        if(StringUtil.isEmpty(secretKey)){
            throw new BizException("secretKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getReqSignPriKey())){
            throw new BizException("SecretKey.reqSignPriKey不能为空");
        }else if(StringUtil.isEmpty(secretKey.getRespVerifyPubKey())){
            throw new BizException("SecretKey.respVerifyPubKey不能为空");
        }
    }

}

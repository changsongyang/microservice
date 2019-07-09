package org.study.common.api.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.api.params.APIParam;
import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.service.UserService;
import org.study.common.api.utils.SignUtil;
import org.study.common.api.vo.CallBackRespVo;
import org.study.common.api.vo.MerchantInfo;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.RSAUtil;
import org.study.common.util.utils.StringUtil;

import java.util.Map;

/**
 * @description 签名、验签的组件，需要使用此组件的项目，需要自己配置这个@Bean，同时自己实现UserService、ValidFailService 然后再通过Spring进行依赖注入
 * @author chenyf
 * @date 2019-02-20
 */
public final class RequestHelper {
    private Logger logger = LoggerFactory.getLogger(RequestHelper.class);
    private UserService userService;

    public RequestHelper(UserService userService){
        this.userService = userService;
    }

    /**
     * IP校验
     * @param ip
     * @param ipValidKey
     * @param requestParam
     * @return
     */
    public Result<String> ipVerify(String ip, String ipValidKey, RequestParam requestParam, APIParam param){
        Result<String> result = new Result();
        if(requestParam == null || StringUtil.isEmpty(requestParam.getMch_no())){
            return result;
        }

        MerchantInfo merchantInfo = userService.getMerchantInfo(requestParam.getMch_no(), param);
        boolean isVerifyOk = false;
        String expectIp = null;//预期的Ip
        Map<String, String> ipMap = merchantInfo.getIpValidMap();
        if(ipMap == null || (expectIp = ipMap.get(ipValidKey)) == null){
            isVerifyOk = true;//没有设置IP，说明不需要校验IP
        }else if(expectIp.contains(ip)){
            isVerifyOk = true;//包含当前IP，校验通过
        }else{
            isVerifyOk = false;//不包含当前IP，校验失败
        }

        result.setVerifyOk(isVerifyOk);
        result.setOtherInfo(expectIp);
        return result;
    }

    /**
     * 签名校验
     * @param requestParam
     * @return
     */
    public Result<Throwable> signVerify(RequestParam requestParam, APIParam param){
        Result result = new Result();
        if(requestParam == null || StringUtil.isEmpty(requestParam.getMch_no())){
            return result;
        }

        MerchantInfo merchantInfo = userService.getMerchantInfo(requestParam.getMch_no(), param);
        boolean isSignOk = false;//签名是否通过校验(默认是false，请勿修改默认值)
        try{
            isSignOk = SignUtil.verify(requestParam, merchantInfo.getSignValidKey());
        }catch(Throwable e){
            result.setOtherInfo(e);
            logger.error("验签失败，因为验签时出现异常 RequestParam = {}", JsonUtil.toString(requestParam), e);
        }
        result.setVerifyOk(isSignOk);
        return result;
    }

    /**
     * 对响应信息的验签
     * @param respVo
     * @return
     */
    public Result<Throwable> signVerify(CallBackRespVo respVo, APIParam param){
        Result result = new Result();
        if(respVo == null || StringUtil.isEmpty(respVo.getMch_no())){
            return result;
        }

        MerchantInfo merchantInfo = userService.getMerchantInfo(respVo.getMch_no(), param);
        boolean isSignOk = false;//签名是否通过校验(默认是false，请勿修改默认值)
        try{
            isSignOk = SignUtil.verify(respVo, merchantInfo.getSignValidKey());
        }catch(Throwable e){
            result.setOtherInfo(e);
            logger.error("验签失败，因为验签时出现异常 RequestParam = {}", e);
        }
        result.setVerifyOk(isSignOk);
        return result;
    }

    /**
     * 给aes_key解密
     * @param requestParam
     */
    public void secKeyDecrypt(RequestParam requestParam, APIParam param){
        if(requestParam == null || StringUtil.isEmpty(requestParam.getMch_no()) || StringUtil.isEmpty(requestParam.getSec_key())){
            return;
        }

        MerchantInfo merchantInfo = userService.getMerchantInfo(requestParam.getMch_no(), param);
        if(merchantInfo == null || StringUtil.isEmpty(merchantInfo.getSecKeyDecryptKey())){
            return;
        }

        String secKey = RSAUtil.decrypt(requestParam.getSec_key(), merchantInfo.getSecKeyDecryptKey());
        requestParam.setSec_key(secKey);
    }

    /**
     * 生成签名
     * @param responseParam
     * @throws org.study.common.api.exceptions.ApiException
     */
    public void signAndEncrypt(ResponseParam responseParam, APIParam param){
        if(responseParam == null || StringUtil.isEmpty(responseParam.getMch_no())){
            responseParam.setSign("");
            return;
        }

        MerchantInfo merchantInfo = userService.getMerchantInfo(responseParam.getMch_no(), param);
        if(merchantInfo == null){
            responseParam.setSign("");
            return;
        }

        if(StringUtil.isEmpty(responseParam.getSign_type()) && (param != null && param.getFillSignType())){
            responseParam.setSign_type(merchantInfo.getSignType());
        }
        if(StringUtil.isEmpty(responseParam.getSign_type())){
            responseParam.setSign("");
            return;
        }

        //如果加密失败，定会抛出异常，此时签名串就为空值，客户端就会验签失败，所以，加密这一步骤要放在加签名之前
        if(StringUtil.isNotEmpty(responseParam.getSec_key()) && StringUtil.isNotEmpty(merchantInfo.getSecKeyEncryptKey())){
            responseParam.setSec_key(RSAUtil.encrypt(responseParam.getSec_key(), merchantInfo.getSecKeyEncryptKey(), false));
        }

        SignUtil.sign(responseParam, merchantInfo.getSignGenerateKey());
    }

    public class Result<T> {
        private boolean isVerifyOk = false;
        private T otherInfo;

        public boolean isVerifyOk() {
            return isVerifyOk;
        }

        public void setVerifyOk(boolean verifyOk) {
            isVerifyOk = verifyOk;
        }

        public T getOtherInfo() {
            return otherInfo;
        }

        public void setOtherInfo(T otherInfo) {
            this.otherInfo = otherInfo;
        }
    }
}

package org.study.common.api.vo;

import com.alibaba.fastjson.JSON;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.util.utils.RandomUtil;

/**
 * 响应给商户的VO，主要用作Controller的出参
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseVo<T> {
    private String respCode;
    private String bizCode;
    private String bizMsg;
    private String mchNo;
    private T data;
    private String randStr;
    private String sign;
    private String signType;
    private String secKey = "";

    public static <V> ResponseVo<V> success(String mchNo, String signType, V data) {
        return success(mchNo, signType, BizCodeEnum.ACCEPT_SUCCESS.getCode(), BizCodeEnum.ACCEPT_SUCCESS.getMsg(), data);
    }

    public static <V> ResponseVo<V> success(String mchNo, String signType, String bizCode, String bizMsg, V data) {
        ResponseVo<V> vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_SUCCESS.getCode());
        vo.setBizCode(bizCode);
        vo.setBizMsg(bizMsg);
        vo.setMchNo(mchNo);
        vo.setData(data);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);
        return vo;
    }

    public static ResponseVo acceptFail(String mchNo, String signType, String bizCode, String bizMsg){
        ResponseVo vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_FAIL.getCode());
        vo.setBizCode(bizCode);
        vo.setBizMsg(bizMsg);
        vo.setMchNo(mchNo);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);
        return vo;
    }

    public static ResponseVo acceptUnknown(String mchNo, String signType){
        ResponseVo vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
        vo.setBizCode(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
        vo.setBizMsg(RespCodeEnum.ACCEPT_UNKNOWN.getMsg());
        vo.setMchNo(mchNo);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);
        return vo;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getBizMsg() {
        return bizMsg;
    }

    public void setBizMsg(String bizMsg) {
        this.bizMsg = bizMsg;
    }

    public String getMchNo() {
        return mchNo;
    }

    public void setMchNo(String mchNo) {
        this.mchNo = mchNo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSecKey() {
        return secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}

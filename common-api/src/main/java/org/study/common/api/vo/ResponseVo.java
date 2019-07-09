package com.gw.api.base.vo;

import com.alibaba.fastjson.JSON;
import com.gw.api.base.constants.CommonConst;
import com.gw.api.base.enums.BizCodeEnum;
import com.gw.api.base.enums.RespCodeEnum;
import com.gw.api.base.utils.RandomUtil;

/**
 * 响应给商户的VO，主要用作Controller的出参
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseVo<T> {
    private String respCode;
    private String respMsg;
    private String mchNo;
    private T data;
    private String randStr;
    private String sign;
    private String signType;
    private String secKey = "";

    public static <V> ResponseVo<V> success(String mchNo, String signType, V data) {
        ResponseVo<V> vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_SUCCESS.getCode());
        vo.setRespMsg(RespCodeEnum.ACCEPT_SUCCESS.getMsg());
        vo.setMchNo(mchNo);
        vo.setData(data);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);
        return vo;
    }

    public static ResponseVo<BizCodeVo> acceptFail(String mchNo, String signType, String bizMsg){
        return acceptFail(mchNo, signType, "", bizMsg);
    }

    public static ResponseVo<BizCodeVo> acceptFail(String mchNo, String signType, String bizCode, String bizMsg){
        ResponseVo<BizCodeVo> vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_FAIL.getCode());
        vo.setRespMsg(RespCodeEnum.ACCEPT_FAIL.getMsg());
        vo.setMchNo(mchNo);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);

        BizCodeVo codeVo = new BizCodeVo();
        codeVo.setBiz_code(bizCode);
        codeVo.setBiz_msg(bizMsg);
        vo.setData(codeVo);
        return vo;
    }

    public static <V> ResponseVo<V> acceptFail(String mchNo, String signType, V data){
        ResponseVo<V> vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_FAIL.getCode());
        vo.setRespMsg(RespCodeEnum.ACCEPT_FAIL.getMsg());
        vo.setMchNo(mchNo);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);
        vo.setData(data);
        return vo;
    }

    public static ResponseVo<BizCodeVo> acceptUnknown(String mchNo, String signType){
        ResponseVo vo = new ResponseVo();
        vo.setRespCode(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
        vo.setRespMsg(RespCodeEnum.ACCEPT_UNKNOWN.getMsg());
        vo.setMchNo(mchNo);
        vo.setRandStr(RandomUtil.get32LenStr());
        vo.setSignType(signType);

        BizCodeVo codeVo = new BizCodeVo();
        codeVo.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
        codeVo.setBiz_msg(BizCodeEnum.ACCEPT_UNKNOWN.getMsg());
        vo.setData(codeVo);
        return vo;
    }

    public void joinSecKey(String secretKey, String iv){
        this.secKey = secretKey + CommonConst.SEC_KEY_SEPARATOR + iv;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
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

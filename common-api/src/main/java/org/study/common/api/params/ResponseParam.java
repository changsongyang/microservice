package org.study.common.api.params;

import org.study.common.api.annonation.NotSign;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.util.utils.RandomUtil;

/**
 * 响应用户请求的参数
 * @author chenyf
 * @date 2018-12-15
 */
public class ResponseParam {
    private String resp_code;
    private String biz_code;
    private String biz_msg;
    private String mch_no;
    private String data;
    private String rand_str;
    private String sign_type;
    @NotSign
    private String sign;
    @NotSign
    private String sec_key;

    public static ResponseParam acceptUnknown(String mchNo){
        ResponseParam responseParam = new ResponseParam();
        responseParam.setResp_code(RespCodeEnum.ACCEPT_UNKNOWN.getCode());
        responseParam.setBiz_code(BizCodeEnum.ACCEPT_UNKNOWN.getCode());
        responseParam.setBiz_msg(BizCodeEnum.ACCEPT_UNKNOWN.getMsg());
        responseParam.setMch_no(mchNo);
        responseParam.setSign("");
        responseParam.setRand_str(RandomUtil.get32LenStr());
        return responseParam;
    }

    public static ResponseParam acceptFail(String mchNo, String bizCode, String bizMsg){
        ResponseParam responseParam = new ResponseParam();
        responseParam.setResp_code(RespCodeEnum.ACCEPT_FAIL.getCode());
        responseParam.setBiz_code(bizCode);
        responseParam.setBiz_msg(bizMsg);
        responseParam.setMch_no(mchNo);
        responseParam.setSign("");
        responseParam.setRand_str(RandomUtil.get32LenStr());
        return responseParam;
    }

    public String getResp_code() {
        return resp_code;
    }

    public void setResp_code(String resp_code) {
        this.resp_code = resp_code;
    }

    public String getBiz_code() {
        return biz_code;
    }

    public void setBiz_code(String biz_code) {
        this.biz_code = biz_code;
    }

    public String getBiz_msg() {
        return biz_msg;
    }

    public void setBiz_msg(String biz_msg) {
        this.biz_msg = biz_msg;
    }

    public String getMch_no() {
        return mch_no;
    }

    public void setMch_no(String mch_no) {
        this.mch_no = mch_no;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRand_str() {
        return rand_str;
    }

    public void setRand_str(String rand_str) {
        this.rand_str = rand_str;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign_type() {
        return sign_type;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }

    public String getSec_key() {
        return sec_key;
    }

    public void setSec_key(String sec_key) {
        this.sec_key = sec_key;
    }
}

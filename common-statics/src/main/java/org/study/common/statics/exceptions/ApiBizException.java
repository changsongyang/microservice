package org.study.common.statics.exceptions;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/29
 */
public class ApiBizException extends BizException{

    public final static ApiBizException GLOBAL_API_EXCEPTION = new ApiBizException(0, "Unexpected Error, Check Your MediaType|RequestPath|Parameter Format&Value|RequestMethod etc. If Not Help, Please Contact Our Business Support!");



    public ApiBizException() {
        super();
    }

    public ApiBizException(int errorCode, String message) {
        super(message);
        this.code = errorCode;
        this.msg = message;
    }
}

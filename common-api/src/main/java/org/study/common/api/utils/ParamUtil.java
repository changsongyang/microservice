package org.study.common.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.api.enums.SignTypeEnum;
import org.study.common.api.exceptions.ApiException;
import org.study.common.api.params.RequestParam;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;

/**
 * 参数处理工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class ParamUtil {
    private static Logger logger = LoggerFactory.getLogger(ParamUtil.class);

    public static boolean paramValid(RequestParam requestParam, String requestUrl) throws ApiException {
        if(requestParam == null){
            throw new ApiException("参数请求体为空！");
        }else if(StringUtil.isEmpty(requestParam.getMethod())){
            throw new ApiException("method 为空！");
        }else if(StringUtil.isEmpty(requestParam.getVersion())){
            throw new ApiException("version 为空！");
        }else if(StringUtil.isEmpty(requestParam.getData())){
            throw new ApiException("data 为空！");
        }else if(StringUtil.isEmpty(requestParam.getRand_str())){
            throw new ApiException("rand_str 为空！");
        }else if(StringUtil.isEmpty(requestParam.getSign_type())){
            throw new ApiException("sign_type 为空！");
        }else if(StringUtil.isEmpty(requestParam.getMch_no())){
            throw new ApiException("mch_no 为空！");
        }else if(StringUtil.isEmpty(requestParam.getSign())){
            throw new ApiException("sign 为空！");
        }

        if(StringUtil.isLengthOver(requestParam.getMethod(), 64)){
            throw new ApiException("method 的长度不能超过64！");
        }else if(StringUtil.isLengthOver(requestParam.getVersion(), 5)){
            throw new ApiException("version 的长度不能超过5！");
        }else if(! StringUtil.isLengthOk(requestParam.getRand_str(), 32, 32)){
            throw new ApiException("rand_str 的长度须为32！");
        }else if(StringUtil.isLengthOver(requestParam.getSign_type(), 5)){
            throw new ApiException("sign_type 的长度不能超过5！");
        }else if(! StringUtil.isLengthOk(requestParam.getMch_no(), 10, 15)){
            throw new ApiException("mch_no 的长度须在10~15之间！");
        }

        if(! SignTypeEnum.getValueMap().containsKey(requestParam.getSign_type())){
            logger.error("未预期的签名类型 RequestParam = {}", JsonUtil.toString(requestParam));
            throw new ApiException("sign_type 非法参数值 !!");
        }

        return true;
    }
}

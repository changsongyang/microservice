package org.study.common.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.api.annonation.NotSign;
import org.study.common.api.enums.SignTypeEnum;
import org.study.common.api.exceptions.ApiException;
import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.vo.CallBackRespVo;
import org.study.common.util.utils.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 签名、验签的工具类
 * @author chenyf
 * @date 2018-12-15
 */
public class SignUtil {

    private final static Logger logger = LoggerFactory.getLogger(SignUtil.class);

    public final static String SIGN_SEPARATOR = "&";//分隔符
    public final static String SIGN_EQUAL = "=";//等于号
    public final static String SIGN_KEY_PARAM_NAME = "key";

    /**
     * 验证回调时的响应签名
     * @param respVo
     * @param secKey
     * @return
     */
    public static boolean verify(CallBackRespVo respVo, String secKey){
        String signData = getSortedString(respVo);
        return verify(signData, respVo.getSign(), respVo.getSign_type(), secKey);
    }

    /**
     * 验证签名请求的签名
     * @param requestParam
     * @param secKey
     * @return
     */
    public static boolean verify(RequestParam requestParam, String secKey){
        String signData = getSortedString(requestParam);
        return verify(signData, requestParam.getSign(), requestParam.getSign_type(), secKey);
    }

    /**
     * 验证签名
     * @param signData
     * @param signParam
     * @param signType
     * @param secKey
     * @return
     */
    public static boolean verify(String signData, String signParam, String signType, String secKey) {
        if(SignTypeEnum.MD5.getValue().equals(signType)){
            signData = genMD5Sign(signData, secKey);
            if(signData.equals(signParam)){
                return true;
            }else{
                return false;
            }
        }else if(SignTypeEnum.RSA.getValue().equals(signType)){
            return RSAUtil.verify(signData, secKey, signParam);
        }else{
            return false;
        }
    }

    /**
     * 生成签名
     * @param responseParam
     * @param key
     * @return
     */
    public static void sign(ResponseParam responseParam, String key){
        if(StringUtil.isEmpty(responseParam)){
            return;
        }else if(StringUtil.isEmpty(responseParam.getMch_no()) || StringUtil.isEmpty(responseParam.getSign_type())){
            responseParam.setSign("");
            return;
        }
        if(StringUtil.isEmpty(responseParam.getRand_str())){
            responseParam.setRand_str(RandomUtil.get32LenStr());
        }

        String signStr = getSortedString(responseParam);
        responseParam.setSign(sign(signStr, responseParam.getSign_type(), key));
    }

    /**
     * 生成签名串
     * @param signData
     * @param signType
     * @param secKey
     * @return
     */
    public static String sign(String signData, String signType, String secKey){
        if(SignTypeEnum.MD5.getValue().equals(signType)){
            return genMD5Sign(signData, secKey);
        }else if(SignTypeEnum.RSA.getValue().equals(signType)){
            return RSAUtil.sign(signData, secKey);
        }else{
            //抛出签名失败的异常
            throw new ApiException("签名失败，未预期的签名类型：" + signType);
        }
    }

    public static String getSortedString(Object obj){
        Field[] fields = obj.getClass().getDeclaredFields();

        Map<String, Object> map = new HashMap<>();
        for(int i=0; i<fields.length; i++){
            Field filed = fields[i];
            String name = filed.getName();
            NotSign notSign = filed.getAnnotation(NotSign.class);
            if(notSign != null){//不参与签名或验签的参数直接跳过
                continue;
            }

            Object value;
            try{
                filed.setAccessible(true);
                value = filed.get(obj);
            }catch(Exception e){
                throw new RuntimeException(e);
            }

            if(value == null){
                value = "";
            }
            map.put(name, value);
        }

        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList(map.keySet());
        Collections.sort(keys); // 排序map
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value;
            if(map.get(key) instanceof String){
                value = (String) map.get(key);
            }else{
                value = JsonUtil.toString(map.get(key));
            }

            if(i != 0){
                content.append(SIGN_SEPARATOR);
            }
            content.append(key).append(SIGN_EQUAL).append(value);
        }
        return content.toString();
    }

    private static String genMD5Sign(String signStr, String key){
        return CodeUtil.base64Encode(MD5Util.getMD5(signStr + SIGN_SEPARATOR + SIGN_KEY_PARAM_NAME + SIGN_EQUAL + key));
    }

    public static void main(String[] args) throws Exception {
        String secretKey = "12345678qwertyui";
        String randStr = RandomUtil.get32LenStr();
        randStr = "099F34608FD3738A599497C5E054AFBA";

        RequestParam requestParam = new RequestParam();
        requestParam.setMch_no("888000000000000");
        requestParam.setMethod("joinpay.trade.singleCreate");
        requestParam.setVersion("1.0");
        requestParam.setRand_str(randStr);
        requestParam.setSign_type(SignTypeEnum.MD5.getValue());

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("product_code", "EL321223345558");
        dataMap.put("product_name", "电磁炉");
        dataMap.put("price", "28000");
        requestParam.setData(JsonUtil.toString(dataMap));

        String signStr = getSortedString(requestParam);

        if(SignTypeEnum.MD5.getValue().equals(requestParam.getSign_type())){
            signStr = genMD5Sign(signStr, secretKey);
        }else if(SignTypeEnum.RSA.getValue().equals(requestParam.getSign_type())){
            signStr = RSAUtil.sign(signStr, secretKey);
        }else{
            signStr = "";
            System.out.println("未预期的签名类型："+requestParam.getSign_type());
        }
        requestParam.setSign(signStr);

        System.out.println("sign.length() = " + requestParam.getSign().length());
        System.out.println("RequestParam = " + JsonUtil.toString(requestParam));
    }
}

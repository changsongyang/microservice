package com.gw.api.base.utils;

import com.gw.api.base.annonation.NotSign;
import com.gw.api.base.enums.SignTypeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.params.RequestParam;
import com.gw.api.base.params.ResponseParam;
import com.gw.api.base.vo.CallBackRespVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static boolean verify(CallBackRespVo respVo, String key){
        String signStr = getSortedString(respVo, true);
        if(SignTypeEnum.MD5.getValue().equals(respVo.getSign_type())){
            signStr = HEXUtil.encode(genMD5Sign(signStr, key), true);
            if(signStr.equals(respVo.getSign().toUpperCase())){
                return true;
            }else{
                return false;
            }
        }else if(SignTypeEnum.RSA.getValue().equals(respVo.getSign_type())){
            return RSAUtil.verify(signStr, key, HEXUtil.decode(respVo.getSign()), true);
        }else{
            return false;
        }
    }

    /**
     * 验证签名
     * @param requestParam
     * @param key
     * @return
     */
    public static boolean verify(RequestParam requestParam, String key){
        String signStr = getSortedString(requestParam, false);
        if(SignTypeEnum.MD5.getValue().equals(requestParam.getSign_type())){
            signStr = HEXUtil.encode(genMD5Sign(signStr, key), true);
            if(signStr.equals(requestParam.getSign().toUpperCase())){
                return true;
            }else{
                return false;
            }
        }else if(SignTypeEnum.RSA.getValue().equals(requestParam.getSign_type())){
            return RSAUtil.verify(signStr, key, HEXUtil.decode(requestParam.getSign()), true);
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

        String signStr = getSortedString(responseParam, false);
        if(SignTypeEnum.MD5.getValue().equals(responseParam.getSign_type())){
            signStr = HEXUtil.encode(genMD5Sign(signStr, key), true);
        }else if(SignTypeEnum.RSA.getValue().equals(responseParam.getSign_type())){
            signStr = HEXUtil.encode(RSAUtil.sign(signStr, key, true));
        }else{
            //抛出签名失败的异常
            throw new ApiException("签名失败，未预期的签名类型："+responseParam.getSign_type());
        }

        responseParam.setSign(signStr);
    }

    protected static String getSortedString(Object obj, boolean ignoreEmpty){
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

            if(ignoreEmpty && StringUtil.isEmpty(value)){//忽略空值
                continue;
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

    private static byte[] genMD5Sign(String signStr, String key){
        return MD5Util.getMD5(signStr + SIGN_SEPARATOR + SIGN_KEY_PARAM_NAME + SIGN_EQUAL + key);
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

        String signStr = getSortedString(requestParam, false);

        if(SignTypeEnum.MD5.getValue().equals(requestParam.getSign_type())){
            signStr = HEXUtil.encode(genMD5Sign(signStr, secretKey), true);
        }else if(SignTypeEnum.RSA.getValue().equals(requestParam.getSign_type())){
            signStr = HEXUtil.encode(RSAUtil.sign(signStr, secretKey, true));
        }else{
            signStr = "";
            System.out.println("未预期的签名类型："+requestParam.getSign_type());
        }
        requestParam.setSign(signStr);

        System.out.println("sign.length() = " + requestParam.getSign().length());
        System.out.println("RequestParam = " + JsonUtil.toString(requestParam));
    }
}

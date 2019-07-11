<?php
namespace utils;

use exceptions\BizException;
use ReflectionClass;
use ReflectionProperty;

/**
 * 签名、验签工具类
 * Class SignUtil
 * @package utils
 */
class SignUtil {
    const MS5_BOUND_SYMBOL = "&key=";

    /**
     * 签名
     * @param string $signData
     * @param string $signType
     * @param string $secKey
     * @return string
     * @throws BizException
     */
    public static function sign(string $signData, string $signType, string $secKey){
        if("1" === $signType){
            return MD5Util::getMd5Str($signData . self::MS5_BOUND_SYMBOL . $secKey);
        }else if("2" === $signType){
            return RSAUtil::sign($signData, $secKey);
        }else{
            throw new BizException(BizException::BIZ_ERROR, "未支持的签名类型：" . $signType);
        }
    }

    /**
     * 验签
     * @param string $signData
     * @param string $signParam
     * @param string $signType
     * @param string $secKey
     * @return bool
     * @throws BizException
     */
    public static function verify(string $signData, string $signParam, string $signType, string $secKey){
        if("1" === $signType){
            $signData = MD5Util::getMd5Str($signData . self::MS5_BOUND_SYMBOL . $secKey);
            return $signData === $signParam;
        }else if("2" === $signType){
            return RSAUtil::verify($signData, $signParam, $secKey);
        }else{
            throw new BizException(BizException::BIZ_ERROR, "未支持的签名类型：" . $signType);
        }
    }

    /**
     * 取得 待签名/待验签 的字符串
     * @param object $param
     * @return string
     * @throws \ReflectionException
     */
    public static function getSortedString(object $param){
        $reflect = new ReflectionClass($param);
        $props = $reflect->getProperties(ReflectionProperty::IS_PUBLIC | ReflectionProperty::IS_PRIVATE | ReflectionProperty::IS_PROTECTED);

        //通过反射取得所有属性和属性的值
        $arr = [];
        foreach ($props as $prop) {
            $prop->setAccessible(true);

            $key = $prop->getName();
            $value = $prop->getValue($param);
            $arr[$key] = $value;
        }

        //按key的字典序升序排序，并保留key值
        ksort($arr);

        //拼接字符串
        $str = '';
        $i = 0;
        foreach($arr as $key => $value) {
            //不参与签名、验签
            if($key == "sign" || $key == "sec_key"){
                continue;
            }

            if($value === null){
                $value = '';
            }

            if($i !== 0){
                $str .= '&';
            }
            $str .= $key . '=' . $value;
            $i ++;
        }
        return $str;
    }
}
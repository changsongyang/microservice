<?php
namespace utils;

class SignUtil {

    public static function sign(string $signData, string $signType, string $secKey){
        if("1" === $signType){
            return base64_encode(md5($signData . "&key=" . $secKey, true));
        }else if("2" === $signType){
            return RSAUtil::sign($signData, $secKey);
        }
    }

    public static function verify(string $signData, string $signParam, string $signType, string $secKey){
        if("1" === $signType){
            $signData = base64_encode(md5($signData . "&key=" . $secKey, true));
            return $signData === $signParam;
        }else if("2" === $signType){
            return RSAUtil::verify($signData, $signParam, $secKey);
        }
    }

    public static function getSortedString(object $param){
        $reflect = new \ReflectionClass($param);
        $props = $reflect->getProperties(\ReflectionProperty::IS_PUBLIC | \ReflectionProperty::IS_PRIVATE | \ReflectionProperty::IS_PROTECTED);

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
            if($i !== 0){
                $str .= '&';
            }
            $i ++;
            $str .= $key . '=' . $value;
        }
        return $str;
    }
}
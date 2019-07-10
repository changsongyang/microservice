<?php
namespace utils;

use param\RequestParam;
use param\ResponseParam;
use param\SecretKey;
use exceptions\BizException;

/**
 * 发起交易请求的工具类
 * Class RequestUtil
 * @package utils
 */
class RequestUtil{

    /**
     * 发起交易请求
     * @param $url
     * @param RequestParam $request
     * @param SecretKey $secretKey
     * @return ResponseParam
     * @throws BizException
     * @throws \ReflectionException
     */
    public static function doRequest($url, RequestParam $request, SecretKey $secretKey){
        //1.参数校验
        static::requestParamValid($request, $secretKey);

        //2.添加签名
        $signStr = SignUtil::getSortedString($request, false);
        $signStr = SignUtil::sign($signStr, $request->getSignType(), $secretKey->getReqSignPriKey());
        $request->setSign($signStr);

        //3.如果存在sec_key，则对其进行rsa加密
        if($request->getSecKey()){
            $request->setSecKey(RSAUtil::encrypt($request->getSecKey(), $secretKey->getSecKeyEncryptPubKey()));
        }

        //4.发起http请求
        $respJson = HttpUtil::postJsonSync($url, json_encode($request));
        if(! $respJson){
            throw new BizException(BizException::BIZ_ERROR, "请求完成，但响应信息为空");
        }

        //5.响应结果转换
        $response = new ResponseParam();
        try{
            self::fillResponse($response, $respJson);
        }catch (Exception $e){
            throw new BizException(BizException::BIZ_ERROR, "请求完成，但响应信息转换失败: " . $e->getMessage() . "，respJson = " . $respJson);
        }

        $isOk = false;
        try{
            $signStr = SignUtil::getSortedString($response, false);
            $isOk = SignUtil::verify($signStr, $response->getSign(), $response->getSignType(), $secretKey->getRespVerifyPubKey());
        }catch (Exception $e){
            throw new BizException(BizException::BIZ_ERROR, "请求完成，但响应信息验签异常: " . $e->getMessage() . "，respJson = " . $respJson);
        }
        if($isOk !== true){
            throw new BizException(BizException::BIZ_ERROR, "响应信息验签不通过！ respJson = " . $respJson);
        }

        if($response->getSecKey() && $secretKey->getSecKeyDecryptPriKey()){
            $secKey = RSAUtil::decrypt($response->getSecKey(), $secretKey->getSecKeyDecryptPriKey());
            $response->setSecKey($secKey);
        }

        return $response;
    }

    /**
     * @param RequestParam $request
     * @param SecretKey $secretKey
     * @throws BizException
     */
    private static function requestParamValid(RequestParam $request, SecretKey $secretKey){
        if(! $request){
            throw new BizException(BizException::PARAM_ERROR, "request不能为空");
        }else if(! $request->getMethod()){
            throw new BizException(BizException::PARAM_ERROR, "Request.method不能为空");
        }else if(! $request->getVersion()){
            throw new BizException(BizException::PARAM_ERROR, "Request.version不能为空");
        }else if(! $request->getData()){
            throw new BizException(BizException::PARAM_ERROR, "Request.data不能为空");
        }else if(! $request->getSignType()){
            throw new BizException(BizException::PARAM_ERROR, "Request.sign_type不能为空");
        }else if(! $request->getMchNo()){
            throw new BizException(BizException::PARAM_ERROR, "Request.mch_no不能为空");
        }

        if(! $secretKey){
            throw new BizException(BizException::PARAM_ERROR, "secretKey不能为空");
        }else if(! $secretKey->getReqSignPriKey()){
            throw new BizException(BizException::PARAM_ERROR, "SecretKey.reqSignPriKey不能为空");
        }else if(! $secretKey->getRespVerifyPubKey()){
            throw new BizException(BizException::PARAM_ERROR, "SecretKey.respVerifyPubKey不能为空");
        }
    }

    /**
     * @param ResponseParam $response
     * @param string $respJson
     */
    private static function fillResponse(ResponseParam &$response, string $respJson){
        $arr = get_object_vars(json_decode($respJson));

        if($arr && is_array($arr)){
            if(isset($arr['resp_code'])){
                $response->setRespCode($arr['resp_code']);
            }
            if(isset($arr['biz_code'])){
                $response->setBizCode($arr['biz_code']);
            }
            if(isset($arr['biz_msg'])){
                $response->setBizMsg($arr['biz_msg']);
            }
            if(isset($arr['mch_no'])){
                $response->setMchNo($arr['mch_no']);
            }
            if(isset($arr['data'])){
                $response->setData($arr['data']);
            }
            if(isset($arr['rand_str'])){
                $response->setRandStr($arr['rand_str']);
            }
            if(isset($arr['sign_type'])){
                $response->setSignType($arr['sign_type']);
            }
            if(isset($arr['sign'])){
                $response->setSign($arr['sign']);
            }
            if(isset($arr['sec_key'])){
                $response->setSecKey($arr['sec_key']);
            }
        }
    }
}


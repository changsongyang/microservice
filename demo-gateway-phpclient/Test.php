<?php
/**
 * 测试入口类
 *
 * PHP VERSION = PHP 7.2.20
 */

require "Autoload.php";

use param\RequestParam;
use param\SecretKey;
use utils\RequestUtil;
use utils\RandomUtil;
use utils\AESUtil;
use vo\DetailVo;
use vo\BatchVo;

$aesKey = RandomUtil::randomStr(16);
$iv = RandomUtil::randomStr(16);

$md5Key = "12345678qwertyui";

//$sysPublicKey = '-----BEGIN PUBLIC KEY-----
//MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCD1PquVQz6inIH66ZMndawRmihQ/4GLX/nHieaX8Htu5NZcn2hB3OZe+rk05AJgcUuUhkNqxhtkArOJJdhxxdF4BNFSQ70Zx9APuda4GgwGnpiA5yJey9awmsmUUS/k4KkQX6bLJWvbKz7TEa5Z6NDD7UBoYu6uFqZH+AL51IlQIDAQAB
//-----END PUBLIC KEY-----';
//$sysPrivateKey = '-----BEGIN RSA PRIVATE KEY-----
//MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIIPU+q5VDPqKcgfrpkyd1rBGaKFD/gYtf+ceJ5pfwe27k1lyfaEHc5l76uTTkAmBxS5SGQ2rGG2QCs4kl2HHF0XgE0VJDvRnH0A+51rgaDAaemIDnIl7L1rCayZRRL+TgqRBfpssla9srPtMRrlno0MPtQGhi7q4Wpkf4AvnUiVAgMBAAECgYBHSsehHr29R1pnzJYUe8lZAghfQbkjMchxuP+VNhbfz7KI0ocGjh0Yil/6GOEH4NB416eK5z1OwmwiRPxWMD2nMFfwgSpH+tewAl6raNhTy9fumyQD6ZNs3y8swCj9e54P4Ph3B+u/OUDB1BZQu6zb2pO0FNIbFPsxPlBN5FDQcQJBAMz/RHGKG16kdTdYyHSHXLR4qtk2xik798i8i9CDJ+OnKfc8VCvGNilWoR6S4a+FcJHEhYs5QcRxNsCClmd0md8CQQCiayLa/sS2lY4dgY3n/G12cAQVhqPSyx8QGcqtLl3jTJQLUbO0fSLo542ZV4azgc/j+f0C/tML4mAY2IozktQLAkBTlQzyAi5woztLqr5ojLxmtQBr+iJHs7SuuvmCtccw0fqRXJ6xDmsM5c5hqd+s8gpY1LjicCD5mHOLgHMUkX0fAkBlR4+Vpha+kGXtalM2HUeY+mLhlXLkyHrXTG4BLg+n5KHQqSL5Yqr5NyMqQtUhbMpZLBMk4ghyubgY5jbP0DhfAkB7+lMimzHiMjLh56AZnsg3UFH+MoupkctS4oseK5vET70tSO0xiUhikf3+0BXZNhnsSfnR493ScDQbyYDKKY1d
//-----END RSA PRIVATE KEY-----';
//
//$mchPublicKey = '-----BEGIN PUBLIC KEY-----
//MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkMtwOfeouGA5T8w8bv5xA4nV1aCTDNxU7T+kMwhkQpNT329k8S+HcIQc4t8CnivpZ5ZgquXA94MUH42S3AO3BTuCQCghhob+iWg0m9SohLh5GYloBlgI+OBFIpynib/dFfCwtLCS/afFsd4PDhrISx6M1cPv0A10QY2JOO1INpQIDAQAB
//-----END PUBLIC KEY-----';
//$mchPrivateKey = '-----BEGIN RSA PRIVATE KEY-----
//MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQy3A596i4YDlPzDxu/nEDidXVoJMM3FTtP6QzCGRCk1Pfb2TxL4dwhBzi3wKeK+lnlmCq5cD3gxQfjZLcA7cFO4JAKCGGhv6JaDSb1KiEuHkZiWgGWAj44EUinKeJv90V8LC0sJL9p8Wx3g8OGshLHozVw+/QDXRBjYk47Ug2lAgMBAAECgYA4A5WoZ/H8eX5hyxgLWklepSJ2w+lOozrd+fvBu3E7iU+RonEwLZ7GLoo9IgpZ3YJcKoPHh20v3r64Wy1fdLSmYlQ1Lk/DasEshXthwWKam+w+lBh7QS+jnChSNxlCzMebQUhKCzFV4Du28ROVVYU/UTS76+LlL5TgwOw/owSQQQJBANX1V4vw2GsS7ri7dR9gJUMl7B80/ciXEMTk1/jO6OfDfhMhWUgHPndTo+OVgyLgpagmeDFSbCCfN1Oa6kwU29UCQQDEdnn46UR1Ye0pYxu1p5YvY4wC036OX4XxLR94DShu24d104prN0ogni6pc6Jh7vtkE1LyM4sh2EiL5x/48mKRAkAUv0StAj7KKzzQ1wSldTpHx56c7BOL5vIuVY6HxvCYwMEx87LnpCQviAHFaNMdh7EonApdpgNsKmRADC6aEA+9AkB24yc+jJLD4eWttO7wx6BnvvrcPvYH3CBm6SJw+K1uIGTh1YifBw9Rm8eq/XHXh9ITJmp8bNqWOZb1KoE7mhoxAkBCxC++0ACafWKKFp8baJmILhdTu0BDKxvXflF5xWpBn2nCOY6eztJYZ9acnzI2HTL4XLe2tYFSr7V8u0e/SN0h
//-----END RSA PRIVATE KEY-----';

$sysPublicKey = "-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCd/fouQL2zw3wDi+qL8yF8teLXt0tBUSCC8I2d7H5o+2/blT9/pg/Nw9PJxXO0bVZeNubmqSLycieS50vR8zEcsJPad8FpKVxSC1DWNfKyma3qY++9yxJPkp951Ho74RCkSJJrwBfXZMbkc27T+K9OcAZRyQJNMSTpz+YhChb4pQIDAQAB
-----END PUBLIC KEY-----";
$sysPrivateKey = "-----BEGIN RSA PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ39+i5AvbPDfAOL6ovzIXy14te3S0FRIILwjZ3sfmj7b9uVP3+mD83D08nFc7RtVl425uapIvJyJ5LnS9HzMRywk9p3wWkpXFILUNY18rKZrepj773LEk+Sn3nUejvhEKRIkmvAF9dkxuRzbtP4r05wBlHJAk0xJOnP5iEKFvilAgMBAAECgYApujOCVc0ElmPBmAmZbtxwUKWZ7aotlRyuGJR+mkCEv6u6Zf/AWf6gjND54HF/vMTr2zo+v3sgZ2/2R6ppx/43NzR27pop/NMdiuONIsFC+/hIHWDUOoBCwaMnfh13pQqlwEm/zO4JRE/UQmiKJcGtcc/REDf2/4chGKurBTKoHQJBAOkIaFI5eGvukroyarxjuA0IJ9obrQ7bIVIobwtW8voidUont93BK/88IIu04Iu2jayytP3kz8a0+rsdmo+3GjsCQQCtkDxeg/QCOvAyntdKE6YYWY+El/MqRoObO4Z4be7040lSKkaxwX6m2m41XK1NS0r/ca6wVIFm2y6Et6tWMCqfAkEAjmoo9zdQNQYUfd6aBJAcxzoYwN7xIIcjEgbL9m4pCF1OuQcVA10u+klQypC8OiZS5xxAKHpR0OqB4SDyeKo6SQJAcy+0QO3FtO00mAO+0ZS0uJhHnTHS2Y2urgkVNzuOSMvGz1brT/Eggs+YMKXvBcsgXOMvkiqjLoXsG3xho3OX9QJBAIyeF4jt9R3zbBkS5X2+gRaaICja/hLwQfg+s3K/2K+AmehmuP+EHyCARe8LNcfXzrRSSW6XokMjgh65+k615Bw=
-----END RSA PRIVATE KEY-----";

$mchPublicKey = "-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMFJWpm+IGgstDlxuGzbNs+E0x75PeCmVCOyEG8pmXRpNpBiV86pnuYgKcL2nSuqHT1oA9Bgvtf4gcxSOlBzLsXmIcHKpoMOqdA3D05Yy2Zw70MOKJA6uSLuCqwz6flh6CtKFVTateAjXfkusOv7TC0mxpmYT4ruagWoMcc5O2OwIDAQAB
-----END PUBLIC KEY-----";
$mchPrivateKey = "-----BEGIN RSA PRIVATE KEY-----
MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIwUlamb4gaCy0OXG4bNs2z4TTHvk94KZUI7IQbymZdGk2kGJXzqme5iApwvadK6odPWgD0GC+1/iBzFI6UHMuxeYhwcqmgw6p0DcPTljLZnDvQw4okDq5Iu4KrDPp+WHoK0oVVNq14CNd+S6w6/tMLSbGmZhPiu5qBagxxzk7Y7AgMBAAECgYBB2qOJeylFWlPo0K82Lpo9jnXsFe90IXr9KgMa2w5t2dYPN76D/V6kfRsxBfFAClFt35emGKOe4afBrsRVHw9G8F6XDrSds1wNUZ2xTBzCd4pWVgBqmp5r44vpd1vrXEQKQgmd3if2EggXe2ZAlUvAPt6RmGwUc9F/NKvHk3QBEQJBANjg3uHLw6nZY1I53JLOz/Bf5uo84QS2Pn4tOMiqo9QQ4enQBbveYktvHqVQV96DCBw4D3ZSNVjWhpXuSC9dLZcCQQClWUtYL8Bt7SRfx1keS4ONBz6YZrxLZHr5tJVdU8P8jbLSdoLN/hIl90g8aLYE789DVZFNC4mOajYiJia/dJj9AkBiFhG3fTiY8MCCx7iCjRZuWHFPLwl14BaTalBsMQC3QItr+7EcLo+2HiN2EMgs0oYwfQpBMRz/eMaVuJbdFP8xAkEAkcPxfxHBs2berS0BbIqnszkSvqm7Dz/Khb3j+z1wRoHohk+BqvVzrFKeNNses6Vxc2vIx0IHhywtAtfdSuUQRQJAODn3cvaCeKkqhfJOpwBWnQFpDdJ+5TKiaRw2UqFVMTj8acejKjB8B9gIr/a3nmL+P/7oXtRxikXZ1mEm/9XRlA==
-----END RSA PRIVATE KEY-----";


$detailNo = "DE" . date("ymd", time());
$totalCount = 0;
$totalAmount = 0;

$details = [];
for($i=1; $i<=1; $i++){
    $amount = 1.02;

    $detail = new DetailVo();

    $itemName = str_pad($i, 5, "0", STR_PAD_LEFT);
    $detail->setDetailNo( $detailNo . $itemName);
    $detail->setName("都是交流交流发就发给对方感到我认为日u我认465dff34DWS34PO发的发生的34343，。？@！#%￥%~,;'=》》‘；【】@发生的开发商的方式飞机克里斯多夫快回家的思考方式对方老师的讲课费" . $itemName);
    $detail->setCount(1);
    $detail->setAmount($amount);

    //加密
    $detail->setName(AESUtil::encryptECB($detail->getName(), $aesKey, $iv));

    $totalCount = $totalCount + $detail->getCount();
    $totalAmount = $totalAmount + $amount;

    array_push($details, $detail);
}

$batchVo = new BatchVo();
$batchVo->setBatchNo("BA" . date("yymd", time()) . "00001");
$batchVo->setRequestTime(date("Y-m-d H:i:s", time()));
$batchVo->setTotalCount($totalCount);
$batchVo->setTotalAmount($totalAmount);
$batchVo->setDetails($details);
$dataStr = json_encode($batchVo, JSON_UNESCAPED_UNICODE);//JSON_UNESCAPED_UNICODE避免中文转码

$request = new RequestParam();
$request->setMethod("demo.batch");
$request->setVersion("1.0");
$request->setMchNo("888000000000000");
$request->setSignType("2");
$request->setRandStr(RandomUtil::randomStr(32));
$request->setData($dataStr);
$request->setSecKey($aesKey);//rsa有效

$secretKey = new SecretKey();
if("1" == $request->getSign()){
    $secretKey->setReqSignPriKey($md5Key);
    $secretKey->setRespVerifyPubKey($md5Key);
    $secretKey->setSecKeyEncryptPubKey($md5Key);
    $secretKey->setSecKeyDecryptPriKey($md5Key);
}else{
    $secretKey->setReqSignPriKey($mchPrivateKey);//签名用商户私钥
    $secretKey->setRespVerifyPubKey($sysPublicKey);//验签用系统公钥
    $secretKey->setSecKeyEncryptPubKey($sysPublicKey);//加密用系统公钥
    $secretKey->setSecKeyDecryptPriKey($mchPrivateKey);//解密用商户私钥
}

$url = "127.0.0.1:8099/test";
try{
    $response = RequestUtil::doRequest($url, $request, $secretKey);
    print_r($response);
}catch(Exception $e){
    print_r($e);
}


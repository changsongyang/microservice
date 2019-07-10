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
$sysPublicKey = '-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCD1PquVQz6inIH66ZMndawRmihQ/4GLX/nHieaX8Htu5NZcn2hB3OZe+rk05AJgcUuUhkNqxhtkArOJJdhxxdF4BNFSQ70Zx9APuda4GgwGnpiA5yJey9awmsmUUS/k4KkQX6bLJWvbKz7TEa5Z6NDD7UBoYu6uFqZH+AL51IlQIDAQAB
-----END PUBLIC KEY-----';
$sysPrivateKey = '-----BEGIN RSA PRIVATE KEY-----
MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIIPU+q5VDPqKcgfrpkyd1rBGaKFD/gYtf+ceJ5pfwe27k1lyfaEHc5l76uTTkAmBxS5SGQ2rGG2QCs4kl2HHF0XgE0VJDvRnH0A+51rgaDAaemIDnIl7L1rCayZRRL+TgqRBfpssla9srPtMRrlno0MPtQGhi7q4Wpkf4AvnUiVAgMBAAECgYBHSsehHr29R1pnzJYUe8lZAghfQbkjMchxuP+VNhbfz7KI0ocGjh0Yil/6GOEH4NB416eK5z1OwmwiRPxWMD2nMFfwgSpH+tewAl6raNhTy9fumyQD6ZNs3y8swCj9e54P4Ph3B+u/OUDB1BZQu6zb2pO0FNIbFPsxPlBN5FDQcQJBAMz/RHGKG16kdTdYyHSHXLR4qtk2xik798i8i9CDJ+OnKfc8VCvGNilWoR6S4a+FcJHEhYs5QcRxNsCClmd0md8CQQCiayLa/sS2lY4dgY3n/G12cAQVhqPSyx8QGcqtLl3jTJQLUbO0fSLo542ZV4azgc/j+f0C/tML4mAY2IozktQLAkBTlQzyAi5woztLqr5ojLxmtQBr+iJHs7SuuvmCtccw0fqRXJ6xDmsM5c5hqd+s8gpY1LjicCD5mHOLgHMUkX0fAkBlR4+Vpha+kGXtalM2HUeY+mLhlXLkyHrXTG4BLg+n5KHQqSL5Yqr5NyMqQtUhbMpZLBMk4ghyubgY5jbP0DhfAkB7+lMimzHiMjLh56AZnsg3UFH+MoupkctS4oseK5vET70tSO0xiUhikf3+0BXZNhnsSfnR493ScDQbyYDKKY1d
-----END RSA PRIVATE KEY-----';


$mchPublicKey = '-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkMtwOfeouGA5T8w8bv5xA4nV1aCTDNxU7T+kMwhkQpNT329k8S+HcIQc4t8CnivpZ5ZgquXA94MUH42S3AO3BTuCQCghhob+iWg0m9SohLh5GYloBlgI+OBFIpynib/dFfCwtLCS/afFsd4PDhrISx6M1cPv0A10QY2JOO1INpQIDAQAB
-----END PUBLIC KEY-----';
$mchPrivateKey = '-----BEGIN RSA PRIVATE KEY-----
MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQy3A596i4YDlPzDxu/nEDidXVoJMM3FTtP6QzCGRCk1Pfb2TxL4dwhBzi3wKeK+lnlmCq5cD3gxQfjZLcA7cFO4JAKCGGhv6JaDSb1KiEuHkZiWgGWAj44EUinKeJv90V8LC0sJL9p8Wx3g8OGshLHozVw+/QDXRBjYk47Ug2lAgMBAAECgYA4A5WoZ/H8eX5hyxgLWklepSJ2w+lOozrd+fvBu3E7iU+RonEwLZ7GLoo9IgpZ3YJcKoPHh20v3r64Wy1fdLSmYlQ1Lk/DasEshXthwWKam+w+lBh7QS+jnChSNxlCzMebQUhKCzFV4Du28ROVVYU/UTS76+LlL5TgwOw/owSQQQJBANX1V4vw2GsS7ri7dR9gJUMl7B80/ciXEMTk1/jO6OfDfhMhWUgHPndTo+OVgyLgpagmeDFSbCCfN1Oa6kwU29UCQQDEdnn46UR1Ye0pYxu1p5YvY4wC036OX4XxLR94DShu24d104prN0ogni6pc6Jh7vtkE1LyM4sh2EiL5x/48mKRAkAUv0StAj7KKzzQ1wSldTpHx56c7BOL5vIuVY6HxvCYwMEx87LnpCQviAHFaNMdh7EonApdpgNsKmRADC6aEA+9AkB24yc+jJLD4eWttO7wx6BnvvrcPvYH3CBm6SJw+K1uIGTh1YifBw9Rm8eq/XHXh9ITJmp8bNqWOZb1KoE7mhoxAkBCxC++0ACafWKKFp8baJmILhdTu0BDKxvXflF5xWpBn2nCOY6eztJYZ9acnzI2HTL4XLe2tYFSr7V8u0e/SN0h
-----END RSA PRIVATE KEY-----';

$detailNo = "DE" . date("ymd", time());
$totalCount = 0;
$totalAmount = 0;

$details = [];
for($i=1; $i<=1; $i++){
    $amount = 1.02;

    $detail = new DetailVo();

    $itemName = str_pad($i, 5, "0", STR_PAD_LEFT);
    $detail->setDetailNo($detailNo . $itemName);
    $detail->setName("明细" . $itemName);
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


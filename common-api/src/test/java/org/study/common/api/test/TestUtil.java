package org.study.common.api.test;

import org.study.common.api.enums.SignTypeEnum;
import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.test.vo.BatchVo;
import org.study.common.api.test.vo.DetailVo;
import org.study.common.util.utils.AESUtil;
import org.study.common.util.utils.DateUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.RandomUtil;

import java.math.BigDecimal;
import java.util.*;

public class TestUtil {

    public static void main(String[] args){

        String testKey = "JNJqExBv3uikdbl5";
        String encryed = AESUtil.encryptECB("发的发生的211fsdfds", testKey);
        System.out.println(encryed);
        System.out.println(AESUtil.decryptECB(encryed, testKey));
        System.exit(1);

        String aesKey = RandomUtil.get16LenStr();

        String md5Key = "12345678qwertyui";
        String sysPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCD1PquVQz6inIH66ZMndawRmihQ/4GLX/nHieaX8Htu5NZcn2hB3OZe+rk05AJgcUuUhkNqxhtkArOJJdhxxdF4BNFSQ70Zx9APuda4GgwGnpiA5yJey9awmsmUUS/k4KkQX6bLJWvbKz7TEa5Z6NDD7UBoYu6uFqZH+AL51IlQIDAQAB";
        String sysPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIIPU+q5VDPqKcgfrpkyd1rBGaKFD/gYtf+ceJ5pfwe27k1lyfaEHc5l76uTTkAmBxS5SGQ2rGG2QCs4kl2HHF0XgE0VJDvRnH0A+51rgaDAaemIDnIl7L1rCayZRRL+TgqRBfpssla9srPtMRrlno0MPtQGhi7q4Wpkf4AvnUiVAgMBAAECgYBHSsehHr29R1pnzJYUe8lZAghfQbkjMchxuP+VNhbfz7KI0ocGjh0Yil/6GOEH4NB416eK5z1OwmwiRPxWMD2nMFfwgSpH+tewAl6raNhTy9fumyQD6ZNs3y8swCj9e54P4Ph3B+u/OUDB1BZQu6zb2pO0FNIbFPsxPlBN5FDQcQJBAMz/RHGKG16kdTdYyHSHXLR4qtk2xik798i8i9CDJ+OnKfc8VCvGNilWoR6S4a+FcJHEhYs5QcRxNsCClmd0md8CQQCiayLa/sS2lY4dgY3n/G12cAQVhqPSyx8QGcqtLl3jTJQLUbO0fSLo542ZV4azgc/j+f0C/tML4mAY2IozktQLAkBTlQzyAi5woztLqr5ojLxmtQBr+iJHs7SuuvmCtccw0fqRXJ6xDmsM5c5hqd+s8gpY1LjicCD5mHOLgHMUkX0fAkBlR4+Vpha+kGXtalM2HUeY+mLhlXLkyHrXTG4BLg+n5KHQqSL5Yqr5NyMqQtUhbMpZLBMk4ghyubgY5jbP0DhfAkB7+lMimzHiMjLh56AZnsg3UFH+MoupkctS4oseK5vET70tSO0xiUhikf3+0BXZNhnsSfnR493ScDQbyYDKKY1d";

        String mchPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkMtwOfeouGA5T8w8bv5xA4nV1aCTDNxU7T+kMwhkQpNT329k8S+HcIQc4t8CnivpZ5ZgquXA94MUH42S3AO3BTuCQCghhob+iWg0m9SohLh5GYloBlgI+OBFIpynib/dFfCwtLCS/afFsd4PDhrISx6M1cPv0A10QY2JOO1INpQIDAQAB";
        String mchPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQy3A596i4YDlPzDxu/nEDidXVoJMM3FTtP6QzCGRCk1Pfb2TxL4dwhBzi3wKeK+lnlmCq5cD3gxQfjZLcA7cFO4JAKCGGhv6JaDSb1KiEuHkZiWgGWAj44EUinKeJv90V8LC0sJL9p8Wx3g8OGshLHozVw+/QDXRBjYk47Ug2lAgMBAAECgYA4A5WoZ/H8eX5hyxgLWklepSJ2w+lOozrd+fvBu3E7iU+RonEwLZ7GLoo9IgpZ3YJcKoPHh20v3r64Wy1fdLSmYlQ1Lk/DasEshXthwWKam+w+lBh7QS+jnChSNxlCzMebQUhKCzFV4Du28ROVVYU/UTS76+LlL5TgwOw/owSQQQJBANX1V4vw2GsS7ri7dR9gJUMl7B80/ciXEMTk1/jO6OfDfhMhWUgHPndTo+OVgyLgpagmeDFSbCCfN1Oa6kwU29UCQQDEdnn46UR1Ye0pYxu1p5YvY4wC036OX4XxLR94DShu24d104prN0ogni6pc6Jh7vtkE1LyM4sh2EiL5x/48mKRAkAUv0StAj7KKzzQ1wSldTpHx56c7BOL5vIuVY6HxvCYwMEx87LnpCQviAHFaNMdh7EonApdpgNsKmRADC6aEA+9AkB24yc+jJLD4eWttO7wx6BnvvrcPvYH3CBm6SJw+K1uIGTh1YifBw9Rm8eq/XHXh9ITJmp8bNqWOZb1KoE7mhoxAkBCxC++0ACafWKKFp8baJmILhdTu0BDKxvXflF5xWpBn2nCOY6eztJYZ9acnzI2HTL4XLe2tYFSr7V8u0e/SN0h";

        String detailNo = "DE" + DateUtil.formatShortDate(new Date());
        Integer totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<DetailVo> detailList = new ArrayList<>();
        for(int i=1; i<=10; i++){
            Double amount = 1.02;

            DetailVo detail = new DetailVo();

            String itemName = String.format("%1$05d", i);
            detail.setDetail_no(detailNo + itemName);
            detail.setName("明细" + itemName);
            detail.setCount(1);
            detail.setAmount(String.valueOf(amount));

            detail.setName(AESUtil.encryptECB(detail.getName(), aesKey));//加密

            totalCount = totalCount + detail.getCount();
            totalAmount = totalAmount.add(BigDecimal.valueOf(amount));

            detailList.add(detail);
        }
        BatchVo batchVo = new BatchVo();
        batchVo.setBatch_no("BA" + DateUtil.formatShortDate(new Date()) + "00001");
        batchVo.setRequest_time(DateUtil.formatDateTime(new Date()));
        batchVo.setTotal_count(totalCount);
        batchVo.setTotal_amount(totalAmount.toString());
        batchVo.setDetails(detailList);

        final RequestParam request = new RequestParam();
        request.setMethod("demo.batch");
        request.setVersion("1.0");
        request.setMch_no("888000000000000");
        request.setSign_type(SignTypeEnum.RSA.getValue());
        request.setRand_str(RandomUtil.get32LenStr());
        request.setData(JsonUtil.toString(batchVo));
        request.setSec_key(aesKey);//rsa有效

        final SecretKey key = new SecretKey();
        if(SignTypeEnum.MD5.getValue().equals(request.getSign_type())){
            key.setReqSignPriKey(md5Key);
            key.setRespVerifyPubKey(md5Key);
            key.setSecKeyEncryptPubKey(md5Key);
            key.setSecKeyDecryptPriKey(md5Key);
        }else{
            key.setReqSignPriKey(mchPrivateKey);//签名用商户私钥
            key.setRespVerifyPubKey(sysPublicKey);//验签用系统公钥
            key.setSecKeyEncryptPubKey(sysPublicKey);//加密用系统公钥
            key.setSecKeyDecryptPriKey(mchPrivateKey);//解密用商户私钥
        }

        int maxThread = 1;
        final String url = "127.0.0.1:8099/test";
        for(int i=1; i<=maxThread; i++){
            final String index = String.valueOf(i);
            new Thread(new Runnable() {
                public void run() {
                    try{
                        ResponseParam response = RequestUtil.doRequest(url, request, key);
                        System.out.println("第 "+index+" 个 Response = " + JsonUtil.toString(response));

                        BatchVo batchVo1 = JsonUtil.toBean(response.getData(), BatchVo.class);
                        System.out.println("第 "+index+" 个 Response.Data = " + JsonUtil.toString(batchVo1));
                    }catch(Throwable e){
                        System.out.println("第 "+index+" 个 发生异常： " + e.getMessage());
                    }
                }
            }).start();
        }
    }
}

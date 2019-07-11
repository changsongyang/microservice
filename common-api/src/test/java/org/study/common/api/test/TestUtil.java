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
        String aesKey = RandomUtil.get16LenStr();

        String md5Key = "12345678qwertyui";
        String sysPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCd/fouQL2zw3wDi+qL8yF8teLXt0tBUSCC8I2d7H5o+2/blT9/pg/Nw9PJxXO0bVZeNubmqSLycieS50vR8zEcsJPad8FpKVxSC1DWNfKyma3qY++9yxJPkp951Ho74RCkSJJrwBfXZMbkc27T+K9OcAZRyQJNMSTpz+YhChb4pQIDAQAB";
        String sysPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ39+i5AvbPDfAOL6ovzIXy14te3S0FRIILwjZ3sfmj7b9uVP3+mD83D08nFc7RtVl425uapIvJyJ5LnS9HzMRywk9p3wWkpXFILUNY18rKZrepj773LEk+Sn3nUejvhEKRIkmvAF9dkxuRzbtP4r05wBlHJAk0xJOnP5iEKFvilAgMBAAECgYApujOCVc0ElmPBmAmZbtxwUKWZ7aotlRyuGJR+mkCEv6u6Zf/AWf6gjND54HF/vMTr2zo+v3sgZ2/2R6ppx/43NzR27pop/NMdiuONIsFC+/hIHWDUOoBCwaMnfh13pQqlwEm/zO4JRE/UQmiKJcGtcc/REDf2/4chGKurBTKoHQJBAOkIaFI5eGvukroyarxjuA0IJ9obrQ7bIVIobwtW8voidUont93BK/88IIu04Iu2jayytP3kz8a0+rsdmo+3GjsCQQCtkDxeg/QCOvAyntdKE6YYWY+El/MqRoObO4Z4be7040lSKkaxwX6m2m41XK1NS0r/ca6wVIFm2y6Et6tWMCqfAkEAjmoo9zdQNQYUfd6aBJAcxzoYwN7xIIcjEgbL9m4pCF1OuQcVA10u+klQypC8OiZS5xxAKHpR0OqB4SDyeKo6SQJAcy+0QO3FtO00mAO+0ZS0uJhHnTHS2Y2urgkVNzuOSMvGz1brT/Eggs+YMKXvBcsgXOMvkiqjLoXsG3xho3OX9QJBAIyeF4jt9R3zbBkS5X2+gRaaICja/hLwQfg+s3K/2K+AmehmuP+EHyCARe8LNcfXzrRSSW6XokMjgh65+k615Bw=";

        String mchPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMFJWpm+IGgstDlxuGzbNs+E0x75PeCmVCOyEG8pmXRpNpBiV86pnuYgKcL2nSuqHT1oA9Bgvtf4gcxSOlBzLsXmIcHKpoMOqdA3D05Yy2Zw70MOKJA6uSLuCqwz6flh6CtKFVTateAjXfkusOv7TC0mxpmYT4ruagWoMcc5O2OwIDAQAB";
        String mchPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIwUlamb4gaCy0OXG4bNs2z4TTHvk94KZUI7IQbymZdGk2kGJXzqme5iApwvadK6odPWgD0GC+1/iBzFI6UHMuxeYhwcqmgw6p0DcPTljLZnDvQw4okDq5Iu4KrDPp+WHoK0oVVNq14CNd+S6w6/tMLSbGmZhPiu5qBagxxzk7Y7AgMBAAECgYBB2qOJeylFWlPo0K82Lpo9jnXsFe90IXr9KgMa2w5t2dYPN76D/V6kfRsxBfFAClFt35emGKOe4afBrsRVHw9G8F6XDrSds1wNUZ2xTBzCd4pWVgBqmp5r44vpd1vrXEQKQgmd3if2EggXe2ZAlUvAPt6RmGwUc9F/NKvHk3QBEQJBANjg3uHLw6nZY1I53JLOz/Bf5uo84QS2Pn4tOMiqo9QQ4enQBbveYktvHqVQV96DCBw4D3ZSNVjWhpXuSC9dLZcCQQClWUtYL8Bt7SRfx1keS4ONBz6YZrxLZHr5tJVdU8P8jbLSdoLN/hIl90g8aLYE789DVZFNC4mOajYiJia/dJj9AkBiFhG3fTiY8MCCx7iCjRZuWHFPLwl14BaTalBsMQC3QItr+7EcLo+2HiN2EMgs0oYwfQpBMRz/eMaVuJbdFP8xAkEAkcPxfxHBs2berS0BbIqnszkSvqm7Dz/Khb3j+z1wRoHohk+BqvVzrFKeNNses6Vxc2vIx0IHhywtAtfdSuUQRQJAODn3cvaCeKkqhfJOpwBWnQFpDdJ+5TKiaRw2UqFVMTj8acejKjB8B9gIr/a3nmL+P/7oXtRxikXZ1mEm/9XRlA==";

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
        request.setData(JsonUtil.toString(batchVo));//设置json格式字符串
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

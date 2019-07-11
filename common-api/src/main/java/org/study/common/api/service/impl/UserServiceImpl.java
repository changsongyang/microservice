package org.study.common.api.service.impl;

import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.study.common.api.enums.SignTypeEnum;
import org.study.common.api.params.APIParam;
import org.study.common.api.service.UserService;
import org.study.common.api.vo.MerchantInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 获取商户信息的实现类，为提高性能，建议加入缓存
 * @author: chenyf
 * @Date: 2019-02-24
 */
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Cache<String, MerchantInfo> cache;//使用本地缓存，避免网络传输的开销

    public UserServiceImpl(@Nullable Cache<String, MerchantInfo> cache){
        this.cache = cache;
    }

    /**
     * 根据商户编号获取商户信息
     * @param mchNo
     * @return
     */
    @Override
    public MerchantInfo getMerchantInfo(String mchNo, APIParam param){
        String key = getCacheKey(mchNo, param.getVersion());
        MerchantInfo merchantInfo = this.getFromCache(key);
        if(merchantInfo == null){
            merchantInfo = this.getFromDb(mchNo, param);
            this.storeToCache(key, merchantInfo);
        }
        return merchantInfo;
    }

    private String getCacheKey(String mchNo, String version){
        return mchNo + "_" + version;
    }

    private MerchantInfo getFromCache(String key){
        if (cache != null) {
            return (MerchantInfo) cache.getIfPresent(key);
        }
        return null;
    }

    private void storeToCache(String key, MerchantInfo merchantInfo) {
        if (cache != null && merchantInfo != null) {
            cache.put(key, merchantInfo);
        }
    }

    private MerchantInfo getFromDb(String mchNo, APIParam param){
        MerchantInfo merchantInfo = new MerchantInfo();

        String md5Key = "12345678qwertyui";
        String sysPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCd/fouQL2zw3wDi+qL8yF8teLXt0tBUSCC8I2d7H5o+2/blT9/pg/Nw9PJxXO0bVZeNubmqSLycieS50vR8zEcsJPad8FpKVxSC1DWNfKyma3qY++9yxJPkp951Ho74RCkSJJrwBfXZMbkc27T+K9OcAZRyQJNMSTpz+YhChb4pQIDAQAB";
        String sysPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ39+i5AvbPDfAOL6ovzIXy14te3S0FRIILwjZ3sfmj7b9uVP3+mD83D08nFc7RtVl425uapIvJyJ5LnS9HzMRywk9p3wWkpXFILUNY18rKZrepj773LEk+Sn3nUejvhEKRIkmvAF9dkxuRzbtP4r05wBlHJAk0xJOnP5iEKFvilAgMBAAECgYApujOCVc0ElmPBmAmZbtxwUKWZ7aotlRyuGJR+mkCEv6u6Zf/AWf6gjND54HF/vMTr2zo+v3sgZ2/2R6ppx/43NzR27pop/NMdiuONIsFC+/hIHWDUOoBCwaMnfh13pQqlwEm/zO4JRE/UQmiKJcGtcc/REDf2/4chGKurBTKoHQJBAOkIaFI5eGvukroyarxjuA0IJ9obrQ7bIVIobwtW8voidUont93BK/88IIu04Iu2jayytP3kz8a0+rsdmo+3GjsCQQCtkDxeg/QCOvAyntdKE6YYWY+El/MqRoObO4Z4be7040lSKkaxwX6m2m41XK1NS0r/ca6wVIFm2y6Et6tWMCqfAkEAjmoo9zdQNQYUfd6aBJAcxzoYwN7xIIcjEgbL9m4pCF1OuQcVA10u+klQypC8OiZS5xxAKHpR0OqB4SDyeKo6SQJAcy+0QO3FtO00mAO+0ZS0uJhHnTHS2Y2urgkVNzuOSMvGz1brT/Eggs+YMKXvBcsgXOMvkiqjLoXsG3xho3OX9QJBAIyeF4jt9R3zbBkS5X2+gRaaICja/hLwQfg+s3K/2K+AmehmuP+EHyCARe8LNcfXzrRSSW6XokMjgh65+k615Bw=";

        String mchPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMFJWpm+IGgstDlxuGzbNs+E0x75PeCmVCOyEG8pmXRpNpBiV86pnuYgKcL2nSuqHT1oA9Bgvtf4gcxSOlBzLsXmIcHKpoMOqdA3D05Yy2Zw70MOKJA6uSLuCqwz6flh6CtKFVTateAjXfkusOv7TC0mxpmYT4ruagWoMcc5O2OwIDAQAB";
        String mchPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIwUlamb4gaCy0OXG4bNs2z4TTHvk94KZUI7IQbymZdGk2kGJXzqme5iApwvadK6odPWgD0GC+1/iBzFI6UHMuxeYhwcqmgw6p0DcPTljLZnDvQw4okDq5Iu4KrDPp+WHoK0oVVNq14CNd+S6w6/tMLSbGmZhPiu5qBagxxzk7Y7AgMBAAECgYBB2qOJeylFWlPo0K82Lpo9jnXsFe90IXr9KgMa2w5t2dYPN76D/V6kfRsxBfFAClFt35emGKOe4afBrsRVHw9G8F6XDrSds1wNUZ2xTBzCd4pWVgBqmp5r44vpd1vrXEQKQgmd3if2EggXe2ZAlUvAPt6RmGwUc9F/NKvHk3QBEQJBANjg3uHLw6nZY1I53JLOz/Bf5uo84QS2Pn4tOMiqo9QQ4enQBbveYktvHqVQV96DCBw4D3ZSNVjWhpXuSC9dLZcCQQClWUtYL8Bt7SRfx1keS4ONBz6YZrxLZHr5tJVdU8P8jbLSdoLN/hIl90g8aLYE789DVZFNC4mOajYiJia/dJj9AkBiFhG3fTiY8MCCx7iCjRZuWHFPLwl14BaTalBsMQC3QItr+7EcLo+2HiN2EMgs0oYwfQpBMRz/eMaVuJbdFP8xAkEAkcPxfxHBs2berS0BbIqnszkSvqm7Dz/Khb3j+z1wRoHohk+BqvVzrFKeNNses6Vxc2vIx0IHhywtAtfdSuUQRQJAODn3cvaCeKkqhfJOpwBWnQFpDdJ+5TKiaRw2UqFVMTj8acejKjB8B9gIr/a3nmL+P/7oXtRxikXZ1mEm/9XRlA==";

        merchantInfo.setMchNo(mchNo);
        merchantInfo.setMchName("xx商户");
        merchantInfo.setMchStatus(100);
        merchantInfo.setSignType(SignTypeEnum.RSA.getValue());

        //默认使用这个共享密钥作为签名、验签、加密、解密
        if(SignTypeEnum.MD5.getValue().equals(merchantInfo.getSignType())){
            merchantInfo.setSignValidKey(md5Key);
            merchantInfo.setSignGenerateKey(md5Key);
            merchantInfo.setSecKeyDecryptKey(md5Key);
            merchantInfo.setSecKeyEncryptKey(md5Key);
        }else{
            merchantInfo.setSignValidKey(mchPublicKey);//验签用商户公钥
            merchantInfo.setSignGenerateKey(sysPrivateKey);//签名用系统密钥
            merchantInfo.setSecKeyDecryptKey(sysPrivateKey);//解密用系统密钥
            merchantInfo.setSecKeyEncryptKey(mchPublicKey);//加密用商户公钥
        }

        return merchantInfo;
    }
}

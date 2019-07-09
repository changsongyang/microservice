package com.gw.api.base.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gw.api.base.clients.RedisClient;
import com.gw.api.base.constants.IPValidKeyConst;
import com.gw.api.base.enums.SignTypeEnum;
import com.gw.api.base.service.UserService;
import com.gw.api.base.params.APIParam;
import com.gw.api.base.vo.MerchantInfo;
import com.gw.facade.user.entity.MerchantOnline;
import com.gw.facade.user.entity.MerchantSecret;
import com.gw.facade.user.service.MerchantOnlineFacade;
import com.gw.facade.user.service.MerchantSecretFacade;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 获取商户信息的实现类，为提高性能，建议加入缓存
 * @author: chenyf
 * @Date: 2019-02-24
 */
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String GATEWAY_MERCHANT_INFO_LOCAL_CACHE_KEY = "GATEWAY_MERCHANT_INFO_LOCAL_CACHE_KEY";

    private RedisClient redisClient;
    private LocalCachedMapOptions cacheOptions;//本地缓存时 过期时间、最大缓存数 等等的配置
    private RLocalCachedMap<String, MerchantInfo> mchInfoCacheMap;//使用本地缓存，避免网络传输的开销

    @Reference(check = false)
    MerchantOnlineFacade merchantOnlineFacade;
    @Reference(check = false)
    private MerchantSecretFacade merchantSecretFacade;

    public UserServiceImpl(@Nullable RedisClient redisClient, @Nullable LocalCachedMapOptions localCachedMapOptions){
        this.redisClient = redisClient;
        this.cacheOptions = localCachedMapOptions;
        initRedisLocalCache();
    }

    private void initRedisLocalCache(){
        if(redisClient != null && cacheOptions != null){
            logger.warn("本地缓存开启，将使用本地缓存存储商户信息 cacheSize={} cacheExpireMills={} maxIdleMills={}",
                    cacheOptions.getCacheSize(), cacheOptions.getTimeToLiveInMillis(), cacheOptions.getMaxIdleInMillis());
            mchInfoCacheMap = redisClient.getClient().getLocalCachedMap(GATEWAY_MERCHANT_INFO_LOCAL_CACHE_KEY, cacheOptions);
        }else{
            logger.warn("未开启本地缓存，频繁从数据库取商户信息将严重影响性能，同时给数据库带来巨大压力");
        }
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
        if(mchInfoCacheMap != null){
            return mchInfoCacheMap.get(key);
        }
        return null;
    }

    private void storeToCache(String key, MerchantInfo merchantInfo){
        if(mchInfoCacheMap != null && merchantInfo != null){
            mchInfoCacheMap.fastPutIfAbsentAsync(key, merchantInfo);
        }
    }

    private MerchantInfo getFromDb(String mchNo, APIParam param){
        MerchantInfo merchantInfo = new MerchantInfo();

        MerchantOnline merchantOnline = merchantOnlineFacade.getMerchantOnlineByMerchantNo(mchNo);
        if(merchantOnline == null || merchantOnline.getEncryptType() == null){
            throw new RuntimeException("mchNo="+mchNo+" 对应的商户信息为空或者加密类型为空");
        }

        merchantInfo.setMchNo(mchNo);
        merchantInfo.setMchName(merchantOnline.getFullName());
        if(merchantOnline.getEncryptType().intValue() == SignTypeEnum.getIntValue(SignTypeEnum.MD5)){
            merchantInfo.setSignType(SignTypeEnum.MD5.getValue());
        }else if(merchantOnline.getEncryptType().intValue() == SignTypeEnum.getIntValue(SignTypeEnum.RSA)){
            merchantInfo.setSignType(SignTypeEnum.RSA.getValue());
        }else{
            //此处勿抛出ApiException，因为此时不确定是否受理失败
            throw new RuntimeException("暂不支持的签名类型:"+merchantOnline.getEncryptType().intValue());
        }

        //默认使用这个共享密钥作为签名、验签、加密、解密
        merchantInfo.setSignValidKey(merchantOnline.getMerchantKey());
        merchantInfo.setSignGenerateKey(merchantOnline.getMerchantKey());
        merchantInfo.setSecKeyDecryptKey(merchantOnline.getMerchantKey());
        merchantInfo.setSecKeyEncryptKey(merchantOnline.getMerchantKey());

        if(merchantOnline.getEncryptType().intValue() == SignTypeEnum.getIntValue(SignTypeEnum.RSA)){
            MerchantSecret merchantSecret = merchantSecretFacade.getByMerchantNo(mchNo);

            merchantInfo.setSignValidKey(merchantSecret.getMerchantPublicKey());//验签，是使用商户公钥来验证
            merchantInfo.setSignGenerateKey(merchantSecret.getPrivateKey());//响应商户时，需要生成签名，使用汇聚平台私钥

            //因为现在还没有分开签名(验签)公私钥、加解密公私钥，所以还是共用同一把公私钥
            merchantInfo.setSecKeyDecryptKey(merchantSecret.getPrivateKey());//收到商户请求，需要解密sec_key时，肯定使用汇聚密钥
            merchantInfo.setSecKeyEncryptKey(merchantSecret.getMerchantPublicKey());//响应商户，需要加密sec_key，肯定使用商户公钥
        }

        //设置ip校验的地址
        Map<String, String> ipValidMap = new HashMap<>();
        ipValidMap.put(IPValidKeyConst.RECEIVE_KEY, merchantOnline.getIpSeg());//收单
        ipValidMap.put(IPValidKeyConst.PAYMENT_KEY, merchantOnline.getPayIpSeg());//代付
        merchantInfo.setIpValidMap(ipValidMap);

        return merchantInfo;
    }
}

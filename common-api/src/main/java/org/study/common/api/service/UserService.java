package com.gw.api.base.service;

import com.gw.api.base.params.APIParam;
import com.gw.api.base.vo.MerchantInfo;

/**
 * 获取用户信息，需要用户自行实现并配置SpringBean
 * @author chenyf
 * @date 2018-12-15
 */
public interface UserService {
    /**
     * 根据商户编号获取商户信息
     * @param mchNo
     * @return
     */
    public MerchantInfo getMerchantInfo(String mchNo, APIParam param);
}

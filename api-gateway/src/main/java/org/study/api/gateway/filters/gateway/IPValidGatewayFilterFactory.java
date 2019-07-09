package com.gw.api.gateway.filters.gateway;

import com.gw.api.base.enums.BizCodeEnum;
import com.gw.api.base.exceptions.ApiException;
import com.gw.api.base.helpers.RequestHelper;
import com.gw.api.base.params.APIParam;
import com.gw.api.base.params.RequestParam;
import com.gw.api.base.service.ValidFailService;
import com.gw.api.base.utils.JsonUtil;
import com.gw.api.base.utils.StringUtil;
import com.gw.api.gateway.config.conts.InnerErrorCode;
import com.gw.api.gateway.config.conts.ReqCacheKey;
import com.gw.api.gateway.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Arrays;
import java.util.List;

/**
 * @description IP校验过滤器(本过滤器不是全局过滤器，如需使用，需要在配置文件中配置)
 * @author chenyf
 * @date 2019-02-23
 */
public class IPValidGatewayFilterFactory extends AbstractGatewayFilterFactory<IPValidGatewayFilterFactory.Config> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final String IP_VALID_KEY = "ipValidKey";

	@Autowired
	private RequestHelper requestHelper;
	@Autowired
	private ValidFailService validFailService;

	public IPValidGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList(IP_VALID_KEY);
	}

	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			String ip = RequestUtil.getIpAddr(request);
			RequestParam requestParam = (RequestParam) exchange.getAttributes().get(ReqCacheKey.CACHE_REQUEST_BODY_OBJECT_KEY);

			boolean isVerifyOk = false;
			String msg = "";

			String expectIp = "";
			String ipValidKey = config.getIpValidKey();
			if(StringUtil.isEmpty(ipValidKey)){
				isVerifyOk = false;
				msg = "Key为空，无法校验请求来源";
			}else{
				try{
					RequestHelper.Result<String> result = requestHelper.ipVerify(ip, ipValidKey, requestParam, new APIParam(requestParam.getVersion()));
					isVerifyOk = result.isVerifyOk();
					expectIp = result.getOtherInfo();
					if(! isVerifyOk){
						msg = "非法来源";
					}
				}catch (Throwable e){
					msg = "请求来源校验失败";
					isVerifyOk = false;
					logger.error("IP校验失败异常 RequestParam = {}", JsonUtil.toString(requestParam), e);
				}
			}

			if(! isVerifyOk){
				try{
					Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
					validFailService.afterIpValidFail(route.getId(), ip, expectIp, requestParam);
				}catch(Throwable e){
					logger.error("IP校验失败，IP校验失败处理器有异常 RequestParam = {}", JsonUtil.toString(requestParam), e);
				}
			}

			if(isVerifyOk){
				return chain.filter(exchange);
			}else{
				throw ApiException.acceptFail(BizCodeEnum.PARAM_VALID_FAIL.getCode(), msg)
						.innerCode(InnerErrorCode.IP_VALID_ERROR);
			}
		};
	}

	public static class Config {
		private String ipValidKey;

		public String getIpValidKey() {
			return ipValidKey;
		}

		public void setIpValidKey(String ipValidKey) {
			this.ipValidKey = ipValidKey;
		}
	}
}

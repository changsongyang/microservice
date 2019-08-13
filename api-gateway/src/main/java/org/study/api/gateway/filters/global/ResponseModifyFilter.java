package org.study.api.gateway.filters.global;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultClientResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;
import org.study.api.gateway.config.conts.FilterOrder;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.api.helpers.RequestHelper;
import org.study.common.api.params.APIParam;
import org.study.common.api.params.RequestParam;
import org.study.common.api.params.ResponseParam;
import org.study.common.api.utils.ResponseUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @description 响应体修改，包括：给响应体加签名 等
 * @author chenyf
 * @date 2019-02-23
 */
@Component
public class ResponseModifyFilter extends AbstractGlobalFilter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	RequestHelper requestHelper;

	/**
	 * 设置当前过滤器的执行顺序：本过滤器在全局过滤器中的顺序为倒数第1个，在响应给用户之前给响应参数加上签名
	 * @return
	 */
	@Override
	public int getOrder() {
		return FilterOrder.POST_LAST;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
				ResponseAdapter responseAdapter = new ResponseAdapter(body, httpHeaders);
				DefaultClientResponse clientResponse = new DefaultClientResponse(responseAdapter, ExchangeStrategies.withDefaults());

				//重新封装响应体，主要是：加签名
				Mono<String> modifiedBody = clientResponse.bodyToMono(String.class)
						.flatMap(originalBody -> {
							//转发到后端服务器之后，不管是正常返回还是抛出了异常，都会到达这里
							return Mono.just(buildNewResponseBody(exchange, originalBody));
						});

				BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
				CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, exchange.getResponse().getHeaders());
				return bodyInserter.insert(outputMessage, new BodyInserterContext())
						.then(Mono.defer(() -> {
							long contentLength1 = getDelegate().getHeaders().getContentLength();
							Flux<DataBuffer> messageBody = outputMessage.getBody();
							//TODO: if (inputStream instanceof Mono) {
							HttpHeaders headers = getDelegate().getHeaders();
							if (/*headers.getContentLength() < 0 &&*/ !headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
								messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
							}
							// }
							//TODO: use isStreamingMediaType?
							return getDelegate().writeWith(messageBody);
						}));
			}

			@Override
			public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
				return writeWith(Flux.from(body)
						.flatMapSequential(p -> p));
			}
		};

		return chain.filter(exchange.mutate().response(responseDecorator).build());
	}

	public String buildNewResponseBody(ServerWebExchange exchange, String originalBody){
		RequestParam requestParam = (RequestParam) exchange.getAttributes().get(CACHE_REQUEST_BODY_OBJECT_KEY);
		ResponseParam responseParam = JsonUtil.toBean(originalBody, ResponseParam.class);
		if(responseParam == null){
			responseParam = new ResponseParam();
		}

		try{
			if(StringUtil.isEmpty(responseParam.getMch_no())){
				responseParam.setMch_no(requestParam==null ? "" : requestParam.getMch_no());
			}
			if(StringUtil.isEmpty(responseParam.getSign_type())){
				responseParam.setSign_type(requestParam==null ? "" : requestParam.getSign_type());
			}
			ResponseUtil.fillAcceptUnknownIfEmpty(responseParam);
			//添加签名
			requestHelper.signAndEncrypt(responseParam, new APIParam(requestParam.getVersion()));
		}catch (Throwable e){
			if(responseParam.getSign() == null){
				responseParam.setSign("");
			}
			logger.error("给响应信息添加签名时异常 RequestParam = {} ResponseParam = {}", JsonUtil.toString(requestParam), JsonUtil.toString(responseParam), e);
		}

		return JsonUtil.toString(responseParam);
	}

	public class ResponseAdapter implements ClientHttpResponse {
		private final Flux<DataBuffer> flux;
		private final HttpHeaders headers;

		public ResponseAdapter(Publisher<? extends DataBuffer> body, HttpHeaders headers) {
			this.headers = headers;
			if (body instanceof Flux) {
				flux = (Flux) body;
			} else {
				flux = ((Mono)body).flux();
			}
		}

		@Override
		public Flux<DataBuffer> getBody() {
			return flux;
		}

		@Override
		public HttpHeaders getHeaders() {
			return headers;
		}

		@Override
		public HttpStatus getStatusCode() {
			return null;
		}

		@Override
		public int getRawStatusCode() {
			return 0;
		}

		@Override
		public MultiValueMap<String, ResponseCookie> getCookies() {
			return null;
		}
	}
}

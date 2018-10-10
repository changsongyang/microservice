package org.study.common.util.utils;

import com.alibaba.fastjson.TypeReference;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.common.statics.exceptions.BizException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT util code
 *
 * @author xbuding
 * @since 2017-6-28
 * @description JWT的生成、校验工具
 */
public class JwtUtil {
    /**
     * jwt的发布方
     */
    public static final String ISS = "iss";

    /**
     * jwt的接收方
     */
    public static final String AUD = "aud";

    /**
     * jwt所面向的用户
     */
    public static final String SUB = "sub";

    /**
     * jwt的签发时间
     */
    public static final String IAT = "iat";

    /**
     * jwt的过期时间
     */
    private static final String EXP = "exp";

    /**
     * 默认过期时间 30分钟
     */
    private static final int DEFAULT_EXP_TIMES = 1800;
    private final static String PAYLOAD_KEY = "PAYLOAD";
    private final static String FORMAT = "TOKEN : %s,错误信息:%s";
    private final static Logger logger = LoggerFactory.getLogger(JwtUtil.class);


    /**
     * 生成JWT的token，将使用默认的过期时间:30分钟
     * @param payload
     * @param secret
     * @param <T>
     * @return
     */
    public static <T> String genToken(T payload, String secret){
        return JwtUtil.genToken(payload, secret, DEFAULT_EXP_TIMES);
    }

    /**
     * 生成JWT的token，并指定过期时间，单位(秒)
     * @param payload
     * @param secret
     * @param expire
     * @param <T>
     * @return
     */
    public static <T> String genToken(T payload, String secret, int expire){
        return JwtUtil.genToken(payload, secret, expire, null);
    }

    /**
     * 生成JWT的token，并指定过期时间，单位(秒)、指定一些claims配置
     * @param payload
     * @param secret
     * @param expire
     * @param claims
     * @param <T>
     * @return
     */
    public static <T> String genToken(T payload, String secret, int expire, Map<String, Object> claims){
        if(claims == null){
            claims = new HashMap<>(3);
        }
        // 发布日期
        Date now = new Date();
        claims.put(IAT, now.getTime());
        // 有效日期
        Date exp = DateUtils.addSeconds(now, expire);
        claims.put(EXP, exp.getTime());

        claims.put(PAYLOAD_KEY, JsonUtil.toString(payload));
        JWTSigner signer = new JWTSigner(secret);
        return signer.sign(claims);
    }

    public static <T> T verify(String token, String secret, Class<T> cls){
        JWTVerifier verifier = new JWTVerifier(secret);
        try {
            Map<String,Object> claims = verifier.verify(token);
            if (claims.containsKey(EXP) && claims.containsKey(PAYLOAD_KEY)) {
                long exp = (long) claims.get(EXP);
                long currentTimeMillis = System.currentTimeMillis();
                if (exp > currentTimeMillis) {
                    String payload = (String) claims.get(PAYLOAD_KEY);
                    return JsonUtil.toBean(payload, cls);
                }
            }
        } catch (Exception e) {
            logger.error("Exception Occur While Verifying Token: "+token, e);
        }
        return null;
    }

    public static Map<String, Object> verify(String token, String secret, String iss, String aud) throws BizException{
        JWTVerifier verifier = new JWTVerifier(secret, aud, iss);
        try {
            return verifier.verify(token);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException | JWTVerifyException | IllegalStateException e) {
            throw new BizException(String.format(FORMAT, token, e.getMessage()));
        }
    }

    public static String sign(Map<String, Object> claims, String secret) {
        JWTSigner signer = new JWTSigner(secret);
        return signer.sign(claims);
    }

//    public static String getJsonWebToken(HttpServletRequest request) {
//        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authorizationHeader == null || StringUtils.isBlank(authorizationHeader)) {
//            throw new UnauthorizedException("Request's Authorization is null");
//        }
//        return authorizationHeader;
//    }

    public static Object getJwtPayloadValue(String token, String key) {
        String[] pieces = token.split("\\.");

        Map<String, Object> jwtPayload = null;
        try {
            jwtPayload = JsonUtil.toBean(new String(Base64.decodeBase64(pieces[1]), "UTF-8"),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (UnsupportedEncodingException e) {
            throw new BizException(e.getMessage());
        }
        if (jwtPayload == null) {
            throw new BizException("jwtPayload is null");
        }
        return jwtPayload.get(key);
    }
}

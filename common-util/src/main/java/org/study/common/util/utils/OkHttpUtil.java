package com.joinpay.sdk.utils;

import okhttp3.*;

import javax.net.ssl.*;
import java.net.Proxy;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final int CONNECT_TIMEOUT = 35;
    private static final int WRITE_TIMEOUT = 35;
    private static final int READ_TIMEOUT = 35;

    private static OkHttpClient httpClient = getOkHttpsClient();


    /**
     * post同步请求，提交Json数据
     * @param url
     * @param json
     * @return
     */
    public static String postJsonSync(String url, String json) throws Exception {
        final RequestBody requestBody = RequestBody.create(JSON_TYPE, json);
        final Request request = new Request.Builder()
                .url(getUrl(url))
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        return response.body().string();
    }

    public static OkHttpClient getOkHttpsClient() {
        try {
            final X509TrustManager trustManager = new X509TrustManager() {
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    if (chain == null) {
                        throw new IllegalArgumentException("checkServerTrusted: X509Certificate array is null");
                    }
                    if (!(chain.length > 0)) {
                        throw new IllegalArgumentException("checkServerTrusted: X509Certificate is empty");
                    }
                    try {
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                        tmf.init((KeyStore) null);
                        for (TrustManager trustManager : tmf.getTrustManagers()) {
                            ((X509TrustManager) trustManager).checkServerTrusted(chain, authType);
                        }
                    } catch (Exception e) {
                        throw new CertificateException(e);
                    }
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, trustManager);
            builder.proxy(Proxy.NO_PROXY); //不使用代理，避免被第三方使用代理抓包
            builder.hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
//                return "www.joinpay.com".equals(hostname) || "api.joinpay.com".equals(hostname);
                }
            });

            OkHttpClient okHttpClient = builder
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)//不允许重试
                    .build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getUrl(String url){
        if(url.startsWith("http://") || url.startsWith("https://")){
            return url;
        }else{
            return "http://" +  url;
        }
    }
}

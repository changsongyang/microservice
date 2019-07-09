package org.study.common.api.servlets;

import org.study.common.api.wrapper.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ServletUtil {

    public static String getRequestBody(HttpServletRequest request){
        if(request instanceof ContentCachingRequestWrapper){
            //解决Request Body只能读一次的问题
            return ((ContentCachingRequestWrapper) request).getRequestBody();
        }else{
            //如果直接从这里读取，会存在Request Body只能读一次的问题
            return readBody(request);
        }
    }

    public static String readBody(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}

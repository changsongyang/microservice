package org.study.demo.gateway.backend;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.study.common.util.utils.JsonUtil;
import org.study.demo.gateway.backend.vo.ResponseVo;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        byte[] paramArr = getJsonParam(request);
        if(paramArr != null && paramArr.length > 0){
            ResponseVo bean = JsonUtil.toBeanOrderly(paramArr, ResponseVo.class);//一定要维持顺序，否则会导致验签失败
            System.out.println(JsonUtil.toString(bean));
        }

        return true;
    }

    private static byte[] getJsonParam(HttpServletRequest request) throws Exception {
        int contentLength = request.getContentLength();
        byte[] charArr = new byte[contentLength];
        ServletInputStream inputStream = request.getInputStream();
        inputStream.read(charArr, 0, charArr.length);
        return charArr;
    }
}

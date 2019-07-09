package org.study.common.api.filters;

import org.study.common.api.wrapper.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求拦截器，通过两个封装类对HttpServletRequest、HttpServletResponse进行封装，以提供后续的验签、验参、生成签名等等的处理
 * @author chenyf
 * @date 2018-12-15
 */
public class ServletRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //设置 ServletRequest、ServletResponse 的包装类
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}

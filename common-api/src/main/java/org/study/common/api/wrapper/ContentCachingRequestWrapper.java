package org.study.common.api.wrapper;

import org.study.common.api.servlets.ServletUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 对HttpServletRequest的封装类，主要用以对请求数据的多次获取
 * @author chenyf
 * @date 2018-12-15
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
    private BufferedReader reader;
    private ServletInputStream inputStream;
    private String body;

    public ContentCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = ServletUtil.readBody(request);
        replaceInputContent(body);
    }

    public String getRequestBody(){
        return body;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
        }
        return reader;
    }

    /**
     * 替换输入的内容
     * @param content
     * @throws IOException
     */
    public void replaceInputContent(String content) throws IOException {
        if(inputStream != null){
            inputStream.close();//关闭资源
            inputStream = null;//等待垃圾回收
        }

        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        inputStream = new RequestCachingInputStream(data);

        if(reader != null){
            reader.close();
            reader = null;
        }
        body = content;
    }

    private static class RequestCachingInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        private boolean isClose;

        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            if(isClose){
                throw new IOException("the inputStream is close");
            }
            return inputStream.read();
        }

        @Override
        public void close() throws IOException {
            isClose = true;
            super.close();
            if(inputStream != null){
                inputStream.close();
            }
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readlistener) {
        }
    }
}

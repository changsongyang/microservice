package org.study.common.statics.exceptions;

import java.util.Map;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/8
 */
public class EsBizException extends BizException {
    private Map<String, String> failedDocuments;

    public EsBizException() {
        super();
    }

    public EsBizException(String message, Throwable cause) {
        super(message, cause);
    }

    public EsBizException(String message) {
        super(message);
    }

    public EsBizException(String message, Map<String, String> failedDocuments) {
        super(message);
        this.failedDocuments = failedDocuments;
    }

    public EsBizException(Throwable cause) {
        super(cause);
    }

    public Map<String, String> getFailedDocuments() {
        return failedDocuments;
    }
}

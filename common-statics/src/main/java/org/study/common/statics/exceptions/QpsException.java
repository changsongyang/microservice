package org.study.common.statics.exceptions;

public class QpsException extends BizException {
    /**
     * 是否被限制，包括限流、熔断等各种原因
     */
    private boolean isLimit;

    private Throwable ex;

    public QpsException(){
        super();
    }

    public QpsException(Throwable ex){
        super();
        this.ex = ex;
    }

    public QpsException(boolean isLimit, Throwable ex){
        super();
        this.isLimit = isLimit;
        this.ex = ex;
    }

    public boolean getIsLimit() {
        return isLimit;
    }

    public void setIsLimit(boolean isLimit) {
        this.isLimit = isLimit;
    }

    public Throwable getEx() {
        return ex;
    }

    public void setEx(Throwable ex) {
        this.ex = ex;
    }
}

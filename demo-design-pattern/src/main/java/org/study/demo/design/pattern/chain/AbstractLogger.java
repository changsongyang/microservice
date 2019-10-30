package org.study.demo.design.pattern.chain;

public abstract class AbstractLogger {
    public static final int ERROR = 1;   //一级日志
    public static final int WARN = 2;   //二级日志包括一级
    public static final int INFO = 3;    //三级包括前两个

    //日志级别
    protected int level;
    //责任链下一个元素
    protected AbstractLogger nextLogger;

    public AbstractLogger getNextLogger() {
        return nextLogger;
    }

    private void setNextLogger(AbstractLogger nextLogger) {
        this.nextLogger = nextLogger;
    }

    abstract protected void write(String message);

    public void info(String message){
        logMessage(INFO, "[INFO]", message);
    }

    public void error(String message){
        logMessage(ERROR, "[ERROR]", message);
    }

    public void warn(String message){
        logMessage(WARN, "[WARN]", message);
    }

    private void logMessage(int level, String prefix, String message){
        if (this.level >= level){ //根据传进来的日志等级,判断哪些责任链元素要去记录
            write(prefix + " " + message);
        }
        if (nextLogger != null){
            nextLogger.logMessage(level, prefix, message);//进行下一个责任链元素处理
        }
    }



    public static class Builder {
        private AbstractLogger first;
        private AbstractLogger last;

        public Builder addHandler(AbstractLogger logger) {
            if (this.first == null) {
                this.first = this.last = logger;
                return this;
            }else{
                this.last.setNextLogger(logger);
                this.last = logger;
                return this;
            }
        }

        public AbstractLogger build() {
            return this.first;
        }
    }
}

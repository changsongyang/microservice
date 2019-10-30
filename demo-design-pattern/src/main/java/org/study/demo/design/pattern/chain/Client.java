package org.study.demo.design.pattern.chain;

/**
 * 客户端
 *
 * 场景：实现 log4j 的日志级别功能
 *      1、a.log 文件中输出 INFO、WARN、ERROR 级别的日志
 *      2、b.log 文件中输出 WARN、ERROR 级别的日志
 *      3、c.log 文件中输出 ERROR 级别的日志
 */
public class Client {

    public static void main(String[] args) {
        AbstractLogger infoFileLogger = new FileLogger(AbstractLogger.INFO, "E://a.log");
        AbstractLogger errorFileLogger = new FileLogger(AbstractLogger.WARN, "E://b.log");
        AbstractLogger debugFileLogger = new FileLogger(AbstractLogger.ERROR, "E://c.log");

        AbstractLogger.Builder builder = new AbstractLogger.Builder();

        builder.addHandler(infoFileLogger)
                .addHandler(errorFileLogger)
                .addHandler(debugFileLogger);

        AbstractLogger logger = builder.build();
        logger.info("第一条日志记录");
        logger.warn("第二条日志记录");
        logger.error("第三条日志记录");
        logger.close();
    }
}

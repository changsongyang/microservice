package org.study.common.statics.annotations;

import java.lang.annotation.*;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/7
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {
    String indexName();

    String type() default "";

    boolean useServerConfiguration() default false;

    short shards() default 5;

    short replicas() default 1;

    String refreshInterval() default "1s";

    String indexStoreType() default "fs";

    boolean createIndex() default true;
}

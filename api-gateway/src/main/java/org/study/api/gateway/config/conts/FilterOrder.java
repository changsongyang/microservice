package org.study.api.gateway.config.conts;

/**
 * @description 过滤器执行顺序的常量配置类
 * @author: chenyf
 * @Date: 2019-02-20
 */
public class FilterOrder {
    //从-30到-21是非业务相关的前置全局过滤器，如：黑名单过滤器
    public final static int PRE_NONE_BIZ_FIRST = -30;

    //从-20到-1是业务相关的全局过滤器
    public final static int PRE_FIRST = -20;
    public final static int PRE_SECOND = -19;
    public final static int PRE_THIRD = -18;
    public final static int PRE_FOURTH = -17;
    public final static int PRE_FIFTH = -16;
    public final static int PRE_SIXTH = -15;
    public final static int PRE_SEVENTH = -14;
    public final static int PRE_EIGHTH = -13;

    public final static int POST_FIRST = -8;
    public final static int POST_SECOND = -7;
    public final static int POST_THIRD = -6;
    public final static int POST_FOURTH = -5;
    public final static int POST_FIFTH = -4;
    public final static int POST_SIXTH = -3;
    public final static int POST_LAST = -2;//最后一个过滤器，不能大于-1，否则修改的内容无法生效
}

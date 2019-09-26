package org.study.starter.component;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.study.common.statics.exceptions.QpsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 熔断器
 * 说明：
 *      1、一个jvm进程需确保只有一个实例
 *      2、一个jvm进程中请勿和sentinel的@SentinelResource混合使用
 *      3、一个jvm进程中本类勿和sentinel的动态数据源共用
 *      4、有前面几点限制的原因是 DegradeRuleManager.currentProperty是一个静态变量，且根据 DegradeRuleManager 的api设计来看，作者是希望
 *         currentProperty属性的值只允许全部替换，往里面追加或部分更新，所以，如果同一个jvm里面有多个地方调用了 DegradeRuleManager.loadRules()
 *         或者 DegradeRuleManager.register2Property()，其currentProperty属性的值就会被替换掉，所以就会导致有些配置被覆盖掉了
 */
public class CircuitBreaker {
    private Map<String, Config> resourceMap;

    public CircuitBreaker(Map<String, Config> resourceMap){
        this.resourceMap = resourceMap;
        initDegradeRule();
    }

    public static Config buildConfig(int type, int count, int timeWindow){
        Config config = new Config();
        config.setType(type);
        config.setCount(count);
        config.setTimeWindow(timeWindow);
        return config;
    }

    /**
     * @see #execute(String, Function, Object, Consumer)
     * @param resource
     * @param param
     * @param bizExecutor
     * @param <T>
     * @param <R>
     * @return
     */
    public <T,R> R execute(String resource, Function<T, R> bizExecutor, T param) throws QpsException {
        return execute(resource, bizExecutor, param, null);
    }

    /**
     * 包装需要降级处理的操作
     * @param resource      需要进行降级处理的资源名称
     * @param bizExecutor   业务逻辑执行器，没有被降级的话将由此参数执行业务逻辑
     * @param param         执行业务逻辑需要的参数
     * @param errExecutor   异常执行器，发生异常时将调用此接口处理，此处的异常包含降级异常和其他异常 @see QpsException
     * @param <T>
     * @param <R>
     * @return
     */
    public <T,R> R execute(String resource, Function<T, R> bizExecutor, T param, Consumer<QpsException> errExecutor) throws QpsException {
        if(! resourceMap.containsKey(resource)){
            throw new RuntimeException("resource:"+resource+"未执行初始化加载！");
        }

        Entry entry = null;
        R result = null;
        Throwable ex = null;
        try {
            entry = SphU.entry(resource);
            result = bizExecutor.apply(param);
        } catch(Throwable t){
            ex = t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        if(ex != null){
            QpsException qpsEx;
            if (BlockException.isBlockException(ex)) {
                qpsEx = new QpsException(true, ex);
            }else{
                qpsEx = new QpsException(false, ex);
                Tracer.trace(ex);
            }

            if(errExecutor != null){
                errExecutor.accept(qpsEx);
            }
            throw qpsEx;
        }

        return result;
    }

    private void initDegradeRule(){
        List<DegradeRule> rules = new ArrayList<>();
        for(Map.Entry<String, Config> entry : resourceMap.entrySet()){
            Config config = entry.getValue();

            DegradeRule rule = new DegradeRule();
            rule.setResource(entry.getKey());
            rule.setGrade(config.getType());
            rule.setCount(config.getCount());
            rule.setTimeWindow(config.getTimeWindow());
            rules.add(rule);
        }
        DegradeRuleManager.loadRules(rules);
    }

    public static class Config {
        /**
         * 熔断的判断依据：0=根据平均响应时间RT 1=根据异常比例 2=根据异常数量
         */
        private int type = RuleConstant.DEGRADE_GRADE_RT;
        /**
         * 执行熔断的阈值
         */
        private int count;
        /**
         * 熔断持续时间（秒）
         */
        private int timeWindow;


        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getTimeWindow() {
            return timeWindow;
        }

        public void setTimeWindow(int timeWindow) {
            this.timeWindow = timeWindow;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}

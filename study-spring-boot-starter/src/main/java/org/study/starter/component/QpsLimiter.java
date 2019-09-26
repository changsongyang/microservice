package org.study.starter.component;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.study.common.statics.exceptions.QpsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * qps限流器
 *
 * 说明：
 *      1、一个jvm进程需确保只有一个实例
 *      2、一个jvm进程中请勿和sentinel的@SentinelResource混合使用
 *      3、一个jvm进程中本类勿和sentinel的动态数据源共用
 *      4、有前面几点限制的原因是 FlowRuleManager.currentProperty是一个静态变量，且根据FlowRuleManager的api设计来看，作者是希望
 *         currentProperty属性的值只允许全部替换，往里面追加或部分更新，所以，如果同一个jvm里面有多个地方调用了 FlowRuleManager.loadRules()
 *         或者 FlowRuleManager.register2Property()，其currentProperty属性的值就会被替换掉，所以就会导致有些配置被覆盖掉了
 */
public class QpsLimiter {
    private Map<String, Integer> resourceMap;

    public QpsLimiter(Map<String, Integer> resourceMap){
        this.resourceMap = resourceMap;
        initFlowRule();
    }

    /**
     * @see #execute(String, Function, Object, Consumer)
     * @param resource
     * @param param
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    public <T,R> R execute(String resource, Function<T, R> function, T param) throws QpsException {
        return execute(resource, function, param, null);
    }

    /**
     * 包装限流操作
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
        QpsException qpsEx = null;
        try {
            entry = SphU.entry(resource);
            result = bizExecutor.apply(param);
        } catch (BlockException e) {
            qpsEx = new QpsException(true, e);
        } catch(Throwable e){
            qpsEx = new QpsException(false, e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        if(qpsEx != null){
            if(errExecutor != null){
                errExecutor.accept(qpsEx);
            }
            throw qpsEx;
        }

        return result;
    }

    private void initFlowRule(){
        List<FlowRule> rules = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : resourceMap.entrySet()){
            FlowRule rule = new FlowRule();
            rule.setResource(entry.getKey());
            rule.setCount(entry.getValue());
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rules.add(rule);
        }
        FlowRuleManager.loadRules(rules);
    }
}

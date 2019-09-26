package org.study.starter.component;

import com.alibaba.csp.sentinel.node.StatisticNode;

/**
 * qps计数器
 */
public class QpsCounter {
    private String resource;
    private StatisticNode statisticNode = new StatisticNode();

    public QpsCounter(String resource){
        this.resource = resource;
    }

    /**
     * 增长通过的数量（注：通过的未必一定success）
     */
    public void incPass(){
        statisticNode.addPassRequest(1);
    }

    public void incPass(int count){
        statisticNode.addPassRequest(count);
    }

    /**
     * 增长被限流的数量
     */
    public void incBlock(){
        statisticNode.increaseBlockQps(1);
    }

    public void incBlock(int count){
        statisticNode.increaseBlockQps(count);
    }

    /**
     * 增长异常的数量
     */
    public void incException(){
        statisticNode.increaseExceptionQps(1);
    }

    /**
     * 增长异常的数量
     */
    public void incException(int count){
        statisticNode.increaseExceptionQps(count);
    }

    /**
     * 同时增长rt和成功的数量
     * @param rt
     */
    public void incRtAndSuccess(long rt){
        statisticNode.addRtAndSuccess(rt, 1);
    }

    public void incRtAndSuccess(long rt, int successCount){
        statisticNode.addRtAndSuccess(rt, successCount);
    }

    public double totalQps(){
        return statisticNode.totalQps();
    }

    public double passQps(){
        return statisticNode.passQps();
    }

    public double blockQps() {
        return statisticNode.blockQps();
    }

    public double exceptionQps(){
        return statisticNode.exceptionQps();
    }

    public double successQps(){
        return statisticNode.successQps();
    }

    public double avgRt(){
        return statisticNode.avgRt();
    }

    public double minRt(){
        return statisticNode.minRt();
    }

    public String getResource() {
        return resource;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("resource:").append(getResource())
                .append(",totalQps:").append(totalQps())
                .append(",passQps:").append(passQps())
                .append(",successQps:").append(successQps())
                .append(",blockQps:").append(blockQps())
                .append(",exceptionQps:").append(exceptionQps())
                .append(",avgRt:").append(avgRt())
                .append(",minRt:").append(minRt())
                .append("}");
        return sb.toString();
    }
}

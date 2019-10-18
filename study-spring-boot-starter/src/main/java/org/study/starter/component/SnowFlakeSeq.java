package org.study.starter.component;

import org.study.common.util.utils.DateUtil;
import org.study.common.util.utils.IPUtil;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 使用雪花算法生成序列号
 */
public class SnowFlakeSeq {
    private static final long CUSTOM_EPOCH = 1567687986915L;//2019-09-05T220:53:06
    public static final String SNOW_FLAKE_ID_FORMAT = "%019d";

    private static final int TOTAL_BITS = 64;
    private static final int EPOCH_BITS = 42;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final int MAX_NODE_ID = (int)(Math.pow(2, NODE_ID_BITS) - 1); //1023
    private static final int MAX_SEQUENCE = (int)(Math.pow(2, SEQUENCE_BITS) - 1); //4095

    private int nodeId;
    private int maxClockBackwardMills = 15;//允许时钟回拨的最大毫秒数

    private volatile long lastTimestamp = -1L;
    private byte sequenceOffset;
    private volatile long sequence = 0L;

    /**
     * 创建当前类对象，并使用默认的节点Id，默认是使用mac地址的hash值作为节点id
     */
    public SnowFlakeSeq() {
        this.nodeId = createNodeId();
    }

    /**
     * 创建当前类对象，并使用指定的节点Id
     * @param nodeId
     */
    public SnowFlakeSeq(int nodeId) {
        if(nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, MAX_NODE_ID));
        }
        this.nodeId = nodeId;
        this.maxClockBackwardMills = maxClockBackwardMills;
    }

    /**
     * 获取单个Id
     * @return
     */
    public synchronized long nextId() {
        return nextSingleId();
    }

    /**
     * 批量获取Id
     * @param count
     * @return
     */
    public synchronized List<Long> nextId(int count){
        List<Long> idList = new ArrayList<>(count);
        for(int i=0; i<count; i++){
            Long id = nextSingleId();
            idList.add(id);
        }
        return idList;
    }

    public String nextId(String prefix, boolean isWithDate){
        Long id = nextId();
        if(isWithDate){
            return prefix + DateUtil.formatShortDate(new Date()) + String.format(SNOW_FLAKE_ID_FORMAT, id);
        }else{
            return prefix + String.format(SNOW_FLAKE_ID_FORMAT, id);
        }
    }

    public List<String> nextSnowId(int count, String prefix, boolean isWithDate){
        List<String> idStrList = new ArrayList<>();
        List<Long> idList = nextId(count);
        for(int i=0; i<idList.size(); i++){
            Long id = idList.get(i);
            String idStr;
            if(isWithDate){
                idStr = prefix + DateUtil.formatShortDate(new Date()) + String.format(SNOW_FLAKE_ID_FORMAT, id);
            }else{
                idStr = prefix + String.format(SNOW_FLAKE_ID_FORMAT, id);
            }
            idStrList.add(idStr);
        }
        return idStrList;
    }

    /**
     * 注意：此方法线程不安全，调用此方法的上一级方法都需要加synchronized锁
     *
     * 生成单个Id
     * @return
     */
    private Long nextSingleId(){
        long currentTimestamp = timestamp();

        long timeDiffMillSecond = lastTimestamp - currentTimestamp;
        if(timeDiffMillSecond > 0) { //发生了时钟回拨
            if(timeDiffMillSecond > maxClockBackwardMills){
                throw new IllegalStateException("Clock moved backwards, Refuse to generate id");
            }

            //如果时钟回拨的范围在可接收的范围内，则休眠一段时间之后再继续生成id
            try {
                Thread.sleep(timeDiffMillSecond);//时钟回拨在允许的范围内，则让线程休眠一段时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if(sequence == 0) {
                // 当前毫秒内的序列号已用完, 则等待下一个毫秒的到来
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequenceReset();
        }

        lastTimestamp = currentTimestamp;
        long id = (currentTimestamp << (TOTAL_BITS - EPOCH_BITS)) | (nodeId << (TOTAL_BITS - EPOCH_BITS - NODE_ID_BITS)) | sequence;
        return id;
    }

    /**
     * 获取时间戳，等于 当前时间 - 开始时间
     * @return
     */
    private static long timestamp() {
        return System.currentTimeMillis() - CUSTOM_EPOCH;
    }

    /**
     * 自旋阻塞，直到下一个毫秒到来
     * @param currentTimestamp
     * @return
     */
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    /**
     * 生成节点Id，使用当前机器的mac地址取hash，然后再跟最大节点id做与运算，保证生成的节点id不会超过最大节点id
     * @return
     */
    private int createNodeId() {
        int nodeId;
        try {
            String macAddress = IPUtil.getMacAddress();
            nodeId = macAddress.hashCode();
        } catch (Exception ex) {
            nodeId = (new SecureRandom().nextInt());
        }
        nodeId = nodeId & MAX_NODE_ID;
        return nodeId;
    }

    private void sequenceReset(){
        sequenceOffset = (byte) (~sequenceOffset & 1);//序列号摆动，视情况归0或归1
        sequence = sequenceOffset; //已到下一个时间，重新归位
    }
}

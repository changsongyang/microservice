package org.study.common.statics.pojos;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/15
 */
public class AggResult implements java.io.Serializable{
    private static final long serialVersionUID = -121231456454645665L;

    private String key;

    private AggType aggType;

    private long count;

    private double min;

    private double max;

    private double sum;

    private double avg;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AggType getAggType() {
        return aggType;
    }

    public void setAggType(AggType aggType) {
        this.aggType = aggType;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }
}

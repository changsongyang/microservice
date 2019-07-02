package org.study.starter.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class EsStatistic implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long count;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal sum;
    private BigDecimal avg;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getAvg() {
        return avg;
    }

    public void setAvg(BigDecimal avg) {
        this.avg = avg;
    }
}

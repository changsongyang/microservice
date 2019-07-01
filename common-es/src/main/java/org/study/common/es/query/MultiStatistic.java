package org.study.common.es.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 多字段统计
 */
public class MultiStatistic implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 统计结果，其中key为字段名，value为统计结果
     */
    Map<String, Statistic> statisticMap = new HashMap<>();

    public Map<String, Statistic> getStatisticMap() {
        return statisticMap;
    }

    public void setStatisticMap(Map<String, Statistic> statisticMap) {
        this.statisticMap = statisticMap;
    }
}

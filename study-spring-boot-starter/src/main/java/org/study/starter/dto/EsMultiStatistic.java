package org.study.starter.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 多字段统计
 */
public class EsMultiStatistic implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 统计结果，其中key为字段名，value为统计结果
     */
    Map<String, EsStatistic> statisticMap = new HashMap<>();

    public Map<String, EsStatistic> getStatisticMap() {
        return statisticMap;
    }

    public void setStatisticMap(Map<String, EsStatistic> statisticMap) {
        this.statisticMap = statisticMap;
    }
}

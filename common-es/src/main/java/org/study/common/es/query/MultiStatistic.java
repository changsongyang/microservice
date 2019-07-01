package org.study.common.es.query;

import java.util.HashMap;
import java.util.Map;

public class MultiStatistic {
    Map<String, Statistic> statisticMap = new HashMap<>();

    public Map<String, Statistic> getStatisticMap() {
        return statisticMap;
    }

    public void setStatisticMap(Map<String, Statistic> statisticMap) {
        this.statisticMap = statisticMap;
    }
}

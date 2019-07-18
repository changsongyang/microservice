package org.study.common.util.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ES聚合统计结果
 */
public class EsAggResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 没有分组时的统计结果，其中key为字段名，value为统计结果
     */
    Map<String, Aggregation> aggMap = new HashMap<>();

    /**
     * 有分组时的统计结果，其中第一层的key为字段名，第二层的key为分组的值，第二层的value为统计结果
     */
    Map<String, Map<String, Aggregation>> aggGroupMap = new HashMap<>();

    public Map<String, Aggregation> getAggMap() {
        return aggMap;
    }

    public void setAggMap(Map<String, Aggregation> aggMap) {
        this.aggMap = aggMap;
    }

    public Map<String, Map<String, Aggregation>> getAggGroupMap() {
        return aggGroupMap;
    }

    public void setAggGroupMap(Map<String, Map<String, Aggregation>> aggGroupMap) {
        this.aggGroupMap = aggGroupMap;
    }
}

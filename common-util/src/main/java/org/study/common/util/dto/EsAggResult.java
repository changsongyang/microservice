package org.study.common.util.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES聚合统计结果
 */
public class EsAggResult implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 没有使用group by时的统计结果，其中key为字段名，value为统计结果
     */
    Map<String, Aggregation> aggMap = new HashMap<>();

    /**
     * 有使用group by时的统计结果，其中第一层的key为被统计的字段名，第二层的key为group by的字段名，第二层的value为统计结果
     */
    Map<String, List<Aggregation>> aggListMap = new HashMap<>();

    public Map<String, Aggregation> getAggMap() {
        return aggMap;
    }

    public void setAggMap(Map<String, Aggregation> aggMap) {
        this.aggMap = aggMap;
    }

    public Map<String, List<Aggregation>> getAggListMap() {
        return aggListMap;
    }

    public void setAggListMap(Map<String, List<Aggregation>> aggListMap) {
        this.aggListMap = aggListMap;
    }
}

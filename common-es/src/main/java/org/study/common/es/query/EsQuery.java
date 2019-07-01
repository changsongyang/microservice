package org.study.common.es.query;

import org.study.common.statics.exceptions.BizException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用elasticsearch查询的请求参数
 */
public class EsQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String index;
    private String type;
    private String groupBy;
    private String orderBy;
    private String returnClassName;

    private boolean isScroll;
    private String scrollId;
    private long scrollExpireSec;
    private int pageCurrent = 1;
    private int pageSize = 10;

    private String[] selectFields;
    private Map<String, Object> eqMap;
    private Map<String, Object> gtMap;
    private Map<String, Object> gteMap;
    private Map<String, Object> ltMap;
    private Map<String, Object> lteMap;
    private Map<String, Object> likeMap;
    private Map<String, Object[]> inMap;
    private Map<String, StatisField> statisFieldMap;

    public static EsQuery build(){
        return new EsQuery();
    }

    /**
     * 需要返回的查询字段，默认是所有字段
     * @param fields
     * @return
     */
    public EsQuery select(String... fields){
        this.selectFields = fields;
        return this;
    }

    /**
     * 查询数据源
     * @param index
     * @return
     */
    public EsQuery from(String index){
        this.index = index;
        return this;
    }

    /**
     * 查询数据源
     * @param index
     * @return
     */
    public EsQuery from(String index, String type){
        this.index = index;
        this.type = type;
        return this;
    }

    /**
     * 等于(精确匹配)
     * @param field
     * @param value
     * @return
     */
    public EsQuery eq(String field, Object value){
        initMapIfNeed(MapType.EQ_MAP);
        this.eqMap.put(field, value);
        return this;
    }

    /**
     * 大于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gt(String field, Object value){
        initMapIfNeed(MapType.GT_MAP);
        return this;
    }

    /**
     * 大于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gte(String field, Object value){
        initMapIfNeed(MapType.GTE_MAP);
        return this;
    }

    /**
     * 小于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lt(String field, Object value){
        initMapIfNeed(MapType.LT_MAP);
        return this;
    }

    /**
     * 小于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lte(String field, Object value){
        initMapIfNeed(MapType.LTE_MAP);
        return this;
    }

    /**
     * 等同于sql的between
     * @param field
     * @param start
     * @param end
     * @return
     */
    public EsQuery between(String field, Object start, Object end){
        gte(field, start);
        lte(field, end);
        return this;
    }

    /**
     * 等同于sql的in查询
     * @param field
     * @param values
     * @return
     */
    public EsQuery in(String field, Collection values){
        initMapIfNeed(MapType.IN_MAP);
        this.inMap.put(field, values.toArray());
        return this;
    }

    /**
     * 模糊匹配(全文检索)
     * @param field
     * @param value
     * @return
     */
    public EsQuery like(String field, Object value){
        initMapIfNeed(MapType.LIKE_MAP);
        this.likeMap.put(field, value);
        return this;
    }

    /**
     * 计算总数
     * @param field
     * @return
     */
    public EsQuery count(String field){
        initStatisIfNeed(field);
        this.statisFieldMap.get(field).setCount(true);
        return this;
    }

    /**
     * 计算总和
     * @param field
     * @return
     */
    public EsQuery sum(String field){
        initStatisIfNeed(field);
        this.statisFieldMap.get(field).setSum(true);
        return this;
    }

    /**
     * 计算最大值
     * @param field
     * @return
     */
    public EsQuery max(String field){
        initStatisIfNeed(field);
        this.statisFieldMap.get(field).setMax(true);
        return this;
    }

    /**
     * 计算最小值
     * @param field
     * @return
     */
    public EsQuery min(String field){
        initStatisIfNeed(field);
        this.statisFieldMap.get(field).setMin(true);
        return this;
    }

    /**
     * 计算平均值
     * @param field
     * @return
     */
    public EsQuery avg(String field){
        initStatisIfNeed(field);
        this.statisFieldMap.get(field).setAvg(true);
        return this;
    }

    /**
     * 分组
     * @param fields
     * @return
     */
    public EsQuery groupBy(String fields){
        this.groupBy = fields;
        return this;
    }

    /**
     * 排序
     * @param fields
     * @return
     */
    public EsQuery orderBy(String fields){
        this.orderBy = fields;
        return this;
    }

    /**
     * 分页查询
     * @param pageCurrent
     * @param pageSize
     * @return
     */
    public EsQuery page(Integer pageCurrent, Integer pageSize){
        this.pageCurrent = pageCurrent;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 不分页查询，也需要指定条数
     * @param pageSize
     * @return
     */
    public EsQuery size(Integer pageSize){
        this.pageSize = pageSize;
        return this;
    }

    /**
     * scroll查询
     * @param scrollId
     * @param expireSec
     * @param pageSize
     * @return
     */
    public EsQuery scroll(String scrollId, long expireSec, int pageSize){
        this.isScroll = true;
        this.scrollId = scrollId;
        this.scrollExpireSec = expireSec;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 查询返回的实体类，如果不调用此方法设置实体类，默认会返回String
     * @param clz
     * @return
     */
    public EsQuery result(Class clz){
        this.returnClassName = clz.getName();
        return this;
    }






    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, StatisField> getStatisFieldMap() {
        return statisFieldMap;
    }

    public void setStatisFieldMap(Map<String, StatisField> statisFieldMap) {
        this.statisFieldMap = statisFieldMap;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getReturnClassName() {
        return returnClassName;
    }

    public void setReturnClassName(String returnClassName) {
        this.returnClassName = returnClassName;
    }

    public boolean getIsScroll() {
        return isScroll;
    }

    public void setIsScroll(boolean isScroll) {
        this.isScroll = isScroll;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public long getScrollExpireSec() {
        return scrollExpireSec;
    }

    public void setScrollExpireSec(long scrollExpireSec) {
        this.scrollExpireSec = scrollExpireSec;
    }

    public int getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(int pageCurrent) {
        this.pageCurrent = pageCurrent;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(String[] selectFields) {
        this.selectFields = selectFields;
    }

    public Map<String, Object> getEqMap() {
        return eqMap;
    }

    public void setEqMap(Map<String, Object> eqMap) {
        this.eqMap = eqMap;
    }

    public Map<String, Object> getGtMap() {
        return gtMap;
    }

    public void setGtMap(Map<String, Object> gtMap) {
        this.gtMap = gtMap;
    }

    public Map<String, Object> getGteMap() {
        return gteMap;
    }

    public void setGteMap(Map<String, Object> gteMap) {
        this.gteMap = gteMap;
    }

    public Map<String, Object> getLtMap() {
        return ltMap;
    }

    public void setLtMap(Map<String, Object> ltMap) {
        this.ltMap = ltMap;
    }

    public Map<String, Object> getLteMap() {
        return lteMap;
    }

    public void setLteMap(Map<String, Object> lteMap) {
        this.lteMap = lteMap;
    }

    public Map<String, Object[]> getInMap() {
        return inMap;
    }

    public void setInMap(Map<String, Object[]> inMap) {
        this.inMap = inMap;
    }

    public Map<String, Object> getLikeMap() {
        return likeMap;
    }

    public void setLikeMap(Map<String, Object> likeMap) {
        this.likeMap = likeMap;
    }

    private void initMapIfNeed(int type){
        boolean isEmpty = true;
        switch (type){
            case MapType.EQ_MAP:
                isEmpty = eqMap == null;
                break;
            case MapType.GT_MAP:
                isEmpty = gtMap == null;
                break;
            case MapType.GTE_MAP:
                isEmpty = gteMap == null;
                break;
            case MapType.LT_MAP:
                isEmpty = ltMap == null;
                break;
            case MapType.LTE_MAP:
                isEmpty = lteMap == null;
                break;
            case MapType.IN_MAP:
                isEmpty = inMap == null;
                break;
            case MapType.LIKE_MAP:
                isEmpty = likeMap == null;
                break;
            case MapType.STATISTIC_MAP:
                isEmpty = statisFieldMap == null;
                break;
        }

        if(!isEmpty){
            return;
        }

        synchronized (this){
            switch (type){
                case MapType.EQ_MAP:
                    if(eqMap == null){
                        eqMap = new HashMap<>();
                    }
                    break;
                case MapType.GT_MAP:
                    if(gtMap == null){
                        gtMap = new HashMap<>();
                    }
                    break;
                case MapType.GTE_MAP:
                    if(gteMap == null){
                        gteMap = new HashMap<>();
                    }
                    break;
                case MapType.LT_MAP:
                    if(ltMap == null){
                        ltMap = new HashMap<>();
                    }
                    break;
                case MapType.LTE_MAP:
                    if(lteMap == null){
                        lteMap = new HashMap<>();
                    }
                    break;
                case MapType.IN_MAP:
                    if(inMap == null){
                        inMap = new HashMap<>();
                    }
                    break;
                case MapType.LIKE_MAP:
                    if(likeMap == null){
                        likeMap = new HashMap<>();
                    }
                    break;
                case MapType.STATISTIC_MAP:
                    if(statisFieldMap == null){
                        statisFieldMap = new HashMap<>();
                    }
                    break;
                default:
                    throw new BizException("未支持的类型type="+type);
            }
        }
    }

    private void initStatisIfNeed(String field){
        initMapIfNeed(MapType.STATISTIC_MAP);

        if(! statisFieldMap.containsKey(field)){
            synchronized (this){
                if(! statisFieldMap.containsKey(field)){
                    statisFieldMap.put(field, new StatisField(field));
                }
            }
        }
    }

    public class StatisField {
        private String field;
        private boolean count = false;
        private boolean sum = false;
        private boolean max = false;
        private boolean min = false;
        private boolean avg = false;

        public StatisField(String field){
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean getCount() {
            return count;
        }

        public void setCount(boolean count) {
            this.count = count;
        }

        public boolean getSum() {
            return sum;
        }

        public void setSum(boolean sum) {
            this.sum = sum;
        }

        public boolean getMax() {
            return max;
        }

        public void setMax(boolean max) {
            this.max = max;
        }

        public boolean getMin() {
            return min;
        }

        public void setMin(boolean min) {
            this.min = min;
        }

        public boolean getAvg() {
            return avg;
        }

        public void setAvg(boolean avg) {
            this.avg = avg;
        }
    }

    class MapType {
        final static int EQ_MAP = 1;
        final static int GT_MAP = 2;
        final static int GTE_MAP = 3;
        final static int LT_MAP = 4;
        final static int LTE_MAP = 5;
        final static int LIKE_MAP = 6;
        final static int IN_MAP = 7;
        final static int STATISTIC_MAP = 8;
    }
}

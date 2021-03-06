package org.study.common.util.dto;

import org.study.common.util.utils.SnakeCaseUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用elasticsearch查询的请求参数，当前类当做客户端的查询参数，而{@link org.study.starter.component.ESClient}则作为服务端，
 * 进行参数解析、执行查询、返回结果等处理
 *
 * 使用注意事项：
 *      1、此类尽可能贴近sql语法和含义，但并没有完全支持所有sql功能，已支持的功能可直接查看当前类提供的方法
 *      2、进行模糊查询时，在ES中是进行全文检索，这跟数据库中like查询会有一些不同
 *      3、聚合统计只支持count、sum、min、max、avg，也支持在聚合统计时进行分组 group by，聚合查询不支持order by
 *      4、非聚合查询(即常规查询)支持order by，但不支持 group by
 *      5、如果ES中存储的字段名和查询参数名格式不一致(如ES中是下划线，查询参数是驼峰)，可使用{@link #EsQuery(boolean snakeCase)}这个
 *         构造方法设置为true，那么在查询和返回时会自动进行转换，但会损耗一部分性能
 *      6、如果查询参数中，在ES中并不存在此字段，则在ESClient中拼接查询参数时会自动过滤掉
 *      7、如果是普通的分页查询，建议不要超过10000条，不然会报错，这是ES为避免深分页问题而特意设置的，如果需要遍历数据，需要使用scroll滚动查询
 *      8、使用scroll滚动查询时，首次查询时scrollId设置空值即可，查询返回之后，再把scrollId设置到查询参数即可
 *
 */
public class EsQuery implements Serializable {
    private static final long serialVersionUID = 1L;
    private QueryParam queryParam = new QueryParam();

    public EsQuery(){}

    /**
     * @param snakeCase 是否需要自动参数转换，如果为true，则输入参数为驼峰时，查询ES时会转成下划线，返回结果时又会转成驼峰，反之亦然
     */
    public EsQuery(boolean snakeCase){
        this.queryParam.setSnakeCase(snakeCase);
    }

    public static EsQuery build(){
        return new EsQuery();
    }

    public static EsQuery build(boolean snakeCase){
        return new EsQuery(snakeCase);
    }

    /**
     * 需要返回的查询字段，默认是所有字段
     * @param fields
     * @return
     */
    public EsQuery select(String... fields){
        if(fields != null && fields.length > 0){
            String[] newFields = new String[]{};
            for(int i=0; i<fields.length; i++){
                filedSnakeCase(fields[i]);
            }
            this.queryParam.setSelectFields(newFields);
        }
        return this;
    }

    /**
     * 查询数据源，即ES中的index名
     * @param index
     * @return
     */
    public EsQuery from(String index){
        this.queryParam.setIndex(index);
        return this;
    }

    /**
     * 等于(精确匹配)
     * @param field
     * @param value
     * @return
     */
    public EsQuery eq(String field, Object value){
        this.queryParam.eqMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 等于(精确匹配)
     * @param eqMap
     * @return
     */
    public EsQuery eq(Map<String, Object> eqMap){
        if(eqMap == null || eqMap.isEmpty()){
            return this;
        }

        for(Map.Entry<String, Object> entry : eqMap.entrySet()){
            this.eq(filedSnakeCase(entry.getKey()), entry.getValue());
        }
        return this;
    }

    /**
     * 不等于(精确匹配)
     * @param field
     * @param value
     * @return
     */
    public EsQuery neq(String field, Object value){
        this.queryParam.neqMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 大于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gt(String field, Object value){
        this.queryParam.gtMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 大于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery gte(String field, Object value){
        this.queryParam.gteMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 小于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lt(String field, Object value){
        this.queryParam.ltMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 小于等于
     * @param field
     * @param value
     * @return
     */
    public EsQuery lte(String field, Object value){
        this.queryParam.lteMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 区间查询，等同于sql的between
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
     * 等同于sql的 in(...) 查询
     * @param field
     * @param values
     * @return
     */
    public EsQuery in(String field, Object[] values){
        this.queryParam.inMap.put(filedSnakeCase(field), values);
        return this;
    }

    /**
     * 等同于sql的 not in(...) 查询
     * @param field
     * @param values
     * @return
     */
    public EsQuery notIn(String field, Object[] values){
        this.queryParam.notInMap.put(filedSnakeCase(field), values);
        return this;
    }

    /**
     * 全文检索，类似于sql的like查询
     * @param field
     * @param value
     * @return
     */
    public EsQuery fullText(String field, Object value){
        this.queryParam.likeMap.put(filedSnakeCase(field), value);
        return this;
    }

    /**
     * 计算总数
     * @param field
     * @return
     */
    public EsQuery count(String field){
        this.queryParam.setAgg(filedSnakeCase(field), "count");
        return this;
    }

    /**
     * 计算总和
     * @param field
     * @return
     */
    public EsQuery sum(String field){
        this.queryParam.setAgg(filedSnakeCase(field), "sum");
        return this;
    }

    /**
     * 计算最大值
     * @param field
     * @return
     */
    public EsQuery max(String field){
        this.queryParam.setAgg(filedSnakeCase(field), "max");
        return this;
    }

    /**
     * 计算最小值
     * @param field
     * @return
     */
    public EsQuery min(String field){
        this.queryParam.setAgg(filedSnakeCase(field), "min");
        return this;
    }

    /**
     * 计算平均值
     * @param field
     * @return
     */
    public EsQuery avg(String field){
        this.queryParam.setAgg(filedSnakeCase(field), "avg");
        return this;
    }

    /**
     * 分组，仅在统计时有用
     * @param field
     * @return
     */
    public EsQuery groupBy(String field){
        this.queryParam.setGroupBy(filedSnakeCase(field));
        return this;
    }

    /**
     * 排序，仅在常规查询有用，聚合查询不起作用
     * @param sortColumns 排序的字段，如果有多个字段，使用英文的逗号分割
     * @return
     */
    public EsQuery orderBy(String sortColumns){
        StringBuffer sbf = new StringBuffer();
        String[] sortColumnArray = sortColumns.split(",");
        for(int i=0; i<sortColumnArray.length; i++){
            String[] sortColumn = sortColumnArray[i].split(" ");
            if(sortColumn.length > 1){
                sbf.append(filedSnakeCase(sortColumn[0])).append(" ").append(sortColumn[1]);
            }else{
                sbf.append(filedSnakeCase(sortColumn[0]));
            }
        }
        this.queryParam.setOrderBy(sbf.toString());
        return this;
    }

    /**
     * 分页查询
     * @param pageCurrent
     * @param pageSize
     * @return
     */
    public EsQuery page(Integer pageCurrent, Integer pageSize){
        this.queryParam.setPageCurrent(pageCurrent);
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * 不分页查询，也需要指定条数
     * @param pageSize
     * @return
     */
    public EsQuery size(Integer pageSize){
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * 设置使用scroll滚动查询，首次查询时调用
     * @param expireSec
     * @param pageSize
     * @return
     */
    public EsQuery scroll(long expireSec, int pageSize){
        this.queryParam.setScrollMode(true);
        this.queryParam.setScrollId(null);
        this.queryParam.setScrollExpireSec(expireSec);
        this.queryParam.setPageSize(pageSize);
        return this;
    }

    /**
     * scroll滚动查询过一次之后，服务端已经返回了scrollId，需要设置进来
     * @param scrollId
     * @return
     */
    public EsQuery scrollId(String scrollId){
        this.queryParam.setScrollMode(true);
        this.queryParam.setScrollId(scrollId);
        return this;
    }

    /**
     * 查询结果返回的类，如果这个类在服务端也存在，则查询结果会直接返回这个类的实体对象，如果这个类在服务端不存在，则会抛出异常，如果不设置，默认返回HashMap
     *
     * @param clz
     * @return
     */
    public EsQuery resultClass(Class clz){
        this.queryParam.setReturnClassName(clz.getName());
        return this;
    }

    public QueryParam getQueryParam() {
        return this.queryParam;
    }

    public void setQueryParam(QueryParam queryParam) {
        this.queryParam = queryParam;
    }

    private String filedSnakeCase(String field){
        return this.queryParam.getSnakeCase() ? SnakeCaseUtil.toSnakeCase(field, false) : field;
    }


    /**--------------------------------------------- 获取查询参数的方法 START ------------------------------------------**/
    /**
     * 理论上，为了保障查询参数的正确性，下面的这些方法返回时，都应该重新new一个对象返回的，但考虑到性能问题，所以没有这样做，仅仅是在开发人员中
     * 进行约定，大家都约定不对返回值做任何修改、新增操作
     */
    public String getIndex() {
        return this.queryParam.getIndex();
    }
    public String getGroupBy() {
        return this.queryParam.getGroupBy();
    }
    public String getOrderBy() {
        return this.queryParam.getOrderBy();
    }
    public String getReturnClassName() {
        return this.queryParam.getReturnClassName();
    }
    public boolean getSnakeCase() {
        return this.queryParam.getSnakeCase();
    }
    public boolean getScrollMode() {
        return this.queryParam.getScrollMode();
    }
    public String getScrollId() {
        return this.queryParam.getScrollId();
    }
    public long getScrollExpireSec() {
        return this.queryParam.getScrollExpireSec();
    }
    public int getPageCurrent() {
        return this.queryParam.getPageCurrent();
    }
    public int getPageSize() {
        return this.queryParam.getPageSize();
    }
    public String[] getSelectFields() {
        return this.queryParam.getSelectFields();
    }
    public Map<String, Object> getEqMap() {
        return this.queryParam.getEqMap();
    }
    public Map<String, Object> getNeqMap() {
        return this.queryParam.getNeqMap();
    }
    public Map<String, Object> getGtMap() {
        return this.queryParam.getGtMap();
    }
    public Map<String, Object> getGteMap() {
        return this.queryParam.getGteMap();
    }
    public Map<String, Object> getLtMap() {
        return this.queryParam.getLtMap();
    }
    public Map<String, Object> getLteMap() {
        return this.queryParam.getLteMap();
    }
    public Map<String, Object> getLikeMap() {
        return this.queryParam.getLikeMap();
    }
    public Map<String, Object[]> getInMap() {
        return this.queryParam.getInMap();
    }
    public Map<String, Object[]> getNotInMap() {
        return this.queryParam.getNotInMap();
    }
    public Map<String, Aggregation> getAggMap() {
        return this.queryParam.getAggMap();
    }
    /**--------------------------------------------- 获取查询参数的方法 END --------------------------------------------**/


    /**
     * 查询参数，设立内部类的目的是为了查询参数不会被随意更改
     */
    private class QueryParam implements Serializable {
        private static final long serialVersionUID = 1L;

        private static final int INI_PARAM_MAP_CAP = 8;//参数的初始容量

        private String index; //一张表就是一个index
        private String groupBy; //仅在统计时有用,且仅支持单个字段分组
        private String orderBy; //排序字段
        private String returnClassName; //返回结果的类名

        private boolean snakeCase; //是否需要执行驼峰、下划线互转，比如，查询参数为驼峰，ES字段为下划线，查询参数会自动转为下划线，查询结果返回又会把下划线转成驼峰
        private boolean scrollMode; //是否是滚动查询
        private String scrollId; //滚动查询时的scrollId
        private long scrollExpireSec = 30;
        private int pageCurrent = 1;
        private int pageSize = 20;

        private String[] selectFields;
        private Map<String, Object> eqMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> neqMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> gtMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> gteMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> ltMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> lteMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object> likeMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object[]> inMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Object[]> notInMap = new HashMap(INI_PARAM_MAP_CAP);
        private Map<String, Aggregation> aggMap = new HashMap(INI_PARAM_MAP_CAP);

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
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

        public boolean getSnakeCase() {
            return snakeCase;
        }

        public void setSnakeCase(boolean snakeCase) {
            this.snakeCase = snakeCase;
        }

        public boolean getScrollMode() {
            return scrollMode;
        }

        public void setScrollMode(boolean scrollMode) {
            this.scrollMode = scrollMode;
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

        public Map<String, Object> getNeqMap() {
            return neqMap;
        }

        public void setNeqMap(Map<String, Object> neqMap) {
            this.neqMap = neqMap;
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

        public Map<String, Object> getLikeMap() {
            return likeMap;
        }

        public void setLikeMap(Map<String, Object> likeMap) {
            this.likeMap = likeMap;
        }

        public Map<String, Object[]> getInMap() {
            return inMap;
        }

        public void setInMap(Map<String, Object[]> inMap) {
            this.inMap = inMap;
        }

        public Map<String, Object[]> getNotInMap() {
            return notInMap;
        }

        public void setNotInMap(Map<String, Object[]> notInMap) {
            this.notInMap = notInMap;
        }

        public Map<String, Aggregation> getAggMap() {
            return aggMap;
        }

        public void setAggMap(Map<String, Aggregation> aggMap) {
            this.aggMap = aggMap;
        }

        private void setAgg(String field, String type){
            initAggMapIfNeed(field);
            switch(type){
                case "count":
                    this.aggMap.get(field).setCount(true);
                    break;
                case "sum":
                    this.aggMap.get(field).setSum(true);
                    break;
                case "min":
                    this.aggMap.get(field).setMin(true);
                    break;
                case "max":
                    this.aggMap.get(field).setMax(true);
                    break;
                case "avg":
                    this.aggMap.get(field).setAvg(true);
                    break;
                default:
                    break;
            }
        }

        private void initAggMapIfNeed(String field){
            if(! this.aggMap.containsKey(field)){
                synchronized (this){
                    if(! this.aggMap.containsKey(field)){
                        this.aggMap.put(field, new Aggregation(field));
                    }
                }
            }
        }
    }

    public class Aggregation {
        private String field;
        private boolean count = false;
        private boolean sum = false;
        private boolean max = false;
        private boolean min = false;
        private boolean avg = false;

        public Aggregation(String field){
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
}

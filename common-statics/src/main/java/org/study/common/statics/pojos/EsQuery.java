package org.study.common.statics.pojos;

import java.util.*;

public class EsQuery {

    private String index;
    private String type;
    private String sum;
    private String count;
    private String groupBy;
    private String orderBy;
    private String returnClassName;

    private boolean isScroll;
    private String scrollId;
    private long scrollExpireSec;
    private Integer pageCurrent;
    private Integer pageSize;

    private String[] selectFields;
    private Map<String, Object> eqMap;
    private Map<String, Object> gtMap;
    private Map<String, Object> gteMap;
    private Map<String, Object> ltMap;
    private Map<String, Object> lteMap;
    private Map<String, Object> likeMap;
    private Map<String, Object[]> inMap;

    public static EsQuery build(){
        return new EsQuery();
    }

    public EsQuery select(String... fields){
        this.selectFields = fields;
        return this;
    }

    public EsQuery from(String index, String type){
        this.index = index;
        this.type = type;
        return this;
    }

    public EsQuery eq(String field, Object value){
        initObjectIfNeed(this.eqMap);
        this.eqMap.put(field, value);
        return this;
    }

    public EsQuery gt(String field, Object value){
        initObjectIfNeed(this.gtMap);
        return this;
    }

    public EsQuery gte(String field, Object value){
        initObjectIfNeed(this.gteMap);
        return this;
    }

    public EsQuery lt(String field, Object value){
        initObjectIfNeed(this.ltMap);
        return this;
    }

    public EsQuery lte(String field, Object value){
        initObjectIfNeed(this.lteMap);
        return this;
    }

    public EsQuery between(String field, Object start, Object end){
        gte(field, start);
        lte(field, end);
        return this;
    }

    public EsQuery in(String field, Collection values){
        initArrayIfNeed(this.inMap);
        this.inMap.put(field, values.toArray());
        return this;
    }

    public EsQuery like(String field, Object value){
        initObjectIfNeed(this.likeMap);
        this.likeMap.put(field, value);
        return this;
    }

    public EsQuery sum(String field){
        this.sum = field;
        return this;
    }

    public EsQuery count(String field){
        this.count = field;
        return this;
    }

    public EsQuery groupBy(String fields){
        this.groupBy = fields;
        return this;
    }

    public EsQuery orderBy(String fields){
        this.orderBy = fields;
        return this;
    }

    public EsQuery page(Integer pageCurrent, Integer pageSize){
        this.pageCurrent = pageCurrent;
        this.pageSize = pageSize;
        return this;
    }

    public EsQuery scroll(String scrollId, long expireSec, int pageSize){
        this.scrollId = scrollId;
        this.scrollExpireSec = expireSec;
        this.pageSize = pageSize;
        return this;
    }

    public EsQuery entity(Class clz){
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

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
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

    public Integer getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(Integer pageCurrent) {
        this.pageCurrent = pageCurrent;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
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

    private void initObjectIfNeed(Map<String, Object> map){
        if(map == null){
            synchronized (this){
                if(map == null){
                    map = new HashMap<String, Object>();
                }
            }
        }
    }

    private void initArrayIfNeed(Map<String, Object[]> map){
        if(map == null){
            synchronized (this){
                if(map == null){
                    map = new HashMap<String, Object[]>();
                }
            }
        }
    }
}

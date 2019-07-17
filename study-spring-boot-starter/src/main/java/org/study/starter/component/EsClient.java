package org.study.starter.component;

import com.google.common.cache.Cache;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.utils.ClassUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;
import org.study.starter.dto.EsQuery;
import org.study.starter.dto.EsAggResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * elasticsearch客户端，提供一些常规的方法，如果需要复杂的查询，可通过 #getRestEsClient() 方法取得ES的原生客户端来处理
 */
public class EsClient {
    private RestHighLevelClient restEsClient;
    private Cache<String, Map<String, String>> cache;

    public EsClient(RestHighLevelClient restEsClient){
        this.restEsClient = restEsClient;
    }

    public EsClient(RestHighLevelClient restEsClient, Cache<String, Map<String, String>> cache){
        this.restEsClient = restEsClient;
        this.cache = cache;
    }

    /**
     * 获取es原生客户端，用以处理比较复杂的需求
     * @return
     */
    public RestHighLevelClient getRestEsClient() {
        return restEsClient;
    }

    /**
     * 取得单个实体
     * @param esQuery
     * @param <T>
     * @return
     */
    public <T> T getOne(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);

        SearchResponse response = executeQuery(esQuery);
        if(response.getHits().getTotalHits().value > 0){
            if(isString(clz)){
                return (T) response.getHits().getHits()[0].getSourceAsString();
            }else{
                return JsonUtil.toBean(response.getHits().getHits()[0].getSourceAsString(), clz);
            }
        }else{
            return null;
        }
    }

    /**
     * 列表查询
     * @param esQuery
     * @param <T>
     * @return
     */
    public <T> List<T> listBy(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);

        SearchResponse response = executeQuery(esQuery);
        if(response.getHits().getTotalHits().value <= 0){
            return new ArrayList<>();
        }
        return getEntityList(response, clz);
    }

    /**
     * 分页查询，需要返回分页结果
     * @param esQuery
     * @param <T>
     * @return
     */
    public <T> PageResult<List<T>> listPage(EsQuery esQuery){
        Class<T> clz = getReturnClass(esQuery);
        SearchResponse response = executeQuery(esQuery);
        long totalRecord = response.getHits().getTotalHits().value;
        if(totalRecord <= 0){
            return PageResult.newInstance(new ArrayList<>(), esQuery.getPageCurrent(), esQuery.getPageSize());
        }
        List<T> entityList = getEntityList(response, clz);
        PageResult result = PageResult.newInstance(entityList, esQuery.getPageCurrent(), esQuery.getPageSize(), totalRecord);
        result.setScrollId(response.getScrollId());
        return result;
    }

    /**
     * 统计，可统计多个字段的多个维度，每个字段都可有：count、sum、min、max、avg 等维度统计
     * @param esQuery
     * @return
     */
    public EsAggResult aggregation(EsQuery esQuery){
        SearchResponse response = executeAggregation(esQuery);

        EsAggResult aggrResult = new EsAggResult();
        if(response.getHits().getTotalHits().value > 0){
            if (isEmpty(esQuery.getGroupBy())){
                fillEsAggResult(aggrResult, null, response.getAggregations().iterator());
            }else{
                Iterator<Aggregation> iterator = response.getAggregations().iterator();
                while (iterator.hasNext()){
                    Aggregation aggr = iterator.next();
                    ParsedTerms terms = (ParsedTerms) aggr;

                    if(terms.getBuckets().isEmpty()){
                        continue;
                    }

                    for(Terms.Bucket bucket : terms.getBuckets()){
                        String groupValue = ((Terms.Bucket) bucket).getKeyAsString();
                        Aggregations bucketAggr = ((Terms.Bucket) bucket).getAggregations();
                        fillEsAggResult(aggrResult, groupValue, bucketAggr.iterator());
                    }
                }
            }
        }
        return aggrResult;
    }

    /**
     * 从查询结果中转换成List<T>返回
     * @param response
     * @return
     */
    public  <T> List<T> getEntityList(SearchResponse response, Class<T> clz){
        List<T> entityList = new ArrayList<>();
        boolean isString = isString(clz);
        if(response.getHits().getTotalHits().value > 0){
            SearchHit[] hits = response.getHits().getHits();
            for(int i=0; i<hits.length; i++){
                if(isString){
                    entityList.add((T)hits[i].getSourceAsString());
                }else{
                    entityList.add(JsonUtil.toBean(hits[i].getSourceAsString(), clz));
                }
            }
        }
        return entityList;
    }

    private SearchResponse executeQuery(EsQuery esQuery){
        paramCheck(esQuery, false);

        if(esQuery.getIsScroll() && StringUtil.isNotEmpty(esQuery.getScrollId())){
            SearchScrollRequest scrollRequest = new SearchScrollRequest(esQuery.getScrollId());
            scrollRequest.scroll(TimeValue.timeValueSeconds(esQuery.getScrollExpireSec()));
            try{
                return restEsClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            }catch(IOException e){
                throw new BizException("IOException " + e.getMessage());
            }
        }

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //设置需要返回的字段
        if(esQuery.getSelectFields() != null && esQuery.getSelectFields().length > 0){
            sourceBuilder.fetchSource(esQuery.getSelectFields(), null);
        }
        //构造查询条件
        sourceBuilder.query(getQueryBuilder(esQuery));
        //增加排序字段
        addSort(sourceBuilder, esQuery.getOrderBy());
        //构建查询请求对象，并指定要查询的 index、type
        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex());
        searchRequest.source(sourceBuilder);
        //处理分页查询
        if(esQuery.getIsScroll()){
            searchRequest.scroll(TimeValue.timeValueSeconds(esQuery.getScrollExpireSec()));
            sourceBuilder.size(esQuery.getPageSize());
        }else{
            int offset = (esQuery.getPageCurrent() - 1) * esQuery.getPageSize();
            sourceBuilder.from(offset).size(esQuery.getPageSize());
        }

        try{
            return restEsClient.search(searchRequest, RequestOptions.DEFAULT);
        }catch(IOException e){
            throw new BizException("IOException " + e.getMessage());
        }
    }

    private SearchResponse executeAggregation(EsQuery esQuery){
        paramCheck(esQuery, true);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(getQueryBuilder(esQuery));
        appendAggregation(sourceBuilder, esQuery);

        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex());
        searchRequest.source(sourceBuilder);

        try{
            return restEsClient.search(searchRequest, RequestOptions.DEFAULT);
        }catch(IOException e){
            throw new BizException("IOException " + e.getMessage());
        }
    }

    private QueryBuilder getQueryBuilder(EsQuery esQuery){
        Map<String, String> fieldMap = getFieldMap(esQuery.getIndex());
        if(fieldMap == null || fieldMap.isEmpty()){
            throw new BizException("es mapping not exist of index: " + esQuery.getIndex());
        }else if(! isEmpty(esQuery.getGroupBy()) && ! fieldMap.containsKey(esQuery.getGroupBy())){
            throw new BizException("cannot use an not exist field to group by : " + esQuery.getGroupBy());
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();//等同于sql中的 and 查询

        //精确匹配(等于)
        if(isNotBlank(esQuery.getEqMap())){
            for(Map.Entry<String, Object> entry : esQuery.getEqMap().entrySet()){
                if(isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())){
                    queryBuilder.filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //精确匹配(不等于)
        if(isNotBlank(esQuery.getNeqMap())) {
            for (Map.Entry<String, Object> entry : esQuery.getNeqMap().entrySet()) {
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.mustNot(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //范围列表查询(in 查询)
        if(isNotBlank(esQuery.getInMap())){
            for(Map.Entry<String, Object[]> entry : esQuery.getInMap().entrySet()){
                if(isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())){
                    queryBuilder.filter(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //精确匹配(not in 查询)
        if(isNotBlank(esQuery.getNotInMap())) {
            for (Map.Entry<String, Object[]> entry : esQuery.getNotInMap().entrySet()) {
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.mustNot(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        //范围区间查询
        if(isNotBlank(esQuery.getGtMap()) || isNotBlank(esQuery.getGteMap())
                || isNotBlank(esQuery.getLtMap()) || isNotBlank(esQuery.getLteMap())){
            Set<String> keys = new HashSet<>();

            if(isNotBlank(esQuery.getGtMap())){
                keys.addAll(esQuery.getGtMap().keySet());
            }
            if(isNotBlank(esQuery.getGteMap())){
                keys.addAll(esQuery.getGteMap().keySet());
            }
            if(isNotBlank(esQuery.getLtMap())){
                keys.addAll(esQuery.getLtMap().keySet());
            }
            if(isNotBlank(esQuery.getLteMap())){
                keys.addAll(esQuery.getLteMap().keySet());
            }

            for(String key : keys){
                if(isEmpty(key) || ! fieldMap.containsKey(key)){
                    continue;
                }

                RangeQueryBuilder query = QueryBuilders.rangeQuery(key);
                Object valueGt = esQuery.getGtMap() == null ? null : esQuery.getGtMap().get(key);
                Object valueGte = esQuery.getGteMap() == null ? null : esQuery.getGteMap().get(key);
                Object valueLt = esQuery.getLtMap() == null ? null : esQuery.getLtMap().get(key);
                Object valueLte = esQuery.getLteMap() == null ? null : esQuery.getLteMap().get(key);

                if(valueGte != null && valueLte != null){
                    query.from(valueGte).to(valueLte);
                }else{
                    if(valueGt != null){
                        query.gt(valueGt);
                    }
                    if(valueGte != null){
                        query.gte(valueGte);
                    }
                    if(valueLt != null){
                        query.lt(valueLt);
                    }
                    if(valueLte != null){
                        query.lte(valueLte);
                    }
                }

                queryBuilder.filter(query);
            }
        }

        //全文搜索(ES服务端需安装有中文分词器)
        if(isNotBlank(esQuery.getLikeMap())){
            for(Map.Entry<String, Object> entry : esQuery.getLikeMap().entrySet()){
                if (isNotEmpty(entry.getKey(), entry.getValue()) && fieldMap.containsKey(entry.getKey())) {
                    queryBuilder.filter(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
                }
            }
        }

        return queryBuilder;
    }

    private void appendAggregation(SearchSourceBuilder sourceBuilder, EsQuery esQuery){
        TermsAggregationBuilder termsAggBuilder = null;
        if (StringUtil.isNotEmpty(esQuery.getGroupBy())) {
            termsAggBuilder = AggregationBuilders.terms(esQuery.getGroupBy()).field(esQuery.getGroupBy()).size(2000);
        }

        for(Map.Entry<String, EsQuery.StatisField> entry : esQuery.getStatisFieldMap().entrySet()){
            String fieldName = entry.getKey();
            if(StringUtil.isEmpty(fieldName)){
                throw new BizException("参数非法，统计时存在为空的参数名");
            }

            EsQuery.StatisField field = entry.getValue();
            boolean isNoneMatch = true;

            if(field.getCount()){
                isNoneMatch = false;
                ValuesSourceAggregationBuilder aggBuilder = AggregationBuilders.count(fillFieldName(fieldName, "count")).field(fieldName);
                if(termsAggBuilder == null){
                    sourceBuilder.aggregation(aggBuilder);
                }else{
                    termsAggBuilder.subAggregation(aggBuilder);
                }
            }

            if(field.getSum()){
                isNoneMatch = false;
                ValuesSourceAggregationBuilder aggBuilder = AggregationBuilders.sum(fillFieldName(fieldName, "sum")).field(fieldName);
                if(termsAggBuilder == null){
                    sourceBuilder.aggregation(aggBuilder);
                }else{
                    termsAggBuilder.subAggregation(aggBuilder);
                }
            }

            if(field.getMax()){
                isNoneMatch = false;
                ValuesSourceAggregationBuilder aggBuilder = AggregationBuilders.max(fillFieldName(fieldName,"max")).field(fieldName);
                if(termsAggBuilder == null){
                    sourceBuilder.aggregation(aggBuilder);
                }else{
                    termsAggBuilder.subAggregation(aggBuilder);
                }
            }

            if(field.getMin()){
                isNoneMatch = false;
                ValuesSourceAggregationBuilder aggBuilder = AggregationBuilders.min(fillFieldName(fieldName, "min")).field(fieldName);
                if(termsAggBuilder == null){
                    sourceBuilder.aggregation(aggBuilder);
                }else{
                    termsAggBuilder.subAggregation(aggBuilder);
                }
            }

            if(field.getAvg()){
                isNoneMatch = false;
                ValuesSourceAggregationBuilder aggBuilder = AggregationBuilders.avg(fillFieldName(fieldName,"avg" )).field(fieldName);
                if(termsAggBuilder == null){
                    sourceBuilder.aggregation(aggBuilder);
                }else{
                    termsAggBuilder.subAggregation(aggBuilder);
                }
            }

            if(isNoneMatch){
                throw new BizException(fieldName+" 需指定至少一个统计项");
            }
        }
        if(termsAggBuilder != null){
            sourceBuilder.aggregation(termsAggBuilder);
        }
    }

    private void fillEsAggResult(EsAggResult aggrResult, String groupValue, Iterator<Aggregation> iterator){
        while(iterator.hasNext()) {
            Aggregation agg = iterator.next();
            String fieldName = splitFieldName(agg.getName());

            org.study.starter.dto.Aggregation aggr;
            if(isEmpty(groupValue)){
                aggr = aggrResult.getAggMap().get(fieldName);
                if (aggr == null) {
                    aggr = new org.study.starter.dto.Aggregation();
                    aggrResult.getAggMap().put(fieldName, aggr);
                }
            }else{
                Map<String, org.study.starter.dto.Aggregation> aggMap = aggrResult.getAggGroupMap().get(fieldName);
                if(aggMap == null){
                    aggr = new org.study.starter.dto.Aggregation();
                    aggMap = new HashMap<>();
                    aggMap.put(groupValue, aggr);
                    aggrResult.getAggGroupMap().put(fieldName, aggMap);
                }else if((aggr = aggMap.get(groupValue)) == null){
                    aggr = new org.study.starter.dto.Aggregation();
                    aggMap.put(groupValue, aggr);
                }
            }

            String type = agg.getType();
            if(ValueCountAggregationBuilder.NAME.equals(type)){
                aggr.setCount( ((ParsedValueCount)agg).getValue() );
            }else if(MaxAggregationBuilder.NAME.equals(type)){
                aggr.setMax(BigDecimal.valueOf( ((ParsedMax)agg).getValue() ));
            }else if(MinAggregationBuilder.NAME.equals(type)){
                aggr.setMin(BigDecimal.valueOf( ((ParsedMin)agg).getValue() ));
            }else if(SumAggregationBuilder.NAME.equals(type)){
                aggr.setSum(BigDecimal.valueOf( ((ParsedSum)agg).getValue() ));
            }else if(AvgAggregationBuilder.NAME.equals(type)){
                aggr.setAvg(BigDecimal.valueOf( ((ParsedAvg)agg).getValue() ));
            }
        }
    }

    /**
     * 取得查询结构返回的实体类
     * @param esQuery
     * @return
     */
    private Class getReturnClass(EsQuery esQuery){
        if(StringUtil.isEmpty(esQuery.getReturnClassName())){
            return String.class;
        }else{
            try{
                return ClassUtil.getClass(esQuery.getReturnClassName());
            }catch (ClassNotFoundException e){
                throw new BizException("ClassNotFoundException " + e.getMessage());
            }
        }
    }

    /**
     * 添加排序字段
     * @param searchBuilder
     * @param sortColumns
     */
    protected void addSort(SearchSourceBuilder searchBuilder, String sortColumns){
        if(StringUtil.isEmpty(sortColumns)){
            return;
        }

        String[] sortColumnArray = sortColumns.split(",");
        for(int i=0; i<sortColumnArray.length; i++){
            String[] sortColumn = sortColumnArray[i].split(" ");
            if(sortColumn.length > 1){
                searchBuilder.sort(sortColumn[0], SortOrder.fromString(sortColumn[1]));
            }else{
                searchBuilder.sort(sortColumn[0], SortOrder.DESC);
            }
        }
    }

    private void paramCheck(EsQuery esQuery, boolean statisFieldMust){
        if(esQuery == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "esQuery不能为空");
        }else if(StringUtil.isEmpty(esQuery.getIndex())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "index不能为空");
        }else if(esQuery.getPageSize() <= 0 || esQuery.getPageCurrent() <= 0){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "pageCurrent和pageSize都需大于0");
        }else if(statisFieldMust && (esQuery.getStatisFieldMap() == null || esQuery.getStatisFieldMap().isEmpty())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "statisFieldMap不能为空");
        }
    }

    private boolean isNotEmpty(String key, Object value){
        return ! ((key == null || key.trim().length() <= 0) || (value == null || value.toString().trim().length() <= 0));
    }
    private boolean isNotEmpty(String key, Object[] value){
        return ! ((key == null || key.trim().length() <= 0) || (value == null || value.length <= 0));
    }
    private boolean isEmpty(String key){
        return (key == null || key.trim().length() <= 0);
    }

    private boolean isNotBlank(Map map){
        return map != null && ! map.isEmpty();
    }

    private boolean isString(Class clz){
        return String.class.getName().equals(clz.getName());
    }

    private String fillFieldName(String field, String suffix){
        return field + "|" + suffix;
    }

    private String splitFieldName(String field){
        return field.split("\\|")[0];
    }

    /**
     * 返回Mapping，其中key为字段名，value为字段的数据类型
     * @param index
     * @return
     */
    private Map<String, String> getFieldMap(String index){
        if(cache != null && cache.getIfPresent(index) != null){
            return cache.getIfPresent(index);
        }

        Map<String, String> fieldMap = new HashMap<>();
        Map<String, MappingMetaData> mappings;
        try{
            GetMappingsResponse mapping = getRestEsClient().indices().getMapping(new GetMappingsRequest().indices(index), RequestOptions.DEFAULT);
            mappings = mapping.mappings();
            if (mappings == null){
                return new HashMap<>();
            }
        }catch(IOException e){
            throw new BizException(e);
        }

        for(Map.Entry<String, MappingMetaData> entry : mappings.entrySet()){
            Map<String, Object> res = (Map<String, Object>) entry.getValue().sourceAsMap().get("properties");
            if(res == null) {
                continue;
            }

            for(Map.Entry<String, Object> entry1 : res.entrySet()){
                LinkedHashMap<String, Object> map = (LinkedHashMap) entry1.getValue();
                fieldMap.put(entry1.getKey(), (String) map.get("type"));
            }
        }
        cache.put(index, fieldMap);
        return fieldMap;
    }
}


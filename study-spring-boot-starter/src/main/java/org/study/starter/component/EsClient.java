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
import org.study.starter.utils.SnakeCaseUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * elasticsearch客户端，提供一些常规的方法，如果需要复杂的查询，可通过 #getRestEsClient() 方法取得ES的原生客户端来处理
 */
public class EsClient {
    public static final int MAX_GROUP_SIZE = 1000;//最大分组数量
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
            if(esQuery.getSnakeCase()){
                Map<String, Object> resultMap = snakeCaseKey(response.getHits().getHits()[0].getSourceAsMap());
                if(isString(clz)){
                    return (T)JsonUtil.toString(resultMap);
                }else{
                    return JsonUtil.toBean(JsonUtil.toString(resultMap), clz);
                }
            }else{
                if(isString(clz)){
                    return (T) response.getHits().getHits()[0].getSourceAsString();
                }else{
                    return JsonUtil.toBean(response.getHits().getHits()[0].getSourceAsString(), clz);
                }
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
        return getEntityList(response, clz, esQuery.getSnakeCase());
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
        List<T> entityList = getEntityList(response, clz, esQuery.getSnakeCase());
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

        EsAggResult aggResult = new EsAggResult();
        if(response.getHits().getTotalHits().value > 0){
            if (isEmpty(esQuery.getGroupBy())){
                fillEsAggResult(aggResult, null, response.getAggregations().iterator(), esQuery.getSnakeCase());
            }else{
                Iterator<Aggregation> iterator = response.getAggregations().iterator();
                while (iterator.hasNext()){
                    Aggregation agg = iterator.next();
                    ParsedTerms terms = (ParsedTerms) agg;

                    if(terms.getBuckets().isEmpty()){
                        continue;
                    }

                    for(Terms.Bucket bucket : terms.getBuckets()){
                        String groupValue = ((Terms.Bucket) bucket).getKeyAsString();
                        Aggregations bucketAgg = ((Terms.Bucket) bucket).getAggregations();
                        fillEsAggResult(aggResult, groupValue, bucketAgg.iterator(), esQuery.getSnakeCase());
                    }
                }
            }
        }
        return aggResult;
    }

    /**
     * 从查询结果中转换成List<T>返回
     * @param response
     * @return
     */
    public  <T> List<T> getEntityList(SearchResponse response, Class<T> clz, boolean snakeCase){
        List<T> entityList = new ArrayList<>();
        boolean isString = isString(clz);
        if(response.getHits().getTotalHits().value > 0){
            SearchHit[] hits = response.getHits().getHits();
            for(int i=0; i<hits.length; i++){
                if(snakeCase){
                    Map<String, Object> resultMap = snakeCaseKey(hits[i].getSourceAsMap());
                    if(isString){
                        entityList.add((T)JsonUtil.toString(resultMap));
                    }else{
                        entityList.add(JsonUtil.toBean(JsonUtil.toString(resultMap), clz));
                    }
                }else{
                    if(isString){
                        entityList.add((T)hits[i].getSourceAsString());
                    }else{
                        entityList.add(JsonUtil.toBean(hits[i].getSourceAsString(), clz));
                    }
                }
            }
        }
        return entityList;
    }

    private SearchResponse executeQuery(EsQuery esQuery){
        paramCheck(esQuery, false);

        if(esQuery.getScrollMode() && StringUtil.isNotEmpty(esQuery.getScrollId())){
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
        if(esQuery.getScrollMode()){
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
        Map<String, String> fieldMap = getESMappingFieldMap(esQuery.getIndex());
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
        Map<String, String> fieldMap = getESMappingFieldMap(esQuery.getIndex());
        boolean isNeedTerms = StringUtil.isNotEmpty(esQuery.getGroupBy());
        TermsAggregationBuilder termsAggBuilder = null;
        if (isNeedTerms) {
            termsAggBuilder = AggregationBuilders.terms(esQuery.getGroupBy()).field(esQuery.getGroupBy()).size(MAX_GROUP_SIZE);
        }

        Field[] fields = EsQuery.Aggregation.class.getDeclaredFields();
        for(Map.Entry<String, EsQuery.Aggregation> entry : esQuery.getAggMap().entrySet()){
            String aggField = entry.getKey();
            if(! fieldMap.containsKey(aggField)){ //ES中不存在的字段将直接忽略
                continue;
            }

            EsQuery.Aggregation agg = entry.getValue();
            for(Field field : fields){
                field.setAccessible(true);

                String name = field.getName();
                if(name.contains("this$") || "field".equals(name)){
                    continue;
                }

                Boolean value;
                try{
                    value = field.getBoolean(agg);
                }catch(Throwable e){
                    throw new BizException("EsQuery.Aggregation 获取"+name+"的属性值出现异常：", e);
                }
                if(value == null || value == false){
                    continue;
                }

                ValuesSourceAggregationBuilder aggBuilder;
                switch(name){
                    case "count":
                        aggBuilder = AggregationBuilders.count(fillFieldName(aggField, "count")).field(aggField);
                        break;
                    case "sum":
                        aggBuilder = AggregationBuilders.sum(fillFieldName(aggField, "sum")).field(aggField);
                        break;
                    case "min":
                        aggBuilder = AggregationBuilders.min(fillFieldName(aggField, "min")).field(aggField);
                        break;
                    case "max":
                        aggBuilder = AggregationBuilders.max(fillFieldName(aggField,"max")).field(aggField);
                        break;
                    case "avg":
                        aggBuilder = AggregationBuilders.avg(fillFieldName(aggField,"avg" )).field(aggField);
                        break;
                    default:
                        throw new BizException("EsQuery.Aggregation 未预期的属性名称：" + name);
                }

                if(isNeedTerms){
                    termsAggBuilder.subAggregation(aggBuilder);
                }else{
                    sourceBuilder.aggregation(aggBuilder);
                }
            }
        }

        if(isNeedTerms){
            sourceBuilder.aggregation(termsAggBuilder);
        }
    }

    private void fillEsAggResult(EsAggResult aggResult, String groupValue, Iterator<Aggregation> iterator, boolean snakeCase){
        while(iterator.hasNext()) {
            Aggregation aggEs = iterator.next();
            String fieldName = splitFieldName(aggEs.getName());
            if(snakeCase){
                fieldName = SnakeCaseUtil.toSnakeCase(fieldName, true);
            }

            org.study.starter.dto.Aggregation agg;
            if(groupValue == null){
                agg = aggResult.getAggMap().get(fieldName);
                if (agg == null) {
                    agg = new org.study.starter.dto.Aggregation();
                    aggResult.getAggMap().put(fieldName, agg);
                }
            }else{
                Map<String, org.study.starter.dto.Aggregation> aggMap = aggResult.getAggGroupMap().get(fieldName);
                if(aggMap == null){
                    agg = new org.study.starter.dto.Aggregation();
                    aggMap = new HashMap<>();
                    aggMap.put(groupValue, agg);
                    aggResult.getAggGroupMap().put(fieldName, aggMap);
                }else if((agg = aggMap.get(groupValue)) == null){
                    agg = new org.study.starter.dto.Aggregation();
                    aggMap.put(groupValue, agg);
                }
            }

            switch(aggEs.getType()){
                case ValueCountAggregationBuilder.NAME:
                    agg.setCount(((ParsedValueCount) aggEs).getValue());
                    break;
                case MaxAggregationBuilder.NAME:
                    agg.setMax(BigDecimal.valueOf(((ParsedMax) aggEs).getValue()));
                    break;
                case MinAggregationBuilder.NAME:
                    agg.setMin(BigDecimal.valueOf(((ParsedMin) aggEs).getValue()));
                    break;
                case SumAggregationBuilder.NAME:
                    agg.setSum(BigDecimal.valueOf(((ParsedSum) aggEs).getValue()));
                    break;
                case AvgAggregationBuilder.NAME:
                    agg.setAvg(BigDecimal.valueOf(((ParsedAvg) aggEs).getValue()));
                    break;
                default:
                    throw new BizException("未支持的聚合类型：" + aggEs.getType());
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

    private void paramCheck(EsQuery esQuery, boolean aggMapMust){
        if(esQuery == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "esQuery不能为空");
        }else if(StringUtil.isEmpty(esQuery.getIndex())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "index不能为空");
        }else if(esQuery.getPageSize() <= 0 || esQuery.getPageCurrent() <= 0){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "pageCurrent和pageSize都需大于0");
        }else if(aggMapMust && (esQuery.getAggMap() == null || esQuery.getAggMap().isEmpty())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "aggMap不能为空");
        }
    }

    private boolean isNotEmpty(String key, Object value){
        return ! (isEmpty(key) || (value == null || value.toString().trim().length() <= 0));
    }
    private boolean isNotEmpty(String key, Object[] values){
        return ! (isEmpty(key) || (values == null || values.length <= 0));
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

    private Map<String, Object> snakeCaseKey(Map<String, Object> entryMap){
        Map<String, Object> resultMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : entryMap.entrySet()) {
            resultMap.put(SnakeCaseUtil.toSnakeCase(entry.getKey(), true), entry.getValue());
        }
        return resultMap;
    }

    /**
     * 返回Mapping，其中key为字段名，value为字段的数据类型
     * @param index
     * @return
     */
    private Map<String, String> getESMappingFieldMap(String index){
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
                if(StringUtil.isNotEmpty(entry1.getKey())){
                    fieldMap.put(entry1.getKey(), (String) map.get("type"));
                }
            }
        }
        cache.put(index, fieldMap);
        return fieldMap;
    }
}


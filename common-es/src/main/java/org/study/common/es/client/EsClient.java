package org.study.common.es.client;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.study.common.es.query.EsQuery;
import org.study.common.es.query.Statistic;
import org.study.common.es.query.MultiStatistic;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.utils.ClassUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * elasticsearch客户端，提供一些常规的查询方法，如果需要复杂的查询，可通过 #getRestEsClient() 方法取得ES的原生客户端来处理
 */
public class EsClient {
    private RestHighLevelClient restEsClient;

    public EsClient(RestHighLevelClient restEsClient){
        this.restEsClient = restEsClient;
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
     * 统计，可统计多个字段，每个字段都可有：count、sum、min、max、avg 等统计方式
     * @param esQuery
     * @return
     */
    public MultiStatistic multiStatistic(EsQuery esQuery){
        SearchResponse response = executeStatic(esQuery);

        MultiStatistic statistic = new MultiStatistic();
        if(response.getHits().getTotalHits().value > 0){
            Iterator<Aggregation> iterator = response.getAggregations().iterator();
            while(iterator.hasNext()){
                Aggregation agg = iterator.next();
                String fieldName = splitFieldName(agg.getName());

                Statistic metrics = statistic.getStatisticMap().get(fieldName);
                if(metrics == null){
                    metrics = new Statistic();
                    statistic.getStatisticMap().put(fieldName, metrics);
                }

                fillMetrics(agg, metrics);
            }
        }
        return statistic;
    }

    /**
     * 统计，只能统计1个字段
     * @see #multiStatistic(EsQuery)
     * @param esQuery
     * @return
     */
    public Statistic statistic(EsQuery esQuery){
        if(esQuery != null && esQuery.getStatisFieldMap() != null && esQuery.getStatisFieldMap().size() > 1){
            throw new BizException("metrics only support single field");
        }

        Statistic metrics = new Statistic();
        SearchResponse response = executeStatic(esQuery);
        if(response.getHits().getTotalHits().value > 0){
            Iterator<Aggregation> iterator = response.getAggregations().iterator();
            while(iterator.hasNext()){
                Aggregation agg = iterator.next();
                fillMetrics(agg, metrics);
            }
        }
        return metrics;
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
        if(StringUtil.isNotEmpty(esQuery.getType())){
            searchRequest.types(esQuery.getType());
        }
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

    private SearchResponse executeStatic(EsQuery esQuery){
        paramCheck(esQuery, true);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(getQueryBuilder(esQuery));
        appendAggregation(sourceBuilder, esQuery);

        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex());
        if(StringUtil.isNotEmpty(esQuery.getType())){
            searchRequest.types(esQuery.getType());
        }
        searchRequest.source(sourceBuilder);

        try{
            return restEsClient.search(searchRequest, RequestOptions.DEFAULT);
        }catch(IOException e){
            throw new BizException("IOException " + e.getMessage());
        }
    }

    private QueryBuilder getQueryBuilder(EsQuery esQuery){
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();//等同于sql中的 and 查询

        //精确匹配
        if(isNotBlank(esQuery.getEqMap())){
            for(Map.Entry<String, Object> entry : esQuery.getEqMap().entrySet()){
                queryBuilder.filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
            }
        }

        //范围列表查询(in 查询)
        if(isNotBlank(esQuery.getInMap())){
            for(Map.Entry<String, Object[]> entry : esQuery.getInMap().entrySet()){
                queryBuilder.filter(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
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

        //全文搜索
        if(isNotBlank(esQuery.getLikeMap())){
            for(Map.Entry<String, Object> entry : esQuery.getLikeMap().entrySet()){
                queryBuilder.filter(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
            }
        }

        return queryBuilder;
    }

    private void appendAggregation(SearchSourceBuilder sourceBuilder, EsQuery esQuery){
        for(Map.Entry<String, EsQuery.StatisField> entry : esQuery.getStatisFieldMap().entrySet()){
            String fieldName = entry.getKey();
            EsQuery.StatisField field = entry.getValue();
            boolean isNoneMatch = true;

            if(field.getCount()){
                isNoneMatch = false;
                sourceBuilder.aggregation(AggregationBuilders.count(fillFieldName(fieldName, "count")).field(fieldName));
            }
            if(field.getSum()){
                isNoneMatch = false;
                sourceBuilder.aggregation(AggregationBuilders.sum(fillFieldName(fieldName, "sum")).field(fieldName));
            }
            if(field.getMax()){
                isNoneMatch = false;
                sourceBuilder.aggregation(AggregationBuilders.max(fillFieldName(fieldName, "max")).field(fieldName));
            }
            if(field.getMin()){
                isNoneMatch = false;
                sourceBuilder.aggregation(AggregationBuilders.min(fillFieldName(fieldName, "min")).field(fieldName));
            }
            if(field.getAvg()){
                isNoneMatch = false;
                sourceBuilder.aggregation(AggregationBuilders.avg(fillFieldName(fieldName, "avg")).field(fieldName));
            }

            if(isNoneMatch){
                throw new BizException(fieldName+"需指定至少一个统计项");
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
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "StatisFieldMap不能为空");
        }
    }

    private void fillMetrics(Aggregation agg, Statistic metrics){
        String type = agg.getType();

        if(ValueCountAggregationBuilder.NAME.equals(type)){
            metrics.setCount( ((ParsedValueCount)agg).getValue() );
        }else if(MaxAggregationBuilder.NAME.equals(type)){
            metrics.setMax(BigDecimal.valueOf( ((ParsedMax)agg).getValue() ));
        }else if(MinAggregationBuilder.NAME.equals(type)){
            metrics.setMin(BigDecimal.valueOf( ((ParsedMin)agg).getValue() ));
        }else if(SumAggregationBuilder.NAME.equals(type)){
            metrics.setSum(BigDecimal.valueOf( ((ParsedSum)agg).getValue() ));
        }else if(AvgAggregationBuilder.NAME.equals(type)){
            metrics.setAvg(BigDecimal.valueOf( ((ParsedAvg)agg).getValue() ));
        }
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

    public RestHighLevelClient getRestEsClient() {
        return restEsClient;
    }
}

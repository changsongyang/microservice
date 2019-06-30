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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.EsQuery;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.utils.ClassUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.StringUtil;

import java.io.IOException;
import java.util.*;

/**
 * elasticsearch客户端，提供一些常规的查询方法，如果需要复杂的查询，可通过 #getRestEsClient() 方法取得ES的原生客户端来处理
 */
public class EsClient {
    private RestHighLevelClient restEsClient;

    public void SetRestHighLevelClient(RestHighLevelClient restEsClient){
        this.restEsClient = restEsClient;
    }


    /**
     * 取得单个实体
     * @param esQuery
     * @param <T>
     * @return
     */
    public <T> T getOne(EsQuery esQuery){
        paramCheck(esQuery);
        Class<T> clz = getReturnClass(esQuery);

        SearchResponse response = executeQuery(esQuery);
        if(response.getHits().getTotalHits().value > 0){
            return JsonUtil.toBean(response.getHits().getHits()[0].getSourceAsString(), clz);
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
        paramCheck(esQuery);
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
        paramCheck(esQuery);
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
     * 从查询结果中转换成List<T>返回
     * @param response
     * @return
     */
    public  <T> List<T> getEntityList(SearchResponse response, Class<T> clz){
        List<T> entityList = new ArrayList<>();
        if(response.getHits().getTotalHits().value > 0){
            SearchHit[] hits = response.getHits().getHits();
            for(int i=0; i<hits.length; i++){
                entityList.add(JsonUtil.toBean(hits[i].getSourceAsString(), clz));
            }
        }
        return entityList;
    }

    private SearchResponse executeQuery(EsQuery esQuery){
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
        if(esQuery.getSelectFields() != null && esQuery.getSelectFields().length > 0){
            sourceBuilder.fetchSource(esQuery.getSelectFields(), null);
        }
        sourceBuilder.query(getQueryBuilder(esQuery));

        SearchRequest searchRequest = new SearchRequest(esQuery.getIndex())
                .types(esQuery.getType())
                .source(sourceBuilder);
        if(esQuery.getIsScroll()){
            searchRequest.scroll(TimeValue.timeValueSeconds(esQuery.getScrollExpireSec()));
            sourceBuilder.size(esQuery.getPageSize());
        }

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
            }
        }

        //全文搜索
        if(isNotBlank(esQuery.getLikeMap())){
            for(Map.Entry<String, Object> entry : esQuery.getLikeMap().entrySet()){
                queryBuilder.filter(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
            }
        }

        return null;
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

    private void paramCheck(EsQuery esQuery){
        if(esQuery == null){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "esQuery不能为空");
        }else if(StringUtil.isEmpty(esQuery.getIndex())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "index不能为空");
        }else if(StringUtil.isEmpty(esQuery.getType())){
            throw new BizException(BizException.PARAM_VALIDATE_ERROR, "type不能为空");
        }
    }

    private boolean isNotBlank(Map map){
        return map != null && ! map.isEmpty();
    }


    public RestHighLevelClient getRestEsClient() {
        return restEsClient;
    }
}

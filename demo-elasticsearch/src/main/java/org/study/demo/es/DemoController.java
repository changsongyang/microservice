package org.study.demo.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.dto.EsAggResult;
import org.study.common.util.dto.EsQuery;
import org.study.starter.component.ESClient;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ESClient esClient;

    @ResponseBody
    @RequestMapping(value = "/getOne", method = RequestMethod.GET)
    public String getOne(String index) {
        try{
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("ID", 22168781);

            String result = esClient.getOne(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/listBy", method = RequestMethod.GET)
    public List<String> listBy(String index, Integer pagSize) {
        try{
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("USER_NO", "888100000005252")
            .size(pagSize);

            List<String> result = esClient.listBy(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/listPage", method = RequestMethod.GET)
    public PageResult<List<String>> listPage(String index, Integer pageCurrent) {
        try{
            EsQuery esQuery = EsQuery.build(true).from(index);

            esQuery
//                    .eq("userNo", "888100000005252")
                    .neq("userNo", "888100000005252")
                    .notIn("alterType", "1,2,3".split(","))
                    .page(pageCurrent, 20)
                    .result(HashMap.class);

            PageResult<List<String>> result = esClient.listPage(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/scrollPage", method = RequestMethod.GET)
    public PageResult<List<String>> scrollPage(String index, String scrollId, Integer pagSize) {
        try{
            EsQuery esQuery = EsQuery.build(true).from(index);

            esQuery.eq("userNo", "888100000005252").scroll(scrollId, 60, pagSize);

            PageResult<List<String>> result = esClient.listPage(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/aggregation", method = RequestMethod.GET)
    public EsAggResult aggregation(String index, String groupBy) {
        EsQuery esQuery = EsQuery.build(true).from(index);

        esQuery.count("id")
                .sum("alterBalanceLong")
                .avg("alterBalanceLong")
                .min("alterBalanceLong")
                .max("alterBalanceLong")
                .eq("userNo", "888100000005252")
                .groupBy(groupBy)
        ;

        EsAggResult result = esClient.aggregation(esQuery);

        return result;
    }
}

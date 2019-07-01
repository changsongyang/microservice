package org.study.demo.es;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.es.client.EsClient;
import org.study.common.es.query.EsQuery;
import org.study.common.es.query.Statistic;
import org.study.common.es.query.MultiStatistic;
import org.study.common.statics.pojos.PageResult;

import java.util.List;

@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EsClient esClient;

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
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("USER_NO", "888100000005252").page(pageCurrent, 2);

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
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("USER_NO", "888100000005252").scroll(scrollId, 60, pagSize);

            PageResult<List<String>> result = esClient.listPage(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
    public MultiStatistic statistic(String index) {
        try{
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("USER_NO", "888100000005252")
                    .count("ID")
                    .max("ID")
                    .min("ID")
                    .avg("ID")
                    .sum("ALTER_BALANCE_LONG")
                    .avg("ALTER_BALANCE_LONG")
                    .min("ALTER_BALANCE_LONG")
                    .max("ALTER_BALANCE_LONG")
            ;

            MultiStatistic result = esClient.multiStatistic(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public Statistic metrics(String index) {
        try{
            EsQuery esQuery = EsQuery.build().from(index);

            esQuery.eq("USER_NO", "888100000005252")
                    .count("ID")
                    .max("ID")
                    .min("ID")
                    .avg("ID")
            ;

            Statistic result = esClient.statistic(esQuery);

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

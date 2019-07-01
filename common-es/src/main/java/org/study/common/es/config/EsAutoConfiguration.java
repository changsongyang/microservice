package org.study.common.es.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.common.es.client.EsClient;

@Configuration
@ConditionalOnClass({RestHighLevelClient.class})
public class EsAutoConfiguration {

    @Bean
    public EsClient esClient(RestHighLevelClient restHighLevelClient){
        return new EsClient(restHighLevelClient);
    }
}

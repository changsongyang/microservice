package org.study.starter.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.starter.component.EsClient;

@AutoConfigureAfter(ElasticsearchAutoConfiguration.class)
@ConditionalOnClass(RestHighLevelClient.class)
@Configuration
public class EsAutoConfiguration {

    @ConditionalOnBean(RestHighLevelClient.class)
    @Bean
    public EsClient esClient(RestHighLevelClient restHighLevelClient){
        return new EsClient(restHighLevelClient);
    }

}

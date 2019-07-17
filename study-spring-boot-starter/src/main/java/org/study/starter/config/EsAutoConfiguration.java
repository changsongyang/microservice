package org.study.starter.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.study.starter.component.EsClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@AutoConfigureAfter(ElasticsearchAutoConfiguration.class)
@ConditionalOnClass(RestHighLevelClient.class)
@Configuration
public class EsAutoConfiguration {

    @ConditionalOnBean(RestHighLevelClient.class)
    @Bean
    public EsClient esClient(RestHighLevelClient restHighLevelClient){
        return new EsClient(restHighLevelClient, esMappingCache());
    }

    /**
     * guava 本地缓存
     *
     * @return
     */
    @Bean
    public Cache<String, Map<String, String>> esMappingCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)//过期时间
                .maximumSize(10000)
                .initialCapacity(50)
                .concurrencyLevel(10)
                .build();
    }
}

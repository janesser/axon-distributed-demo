package org.axonframework.queryhandling.config;

import org.axonframework.queryhandling.updatestore.repository.redis.SubscriptionIdRedisBytesReader;
import org.axonframework.queryhandling.updatestore.repository.redis.SubscriptionIdRedisBytesWriter;
import org.axonframework.queryhandling.updatestore.repository.redis.SubscriptionIdRedisStringReader;
import org.axonframework.queryhandling.updatestore.repository.redis.SubscriptionIdRedisStringWriter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;

import java.util.Arrays;

/**
 * @see RedisRepositoriesAutoConfiguration
 * @see RedisRepositoryConfigurationExtension#registerBeansForRoot(org.springframework.beans.factory.support.BeanDefinitionRegistry, org.springframework.data.repository.config.RepositoryConfigurationSource)
 */
@ConditionalOnProperty(
        prefix = "axon.queryhandling",
        name = "updatestore",
        havingValue = "redis"
)
@Configuration
@ComponentScan("org.axonframework.queryhandling.updatestore.repository.redis")
@EnableRedisRepositories("org.axonframework.queryhandling.updatestore.repository")
@AutoConfigureBefore(RedisRepositoriesAutoConfiguration.class)
public class DistributedQueryBusRedisAutoConfiguration {

    @Bean
    public RedisCustomConversions redisCustomConversions(
            SubscriptionIdRedisStringReader stringReader,
            SubscriptionIdRedisBytesReader bytesReader,
            SubscriptionIdRedisStringWriter stringWriter,
            SubscriptionIdRedisBytesWriter bytesWriter
    ) {
        return new RedisCustomConversions(Arrays.asList(
                stringReader,
                bytesReader,
                stringWriter,
                bytesWriter
        ));
    }
}

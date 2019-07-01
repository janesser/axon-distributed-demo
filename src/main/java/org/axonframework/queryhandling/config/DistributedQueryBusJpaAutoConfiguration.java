package org.axonframework.queryhandling.config;

import org.axonframework.queryhandling.updatestore.JpaStoreCleansing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConditionalOnProperty(
        prefix = "axon.queryhandling",
        name = "updatestore",
        havingValue = "jpa",
        matchIfMissing = true
)
@Configuration
@EnableJpaRepositories(basePackages = "org.axonframework.queryhandling.updatestore.repository")
public class DistributedQueryBusJpaAutoConfiguration {

    @Value("${cleanRateSeconds:5}")
    private long cleanRateSeconds;

    @Value("${updateAgeSeconds:300}")
    private long updateAgeSeconds;

    @Value("${subscriptionAgeSeconds:600}")
    private long subscriptionAgeSeconds;

    @ConditionalOnProperty(
            prefix = "axon.queryhandling",
            name = "updatestore.cleansing",
            havingValue = "true",
            matchIfMissing = true
    )
    @Bean
    public JpaStoreCleansing storeCleansing() {
        return new JpaStoreCleansing(
                cleanRateSeconds,
                updateAgeSeconds,
                subscriptionAgeSeconds
        );
    }

}

package org.axonframework.queryhandling;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.queryhandling.jpa.JpaQueryUpdateStore;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigureBefore(AxonAutoConfiguration.class)
@RegisterDefaultEntities(packages = {
        "org.axonframework.queryhandling.jpa.model"
})
@EnableJpaRepositories(basePackages = "org.axonframework.queryhandling.jpa.repository")
public class DistributedQueryBusAutoConfig {

    @Primary
    @Bean("queryUpdateEmitter")
    @DependsOn("localSegment")
    public DistributedQueryUpdateEmitter distributedQueryUpdateEmitter() {
        return new DistributedQueryUpdateEmitter();
    }

    @Primary
    @Bean("queryBus")
    @DependsOn("localSegment")
    public DistributedQueryBus distributedQueryBus() {
        return new DistributedQueryBus();
    }

    @Bean
    public QueryUpdatePollingService queryUpdatePollingService() {
        return new QueryUpdatePollingService();
    }

    @Bean
    public QueryUpdateStore queryUpdateStore() {
        return new JpaQueryUpdateStore();
    }

    @Bean
    public SubscriberIdentityService subscriberIdentityService() {
        return new SubscriberIdentityService();
    }

    /*
     * copy from org.axonframework.config.DefaultConfigurer.defaultQueryUpdateEmitter
     */
    @Bean("localQueryUpdateEmitter")
    public QueryUpdateEmitter localQueryUpdateEmitter(AxonConfiguration config) {
        MessageMonitor<? super SubscriptionQueryUpdateMessage<?>> updateMessageMonitor =
                config.messageMonitor(QueryUpdateEmitter.class, "queryUpdateEmitter");
        return SimpleQueryUpdateEmitter.builder()
                .updateMessageMonitor(updateMessageMonitor)
                .build();
    }

    // FROM AxonAutoConfiguration START

    // @ConditionalOnMissingBean(QueryInvocationErrorHandler.class)
    @Bean("localSegment")
    public SimpleQueryBus queryBus(AxonConfiguration axonConfiguration,
                                   TransactionManager transactionManager,
                                   @Qualifier("localQueryUpdateEmitter") QueryUpdateEmitter localQueryUpdateEmitter) {
        return SimpleQueryBus.builder()
                .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class, "queryBus"))
                .transactionManager(transactionManager)
                .errorHandler(axonConfiguration.getComponent(
                        QueryInvocationErrorHandler.class,
                        () -> LoggingQueryInvocationErrorHandler.builder().build()
                ))
                .queryUpdateEmitter(localQueryUpdateEmitter)
                .build();
    }

    // FROM AxonAutoConfiguration END

}

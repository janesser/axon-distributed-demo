package org.axonframework.queryhandling.benchmark;

import demo.DemoApp;
import org.axonframework.queryhandling.DistributedQueryBus;
import org.axonframework.queryhandling.SubscriberIdentityService;
import org.axonframework.queryhandling.config.DistributedQueryBusAutoConfiguration;
import org.openjdk.jmh.annotations.Benchmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest(classes = {
        DistributedQueryBusJpaBenchmark.RandomSubscriberIdentityConfiguration.class,
        DistributedQueryBusAutoConfiguration.class,
        DemoApp.class
})
@ActiveProfiles({"spring-test-hsqldb"})
public class DistributedQueryBusJpaBenchmark extends AbstractQueryBusBenchmark {

    @TestConfiguration
    public static class RandomSubscriberIdentityConfiguration {

        @Bean
        public SubscriberIdentityService subscriberIdentityService() {
            return new SubscriberIdentityService() {
                @Override
                public String getSubscriberIdentify() {
                    // never identify self
                    return UUID.randomUUID().toString();
                }
            };
        }
    }

    @Autowired
    public void setDistributedQueryBus(DistributedQueryBus queryBus) {
        super.setQueryBus(queryBus);
    }

    @Benchmark
    public void benchmarkDistributedRemote() {
        super.benchmarkQueryBus();
    }
}

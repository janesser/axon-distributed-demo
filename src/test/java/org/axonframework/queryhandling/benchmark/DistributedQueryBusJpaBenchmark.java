package org.axonframework.queryhandling.benchmark;

import demo.DemoApp;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.queryhandling.*;
import org.axonframework.queryhandling.config.DistributedQueryBusAutoConfiguration;
import org.axonframework.spring.config.AxonConfiguration;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.Benchmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Predicate;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@SpringBootTest(classes = {
        DistributedQueryBusJpaBenchmark.MutedLocalSegmentTestConfig.class,
        DistributedQueryBusAutoConfiguration.class,
        DemoApp.class
})
@ActiveProfiles({"spring-test-hsqldb"})
public class DistributedQueryBusJpaBenchmark extends AbstractQueryBusBenchmark {

    @TestConfiguration
    public static class MutedLocalSegmentTestConfig {

        @Bean("localQueryUpdateEmitter")
        public QueryUpdateEmitter localQueryUpdateEmitter(AxonConfiguration config) {
            MessageMonitor<? super SubscriptionQueryUpdateMessage<?>> updateMessageMonitor =
                    config.messageMonitor(QueryUpdateEmitter.class, "queryUpdateEmitter");
            QueryUpdateEmitter spy = spy(SimpleQueryUpdateEmitter.builder()
                    .updateMessageMonitor(updateMessageMonitor)
                    .build());
            muteLocalQueryUpdateEmitter(spy);
            return spy;
        }
    }

    private static <U> void muteLocalQueryUpdateEmitter(QueryUpdateEmitter spy) {
        doNothing().when(spy).emit(
                Mockito.<Predicate<SubscriptionQueryMessage<?, ?, U>>>any(),
                Mockito.<SubscriptionQueryUpdateMessage<U>>any()
        );
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

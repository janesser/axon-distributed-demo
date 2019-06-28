package org.axonframework.queryhandling.benchmark;

import demo.DemoApp;
import demo.DemoQuery;
import demo.DemoQueryResult;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.*;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.UUID;

/**
 * https://gist.github.com/msievers/ce80d343fc15c44bea6cbb741dde7e45
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DistributedQueryBusAutoConfig.class,
        DemoApp.class
})
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@ActiveProfiles("spring-test")
public abstract class AbstractQueryBusBenchmark extends AbstractBenchmarkTest {

    private static QueryBus queryBus;

    protected void setQueryBus(QueryBus queryBus) {
        AbstractQueryBusBenchmark.queryBus = queryBus;
    }

    public void benchmarkQueryBus() {
        QueryGateway queryGateway = DefaultQueryGateway.builder().queryBus(queryBus).build();
        String aggId = UUID.randomUUID().toString();

        DemoQuery q = new DemoQuery(aggId);

        SubscriptionQueryResult<DemoQueryResult, DemoQueryResult> result = queryGateway.subscriptionQuery(
                q,
                ResponseTypes.instanceOf(DemoQueryResult.class),
                ResponseTypes.instanceOf(DemoQueryResult.class)
        );

        queryBus.queryUpdateEmitter().emit(DemoQuery.class,
                dq -> true,
                new DemoQueryResult(aggId));

        result.updates().blockFirst(Duration.ofSeconds(1L));
    }
}

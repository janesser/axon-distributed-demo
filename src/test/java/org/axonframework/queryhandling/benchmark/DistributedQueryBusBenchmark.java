package org.axonframework.queryhandling.benchmark;

import org.axonframework.queryhandling.DistributedQueryBus;
import org.openjdk.jmh.annotations.Benchmark;
import org.springframework.beans.factory.annotation.Autowired;

public class DistributedQueryBusBenchmark extends AbstractQueryBusBenchmark {

    @Autowired
    public void setDistributedQueryBus(DistributedQueryBus queryBus) {
        super.setQueryBus(queryBus);
    }

    @Benchmark
    public void benchmarkDistributedLocal() {
        super.benchmarkQueryBus();
    }

    // @Benchmark
    public void benchmarkDistributedJpa() {
        // TODO bypass embedded SimpleQueryBus
        super.benchmarkQueryBus();
    }

}

package org.axonframework.queryhandling.benchmark;

import org.axonframework.queryhandling.SimpleQueryBus;
import org.openjdk.jmh.annotations.Benchmark;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleQueryBusBenchmark extends AbstractQueryBusBenchmark {

    @Autowired
    public void setSimpleQueryBus(SimpleQueryBus queryBus) {
        super.setQueryBus(queryBus);
    }

    @Benchmark
    public void benchmarkLocalSegment() {
        super.benchmarkQueryBus();
    }
}

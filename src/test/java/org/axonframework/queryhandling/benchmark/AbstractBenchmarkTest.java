package org.axonframework.queryhandling.benchmark;

import org.junit.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * https://gist.github.com/msievers/ce80d343fc15c44bea6cbb741dde7e45
 */
public abstract class AbstractBenchmarkTest {

    @Test
    public void benchmark() throws RunnerException {
        Options options = new OptionsBuilder()
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(0)
                .measurementIterations(1)
                .forks(0)
                .threads(2)
                .shouldFailOnError(true)
                .build();

        new Runner(options).run();
    }
}
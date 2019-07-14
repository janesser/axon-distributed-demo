package org.axonframework.queryhandling.benchmark;

import demo.DemoApp;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.queryhandling.config.DistributedQueryBusAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * FIXME updates Flux doesn't get unblocked more than once.
 *
 * @see AxonServerQueryBus
 */
@Ignore
@SpringBootTest(classes = {
        AxonServerAutoConfiguration.class,
        AxonAutoConfiguration.class,
        DemoApp.class
})
@EnableAutoConfiguration(exclude = DistributedQueryBusAutoConfiguration.class)
@ActiveProfiles({"spring-test-hsqldb"})
@DirtiesContext
public class AxonServerQueryBusBenchmark extends AbstractQueryBusBenchmark {

    @Autowired
    public void setAxonServerQueryGateway(AxonServerQueryBus queryBus) {
        super.setQueryBus(queryBus);
    }

}

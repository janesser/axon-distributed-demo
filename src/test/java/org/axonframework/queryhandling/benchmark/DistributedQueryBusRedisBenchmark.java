package org.axonframework.queryhandling.benchmark;

import demo.DemoApp;
import org.axonframework.queryhandling.config.DistributedQueryBusAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@SpringBootTest(classes = {
        DistributedQueryBusJpaBenchmark.RandomSubscriberIdentityConfiguration.class,
        DistributedQueryBusRedisBenchmark.EmbeddedRedisTestConfiguration.class,
        DistributedQueryBusAutoConfiguration.class,
        DemoApp.class
})
@ActiveProfiles({"spring-test-redis", "spring-test-hsqldb"})
public class DistributedQueryBusRedisBenchmark extends DistributedQueryBusJpaBenchmark {

    @TestConfiguration
    public static class EmbeddedRedisTestConfiguration {

        private final redis.embedded.RedisServer redisServer;

        public EmbeddedRedisTestConfiguration(@Value("${spring.redis.port}") int redisPort) throws IOException {
            this.redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("maxmemory 1G")
                    .setting("save 10 1000")
                    .build();
        }

        @PostConstruct
        public void startRedis() {
            this.redisServer.start();
        }

        @PreDestroy
        public void stopRedis() {
            this.redisServer.stop();
        }
    }
}

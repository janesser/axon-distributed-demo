package org.axonframework.queryhandling.updatestore;

import org.axonframework.queryhandling.updatestore.repository.QueryUpdateRepository;
import org.axonframework.queryhandling.updatestore.repository.SubscriptionRepository;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JpaStoreCleansing {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    @Resource
    private SubscriptionRepository subscriptionRepository;

    @Resource
    private QueryUpdateRepository queryUpdateRepository;

    public JpaStoreCleansing(
            long cleanRateSeconds,
            long updateAgeSeconds,
            long subscriptionAgeSeconds
    ) {
        executorService.scheduleAtFixedRate(() -> {
            Instant updateDeadline =
                    Instant.now()
                            .minus(updateAgeSeconds, ChronoUnit.SECONDS);
            queryUpdateRepository.findByCreationTimeLessThan(updateDeadline)
                    .forEach(upt ->
                            queryUpdateRepository.delete(upt));

            Instant subscriptionDeadline =
                    Instant.now()
                            .minus(subscriptionAgeSeconds, ChronoUnit.SECONDS);
            subscriptionRepository.findByCreationTimeLessThan(subscriptionDeadline)
                    .forEach(sub ->
                            subscriptionRepository.delete(sub));
        }, 0L, cleanRateSeconds, TimeUnit.SECONDS);


    }
}

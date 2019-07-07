package org.axonframework.queryhandling.updatestore;

import org.axonframework.queryhandling.updatestore.repository.QueryUpdateRepository;
import org.axonframework.queryhandling.updatestore.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class JpaStoreCleansing {

    @Value("${axon.queryhandling.updatestore.JpaStoreCleansing.cleanRateMillis:5}")
    private long cleanRateMillis;

    @Value("${axon.queryhandling.updatestore.JpaStoreCleansing.updateAgeSeconds:300}")
    private long updateAgeSeconds;

    @Value("${axon.queryhandling.updatestore.JpaStoreCleansing.subscriptionAgeSeconds:600}")
    private long subscriptionAgeSeconds;

    @Resource
    private SubscriptionRepository subscriptionRepository;

    @Resource
    private QueryUpdateRepository queryUpdateRepository;

    private final Timer cleansingTimer = new Timer();

    @PostConstruct
    public void startCleansingTimer() {
        cleansingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
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
            }
        }, 0L, cleanRateMillis);

    }

    @PreDestroy
    public void stopCleansingTimer() {
        cleansingTimer.cancel();
    }
}

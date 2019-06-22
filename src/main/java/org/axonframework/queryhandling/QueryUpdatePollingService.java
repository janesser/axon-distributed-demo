package org.axonframework.queryhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class QueryUpdatePollingService {
    private static final ScheduledExecutorService UPDATE_POLL = Executors.newScheduledThreadPool(2);

    @Value("${org.axonframework.queryhandling.QueryUpdatePollingService.periodMillis:100}")
    private long periodMillis;

    @Autowired
    private QueryUpdateStore queryUpdateStore;

    public <U> ScheduledFuture<?> startPolling(SubscriptionId subscriptionId, FluxSinkWrapper<U> fluxSinkWrapper) {
        log.info("polling now for: " + subscriptionId);
        log.info("period millis: " + periodMillis);
        return UPDATE_POLL.scheduleAtFixedRate(() -> {
                    Optional<U> peek = queryUpdateStore
                            .popUpdate(subscriptionId);
                    log.error("Polled on {} got {}.", subscriptionId, peek);
                    peek.ifPresent(fluxSinkWrapper::next);
                }, 0L, periodMillis, TimeUnit.MILLISECONDS
        );
    }

}

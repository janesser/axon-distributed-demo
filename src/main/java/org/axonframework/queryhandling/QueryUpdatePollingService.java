package org.axonframework.queryhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.axonframework.queryhandling.jpa.model.QueryUpdateEntity.asSubscriptionQueryUpdateMessage;

@Slf4j
public class QueryUpdatePollingService {
    private static final ScheduledExecutorService UPDATE_POLL = Executors.newSingleThreadScheduledExecutor();

    @Value("${org.axonframework.queryhandling.QueryUpdatePollingService.periodMillis:500}")
    private long periodMillis;

    @Value("${org.axonframework.queryhandling.QueryUpdatePollingService.maxMillis:10000}")
    private long maxMillis;

    @Autowired
    private QueryUpdateStore queryUpdateStore;

    public <U> ScheduledFuture<?> startPolling(SubscriptionId subscriptionId, FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper) {
        log.debug("polling now for: {}", subscriptionId);
        log.debug("period millis: {} max millies: {}", periodMillis, maxMillis);
        ScheduledFuture<?> future = UPDATE_POLL.scheduleAtFixedRate(() -> {
                    Optional<U> peek = queryUpdateStore
                            .popUpdate(subscriptionId);
                    log.debug("Polled on {} got {}.", subscriptionId, peek);
                    peek.ifPresent(u -> fluxSinkWrapper.next(asSubscriptionQueryUpdateMessage(u)));
                }, 0L, periodMillis, TimeUnit.MILLISECONDS
        );

        UPDATE_POLL.scheduleWithFixedDelay(() -> future.cancel(true), 0L, maxMillis, TimeUnit.MILLISECONDS);

        return future;
    }

}

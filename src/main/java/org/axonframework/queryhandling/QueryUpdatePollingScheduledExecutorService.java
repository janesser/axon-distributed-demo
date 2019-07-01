package org.axonframework.queryhandling;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Registration;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * needs to be exactly in this package for access to {@link FluxSinkWrapper}.
 */
@Slf4j
public class QueryUpdatePollingScheduledExecutorService extends AbstractQueryUpdatePollingService {

    private static final ScheduledExecutorService UPDATE_POLL = Executors.newSingleThreadScheduledExecutor();

    @Value("${org.axonframework.queryhandling.QueryUpdatePollingScheduledExecutorService.periodMillis:500}")
    private long periodMillis;

    @Value("${org.axonframework.queryhandling.QueryUpdatePollingScheduledExecutorService.maxMillis:10000}")
    private long maxMillis;

    @Resource
    private QueryUpdateStore queryUpdateStore;

    @Override
    public <U> Registration startPolling(SubscriptionId subscriptionId, FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper) {
        log.debug("polling now for: {}", subscriptionId);
        log.debug("period millis: {} max millies: {}", periodMillis, maxMillis);
        ScheduledFuture<?> future = UPDATE_POLL.scheduleAtFixedRate(() -> {
                    super.peekAndSink(subscriptionId, fluxSinkWrapper);
                }, 0L, periodMillis, TimeUnit.MILLISECONDS
        );

        UPDATE_POLL.scheduleWithFixedDelay(() -> future.cancel(true), 0L, maxMillis, TimeUnit.MILLISECONDS);

        return new Registration() {
            @Override
            public boolean cancel() {
                return future.cancel(true);
            }
        };
    }

}

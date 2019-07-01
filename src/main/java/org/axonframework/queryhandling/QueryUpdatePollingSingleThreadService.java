package org.axonframework.queryhandling;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Registration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class QueryUpdatePollingSingleThreadService extends AbstractQueryUpdatePollingService {

    @Value
    private class PollingPair<U> {
        private final SubscriptionId id;
        private final FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> sinkWrapper;

        public PollingPair(SubscriptionId id, FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> sinkWrapper) {
            this.id = id;
            this.sinkWrapper = sinkWrapper;
        }
    }

    private final List<PollingPair> pollings = new CopyOnWriteArrayList<>();

    private final Thread poller = new Thread(() -> {
        while (true) {
            log.debug("polling for {} pollings.", pollings.size());

            for (PollingPair pair : pollings) {
                super.peekAndSink(pair.getId(), pair.getSinkWrapper());
            }

            Thread.yield();
        }
    });

    @PostConstruct
    public void startPolling() {
        poller.start();
    }

    @Override
    public <U> Registration startPolling(SubscriptionId subscriptionId, FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper) {
        PollingPair pair = new PollingPair(subscriptionId, fluxSinkWrapper);
        pollings.add(pair);

        return () -> {
            log.debug("poll for {} removed", pair.getId());
            pollings.remove(pair);
            return true;
        };
    }

    @SuppressWarnings("deprecation")
    @PreDestroy
    public void stopPolling() {
        poller.stop();
    }
}

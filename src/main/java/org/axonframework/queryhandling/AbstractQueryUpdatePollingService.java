package org.axonframework.queryhandling;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Optional;

import static org.axonframework.queryhandling.updatestore.model.QueryUpdateEntity.asSubscriptionQueryUpdateMessage;

@Slf4j
public abstract class AbstractQueryUpdatePollingService implements QueryUpdatePollingService {
    @Resource
    protected QueryUpdateStore queryUpdateStore;

    <U> void peekAndSink(
            SubscriptionId subscriptionId,
            FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper
    ) {
        Optional<U> peek = queryUpdateStore
                .popUpdate(subscriptionId);
        log.debug("Polled on {} got {}.", subscriptionId, peek);
        peek.ifPresent(u -> fluxSinkWrapper.next(asSubscriptionQueryUpdateMessage(u)));
    }
}

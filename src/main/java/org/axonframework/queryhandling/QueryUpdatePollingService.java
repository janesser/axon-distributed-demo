package org.axonframework.queryhandling;

import org.axonframework.common.Registration;
import org.axonframework.queryhandling.updatestore.model.SubscriptionEntity;

public interface QueryUpdatePollingService {

    <U> Registration startPolling(
            SubscriptionId subscriptionId,
            FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper
    );
}

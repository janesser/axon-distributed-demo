package org.axonframework.queryhandling.jpa.repository;

import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.SubscriptionId;
import org.axonframework.queryhandling.jpa.model.SubscriptionEntity;
import org.axonframework.serialization.Serializer;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface SubscriptionRepository<Q, I, U> extends CrudRepository<SubscriptionEntity<Q, I, U>, SubscriptionId> {

    default SubscriptionEntity<Q, I, U> getOrCreateSubscription(
            SubscriptionId id,
            Q payload,
            ResponseType<I> initialResponseType,
            ResponseType<U> updateResponseType,
            Serializer messageSerializer) {
        Optional<SubscriptionEntity<Q, I, U>> subscriptionOpt = findById(id);
        SubscriptionEntity<Q, I, U> subscriptionEntity = subscriptionOpt
                .orElse(new SubscriptionEntity<>(
                        id.getNodeId(),
                        payload,
                        initialResponseType,
                        updateResponseType,
                        messageSerializer
                ));
        save(subscriptionEntity);
        return subscriptionEntity;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    Iterable<SubscriptionEntity<Q, I, U>> findAll();
}

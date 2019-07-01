package org.axonframework.queryhandling.updatestore.repository;

import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.SubscriptionId;
import org.axonframework.queryhandling.updatestore.model.SubscriptionEntity;
import org.axonframework.serialization.Serializer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
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

    List<SubscriptionEntity> findByCreationTimeLessThan(Instant minAge) ;
}

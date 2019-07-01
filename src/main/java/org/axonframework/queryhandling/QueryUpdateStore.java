package org.axonframework.queryhandling;

import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.updatestore.model.SubscriptionEntity;
import org.axonframework.serialization.Serializer;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface QueryUpdateStore {
    <Q, I, U> SubscriptionEntity<Q, I, U> getOrCreateSubscription(SubscriptionId id, Q payload, ResponseType<I> responseType, ResponseType<U> updateResponseType);

    default <Q, I, U> SubscriptionEntity<Q, I, U> getOrCreateSubscription(SubscriptionQueryMessage<Q, I, U> query) {
        return getOrCreateSubscription(buildIdFromQuery(query), query.getPayload(), query.getResponseType(), query.getUpdateResponseType());
    }

    boolean subscriptionExists(SubscriptionId id);

    default boolean subscriptionExists(SubscriptionQueryMessage<?, ?, ?> query) {
        return subscriptionExists(buildIdFromQuery(query));
    }

    void removeSubscription(SubscriptionId id);

    default void removeSubscription(SubscriptionQueryMessage<?, ?, ?> query) {
        removeSubscription(buildIdFromQuery(query));
    }

    <Q, I, U> Stream<SubscriptionEntity<Q, I, U>> getSubscriptions(
            Predicate<SubscriptionEntity<Q, I, U>> filter);

    default <Q, I, U> Stream<SubscriptionEntity<Q, I, U>> getSubscriptionsFiltered(
            Predicate<SubscriptionQueryMessage<?, ?, U>> filter,
            Serializer serializer) {
        return getSubscriptions(subscriptionEntity ->
                filter.test(subscriptionEntity.asSubscriptionQueryMessage(serializer))
        );
    }


    <U> void postUpdate(SubscriptionEntity subscription, SubscriptionQueryUpdateMessage<U> update);

    <U> Optional<U> popUpdate(SubscriptionId subscriptionEntityId);


    SubscriptionId buildIdFromQuery(SubscriptionQueryMessage<?, ?, ?> query);
}

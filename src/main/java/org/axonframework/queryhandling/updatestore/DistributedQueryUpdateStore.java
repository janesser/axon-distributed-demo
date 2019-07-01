package org.axonframework.queryhandling.updatestore;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.*;
import org.axonframework.queryhandling.updatestore.model.QueryUpdateEntity;
import org.axonframework.queryhandling.updatestore.model.SubscriptionEntity;
import org.axonframework.queryhandling.updatestore.repository.QueryUpdateRepository;
import org.axonframework.queryhandling.updatestore.repository.SubscriptionRepository;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Slf4j
@SuppressWarnings("unchecked")
public class DistributedQueryUpdateStore implements QueryUpdateStore {

    @Autowired
    private SubscriberIdentityService identityService;

    @Autowired
    private Serializer messageSerializer;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private QueryUpdateRepository queryUpdateRepository;

    @Override
    public SubscriptionId buildIdFromQuery(SubscriptionQueryMessage<?, ?, ?> query) {
        return SubscriptionId.from(
                identityService.getSubscriberIdentify(),
                query,
                messageSerializer
        );
    }

    @Override
    public <Q, I, U> SubscriptionEntity<Q, I, U> getOrCreateSubscription(
            SubscriptionId id,
            Q payload,
            ResponseType<I> initialResponseType,
            ResponseType<U> updateResponseType) {
        return subscriptionRepository.getOrCreateSubscription(id, payload, initialResponseType, updateResponseType, messageSerializer);
    }

    @Override
    public boolean subscriptionExists(SubscriptionId id) {
        return subscriptionRepository.findById(id).isPresent();
    }

    @Override
    public void removeSubscription(SubscriptionId subscriptionId) {
        queryUpdateRepository.findBySubscriptionId(subscriptionId)
                .forEach(queryUpdateRepository::delete);

        subscriptionRepository.findById(subscriptionId)
                .ifPresent(subscriptionRepository::delete);
    }

    @Override
    public <Q, I, U> Stream<SubscriptionEntity<Q, I, U>> getSubscriptions(
            Predicate<SubscriptionEntity<Q, I, U>> filter) {
        return stream(subscriptionRepository.findAll().spliterator(), false)
                .filter(filter);
    }

    @Override
    public <U> void postUpdate(SubscriptionEntity subscription, SubscriptionQueryUpdateMessage<U> update) {
        log.debug("posting for nodeId: " + subscription + " update: " + update);
        queryUpdateRepository.save(new QueryUpdateEntity(subscription.getId(), update, messageSerializer));
    }

    @Override
    public <U> Optional<U> popUpdate(SubscriptionId subscriptionId) {
        Optional<QueryUpdateEntity> updateOpt = queryUpdateRepository
                .findBySubscriptionId(subscriptionId)
                .stream()
                .findFirst();
        updateOpt.ifPresent(queryUpdateRepository::delete);
        updateOpt.ifPresent(upt -> log.debug("Receiving update: " + upt));
        return updateOpt.map(que -> que.getPayload(messageSerializer));
    }
}

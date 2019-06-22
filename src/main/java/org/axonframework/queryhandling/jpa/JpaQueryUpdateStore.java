package org.axonframework.queryhandling.jpa;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.*;
import org.axonframework.queryhandling.jpa.model.QueryUpdateEntity;
import org.axonframework.queryhandling.jpa.model.SubscriptionEntity;
import org.axonframework.queryhandling.jpa.repository.QueryUpdateRepository;
import org.axonframework.queryhandling.jpa.repository.SubscriptionRepository;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/**
 * TODO explain underlying infra-structure
 * <p>
 * TODO establish clean-up
 * 1. overdue subscription
 * 2. stale updates
 */
@Slf4j
@Component
@Transactional
@SuppressWarnings("unchecked")
public class JpaQueryUpdateStore implements QueryUpdateStore {

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
    public void removeSubscription(SubscriptionId id) {
        Optional<SubscriptionEntity> subOpt = subscriptionRepository.findById(id);
        subOpt.ifPresent(sub -> {
            queryUpdateRepository.findBySubscription(sub)
                    .forEach(queryUpdateRepository::delete);

            subscriptionRepository.deleteById(id);
        });


    }

    @Override
    public <Q, I, U> Stream<SubscriptionEntity<Q, I, U>> getSubscriptions(
            Predicate<SubscriptionEntity<Q, I, U>> filter) {
        return stream(subscriptionRepository.findAll().spliterator(), false)
                .filter(filter);
    }

    @Override
    public <U> void postUpdate(SubscriptionEntity subscription, SubscriptionQueryUpdateMessage<U> update) {
        log.info("posting for nodeId: " + subscription + " update: " + update);
        queryUpdateRepository.save(new QueryUpdateEntity(subscription, update, messageSerializer));
    }

    @Override
    public <U> Optional<U> popUpdate(SubscriptionId subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).map(
                sub -> {
                    Optional<QueryUpdateEntity> updateOpt = queryUpdateRepository
                            .findBySubscription((SubscriptionEntity) sub)
                            .stream()
                            .findFirst();
                    updateOpt.ifPresent(queryUpdateRepository::delete);
                    updateOpt.ifPresent(upt -> log.info("Receiving update: " + upt));
                    return updateOpt.map(que -> que.getPayload(messageSerializer)).orElse(null);
                }
        );
    }
}

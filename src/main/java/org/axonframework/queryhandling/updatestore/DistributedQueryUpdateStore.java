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
    public <Q, I, U> SubscriptionEntity<Q, I, U> createSubscription(
            SubscriptionId id,
            Q payload,
            ResponseType<I> initialResponseType,
            ResponseType<U> updateResponseType) {
        return subscriptionRepository.createSubscription(id, payload, initialResponseType, updateResponseType, messageSerializer);
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
    public <Q, I, U> Iterable<SubscriptionEntity<Q, I, U>> getCurrentSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public <U> void postUpdate(SubscriptionEntity subscription, SubscriptionQueryUpdateMessage<U> update) {
        log.debug("posting for nodeId: {} update: {}", subscription, update);
        queryUpdateRepository.save(new QueryUpdateEntity(subscription.getId(), update, messageSerializer));
    }

    @Override
    public <U> Optional<U> popUpdate(SubscriptionId subscriptionId) {
        Optional<QueryUpdateEntity> updateOpt = queryUpdateRepository
                .findBySubscriptionId(subscriptionId)
                .stream()
                .findFirst();

        updateOpt.ifPresent(upt -> {
            log.debug("Receiving update: {}", upt);
            queryUpdateRepository.delete(upt);
        });

        return updateOpt.map(que -> que.getPayload(messageSerializer));
    }
}

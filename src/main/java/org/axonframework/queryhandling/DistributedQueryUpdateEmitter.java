package org.axonframework.queryhandling;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.queryhandling.jpa.model.SubscriptionEntity;
import org.axonframework.serialization.Serializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;

import javax.annotation.Resource;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @see SimpleQueryUpdateEmitter
 */
@Slf4j
@Component
public class DistributedQueryUpdateEmitter implements QueryUpdateEmitter {

    @Resource
    private QueryBus localSegment;

    @Resource
    private QueryUpdatePollingService queryUpdatePollingService;

    @Resource
    private QueryUpdateStore queryUpdateStore;

    @Resource
    private Serializer messageSerializer;

    @Override
    public <U> void emit(Predicate<SubscriptionQueryMessage<?, ?, U>> filter, SubscriptionQueryUpdateMessage<U> update) {
        /*
         * 1. filter subscriptions
         * 2. for each: persist update
         */
        Stream<SubscriptionEntity<Object, Object, U>> subscriptions =
                queryUpdateStore.getSubscriptionsFiltered(filter, messageSerializer);
        subscriptions.forEach(subscription ->
                queryUpdateStore.postUpdate(subscription, update)
        );

        localSegment.queryUpdateEmitter().emit(filter, update);
    }

    @Override
    public void complete(Predicate<SubscriptionQueryMessage<?, ?, ?>> filter) {
        localSegment.queryUpdateEmitter().complete(filter);
    }


    @Override
    public void completeExceptionally(Predicate<SubscriptionQueryMessage<?, ?, ?>> filter, Throwable cause) {
        localSegment.queryUpdateEmitter().completeExceptionally(filter, cause);
    }

    @Override
    public boolean queryUpdateHandlerRegistered(SubscriptionQueryMessage<?, ?, ?> query) {
        return queryUpdateStore.subscriptionExists(query);
    }

    @Override
    public <U> UpdateHandlerRegistration<U> registerUpdateHandler(SubscriptionQueryMessage<?, ?, ?> query, SubscriptionQueryBackpressure backpressure, int updateBufferSize) {
        /*
         * 1. persist subscription
         * 2. initialize flux
         * 3. poll on updates and feed them into the flux
         * 4. subscribe to local updates
         * 5. dispose all on Registration.cancel()
         */

        // Persist subscription
        SubscriptionEntity subscriptionEntity = queryUpdateStore.getOrCreateSubscription(query);


        // Initialize Flux
        EmitterProcessor<SubscriptionQueryUpdateMessage<U>> processor = EmitterProcessor.create(updateBufferSize);
        FluxSink<SubscriptionQueryUpdateMessage<U>> sink = processor.sink(backpressure.getOverflowStrategy());
        sink.onDispose(() -> queryUpdateStore.removeSubscription(query));
        FluxSinkWrapper<SubscriptionQueryUpdateMessage<U>> fluxSinkWrapper = new FluxSinkWrapper<>(sink);


        // Start polling for updates
        ScheduledFuture<?> scheduledFuture = queryUpdatePollingService.startPolling(
                SubscriptionId.from(subscriptionEntity),
                fluxSinkWrapper);

        // Subscribe to local updates
        UpdateHandlerRegistration<U> updateHandlerRegistration = localSegment.queryUpdateEmitter().registerUpdateHandler(query, backpressure, updateBufferSize);
        Disposable localSubscription = updateHandlerRegistration.getUpdates()
                .subscribe(fluxSinkWrapper::next);


        // Dispose all on Registration.cancel()
        Registration registration = () -> {
            boolean pollingCanceled =
                    scheduledFuture.cancel(true);
            fluxSinkWrapper.complete();

            localSubscription.dispose();
            boolean localRegistrationCanceled =
                    updateHandlerRegistration.getRegistration().cancel();

            return pollingCanceled && localRegistrationCanceled;
        };

        return new UpdateHandlerRegistration<>(registration,
                processor.replay(updateBufferSize).autoConnect());
    }

    @Override
    public Registration registerDispatchInterceptor(MessageDispatchInterceptor<? super SubscriptionQueryUpdateMessage<?>> dispatchInterceptor) {
        return localSegment.queryUpdateEmitter().registerDispatchInterceptor(dispatchInterceptor);
    }
}

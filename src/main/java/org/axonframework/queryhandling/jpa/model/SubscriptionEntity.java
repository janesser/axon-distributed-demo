package org.axonframework.queryhandling.jpa.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.GenericSubscriptionQueryMessage;
import org.axonframework.queryhandling.SubscriptionId;
import org.axonframework.queryhandling.SubscriptionQueryMessage;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

import static org.axonframework.queryhandling.SubscriptionId.hash;
import static org.axonframework.queryhandling.SubscriptionId.serialize;

@Entity
@IdClass(SubscriptionId.class)
@Data
@NoArgsConstructor
public class SubscriptionEntity<Q, I, U> {

    @Id
    String nodeId;

    @Id
    String queryPayloadHash;

    @Lob
    @Column(length = 8 * 1024)
    byte[] queryPayload;
    String queryPayloadType;
    String queryPayloadRevision;

    String queryInitialResponseType;
    String queryUpdateResponseType;

    Instant creationTime = Instant.now();

    public SubscriptionEntity(String nodeId,
                              Q queryPayload,
                              ResponseType<I> queryInitialResponseType,
                              ResponseType<U> queryUpdateResponseType,
                              Serializer serializer) {
        this.nodeId = nodeId;

        SerializedObject<byte[]> serializedPayload = serialize(queryPayload, serializer);
        this.queryPayload = serializedPayload.getData();
        this.queryPayloadType = serializedPayload.getType().getName();
        this.queryPayloadRevision = serializedPayload.getType().getRevision();

        this.queryPayloadHash = hash(this.queryPayload);

        this.queryInitialResponseType = queryInitialResponseType.responseMessagePayloadType().getName();
        this.queryUpdateResponseType = queryUpdateResponseType.responseMessagePayloadType().getName();
    }

    @SuppressWarnings("unchecked")
    @Transient
    public SubscriptionQueryMessage<Q, I, U> asSubscriptionQueryMessage(Serializer serializer) {
        try {
            Q payload = serializer.deserialize(
                    new SimpleSerializedObject<>(
                            queryPayload,
                            byte[].class,
                            queryPayloadType,
                            queryPayloadRevision)
            );
            return new GenericSubscriptionQueryMessage<>(
                    payload,
                    nodeId,
                    ResponseTypes.instanceOf((Class<I>) Class.forName(queryInitialResponseType)),
                    ResponseTypes.instanceOf((Class<U>) Class.forName(queryUpdateResponseType))
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

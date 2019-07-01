package org.axonframework.queryhandling.updatestore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.queryhandling.GenericSubscriptionQueryUpdateMessage;
import org.axonframework.queryhandling.SubscriptionId;
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.*;
import java.time.Instant;

/**
 * @see org.axonframework.eventhandling.AbstractEventEntry
 */
@Entity
@RedisHash("queryUpdate")
@Data
@NoArgsConstructor
public class QueryUpdateEntity {

    public static <U> SubscriptionQueryUpdateMessage<U> asSubscriptionQueryUpdateMessage(U payload) {
        return new GenericSubscriptionQueryUpdateMessage<>(payload);
    }

    @Id
    @GeneratedValue(generator = "update-uuid")
    @GenericGenerator(name = "update-uuid", strategy = "uuid")
    private String id;

    // omit all kind of EntityGraph
    // @ManyToOne(targetEntity = SubscriptionEntity.class)
    @Indexed
    private SubscriptionId subscriptionId;

    @Lob
    @Column(length = 16 * 1024)
    private byte[] updatePayload;
    private String updatePayloadType;
    private String updatePayloadRevision;

    private Instant creationTime = Instant.now();

    public QueryUpdateEntity(SubscriptionId subscriptionId, SubscriptionQueryUpdateMessage<?> updateMessage, Serializer serializer) {
        this.subscriptionId = subscriptionId;

        SerializedObject<byte[]> serializePayload = updateMessage.serializePayload(serializer, byte[].class);
        this.updatePayload = serializePayload.getData();
        this.updatePayloadType = serializePayload.getType().getName();
        this.updatePayloadRevision = serializePayload.getType().getRevision();
    }

    @Transient
    public <U> U getPayload(Serializer serializer) {
        SerializedObject<byte[]> sso = new SimpleSerializedObject<>(
                updatePayload,
                byte[].class,
                updatePayloadType,
                updatePayloadRevision
        );
        return serializer.deserialize(sso);
    }
}
